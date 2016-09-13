package org.rabix.bindings.cwl1.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CWL1WagnerPythonTool extends CWL1JobApp {

  @JsonProperty("function")
  private Object function;
  
  public Object getFunction() {
    return function;
  }

  @Override
  @JsonIgnore
  public CWL1JobAppType getType() {
    return CWL1JobAppType.WAGNER_PYTHON_TOOL;
  }

  @Override
  public String toString() {
    return "CWL1WagnerPythonTool [function=" + function + ", id=" + id + ", getInputs()=" + getInputs()
        + ", getOutputs()=" + getOutputs() + "]";
  }

}
