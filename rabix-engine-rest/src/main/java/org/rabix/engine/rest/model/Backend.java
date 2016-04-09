package org.rabix.engine.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Backend {

  @JsonProperty("id")
  private final String id;
  @JsonProperty("broker")
  private final String broker;
  @JsonProperty("sendQueue")
  private final String sendQueue;
  @JsonProperty("receiveQueue")
  private final String receiveQueue;
  @JsonProperty("heartbeatQueue")
  private final String heartbeatQueue;
  
  public Backend(@JsonProperty("id") String id, @JsonProperty("broker") String broker, @JsonProperty("sendQueue") String sendQueue, @JsonProperty("receiveQueue") String receiveQueue, @JsonProperty("heartbeatQueue") String heartbeatQueue) {
    this.id = id;
    this.broker = broker;
    this.sendQueue = sendQueue;
    this.receiveQueue = receiveQueue;
    this.heartbeatQueue = heartbeatQueue;
  }

  public static Backend cloneWithID(Backend backend, String id) {
    return new Backend(id, backend.broker, backend.sendQueue, backend.receiveQueue, backend.heartbeatQueue);
  }
  
  public String getId() {
    return id;
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
    Backend other = (Backend) obj;
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
    return "Backend [id=" + id + ", sendQueue=" + sendQueue + ", receiveQueue=" + receiveQueue + ", heartbeatQueue="  + heartbeatQueue + "]";
  }

}
