package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.model.VariableRecord;

public class VariableRecordService {

  private Map<String, List<VariableRecord>> variableRecordsPerContext = new HashMap<String, List<VariableRecord>>();

  public synchronized void create(VariableRecord variableRecord) {
    getVariableRecords(variableRecord.getContextId()).add(variableRecord);
  }

  public synchronized void update(VariableRecord variableRecord) {
    for (VariableRecord vr : getVariableRecords(variableRecord.getContextId())) {
      if (vr.getJobId().equals(variableRecord.getJobId()) && vr.getPortId().equals(variableRecord.getPortId()) && vr.getType().equals(variableRecord.getType()) && vr.getContextId().equals(variableRecord.getContextId())) {
        vr.setValue(variableRecord.getValue());
        return;
      }
    }
  }
  
  public synchronized List<VariableRecord> find(String jobId, LinkPortType type, String contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getType().equals(type) && vr.getContextId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }
  
  public synchronized List<VariableRecord> find(String jobId, String portId, String contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getPortId().equals(portId) && vr.getContextId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }

  public synchronized VariableRecord find(String jobId, String portId, LinkPortType type, String contextId) {
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getPortId().equals(portId) && vr.getType().equals(type) && vr.getContextId().equals(contextId)) {
        return vr;
      }
    }
    return null;
  }

  public synchronized List<VariableRecord> findByJobId(String jobId, LinkPortType type, String contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariableRecords(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getType().equals(type) && vr.getContextId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }

  public synchronized List<VariableRecord> find(String contextId) {
    return getVariableRecords(contextId);
  }
  
  @SuppressWarnings("unchecked")
  public void addValue(VariableRecord variableRecord, Object value, Integer position) {
    variableRecord.setTimesUpdatedCount(variableRecord.getTimesUpdatedCount() + 1);
    if (variableRecord.isDefault()) {
      variableRecord.setValue(null);
      variableRecord.setDefault(false);
    }
    if (variableRecord.getValue() == null) {
      if (position == 1) {
        variableRecord.setValue(value);
      } else {
        List<Object> valueList = new ArrayList<>();
        expand(valueList, position);
        valueList.set(position - 1, value);
        variableRecord.setValue(valueList);
        variableRecord.setWrapped(true);
      }
    } else {
      if (variableRecord.isWrapped()) {
        expand((List<Object>) variableRecord.getValue(), position);
        ((List<Object>) variableRecord.getValue()).set(position - 1, value);
      } else {
        List<Object> valueList = new ArrayList<>();
        valueList.add(variableRecord.getValue());
        expand(valueList, position);
        valueList.set(position - 1, value);
        variableRecord.setValue(valueList);
        variableRecord.setWrapped(true);
      }
    }
  }

  public Object transformValue(VariableRecord variableRecord) {
    if (variableRecord.getLinkMerge() == null) {
      return variableRecord.getValue();
    }
    switch (variableRecord.getLinkMerge()) {
    case merge_nested:
      return variableRecord.getValue();
    case merge_flattened:
      return mergeFlatten(variableRecord.getValue());
    default:
      return variableRecord.getValue();
    }
  }

  private <T> void expand(List<T> list, Integer position) {
    int initialSize = list.size();
    if (initialSize >= position) {
      return;
    }
    for (int i = 0; i < position - initialSize; i++) {
      list.add(null);
    }
    return;
  }

  @SuppressWarnings("unchecked")
  private Object mergeFlatten(Object value) {
    if (value == null) {
      return null;
    }
    if (!(value instanceof List<?>)) {
      return value;
    }
    List<Object> flattenedValues = new ArrayList<>();
    if (value instanceof List<?>) {
      for (Object subvalue : ((List<?>) value)) {
        Object flattenedSubvalue = mergeFlatten(subvalue);
        if (flattenedSubvalue instanceof List<?>) {
          flattenedValues.addAll((Collection<? extends Object>) flattenedSubvalue);
        } else {
          flattenedValues.add(flattenedSubvalue);
        }
      }
    } else {
      flattenedValues.add(value);
    }
    return flattenedValues;
  }
  
  public synchronized List<VariableRecord> getVariableRecords(String contextId) {
    List<VariableRecord> variableList = variableRecordsPerContext.get(contextId);
    if (variableList == null) {
      variableList = new ArrayList<>();
      variableRecordsPerContext.put(contextId, variableList);
    }
    return variableList;
  }
  
}
