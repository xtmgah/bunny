package org.rabix.engine.processor.handler.impl;

import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.EngineServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ContextStatusEventHandler implements EventHandler<ContextStatusEvent> {

  private final static Logger logger = LoggerFactory.getLogger(ContextStatusEventHandler.class);
  
  private final ContextRecordService contextRecordService;

  @Inject
  public ContextStatusEventHandler(ContextRecordService contextRecordService) {
    this.contextRecordService = contextRecordService;
  }

  @Override
  @Transactional
  public void handle(ContextStatusEvent event) throws EventHandlerException {
    try {
      ContextRecord contextRecord = contextRecordService.find(event.getContextId());
      contextRecord.setStatus(event.getStatus());
      contextRecordService.update(contextRecord);
    } catch (EngineServiceException e) {
      logger.error("Failed to handle ContextStatusEvent " + event, e);
      throw new EventHandlerException("Failed to handle ContextStatusEvent " + event, e);
    }
  }

}
