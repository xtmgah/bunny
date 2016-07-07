package org.rabix.bindings.protocol.draft4;

public class Draft4Exception extends Exception {

  private static final long serialVersionUID = -7156511203446344280L;

  public Draft4Exception(String message) {
    super(message);
  }
  
  public Draft4Exception(String message, Throwable t) {
    super(message, t);
  }
}
