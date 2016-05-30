package org.rabix.engine.processor.handler.impl;

import java.util.List;

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
import org.rabix.engine.service.scatter.strategy.ScatterStrategyHandler;
import org.rabix.engine.service.scatter.strategy.ScatterStrategyHandlerFactory;
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
  private final ScatterStrategyHandlerFactory scatterStrategyHandlerFactory;
  
  @Inject
  public OutputEventHandler(EventProcessor eventProcessor, JobRecordService jobService, VariableRecordService variableService, LinkRecordService linkService, ScatterStrategyHandlerFactory scatterStrategyHandlerFactory) {
    this.jobService = jobService;
    this.linkService = linkService;
    this.variableService = variableService;
    this.eventProcessor = eventProcessor;
    this.scatterStrategyHandlerFactory = scatterStrategyHandlerFactory;
  }

  public void handle(final OutputUpdateEvent event) throws EventHandlerException {
    JobRecord sourceJob = jobService.find(event.getJobId(), event.getContextId());
    if (event.isFromScatter()) {
      jobService.resetOutputPortCounters(sourceJob, event.getNumberOfScattered());
    }
    VariableRecord sourceVariable = variableService.find(event.getJobId(), event.getPortId(), LinkPortType.OUTPUT, event.getContextId());
    jobService.decrementPortCounter(sourceJob, event.getPortId(), LinkPortType.OUTPUT);
    variableService.addValue(sourceVariable, event.getValue(), event.getPosition());
    jobService.update(sourceJob);
    
    boolean isCompleted = jobService.isCompleted(sourceJob);
    if (isCompleted) {
      sourceJob.setState(JobState.COMPLETED);
      jobService.update(sourceJob);
      if (sourceJob.isRoot()) {
        eventProcessor.addToQueue(new ContextStatusEvent(event.getContextId(), ContextStatus.COMPLETED));
      }
    }
    
    if (sourceJob.isScatterWrapper()) {
      processScatterWrapper(sourceJob, sourceVariable, event);
      return;
    }
    
    boolean isOutputPortReady = jobService.isOutputPortReady(sourceJob, event.getPortId());
    if (isOutputPortReady) {
      processReadyOutputPort(sourceJob, sourceVariable, event);
    }
  }
  
  private void processScatterWrapper(JobRecord sourceJob, VariableRecord sourceVariable, OutputUpdateEvent event) throws EventHandlerException {
    ScatterStrategyHandler scatterStrategyHandler = scatterStrategyHandlerFactory.create(sourceJob.getScatterStrategy().getScatterMethod());
    
    Object value = null;
    if (scatterStrategyHandler.isBlocking()) {
      boolean isOutputPortReady = jobService.isOutputPortReady(sourceJob, event.getPortId());
      if (isOutputPortReady) {
        value = scatterStrategyHandler.values(sourceJob.getScatterStrategy(), sourceJob.getId(), event.getPortId(), event.getContextId());
      } else {
        return;
      }
    }
    
    List<LinkRecord> links = linkService.findBySource(sourceVariable.getJobId(), sourceVariable.getPortId(), event.getContextId());
    for (LinkRecord link : links) {
      List<VariableRecord> destinationVariables = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), event.getContextId());

      JobRecord destinationJob = null;
      boolean isDestinationPortScatterable = false;
      for (VariableRecord destinationVariable : destinationVariables) {
        switch (destinationVariable.getType()) {
        case INPUT:
          destinationJob = jobService.find(destinationVariable.getJobId(), destinationVariable.getContextId());
          isDestinationPortScatterable = jobService.isScatterPort(destinationJob, destinationVariable.getPortId());
          int numberOfDestinationIncomingLinks = jobService.getInputPortIncoming(destinationJob, event.getPortId());
          if (isDestinationPortScatterable && !destinationJob.isBlocking() && numberOfDestinationIncomingLinks <= 1) {
            value = value != null ? value : event.getValue();
            int numberOfScattered = sourceJob.getGlobalOutputsCount();
            Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, true, numberOfScattered, event.getPosition());
            eventProcessor.send(updateInputEvent);
          } else {
            boolean isOutputPortReady = jobService.isOutputPortReady(sourceJob, event.getPortId());
            if (isOutputPortReady) {
              value = value != null ? value : variableService.transformValue(sourceVariable);
              Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition());
              eventProcessor.send(updateInputEvent);
            }
          }
          break;
        case OUTPUT:
          destinationJob = jobService.find(destinationVariable.getJobId(), destinationVariable.getContextId());
          int numberOfOutputPortIncomingLinks = jobService.getOutputPortIncoming(destinationJob, event.getPortId());
          if (numberOfOutputPortIncomingLinks <= 1) {
            value = value != null? value : event.getValue();
            int numberOfScattered = sourceJob.getGlobalOutputsCount();
            if (scatterStrategyHandler.isBlocking()) {
              Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, false, 1, 1);
              eventProcessor.send(updateOutputEvent);
            } else {
              Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, true, numberOfScattered, event.getPosition());
              eventProcessor.send(updateOutputEvent);
            }
          } else {
            boolean isOutputPortReady = jobService.isOutputPortReady(sourceJob, event.getPortId());
            if (isOutputPortReady) {
              value = value != null? value : variableService.transformValue(sourceVariable);
              Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition());
              eventProcessor.send(updateInputEvent);
            }
          }
          break;
        }
      }
    }
  }
  
  private void processReadyOutputPort(JobRecord sourceJob, VariableRecord sourceVariable, OutputUpdateEvent event) throws EventHandlerException {
    List<LinkRecord> links = linkService.findBySource(event.getJobId(), event.getPortId(), event.getContextId());
    for (LinkRecord link : links) {
      List<VariableRecord> destinationVariables = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), event.getContextId());

      Object value = variableService.transformValue(sourceVariable);
      for (VariableRecord destinationVariable : destinationVariables) {
        switch (destinationVariable.getType()) {
        case INPUT:
          Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition());
          eventProcessor.send(updateInputEvent);
          break;
        case OUTPUT:
          if (sourceJob.isScattered()) {
            int numberOfScattered = sourceJob.getGlobalOutputsCount();
            int position = InternalSchemaHelper.getScatteredNumber(sourceJob.getId());
            Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, true, numberOfScattered, position);
            eventProcessor.send(updateOutputEvent);
          } else {
            Event updateOutputEvent = new OutputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition());
            eventProcessor.send(updateOutputEvent);
          }
          break;
        }
      }
    }
  }
  
}
