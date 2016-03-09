package org.rabix.engine.rest.db;

import java.util.HashMap;
import java.util.Map;

public class TaskDB {

  private Map<String, Boolean> taskStates = new HashMap<>();
  
  public void set(String contextId, Boolean state) {
    taskStates.put(contextId, state);
  }
  
  public Boolean get(String contextId) {
    return taskStates.get(contextId);
  }
  
  public Map<String, Boolean> getTaskStates() {
    return taskStates;
  }
}
