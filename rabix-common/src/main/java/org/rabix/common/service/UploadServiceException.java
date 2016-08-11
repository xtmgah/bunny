package org.rabix.common.service;

public class UploadServiceException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 791966926753484849L;

  public UploadServiceException(String message) {
    super(message);
  }

  public UploadServiceException(String message, Throwable t) {
    super(message, t);
  }

  public UploadServiceException(Throwable t) {
    super(t);
  }

}
