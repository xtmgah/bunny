package org.rabix.transport.mechanism.impl;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.rabix.common.json.BeanSerializer;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPluginType;
import org.rabix.transport.mechanism.TransportQueueRabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class TransportPluginRabbitMQ implements TransportPlugin<TransportQueueRabbitMQ> {

  public static final long DEFAULT_TIMEOUT = 10;
  public static final String DEFAULT_ENCODING = "UTF-8";

  
  private String host;
  private ConnectionFactory factory;
  
  public TransportPluginRabbitMQ(String host) {
    this.host = host;
    initializeConnectionFactory();
  }
  
  private void initializeConnectionFactory() {
    factory = new ConnectionFactory();
    factory.setHost(host);
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
  public <T> ResultPair<T> receive(TransportQueueRabbitMQ queue, Class<T> clazz) {
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

      QueueingConsumer.Delivery delivery = consumer.nextDelivery(DEFAULT_TIMEOUT);
      String message = new String(delivery.getBody());
      return ResultPair.success(BeanSerializer.deserialize(message, clazz));
    } catch (Exception e) {
      return ResultPair.fail(e, "Failed to receive message from " + queue);
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
