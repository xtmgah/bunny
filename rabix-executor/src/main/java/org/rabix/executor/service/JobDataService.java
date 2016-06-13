package org.rabix.executor.service;

import java.util.List;

import org.rabix.executor.engine.EngineStub;
import org.rabix.executor.model.JobData;
import org.rabix.executor.model.JobData.JobDataStatus;

public interface JobDataService {

  void initialize(EngineStub engineStub);
  
  void save(JobData data, String contextId);

  void save(JobData jobData, String message, JobDataStatus status, String contextId);
  
  JobData find(String id, String contextId);

  List<JobData> find(JobDataStatus... statuses);

}
