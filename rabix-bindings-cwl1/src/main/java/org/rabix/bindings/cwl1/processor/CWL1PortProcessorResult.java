package org.rabix.bindings.cwl1.processor;

public class CWL1PortProcessorResult {

  private Object value;
  private boolean processed;
  
  public CWL1PortProcessorResult(Object value, boolean processed) {
    this.value = value;
    this.processed = processed;
  }

  public Object getValue() {
    return value;
  }

  public boolean isProcessed() {
    return processed;
  }

  @Override
  public String toString() {
    return "PortProcessorResult [value=" + value + ", processed=" + processed + "]";
  }
  
}
