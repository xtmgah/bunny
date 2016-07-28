package org.rabix.common.retry;

public class RetryFailedException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -7405053055545840261L;

  public RetryFailedException(String message) {
    super(message);
  }
  
  public RetryFailedException(String message, Exception e) {
    super(message, e);
  }
}