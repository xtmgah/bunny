package org.rabix.engine.processor.dispatcher;

import org.rabix.engine.processor.dispatcher.impl.AsyncEventDispatcher;
import org.rabix.engine.processor.dispatcher.impl.SyncEventDispatcher;

import com.google.inject.Inject;

public class EventDispatcherFactory {

  private final SyncEventDispatcher syncEventDispatcher;
  private final AsyncEventDispatcher asyncEventDispatcher;
  
  @Inject
  public EventDispatcherFactory(SyncEventDispatcher syncEventDispatcher, AsyncEventDispatcher asyncEventDispatcher) {
    this.syncEventDispatcher = syncEventDispatcher;
    this.asyncEventDispatcher = asyncEventDispatcher;
  }

  public EventDispatcher create(EventDispatcher.Type type) {
    switch (type) {
      case ASYNC:
        return asyncEventDispatcher;
      case SYNC:
        return syncEventDispatcher;
      default:
        throw new RuntimeException("Failed to create EventDispacther " + type);
    }
  }

}
