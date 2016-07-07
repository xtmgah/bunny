package org.rabix.bindings.protocol.draft4.expression;

public class Draft4ExpressionException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 8479390663893415044L;

  public Draft4ExpressionException(String message) {
    super(message);
  }
  
  public Draft4ExpressionException(Throwable e) {
    super(e);
  }

  public Draft4ExpressionException(String message, Throwable e) {
    super(message, e);
  }
  
}
