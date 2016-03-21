package org.rabix.executor.execution.command;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.Executable.ExecutableStatus;
import org.rabix.executor.execution.ExecutableHandlerCommand;
import org.rabix.executor.handler.ExecutableHandler;
import org.rabix.executor.model.ExecutableData;
import org.rabix.executor.service.ExecutableDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that checks status of {@link ExecutableHandler} 
 */
public class StatusCommand extends ExecutableHandlerCommand {

  private final static Logger logger = LoggerFactory.getLogger(StatusCommand.class);

  public final static long DEFAULT_DELAY = TimeUnit.SECONDS.toMillis(15);
  
  @Inject
  public StatusCommand(ExecutableDataService executableDataService) {
    super(executableDataService);
  }

  @Override
  public Result run(ExecutableData executableData, ExecutableHandler executableHandler, String contextId) {
    String executableId = executableData.getExecutable().getId();
    logger.debug("Check status for {} command line tool.", executableId);

    if (!ExecutableStatus.STARTED.equals(executableData.getStatus())) {
      logger.info("Command line tool {} is not started yet.", executableId);
      return new Result(false);
    }
    try {
      Executable executable = executableData.getExecutable();
      if (executableHandler.isRunning()) {
        logger.info("Command line tool {} for context {} is still running.", executable.getId(), executable.getContext().getId());
        return new Result(false);
      }
      
      String message = null;
      executable = executableHandler.postprocess(executableData.isTerminal());
      if (!executableHandler.isSuccessful()) {
        message = String.format("Executable %s failed with exit code %d.", executable.getId(), executableHandler.getExitStatus());
        executableDataService.save(executableData, message, ExecutableStatus.FAILED, contextId);
        failed(executableData, message, null);
      } else {
        message = String.format("Executable %s completed successfully.", executable.getId());
        executableDataService.save(executableData, message, ExecutableStatus.COMPLETED, contextId);
        completed(executableData, message, executable.getOutputs());
      }
    } catch (Exception e) {
      String message = String.format("Failed to execute status command for %s. %s", executableId, e.getMessage());
      executableDataService.save(executableData, message, ExecutableStatus.FAILED, contextId);
      failed(executableData, message, e);
      return new Result(true);
    }
    return new Result(true);
  }

  @Override
  public Repeat getRepeat() {
    return new Repeat(DEFAULT_DELAY);
  }

  @Override
  public ExecutableHandlerCommandType getType() {
    return ExecutableHandlerCommandType.STATUS;
  }

}
