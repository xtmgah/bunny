package org.rabix.bindings.cwl1.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CWL1ExpressionTool extends CWL1JobApp {

  @JsonProperty("expression")
  private Object script;

  public Object getScript() {
    return script;
  }

  @Override
  @JsonIgnore
  public CWL1JobAppType getType() {
    return CWL1JobAppType.EXPRESSION_TOOL;
  }

  @Override
  public String toString() {
    return "CWL1ExpressionTool [script=" + script + ", id=" + id + ", context=" + context + ", description=" + description + ", inputs=" + getInputs() + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }
  
}
