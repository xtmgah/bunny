package org.rabix.backend.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.Executable.ExecutableStatus;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.FileHelper;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.EventProcessor.IterationCallback;
import org.rabix.engine.service.ContextService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.LinkService;
import org.rabix.engine.service.VariableService;

import dnl.utils.text.table.TextTable;

/**
 * Prints engine information (iterations) to the output directory
 */
public class CommandLinePrinter implements IterationCallback {

  private static final String JOBS_FILE = "jobs.txt";
  private static final String LINKS_FILE = "links.txt";
  private static final String VARIABLES_FILE = "variables.txt";
  
  private static final String READY_EXECUTABLES_DIR = "ready";
  
  
  private final File outputDir;

  private final DAGNodeDB nodeDB;
  private final JobService jobService;
  private final VariableService variableService;
  private final LinkService linkService;
  private final ContextService contextService;

  public CommandLinePrinter(File outputDir, String contextId, JobService jobService, VariableService variableService, LinkService linkService, ContextService contextService, DAGNodeDB nodeDB) {
    this.nodeDB = nodeDB;
    this.jobService = jobService;
    this.variableService = variableService;
    this.contextService = contextService;
    this.linkService = linkService;
    this.outputDir = outputDir;
  }

  @Override
  public void call(EventProcessor eventProcessor, String contextId, int iteration) {
    printJobs(eventProcessor, contextId, iteration);
    printVariables(eventProcessor, contextId, iteration);
    printLinks(eventProcessor, contextId, iteration);

    File iterationDirectory = new File(outputDir, "iteration_" + iteration);
    if (!iterationDirectory.exists()) {
      iterationDirectory.mkdirs();
    }
    List<Executable> executables = createExecutables(eventProcessor, contextId);
    if (!executables.isEmpty()) {
      File readyDirectory = new File(iterationDirectory, READY_EXECUTABLES_DIR);
      readyDirectory.mkdirs();

      int suffix = 0;
      for (Executable executable : executables) {
        FileHelper.serialize(new File(readyDirectory, "executable_" + suffix++ + ".json"), executable);
      }
    }
  }

  private void printJobs(EventProcessor eventProcessor, String contextId, int iteration) {
    List<JobRecord> jobs = jobService.find(contextId);
    Object[][] data = new Object[jobs.size()][8];

    for (int i = 0; i < jobs.size(); i++) {
      JobRecord jr = jobs.get(i);

      data[i][0] = contextId;
      data[i][1] = jr.getId();
      data[i][2] = jr.getExternalId();
      data[i][3] = jr.getState();

      String inputCounters = new String();
      for (int j = 0; j < jr.getInputCounters().size(); j++) {
        if (j != 0) {
          inputCounters += ",";
        }
        PortCounter pc = jr.getInputCounters().get(j);
        inputCounters += pc.getPort() + "=" + pc.getCounter();
        if (pc.isScatter()) {
          inputCounters += " [S]";
        }
      }
      data[i][4] = inputCounters;

      String outputCounters = new String();
      for (int j = 0; j < jr.getOutputCounters().size(); j++) {
        if (j != 0) {
          outputCounters += ",";
        }
        PortCounter pc = jr.getOutputCounters().get(j);
        outputCounters += pc.getPort() + "=" + pc.getCounter();
      }
      data[i][5] = outputCounters;
      data[i][6] = jr.isContainer();
      data[i][7] = jr.isScattered();
    }

    try {
      File iterationDirectory = new File(outputDir, "iteration_" + iteration);
      if (!iterationDirectory.exists()) {
        iterationDirectory.mkdirs();
      }

      File linksFile = new File(iterationDirectory, JOBS_FILE);
      linksFile.createNewFile();

      PrintStream ps = new PrintStream(new FileOutputStream(linksFile, true));
      TextTable tt = new TextTable(new String[] { "CONTEXT", "ID", "EXTERNAL_ID", "STATE", "INPUT COUNTERS", "OUTPUT COUNTERS", "IS CONTAINER", "IS SCATTERED" }, data);
      tt.printTable(ps, 0);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void printLinks(EventProcessor eventProcessor, String contextId, int iteration) {
    List<LinkRecord> links = linkService.find(contextId);
    Object[][] data = new Object[links.size()][7];

    for (int i = 0; i < links.size(); i++) {
      LinkRecord lr = links.get(i);

      data[i][0] = contextId;
      data[i][1] = lr.getSourceJobId();
      data[i][2] = lr.getSourceJobPort();
      data[i][3] = lr.getSourceVarType();
      data[i][4] = lr.getDestinationJobId();
      data[i][5] = lr.getDestinationJobPort();
      data[i][6] = lr.getDestinationVarType();
    }

    try {
      File iterationDirectory = new File(outputDir, "iteration_" + iteration);
      if (!iterationDirectory.exists()) {
        iterationDirectory.mkdirs();
      }

      File linksFile = new File(iterationDirectory, LINKS_FILE);
      linksFile.createNewFile();

      PrintStream ps = new PrintStream(new FileOutputStream(linksFile, true));
      TextTable tt = new TextTable(new String[] { "CONTEXT", "SOURCE", "PORT", "TYPE", "DESTINATION", "PORT", "TYPE" }, data);
      tt.printTable(ps, 0);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void printVariables(EventProcessor eventProcessor, String contextId, int iteration) {
    List<VariableRecord> variables = variableService.find(contextId);
    Object[][] data = new Object[variables.size()][5];

    for (int i = 0; i < variables.size(); i++) {
      VariableRecord vr = variables.get(i);

      data[i][0] = contextId;
      data[i][1] = vr.getJobId();
      data[i][2] = vr.getPortId();
      data[i][3] = vr.getType();
      data[i][4] = vr.getValue();
    }

    try {
      File iterationDirectory = new File(outputDir, "iteration_" + iteration);
      if (!iterationDirectory.exists()) {
        iterationDirectory.mkdirs();
      }

      File linksFile = new File(iterationDirectory, VARIABLES_FILE);
      linksFile.createNewFile();

      PrintStream ps = new PrintStream(new FileOutputStream(linksFile, true));
      TextTable tt = new TextTable(new String[] { "CONTEXT", "ID", "PORT", "TYPE", "VALUE" }, data);
      tt.printTable(ps, 0);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<Executable> createExecutables(EventProcessor eventProcessor, String contextId) {
    List<Executable> executables = new ArrayList<>();
    List<JobRecord> jobs = jobService.findReady(contextId);

    if (!jobs.isEmpty()) {
      for (JobRecord job : jobs) {
        DAGNode node = nodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), contextId);
        Map<String, Object> inputs = new HashMap<>();

        List<VariableRecord> inputVariables = variableService.find(job.getId(), LinkPortType.INPUT, contextId);
        for (VariableRecord inputVariable : inputVariables) {
          inputs.put(inputVariable.getPortId(), inputVariable.getValue());
        }
        ContextRecord contextRecord = contextService.find(job.getContextId());
        Context context = new Context(contextRecord.getId(), contextRecord.getConfig());
        executables.add(new Executable(job.getExternalId(), job.getId(), node, ExecutableStatus.READY, inputs, null, context, false));
      }
    }
    return executables;
  }

}
