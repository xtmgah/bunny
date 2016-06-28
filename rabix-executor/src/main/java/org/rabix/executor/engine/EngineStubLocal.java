package org.rabix.executor.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.common.engine.control.EngineControlMessage;
import org.rabix.common.engine.control.EngineControlStopMessage;
import org.rabix.executor.service.ExecutorService;
import org.rabix.transport.backend.HeartbeatInfo;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.mechanism.TransportPlugin;
import org.rabix.transport.mechanism.TransportPlugin.ErrorCallback;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.local.TransportPluginLocal;
import org.rabix.transport.mechanism.impl.local.TransportQueueLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineStubLocal implements EngineStub {

  private final static Logger logger = LoggerFactory.getLogger(EngineStubLocal.class);
  
  private BackendLocal backendLocal;
  private ExecutorService executorService;
  private TransportPlugin<TransportQueueLocal> transportPlugin;

  private ScheduledExecutorService scheduledHeartbeatService = Executors.newSingleThreadScheduledExecutor();

  private final TransportQueueLocal sendToBackendQueue = new TransportQueueLocal(BackendLocal.SEND_TO_BACKEND_QUEUE);
  private final TransportQueueLocal sendToBackendControlQueue = new TransportQueueLocal(BackendLocal.SEND_TO_BACKEND_CONTROL_QUEUE);
  private final TransportQueueLocal receiveFromBackendQueue = new TransportQueueLocal(BackendLocal.RECEIVE_FROM_BACKEND_QUEUE);
  private final TransportQueueLocal receiveFromBackendHeartbeatQueue = new TransportQueueLocal(BackendLocal.RECEIVE_FROM_BACKEND_HEARTBEAT_QUEUE);
  
  public EngineStubLocal(BackendLocal backendLocal, ExecutorService executorService, Configuration configuration) throws TransportPluginException {
    this.backendLocal = backendLocal;
    this.executorService = executorService;
    this.transportPlugin = new TransportPluginLocal(configuration);
  }
  
  @Override
  public void start() {
    transportPlugin.startReceiver(sendToBackendQueue, Job.class, new ReceiveCallback<Job>() {
      @Override
      public void handleReceive(Job job) throws TransportPluginException {
        executorService.start(job, job.getContext().getId());
      }
    }, new ErrorCallback() {
      @Override
      public void handleError(Exception error) {
        logger.error("Failed to receive message.", error);
      }
    });

    transportPlugin.startReceiver(sendToBackendControlQueue, EngineControlMessage.class, new ReceiveCallback<EngineControlMessage>() {
      @Override
      public void handleReceive(EngineControlMessage controlMessage) throws TransportPluginException {
        switch (controlMessage.getType()) {
        case STOP:
          List<String> ids = new ArrayList<>();
          ids.add(((EngineControlStopMessage)controlMessage).getId());
          executorService.stop(ids, controlMessage.getRootId());
          break;
        default:
          break;
        }
      }
    }, new ErrorCallback() {
      @Override
      public void handleError(Exception error) {
        logger.error("Failed to execute control message.", error);
      }
    });

    scheduledHeartbeatService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        transportPlugin.send(receiveFromBackendHeartbeatQueue,
            new HeartbeatInfo(backendLocal.getId(), System.currentTimeMillis()));
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
