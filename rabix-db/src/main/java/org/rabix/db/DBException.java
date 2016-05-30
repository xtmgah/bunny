package org.rabix.db;

public class DBException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 6195249097405022447L;
  
  public DBException(Throwable e) {
    super(e);
  }
  
  public DBException(String message) {
    super(message);
  }
  
  public DBException(String message, Throwable e) {
    super(message, e);
  }

}
