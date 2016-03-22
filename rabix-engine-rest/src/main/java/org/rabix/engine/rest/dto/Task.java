package org.rabix.engine.rest.dto;

import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.model.Context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Task {

  @JsonProperty("id")
  private String id;
  @JsonProperty("payload")
  private String payload;
  @JsonProperty("type")
  private ProtocolType type;
  @JsonProperty("context")
  private Context context;
  @JsonProperty("completed")
  private boolean completed;

  @JsonCreator
  public Task(@JsonProperty("id") String id, @JsonProperty("payload") String payload, @JsonProperty("type") ProtocolType type, @JsonProperty("context") Context context, @JsonProperty("completed") boolean completed) {
    this.id = id;
    this.type = type;
    this.payload = payload;
    this.context = context;
    this.completed = completed;
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

  public Context getContext() {
    return context;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  @Override
  public String toString() {
    return "Task [id=" + id + ", payload=" + payload + ", type=" + type + ", context=" + context + ", completed=" + completed + "]";
  }

}
