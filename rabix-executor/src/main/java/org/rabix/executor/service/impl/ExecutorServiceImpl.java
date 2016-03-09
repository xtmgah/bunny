package org.rabix.executor.service.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.Executable.ExecutableStatus;
import org.rabix.executor.execution.ExecutableHandlerCommandDispatcher;
import org.rabix.executor.execution.command.StartCommand;
import org.rabix.executor.execution.command.StatusCommand;
import org.rabix.executor.execution.command.StopCommand;
import org.rabix.executor.model.ExecutableData;
import org.rabix.executor.service.ExecutableDataService;
import org.rabix.executor.service.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ExecutorServiceImpl implements ExecutorService {

  private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceImpl.class);

  private final ExecutableDataService executableDataService;
  private final ExecutableHandlerCommandDispatcher executableHandlerCommandDispatcher;

  private final Provider<StopCommand> stopCommandProvider;
  private final Provider<StartCommand> startCommandProvider;
  private final Provider<StatusCommand> statusCommandProvider;
  
  private final AtomicBoolean stopped = new AtomicBoolean(false);

  @Inject
  public ExecutorServiceImpl(ExecutableDataService executableDataService, ExecutableHandlerCommandDispatcher executableHandlerCommandDispatcher, Provider<StopCommand> stopCommandProvider,
      Provider<StartCommand> startCommandProvider, Provider<StatusCommand> statusCommandProvider) {
    this.executableDataService = executableDataService;
    this.stopCommandProvider = stopCommandProvider;
    this.startCommandProvider = startCommandProvider;
    this.statusCommandProvider = statusCommandProvider;
    this.executableHandlerCommandDispatcher = executableHandlerCommandDispatcher;
  }

  @Override
  public void start(final Executable executable, String contextId) {
    logger.debug("start(id={}, important={}, uploadOutputs={})", executable.getId());

    final ExecutableData jobData = new ExecutableData(executable, ExecutableStatus.READY, false, false);
    executableDataService.save(jobData, contextId);

    executableHandlerCommandDispatcher.dispatch(jobData, startCommandProvider.get());
    executableHandlerCommandDispatcher.dispatch(jobData, statusCommandProvider.get());
  }

  @Override
  public void stop(String jobId, String contextId) {
    logger.debug("stop(id={})", jobId);

    final ExecutableData jobData = executableDataService.find(jobId, contextId);
    executableHandlerCommandDispatcher.dispatch(jobData, stopCommandProvider.get());
  }

  @Override
  public ExecutableStatus findStatus(String jobId, String contextId) {
    logger.debug("findStatus(id={})", jobId);

    ExecutableData jobData = executableDataService.find(jobId, contextId);
    if (jobData != null) {
      return jobData.getStatus();
    }
    return null;
  }

  @Override
  public void shutdown(Boolean stopEverything) {
    logger.debug("shutdown(stopEverything={})", stopEverything);

    List<ExecutableData> jobsToStop = executableDataService.find(ExecutableStatus.STARTED, ExecutableStatus.READY);

    int abortedJobsCount = 0;
    if (jobsToStop != null) {
      for (ExecutableData jobData : jobsToStop) {
        if (!stopEverything && jobData.isImportant()) {
          continue;
        }
        executableHandlerCommandDispatcher.dispatch(jobData, stopCommandProvider.get());
        abortedJobsCount++;
      }
    }
    stopped.set(true);
    String message = String.format("Shutdown%s executed. Worker has stopped %d %s.", stopEverything ? " NOW" : "", abortedJobsCount, abortedJobsCount == 1 ? "job" : "jobs");
    logger.info(message);
  }

  @Override
  public Object getResult(String id, String contextId) {
    ExecutableData jobData = executableDataService.find(id, contextId);
    return jobData.getResult();
  }
  
  @Override
  public boolean isRunning(String id, String contextId) {
    logger.debug("isRunning(id={})", id);

    ExecutableData jobData = executableDataService.find(id, contextId);
    if (jobData != null && !isFinished(jobData.getStatus())) {
      logger.info("Command line tool {} is running. The status is {}", id, jobData.getStatus());
      return true;
    }
    logger.info("Command line tool {} is not running", id);
    return false;
  }

  private boolean isFinished(ExecutableStatus jobStatus) {
    switch (jobStatus) {
    case FINISHED:
    case FAILED:
    case STOPPED:
      return true;
    default:
      return false;
    }
  }

  @Override
  public boolean isStopped() {
    return stopped.get();
  }

}
