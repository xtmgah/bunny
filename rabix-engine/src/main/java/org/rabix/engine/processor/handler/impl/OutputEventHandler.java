package org.rabix.engine.processor.handler.impl;

import java.util.List;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.OutputUpdateEvent;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;

import com.google.inject.Inject;

/**
 * Handles {@link OutputUpdateEvent} events.
 */
public class OutputEventHandler implements EventHandler<OutputUpdateEvent> {

  private JobRecordService jobService;
  private VariableRecordService variableService;
  private LinkRecordService linkService;
  
  private final EventProcessor eventProcessor;
  
  @Inject
  public OutputEventHandler(EventProcessor eventProcessor, JobRecordService jobService, VariableRecordService variableService, LinkRecordService linkService) {
    this.jobService = jobService;
    this.linkService = linkService;
    this.variableService = variableService;
    this.eventProcessor = eventProcessor;
  }

  public void handle(final OutputUpdateEvent event) throws EventHandlerException {
    VariableRecord sourceVariable = variableService.find(event.getJobId(), event.getPortId(), LinkPortType.OUTPUT, event.getContextId());
    sourceVariable.addValue(event.getValue(), LinkMerge.merge_nested, event.getPosition());
    
    JobRecord sourceJob = jobService.find(event.getJobId(), event.getContextId());
    if (event.isFromScatter()) {
      sourceJob.resetOutputPortCounters(event.getNumberOfScattered());
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
      
      boolean isLookAhead = sourceJob.isScattered() || sourceJob.isScatterWrapper();
      Integer numberOfOutputs = null;
      Object value = event.getValue();
      
      if (sourceJob.isScattered()) {
        numberOfOutputs = sourceVariable.getNumberOfGlobals();
        value = sourceVariable.getValue();
      } else if (sourceJob.isScatterWrapper()) {
        numberOfOutputs = sourceJob.getNumberOfGlobalOutputs();
        value = event.getValue();
      }
      
      Integer position = null;
      for (VariableRecord destinationVariable : destinationVariables) {
        switch (destinationVariable.getType()) {
        case INPUT:
          JobRecord destinationJob = jobService.find(destinationVariable.getJobId(), event.getContextId());
          boolean isDestinationPortScatterable = destinationJob.isScatterPort(destinationVariable.getPortId());
          if (!isDestinationPortScatterable && !event.isFromScatter()) {
            value = sourceVariable.getValue();
          }
          position = link.getPosition();
          if (isLookAhead) {
            position = position * event.getPosition();
          }
          Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, isLookAhead, numberOfOutputs, position);
          eventProcessor.send(updateInputEvent);
          break;
        default:
          position = InternalSchemaHelper.getScatteredNumber(sourceJob.getId());
          position = position != null ? position : 1;
          Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, isLookAhead, numberOfOutputs, position);
          eventProcessor.send(updateOutputEvent);
          break;
        }
      }
    }
  }
  
}
