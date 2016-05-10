package org.rabix.transport.backend.impl;

import org.rabix.transport.backend.Backend;

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
  
  public String getToBackendQueue() {
    return toBackendQueue;
  }

  public void setToBackendQueue(String toBackendQueue) {
    this.toBackendQueue = toBackendQueue;
  }

  public String getFromBackendQueue() {
    return fromBackendQueue;
  }

  public void setFromBackendQueue(String fromBackendQueue) {
    this.fromBackendQueue = fromBackendQueue;
  }

  public String getFromBackendHeartbeatQueue() {
    return fromBackendHeartbeatQueue;
  }

  public void setFromBackendHeartbeatQueue(String fromBackendHeartbeatQueue) {
    this.fromBackendHeartbeatQueue = fromBackendHeartbeatQueue;
  }

  @Override
  public BackendType getType() {
    return BackendType.LOCAL;
  }

}
