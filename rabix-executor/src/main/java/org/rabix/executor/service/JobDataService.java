package org.rabix.executor.service;

import java.util.List;

import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.executor.model.JobData;

public interface JobDataService {

  void save(JobData data, String contextId);

  void save(JobData jobData, String message, JobStatus status, String contextId);
  
  public JobData find(String id, String contextId);

  public List<JobData> find(JobStatus... statuses);
  
}
