package org.rabix.transport.mechanism.impl.activemq;

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
import org.apache.commons.configuration.Configuration;
import org.rabix.common.json.BeanSerializer;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.TransportPluginType;

public class TransportPluginActiveMQ implements TransportPlugin<TransportQueueActiveMQ> {

  private PooledConnectionFactory connectionFactory;
  
  public TransportPluginActiveMQ(Configuration configuration) throws TransportPluginException {
    connectionFactory = new PooledConnectionFactory(""); // TODO
    connectionFactory.setIdleTimeout(5000);
    connectionFactory.setMaxConnections(10);
    connectionFactory.setBlockIfSessionPoolIsFull(false);
    connectionFactory.setMaximumActiveSessionPerConnection(5000);
    connectionFactory.start();
  }
  
  @Override
  public <T> ResultPair<T> send(TransportQueueActiveMQ destinationQueue, T entity) {
    Session session = null;
    Connection connection = null;
    try {
      connection = connectionFactory.createConnection();
      connection.start();

      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Destination destination = session.createQueue(destinationQueue.getQueue());

      MessageProducer producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

      String payload = BeanSerializer.serializeFull(entity);
      TextMessage message = session.createTextMessage(payload);
      producer.send(message);
      return ResultPair.<T> success(null);
    } catch (JMSException e) {
      return ResultPair.<T> fail(null, null);
    } finally {
      try {
        session.close();
        connection.close();
      } catch (JMSException ignore) {
      }
    }
  }

  public <T> ResultPair<T> receive(TransportQueueActiveMQ sourceQueue, Class<T> clazz, ReceiveCallback<T> receiveCallback) {
    Session session = null;
    Connection connection = null;
    MessageConsumer consumer = null;

    try {
      connection = connectionFactory.createConnection();
      connection.start();

      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Destination destination = session.createQueue(sourceQueue.getQueue());
      consumer = session.createConsumer(destination);

      Message message = consumer.receive();
      TextMessage textMessage = (TextMessage) message;
      String text = textMessage.getText();
      receiveCallback.handleReceive(BeanSerializer.deserialize(text, clazz));
      return ResultPair.<T>success(null);
    } catch (JMSException e) {
      return ResultPair.<T> fail(e, "");
    } finally {
      try {
        consumer.close();
        session.close();
        connection.close();
      } catch (JMSException ignore) {
      }
    }
  }

  @Override
  public TransportPluginType getType() {
    return TransportPluginType.ACTIVE_MQ;
  }

}
