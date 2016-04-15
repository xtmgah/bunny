package org.rabix.executor.transport;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Inject;

public class TransportQueueConfig {

  private final String broker;
  private final String sendQueue;
  private final String receiveQueue;
  private final String heartbeatQueue;
  
  private final boolean mqEnabled;
  
  @Inject
  public TransportQueueConfig(Configuration configuration) {
    this.broker = configuration.getString("mq.broker", null);
    this.mqEnabled = configuration.getBoolean("mq.enabled", false);
    this.sendQueue = configuration.getString("mq.sendQueue", null);
    this.receiveQueue = configuration.getString("mq.receiveQueue", null);
    this.heartbeatQueue = configuration.getString("mq.heartbeatQueue", null);
  }

  public String getBroker() {
    return broker;
  }

  public String getSendQueue() {
    return sendQueue;
  }

  public String getReceiveQueue() {
    return receiveQueue;
  }

  public String getHeartbeatQueue() {
    return heartbeatQueue;
  }
  
  public boolean isMQEnabled() {
    return mqEnabled;
  }
  
}
