package org.rabix.executor.transport;

public interface TransportStub {

  public <T> ResultPair<T> send(String destinationQueue, T entity);

  public <T> ResultPair<T> receive(String sourceQueue, Class<T> clazz);
  
  
  public static class ResultPair<T> {
    private boolean success;

    private T result;

    private String message;
    private Exception exception;

    public ResultPair() {
    }

    public boolean isSuccess() {
      return success;
    }

    public T getResult() {
      return result;
    }

    public String getMessage() {
      return message;
    }

    public Exception getException() {
      return exception;
    }

    public static <T> ResultPair<T> success(T result) {
      ResultPair<T> resultPair = new ResultPair<T>();
      resultPair.success = true;
      resultPair.result = result;
      return resultPair;
    }

    public static <T> ResultPair<T> fail(Exception exception, String message) {
      ResultPair<T> resultPair = new ResultPair<T>();
      resultPair.success = false;
      resultPair.message = message;
      resultPair.exception = exception;
      return resultPair;
    }
  }
}
