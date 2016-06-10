package org.rabix.engine.rest.db;

import org.rabix.transport.backend.Backend;

public class BackendRecord {

  private String id;
  private long heartbeatTime;
  private boolean active;
  private Backend backend;
  
  public BackendRecord() {
  }
  
  public BackendRecord(String id, long heartbeatTime, boolean active, Backend backend) {
    this.id = id;
    this.heartbeatTime = heartbeatTime;
    this.active = active;
    this.backend = backend;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getHeartbeatTime() {
    return heartbeatTime;
  }

  public void setHeartbeatTime(long heartbeatTime) {
    this.heartbeatTime = heartbeatTime;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Backend getBackend() {
    return backend;
  }

  public void setBackend(Backend backend) {
    this.backend = backend;
  }

  @Override
  public String toString() {
    return "BackendRecord [id=" + id + ", heartbeatTime=" + heartbeatTime + ", active=" + active + ", backend=" + backend + "]";
  }
  
}
