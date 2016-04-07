package org.rabix.engine.rest.backend.impl;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.model.Backend;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.engine.rest.transport.TransportPluginException;
import org.rabix.engine.rest.transport.impl.TransportPluginMQ;

public class BackendMQ {

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
        try {
          Job job = transportPluginMQ.receive(backend.getReceiveQueue(), Job.class);
          jobService.update(job);
        } catch (TransportPluginException e) {
          e.printStackTrace(); // TODO handle
        } catch (JobServiceException e) {
          e.printStackTrace(); // TODO handle
        }
      }
    }, 0, 10, TimeUnit.MILLISECONDS);
  }
  
  public void send(Job job) throws TransportPluginException {
    this.transportPluginMQ.send(backend.getSendQueue(), job);
  }
  
  public void send(Set<Job> jobs) throws TransportPluginException {
    for (Job job : jobs) {
      this.transportPluginMQ.send(backend.getSendQueue(), job);
    }
  }
  
  public Backend getBackend() {
    return backend;
  }
  
}
