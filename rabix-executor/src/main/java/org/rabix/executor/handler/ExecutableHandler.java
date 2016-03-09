package org.rabix.executor.handler;

import org.rabix.bindings.model.Executable;
import org.rabix.executor.ExecutorException;

public interface ExecutableHandler {

  public final static int DEFAULT_ERROR_CODE = -1;
  public final static int DEFAULT_SUCCESS_CODE = 0;

  /**
   * Start execution
   */
  void start() throws ExecutorException;

  /**
   * Stop execution
   */
  void stop() throws ExecutorException;

  /**
   * Is handler started 
   */
  boolean isStarted() throws ExecutorException;
  
  /**
   * Is handler running
   */
  boolean isRunning() throws ExecutorException;

  /**
   * Get exit status
   */
  int getExitStatus() throws ExecutorException;

  /**
   * Do after-processing
   */
  Executable postprocess(boolean isTerminal) throws ExecutorException;

  /**
   * Is Executable finished successfully or not
   */
  boolean isSuccessful(int processExitCode) throws ExecutorException;
  
  /**
   * Is Executable finished successfully or not
   */
  boolean isSuccessful() throws ExecutorException;
  
}
