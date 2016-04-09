package org.rabix.bindings.protocol.rabix.bean;

import java.util.Map;

public class RabixJob {

  private String id;
  private RabixJobApp app;
  private Map<String, Object> inputs;
  private Map<String, Object> outputs;

  public RabixJob(String id, RabixJobApp app, Map<String, Object> inputs, Map<String, Object> outputs) {
    super();
    this.id = id;
    this.app = app;
    this.inputs = inputs;
    this.outputs = outputs;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public RabixJobApp getApp() {
    return app;
  }

  public void setApp(RabixJobApp app) {
    this.app = app;
  }

  public Map<String, Object> getInputs() {
    return inputs;
  }

  public void setInputs(Map<String, Object> inputs) {
    this.inputs = inputs;
  }

  public Map<String, Object> getOutputs() {
    return outputs;
  }

  public void setOutputs(Map<String, Object> outputs) {
    this.outputs = outputs;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((app == null) ? 0 : app.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
    result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
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
    RabixJob other = (RabixJob) obj;
    if (app == null) {
      if (other.app != null)
        return false;
    } else if (!app.equals(other.app))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (inputs == null) {
      if (other.inputs != null)
        return false;
    } else if (!inputs.equals(other.inputs))
      return false;
    if (outputs == null) {
      if (other.outputs != null)
        return false;
    } else if (!outputs.equals(other.outputs))
      return false;
    return true;
  }

}
