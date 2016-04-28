package org.rabix.engine.rest.backend.stub.impl;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rabix.bindings.model.Job;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.rest.backend.Backend;
import org.rabix.engine.rest.backend.HeartbeatInfo;
import org.rabix.engine.rest.backend.impl.BackendLocal;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.engine.rest.transport.TransportPlugin.ResultPair;
import org.rabix.engine.rest.transport.impl.TransportPluginLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendStubLocal implements BackendStub {

  private final static Logger logger = LoggerFactory.getLogger(BackendStubLocal.class);
  
  private JobService jobService;
  private BackendLocal backendLocal;
  private TransportPluginLocal transportPluginLocal;
  
  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  
  public BackendStubLocal(JobService jobService, BackendLocal backendLocal) {
    this.jobService = jobService;
    this.backendLocal = backendLocal;
    this.transportPluginLocal = new TransportPluginLocal(backendLocal);
  }
  
  @Override
  public void start() {
    executorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        ResultPair<Job> result = transportPluginLocal.receive(BackendLocal.RECEIVE_FROM_BACKEND_QUEUE, Job.class);
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
    transportPluginLocal.send(BackendLocal.SEND_TO_BACKEND_QUEUE, job);
  }

  @Override
  public void send(Set<Job> jobs) {
    for (Job job : jobs) {
      send(job);
    }
  }

  @Override
  public HeartbeatInfo getHeartbeat() {
    String payload = backendLocal.getFromBackendQueue().poll();
    if (payload != null) {
      return BeanSerializer.deserialize(payload, HeartbeatInfo.class);
    }
    return null;
  }

  @Override
  public Backend getBackend() {
    return backendLocal;
  }

}
