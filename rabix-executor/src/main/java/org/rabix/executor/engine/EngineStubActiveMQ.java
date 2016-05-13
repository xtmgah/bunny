package org.rabix.executor.engine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.executor.service.ExecutorService;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.rabix.transport.mechanism.TransportPlugin.ResultPair;
import org.rabix.transport.mechanism.impl.activemq.TransportPluginActiveMQ;
import org.rabix.transport.mechanism.impl.activemq.TransportQueueActiveMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineStubActiveMQ implements EngineStub {

  private static final Logger logger = LoggerFactory.getLogger(EngineStubActiveMQ.class);
  
  private BackendActiveMQ backendActiveMQ;
  private ExecutorService executorService;
  private TransportPlugin<TransportQueueActiveMQ> transportPlugin;

  private ScheduledExecutorService scheduledHeartbeatService = Executors.newSingleThreadScheduledExecutor();

  private TransportQueueActiveMQ sendToBackendQueue;
  private TransportQueueActiveMQ receiveFromBackendQueue;
  private TransportQueueActiveMQ receiveFromBackendHeartbeatQueue;
  
  public EngineStubActiveMQ(BackendActiveMQ backendActiveMQ, ExecutorService executorService, Configuration configuration) throws TransportPluginException {
    this.backendActiveMQ = backendActiveMQ;
    this.executorService = executorService;
    this.transportPlugin = new TransportPluginActiveMQ(configuration);
    
    this.sendToBackendQueue = new TransportQueueActiveMQ(backendActiveMQ.getToBackendQueue());
    this.receiveFromBackendQueue = new TransportQueueActiveMQ(backendActiveMQ.getFromBackendQueue());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueActiveMQ(backendActiveMQ.getFromBackendHeartbeatQueue());
  }
  
  @Override
  public void start() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        while(true) {
          ResultPair<Job> result = transportPlugin.receive(sendToBackendQueue, Job.class, new ReceiveCallback<Job>() {
            @Override
            public void handleReceive(Job job) throws TransportPluginException {
              executorService.start(job, job.getContext().getId());
            }
          });
          if (!result.isSuccess()) {
            logger.error(result.getMessage(), result.getException());
          }
        }
      }
    }).start();
    
    scheduledHeartbeatService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        transportPlugin.send(receiveFromBackendHeartbeatQueue, new HeartbeatInfo(backendActiveMQ.getId(), System.currentTimeMillis()));
      }
    }, 0, 1, TimeUnit.SECONDS);
  }

  @Override
  public void stop() {
    scheduledHeartbeatService.shutdown();
  }

  @Override
  public void send(Job job) {
    transportPlugin.send(receiveFromBackendQueue, job);
  }

}
