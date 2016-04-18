package org.rabix.engine.rest.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.rabix.engine.rest.backend.Backend;

public class BackendDB {

  private Map<String, Backend> backends = new HashMap<>();
  
  public void add(Backend backend) {
    backends.put(backend.getId(), backend);
  }
  
  public void update(Backend backend) {
    backends.put(backend.getId(), backend);
  }
  
  public Backend get(String id) {
    return backends.get(id);
  }
  
  public Set<Backend> getJobs() {
    Set<Backend> backendSet = new HashSet<>();
    for (Backend backend : backends.values()) {
      backendSet.add(backend);
    }
    return backendSet;
  }
  
}
