package org.rabix.executor.rest;

public class ExecutorException extends Exception {

  private static final long serialVersionUID = 303635527925358048L;

  public ExecutorException(String message, Throwable t) {
    super(message, t);
  }
}
