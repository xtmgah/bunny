package org.rabix.engine.processor.dispatcher.impl;

import org.rabix.engine.event.Event;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.dispatcher.EventDispatcher;

import com.google.inject.Inject;

public class AsyncEventDispatcher implements EventDispatcher {

  private final EventProcessor engine;

  @Inject
  public AsyncEventDispatcher(EventProcessor engine) {
    this.engine = engine;
  }
  
  @Override
  public void send(Event event) {
    engine.addToQueue(event);
  }

  @Override
  public Type getType() {
    return Type.ASYNC;
  }

}
