package org.rabix.executor.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.model.Job;
import org.rabix.executor.model.JobData;
import org.rabix.executor.model.JobData.JobDataStatus;
import org.rabix.executor.service.JobDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class JobDataServiceImpl implements JobDataService {

  private final static Logger logger = LoggerFactory.getLogger(JobDataServiceImpl.class);

  private final Map<String, Map<String, JobData>> jobs = new HashMap<>();

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

}
