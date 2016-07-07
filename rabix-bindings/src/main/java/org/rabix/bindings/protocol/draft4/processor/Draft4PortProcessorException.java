package org.rabix.bindings.protocol.draft4.processor;

public class Draft4PortProcessorException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1232749409810371749L;

  public Draft4PortProcessorException(String message) {
    super(message);
  }
  
  public Draft4PortProcessorException(Throwable t) {
    super(t);
  }
  
  public Draft4PortProcessorException(String message, Throwable t) {
    super(message, t);
  }
  
}
