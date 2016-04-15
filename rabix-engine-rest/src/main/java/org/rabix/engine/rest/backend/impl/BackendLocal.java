package org.rabix.engine.rest.backend.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.rabix.engine.rest.backend.Backend;

public class BackendLocal implements Backend {

  public final static String SEND_QUEUE = "sendQueue";
  public final static String RECEIVE_QUEUE = "receiveQueue";
  public final static String HEARTBEAT_QUEUE = "heartbeatQueue";
  
  private String id;
  private Map<String, Queue<String>> queues = new HashMap<>();
  
  public BackendLocal(Queue<String> sendQueue, Queue<String> receiveQueue, Queue<String> heartbeatQueue) {
    queues.put(SEND_QUEUE, sendQueue);
    queues.put(RECEIVE_QUEUE, receiveQueue);
    queues.put(HEARTBEAT_QUEUE, heartbeatQueue);
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
    return queues.get(name);
  }
  
  public Queue<String> getSendQueue() {
    return queues.get(SEND_QUEUE);
  }
  
  public Queue<String> getReceiveQueue() {
    return queues.get(RECEIVE_QUEUE);
  }
  
  public Queue<String> getHeartbeatQueue() {
    return queues.get(HEARTBEAT_QUEUE);
  }

  @Override
  public BackendType getType() {
    return BackendType.LOCAL;
  }

}
