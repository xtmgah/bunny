package org.rabix.transport.backend.impl;

import org.rabix.transport.backend.Backend;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BackendLocal extends Backend {

  public final static String SEND_TO_BACKEND_QUEUE = "toBackendQueue";
  public final static String RECEIVE_FROM_BACKEND_QUEUE = "fromBackendQueue";
  public final static String RECEIVE_FROM_BACKEND_HEARTBEAT_QUEUE = "fromBackendHeartbeatQueue";
  
  @JsonProperty("to_backend_queue")
  private String toBackendQueue;
  @JsonProperty("from_backend_queue")
  private String fromBackendQueue;
  @JsonProperty("from_backend_heartbeat_queue")
  private String fromBackendHeartbeatQueue;
  
  public BackendLocal() {
    this.toBackendQueue = SEND_TO_BACKEND_QUEUE;
    this.fromBackendQueue = RECEIVE_FROM_BACKEND_QUEUE;
    this.fromBackendHeartbeatQueue = RECEIVE_FROM_BACKEND_HEARTBEAT_QUEUE;
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
