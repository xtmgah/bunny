package org.rabix.engine.rest.transport.impl;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.rest.transport.TransportPlugin;
import org.rabix.engine.rest.transport.TransportPluginType;

public class TransportPluginMQ implements TransportPlugin {

  private String broker;
  private PooledConnectionFactory connectionFactory;

  public TransportPluginMQ(String broker) {
    this.broker = broker;
    initializeConnectionFactory();
  }

  private void initializeConnectionFactory() {
    connectionFactory = new PooledConnectionFactory(broker);
    connectionFactory.setIdleTimeout(5000);
    connectionFactory.setMaxConnections(10);
    connectionFactory.setBlockIfSessionPoolIsFull(false);
    connectionFactory.setMaximumActiveSessionPerConnection(5000);
    connectionFactory.start();
  }
  
  public <T> ResultPair<T> send(String destinationQueue, T entity) {
    Session session = null;
    Connection connection = null;
    try {
      connection = connectionFactory.createConnection();
      connection.start();

      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Destination destination = session.createQueue(destinationQueue);

      MessageProducer producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

      String payload = BeanSerializer.serializeFull(entity);
      TextMessage message = session.createTextMessage(payload);
      producer.send(message);
      return new ResultPair<T>(true, null);
    } catch (JMSException e) {
      return new ResultPair<T>(false, null);
    } finally {
      try {
        session.close();
        connection.close();
      } catch (JMSException e) {
        // do nothing
      }
    }
  }

  public <T> ResultPair<T> receive(String sourceQueue, Class<T> clazz) {
    Session session = null;
    Connection connection = null;
    MessageConsumer consumer = null;

    try {
      connection = connectionFactory.createConnection();
      connection.start();

      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Destination destination = session.createQueue(sourceQueue);
      consumer = session.createConsumer(destination);

      Message message = consumer.receive(1000);
      if (message == null) {
        return new ResultPair<T>(false, null);
      }
      TextMessage textMessage = (TextMessage) message;
      String text = textMessage.getText();
      return new ResultPair<T>(true, BeanSerializer.deserialize(text, clazz));
    } catch (JMSException e) {
      return new ResultPair<T>(false, null);
    } finally {
      try {
        consumer.close();
        session.close();
        connection.close();
      } catch (JMSException e) {
        // do nothing
      }
    }
  }

  @Override
  public TransportPluginType getType() {
    return TransportPluginType.MQ;
  }
  
  public static class ResultPair<T> {
    private boolean success;
    private T result;
    
    public ResultPair(boolean success, T result) {
      this.success = success;
      this.result = result;
    }

    public boolean isSuccess() {
      return success;
    }
    
    public T getResult() {
      return result;
    }
  }

}
