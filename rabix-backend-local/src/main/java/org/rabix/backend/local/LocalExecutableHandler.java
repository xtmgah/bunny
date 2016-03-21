package org.rabix.backend.local;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.Executable.ExecutableStatus;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.EventProcessor.IterationCallback;
import org.rabix.engine.service.ContextService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.JobService.JobState;
import org.rabix.engine.service.VariableService;
import org.rabix.executor.service.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple (local) executable handler that dispatches Executable(s) to the single Executor 
 */
public class LocalExecutableHandler implements IterationCallback {

  private final static Logger logger = LoggerFactory.getLogger(LocalExecutableHandler.class);
  
  private JobService jobService;
  private DAGNodeDB dagNodeDB;
  private VariableService variableService;
  private ExecutorService executorService;

  private java.util.concurrent.ExecutorService threadPool = Executors.newCachedThreadPool();

  private Set<Executable> runningExecutables = new HashSet<>();
  private ContextService contextService;

  public LocalExecutableHandler(ExecutorService executorService, JobService jobService, VariableService variableService, ContextService contextService, DAGNodeDB dagNodeDB) {
    this.jobService = jobService;
    this.variableService = variableService;
    this.dagNodeDB = dagNodeDB;
    this.executorService = executorService;
    this.contextService = contextService;
  }

  @Override
  public void call(final EventProcessor eventProcessor, final String contextId, int iteration) {
    List<Executable> executables = createExecutables(eventProcessor, contextId);
    for (final Executable executable : executables) {
      if (!runningExecutables.contains(executable)) {
        runningExecutables.add(executable);

        executorService.start(executable, contextId);
        threadPool.submit(new Runnable() {
          @Override
          public void run() {
            ExecutableStatus status = executorService.findStatus(executable.getId(), contextId);
            while (!(status.equals(ExecutableStatus.COMPLETED) || status.equals(ExecutableStatus.FAILED))) {
              try {
                Thread.sleep(7000);
              } catch (InterruptedException e) {
                logger.error("Fatal error happened.", e);
                System.exit(1);
              }
              status = executorService.findStatus(executable.getId(), contextId);
            }
            try {
              Bindings bindings = BindingsFactory.create(executable);
              if (status.equals(ExecutableStatus.COMPLETED)) {
                Object results = executorService.getResult(executable.getId(), contextId);

                ProtocolType protocolType = bindings.getProtocolType();
                JobStatusEvent statusEvent = new JobStatusEvent(executable.getNodeId(), executable.getContext().getId(), JobState.COMPLETED, results, protocolType);
                eventProcessor.addToQueue(statusEvent);
              } else {
                logger.error("Executable {} has failed. Stop everything.", executable.getId());
                System.exit(10);
              }
            } catch (BindingException e) {
              logger.error("Cannot find Bindings");
              System.exit(1);
            }
          }
        });
      }
    }
  }

  /**
   * Creates ready Executables 
   */
  private List<Executable> createExecutables(EventProcessor eventProcessor, String contextId) {
    List<Executable> executables = new ArrayList<>();
    List<JobRecord> jobs = jobService.findReady(contextId);

    if (!jobs.isEmpty()) {
      for (JobRecord job : jobs) {
        DAGNode node = dagNodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), contextId);

        try {
          Bindings bindings = BindingsFactory.create(node.getApp());

          Object inputs = null;
          List<VariableRecord> inputVariables = variableService.find(job.getId(), LinkPortType.INPUT, contextId);
          for (VariableRecord inputVariable : inputVariables) {
            inputs = bindings.addToInputs(inputs, inputVariable.getPortId(), inputVariable.getValue());
          }
          ContextRecord contextRecord = contextService.find(job.getContextId());
          Context context = new Context(contextRecord.getId(), contextRecord.getConfig());
          executables.add(new Executable(job.getExternalId(), job.getId(), node, ExecutableStatus.READY, inputs, null, context));
        } catch (BindingException e) {
          logger.error("Cannot find Bindings.", e);
          System.exit(1);
        }
      }
    }
    return executables;
  }
}
