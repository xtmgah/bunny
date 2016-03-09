package org.rabix.bindings.model.requirement;

public class CPURequirement implements Requirement {

  private final Integer cpu;
  
  public CPURequirement(Integer cpu) {
    this.cpu = cpu;
  }
  
  public Integer getCpu() {
    return cpu;
  }

  @Override
  public String toString() {
    return "CPURequirement [cpu=" + cpu + "]";
  }
  
}
