package org.rabix.engine.processor.handler.impl;

import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService.JobState;

import com.google.inject.Inject;

public class ContextStatusEventHandler implements EventHandler<ContextStatusEvent> {

  private final EventProcessor eventProcessor;
  private final ContextRecordService contextRecordService;

  @Inject
  public ContextStatusEventHandler(ContextRecordService contextRecordService, EventProcessor eventProcessor) {
    this.eventProcessor = eventProcessor;
    this.contextRecordService = contextRecordService;
  }
  
  @Override
  public void handle(ContextStatusEvent event) throws EventHandlerException {
    if (event.getStatus().equals(ContextStatus.FAILED)) {
      eventProcessor.send(new JobStatusEvent("root", event.getContextId(), JobState.FAILED, null));
    }
    
    ContextRecord contextRecord = contextRecordService.find(event.getContextId());
    contextRecord.setStatus(event.getStatus());
    contextRecordService.update(contextRecord);
  }

}
