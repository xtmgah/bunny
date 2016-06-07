package org.rabix.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.DAGNodeRecord.DAGNodeGraph;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.service.ApplicationService;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.DAGNodeService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.EngineServiceException;
import org.rabix.engine.service.VariableRecordService;

public class JobHelper {

  public static String generateId() {
    return UUID.randomUUID().toString();
  }
  
  public static Set<Job> createReadyJobs(JobRecordService jobRecordService, VariableRecordService variableRecordService, ContextRecordService contextRecordService, DAGNodeService dagNodeService, ApplicationService applicationService, String contextId) throws EngineServiceException {
    Set<Job> jobs = new HashSet<>();
    List<JobRecord> jobRecords = jobRecordService.findReady(contextId);

    if (!jobRecords.isEmpty()) {
      for (JobRecord job : jobRecords) {
        DAGNodeGraph node = dagNodeService.find(InternalSchemaHelper.normalizeId(job.getId()), contextId);

        Map<String, Object> inputs = new HashMap<>();
        List<VariableRecord> inputVariables = variableRecordService.find(job.getId(), LinkPortType.INPUT, contextId);
        for (VariableRecord inputVariable : inputVariables) {
          Object value = variableRecordService.transformValue(inputVariable);
          inputs.put(inputVariable.getPortId(), value);
        }
        ContextRecord contextRecord = contextRecordService.find(job.getRootId());
        Context context = new Context(job.getRootId(), contextRecord.getConfig());
        String encodedApp = URIHelper.createDataURI(applicationService.get(node.getAppHash()));
        jobs.add(new Job(job.getExternalId(), job.getParentId(), job.getRootId(), job.getId(), encodedApp, JobStatus.READY, inputs, null, context));
      }
    }
    return jobs;
  }
  
  public static Job fillOutputs(Job job, JobRecordService jobRecordService, VariableRecordService variableRecordService) throws EngineServiceException {
    JobRecord jobRecord = jobRecordService.findRoot(job.getContext().getId());
    List<VariableRecord> outputVariables = variableRecordService.find(jobRecord.getId(), LinkPortType.OUTPUT, job.getContext().getId());
    
    Map<String, Object> outputs = new HashMap<>();
    for (VariableRecord outputVariable : outputVariables) {
      Object value = variableRecordService.transformValue(outputVariable);
      outputs.put(outputVariable.getPortId(), value);
    }
    return Job.cloneWithOutputs(job, outputs);
  }
  
}
