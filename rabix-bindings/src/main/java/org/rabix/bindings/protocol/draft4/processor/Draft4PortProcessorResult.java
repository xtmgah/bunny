package org.rabix.bindings.protocol.draft4.processor;

public class Draft4PortProcessorResult {

  private Object value;
  private boolean processed;
  
  public Draft4PortProcessorResult(Object value, boolean processed) {
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
