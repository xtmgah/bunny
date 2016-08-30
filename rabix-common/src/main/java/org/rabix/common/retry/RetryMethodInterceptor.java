package org.rabix.common.retry;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryMethodInterceptor implements MethodInterceptor {

  private final static Logger logger = LoggerFactory.getLogger(RetryMethodInterceptor.class);

  private final static ExecutorService timeoutExecutor = Executors.newFixedThreadPool(5);

  private final RetryConfig config;

  public RetryMethodInterceptor() {
    config = new RetryConfig();
  }

  public RetryMethodInterceptor(RetryConfig config) {
    this.config = config;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    Method method = invocation.getMethod();

    Retry retryAnnotation = method.getAnnotation(Retry.class);

    Long times = config.getRetryTimes(retryAnnotation);
    Long sleepTimeMillis = config.getSleepTimeMillis(retryAnnotation);
    Long methodTimeoutMillis = config.getMethodTimeoutMillis(retryAnnotation);
    boolean isExponential = config.getExponentialBackoff(retryAnnotation);
    Class<? extends RetryCleanupCallback> callbackClass = config.getCallback(retryAnnotation);

    for (int i = 0; i < times; i++) {
      try {
        return methodTimeoutMillis > 0 ? callWithTimeout(invocation, methodTimeoutMillis) : invocation.proceed();
      } catch (Exception e) {
        logger.error("Method invocation failed. Method " + method, e);
        sleep(i, sleepTimeMillis, isExponential);
      }
    }

    if (RetryCleanupCallback.class != callbackClass) {
      RetryCleanupCallback callback = callbackClass.newInstance();
      callback.call();
    }
    throw new RetryFailedException("Failed to proceed method invocation. Method " + method);
  }

  /**
   * Sleep logic
   */
  private void sleep(int iteration, long sleepTime, boolean isExponential) {
    long currentSleepTime = isExponential ? (long) (sleepTime * Math.pow(2, iteration)) : sleepTime;

    try {
      logger.info("Trying to sleep for " + currentSleepTime + " millis.");
      Thread.sleep(currentSleepTime);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Call method with timeout
   */
  private Object callWithTimeout(final MethodInvocation invocation, long timeout) throws Exception {
    Callable<Object> task = new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        try {
          return invocation.proceed();
        } catch (Throwable e) {
          throw new Exception(e);
        }
      }
    };
    Future<Object> future = timeoutExecutor.submit(task);
    return future.get(timeout, TimeUnit.MILLISECONDS);
  }
}
