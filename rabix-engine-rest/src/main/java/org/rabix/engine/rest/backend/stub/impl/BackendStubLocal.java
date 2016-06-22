package org.rabix.engine.rest.backend.stub.impl;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.backend.HeartbeatInfo;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.mechanism.TransportPlugin.ErrorCallback;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.local.TransportPluginLocal;
import org.rabix.transport.mechanism.impl.local.TransportQueueLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendStubLocal implements BackendStub {

  private final static Logger logger = LoggerFactory.getLogger(BackendStubLocal.class);
  
  private final JobService jobService;
  private final BackendLocal backendLocal;
  private final TransportPluginLocal transportPluginLocal;

  private final TransportQueueLocal sendToBackendQueue;
  private final TransportQueueLocal receiveFromBackendQueue;
  private final TransportQueueLocal receiveFromBackendHeartbeatQueue;
  
  private final ExecutorService executorService = Executors.newFixedThreadPool(2);

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
    transportPluginLocal.startReceiver(receiveFromBackendQueue, Job.class, new ReceiveCallback<Job>() {
      @Override
      public void handleReceive(Job job) throws TransportPluginException {
        try {
          jobService.update(job);
        } catch (JobServiceException e) {
          throw new TransportPluginException("Failed to update Job", e);
        }
      }
    }, new ErrorCallback() {
      @Override
      public void handleError(Exception error) {
        logger.error("Failed to receive message.", error);
      }
    });

    transportPluginLocal.startReceiver(receiveFromBackendHeartbeatQueue, HeartbeatInfo.class,
        new ReceiveCallback<HeartbeatInfo>() {
          @Override
          public void handleReceive(HeartbeatInfo entity) throws TransportPluginException {
            heartbeatInfo.put(entity.getId(), entity.getTimestamp());
          }
        }, new ErrorCallback() {
          @Override
          public void handleError(Exception error) {
            logger.error("Failed to receive message.", error);
          }
        });
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
  public Backend getBackend() {
    return backendLocal;
  }

}
