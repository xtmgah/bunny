package org.rabix.executor.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.rabix.bindings.model.Job;
import org.rabix.executor.engine.EngineStub;
import org.rabix.executor.execution.JobHandlerCommandDispatcher;
import org.rabix.executor.execution.command.StartCommand;
import org.rabix.executor.execution.command.StatusCommand;
import org.rabix.executor.execution.command.StopCommand;
import org.rabix.executor.model.JobData;
import org.rabix.executor.model.JobData.JobDataStatus;
import org.rabix.executor.service.JobDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class JobDataServiceImpl implements JobDataService {

  private final static Logger logger = LoggerFactory.getLogger(JobDataServiceImpl.class);

  private final Map<String, Map<String, JobData>> jobs = new HashMap<>();

  private final JobHandlerCommandDispatcher jobHandlerCommandDispatcher;

  private final Provider<StartCommand> startCommandProvider;
  private final Provider<StopCommand> stopCommandProvider;
  private final Provider<StatusCommand> statusCommandProvider;

  private EngineStub engineStub;
  private ScheduledExecutorService singleThreadExecutor = Executors.newScheduledThreadPool(1);
  
  @Inject
  public JobDataServiceImpl(JobHandlerCommandDispatcher jobHandlerCommandDispatcher, Provider<StartCommand> startCommandProvider, Provider<StatusCommand> statusCommandProvider, Provider<StopCommand> stopCommandProvider) {
    this.startCommandProvider = startCommandProvider;
    this.stopCommandProvider = stopCommandProvider;
    this.statusCommandProvider = statusCommandProvider;
    this.jobHandlerCommandDispatcher = jobHandlerCommandDispatcher;
  }
  
  @Override
  public void initialize(EngineStub engineStub) {
    this.engineStub = engineStub;
    this.singleThreadExecutor.scheduleAtFixedRate(new JobStatusHandler(), 0, 5, TimeUnit.SECONDS);
  }
  
  @Override
  public synchronized JobData find(String id, String contextId) {
    Preconditions.checkNotNull(id);
    logger.debug("find(id={})", id);
    return getJobs(contextId).get(id);
  }

  @Override
  public synchronized List<JobData> find(JobDataStatus... statuses) {
    Preconditions.checkNotNull(statuses);

    List<JobDataStatus> statusList = Arrays.asList(statuses);
    logger.debug("find(status={})", statusList);

    List<JobData> jobDataByStatus = new ArrayList<>();
    for (Entry<String, Map<String, JobData>> entry : jobs.entrySet()) {
      for (JobData jobData : entry.getValue().values()) {
        if (statusList.contains(jobData.getStatus())) {
          jobDataByStatus.add(jobData);
        }
      }
    }
    return jobDataByStatus;
  }

  @Override
  public synchronized void save(JobData jobData, String contextId) {
    logger.debug("save(jobData={})", jobData);
    Job job = jobData.getJob();
    getJobs(contextId).put(job.getId(), jobData);
  }

  @Override
  public synchronized void save(JobData jobData, String message, JobDataStatus status, String contextId) {
    Preconditions.checkNotNull(jobData);
    logger.debug("save(jobData={}, message={}, status={})", jobData, message, status);
    jobData.setStatus(status);
    jobData.setMessage(message);
    save(jobData, contextId);
  }
  
  private synchronized Map<String, JobData> getJobs(String contextId) {
    Map<String, JobData> jobList = jobs.get(contextId);
    if (jobList == null) {
      jobList = new HashMap<>();
      jobs.put(contextId, jobList);
    }
    return jobList;
  }
  
  private class JobStatusHandler implements Runnable {
    
    private int maxRunningJobs = 10;
    
    @Override
    public void run() {
      List<JobData> aborting = find(JobDataStatus.ABORTING);
      for (JobData jobData : aborting) {
        jobData.setStatus(JobDataStatus.ABORTED);
        save(jobData, jobData.getJob().getRootId());
        jobHandlerCommandDispatcher.dispatch(jobData, stopCommandProvider.get(), engineStub);
        jobHandlerCommandDispatcher.dispatch(jobData, startCommandProvider.get(), engineStub);
      }
      
      List<JobData> running = find(JobDataStatus.RUNNING, JobDataStatus.STARTED);
      
      List<JobData> pending = find(JobDataStatus.PENDING);

      if (maxRunningJobs - running.size() == 0) {
        logger.info("Running {} jobs. Waiting for some to finish...", running.size());
      } else {
        for (int i = 0; i < Math.min(pending.size(), maxRunningJobs - running.size()); i++) {
          JobData jobData = pending.get(i);
          jobData.setStatus(JobDataStatus.READY);
          save(jobData, jobData.getJob().getRootId());

          jobHandlerCommandDispatcher.dispatch(jobData, startCommandProvider.get(), engineStub);
          jobHandlerCommandDispatcher.dispatch(jobData, statusCommandProvider.get(), engineStub);
        }
      }
    }
  }

}
