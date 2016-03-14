package org.rabix.engine.processor.handler.impl;

import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode.LinkMerge;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.OutputUpdateEvent;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.JobService.JobState;
import org.rabix.engine.service.LinkService;
import org.rabix.engine.service.VariableService;

import com.google.inject.Inject;

/**
 * Handles {@link OutputUpdateEvent} events.
 */
public class OutputEventHandler implements EventHandler<OutputUpdateEvent> {

  private JobService jobService;
  private VariableService variableService;
  private LinkService linkService;
  
  private final EventProcessor eventProcessor;
  
  @Inject
  public OutputEventHandler(EventProcessor eventProcessor, JobService jobService, VariableService variableService, LinkService linkService) {
    this.jobService = jobService;
    this.linkService = linkService;
    this.variableService = variableService;
    this.eventProcessor = eventProcessor;
  }

  public void handle(final OutputUpdateEvent event) throws EventHandlerException {
    VariableRecord sourceVariable = variableService.find(event.getJobId(), event.getPortId(), LinkPortType.OUTPUT, event.getContextId());
    sourceVariable.addValue(event.getValue(), LinkMerge.merge_nested);
    
    JobRecord sourceJob = jobService.find(event.getJobId(), event.getContextId());
    if (event.isFromScatter()) {
      sourceJob.resetOutputPortCounters(event.getScatteredNodes());
    }
    sourceJob.decrementPortCounter(event.getPortId(), LinkPortType.OUTPUT);
    
    if (sourceJob.isCompleted()) {
      sourceJob.setState(JobState.COMPLETED);
      jobService.update(sourceJob);
      
      if (sourceJob.isMaster()) {
        eventProcessor.addToQueue(new ContextStatusEvent(event.getContextId(), ContextStatus.COMPLETED));
      }
    }
    
    if (sourceJob.isScatterWrapper()) {
      dispatchLookAheadEvents(sourceJob, sourceVariable, event);
      return;
    }
    
    if (sourceJob.isOutputPortReady(event.getPortId())) {
      dispatchReadyOutputs(sourceJob, sourceVariable, event);
    }
  }
  
  /**
   * Dispatch look-ahead events 
   */
  private void dispatchLookAheadEvents(JobRecord sourceJob, VariableRecord sourceVariable, OutputUpdateEvent event) throws EventHandlerException {
    List<LinkRecord> links = linkService.findBySource(sourceVariable.getJobId(), sourceVariable.getPortId(), event.getContextId());
    dispatchEvents(sourceJob, sourceVariable, links, event);
  }
  
  /**
   * Dispatch ready outputs 
   */
  private void dispatchReadyOutputs(JobRecord sourceJob, VariableRecord sourceVariable, OutputUpdateEvent event) throws EventHandlerException {
    List<LinkRecord> links = linkService.findBySource(event.getJobId(), event.getPortId(), event.getContextId());
    dispatchEvents(sourceJob, sourceVariable, links, event);
  }
  
  /**
   * Dispatch other INPUT and OUTPUT events 
   */
  private void dispatchEvents(JobRecord sourceJob, VariableRecord sourceVariable, List<LinkRecord> links, OutputUpdateEvent event) throws EventHandlerException {
    for (LinkRecord link : links) {
      List<VariableRecord> destinationVariables = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), event.getContextId());
      
      boolean isFromScatter = false;
      Integer numberOfOutputs = null;
      Object value = event.getValue();
      
      if (sourceJob.isScattered()) {
        isFromScatter = true;
        numberOfOutputs = sourceVariable.getNumberOfGlobals();
        value = sourceVariable.getValue();
      } else if (sourceJob.isScatterWrapper()) {
        isFromScatter = true;
        numberOfOutputs = sourceJob.getNumberOfGlobalOutputs();
        value = event.getValue();
      }
      
      for (VariableRecord destinationVariable : destinationVariables) {
        switch (destinationVariable.getType()) {
        case INPUT:
          JobRecord destinationJob = jobService.find(destinationVariable.getJobId(), event.getContextId());
          boolean isDestinationPortScatterable = destinationJob.isScatterPort(destinationVariable.getPortId());
          if (!isDestinationPortScatterable && !event.isFromScatter()) {
            value = sourceVariable.getValue();
          }
          Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, isFromScatter, isFromScatter, numberOfOutputs);
          eventProcessor.send(updateInputEvent);
          break;
        default:
          Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, isFromScatter, numberOfOutputs);
          eventProcessor.send(updateOutputEvent);
          break;
        }
      }
    }
  }
  
}
