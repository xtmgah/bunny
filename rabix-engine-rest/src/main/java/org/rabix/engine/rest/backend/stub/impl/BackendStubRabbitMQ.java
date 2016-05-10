package org.rabix.engine.rest.backend.stub.impl;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.backend.HeartbeatInfo;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.backend.impl.BackendRabbitMQ.EngineConfiguration;
import org.rabix.transport.mechanism.TransportPlugin.ResultPair;
import org.rabix.transport.mechanism.TransportQueueRabbitMQ;
import org.rabix.transport.mechanism.impl.TransportPluginRabbitMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendStubRabbitMQ implements BackendStub {

  private final static Logger logger = LoggerFactory.getLogger(BackendStubActiveMQ.class);

  private JobService jobService;
  private BackendRabbitMQ backendRabbitMQ;
  private TransportPluginRabbitMQ transportPluginMQ;

  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  private TransportQueueRabbitMQ sendToBackendQueue;
  private TransportQueueRabbitMQ receiveFromBackendQueue;
  private TransportQueueRabbitMQ receiveFromBackendHeartbeatQueue;

  public BackendStubRabbitMQ(JobService jobService, BackendRabbitMQ backend) {
    this.backendRabbitMQ = backend;
    this.jobService = jobService;
    this.transportPluginMQ = new TransportPluginRabbitMQ(backend.getHost());
    
    BackendConfiguration backendConfiguration = backend.getBackendConfiguration();
    this.sendToBackendQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveRoutingKey());
    
    EngineConfiguration engineConfiguration = backend.getEngineConfiguration();
    this.receiveFromBackendQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getReceiveRoutingKey());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getHeartbeatRoutingKey());
  }

  @Override
  public void start() {
    executorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        ResultPair<Job> result = receive(receiveFromBackendQueue, Job.class);
        if (result.isSuccess()) {
          try {
            jobService.update(result.getResult());
          } catch (JobServiceException e) {
            logger.error("Failed to update Job " + result.getResult());
          }
        }
      }
    }, 0, 10, TimeUnit.SECONDS);
  }

  @Override
  public void stop() {
    executorService.shutdown();
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
  public HeartbeatInfo getHeartbeat() {
    ResultPair<HeartbeatInfo> resultPair = receive(receiveFromBackendHeartbeatQueue, HeartbeatInfo.class);
    return resultPair.getResult();
  }

  public <T> ResultPair<T> receive(TransportQueueRabbitMQ queue, Class<T> clazz) {
    return transportPluginMQ.receive(queue, clazz);
  }

  @Override
  public Backend getBackend() {
    return backendRabbitMQ;
  }

}
