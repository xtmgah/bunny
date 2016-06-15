package org.rabix.transport.mechanism.impl.rabbitmq;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.json.BeanSerializer;
import org.rabix.common.json.processor.BeanProcessorException;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.TransportPluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class TransportPluginRabbitMQ implements TransportPlugin<TransportQueueRabbitMQ> {

  public static final String DEFAULT_ENCODING = "UTF-8";

  private static final Logger logger = LoggerFactory.getLogger(TransportPluginRabbitMQ.class);

  private Connection connection;
  private ConnectionFactory factory;

  private ConcurrentMap<TransportQueueRabbitMQ, Receiver<?>> receivers = new ConcurrentHashMap<>();
  
  private ExecutorService receiverThreadPool = Executors.newCachedThreadPool();

  public TransportPluginRabbitMQ(Configuration configuration) throws TransportPluginException {
    factory = new ConnectionFactory();

    try {
      if (TransportConfigRabbitMQ.isDev(configuration)) {
        factory.setHost(TransportConfigRabbitMQ.getHost(configuration));
      } else {
        factory.setHost(TransportConfigRabbitMQ.getHost(configuration));
        factory.setPort(TransportConfigRabbitMQ.getPort(configuration));
        factory.setUsername(TransportConfigRabbitMQ.getUsername(configuration));
        factory.setPassword(TransportConfigRabbitMQ.getPassword(configuration));
        factory.setVirtualHost(TransportConfigRabbitMQ.getVirtualhost(configuration));
        if (TransportConfigRabbitMQ.isSSL(configuration)) {
          factory.useSslProtocol();
        }
      }
      connection = factory.newConnection();
    } catch (Exception e) {
      throw new TransportPluginException("Failed to initialize TransportPluginRabbitMQ", e);
    }
  }

  /**
   * {@link TransportPluginRabbitMQ} extension for Exchange initialization
   */
  public void initializeExchange(String exchange, String type) throws TransportPluginException {
    Channel channel = null;
    try {
      channel = connection.createChannel();
      channel.exchangeDeclare(exchange, type);
    } catch (Exception e) {
      throw new TransportPluginException("Failed to declare RabbitMQ exchange " + exchange + " and type " + type, e);
    } finally {
      if (channel != null) {
        try {
          channel.close();
        } catch (Exception ignore) {
        }
      }
    }
  }

  /**
   * {@link TransportPluginRabbitMQ} extension for Exchange initialization
   */
  public void deleteExchange(String exchange) throws TransportPluginException {
    Channel channel = null;
    try {
      channel = connection.createChannel();
      channel.exchangeDelete(exchange, true);
    } catch (Exception e) {
      throw new TransportPluginException("Failed to delete RabbitMQ exchange " + exchange, e);
    } finally {
      if (channel != null) {
        try {
          channel.close();
        } catch (Exception ignore) {
        }
      }
    }
  }

  @Override
  public <T> ResultPair<T> send(TransportQueueRabbitMQ queue, T entity) {
    Channel channel = null;
    try {
      channel = connection.createChannel();

      String payload = BeanSerializer.serializeFull(entity);
      channel.basicPublish(queue.getExchange(), queue.getRoutingKey(), null, payload.getBytes(DEFAULT_ENCODING));
      return ResultPair.success();
    } catch (IOException e) {
      logger.error("Failed to send a message to " + queue, e);
      return ResultPair.fail("Failed to send a message to " + queue, e);
    } finally {
      if (channel != null) {
        try {
          channel.close();
        } catch (Exception ignore) {
        }
      }
    }
  }

  @Override
  public TransportPluginType getType() {
    return TransportPluginType.RABBIT_MQ;
  }

  @Override
  public <T> void startReceiver(TransportQueueRabbitMQ sourceQueue, Class<T> clazz, ReceiveCallback<T> receiveCallback, ErrorCallback errorCallback) {
    final Receiver<T> receiver = new Receiver<>(clazz, receiveCallback, errorCallback, sourceQueue);
    receivers.put(sourceQueue, receiver);
    receiverThreadPool.submit(new Runnable() {
      @Override
      public void run() {
        receiver.start();
      }
    });
  }
  
  @Override
  public void stopReceiver(TransportQueueRabbitMQ queue) {
    Receiver<?> receiver = receivers.get(queue);
    if (receiver != null) {
      receiver.stop();
      receivers.remove(queue);
    }
  }

  private class Receiver<T> {

    private Class<T> clazz;
    private ReceiveCallback<T> callback;
    private ErrorCallback errorCallback;

    private TransportQueueRabbitMQ queue;

    private volatile boolean isStopped = false;

    public Receiver(Class<T> clazz, ReceiveCallback<T> callback, ErrorCallback errorCallback, TransportQueueRabbitMQ queue) {
      this.clazz = clazz;
      this.callback = callback;
      this.queue = queue;
    }

    void start() {
      Channel channel = null;
      try {
        channel = connection.createChannel();

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, queue.getExchange(), queue.getRoutingKey());

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);
        while (!isStopped) {
          QueueingConsumer.Delivery delivery = consumer.nextDelivery();
          String message = new String(delivery.getBody());
          callback.handleReceive(BeanSerializer.deserialize(message, clazz));
        }
      } catch (BeanProcessorException e) {
        logger.error("Failed to deserialize message payload", e);
        errorCallback.handleError(e);
      } catch (TransportPluginException e) {
        logger.error("Failed to handle receive", e);
        errorCallback.handleError(e);
      } catch (Exception e) {
        logger.error("Failed to receive a message from " + queue, e);
        errorCallback.handleError(e);
      } finally {
        if (channel != null) {
          try {
            channel.close();
          } catch (Exception ignore) {
          }
        }
      }
    }

    void stop() {
      isStopped = true;
    }

  }

}
