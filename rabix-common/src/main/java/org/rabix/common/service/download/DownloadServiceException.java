package org.rabix.common.service.download;

public class DownloadServiceException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 5840816335325140572L;

  public DownloadServiceException(String message) {
    super(message);
  }

  public DownloadServiceException(String message, Throwable t) {
    super(message, t);
  }

  public DownloadServiceException(Throwable t) {
    super(t);
  }
  
}
