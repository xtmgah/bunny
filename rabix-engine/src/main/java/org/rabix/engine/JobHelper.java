package org.rabix.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.CloneHelper;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobHelper {

  private static Logger logger = LoggerFactory.getLogger(JobHelper.class);
  
  public static String generateId() {
    return UUID.randomUUID().toString();
  }
  
  public static Set<Job> createReadyJobs(JobRecordService jobRecordService, VariableRecordService variableRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB, String contextId) {
    Set<Job> jobs = new HashSet<>();
    List<JobRecord> jobRecords = jobRecordService.findReady(contextId);

    if (!jobRecords.isEmpty()) {
      for (JobRecord job : jobRecords) {
        jobs.add(createJob(job, JobStatus.READY, jobRecordService, variableRecordService, contextRecordService, dagNodeDB, contextId));
      }
    }
    return jobs;
  }
  
  public static Job createJob(JobRecord job, JobStatus status, JobRecordService jobRecordService, VariableRecordService variableRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB, String contextId) {
    DAGNode node = dagNodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), job.getRootId());

    Map<String, Object> preprocesedInputs = new HashMap<>();
    Map<String, Object> inputs = new HashMap<>();
    List<VariableRecord> inputVariables = variableRecordService.find(job.getId(), LinkPortType.INPUT, contextId);
    
    for (VariableRecord inputVariable : inputVariables) {
    	preprocesedInputs.put(inputVariable.getPortId(), inputVariable.getValue());
    }
    
    ContextRecord contextRecord = contextRecordService.find(job.getRootId());
    Context context = new Context(job.getRootId(), contextRecord.getConfig());
    String encodedApp = URIHelper.createDataURI(node.getApp().serialize());
    Job newJob = new Job(job.getExternalId(), job.getParentId(), job.getRootId(), job.getId(), encodedApp, JobStatus.READY, preprocesedInputs, null, context, null);
    
    try {
          Bindings bindings = BindingsFactory.create(encodedApp);
          
          
          for (VariableRecord inputVariable : inputVariables) {
            Object value = inputVariable.getValue();
            for (DAGLinkPort port: node.getInputPorts()) {
              if(port.getId() == inputVariable.getPortId()) {
                if (port.getTransform() != null) {
                  Object transform = port.getTransform();
                  
                  if(transform != null) {
                    value = bindings.transformInputs(value, newJob, transform);
                  }
                }
              }
            }
          inputs.put(inputVariable.getPortId(), value);
    	} 
    } catch (BindingException e) {
    	e.printStackTrace();
    }
    
    boolean autoBoxingEnabled = true;   // get from configuration
    
    StringBuilder inputsLogBuilder = new StringBuilder("\n ---- JobRecord ").append(job.getId()).append("\n");
    
    for (VariableRecord inputVariable : inputVariables) {
      Object value = CloneHelper.deepCopy(inputVariable.getValue());
      ApplicationPort port = node.getApp().getInput(inputVariable.getPortId());
      if (port != null && autoBoxingEnabled) {
        if (port.isList() && !(value instanceof List)) {
          List<Object> transformed = new ArrayList<>();
          transformed.add(value);
          value = transformed;
        }

      }
      inputsLogBuilder.append(" ---- Input ").append(inputVariable.getPortId()).append(", value ").append(value).append("\n");
      inputs.put(inputVariable.getPortId(), value);
    }
    logger.debug(inputsLogBuilder.toString());
    
//    ContextRecord contextRecord = contextRecordService.find(job.getRootId());
//    Context context = new Context(job.getRootId(), contextRecord.getConfig());
//    String encodedApp = URIHelper.createDataURI(node.getApp().serialize());
    return new Job(job.getExternalId(), job.getParentId(), job.getRootId(), job.getId(), encodedApp, status, inputs, null, context, null);
  }
  
  public static Job fillOutputs(Job job, JobRecordService jobRecordService, VariableRecordService variableRecordService) {
    JobRecord jobRecord = jobRecordService.findRoot(job.getContext().getId());
    List<VariableRecord> outputVariables = variableRecordService.find(jobRecord.getId(), LinkPortType.OUTPUT, job.getContext().getId());
    
    Map<String, Object> outputs = new HashMap<>();
    for (VariableRecord outputVariable : outputVariables) {
      outputs.put(outputVariable.getPortId(), outputVariable.getValue());
    }
    return Job.cloneWithOutputs(job, outputs);
  }
  
}
