package org.rabix.bindings.sb.service;

public class SBGlobException extends Exception {

  private static final long serialVersionUID = -5829706409580726263L;

  public SBGlobException(String message, Throwable t) {
    super(message, t);
  }
  
  public SBGlobException(Throwable t) {
    super(t);
  }
}
