package org.rabix.common.retry;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * This module binds retry logic to methods annotated with {@code Retry}
 * annotation.
 * 
 * Note: retry logic is applied in a synchronous manner.
 */
public class RetryInterceptorModule extends AbstractModule {

  private final RetryConfig retryConfig;

  public RetryInterceptorModule() {
    retryConfig = new RetryConfig();
  }

  public RetryInterceptorModule(RetryConfig retryConfig) {
    this.retryConfig = retryConfig;
  }

  @Override
  protected void configure() {
    bindInterceptor(Matchers.any(), Matchers.annotatedWith(Retry.class), new RetryMethodInterceptor(retryConfig));
  }

}