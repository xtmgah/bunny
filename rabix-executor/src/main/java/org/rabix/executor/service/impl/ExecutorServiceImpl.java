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
import org.rabix.executor.model.JobData;
import org.rabix.executor.model.JobData.JobDataStatus;
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

public class ExecutorServiceImpl implements ExecutorService {

  private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceImpl.class);

  private final JobDataService jobDataService;

  private final AtomicBoolean stopped = new AtomicBoolean(false);

  private EngineStub engineStub;
  private Configuration configuration;

  @Inject
  public ExecutorServiceImpl(JobDataService jobDataService, Configuration configuration) {
    this.configuration = configuration;
    this.jobDataService = jobDataService;
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
      jobDataService.initialize(engineStub);
    } catch (TransportPluginException e) {
      logger.error("Failed to initialize Executor", e);
      throw new RuntimeException("Failed to initialize Executor", e);
    }
  }

  @Override
  public void start(final Job job, String contextId) {
    logger.debug("start(id={}, important={}, uploadOutputs={})", job.getId());

    final JobData jobData = new JobData(job, JobDataStatus.PENDING, false, false);
    jobDataService.save(jobData, contextId);
  }

  @Override
  public void stop(String jobId, String contextId) {
    logger.debug("stop(id={})", jobId);

    final JobData jobData = jobDataService.find(jobId, contextId);
    jobData.setStatus(JobDataStatus.ABORTING);
    jobDataService.save(jobData, contextId);
  }

  @Override
  public JobStatus findStatus(String jobId, String contextId) {
    logger.debug("findStatus(id={})", jobId);

    JobData jobData = jobDataService.find(jobId, contextId);
    if (jobData != null) {
      return JobDataStatus.convertToJobStatus(jobData.getStatus());
    }
    return null;
  }

  @Override
  public void shutdown(Boolean stopEverything) {
    logger.debug("shutdown(stopEverything={})", stopEverything);

    List<JobData> jobsToStop = jobDataService.find(JobDataStatus.PENDING, JobDataStatus.STARTED, JobDataStatus.READY);

    int abortedJobsCount = 0;
    if (jobsToStop != null) {
      for (JobData jobData : jobsToStop) {
        if (!stopEverything && jobData.isImportant()) {
          continue;
        }
        stop(jobData.getJob().getId(), jobData.getJob().getRootId());
        abortedJobsCount++;
      }
    }
    stopped.set(true);
    String message = String.format("Shutdown%s executed. Executor has stopped %d %s.", stopEverything ? " now" : "", abortedJobsCount, abortedJobsCount == 1 ? "job" : "jobs");
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

  private boolean isFinished(JobDataStatus jobStatus) {
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
