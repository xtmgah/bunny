package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.engine.model.JobRecord;

public class JobService {

  public static enum JobState {
    PENDING,
    READY,
    RUNNING,
    COMPLETED,
    FAILED
  }

  private Map<String, List<JobRecord>> jobsPerContext = new HashMap<String, List<JobRecord>>();

  public synchronized void create(JobRecord job) {
    getJobs(job.getContextId()).add(job);
  }

  public synchronized void update(JobRecord job) {
    for (JobRecord jr : getJobs(job.getContextId())) {
      if (jr.getId().equals(job.getId())) {
        jr.setState(job.getState());
        jr.setContainer(job.isContainer());
        jr.setScattered(job.isScattered());
        jr.setInputCounters(job.getInputCounters());
        jr.setOutputCounters(job.getOutputCounters());
        jr.setScatterWrapper(job.isScatterWrapper());
        jr.setScatterMapping(job.getScatterMapping());
        return;
      }
    }
  }
  
  public synchronized List<JobRecord> find(String contextId) {
    return getJobs(contextId);
  }
  
  public synchronized List<JobRecord> findReady(String contextId) {
    List<JobRecord> result = new ArrayList<>();
    
    for (JobRecord jr : getJobs(contextId)) {
      if (jr.getState().equals(JobState.READY) && jr.getContextId().equals(contextId)) {
        result.add(jr);
      }
    }
    return result;
  }
  
  public synchronized JobRecord find(String id, String contextId) {
    for (JobRecord jr : getJobs(contextId)) {
      if (jr.getId().equals(id) && jr.getContextId().equals(contextId)) {
        return jr;
      }
    }
    return null;
  }
  
  public synchronized JobRecord findRoot(String contextId) {
    for (JobRecord jr : getJobs(contextId)) {
      if (jr.isMaster() && jr.getContextId().equals(contextId)) {
        return jr;
      }
    }
    return null;
  }
  
  private synchronized List<JobRecord> getJobs(String contextId) {
    List<JobRecord> jobList = jobsPerContext.get(contextId);
    if (jobList == null) {
      jobList = new ArrayList<>();
      jobsPerContext.put(contextId, jobList);
    }
    return jobList;
  }
  
}
