package org.rabix.executor.execution.command;

import javax.inject.Inject;

import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.Executable.ExecutableStatus;
import org.rabix.executor.ExecutorException;
import org.rabix.executor.execution.ExecutableHandlerCommand;
import org.rabix.executor.handler.ExecutableHandler;
import org.rabix.executor.model.ExecutableData;
import org.rabix.executor.service.ExecutableDataService;

/**
 * Command that starts {@link ExecutableHandler}
 */
public class StartCommand extends ExecutableHandlerCommand {

  @Inject
  public StartCommand(ExecutableDataService executableDataService) {
    super(executableDataService);
  }

  @Override
  public Result run(ExecutableData data, ExecutableHandler handler, String contextId) {
    Executable executable = data.getExecutable();
    try {
      handler.start();
      executableDataService.save(data, "Executable " + executable.getId() + " started successfully.", ExecutableStatus.STARTED, contextId);
      started(data, "Executable " + executable.getId() + " started successfully.");
    } catch (ExecutorException e) {
      String message = String.format("Failed to start %s. %s", executable.getId(), e.toString());
      executableDataService.save(data, message, ExecutableStatus.FAILED, contextId);
      failed(data, message, e);
      return new Result(true);
    }
    return new Result(false);
  }

  @Override
  public ExecutableHandlerCommandType getType() {
    return ExecutableHandlerCommandType.START;
  }

}
