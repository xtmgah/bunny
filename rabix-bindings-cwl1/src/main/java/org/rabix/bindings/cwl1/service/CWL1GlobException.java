package org.rabix.bindings.cwl1.service;

public class CWL1GlobException extends Exception {

  private static final long serialVersionUID = -5829706409580726263L;

  public CWL1GlobException(String message, Throwable t) {
    super(message, t);
  }
  
  public CWL1GlobException(Throwable t) {
    super(t);
  }
}
