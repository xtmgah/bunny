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
  @JsonProperty("status")
  private TaskStatus status;

  @JsonCreator
  public Task(@JsonProperty("id") String id, @JsonProperty("payload") String payload, @JsonProperty("type") ProtocolType type, @JsonProperty("context") Context context, @JsonProperty("status") TaskStatus status) {
    this.id = id;
    this.type = type;
    this.payload = payload;
    this.context = context;
    this.status = status;
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

  public TaskStatus getStatus() {
    return status;
  }

  public void setStatus(TaskStatus status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "Task [id=" + id + ", payload=" + payload + ", type=" + type + ", context=" + context + ", status=" + status + "]";
  }

}
