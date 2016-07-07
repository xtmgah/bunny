package org.rabix.bindings.protocol.draft4.service;

public class Draft4GlobException extends Exception {

  private static final long serialVersionUID = -5829706409580726263L;

  public Draft4GlobException(String message, Throwable t) {
    super(message, t);
  }
  
  public Draft4GlobException(Throwable t) {
    super(t);
  }
}
