package org.rabix.bindings.cwl1;

public class CWL1Exception extends Exception {

  private static final long serialVersionUID = -7156511203446344280L;

  public CWL1Exception(String message) {
    super(message);
  }
  
  public CWL1Exception(String message, Throwable t) {
    super(message, t);
  }
}
