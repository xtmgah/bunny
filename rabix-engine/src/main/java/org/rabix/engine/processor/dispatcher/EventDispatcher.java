package org.rabix.engine.processor.dispatcher;

import org.rabix.engine.event.Event;
import org.rabix.engine.processor.handler.EventHandlerException;

public interface EventDispatcher {

  public static enum Type {
    ASYNC, SYNC
  }
  
  void send(Event event) throws EventHandlerException;
  
  Type getType();
  
}
