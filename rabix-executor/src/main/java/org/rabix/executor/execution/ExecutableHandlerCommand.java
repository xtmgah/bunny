package org.rabix.executor.execution;

import org.rabix.bindings.model.Executable.ExecutableStatus;
import org.rabix.executor.handler.ExecutableHandler;
import org.rabix.executor.model.ExecutableData;
import org.rabix.executor.service.ExecutableDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Executable execution command abstraction. 
 */
public abstract class ExecutableHandlerCommand {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * Command types 
   */
  public enum ExecutableHandlerCommandType {
    START, STOP, STATUS
  }

  protected final ExecutableDataService executableDataService;

  public ExecutableHandlerCommand(ExecutableDataService executableDataService) {
    this.executableDataService = executableDataService;
  }

  /**
   * Find {@link ExecutableData} and run command 
   */
  public Result run(String id, String contextId, ExecutableHandler handler) {
    ExecutableData data = null;
    try {
      data = executableDataService.find(id, contextId);
      if (data == null) {
        throw new RuntimeException("No ExecutableData assocated for ID = " + id);
      }
      return run(data, handler, contextId);
    } catch (Exception e) {
      failed(data, "Executor faced a runtime exception.", e);
      executableDataService.save(data, "Executor faced a runtime exception.", ExecutableStatus.FAILED, contextId);
      throw e;
    }
  }

  /**
   * Run command using the {@link ExecutableData} 
   */
  public abstract Result run(ExecutableData executableData, ExecutableHandler handler, String contextId);

  /**
   * Get repeat information. By default, the command is not repeatable.
   */
  public Repeat getRepeat() {
    return null;
  }

  /**
   * Send notification to master about STARTED event 
   */
  protected void started(ExecutableData executableData, String message) {
    logger.info(message);
  }

  /**
   * Send notification to master about FAILED event 
   */
  protected void failed(ExecutableData executableData, String message, Throwable e) {
    logger.error(message, e);
  }

  /**
   * Send notification to master about STOPPED event 
   */
  protected void stopped(ExecutableData executableData, String message) {
    logger.info(message);
  }

  /**
   * Send notification to master about COMPLETED event 
   */
  protected void completed(ExecutableData executableData, String message, Object result) {
    logger.info(message);
  }

  /**
   * Get command type 
   */
  public abstract ExecutableHandlerCommandType getType();
  
  @Override
  public String toString() {
    return getType().name();
  }
  
  /**
   * Simple wrapper for command result
   */
  public static class Result {
    public final boolean isLastCommand;

    public Result(boolean isLastCommand) {
      this.isLastCommand = isLastCommand;
    }
  }

  /**
   * Command repeat information
   */
  public static class Repeat {
    public final long delay;

    public Repeat(long delay) {
      this.delay = delay;
    }
  }
}
