package org.rabix.common.json.processor;

public class BeanProcessorException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -2806321215682217514L;

  public BeanProcessorException(String message, Exception e) {
    super(message, e);
  }
  
  public BeanProcessorException(String message) {
    super(message);
  }
  
  public BeanProcessorException(Exception e) {
    super(e);
  }
  
}
