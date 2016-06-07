package org.rabix.engine.service;

public class EngineServiceException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 4793160594130473649L;

  public EngineServiceException(String message, Throwable t) {
    super(message, t);
  }
  
  public EngineServiceException(String m) {
    super(m);
  }
  
  public EngineServiceException(Throwable t) {
    super(t);
  }
}
