package org.rabix.transport.backend.impl;

import org.rabix.transport.backend.Backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BackendLocal extends Backend {

  public final static String SEND_TO_BACKEND_QUEUE = "toBackendQueue";
  public final static String SEND_TO_BACKEND_CONTROL_QUEUE = "toBackendControlQueue";
  public final static String RECEIVE_FROM_BACKEND_QUEUE = "fromBackendQueue";
  public final static String RECEIVE_FROM_BACKEND_HEARTBEAT_QUEUE = "fromBackendHeartbeatQueue";
  
  @JsonProperty("to_backend_queue")
  private String toBackendQueue;
  @JsonProperty("to_backend_control_queue")
  private String toBackendControlQueue;
  @JsonProperty("from_backend_queue")
  private String fromBackendQueue;
  @JsonProperty("from_backend_heartbeat_queue")
  private String fromBackendHeartbeatQueue;
  
  public BackendLocal() {
    this.toBackendQueue = SEND_TO_BACKEND_QUEUE;
    this.toBackendControlQueue = SEND_TO_BACKEND_CONTROL_QUEUE;
    this.fromBackendQueue = RECEIVE_FROM_BACKEND_QUEUE;
    this.fromBackendHeartbeatQueue = RECEIVE_FROM_BACKEND_HEARTBEAT_QUEUE;
  }
  
  public String getToBackendQueue() {
    return toBackendQueue;
  }

  public void setToBackendQueue(String toBackendQueue) {
    this.toBackendQueue = toBackendQueue;
  }

  public String getToBackendControlQueue() {
    return toBackendControlQueue;
  }

  public void setToBackendControlQueue(String toBackendControlQueue) {
    this.toBackendControlQueue = toBackendControlQueue;
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
  @JsonIgnore
  public BackendType getType() {
    return BackendType.LOCAL;
  }

  @Override
  public String toString() {
    return "BackendLocal [toBackendQueue=" + toBackendQueue + ", toBackendControlQueue=" + toBackendControlQueue + ", fromBackendQueue=" + fromBackendQueue + ", fromBackendHeartbeatQueue=" + fromBackendHeartbeatQueue + "]";
  }

}
