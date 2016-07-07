package org.rabix.bindings.protocol.draft4.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Draft4ExpressionTool extends Draft4JobApp {

  @JsonProperty("expression")
  private Object script;

  public Object getScript() {
    return script;
  }

  @Override
  @JsonIgnore
  public Draft4JobAppType getType() {
    return Draft4JobAppType.EXPRESSION_TOOL;
  }

  @Override
  public String toString() {
    return "Draft4ExpressionTool [script=" + script + ", id=" + id + ", context=" + context + ", description=" + description + ", inputs=" + getInputs() + ", outputs=" + getOutputs() + ", requirements=" + requirements + "]";
  }
  
}
