package org.rabix.executor.transport;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Inject;

public class TransportQueueConfig {

  private final String broker;
  private final String toBackendQueue;
  private final String fromBackendQueue;
  private final String fromBackendHeartbeatQueue;
  
  @Inject
  public TransportQueueConfig(Configuration configuration) {
    this.broker = configuration.getString("mq.broker", null);

    this.toBackendQueue = configuration.getString("queue.toBackendQueue", null);
    this.fromBackendQueue = configuration.getString("queue.fromBackendQueue", null);
    this.fromBackendHeartbeatQueue = configuration.getString("queue.fromBackendHeartbeatQueue", null);
  }

  public String getBroker() {
    return broker;
  }

  public String getToBackendQueue() {
    return toBackendQueue;
  }

  public String getFromBackendQueue() {
    return fromBackendQueue;
  }

  public String getFromBackendHeartbeatQueue() {
    return fromBackendHeartbeatQueue;
  }

}
