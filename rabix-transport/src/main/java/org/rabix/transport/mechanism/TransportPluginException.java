package org.rabix.transport.mechanism;

public class TransportPluginException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = -1839916097572304795L;
  
  public TransportPluginException(String message) {
    super(message);
  }
  
  public TransportPluginException(String message, Throwable t) {
    super(message, t);
  }

  public TransportPluginException(Throwable e) {
    super(e);
  }

}
