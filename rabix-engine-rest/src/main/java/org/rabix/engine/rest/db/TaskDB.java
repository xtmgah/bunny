package org.rabix.engine.rest.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.rabix.engine.rest.dto.Task;

public class TaskDB {

  private Map<String, Task> tasks = new HashMap<>();
  
  public void add(Task task) {
    tasks.put(task.getId(), task);
  }
  
  public void update(Task task) {
    tasks.put(task.getId(), task);
  }
  
  public Task get(String id) {
    return tasks.get(id);
  }
  
  public Set<Task> getTasks() {
    Set<Task> taskSet = new HashSet<>();
    for (Task task : tasks.values()) {
      taskSet.add(task);
    }
    return taskSet;
  }
}
