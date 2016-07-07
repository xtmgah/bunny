package org.rabix.engine.rest.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.Configuration;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.Resources;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.SystemEnvironmentHelper;
import org.rabix.engine.JobHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.EventProcessor.JobStatusCallback;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.rest.backend.BackendDispatcher;
import org.rabix.engine.rest.db.JobDB;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.validator.JobStateValidationException;
import org.rabix.engine.validator.JobStateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JobServiceImpl implements JobService {

  private final static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);
  
  private final JobRecordService jobRecordService;
  private final VariableRecordService variableRecordService;
  private final ContextRecordService contextRecordService;
  
  private final JobDB jobDB;
  private final DAGNodeDB dagNodeDB;
  
  private final EventProcessor eventProcessor;
  private final BackendDispatcher backendDispatcher;
  
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Inject
  public JobServiceImpl(EventProcessor eventProcessor, JobRecordService jobRecordService, VariableRecordService variableRecordService, ContextRecordService contextRecordService, BackendDispatcher backendDispatcher, Configuration configuration, DAGNodeDB dagNodeDB, JobDB jobDB) {
    this.jobDB = jobDB;
    this.dagNodeDB = dagNodeDB;
    this.eventProcessor = eventProcessor;
    
    this.jobRecordService = jobRecordService;
    this.variableRecordService = variableRecordService;
    this.contextRecordService = contextRecordService;
    this.backendDispatcher = backendDispatcher;

    boolean isLocalBackend = configuration.getBoolean("local.backend", false);
    this.eventProcessor.start(null, new JobStatusCallbackImpl(isLocalBackend, isLocalBackend));
  }
  
  @Override
  public void update(Job job) throws JobServiceException {
    logger.debug("Update Job {}", job.getId());
    
    JobRecord jobRecord = jobRecordService.find(job.getName(), job.getRootId());
    try {
      JobStatusEvent statusEvent = null;
      JobStatus status = job.getStatus();
      switch (status) {
      case RUNNING:
        if (JobState.RUNNING.equals(jobRecord.getState())) {
          return;
        }
        JobStateValidator.checkState(jobRecord, JobState.RUNNING);
        statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobState.RUNNING, job.getOutputs());
        eventProcessor.addToQueue(statusEvent);
        break;
      case FAILED:
        if (JobState.FAILED.equals(jobRecord.getState())) {
          return;
        }
        JobStateValidator.checkState(jobRecord, JobState.FAILED);
        statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobState.FAILED, null);
        eventProcessor.addToQueue(statusEvent);
        break;
      case COMPLETED:
        if (JobState.COMPLETED.equals(jobRecord.getState())) {
          return;
        }
        JobStateValidator.checkState(jobRecord, JobState.COMPLETED);
        statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobState.COMPLETED, job.getOutputs());
        eventProcessor.addToQueue(statusEvent);
        break;
      default:
        break;
      }
      jobDB.update(job);
    } catch (JobStateValidationException e) {
      // TODO handle exception
      logger.warn("Failed to update Job state from {} to {}", jobRecord.getState(), job.getStatus());
    }
  }
  
  @Override
  public Job start(Job job) throws JobServiceException {
    logger.debug("Start Job {}", job);
    
    Context context = job.getContext() != null? job.getContext() : createContext(UUID.randomUUID().toString());
    job = Job.cloneWithIds(job, context.getId(), context.getId());
    job = Job.cloneWithContext(job, context);

    Bindings bindings = null;
    try {
      bindings = BindingsFactory.create(job);

      DAGNode node = bindings.translateToDAG(job);
      
      job = Job.cloneWithStatus(job, JobStatus.RUNNING);
      jobDB.add(job);

      InitEvent initEvent = new InitEvent(context, context.getId(), node, job.getInputs());
      eventProcessor.send(initEvent);
      return job;
    } catch (EventHandlerException e) {
      throw new JobServiceException("Failed to start job", e);
    } catch (Exception e) {
      logger.error("Failed to create Bindings", e);
      throw new JobServiceException("Failed to create Bindings", e);
    }
  }
  
  @Override
  public void stop(String id) throws JobServiceException {
    logger.debug("Stop Job {}", id);
    
    Job job = jobDB.get(id);
    if (job.isRoot()) {
      Set<Job> jobs = jobDB.getJobs(id);
      backendDispatcher.stop(jobs.toArray(new Job[jobs.size()]));
    } else {
      backendDispatcher.stop(job);
    }
  }
  
  @Override
  public Set<Job> getReady(EventProcessor eventProcessor, String contextId) throws JobServiceException {
    return JobHelper.createReadyJobs(jobRecordService, variableRecordService, contextRecordService, dagNodeDB, contextId);
  }
  
  @Override
  public Set<Job> get() {
    return jobDB.getJobs();
  }

  @Override
  public Job get(String id) {
    return jobDB.get(id);
  }

  private Context createContext(String contextId) {
    return new Context(contextId, null);
  }
  
  private class JobStatusCallbackImpl implements JobStatusCallback {

    private boolean stopOnFail;
    private boolean setResources;
    
    private AtomicInteger failCount = new AtomicInteger(0);
    private AtomicInteger successCount = new AtomicInteger(0);

    private Set<String> stoppingRootIds = new HashSet<>();
    
    public JobStatusCallbackImpl(boolean setResources, boolean stopOnFail) {
      this.stopOnFail = stopOnFail;
      this.setResources = setResources;
    }
    
    @Override
    public void onReady(Job job) {
      if (setResources) {
        long numberOfCores = SystemEnvironmentHelper.getNumberOfCores();
        long memory = SystemEnvironmentHelper.getTotalPhysicalMemorySizeInMB();
        
        Resources resources = new Resources(numberOfCores, memory, null, true, null);
        job = Job.cloneWithResources(job, resources);
      }
      jobDB.update(job);
      backendDispatcher.send(job);
    }

    @Override
    public void onFailed(final Job failedJob) throws Exception {
      if (stopOnFail) {
        synchronized (stoppingRootIds) {
          if (stoppingRootIds.contains(failedJob.getRootId())) {
            return;
          }
          stoppingRootIds.add(failedJob.getRootId());
          
          stop(failedJob.getRootId());
          executorService.submit(new Runnable() {
            @Override
            public void run() {
              while (true) {
                try {
                  boolean exit = true;
                  for (Job job : jobDB.getJobs(failedJob.getRootId())) {
                    if (!job.isRoot() && !isFinished(job.getStatus())) {
                      exit = false;
                      break;
                    }
                  }
                  if (exit) {
                    onRootFailed(failedJob.getRootId());
                    break;
                  }
                  Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                } catch (Exception e) {
                  logger.error("Failed to stop root Job " + failedJob.getRootId(), e);
                  break;
                }
              }
            }
          });
        }
      }
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
    public void onRootCompleted(String rootId) throws Exception {
      Job job = jobDB.get(rootId);
      job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
      job = JobHelper.fillOutputs(job, jobRecordService, variableRecordService);
      jobDB.update(job);
      logger.info("Root Job {} completed. Successfull {}.", job.getId(), successCount.incrementAndGet());
    }

    @Override
    public void onRootFailed(String rootId) throws Exception {
      synchronized (stoppingRootIds) {
        Job job = jobDB.get(rootId);
        job = Job.cloneWithStatus(job, JobStatus.FAILED);
        jobDB.update(job);

        backendDispatcher.remove(job);
        stoppingRootIds.remove(rootId);
        logger.info("Root Job {} failed. Failed {}.", job.getId(), failCount.incrementAndGet());
      }
    }

  }
  
}
