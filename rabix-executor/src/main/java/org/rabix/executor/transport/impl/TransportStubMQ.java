package org.rabix.executor.transport.impl;

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
import org.rabix.executor.transport.TransportQueueConfig;
import org.rabix.executor.transport.TransportStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class TransportStubMQ implements TransportStub {

  private final static Logger logger = LoggerFactory.getLogger(TransportStubMQ.class);
  
  private TransportQueueConfig transportQueueConfig;
  private PooledConnectionFactory connectionFactory;

  @Inject
  public TransportStubMQ(TransportQueueConfig transportQueueConfig) {
    this.transportQueueConfig = transportQueueConfig;
    
    if (this.transportQueueConfig.isMQEnabled()) {
      initializeConnectionFactory();      
    }
  }

  private void initializeConnectionFactory() {
    connectionFactory = new PooledConnectionFactory(transportQueueConfig.getBroker());
    connectionFactory.setIdleTimeout(5000);
    connectionFactory.setMaxConnections(10);
    connectionFactory.setBlockIfSessionPoolIsFull(true);
    connectionFactory.setMaximumActiveSessionPerConnection(5000);
    connectionFactory.start();
  }
  
  public <T> ResultPair<T> send(String destinationQueue, T entity) {
    if (!transportQueueConfig.isMQEnabled()) {
      return null;
    }
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
      return ResultPair.<T> success(null);
    } catch (JMSException e) {
      logger.error("Failed to send " + entity + " to " + destinationQueue, e);
      return ResultPair.<T> fail(null, null);
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
    if (!transportQueueConfig.isMQEnabled()) {
      return null;
    }
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
        return ResultPair.<T> fail(null, null);
      }
      TextMessage textMessage = (TextMessage) message;
      String text = textMessage.getText();
      return ResultPair.<T> success(BeanSerializer.deserialize(text, clazz));
    } catch (JMSException e) {
      logger.error("Failed to receiver from " + sourceQueue, e);
      return ResultPair.<T> fail(null, null);
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

}
