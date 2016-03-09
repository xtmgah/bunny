package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.List;

import org.rabix.engine.model.ContextRecord;

public class ContextService {

  private List<ContextRecord> contexts = new ArrayList<>();
  
  public synchronized void create(ContextRecord context) {
    contexts.add(context);
  }
  
  public synchronized void update(ContextRecord context) {
    for (ContextRecord c : contexts) {
      if (c.getId().equals(context.getId())) {
        c.setStatus(context.getStatus());
        return;
      }
    }
  }
  
  public synchronized ContextRecord find(String id) {
    for (ContextRecord context : contexts) {
      if (context.getId().equals(id)) {
        return context;
      }
    }
    return null;
  }
  
}
