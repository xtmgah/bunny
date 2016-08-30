package org.rabix.bindings.sb.expression;

public class SBExpressionException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 8479390663893415044L;

  public SBExpressionException(String message) {
    super(message);
  }
  
  public SBExpressionException(Throwable e) {
    super(e);
  }

  public SBExpressionException(String message, Throwable e) {
    super(message, e);
  }
  
}
