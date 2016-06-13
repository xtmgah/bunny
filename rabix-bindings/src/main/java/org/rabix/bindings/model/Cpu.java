package org.rabix.bindings.model;

public class Cpu {

  private Long min;
  private Long max;
  
  public Cpu(Long min, Long max) {
    this.min = min;
    this.max = max;
  }

  public Long getMin() {
    return min;
  }

  public void setMin(Long min) {
    this.min = min;
  }

  public Long getMax() {
    return max;
  }

  public void setMax(Long max) {
    this.max = max;
  }

  @Override
  public String toString() {
    return "Cpu [min=" + min + ", max=" + max + "]";
  }
  
}
