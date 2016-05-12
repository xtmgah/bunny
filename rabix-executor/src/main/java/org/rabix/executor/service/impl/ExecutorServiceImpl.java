package org.rabix.executor.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.executor.engine.EngineStub;
import org.rabix.executor.engine.EngineStubActiveMQ;
import org.rabix.executor.engine.EngineStubLocal;
import org.rabix.executor.engine.EngineStubRabbitMQ;
import org.rabix.executor.execution.JobHandlerCommandDispatcher;
import org.rabix.executor.execution.command.StartCommand;
import org.rabix.executor.execution.command.StatusCommand;
import org.rabix.executor.execution.command.StopCommand;
import org.rabix.executor.model.JobData;
import org.rabix.executor.service.ExecutorService;
import org.rabix.executor.service.JobDataService;
import org.rabix.transport.backend.Backend;
import org.rabix.transport.backend.impl.BackendActiveMQ;
import org.rabix.transport.backend.impl.BackendLocal;
import org.rabix.transport.backend.impl.BackendRabbitMQ;
import org.rabix.transport.mechanism.TransportPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ExecutorServiceImpl implements ExecutorService {

  private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceImpl.class);

  private final JobDataService jobDataService;
  private final JobHandlerCommandDispatcher jobHandlerCommandDispatcher;

  private final Provider<StopCommand> stopCommandProvider;
  private final Provider<StartCommand> startCommandProvider;
  private final Provider<StatusCommand> statusCommandProvider;

  private final AtomicBoolean stopped = new AtomicBoolean(false);

  private EngineStub engineStub;
  private Configuration configuration;

  @Inject
  public ExecutorServiceImpl(JobDataService jobDataService, JobHandlerCommandDispatcher jobHandlerCommandDispatcher,
      Provider<StopCommand> stopCommandProvider, Provider<StartCommand> startCommandProvider,
      Provider<StatusCommand> statusCommandProvider, Configuration configuration) {
    this.jobDataService = jobDataService;
    this.stopCommandProvider = stopCommandProvider;
    this.startCommandProvider = startCommandProvider;
    this.statusCommandProvider = statusCommandProvider;
    this.jobHandlerCommandDispatcher = jobHandlerCommandDispatcher;
  }

  @Override
  public void initialize(Backend backend) {
    try {
      switch (backend.getType()) {
      case LOCAL:
        engineStub = new EngineStubLocal((BackendLocal) backend, this, configuration);
        break;
      case RABBIT_MQ:
        engineStub = new EngineStubRabbitMQ((BackendRabbitMQ) backend, this, configuration);
        break;
      case ACTIVE_MQ:
        engineStub = new EngineStubActiveMQ((BackendActiveMQ) backend, this, configuration);
      default:
        break;
      }
      engineStub.start();
    } catch (TransportPluginException e) {
      logger.error("Failed to initialize Executor", e);
      throw new RuntimeException("Failed to initialize Executor", e);
    }
  }

  @Override
  public void start(final Job job, String contextId) {
    logger.debug("start(id={}, important={}, uploadOutputs={})", job.getId());

    final JobData jobData = new JobData(job, JobStatus.READY, false, false);
    jobDataService.save(jobData, contextId);

    jobHandlerCommandDispatcher.dispatch(jobData, startCommandProvider.get(), engineStub);
    jobHandlerCommandDispatcher.dispatch(jobData, statusCommandProvider.get(), engineStub);
  }

  @Override
  public void stop(String jobId, String contextId) {
    logger.debug("stop(id={})", jobId);

    final JobData jobData = jobDataService.find(jobId, contextId);
    jobHandlerCommandDispatcher.dispatch(jobData, stopCommandProvider.get(), engineStub);
  }

  @Override
  public JobStatus findStatus(String jobId, String contextId) {
    logger.debug("findStatus(id={})", jobId);

    JobData jobData = jobDataService.find(jobId, contextId);
    if (jobData != null) {
      return jobData.getStatus();
    }
    return null;
  }

  @Override
  public void shutdown(Boolean stopEverything) {
    logger.debug("shutdown(stopEverything={})", stopEverything);

    List<JobData> jobsToStop = jobDataService.find(JobStatus.STARTED, JobStatus.READY);

    int abortedJobsCount = 0;
    if (jobsToStop != null) {
      for (JobData jobData : jobsToStop) {
        if (!stopEverything && jobData.isImportant()) {
          continue;
        }
        jobHandlerCommandDispatcher.dispatch(jobData, stopCommandProvider.get(), engineStub);
        abortedJobsCount++;
      }
    }
    stopped.set(true);
    String message = String.format("Shutdown%s executed. Worker has stopped %d %s.", stopEverything ? " now" : "",
        abortedJobsCount, abortedJobsCount == 1 ? "job" : "jobs");
    logger.info(message);
  }

  @Override
  public Map<String, Object> getResult(String id, String contextId) {
    JobData jobData = jobDataService.find(id, contextId);
    return jobData.getResult();
  }

  @Override
  public boolean isRunning(String id, String contextId) {
    logger.debug("isRunning(id={})", id);

    JobData jobData = jobDataService.find(id, contextId);
    if (jobData != null && !isFinished(jobData.getStatus())) {
      logger.info("Command line tool {} is running. The status is {}", id, jobData.getStatus());
      return true;
    }
    logger.info("Command line tool {} is not running", id);
    return false;
  }

  private boolean isFinished(JobStatus jobStatus) {
    switch (jobStatus) {
    case COMPLETED:
    case FAILED:
    case ABORTED:
      return true;
    default:
      return false;
    }
  }

  @Override
  public boolean isStopped() {
    return stopped.get();

  }

}
