package org.rabix.bindings.cwl1.expression;

public class Draft3ExpressionTimeoutException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1953101471734370702L;

  public Draft3ExpressionTimeoutException(String message) {
    super(message);
  }
  
  public Draft3ExpressionTimeoutException(String message, Throwable e) {
    super(message, e);
  }

}
