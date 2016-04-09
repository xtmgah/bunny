package org.rabix.executor.mq;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Inject;

public class MQConfig {

  private final String broker;
  private final String sendQueue;
  private final String receiveQueue;
  private final String heartbeatQueue;
  
  @Inject
  public MQConfig(Configuration configuration) {
    this.broker = configuration.getString("mq.broker", null);
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
  
}
