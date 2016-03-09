package org.rabix.executor.execution;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.rabix.executor.execution.ExecutableHandlerCommand.Repeat;
import org.rabix.executor.handler.ExecutableHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executable execution thread runnable. It executes commands one by one in synchronous matter. 
 */
public class ExecutableHandlerRunnable implements Runnable {

  private final static Logger logger = LoggerFactory.getLogger(ExecutableHandlerRunnable.class);

  private final static long DEFAULT_SLEEP_TIME = TimeUnit.SECONDS.toMillis(1);

  private final String contextId;
  private final String executableId;
  private final ExecutableHandler executableHandler;
  private final BlockingQueue<ExecutableHandlerCommand> commands;

  private final AtomicBoolean stop = new AtomicBoolean(false);

  public ExecutableHandlerRunnable(String id, String contextId, ExecutableHandler executableHandler) {
    this.executableId = id;
    this.contextId = contextId;
    this.executableHandler = executableHandler;
    this.commands = new LinkedBlockingQueue<>();
  }

  @Override
  public void run() {
    logger.info("ExecutableHandlerRunnable {} started.", Thread.currentThread().getName());

    long sleepTime = DEFAULT_SLEEP_TIME;
    
    while (!isStopped()) {
      try {
        ExecutableHandlerCommand command = commands.poll();
        if (command == null) {
          logger.debug("No active commands. Sleep for {}", sleepTime);
          Thread.sleep(sleepTime);
          continue;
        }
        logger.debug("Command {} found. Start execution.", command);

        Repeat repeat = command.getRepeat();
        if (repeat != null) {
          logger.debug("Command {} is repeatable. Delay and put it back to queue.", command);
          Thread.sleep(repeat.delay);
          addCommand(command);
        }

        ExecutableHandlerCommand.Result result = command.run(executableId, contextId, executableHandler);
        if (result.isLastCommand) {
          logger.debug("Command {} is last command. Stop thread.", command);
          stop();
        }
      } catch (Exception e) {
        logger.error("ExecutableHandlerRunnable faced a runtime error. Stop execution.", e);
        stop();
      }
    }
    logger.info("ExecutableHandlerRunnable {} finished.", Thread.currentThread().getName());
  }

  /**
   * Add command to queue 
   */
  public void addCommand(ExecutableHandlerCommand command) {
    if (stop.get()) {
      logger.error("Failed to add command {}. Thread is stopped.", command);
    }
    this.commands.add(command);
  }

  /**
   * Stop runnable
   */
  public void stop() {
    stop.set(true);
    logger.info("ExecutableHandlerRunnable {} stopped.", Thread.currentThread().getName());
  }

  /**
   * Is runnable stopped? 
   */
  public boolean isStopped() {
    return stop.get();
  }

}
