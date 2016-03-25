package org.rabix.engine.processor.handler.impl;

import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.event.impl.OutputUpdateEvent;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;

import com.google.inject.Inject;

public class JobStatusEventHandler implements EventHandler<JobStatusEvent> {

  private final JobRecordService jobRecordService;
  private final EventProcessor eventProcessor;

  @Inject
  public JobStatusEventHandler(final JobRecordService jobRecordService, final EventProcessor eventProcessor) {
    this.eventProcessor = eventProcessor;
    this.jobRecordService = jobRecordService;
  }

  @Override
  public void handle(JobStatusEvent event) throws EventHandlerException {
    JobRecord jobRecord = jobRecordService.find(event.getJobId(), event.getContextId());

    switch (event.getState()) {
    case RUNNING:
      jobRecord.setState(JobState.RUNNING);
      jobRecordService.update(jobRecord);
      break;
    case COMPLETED:
      for (PortCounter portCounter : jobRecord.getOutputCounters()) {
        Object output = event.getResult().get(portCounter.getPort());
        eventProcessor.addToQueue(new OutputUpdateEvent(jobRecord.getContextId(), jobRecord.getId(), portCounter.getPort(), output));
      }
      break;
    case FAILED:
      eventProcessor.addToQueue(new ContextStatusEvent(event.getContextId(), ContextStatus.FAILED));
      break;
    default:
      break;
    }
  }

}
