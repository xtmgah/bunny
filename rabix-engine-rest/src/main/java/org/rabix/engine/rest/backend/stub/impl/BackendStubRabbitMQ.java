package org.rabix.engine.rest.backend.stub.impl;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.engine.rest.backend.HeartbeatInfo;
import org.rabix.engine.rest.backend.control.StopControlMessage;
import org.rabix.engine.rest.backend.stub.BackendStub;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.backend.impl.BackendRabbitMQ.BackendConfiguration;
import org.rabix.transport.backend.impl.BackendRabbitMQ.EngineConfiguration;
import org.rabix.transport.mechanism.TransportPlugin.ErrorCallback;
import org.rabix.transport.mechanism.TransportPlugin.ReceiveCallback;
import org.rabix.transport.mechanism.TransportPluginException;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportPluginRabbitMQ;
import org.rabix.transport.mechanism.impl.rabbitmq.TransportQueueRabbitMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendStubRabbitMQ implements BackendStub {

  private final static Logger logger = LoggerFactory.getLogger(BackendStubRabbitMQ.class);
  
  private JobService jobService;
  private BackendRabbitMQ backendRabbitMQ;
  private TransportPluginRabbitMQ transportPluginMQ;

  private TransportQueueRabbitMQ sendToBackendQueue;
  private TransportQueueRabbitMQ receiveFromBackendQueue;
  private TransportQueueRabbitMQ receiveFromBackendHeartbeatQueue;

  private final ExecutorService executorService = Executors.newFixedThreadPool(2);

  public BackendStubRabbitMQ(JobService jobService, BackendRabbitMQ backend, Configuration configuration) throws TransportPluginException {
    this.jobService = jobService;
    this.backendRabbitMQ = backend;

    this.transportPluginMQ = new TransportPluginRabbitMQ(configuration);

    BackendConfiguration backendConfiguration = backend.getBackendConfiguration();
    this.sendToBackendQueue = new TransportQueueRabbitMQ(backendConfiguration.getExchange(), backendConfiguration.getExchangeType(), backendConfiguration.getReceiveRoutingKey());

    EngineConfiguration engineConfiguration = backend.getEngineConfiguration();
    this.receiveFromBackendQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getReceiveRoutingKey());
    this.receiveFromBackendHeartbeatQueue = new TransportQueueRabbitMQ(engineConfiguration.getExchange(), engineConfiguration.getExchangeType(), engineConfiguration.getHeartbeatRoutingKey());

    initialize();
  }

  /**
   * Try to initialize both exchanges (engine, backend)
   */
  private void initialize() {
    try {
      transportPluginMQ.initializeExchange(backendRabbitMQ.getBackendConfiguration().getExchange(), backendRabbitMQ.getBackendConfiguration().getExchangeType());
      transportPluginMQ.initializeExchange(backendRabbitMQ.getEngineConfiguration().getExchange(), backendRabbitMQ.getEngineConfiguration().getExchangeType());
    } catch (TransportPluginException e) {
      // do nothing
    }
  }

  @Override
  public void start(final Map<String, Long> heartbeatInfo) {
    transportPluginMQ.startReceiver(receiveFromBackendQueue, Job.class, new ReceiveCallback<Job>() {
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

    transportPluginMQ.startReceiver(receiveFromBackendHeartbeatQueue, HeartbeatInfo.class,
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

    try {
      transportPluginMQ.deleteExchange(backendRabbitMQ.getBackendConfiguration().getExchange());
    } catch (TransportPluginException e) {
      // do nothing
    }
  }

  @Override
  public void send(Job job) {
    this.transportPluginMQ.send(sendToBackendQueue, job);
  }

  @Override
  public Backend getBackend() {
    return backendRabbitMQ;
  }

  @Override
  public void send(StopControlMessage controlMessage) {
    // TODO Auto-generated method stub
  }

}
