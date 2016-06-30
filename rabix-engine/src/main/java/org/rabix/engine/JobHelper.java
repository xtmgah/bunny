package org.rabix.engine;

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
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.VariableRecordService;

import ch.qos.logback.core.helpers.Transform;

public class JobHelper {

  public static String generateId() {
    return UUID.randomUUID().toString();
  }
  
  public static Set<Job> createReadyJobs(JobRecordService jobRecordService, VariableRecordService variableRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB, String contextId) {
    Set<Job> jobs = new HashSet<>();
    List<JobRecord> jobRecords = jobRecordService.findReady(contextId);

    if (!jobRecords.isEmpty()) {
      for (JobRecord job : jobRecords) {
        DAGNode node = dagNodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), contextId);

        Map<String, Object> preprocesedInputs = new HashMap<>();
        Map<String, Object> inputs = new HashMap<>();
        List<VariableRecord> inputVariables = variableRecordService.find(job.getId(), LinkPortType.INPUT, contextId);
          
        for (VariableRecord inputVariable : inputVariables) {
          Object value = inputVariable.getValue();
          preprocesedInputs.put(inputVariable.getPortId(), value);
        }
        
        ContextRecord contextRecord = contextRecordService.find(job.getRootId());
        Context context = new Context(job.getRootId(), contextRecord.getConfig());
        String encodedApp = URIHelper.createDataURI(node.getApp().serialize());
        Job newJob = new Job(job.getExternalId(), job.getParentId(), job.getRootId(), job.getId(), encodedApp, JobStatus.READY, preprocesedInputs, null, context, null);
        
        try {
          Bindings bindings = BindingsFactory.create(encodedApp);
          
          for (VariableRecord inputVariable : inputVariables) {
            Object transform = inputVariable.getTransform();
            Object value = inputVariable.getValue();
            if(transform != null) {
              value = bindings.transformInputs(value, newJob, transform);
            }
          inputs.put(inputVariable.getPortId(), value);
          }
          jobs.add(new Job(job.getExternalId(), job.getParentId(), job.getRootId(), job.getId(), encodedApp, JobStatus.READY, inputs, null, context, null));
        } catch (BindingException e) {
          e.printStackTrace();
        }
      }
    }
    return jobs;
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
