package org.rabix.executor.container;

public class ContainerException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 3829256829373074603L;

  public ContainerException(Throwable e) {
    super(e);
  }
  
  public ContainerException(String message) {
    super(message);
  }
  
  public ContainerException(String message, Throwable e) {
    super(message, e);
  }
  
}
