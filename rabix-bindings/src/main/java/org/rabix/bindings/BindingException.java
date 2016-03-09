package org.rabix.bindings;

public class BindingException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 5183177287123898073L;

  public BindingException(Throwable t) {
    super(t);
  }
  
  public BindingException(String message) {
    super(message);
  }
  
  public BindingException(String message, Throwable t) {
    super(message, t);
  }
  
}
