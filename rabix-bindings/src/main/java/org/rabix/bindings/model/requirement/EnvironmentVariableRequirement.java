package org.rabix.bindings.model.requirement;

import java.util.Map;

public class EnvironmentVariableRequirement implements Requirement  {

  private final Map<String, String> variables;
  
  public EnvironmentVariableRequirement(Map<String, String> variables) {
    this.variables = variables;
  }
  
  public Map<String, String> getVariables() {
    return variables;
  }

  @Override
  public String toString() {
    return "EnvironmentVariableRequirement [variables=" + variables + "]";
  }
  
}
