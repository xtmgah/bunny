package org.rabix.bindings.protocol.draft4.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Draft4WagnerPythonTool extends Draft4JobApp {

  @JsonProperty("function")
  private Object function;
  
  public Object getFunction() {
    return function;
  }

  @Override
  @JsonIgnore
  public Draft4JobAppType getType() {
    return Draft4JobAppType.WAGNER_PYTHON_TOOL;
  }

  @Override
  public String toString() {
    return "Draft4WagnerPythonTool [function=" + function + ", id=" + id + ", getInputs()=" + getInputs()
        + ", getOutputs()=" + getOutputs() + "]";
  }

}
