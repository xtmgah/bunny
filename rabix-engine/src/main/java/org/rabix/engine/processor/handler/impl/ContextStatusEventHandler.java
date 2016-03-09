package org.rabix.engine.processor.handler.impl;

import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.ContextService;

import com.google.inject.Inject;

public class ContextStatusEventHandler implements EventHandler<ContextStatusEvent> {

  private final ContextService contextService;

  @Inject
  public ContextStatusEventHandler(ContextService contextService) {
    this.contextService = contextService;
  }
  
  @Override
  public void handle(ContextStatusEvent event) throws EventHandlerException {
    ContextRecord contextRecord = contextService.find(event.getContextId());
    contextRecord.setStatus(event.getStatus());
    contextService.update(contextRecord);
  }

}
