package org.rabix.engine.rest.service.impl;

import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.engine.JobHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.processor.EventProcessor;
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
  
  private final DAGNodeDB dagNodeDB;
  
  private final EventProcessor eventProcessor;

  @Inject
  public JobServiceImpl(EventProcessor eventProcessor, JobRecordService jobRecordService, VariableRecordService variableRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB) {
    this.dagNodeDB = dagNodeDB;
    this.eventProcessor = eventProcessor;
    
    this.jobRecordService = jobRecordService;
    this.variableRecordService = variableRecordService;
    this.contextRecordService = contextRecordService;
  }
  
  public void update(Job job) throws JobServiceException {
    try {
      Bindings bindings = BindingsFactory.create(job);
      ProtocolType protocolType = bindings.getProtocolType();
      
      JobRecord jobRecord = jobRecordService.find(job.getNodeId(), job.getContext().getId());
      
      JobStatusEvent statusEvent = null;
      JobStatus status = job.getStatus();
      switch (status) {
      case RUNNING:
        JobStateValidator.checkState(jobRecord, JobState.RUNNING);
        statusEvent = new JobStatusEvent(job.getNodeId(), job.getContext().getId(), JobState.RUNNING, job.getOutputs(), protocolType);
        eventProcessor.addToQueue(statusEvent);
        break;
      case FAILED:
        JobStateValidator.checkState(jobRecord, JobState.FAILED);
        statusEvent = new JobStatusEvent(job.getNodeId(), job.getContext().getId(), JobState.FAILED, null, protocolType);
        eventProcessor.addToQueue(statusEvent);
        break;
      case COMPLETED:
        JobStateValidator.checkState(jobRecord, JobState.COMPLETED);
        statusEvent = new JobStatusEvent(job.getNodeId(), job.getContext().getId(), JobState.COMPLETED, job.getOutputs(), protocolType);
        eventProcessor.addToQueue(statusEvent);
        break;
      default:
        break;
      }
    } catch (BindingException e) {
      logger.error("Cannot find Bindings", e);
      throw new JobServiceException("Cannot find Bindings", e);
    } catch (JobStateValidationException e) {
      logger.error("Failed to update Job state");
      throw new JobServiceException("Failed to update Job state", e);
    }
  }
  
  public Set<Job> getReady(EventProcessor eventProcessor, String contextId) throws JobServiceException {
    return JobHelper.createReadyJobs(jobRecordService, variableRecordService, contextRecordService, dagNodeDB, contextId);
  }
  
}
