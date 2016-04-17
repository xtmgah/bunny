package org.rabix.engine.rest.backend.impl;

import java.util.Queue;

import org.rabix.common.VMQueues;
import org.rabix.engine.rest.backend.Backend;

public class BackendLocal implements Backend {

  public final static String SEND_TO_BACKEND_QUEUE = "toBackendQueue";
  public final static String RECEIVE_FROM_BACKEND_QUEUE = "fromBackendQueue";
  public final static String RECEIVE_FROM_BACKEND_HEARTBEAT_QUEUE = "fromBackendHeartbeatQueue";
  
  private String id;
  
  private String toBackendQueue;
  private String fromBackendQueue;
  private String fromBackendHeartbeatQueue;
  
  public BackendLocal() {
    this.toBackendQueue = SEND_TO_BACKEND_QUEUE;
    this.fromBackendQueue = RECEIVE_FROM_BACKEND_QUEUE;
    this.fromBackendHeartbeatQueue = RECEIVE_FROM_BACKEND_HEARTBEAT_QUEUE;
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
  
  public Queue<String> getToBackendQueue() {
    return VMQueues.getQueue(toBackendQueue);
  }
  
  public Queue<String> getFromBackendQueue() {
    return VMQueues.getQueue(fromBackendQueue);
  }
  
  public Queue<String> getFromBackendHeartbeatQueue() {
    return VMQueues.getQueue(fromBackendHeartbeatQueue);
  }

  @Override
  public BackendType getType() {
    return BackendType.LOCAL;
  }

}
