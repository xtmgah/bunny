package org.rabix.executor.execution.command;

import javax.inject.Inject;

import org.rabix.executor.ExecutorException;
import org.rabix.executor.execution.JobHandlerCommand;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.model.JobData;
import org.rabix.executor.model.JobData.JobDataStatus;
import org.rabix.executor.service.JobDataService;

/**
 * Command that stops {@link JobHandler} 
 */
public class StopCommand extends JobHandlerCommand {

  @Inject
  public StopCommand(JobDataService jobDataService) {
    super(jobDataService);
  }

  @Override
  public Result run(JobData jobData, JobHandler handler, String contextId) {
    String jobId = jobData.getJob().getId();
    try {
      handler.stop();

      String message = String.format("Job %s aborted successfully.", jobId);
      jobData = jobDataService.save(jobData, message, JobDataStatus.ABORTED);
      stopped(jobData, message, handler.getEngineStub());
    } catch (ExecutorException e) {
      String message = String.format("Failed to stop %s. %s", jobId, e.toString());
      jobData = jobDataService.save(jobData, message, JobDataStatus.FAILED);
    }
    return new Result(true);
  }

  @Override
  public JobHandlerCommandType getType() {
    return JobHandlerCommandType.STOP;
  }

}
