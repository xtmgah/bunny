package org.rabix.bindings.draft2;

public class Draft2Exception extends Exception {

  private static final long serialVersionUID = -7156511203446344280L;

  public Draft2Exception(String message) {
    super(message);
  }
  
  public Draft2Exception(String message, Throwable t) {
    super(message, t);
  }
}
