package org.rabix.common.retry;

/**
 * Callback used for cleanup in case that retry fails
 */
public interface RetryCleanupCallback {

  void call();
  
}