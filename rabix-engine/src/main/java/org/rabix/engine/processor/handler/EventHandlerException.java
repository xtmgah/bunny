package org.rabix.engine.processor.handler;

public class EventHandlerException extends Exception {

  private static final long serialVersionUID = 6168210613413767464L;
  
  public EventHandlerException(String message) {
    super(message);
  }
  
  public EventHandlerException(Throwable throwable) {
    super(throwable);
  }
  
  public EventHandlerException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
