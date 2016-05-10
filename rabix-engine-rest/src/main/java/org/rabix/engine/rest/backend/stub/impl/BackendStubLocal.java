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
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.mechanism.TransportPlugin.ResultPair;
import org.rabix.transport.mechanism.TransportQueueLocal;
import org.rabix.transport.mechanism.impl.TransportPluginLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendStubLocal implements BackendStub {

  private final static Logger logger = LoggerFactory.getLogger(BackendStubLocal.class);
  
  private JobService jobService;
  private BackendLocal backendLocal;
  private TransportPluginLocal transportPluginLocal;
  
  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  
  private TransportQueueLocal sendToBackendQueue;
  private TransportQueueLocal receiveFromBackendQueue;
  private TransportQueueLocal receiveFromBackendHeartbeatQueue;
  
  public BackendStubLocal(JobService jobService, BackendLocal backendLocal) {
    this.jobService = jobService;
    this.backendLocal = backendLocal;
    this.transportPluginLocal = new TransportPluginLocal();
    
    this.sendToBackendQueue = new TransportQueueLocal(backendLocal.getToBackendQueue());
    this.receiveFromBackendQueue = new TransportQueueLocal(backendLocal.getFromBackendQueue());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueLocal(backendLocal.getFromBackendHeartbeatQueue());
  }
  
  @Override
  public void start() {
    executorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        ResultPair<Job> result = transportPluginLocal.receive(receiveFromBackendQueue, Job.class);
        if (result.isSuccess()) {
          try {
            jobService.update(result.getResult());
          } catch (JobServiceException e) {
            logger.error("Failed to update Job " + result.getResult());
          }
        }
      }
    }, 0, 1, TimeUnit.SECONDS);
  }

  @Override
  public void stop() {
    executorService.shutdown();
  }

  @Override
  public void send(Job job) {
    transportPluginLocal.send(sendToBackendQueue, job);
  }

  @Override
  public void send(Set<Job> jobs) {
    for (Job job : jobs) {
      send(job);
    }
  }

  @Override
  public HeartbeatInfo getHeartbeat() {
    ResultPair<HeartbeatInfo> result = transportPluginLocal.receive(receiveFromBackendHeartbeatQueue, HeartbeatInfo.class);
    if (result.isSuccess()) {
      return result.getResult();
    }
    return null;
  }

  @Override
  public Backend getBackend() {
    return backendLocal;
  }

}
