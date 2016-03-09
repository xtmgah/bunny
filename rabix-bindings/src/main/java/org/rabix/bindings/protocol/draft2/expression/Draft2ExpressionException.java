package org.rabix.bindings.protocol.draft2.expression;

public class Draft2ExpressionException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 8479390663893415044L;

  public Draft2ExpressionException(String message) {
    super(message);
  }
  
  public Draft2ExpressionException(Throwable e) {
    super(e);
  }

  public Draft2ExpressionException(String message, Throwable e) {
    super(message, e);
  }
  
}
