package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.engine.model.VariableRecord;

public class VariableService {

  private Map<String, List<VariableRecord>> variablesPerContext = new HashMap<String, List<VariableRecord>>();

  public synchronized void create(VariableRecord variable) {
    getVariables(variable.getContextId()).add(variable);
  }

  public synchronized void update(VariableRecord variable) {
    for (VariableRecord vr : getVariables(variable.getContextId())) {
      if (vr.getJobId().equals(variable.getJobId()) && vr.getPortId().equals(variable.getPortId()) && vr.getType().equals(variable.getType()) && vr.getContextId().equals(variable.getContextId())) {
        vr.setValue(variable.getValue());
        return;
      }
    }
  }
  
  public synchronized List<VariableRecord> find(String jobId, LinkPortType type, String contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariables(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getType().equals(type) && vr.getContextId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }
  
  public synchronized List<VariableRecord> find(String jobId, String portId, String contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariables(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getPortId().equals(portId) && vr.getContextId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }

  public synchronized VariableRecord find(String jobId, String portId, LinkPortType type, String contextId) {
    for (VariableRecord vr : getVariables(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getPortId().equals(portId) && vr.getType().equals(type) && vr.getContextId().equals(contextId)) {
        return vr;
      }
    }
    return null;
  }

  public synchronized List<VariableRecord> findByJobId(String jobId, LinkPortType type, String contextId) {
    List<VariableRecord> result = new ArrayList<>();
    for (VariableRecord vr : getVariables(contextId)) {
      if (vr.getJobId().equals(jobId) && vr.getType().equals(type) && vr.getContextId().equals(contextId)) {
        result.add(vr);
      }
    }
    return result;
  }

  public synchronized List<VariableRecord> find(String contextId) {
    return getVariables(contextId);
  }
  
  private synchronized List<VariableRecord> getVariables(String contextId) {
    List<VariableRecord> variableList = variablesPerContext.get(contextId);
    if (variableList == null) {
      variableList = new ArrayList<>();
      variablesPerContext.put(contextId, variableList);
    }
    return variableList;
  }
  
}
