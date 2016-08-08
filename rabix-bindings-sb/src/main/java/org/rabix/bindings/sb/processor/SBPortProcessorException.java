package org.rabix.bindings.sb.processor;

public class SBPortProcessorException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1232749409810371749L;

  public SBPortProcessorException(String message) {
    super(message);
  }
  
  public SBPortProcessorException(Throwable t) {
    super(t);
  }
  
  public SBPortProcessorException(String message, Throwable t) {
    super(message, t);
  }
  
}
