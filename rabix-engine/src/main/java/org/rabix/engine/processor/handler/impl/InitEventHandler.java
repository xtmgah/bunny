package org.rabix.engine.processor.handler.impl;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.VariableRecordService;

import com.google.inject.Inject;

/**
 * Handles {@link InitEvent} events.
 */
public class InitEventHandler implements EventHandler<InitEvent> {

  private DAGNodeDB nodeDB;
  private EventProcessor eventProcessor;
  private JobRecordService jobRecordService;
  private ContextRecordService contextRecordService;
  private VariableRecordService variableRecordService;

  @Inject
  public InitEventHandler(EventProcessor eventProcessor, JobRecordService jobRecordService, VariableRecordService variableRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB) {
    this.nodeDB = dagNodeDB;
    this.eventProcessor = eventProcessor;
    this.jobRecordService = jobRecordService;
    this.contextRecordService = contextRecordService;
    this.variableRecordService = variableRecordService;
  }

  public void handle(final InitEvent event) throws EventHandlerException {
    ContextRecord context = new ContextRecord(event.getContext().getId(), event.getContext().getConfig(), ContextStatus.RUNNING);
    
    contextRecordService.create(context);
    nodeDB.loadDB(event.getNode(), event.getContextId());
    
    DAGNode node = nodeDB.get(event.getNode().getId(), event.getContextId());
    JobRecord job = new JobRecord(event.getContextId(), event.getNode().getId(), event.getContextId(), JobState.PENDING, node instanceof DAGContainer, false, true);

    for (DAGLinkPort inputPort : node.getInputPorts()) {
      if (job.getState().equals(JobState.PENDING)) {
        job.incrementPortCounter(inputPort, LinkPortType.INPUT);
      }

      VariableRecord variable = new VariableRecord(event.getContextId(), event.getNode().getId(), inputPort.getId(), LinkPortType.INPUT, null);
      variableRecordService.create(variable);
    }

    for (DAGLinkPort outputPort : node.getOutputPorts()) {
      job.incrementPortCounter(outputPort, LinkPortType.OUTPUT);

      VariableRecord variable = new VariableRecord(event.getContextId(), event.getNode().getId(), outputPort.getId(), LinkPortType.OUTPUT, null);
      variableRecordService.create(variable);
    }
    jobRecordService.create(job);

    
    try {
      Bindings bindings = BindingsFactory.create(node.getApp());
      
      for (DAGLinkPort inputPort : node.getInputPorts()) {
        Object value = bindings.getInputValueById(event.getValue(), inputPort.getId());
        Event updateInputEvent = new InputUpdateEvent(event.getContextId(), event.getNode().getId(), inputPort.getId(), value);
        eventProcessor.send(updateInputEvent);
      }
    } catch (BindingException e) {
      throw new EventHandlerException("Failed to create Bindings", e);
    }
  }

}
