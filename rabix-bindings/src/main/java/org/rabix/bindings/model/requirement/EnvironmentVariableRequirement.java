package org.rabix.bindings.model.requirement;

import java.util.List;

public class EnvironmentVariableRequirement implements Requirement  {

  private final List<String> variables;
  
  public EnvironmentVariableRequirement(List<String> variables) {
    this.variables = variables;
  }
  
  public List<String> getVariables() {
    return variables;
  }

  @Override
  public String toString() {
    return "EnvironmentVariableRequirement [variables=" + variables + "]";
  }
  
}
