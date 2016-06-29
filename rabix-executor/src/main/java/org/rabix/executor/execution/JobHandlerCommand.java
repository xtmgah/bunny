package org.rabix.executor.execution;

import java.util.Map;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.common.logging.VerboseLogger;
import org.rabix.executor.engine.EngineStub;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.model.JobData;
import org.rabix.executor.model.JobData.JobDataStatus;
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
      failed(data, "Executor faced a runtime exception.", handler.getEngineStub(), e);
      data = jobDataService.save(data, "Executor faced a runtime exception.", JobDataStatus.FAILED);
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
  protected void started(JobData jobData, String message, EngineStub<?,?,?> engineStub) {
    logger.info(message);

    Job job = Job.cloneWithStatus(jobData.getJob(), JobStatus.RUNNING);
    jobData = JobData.cloneWithJob(jobData, job);
    jobDataService.save(jobData);
    engineStub.send(job);
    
    VerboseLogger.log(String.format("Job %s has started", job.getName()));
  }

  /**
   * Send notification to master about FAILED event 
   */
  protected void failed(JobData jobData, String message, EngineStub<?,?,?> engineStub, Throwable e) {
    logger.error(message, e);

    Job job = Job.cloneWithStatus(jobData.getJob(), JobStatus.FAILED);
    jobData = JobData.cloneWithJob(jobData, job);
    jobDataService.save(jobData);
    engineStub.send(job);
    
    VerboseLogger.log(String.format("%s", message));
  }

  /**
   * Send notification to master about STOPPED event 
   */
  protected void stopped(JobData jobData, String message, EngineStub<?,?,?> engineStub) {
    logger.info(message);

    Job job = Job.cloneWithStatus(jobData.getJob(), JobStatus.ABORTED);
    jobData = JobData.cloneWithJob(jobData, job);
    jobDataService.save(jobData);
    engineStub.send(job);
    
    VerboseLogger.log(String.format("Job %s has stopped", job.getName()));
  }

  static volatile int count = 0;
  
  /**
   * Send notification to master about COMPLETED event 
   */
  protected void completed(JobData jobData, String message, Map<String, Object> result, EngineStub<?,?,?> engineStub) {
    logger.info(message);

    Job job = Job.cloneWithStatus(jobData.getJob(), JobStatus.COMPLETED);
    job = Job.cloneWithOutputs(job, result);
    jobData = JobData.cloneWithJob(jobData, job);
    jobDataService.save(jobData);
    engineStub.send(job);
    
    VerboseLogger.log(String.format("Job %s has completed", job.getName()));
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
