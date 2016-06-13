package org.rabix.engine.rest.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.db.DBException;
import org.rabix.engine.JobHelper;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.EventProcessor.IterationCallback;
import org.rabix.engine.processor.JobCallback;
import org.rabix.engine.rest.backend.BackendDispatcher;
import org.rabix.engine.rest.db.JobRepository;
import org.rabix.engine.rest.service.EngineRestServiceException;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.engine.service.ApplicationPayloadService;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.DAGNodeGraphService;
import org.rabix.engine.service.EngineServiceException;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.validator.JobStateValidationException;
import org.rabix.engine.validator.JobStateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class JobServiceImpl implements JobService {

  private final static Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);
  
  private final JobRecordService jobRecordService;
  private final VariableRecordService variableRecordService;
  private final ContextRecordService contextRecordService;
  
  private final JobRepository jobRepository;
  private final DAGNodeGraphService dagNodeService;
  private final ApplicationPayloadService applicationService;
  
  private final EventProcessor eventProcessor;
  private final BackendDispatcher backendDispatcher;

  @Inject
  public JobServiceImpl(EventProcessor eventProcessor, JobRecordService jobRecordService, VariableRecordService variableRecordService, ContextRecordService contextRecordService, BackendDispatcher backendDispatcher, DAGNodeGraphService dagNodeService, ApplicationPayloadService applicationService, JobRepository jobRepository) {
    this.jobRepository = jobRepository;
    this.dagNodeService = dagNodeService;
    this.applicationService = applicationService;
    this.eventProcessor = eventProcessor;
    
    this.jobRecordService = jobRecordService;
    this.variableRecordService = variableRecordService;
    this.contextRecordService = contextRecordService;
    this.backendDispatcher = backendDispatcher;

    this.eventProcessor.start(new ArrayList<IterationCallback>(), new JobReadyCallback());
  }
  
  @Override
  @Transactional
  public void update(Job job) throws JobServiceException {
    try {
      JobRecord jobRecord = jobRecordService.find(job.getName(), job.getRootId());
      
      if (jobRecord == null) {
        // TODO handle
        logger.error("Failed to find Job " + job.getId());
        return;
      }
      
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
        backendDispatcher.remove(job);
        break;
      case COMPLETED:
        if (JobState.COMPLETED.equals(jobRecord.getState())) {
          return;
        }
        JobStateValidator.checkState(jobRecord, JobState.COMPLETED);
        statusEvent = new JobStatusEvent(job.getName(), job.getRootId(), JobState.COMPLETED, job.getOutputs());
        eventProcessor.addToQueue(statusEvent);
        backendDispatcher.remove(job);
        break;
      default:
        break;
      }
      jobRepository.update(job);
    } catch (JobStateValidationException e) {
      logger.error("Failed to update Job state", e);
    } catch (Exception e) {
      logger.error("Failed to handle update for Job " + job, e);
      throw new JobServiceException("Failed to handle update for Job " + job, e);
    }
  }
  
  @Override
  @Transactional
  public Set<Job> getReady(EventProcessor eventProcessor, String contextId) throws JobServiceException {
    try {
      return JobHelper.createReadyJobs(jobRecordService, variableRecordService, contextRecordService, dagNodeService, applicationService, contextId);
    } catch (EngineServiceException e) {
      logger.error("Failed to get ready Jobs", e);
      throw new JobServiceException("Failed to get ready Jobs", e);
    }
  }
  
  @Override
  @Transactional
  public Job create(Job job) throws JobServiceException {
    Context context = job.getContext() != null? job.getContext() : createContext(UUID.randomUUID().toString());
    job = Job.cloneWithIds(job, context.getId(), context.getId());
    job = Job.cloneWithContext(job, context);

    Bindings bindings = null;
    try {
      bindings = BindingsFactory.create(job);

      DAGNode node = bindings.translateToDAG(job);
      
      job = Job.cloneWithStatus(job, JobStatus.RUNNING);
      jobRepository.insert(job);

      InitEvent initEvent = new InitEvent(context, context.getId(), node, job.getInputs());
      eventProcessor.addToQueue(initEvent);
      return job;
    } catch (Exception e) {
      logger.error("Failed to create Bindings", e);
      throw new JobServiceException("Failed to create Bindings", e);
    }
  }
  
  @Override
  public List<Job> get() throws EngineRestServiceException {
    try {
      return jobRepository.find();
    } catch (DBException e) {
      logger.error("Failed to get all Jobs", e);
      throw new EngineRestServiceException("Failed to get all Jobs", e);
    }
  }

  @Override
  public Job get(String id) throws EngineRestServiceException {
    try {
      return jobRepository.find(id);
    } catch (DBException e) {
      logger.error("Failed to get Job for id=" + id, e);
      throw new EngineRestServiceException("Failed to get Job for id=" + id, e);
    }
  }

  private Context createContext(String contextId) {
    return new Context(contextId, null);
  }
  
  private class JobReadyCallback implements JobCallback {
    private AtomicInteger successCount = new AtomicInteger(0);
    
    @Override
    public void onReady(Job job) throws Exception {
      jobRepository.update(job);
      
      Set<Job> jobs = new HashSet<>();
      jobs.add(job);
      backendDispatcher.send(jobs);
    }

    @Override
    public void onRootCompleted(String rootId) throws Exception {
      Job job = jobRepository.find(rootId);
      job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
      job = JobHelper.fillOutputs(job, jobRecordService, variableRecordService);
      jobRepository.update(job);
      System.out.println("Number of successfull Jobs until now is " + successCount.incrementAndGet());
    }

    @Override
    public void onRootFailed(String rootId) throws Exception {
      Job job = jobRepository.find(rootId);
      job = Job.cloneWithStatus(job, JobStatus.FAILED);
      jobRepository.update(job);
    }
  }

}
