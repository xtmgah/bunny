package org.rabix.engine.rest.dto;

import org.rabix.bindings.ProtocolType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Task {

  @JsonProperty("id")
  private String id;
  @JsonProperty("payload")
  private String payload;
  @JsonProperty("type")
  private ProtocolType type;
  @JsonProperty("completed")
  private boolean completed;

  @JsonCreator
  public Task(@JsonProperty("id") String id, @JsonProperty("payload") String payload, @JsonProperty("type") ProtocolType type, @JsonProperty("completed") boolean completed) {
    this.id = id;
    this.type = type;
    this.completed = completed;
    this.payload = payload;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean running) {
    this.completed = running;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public ProtocolType getType() {
    return type;
  }

  public void setType(ProtocolType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Task [payload=" + payload + ", type=" + type + "]";
  }

}
