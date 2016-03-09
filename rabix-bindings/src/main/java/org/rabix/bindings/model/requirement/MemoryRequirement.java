package org.rabix.bindings.model.requirement;

public class MemoryRequirement implements Requirement {

  private final Integer memory;
  
  public MemoryRequirement(Integer memory) {
    this.memory = memory;
  }
  
  public Integer getMemory() {
    return memory;
  }

  @Override
  public String toString() {
    return "MemoryRequirement [memory=" + memory + "]";
  }
  
}
