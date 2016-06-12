package org.rabix.transport.mechanism.impl.rabbitmq;

import java.io.IOException;

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
        } catch (Exception ignore) { }
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
        } catch (Exception ignore) { }
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
        } catch (Exception ignore) { }
      }
    }
  }

  @Override
  public <T> ResultPair<T> receive(final TransportQueueRabbitMQ queue, final Class<T> clazz, final ReceiveCallback<T> receiveCallback) {
    Channel channel = null;
    try {
      channel = connection.createChannel();
      
      String queueName = channel.queueDeclare().getQueue();
      channel.queueBind(queueName, queue.getExchange(), queue.getRoutingKey());

      QueueingConsumer consumer = new QueueingConsumer(channel);
      channel.basicConsume(queueName, true, consumer);

      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      String message = new String(delivery.getBody());
      receiveCallback.handleReceive(BeanSerializer.deserialize(message, clazz));
      return ResultPair.<T>success();
    } catch (BeanProcessorException e) {
      logger.error("Failed to deserialize message payload", e);
      return ResultPair.<T> fail("Failed to deserialize message payload", e);
    } catch (TransportPluginException e) {
      logger.error("Failed to handle receive", e);
      return ResultPair.<T> fail("Failed to handle receive", e);
    } catch (Exception e) {
      logger.error("Failed to receive a message from " + queue, e);
      return ResultPair.<T> fail("Failed to receive a message from " + queue, e);
    } finally {
      if (channel != null) {
        try {
          channel.close();
        } catch (Exception ignore) { }
      }
    }
  }
  
  @Override
  public TransportPluginType getType() {
    return TransportPluginType.RABBIT_MQ;
  }

}
