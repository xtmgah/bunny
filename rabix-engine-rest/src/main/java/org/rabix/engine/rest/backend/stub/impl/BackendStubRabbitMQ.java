package org.rabix.engine.rest.backend.stub.impl;

import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.backend.HeartbeatInfo;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.backend.impl.BackendRabbitMQ.EngineConfiguration;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportPluginRabbitMQ;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportQueueRabbitMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendStubRabbitMQ implements BackendStub {

  private final static Logger logger = LoggerFactory.getLogger(BackendStubActiveMQ.class);

  private JobService jobService;
  private BackendRabbitMQ backendRabbitMQ;
  private TransportPluginRabbitMQ transportPluginMQ;

  private TransportQueueRabbitMQ sendToBackendQueue;
  private TransportQueueRabbitMQ receiveFromBackendQueue;
  private TransportQueueRabbitMQ receiveFromBackendHeartbeatQueue;

  public BackendStubRabbitMQ(JobService jobService, BackendRabbitMQ backend, Configuration configuration) throws TransportPluginException {
    this.jobService = jobService;
    this.backendRabbitMQ = backend;

    this.transportPluginMQ = new TransportPluginRabbitMQ(configuration);

    BackendConfiguration backendConfiguration = backend.getBackendConfiguration();
    this.sendToBackendQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveRoutingKey());

    EngineConfiguration engineConfiguration = backend.getEngineConfiguration();
    this.receiveFromBackendQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getReceiveRoutingKey());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getHeartbeatRoutingKey());
  }

  @Override
  public void start(final Map<String, Long> heartbeatInfo) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          transportPluginMQ.receive(receiveFromBackendQueue, Job.class, new ReceiveCallback<Job>() {
            @Override
            public void handleReceive(Job job) {
              try {
                jobService.update(job);
              } catch (JobServiceException e) {
                logger.error("Failed to update Job " + job, e);
              }
            }
          });
        }
      }
    }).start();

    new Thread(new Runnable() {
      @Override
      public void run() {
        transportPluginMQ.receive(receiveFromBackendHeartbeatQueue, HeartbeatInfo.class,
            new ReceiveCallback<HeartbeatInfo>() {
          @Override
          public void handleReceive(HeartbeatInfo entity) {
            heartbeatInfo.put(entity.getId(), entity.getTimestamp());
          }
        });
      }
    }).start();
  }

  @Override
  public void stop() {
  }

  @Override
  public void send(Job job) {
    this.transportPluginMQ.send(sendToBackendQueue, job);
  }

  @Override
  public void send(Set<Job> jobs) {
    for (Job job : jobs) {
      send(job);
    }
  }

  @Override
  public Backend getBackend() {
    return backendRabbitMQ;
  }

}
