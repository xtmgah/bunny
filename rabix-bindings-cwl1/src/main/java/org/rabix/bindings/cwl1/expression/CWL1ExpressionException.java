package org.rabix.bindings.cwl1.expression;

public class CWL1ExpressionException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 8479390663893415044L;

  public CWL1ExpressionException(String message) {
    super(message);
  }
  
  public CWL1ExpressionException(Throwable e) {
    super(e);
  }

  public CWL1ExpressionException(String message, Throwable e) {
    super(message, e);
  }
  
}
