package org.rabix.bindings.draft2.processor;

public class Draft2PortProcessorResult {

  private Object value;
  private boolean processed;
  
  public Draft2PortProcessorResult(Object value, boolean processed) {
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
