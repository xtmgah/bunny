package org.rabix.transport.mechanism.impl;

import org.rabix.common.VMQueues;
import org.rabix.common.json.BeanSerializer;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPluginType;
import org.rabix.transport.mechanism.TransportQueueLocal;

public class TransportPluginLocal implements TransportPlugin<TransportQueueLocal> {

  public TransportPluginLocal() {
  }

  @Override
  public <T> ResultPair<T> send(TransportQueueLocal destinationQueue, T entity) {
    VMQueues.getQueue(destinationQueue.getQueue()).add(BeanSerializer.serializeFull(entity));
    return ResultPair.<T> success(null);
  }

  @Override
  public <T> ResultPair<T> receive(TransportQueueLocal sourceQueue, Class<T> clazz) {
    String payload = VMQueues.<String>getQueue(sourceQueue.getQueue()).poll();
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
