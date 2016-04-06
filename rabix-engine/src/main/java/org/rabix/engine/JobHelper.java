package org.rabix.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.Job.JobStatus;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.service.ContextRecordService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.VariableRecordService;

public class JobHelper {

  public static Set<Job> createReadyJobs(JobRecordService jobRecordService, VariableRecordService variableRecordService, ContextRecordService contextRecordService, DAGNodeDB dagNodeDB, String contextId) {
    Set<Job> jobs = new HashSet<>();
    List<JobRecord> jobRecords = jobRecordService.findReady(contextId);

    if (!jobRecords.isEmpty()) {
      for (JobRecord job : jobRecords) {
        DAGNode node = dagNodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), contextId);

        Map<String, Object> inputs = new HashMap<>();
        List<VariableRecord> inputVariables = variableRecordService.find(job.getId(), LinkPortType.INPUT, contextId);
        for (VariableRecord inputVariable : inputVariables) {
          inputs.put(inputVariable.getPortId(), inputVariable.getValue());
        }
        ContextRecord contextRecord = contextRecordService.find(job.getContextId());
        Context context = new Context(contextRecord.getId(), contextRecord.getConfig());
        String encodedApp = URIHelper.createDataURI(BeanSerializer.serializeFull(node.getApp()));
        jobs.add(new Job(job.getExternalId(), job.getId(), encodedApp, JobStatus.READY, inputs, null, context));
      }
    }
    return jobs;
  }
  
}
