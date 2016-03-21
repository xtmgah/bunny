package org.rabix.engine.processor.handler.impl;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.event.impl.OutputUpdateEvent;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.JobService.JobState;

import com.google.inject.Inject;

public class JobStatusEventHandler implements EventHandler<JobStatusEvent> {

  private final JobService jobService;
  private final EventProcessor eventProcessor;

  @Inject
  public JobStatusEventHandler(final JobService jobService, final EventProcessor eventProcessor) {
    this.jobService = jobService;
    this.eventProcessor = eventProcessor;
  }

  @Override
  public void handle(JobStatusEvent event) throws EventHandlerException {
    JobRecord job = jobService.find(event.getJobId(), event.getContextId());
    
    switch (event.getState()) {
      case RUNNING:
        job.setState(JobState.RUNNING);
        jobService.update(job);
        break;
      case COMPLETED:
        try {
          Bindings bindings = BindingsFactory.create(event.getProtocolType());

          for (PortCounter portCounter : job.getOutputCounters()) {
            Object output = bindings.getOutputValueById(event.getResult(), portCounter.getPort());
            eventProcessor.addToQueue(new OutputUpdateEvent(job.getContextId(), job.getId(), portCounter.getPort(), output));
          }
        } catch (BindingException e) {
          throw new EventHandlerException(e);
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
