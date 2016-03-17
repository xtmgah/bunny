package org.rabix.engine.service;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.service.JobService.JobState;

public class JobStateService {

  private Map<JobState, List<JobState>> transitions = new HashMap<JobState, List<JobState>>();

  public JobStateService() {
    transitions = new HashMap<JobState, List<JobState>>();
    List<JobState> transitionFromPending = new ArrayList<JobState>();
    transitionFromPending.add(JobState.READY);
    transitions.put(JobState.PENDING, transitionFromPending);
    List<JobState> transitionFromReady = new ArrayList<JobState>();
    transitionFromReady.add(JobState.RUNNING);
    transitionFromReady.add(JobState.FAILED);
    transitions.put(JobState.READY, transitionFromReady);
    List<JobState> transitionFromRunning = new ArrayList<JobState>();
    transitionFromRunning.add(JobState.COMPLETED);
    transitionFromRunning.add(JobState.FAILED);
    transitions.put(JobState.RUNNING, transitionFromRunning);
    List<JobState> transitionFromCompleted = new ArrayList<JobState>();
    transitionFromCompleted.add(JobState.READY);
    transitions.put(JobState.COMPLETED, transitionFromCompleted);
    List<JobState> transitionFromFailed = new ArrayList<JobState>();
    transitions.put(JobState.FAILED, transitionFromFailed);
  }

  public JobState checkState(JobRecord jobRecord, JobState jobState) {
    JobState currentState = jobRecord.getState();
    if (transitions.get(currentState).contains(jobState)) {
      return jobState;
    } else {
      throw new IllegalStateException();
    }
  }

}
