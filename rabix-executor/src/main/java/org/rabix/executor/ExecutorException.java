package org.rabix.executor;

public class ExecutorException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 8732848609134528210L;

  public ExecutorException(Throwable e) {
    super(e);
  }
  
  public ExecutorException(String message) {
    super(message);
  }

  public ExecutorException(String message, Throwable e) {
    super(message, e);
  }

}
