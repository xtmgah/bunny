package org.rabix.engine.rest.backend.impl;

import org.rabix.engine.rest.backend.Backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BackendActiveMQ implements Backend {

  @JsonProperty("id")
  private String id;
  @JsonProperty("broker")
  private String broker;
  @JsonProperty("toBackendQueue")
  private String toBackendQueue;
  @JsonProperty("fromBackendQueue")
  private String fromBackendQueue;
  @JsonProperty("fromBackendHeartbeatQueue")
  private String fromBackendHeartbeatQueue;
  
  public BackendActiveMQ(@JsonProperty("id") String id, @JsonProperty("broker") String broker, @JsonProperty("toBackendQueue") String toBackendQueue, @JsonProperty("fromBackendQueue") String fromBackendQueue, @JsonProperty("fromBackendHeartbeatQueue") String fromBackendHeartbeatQueue) {
    this.id = id;
    this.broker = broker;
    this.toBackendQueue = toBackendQueue;
    this.fromBackendQueue = fromBackendQueue;
    this.fromBackendHeartbeatQueue = fromBackendHeartbeatQueue;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBroker() {
    return broker;
  }

  public void setBroker(String broker) {
    this.broker = broker;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fromBackendHeartbeatQueue == null) ? 0 : fromBackendHeartbeatQueue.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((fromBackendQueue == null) ? 0 : fromBackendQueue.hashCode());
    result = prime * result + ((toBackendQueue == null) ? 0 : toBackendQueue.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BackendActiveMQ other = (BackendActiveMQ) obj;
    if (fromBackendHeartbeatQueue == null) {
      if (other.fromBackendHeartbeatQueue != null)
        return false;
    } else if (!fromBackendHeartbeatQueue.equals(other.fromBackendHeartbeatQueue))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (fromBackendQueue == null) {
      if (other.fromBackendQueue != null)
        return false;
    } else if (!fromBackendQueue.equals(other.fromBackendQueue))
      return false;
    if (toBackendQueue == null) {
      if (other.toBackendQueue != null)
        return false;
    } else if (!toBackendQueue.equals(other.toBackendQueue))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "BackendActiveMQ [id=" + id + ", sendQueue=" + toBackendQueue + ", receiveQueue=" + fromBackendQueue + ", heartbeatQueue="  + fromBackendHeartbeatQueue + "]";
  }

  @Override
  @JsonIgnore
  public BackendType getType() {
    return BackendType.ACTIVE_MQ;
  }

}
