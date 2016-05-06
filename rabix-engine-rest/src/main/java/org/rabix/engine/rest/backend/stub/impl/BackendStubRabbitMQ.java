package org.rabix.engine.rest.backend.stub.impl;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.rabix.bindings.model.Job;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.rest.backend.Backend;
import org.rabix.engine.rest.backend.HeartbeatInfo;
import org.rabix.engine.rest.backend.impl.BackendRabbitMQ;
import org.rabix.engine.rest.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.engine.rest.backend.impl.BackendRabbitMQ.EngineConfiguration;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class BackendStubRabbitMQ implements BackendStub {

  private final static Logger logger = LoggerFactory.getLogger(BackendStubRabbitMQ.class);

  private JobService jobService;
  private BackendRabbitMQ backend;

  private Connection connection;
  
  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  public BackendStubRabbitMQ(JobService jobService, BackendRabbitMQ backend) {
    this.backend = backend;
    this.jobService = jobService;
  }

  @Override
  public void start() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(backend.getHost());
    try {
      connection = factory.newConnection();

      executorService.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          try {
            Channel channel = connection.createChannel();

            EngineConfiguration engineConfiguration = backend.getEngineConfiguration();
            channel.exchangeDeclare(engineConfiguration.getExchange(), engineConfiguration.getExchangeType());
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, engineConfiguration.getExchange(), engineConfiguration.getReceiveRoutingKey());

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueName, true, consumer);

            Job job = null;
            try {
              int timeout = 1000;
              QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeout);
              String message = new String(delivery.getBody());
              job = BeanSerializer.deserialize(message, Job.class);
            } catch (Exception e) {
              // ignore
            }
            if (job != null) {
              jobService.update(job);
            }
          } catch (Exception e) {
            logger.error("Failed to receive Job from backend " + backend.getId(), e);
          }
        }
      }, 0, 10, TimeUnit.MILLISECONDS);
    } catch (IOException | TimeoutException e) {
      logger.error("Failed to create RabbitMQ connection to " + backend.getHost());
    }
  }

  @Override
  public void stop() {
    if (connection != null) {
      try {
        connection.close();
      } catch (IOException ignore) {
        // ignore
      }
    }
  }

  @Override
  public void send(Job job) {
    Channel channel = null;
    try {
      channel = connection.createChannel();

      BackendConfiguration backendConfiguration = backend.getBackendConfiguration();
      channel.exchangeDeclare(backendConfiguration.getExchange(), backendConfiguration.getExchangeType());
      
      String payload = BeanSerializer.serializeFull(job);
      channel.basicPublish(backendConfiguration.getExchange(), backendConfiguration.getReceiveRoutingKey(), null, payload.getBytes("UTF-8"));
    } catch (IOException e) {
      logger.error("Failed to send message via RabbitMQ", e);
    } finally {
      if (channel != null) {
        try {
          channel.close();
        } catch (IOException | TimeoutException e) {
          // do nothing
        }
      }
    }
  }

  @Override
  public void send(Set<Job> jobs) {
    for (Job job : jobs) {
      send(job);
    }
  }

  @Override
  public HeartbeatInfo getHeartbeat() {
    Channel channel;
    try {
      channel = connection.createChannel();

      EngineConfiguration engineConfiguration = backend.getEngineConfiguration();
      channel.exchangeDeclare(engineConfiguration.getExchange(), engineConfiguration.getExchangeType());
      String queueName = channel.queueDeclare().getQueue();
      channel.queueBind(queueName, engineConfiguration.getExchange(), engineConfiguration.getHeartbeatRoutingKey());

      QueueingConsumer consumer = new QueueingConsumer(channel);
      channel.basicConsume(queueName, true, consumer);

      int timeout = 1000;
      QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeout);
      String message = new String(delivery.getBody());
      return BeanSerializer.deserialize(message, HeartbeatInfo.class);
    } catch (Exception e) {
      logger.error("Failed to receive heartbeat information for backend " + backend.getId(), e);
    }
    return null;
  }

  @Override
  public Backend getBackend() {
    return backend;
  }

}
