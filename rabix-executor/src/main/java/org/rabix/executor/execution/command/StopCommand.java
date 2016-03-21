package org.rabix.executor.execution.command;

import javax.inject.Inject;

import org.rabix.bindings.model.Executable.ExecutableStatus;
import org.rabix.executor.ExecutorException;
import org.rabix.executor.execution.ExecutableHandlerCommand;
import org.rabix.executor.handler.ExecutableHandler;
import org.rabix.executor.model.ExecutableData;
import org.rabix.executor.service.ExecutableDataService;

/**
 * Command that stops {@link ExecutableHandler} 
 */
public class StopCommand extends ExecutableHandlerCommand {

  @Inject
  public StopCommand(ExecutableDataService executableDataService) {
    super(executableDataService);
  }

  @Override
  public Result run(ExecutableData executableData, ExecutableHandler handler, String contextId) {
    String executableId = executableData.getExecutable().getId();
    try {
      handler.stop();

      String message = String.format("Executable %s aborted successfully.", executableId);
      executableDataService.save(executableData, message, ExecutableStatus.ABORTED, contextId);
      stopped(executableData, message);
    } catch (ExecutorException e) {
      String message = String.format("Failed to stop %s. %s", executableId, e.toString());
      executableDataService.save(executableData, message, ExecutableStatus.FAILED, contextId);
    }
    return new Result(true);
  }

  @Override
  public ExecutableHandlerCommandType getType() {
    return ExecutableHandlerCommandType.STOP;
  }

}
