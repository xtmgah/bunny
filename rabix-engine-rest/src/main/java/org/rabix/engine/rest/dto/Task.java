package org.rabix.engine.rest.dto;

import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.model.Context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Task {

  @JsonProperty("id")
  private String id;
  @JsonProperty("app")
  private String app;
  @JsonProperty("inputs")
  private String inputs;
  @JsonProperty("type")
  private ProtocolType type;
  @JsonProperty("context")
  private Context context;
  @JsonProperty("status")
  private TaskStatus status;

  @JsonCreator
  public Task(@JsonProperty("id") String id, @JsonProperty("app") String app, @JsonProperty("inputs") String inputs, @JsonProperty("type") ProtocolType type, @JsonProperty("context") Context context, @JsonProperty("status") TaskStatus status) {
    this.id = id;
    this.type = type;
    this.app = app;
    this.inputs = inputs;
    this.context = context;
    this.status = status;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getApp() {
    return app;
  }

  public void setApp(String app) {
    this.app = app;
  }

  public String getInputs() {
    return inputs;
  }

  public void setInputs(String inputs) {
    this.inputs = inputs;
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
    return "Task [id=" + id + ", app=" + app + ", inputs=" + inputs + ", type=" + type + ", context=" + context + ", status=" + status + "]";
  }

}
