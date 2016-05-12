package org.rabix.executor.engine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.executor.service.ExecutorService;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.backend.impl.BackendRabbitMQ.EngineConfiguration;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportPluginRabbitMQ;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportQueueRabbitMQ;

public class EngineStubRabbitMQ implements EngineStub {

  private BackendRabbitMQ backendRabbitMQ;
  private ExecutorService executorService;
  private TransportPlugin<TransportQueueRabbitMQ> transportPlugin;

  private ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService scheduledHeartbeatService = Executors.newSingleThreadScheduledExecutor();

  private TransportQueueRabbitMQ sendToBackendQueue;
  private TransportQueueRabbitMQ receiveFromBackendQueue;
  private TransportQueueRabbitMQ receiveFromBackendHeartbeatQueue;
  
  public EngineStubRabbitMQ(BackendRabbitMQ backendRabbitMQ, ExecutorService executorService, Configuration configuration) throws TransportPluginException {
    this.backendRabbitMQ = backendRabbitMQ;
    this.executorService = executorService;
    this.transportPlugin = new TransportPluginRabbitMQ(configuration);
    
    BackendConfiguration backendConfiguration = backendRabbitMQ.getBackendConfiguration();
    this.sendToBackendQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveRoutingKey());
    
    EngineConfiguration engineConfiguration = backendRabbitMQ.getEngineConfiguration();
    this.receiveFromBackendQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getReceiveRoutingKey());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getHeartbeatRoutingKey());
  }
  
  @Override
  public void start() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        while(true) {
          transportPlugin.receive(sendToBackendQueue, Job.class, new ReceiveCallback<Job>() {
            @Override
            public void handleReceive(Job job) {
              executorService.start(job, job.getContext().getId());
            }
          });
        }
      }
    }).start();
    
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
