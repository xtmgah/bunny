package org.rabix.engine.rest.backend.impl;

import java.util.Queue;

import org.rabix.common.VMQueues;
import org.rabix.engine.rest.backend.Backend;

public class BackendLocal implements Backend {

  public final static String SEND_QUEUE = "sendQueue";
  public final static String RECEIVE_QUEUE = "receiveQueue";
  public final static String HEARTBEAT_QUEUE = "heartbeatQueue";
  
  private String id;
  
  private String sendQueue;
  private String receiveQueue;
  private String heartbeatQueue;
  
  public BackendLocal() {
    this.sendQueue = SEND_QUEUE;
    this.receiveQueue = RECEIVE_QUEUE;
    this.heartbeatQueue = HEARTBEAT_QUEUE;
  }
  
  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }
  
  public Queue<String> getQueue(String name) {
    return VMQueues.getQueue(name);
  }
  
  public Queue<String> getSendQueue() {
    return VMQueues.getQueue(sendQueue);
  }
  
  public Queue<String> getReceiveQueue() {
    return VMQueues.getQueue(receiveQueue);
  }
  
  public Queue<String> getHeartbeatQueue() {
    return VMQueues.getQueue(heartbeatQueue);
  }

  @Override
  public BackendType getType() {
    return BackendType.LOCAL;
  }

}
