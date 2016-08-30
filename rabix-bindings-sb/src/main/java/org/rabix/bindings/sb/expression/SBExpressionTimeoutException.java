package org.rabix.bindings.sb.expression;

public class SBExpressionTimeoutException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1953101471734370702L;

  public SBExpressionTimeoutException(String message) {
    super(message);
  }
  
  public SBExpressionTimeoutException(String message, Throwable e) {
    super(message, e);
  }

}
