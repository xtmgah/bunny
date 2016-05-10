package org.rabix.executor.engine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rabix.bindings.model.Job;
import org.rabix.executor.service.ExecutorService;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPlugin.ResultPair;
import org.rabix.transport.mechanism.TransportQueueActiveMQ;
import org.rabix.transport.mechanism.impl.TransportPluginActiveMQ;

public class EngineStubActiveMQ implements EngineStub {

  private BackendActiveMQ backendActiveMQ;
  private ExecutorService executorService;
  private TransportPlugin<TransportQueueActiveMQ> transportPlugin;

  private ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService scheduledHeartbeatService = Executors.newSingleThreadScheduledExecutor();

  private TransportQueueActiveMQ sendToBackendQueue;
  private TransportQueueActiveMQ receiveFromBackendQueue;
  private TransportQueueActiveMQ receiveFromBackendHeartbeatQueue;
  
  public EngineStubActiveMQ(BackendActiveMQ backendActiveMQ, ExecutorService executorService) {
    this.backendActiveMQ = backendActiveMQ;
    this.executorService = executorService;
    this.transportPlugin = new TransportPluginActiveMQ(backendActiveMQ.getBroker());
    
    this.sendToBackendQueue = new TransportQueueActiveMQ(backendActiveMQ.getToBackendQueue());
    this.receiveFromBackendQueue = new TransportQueueActiveMQ(backendActiveMQ.getFromBackendQueue());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueActiveMQ(backendActiveMQ.getFromBackendHeartbeatQueue());
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
    }, 0, 1, TimeUnit.SECONDS);
    
    scheduledHeartbeatService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        transportPlugin.send(receiveFromBackendHeartbeatQueue, new HeartbeatInfo(backendActiveMQ.getId(), System.currentTimeMillis()));
      }
    }, 0, 1, TimeUnit.SECONDS);
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
