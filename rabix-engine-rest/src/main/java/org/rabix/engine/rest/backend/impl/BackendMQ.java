package org.rabix.engine.rest.backend.impl;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.model.Backend;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.engine.rest.transport.impl.TransportPluginMQ;
import org.rabix.engine.rest.transport.impl.TransportPluginMQ.ResultPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendMQ {

  private final static Logger logger = LoggerFactory.getLogger(BackendMQ.class);
  
  private Backend backend;
  private JobService jobService;
  private TransportPluginMQ transportPluginMQ;
  
  private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  
  public BackendMQ(JobService jobService, Backend backend) {
    this.backend = backend;
    this.jobService = jobService;
    this.transportPluginMQ = new TransportPluginMQ(backend.getBroker());
  }
  
  public void startConsumer() {
    executorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        ResultPair<Job> result = transportPluginMQ.receive(backend.getReceiveQueue(), Job.class);
        if (result.isSuccess() && result.getResult() != null) {
          try {
            jobService.update(result.getResult());
          } catch (JobServiceException e) {
            logger.error("Failed to update Job " + result.getResult());
          }
        }
      }
    }, 0, 10, TimeUnit.MILLISECONDS);
  }
  
  public void send(Job job) {
    this.transportPluginMQ.send(backend.getSendQueue(), job);
  }
  
  public void send(Set<Job> jobs) {
    for (Job job : jobs) {
      this.transportPluginMQ.send(backend.getSendQueue(), job);
    }
  }
  
  public Backend getBackend() {
    return backend;
  }
  
}
