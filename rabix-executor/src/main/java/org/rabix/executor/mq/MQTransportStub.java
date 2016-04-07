package org.rabix.executor.mq;

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

import com.google.inject.Inject;

public class MQTransportStub {

  private MQConfig mqConfig;
  private PooledConnectionFactory connectionFactory;

  @Inject
  public MQTransportStub(MQConfig mqConfig) {
    this.mqConfig = mqConfig;
    initializeConnectionFactory();
  }

  private void initializeConnectionFactory() {
    connectionFactory = new PooledConnectionFactory(mqConfig.getBroker());
    connectionFactory.setIdleTimeout(5000);
    connectionFactory.setMaxConnections(10);
    connectionFactory.setBlockIfSessionPoolIsFull(false);
    connectionFactory.setMaximumActiveSessionPerConnection(5000);
    connectionFactory.start();
  }
  
  public <T> void send(String destinationQueue, T entity) throws MQTransportStubException {
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
    } catch (JMSException e) {
      throw new MQTransportStubException("Failed to send " + entity + " to " + destinationQueue, e);
    } finally {
      try {
        session.close();
        connection.close();
      } catch (JMSException e) {
        // do nothing
      }
    }
  }

  public <T> T receive(String sourceQueue, Class<T> clazz) throws MQTransportStubException {
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
      TextMessage textMessage = (TextMessage) message;
      String text = textMessage.getText();
      return BeanSerializer.deserialize(text, clazz);
    } catch (JMSException e) {
      throw new MQTransportStubException("Failed to receive message from " + sourceQueue, e);
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
