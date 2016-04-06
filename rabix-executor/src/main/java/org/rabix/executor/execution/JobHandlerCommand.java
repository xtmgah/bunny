package org.rabix.executor.execution;

import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.model.JobData;
import org.rabix.executor.service.JobDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Job execution command abstraction. 
 */
public abstract class JobHandlerCommand {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * Command types 
   */
  public enum JobHandlerCommandType {
    START, STOP, STATUS
  }

  protected final JobDataService jobDataService;

  public JobHandlerCommand(JobDataService jobDataService) {
    this.jobDataService = jobDataService;
  }

  /**
   * Find {@link JobData} and run command 
   */
  public Result run(String id, String contextId, JobHandler handler) {
    JobData data = null;
    try {
      data = jobDataService.find(id, contextId);
      if (data == null) {
        throw new RuntimeException("No JobData assocated for ID = " + id);
      }
      return run(data, handler, contextId);
    } catch (Exception e) {
      failed(data, "Executor faced a runtime exception.", e);
      jobDataService.save(data, "Executor faced a runtime exception.", JobStatus.FAILED, contextId);
      throw e;
    }
  }

  /**
   * Run command using the {@link JobData} 
   */
  public abstract Result run(JobData jobData, JobHandler handler, String contextId);

  /**
   * Get repeat information. By default, the command is not repeatable.
   */
  public Repeat getRepeat() {
    return null;
  }

  /**
   * Send notification to master about STARTED event 
   */
  protected void started(JobData jobData, String message) {
    logger.info(message);
  }

  /**
   * Send notification to master about FAILED event 
   */
  protected void failed(JobData jobData, String message, Throwable e) {
    logger.error(message, e);
  }

  /**
   * Send notification to master about STOPPED event 
   */
  protected void stopped(JobData jobData, String message) {
    logger.info(message);
  }

  /**
   * Send notification to master about COMPLETED event 
   */
  protected void completed(JobData jobData, String message, Object result) {
    logger.info(message);
  }

  /**
   * Get command type 
   */
  public abstract JobHandlerCommandType getType();
  
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
