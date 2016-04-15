package org.rabix.engine.rest.backend.impl;

import org.rabix.engine.rest.backend.Backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BackendMQ implements Backend {

  @JsonProperty("id")
  private String id;
  @JsonProperty("broker")
  private String broker;
  @JsonProperty("sendQueue")
  private String sendQueue;
  @JsonProperty("receiveQueue")
  private String receiveQueue;
  @JsonProperty("heartbeatQueue")
  private String heartbeatQueue;
  
  public BackendMQ(@JsonProperty("id") String id, @JsonProperty("broker") String broker, @JsonProperty("sendQueue") String sendQueue, @JsonProperty("receiveQueue") String receiveQueue, @JsonProperty("heartbeatQueue") String heartbeatQueue) {
    this.id = id;
    this.broker = broker;
    this.sendQueue = sendQueue;
    this.receiveQueue = receiveQueue;
    this.heartbeatQueue = heartbeatQueue;
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

  public String getSendQueue() {
    return sendQueue;
  }

  public void setSendQueue(String sendQueue) {
    this.sendQueue = sendQueue;
  }

  public String getReceiveQueue() {
    return receiveQueue;
  }

  public void setReceiveQueue(String receiveQueue) {
    this.receiveQueue = receiveQueue;
  }

  public String getHeartbeatQueue() {
    return heartbeatQueue;
  }

  public void setHeartbeatQueue(String heartbeatQueue) {
    this.heartbeatQueue = heartbeatQueue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((heartbeatQueue == null) ? 0 : heartbeatQueue.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((receiveQueue == null) ? 0 : receiveQueue.hashCode());
    result = prime * result + ((sendQueue == null) ? 0 : sendQueue.hashCode());
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
    BackendMQ other = (BackendMQ) obj;
    if (heartbeatQueue == null) {
      if (other.heartbeatQueue != null)
        return false;
    } else if (!heartbeatQueue.equals(other.heartbeatQueue))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (receiveQueue == null) {
      if (other.receiveQueue != null)
        return false;
    } else if (!receiveQueue.equals(other.receiveQueue))
      return false;
    if (sendQueue == null) {
      if (other.sendQueue != null)
        return false;
    } else if (!sendQueue.equals(other.sendQueue))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "BackendMQ [id=" + id + ", sendQueue=" + sendQueue + ", receiveQueue=" + receiveQueue + ", heartbeatQueue="  + heartbeatQueue + "]";
  }

  @Override
  @JsonIgnore
  public BackendType getType() {
    return BackendType.MQ;
  }

}
