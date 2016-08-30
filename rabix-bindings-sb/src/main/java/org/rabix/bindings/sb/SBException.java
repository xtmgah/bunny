package org.rabix.bindings.sb;

public class SBException extends Exception {

  private static final long serialVersionUID = -7156511203446344280L;

  public SBException(String message) {
    super(message);
  }
  
  public SBException(String message, Throwable t) {
    super(message, t);
  }
}
