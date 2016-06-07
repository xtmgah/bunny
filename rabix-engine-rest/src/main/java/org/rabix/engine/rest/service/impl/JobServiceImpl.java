package org.rabix.engine.rest.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.JobHelper;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.EventProcessor.IterationCallback;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.rest.backend.BackendDispatcher;
import org.rabix.engine.rest.db.JobDB;
import org.rabix.engine.rest.service.JobService;
import org.rabix.engine.rest.service.JobServiceException;
import org.rabix.engine.service.ApplicationPayloadService;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.DAGNodeGraphService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.EngineServiceException;
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
  private final DAGNodeGraphService dagNodeService;
  private final ApplicationPayloadService applicationService;
  
  private final EventProcessor eventProcessor;
  private final BackendDispatcher backendDispatcher;

  @Inject
  public JobServiceImpl(EventProcessor eventProcessor, JobRecordService jobRecordService, VariableRecordService variableRecordService, ContextRecordService contextRecordService, BackendDispatcher backendDispatcher, DAGNodeGraphService dagNodeService, ApplicationPayloadService applicationService, JobDB jobDB) {
    this.jobDB = jobDB;
    this.dagNodeService = dagNodeService;
    this.applicationService = applicationService;
    this.eventProcessor = eventProcessor;
    
    this.jobRecordService = jobRecordService;
    this.variableRecordService = variableRecordService;
    this.contextRecordService = contextRecordService;
    this.backendDispatcher = backendDispatcher;

    List<IterationCallback> callbacks = new ArrayList<>();
    callbacks.add(new EndJobCallback());
    callbacks.add(new SendJobsCallback());
    this.eventProcessor.start(callbacks);
  }
  
  @Override
  public void update(Job job) throws JobServiceException {
    try {
      JobRecord jobRecord = jobRecordService.find(job.getName(), job.getRootId());
      
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
      jobDB.update(job);
    } catch (JobStateValidationException e) {
      logger.error("Failed to update Job state", e);
      throw new JobServiceException("Failed to update Job state", e);
    } catch (EngineServiceException e) {
      logger.error("Failed to handle update for Job " + job, e);
      throw new JobServiceException("Failed to handle update for Job " + job, e);
    }
  }
  
  @Override
  public Set<Job> getReady(EventProcessor eventProcessor, String contextId) throws JobServiceException {
    try {
      return JobHelper.createReadyJobs(jobRecordService, variableRecordService, contextRecordService, dagNodeService, applicationService, contextId);
    } catch (EngineServiceException e) {
      logger.error("Failed to get ready Jobs", e);
      throw new JobServiceException("Failed to get ready Jobs", e);
    }
  }
  
  @Override
  public Job create(Job job) throws JobServiceException {
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
    } catch (BindingException e) {
      logger.error("Failed to create Bindings", e);
      throw new JobServiceException("Failed to create Bindings", e);
    } catch (EventHandlerException e) {
      throw new JobServiceException("Failed to start job", e);
    }
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
  
  private class SendJobsCallback implements IterationCallback {
    @Override
    public void call(EventProcessor eventProcessor, String contextId, int iteration) throws Exception {
      Set<Job> jobs = getReady(eventProcessor, contextId);
      for (Job job : jobs) {
        jobDB.update(job);
      }
      backendDispatcher.send(jobs);
    }
  }

  private class EndJobCallback implements IterationCallback {
    @Override
    public void call(EventProcessor eventProcessor, String contextId, int iteration) throws Exception {
      ContextRecord context = contextRecordService.find(contextId);
      
      Job job = null;
      switch (context.getStatus()) {
      case COMPLETED:
        job = jobDB.get(contextId);
        job = Job.cloneWithStatus(job, JobStatus.COMPLETED);
        job = JobHelper.fillOutputs(job, jobRecordService, variableRecordService);
        jobDB.update(job);
        break;
      case FAILED:
        job = jobDB.get(contextId);
        job = Job.cloneWithStatus(job, JobStatus.FAILED);
        jobDB.update(job);
        break;
      default:
        break;
      }
    }
  }

}
