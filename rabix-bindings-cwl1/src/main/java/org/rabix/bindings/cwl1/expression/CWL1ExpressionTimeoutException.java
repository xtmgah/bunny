package org.rabix.bindings.cwl1.expression;

public class CWL1ExpressionTimeoutException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1953101471734370702L;

  public CWL1ExpressionTimeoutException(String message) {
    super(message);
  }
  
  public CWL1ExpressionTimeoutException(String message, Throwable e) {
    super(message, e);
  }

}
