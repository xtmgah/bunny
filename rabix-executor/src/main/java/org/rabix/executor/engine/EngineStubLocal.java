package org.rabix.executor.engine;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rabix.bindings.model.Job;
import org.rabix.executor.service.ExecutorService;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPlugin.ResultPair;
import org.rabix.transport.mechanism.TransportQueueLocal;
import org.rabix.transport.mechanism.impl.TransportPluginLocal;

public class EngineStubLocal implements EngineStub {

  private BackendLocal backendLocal;
  private ExecutorService executorService;
  private TransportPlugin<TransportQueueLocal> transportPlugin;

  private ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService scheduledHeartbeatService = Executors.newSingleThreadScheduledExecutor();

  private final TransportQueueLocal sendToBackendQueue = new TransportQueueLocal(BackendLocal.SEND_TO_BACKEND_QUEUE);
  private final TransportQueueLocal receiveFromBackendQueue = new TransportQueueLocal(BackendLocal.RECEIVE_FROM_BACKEND_QUEUE);
  private final TransportQueueLocal receiveFromBackendHeartbeatQueue = new TransportQueueLocal(BackendLocal.RECEIVE_FROM_BACKEND_HEARTBEAT_QUEUE);
  
  public EngineStubLocal(BackendLocal backendLocal, ExecutorService executorService) {
    this.backendLocal = backendLocal;
    this.executorService = executorService;
    this.transportPlugin = new TransportPluginLocal();
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
        transportPlugin.send(receiveFromBackendHeartbeatQueue, new HeartbeatInfo(backendLocal.getId(), System.currentTimeMillis()));
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
