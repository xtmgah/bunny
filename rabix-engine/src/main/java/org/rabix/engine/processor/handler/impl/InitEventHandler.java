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
import org.rabix.engine.service.ContextService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.JobService.JobState;
import org.rabix.engine.service.VariableService;

import com.google.inject.Inject;

/**
 * Handles {@link InitEvent} events.
 */
public class InitEventHandler implements EventHandler<InitEvent> {

  private DAGNodeDB nodeDB;
  private JobService jobService;
  private EventProcessor eventProcessor;
  private VariableService variableService;
  private ContextService contextService;

  @Inject
  public InitEventHandler(EventProcessor eventProcessor, JobService jobService, VariableService variableService, ContextService contextService, DAGNodeDB nodeDB) {
    this.nodeDB = nodeDB;
    this.jobService = jobService;
    this.eventProcessor = eventProcessor;
    this.variableService = variableService;
    this.contextService = contextService;
  }

  public void handle(final InitEvent event) throws EventHandlerException {
    ContextRecord context = new ContextRecord(event.getContext().getId(), event.getContext().getConfig(), ContextStatus.RUNNING);
    
    contextService.create(context);
    nodeDB.loadDB(event.getNode(), event.getContextId());
    
    DAGNode node = nodeDB.get(event.getNode().getId(), event.getContextId());
    JobRecord job = new JobRecord(event.getContextId(), event.getNode().getId(), event.getContextId(), JobState.PENDING, node instanceof DAGContainer, false, true);

    for (DAGLinkPort inputPort : node.getInputPorts()) {
      if (job.getState().equals(JobState.PENDING)) {
        job.incrementPortCounter(inputPort, LinkPortType.INPUT);
      }

      VariableRecord variable = new VariableRecord(event.getContextId(), event.getNode().getId(), inputPort.getId(), LinkPortType.INPUT, null);
      variableService.create(variable);
    }

    for (DAGLinkPort outputPort : node.getOutputPorts()) {
      job.incrementPortCounter(outputPort, LinkPortType.OUTPUT);

      VariableRecord variable = new VariableRecord(event.getContextId(), event.getNode().getId(), outputPort.getId(), LinkPortType.OUTPUT, null);
      variableService.create(variable);
    }
    jobService.create(job);

    
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
