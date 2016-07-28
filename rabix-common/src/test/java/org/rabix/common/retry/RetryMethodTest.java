package org.rabix.common.retry;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

@Test(groups = { "functional" })
public class RetryMethodTest {

  /***/
  public static class GoodRetriableClient {
    public int sendCount = 0;

    @Retry(times = 1, sleepTimeMillis = 10)
    public void send() {
      sendCount++;
    }
  }

  /***/
  public static class BadRetriableClient {
    @Retry(times = 2, sleepTimeMillis = 10, callback = RetryCleanupCallbackImpl.class)
    public void send() {
      throw new RuntimeException("Permanent problem!");
    }
  }

  /***/
  public static class FailTwiceClient {
    public int failCount;
    public int sendCount;

    @Retry(times = 5, sleepTimeMillis = 10, callback = RetryCleanupCallbackImpl.class)
    public void send() {
      if (failCount < 2) {
        failCount++;
        throw new RuntimeException("Temporary problem! #" + failCount);
      } else {
        sendCount++;
      }
    }
  }

  /***/
  public static class RetryCleanupCallbackImpl implements RetryCleanupCallback {
    public static int cleanupCount = 0;

    @Override
    public void call() {
      cleanupCount++;
    }
  }

  private BadRetriableClient badRetriableClient;
  private GoodRetriableClient goodRetriableClient;
  private FailTwiceClient failTwiceClient;

  @BeforeMethod
  public void before() {
    Injector injector = Guice.createInjector(new RetryInterceptorModule());
    badRetriableClient = injector.getInstance(BadRetriableClient.class);
    goodRetriableClient = injector.getInstance(GoodRetriableClient.class);
    failTwiceClient = injector.getInstance(FailTwiceClient.class);
  }

  @Test
  public void testGood() {
    goodRetriableClient.sendCount = 0;
    goodRetriableClient.send();
    goodRetriableClient.send();
    goodRetriableClient.send();
    Assert.assertEquals(goodRetriableClient.sendCount, 3);
  }

  @Test
  public void testBad() {
    RetryCleanupCallbackImpl.cleanupCount = 0;
    try {
      badRetriableClient.send();
      Assert.fail("expected RetryFailedException");
    } catch (RetryFailedException e) {
      Assert.assertEquals(RetryCleanupCallbackImpl.cleanupCount, 1);
    }
  }
  
  @Test
  public void testFailTwice(){
    failTwiceClient.failCount = 0;
    failTwiceClient.sendCount = 0;
    failTwiceClient.send();
    Assert.assertEquals(failTwiceClient.failCount, 2);
    Assert.assertEquals(failTwiceClient.sendCount, 1);
  }
}