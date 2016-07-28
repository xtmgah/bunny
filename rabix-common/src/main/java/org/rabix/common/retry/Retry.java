package org.rabix.common.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Retry {

  public final static long SLEEP_TIME_MILLIS = 1000L;

  long times() default Long.MAX_VALUE;

  long sleepTimeMillis() default SLEEP_TIME_MILLIS;
  
  long methodTimeoutMillis() default 0;
  
  boolean exponentialBackoff() default true;

  Class<? extends RetryCleanupCallback> callback() default RetryCleanupCallback.class;
}