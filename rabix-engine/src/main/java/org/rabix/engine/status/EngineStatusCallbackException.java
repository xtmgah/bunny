package org.rabix.engine.status;

public class EngineStatusCallbackException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -3644662150384817316L;

  public EngineStatusCallbackException(String message) {
    super(message);
  }

  public EngineStatusCallbackException(Throwable t) {
    super(t);
  }

  public EngineStatusCallbackException(String message, Throwable t) {
    super(message, t);
  }

}
