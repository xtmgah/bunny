package org.rabix.engine.rest.backend.stub.impl;

import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.backend.HeartbeatInfo;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.rabix.transport.mechanism.impl.local.TransportPluginLocal;
import org.rabix.transport.mechanism.impl.local.TransportQueueLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendStubLocal implements BackendStub {

  private final static Logger logger = LoggerFactory.getLogger(BackendStubLocal.class);

  private JobService jobService;
  private BackendLocal backendLocal;
  private TransportPluginLocal transportPluginLocal;

  private TransportQueueLocal sendToBackendQueue;
  private TransportQueueLocal receiveFromBackendQueue;
  private TransportQueueLocal receiveFromBackendHeartbeatQueue;

  public BackendStubLocal(JobService jobService, Configuration configuration, BackendLocal backendLocal) throws TransportPluginException {
    this.jobService = jobService;
    this.backendLocal = backendLocal;
    this.transportPluginLocal = new TransportPluginLocal(configuration);

    this.sendToBackendQueue = new TransportQueueLocal(backendLocal.getToBackendQueue());
    this.receiveFromBackendQueue = new TransportQueueLocal(backendLocal.getFromBackendQueue());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueLocal(backendLocal.getFromBackendHeartbeatQueue());
  }

  @Override
  public void start(final Map<String, Long> heartbeatInfo) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          transportPluginLocal.receive(receiveFromBackendQueue, Job.class, new ReceiveCallback<Job>() {
            @Override
            public void handleReceive(Job job) {
              try {
                jobService.update(job);
              } catch (JobServiceException e) {
                logger.error("Failed to update Job " + job, e);
              }
            }
          });
        }
      }
    }).start();

    new Thread(new Runnable() {
      @Override
      public void run() {
        transportPluginLocal.receive(receiveFromBackendHeartbeatQueue, HeartbeatInfo.class, new ReceiveCallback<HeartbeatInfo>() {
          @Override
          public void handleReceive(HeartbeatInfo entity) {
            heartbeatInfo.put(entity.getId(), entity.getTimestamp());
          }
        });
      }
    }).start();
  }

  @Override
  public void stop() {
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
  public Backend getBackend() {
    return backendLocal;
  }

}
