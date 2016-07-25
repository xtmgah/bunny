package org.rabix.common.retry;

public class RetryConfig {
  private final Long times;
  private final Long sleepTimeMillis;
  private final Boolean exponentialBackoff;
  private final Class<? extends RetryCleanupCallback> callback;
  private final Long methodTimeoutMillis;

  public RetryConfig(Long times, Long sleepTimeMillis, Long methodTimeoutMillis, Boolean exponentialBackoff, Class<? extends RetryCleanupCallback> callback) {
    this.times = times;
    this.sleepTimeMillis = sleepTimeMillis;
    this.methodTimeoutMillis = methodTimeoutMillis;
    this.exponentialBackoff = exponentialBackoff;
    this.callback = callback;
  }

  public RetryConfig() {
    times = null;
    sleepTimeMillis = null;
    exponentialBackoff = null;
    callback = null;
    methodTimeoutMillis = null;
  }

  public Long getTimes() {
    return times;
  }

  public Long getSleepTimeMillis() {
    return sleepTimeMillis;
  }

  public Boolean getExponentialBackoff() {
    return exponentialBackoff;
  }

  public Class<? extends RetryCleanupCallback> getCallback() {
    return callback;
  }

  public Long getMethodTimeoutMillis() {
    return methodTimeoutMillis;
  }

  public Long getRetryTimes(Retry retryAnnotation) {
    Long times = this.getTimes();
    times = (times != null) ? times : retryAnnotation.times();

    if (times < 0) {
      throw new RuntimeException("Retry times cannot be negative! Times " + times);
    }
    return times;
  }

  public Long getSleepTimeMillis(Retry retryAnnotation) {
    Long sleepTimeMillis = this.getSleepTimeMillis();
    sleepTimeMillis = (sleepTimeMillis != null) ? sleepTimeMillis : retryAnnotation.sleepTimeMillis();

    if (sleepTimeMillis < 0) {
      throw new RuntimeException("Sleep time cannot be negative! Sleep time " + sleepTimeMillis);
    }
    return sleepTimeMillis;
  }

  public Long getMethodTimeoutMillis(Retry retryAnnotation) {
    Long methodTimeoutMillis = this.getMethodTimeoutMillis();
    methodTimeoutMillis = (methodTimeoutMillis != null) ? methodTimeoutMillis : retryAnnotation.methodTimeoutMillis();

    if (methodTimeoutMillis < 0) {
      throw new RuntimeException("Method timeout cannot be negative! Method timeout " + methodTimeoutMillis);
    }
    return methodTimeoutMillis;
  }

  public boolean getExponentialBackoff(Retry retryAnnotation) {
    Boolean exponentialBackoff = this.getExponentialBackoff();
    return (exponentialBackoff != null) ? exponentialBackoff : retryAnnotation.exponentialBackoff();
  }

  public Class<? extends RetryCleanupCallback> getCallback(Retry retryAnnotation) {
    Class<? extends RetryCleanupCallback> callback = this.getCallback();
    return (callback != null) ? callback : retryAnnotation.callback();
  }
}