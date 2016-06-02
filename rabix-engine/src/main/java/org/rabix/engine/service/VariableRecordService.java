package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.db.DBException;
import org.rabix.engine.db.VariableRecordRepository;
import org.rabix.engine.model.VariableRecord;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class VariableRecordService {

  private final VariableRecordRepository variableRecordRepository;
  
  @Inject
  public VariableRecordService(final VariableRecordRepository variableRecordRepository) {
    this.variableRecordRepository = variableRecordRepository;
  }
  
  @Transactional
  public void create(VariableRecord variableRecord) {
    try {
      variableRecordRepository.insert(variableRecord);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Transactional
  public void update(VariableRecord variableRecord) {
    try {
      variableRecordRepository.update(variableRecord);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Transactional
  public List<VariableRecord> find(String jobId, LinkPortType type, String contextId) {
    try {
      return variableRecordRepository.findByType(jobId, type, contextId);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }
  
  @Transactional
  public List<VariableRecord> find(String jobId, String portId, String contextId) {
    try {
      return variableRecordRepository.findByPort(jobId, portId, contextId);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  @Transactional
  public VariableRecord find(String jobId, String portId, LinkPortType type, String contextId) {
    try {
      return variableRecordRepository.find(jobId, portId, type, contextId);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
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
  
}
