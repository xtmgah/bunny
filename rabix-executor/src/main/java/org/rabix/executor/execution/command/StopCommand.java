package org.rabix.executor.execution.command;

import javax.inject.Inject;

import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.executor.ExecutorException;
import org.rabix.executor.execution.JobHandlerCommand;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.model.JobData;
import org.rabix.executor.mq.MQConfig;
import org.rabix.executor.mq.MQTransportStub;
import org.rabix.executor.service.JobDataService;

/**
 * Command that stops {@link JobHandler} 
 */
public class StopCommand extends JobHandlerCommand {

  @Inject
  public StopCommand(JobDataService jobDataService, MQTransportStub mqTransportStub, MQConfig mqConfig) {
    super(jobDataService, mqTransportStub, mqConfig);
  }

  @Override
  public Result run(JobData jobData, JobHandler handler, String contextId) {
    String jobId = jobData.getJob().getId();
    try {
      handler.stop();

      String message = String.format("Job %s aborted successfully.", jobId);
      jobDataService.save(jobData, message, JobStatus.ABORTED, contextId);
      stopped(jobData, message);
    } catch (ExecutorException e) {
      String message = String.format("Failed to stop %s. %s", jobId, e.toString());
      jobDataService.save(jobData, message, JobStatus.FAILED, contextId);
    }
    return new Result(true);
  }

  @Override
  public JobHandlerCommandType getType() {
    return JobHandlerCommandType.STOP;
  }

}
