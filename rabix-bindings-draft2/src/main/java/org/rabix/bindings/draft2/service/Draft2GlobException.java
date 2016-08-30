package org.rabix.bindings.draft2.service;

public class Draft2GlobException extends Exception {

  private static final long serialVersionUID = -5829706409580726263L;

  public Draft2GlobException(String message, Throwable t) {
    super(message, t);
  }
  
  public Draft2GlobException(Throwable t) {
    super(t);
  }
}
