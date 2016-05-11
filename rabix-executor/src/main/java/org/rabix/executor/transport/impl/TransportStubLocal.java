package org.rabix.executor.transport.impl;

import org.rabix.common.VMQueues;
import org.rabix.common.json.BeanSerializer;
import org.rabix.executor.transport.TransportStub;

public class TransportStubLocal implements TransportStub {

  public <T> ResultPair<T> send(String destinationQueue, T entity) {
    String payload = BeanSerializer.serializeFull(entity);
    VMQueues.getQueue(destinationQueue).add(payload);
    return ResultPair.<T> success(null);
  }

  public <T> ResultPair<T> receive(String sourceQueue, Class<T> clazz) {
    String result = VMQueues.<String>getQueue(sourceQueue).poll();
    if (result == null) {
      return ResultPair.<T> fail(null, null);
    }
    return ResultPair.<T> success(BeanSerializer.deserialize(result, clazz));
  }

}
