package org.rabix.bindings.filemapper;

public class FileMappingException extends Exception {
  
  private static final long serialVersionUID = 1L;

  public FileMappingException(String message) {
    super(message);
  }
  
  public FileMappingException(Throwable cause) {
    super(cause);
  }
  
  public FileMappingException(String message, Throwable cause) {
    super(message, cause);
  }
}
