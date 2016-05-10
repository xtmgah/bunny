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
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.mechanism.TransportPlugin.ResultPair;
import org.rabix.transport.mechanism.TransportQueueActiveMQ;
import org.rabix.transport.mechanism.impl.TransportPluginActiveMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendStubActiveMQ implements BackendStub {

  private final static Logger logger = LoggerFactory.getLogger(BackendStubActiveMQ.class);
  
  private JobService jobService;
  private BackendActiveMQ backendActiveMQ;
  private TransportPluginActiveMQ transportPluginMQ;
  
  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  
  private TransportQueueActiveMQ sendToBackendQueue;
  private TransportQueueActiveMQ receiveFromBackendQueue;
  private TransportQueueActiveMQ receiveFromBackendHeartbeatQueue;
  
  public BackendStubActiveMQ(JobService jobService, BackendActiveMQ backend) {
    this.backendActiveMQ = backend;
    this.jobService = jobService;
    this.transportPluginMQ = new TransportPluginActiveMQ(backend.getBroker());
    
    this.sendToBackendQueue = new TransportQueueActiveMQ(backend.getToBackendQueue());
    this.receiveFromBackendQueue = new TransportQueueActiveMQ(backend.getFromBackendQueue());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueActiveMQ(backend.getFromBackendHeartbeatQueue());
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
    }, 0, 10, TimeUnit.MILLISECONDS);
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
  
  public <T> ResultPair<T> receive(TransportQueueActiveMQ queue, Class<T> clazz) {
    return transportPluginMQ.receive(queue, clazz);
  }
  
  @Override
  public Backend getBackend() {
    return backendActiveMQ;
  }
  
}
