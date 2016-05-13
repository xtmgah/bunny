package org.rabix.transport.mechanism.impl.local;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.VMQueues;
import org.rabix.common.json.BeanSerializer;
import org.rabix.common.json.processor.BeanProcessorException;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.TransportPluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportPluginLocal implements TransportPlugin<TransportQueueLocal> {

  private static final Logger logger = LoggerFactory.getLogger(TransportPluginLocal.class);
  
  public TransportPluginLocal(Configuration configuration) throws TransportPluginException {
  }

  @Override
  public <T> ResultPair<T> send(TransportQueueLocal queue, T entity) {
    try {
      VMQueues.getQueue(queue.getQueue()).put(BeanSerializer.serializeFull(entity));
      return ResultPair.<T> success();
    } catch (InterruptedException e) {
      logger.error("Failed to send a message to " + queue, e);
      return ResultPair.<T>fail("Failed to put to queue " + queue, e);
    }
  }

  @Override
  public <T> ResultPair<T> receive(TransportQueueLocal queue, Class<T> clazz, ReceiveCallback<T> receiveCallback) {
    try {
      String payload = VMQueues.<String> getQueue(queue.getQueue()).take();
      receiveCallback.handleReceive(BeanSerializer.deserialize(payload, clazz));
      return ResultPair.success();
    } catch (InterruptedException e) {
      logger.error("Failed to receive a message from " + queue, e);
      return ResultPair.<T> fail("Failed to receive a message from " + queue, e);
    } catch (BeanProcessorException e) {
      logger.error("Failed to deserialize message payload", e);
      return ResultPair.<T> fail("Failed to deserialize message payload", e);
    } catch (TransportPluginException e) {
      logger.error("Failed to handle receive", e);
      return ResultPair.<T> fail("Failed to handle receive", e);
    }
  }

  @Override
  public TransportPluginType getType() {
    return TransportPluginType.LOCAL;
  }

}
