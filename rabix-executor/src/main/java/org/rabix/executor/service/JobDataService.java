package org.rabix.executor.service;

import java.util.List;

import org.rabix.executor.model.JobData;
import org.rabix.executor.model.JobData.JobDataStatus;

public interface JobDataService {

  void save(JobData data, String contextId);

  void save(JobData jobData, String message, JobDataStatus status, String contextId);
  
  public JobData find(String id, String contextId);

  public List<JobData> find(JobDataStatus... statuses);
  
}
