package org.rabix.bindings.protocol.draft4.expression;

public class Draft4ExpressionTimeoutException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1953101471734370702L;

  public Draft4ExpressionTimeoutException(String message) {
    super(message);
  }
  
  public Draft4ExpressionTimeoutException(String message, Throwable e) {
    super(message, e);
  }

}
