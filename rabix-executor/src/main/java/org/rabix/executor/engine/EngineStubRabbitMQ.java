package org.rabix.executor.engine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rabix.bindings.model.Job;
import org.rabix.executor.service.ExecutorService;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.backend.impl.BackendRabbitMQ.EngineConfiguration;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPlugin.ResultPair;
import org.rabix.transport.mechanism.TransportQueueRabbitMQ;
import org.rabix.transport.mechanism.impl.TransportPluginRabbitMQ;

public class EngineStubRabbitMQ implements EngineStub {

  private BackendRabbitMQ backendRabbitMQ;
  private ExecutorService executorService;
  private TransportPlugin<TransportQueueRabbitMQ> transportPlugin;

  private ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService scheduledHeartbeatService = Executors.newSingleThreadScheduledExecutor();

  private TransportQueueRabbitMQ sendToBackendQueue;
  private TransportQueueRabbitMQ receiveFromBackendQueue;
  private TransportQueueRabbitMQ receiveFromBackendHeartbeatQueue;
  
  public EngineStubRabbitMQ(BackendRabbitMQ backendRabbitMQ, ExecutorService executorService) {
    this.backendRabbitMQ = backendRabbitMQ;
    this.executorService = executorService;
    this.transportPlugin = new TransportPluginRabbitMQ(backendRabbitMQ.getHost());
    
    BackendConfiguration backendConfiguration = backendRabbitMQ.getBackendConfiguration();
    this.sendToBackendQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveRoutingKey());
    
    EngineConfiguration engineConfiguration = backendRabbitMQ.getEngineConfiguration();
    this.receiveFromBackendQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getReceiveRoutingKey());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getHeartbeatRoutingKey());
  }
  
  @Override
  public void start() {
    scheduledService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        ResultPair<Job> result = transportPlugin.receive(sendToBackendQueue, Job.class);
        if (result.isSuccess()) {
          Job job = result.getResult();
          executorService.start(job, job.getContext().getId());
        }
      }
    }, 0, 30, TimeUnit.SECONDS);
    
    scheduledHeartbeatService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        transportPlugin.send(receiveFromBackendHeartbeatQueue, new HeartbeatInfo(backendRabbitMQ.getId(), System.currentTimeMillis()));
      }
    }, 0, 10, TimeUnit.SECONDS);
  }

  @Override
  public void stop() {
    scheduledService.shutdown();
    scheduledHeartbeatService.shutdown();
  }

  @Override
  public void send(Job job) {
    transportPlugin.send(receiveFromBackendQueue, job);
  }

}
