package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.List;

import org.rabix.engine.model.ContextRecord;

public class ContextRecordService {

  private List<ContextRecord> contextRecords = new ArrayList<>();
  
  public synchronized void create(ContextRecord contextRecord) {
    contextRecords.add(contextRecord);
  }
  
  public synchronized void update(ContextRecord context) {
    for (ContextRecord c : contextRecords) {
      if (c.getId().equals(context.getId())) {
        c.setStatus(context.getStatus());
        return;
      }
    }
  }
  
  public synchronized ContextRecord find(String id) {
    for (ContextRecord contextRecord : contextRecords) {
      if (contextRecord.getId().equals(id)) {
        return contextRecord;
      }
    }
    return null;
  }
  
}
