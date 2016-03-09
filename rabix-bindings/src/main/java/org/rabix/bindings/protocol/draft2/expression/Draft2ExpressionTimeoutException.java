package org.rabix.bindings.protocol.draft2.expression;

public class Draft2ExpressionTimeoutException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1953101471734370702L;

  public Draft2ExpressionTimeoutException(String message) {
    super(message);
  }
  
  public Draft2ExpressionTimeoutException(String message, Throwable e) {
    super(message, e);
  }

}
