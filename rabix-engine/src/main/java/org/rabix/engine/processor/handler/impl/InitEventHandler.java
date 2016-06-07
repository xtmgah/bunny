package org.rabix.engine.processor.handler.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.CloneHelper;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.model.DAGNodeRecord.DAGNodeGraph;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.DAGNodeGraphService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.EngineServiceException;
import org.rabix.engine.service.VariableRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Handles {@link InitEvent} events.
 */
public class InitEventHandler implements EventHandler<InitEvent> {

  private final static Logger logger = LoggerFactory.getLogger(InitEventHandler.class);
  
  private DAGNodeGraphService dagNodeService;
  private EventProcessor eventProcessor;
  private JobRecordService jobRecordService;
  private ContextRecordService contextRecordService;
  private VariableRecordService variableRecordService;

  @Inject
  public InitEventHandler(EventProcessor eventProcessor, JobRecordService jobRecordService,
      VariableRecordService variableRecordService, ContextRecordService contextRecordService,
      DAGNodeGraphService dagNodeService) {
    this.dagNodeService = dagNodeService;
    this.eventProcessor = eventProcessor;
    this.jobRecordService = jobRecordService;
    this.contextRecordService = contextRecordService;
    this.variableRecordService = variableRecordService;
  }

  @Transactional
  public void handle(final InitEvent event) throws EventHandlerException {
    try {
      ContextRecord context = new ContextRecord(event.getRootId(), event.getContext().getConfig(), ContextStatus.RUNNING);

      contextRecordService.create(context);
      DAGNodeGraph node = dagNodeService.insert(event.getNode(), event.getContextId());
      JobRecord job = new JobRecord(event.getContextId(), event.getNode().getId(), event.getContextId(), null, JobState.PENDING, node.isContainer(), false, false);

      for (DAGLinkPort inputPort : node.getInputPorts()) {
        if (job.getState().equals(JobState.PENDING)) {
          jobRecordService.incrementPortCounter(job, inputPort, LinkPortType.INPUT);
        }
        Object defaultValue = node.getDefaults().get(inputPort.getId());
        VariableRecord variable = new VariableRecord(event.getContextId(), event.getNode().getId(), inputPort.getId(), LinkPortType.INPUT, defaultValue, node.getLinkMerge(inputPort.getId(), inputPort.getType()));
        variableRecordService.create(variable);
      }

      for (DAGLinkPort outputPort : node.getOutputPorts()) {
        jobRecordService.incrementPortCounter(job, outputPort, LinkPortType.OUTPUT);

        VariableRecord variable = new VariableRecord(event.getContextId(), event.getNode().getId(), outputPort.getId(), LinkPortType.OUTPUT, null, node.getLinkMerge(outputPort.getId(), outputPort.getType()));
        variableRecordService.create(variable);
      }
      jobRecordService.create(job);

      if (node.getInputPorts().isEmpty()) {
        // the node is ready
        eventProcessor.send(new JobStatusEvent(job.getId(), event.getContextId(), JobState.READY, null));
        return;
      }

      Map<String, Object> mixedInputs = mixInputs(node, event.getValue());
      for (DAGLinkPort inputPort : node.getInputPorts()) {
        Object value = mixedInputs.get(inputPort.getId());
        eventProcessor.send(new InputUpdateEvent(event.getContextId(), event.getNode().getId(), inputPort.getId(), value, 1));
      }
    } catch (EngineServiceException e) {
      logger.error("Failed to handle InitEvent " + event, e);
      throw new EventHandlerException("Failed to handle InitEvent " + event, e);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> mixInputs(DAGNodeGraph dagNode, Map<String, Object> inputs) {
    Map<String, Object> mixedInputs;
    try {
      mixedInputs = (Map<String, Object>) CloneHelper.deepCopy(dagNode.getDefaults());
      if (inputs == null) {
        return mixedInputs;
      }
      for (Entry<String, Object> inputEntry : inputs.entrySet()) {
        mixedInputs.put(inputEntry.getKey(), inputEntry.getValue());
      }
      return mixedInputs;
    } catch (Exception e) {
      throw new RuntimeException("Failed to clone default inputs for node " + dagNode.getId());
    }
  }

}
