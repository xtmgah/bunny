package org.rabix.executor.execution.command;

import javax.inject.Inject;

import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.executor.ExecutorException;
import org.rabix.executor.execution.JobHandlerCommand;
import org.rabix.executor.handler.JobHandler;
import org.rabix.executor.model.JobData;
import org.rabix.executor.service.JobDataService;
import org.rabix.executor.transport.TransportQueueConfig;
import org.rabix.executor.transport.TransportStub;

/**
 * Command that starts {@link JobHandler}
 */
public class StartCommand extends JobHandlerCommand {

  @Inject
  public StartCommand(JobDataService jobDataService, TransportStub mqTransportStub, TransportQueueConfig mqConfig) {
    super(jobDataService, mqTransportStub, mqConfig);
  }

  @Override
  public Result run(JobData data, JobHandler handler, String contextId) {
    Job job = data.getJob();
    try {
      handler.start();
      jobDataService.save(data, "Job " + job.getId() + " started successfully.", JobStatus.STARTED, contextId);
      started(data, "Job " + job.getId() + " started successfully.");
    } catch (ExecutorException e) {
      String message = String.format("Failed to start %s. %s", job.getId(), e.toString());
      jobDataService.save(data, message, JobStatus.FAILED, contextId);
      failed(data, message, e);
      return new Result(true);
    }
    return new Result(false);
  }

  @Override
  public JobHandlerCommandType getType() {
    return JobHandlerCommandType.START;
  }

}
