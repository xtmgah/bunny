package org.rabix.engine.processor.dispatcher.impl;

import org.rabix.engine.event.Event;
import org.rabix.engine.processor.dispatcher.EventDispatcher;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.processor.handler.HandlerFactory;

import com.google.inject.Inject;

public class SyncEventDispatcher implements EventDispatcher {

  private final HandlerFactory handlerFactory;

  @Inject
  public SyncEventDispatcher(HandlerFactory handlerFactory) {
    this.handlerFactory = handlerFactory;
  }
  
  @Override
  public void send(Event event) throws EventHandlerException {
    handlerFactory.get(event.getType()).handle(event);
  }

  @Override
  public Type getType() {
    return Type.SYNC;
  }

}
