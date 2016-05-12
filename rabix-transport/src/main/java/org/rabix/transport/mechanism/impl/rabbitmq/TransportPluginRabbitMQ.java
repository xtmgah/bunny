package org.rabix.transport.mechanism.impl.rabbitmq;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.configuration.Configuration;
import org.rabix.common.json.BeanSerializer;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.TransportPluginType;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class TransportPluginRabbitMQ implements TransportPlugin<TransportQueueRabbitMQ> {

  public static final String DEFAULT_ENCODING = "UTF-8";

  private ConnectionFactory factory;
  
  public TransportPluginRabbitMQ(Configuration configuration) throws TransportPluginException {
    factory = new ConnectionFactory();
    
    if (TransportConfigRabbitMQ.isDev(configuration)) {
      factory.setHost("localhost");
    } else {
      factory.setHost(TransportConfigRabbitMQ.getHost(configuration));
      factory.setPort(TransportConfigRabbitMQ.getPort(configuration));
      factory.setUsername(TransportConfigRabbitMQ.getUsername(configuration));
      factory.setPassword(TransportConfigRabbitMQ.getPassword(configuration));
      factory.setVirtualHost(TransportConfigRabbitMQ.getVirtualhost(configuration));
      if (TransportConfigRabbitMQ.isSSL(configuration)) {
        try {
          factory.useSslProtocol();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
          throw new TransportPluginException("Failed to initialize TransportPluginRabbitMQ", e);
        }
      }
    }
  }
  
  @Override
  public <T> ResultPair<T> send(TransportQueueRabbitMQ queue, T entity) {
    Channel channel = null;
    Connection connection = null;
    try {
      connection = factory.newConnection();
      channel = connection.createChannel();
      
      channel.exchangeDeclare(queue.getExchange(), queue.getExchangeType());
      String payload = BeanSerializer.serializeFull(entity);
      channel.basicPublish(queue.getExchange(), queue.getRoutingKey(), null, payload.getBytes(DEFAULT_ENCODING));
      return ResultPair.success(null);
    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
      return ResultPair.fail(e, "Failed to send a message to " + queue);
    } finally {
      if (channel != null) {
        try {
          channel.close();
        } catch (Exception ignore) { }
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (Exception ignore) { }
      }
    }
  }

  @Override
  public <T> ResultPair<T> receive(final TransportQueueRabbitMQ queue, final Class<T> clazz, final ReceiveCallback<T> receiveCallback) {
    Channel channel = null;
    Connection connection = null;
    try {
      connection = factory.newConnection();
      channel = connection.createChannel();
      
      channel.exchangeDeclare(queue.getExchange(), queue.getExchangeType());
      String queueName = channel.queueDeclare().getQueue();
      channel.queueBind(queueName, queue.getExchange(), queue.getRoutingKey());

      QueueingConsumer consumer = new QueueingConsumer(channel);
      channel.basicConsume(queueName, true, consumer);

      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
      String message = new String(delivery.getBody());
      receiveCallback.handleReceive(BeanSerializer.deserialize(message, clazz));
      return ResultPair.<T>success(null);
    } catch (Exception e) {
      return ResultPair.<T>fail(e, "Failed to receive a message from " + queue);
    } finally {
      if (channel != null) {
        try {
          channel.close();
        } catch (Exception ignore) { }
      }
      if (connection != null) {
        try {
          connection.close();
        } catch (Exception ignore) { }
      }
    }
  }
  
  @Override
  public TransportPluginType getType() {
    return TransportPluginType.RABBIT_MQ;
  }

}
