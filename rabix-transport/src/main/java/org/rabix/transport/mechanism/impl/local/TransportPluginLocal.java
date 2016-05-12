package org.rabix.transport.mechanism.impl.local;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.VMQueues;
import org.rabix.common.json.BeanSerializer;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.TransportPluginType;

public class TransportPluginLocal implements TransportPlugin<TransportQueueLocal> {

  public TransportPluginLocal(Configuration configuration) throws TransportPluginException {
  }

  @Override
  public <T> ResultPair<T> send(TransportQueueLocal destinationQueue, T entity) {
    try {
      VMQueues.getQueue(destinationQueue.getQueue()).put(BeanSerializer.serializeFull(entity));
      return ResultPair.<T> success(null);
    } catch (InterruptedException e) {
      return ResultPair.<T>fail(e, "Failed to put to queue " + destinationQueue);
    }
  }

  @Override
  public <T> ResultPair<T> receive(TransportQueueLocal sourceQueue, Class<T> clazz, ReceiveCallback<T> receiveCallback) {
    String payload;
    try {
      payload = VMQueues.<String>getQueue(sourceQueue.getQueue()).take();
      receiveCallback.handleReceive(BeanSerializer.deserialize(payload, clazz));
      return ResultPair.success(null);
    } catch (InterruptedException e) {
      return ResultPair.<T>fail(e, "Failed to receive a message from " + sourceQueue);
    }
  }

  @Override
  public TransportPluginType getType() {
    return TransportPluginType.LOCAL;
  }

}
