package org.rabix.engine.rest.transport.impl;

import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.rest.backend.impl.BackendLocal;
import org.rabix.engine.rest.transport.TransportPlugin;
import org.rabix.engine.rest.transport.TransportPluginType;

public class TransportPluginLocal implements TransportPlugin {

  private BackendLocal backendLocal;

  public TransportPluginLocal(BackendLocal backendLocal) {
    this.backendLocal = backendLocal;
  }

  @Override
  public <T> ResultPair<T> send(String destinationQueue, T entity) {
    backendLocal.getQueue(destinationQueue).add(BeanSerializer.serializeFull(entity));
    return ResultPair.<T> success(null);
  }

  @Override
  public <T> ResultPair<T> receive(String sourceQueue, Class<T> clazz) {
    String payload = backendLocal.getQueue(sourceQueue).poll();
    if (payload != null) {
      return ResultPair.<T> success(BeanSerializer.deserialize(payload, clazz));
    }
    return ResultPair.<T> fail(null, null);
  }

  @Override
  public TransportPluginType getType() {
    return TransportPluginType.LOCAL;
  }

}
