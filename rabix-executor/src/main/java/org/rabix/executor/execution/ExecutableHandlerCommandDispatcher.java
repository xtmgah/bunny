package org.rabix.executor.execution;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.rabix.bindings.model.Executable;
import org.rabix.executor.handler.ExecutableHandlerFactory;
import org.rabix.executor.model.ExecutableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executable execution command dispatcher.
 */
public class ExecutableHandlerCommandDispatcher {

  private static final Logger logger = LoggerFactory.getLogger(ExecutableHandlerCommandDispatcher.class);

  private final ExecutableHandlerFactory executableHandlerFactory;

  private final Map<String, Map<String, ExecutableHandlerRunnable>> executableHandlerRunnables = new HashMap<>();

  private final ThreadFactory executableHandlerThreadFactory;
  private final ExecutorService executableHandlerThreadExecutor;
  private final ScheduledExecutorService executableHandlerThreadCleanExecutor;

  @Inject
  public ExecutableHandlerCommandDispatcher(ExecutableHandlerFactory executableHandlerFactory) {
    this.executableHandlerFactory = executableHandlerFactory;
    this.executableHandlerThreadFactory = buildExecutableHandlerThreadFactory();
    this.executableHandlerThreadExecutor = Executors.newCachedThreadPool(executableHandlerThreadFactory);
    this.executableHandlerThreadCleanExecutor = Executors.newScheduledThreadPool(1);
    init();
  }

  /**
   * Initializes dispatcher
   */
  private void init() {
    scheduleCleaner();
  }

  /**
   * Dispatch commands to appropriate runnable threads
   */
  public void dispatch(ExecutableData executableData, ExecutableHandlerCommand command) {
    synchronized (executableHandlerRunnables) {
      String contextId = executableData.getExecutable().getContext().getId();
      ExecutableHandlerRunnable executableHandlerRunnable = getExecutables(contextId).get(executableData.getExecutable().getId());

      if (executableHandlerRunnable == null) {
        Executable executable = executableData.getExecutable();
        executableHandlerRunnable = new ExecutableHandlerRunnable(executable.getId(), executable.getContext().getId(), executableHandlerFactory.createHandler(executable));
        getExecutables(contextId).put(executable.getId(), executableHandlerRunnable);
        executableHandlerThreadExecutor.execute(executableHandlerRunnable);
        logger.info("ExecutableHandlerRunnable created for {}.", executable.getId());
      }
      executableHandlerRunnable.addCommand(command);
    }
  }

  private Map<String, ExecutableHandlerRunnable> getExecutables(String contextId) {
    synchronized (executableHandlerRunnables) {
      Map<String, ExecutableHandlerRunnable> executableList = executableHandlerRunnables.get(contextId);
      if (executableList == null) {
        executableList = new HashMap<>();
        executableHandlerRunnables.put(contextId, executableList);
      }
      return executableList;
    }
  }

  /**
   * Creates simple Executable handler thread factory
   */
  private ThreadFactory buildExecutableHandlerThreadFactory() {
    return new ExecutableHandlerThreadFactoryBuilder()
      .setNamePrefix("ExecutableHandler-Thread")
      .setDaemon(false)
      .setPriority(Thread.MAX_PRIORITY)
      .setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
          logger.error(String.format("Thread %s threw exception - %s", t.getName(), e.getMessage()));
        }
      }).build();
  }

  /**
   * Schedule cleaner thread that will go through the list of Executable threads
   */
  private void scheduleCleaner() {
    executableHandlerThreadCleanExecutor.scheduleWithFixedDelay(new Runnable() {
      @Override
      public void run() {
        synchronized (executableHandlerRunnables) {
          logger.debug("Cleaner thread is executing. There are {} runnable(s) in the pool.", executableHandlerRunnables.size());

          List<Map<String, String>> stoppedIds = new ArrayList<>();
          List<Map<String, String>> runningIds = new ArrayList<>();

          for (Entry<String, Map<String, ExecutableHandlerRunnable>> runnableEntry : executableHandlerRunnables.entrySet()) {
            String contextId = runnableEntry.getKey();
            for (Entry<String, ExecutableHandlerRunnable> runnable : runnableEntry.getValue().entrySet()) {
              String id = runnable.getKey();
              ExecutableHandlerRunnable thread = runnable.getValue();

              if (thread.isStopped()) {
                stoppedIds.add(mapping(id, contextId));
              } else {
                runningIds.add(mapping(id, contextId));
              }
            }
          }

          for (Map<String, String> stopped : stoppedIds) {
            logger.debug("Cleaner thread removes ExecutableHandlerRunnable for context {} and executable {}.", stopped.get("context_id"), stopped.get("executable_id"));
            executableHandlerRunnables.get(stopped.get("context_id")).remove(stopped.get("executable_id"));
          }
        }
      }

      private Map<String, String> mapping(String executableId, String contextId) {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("executable_id", executableId);
        mapping.put("context_id", contextId);
        return mapping;
      }

    }, 1, 1, TimeUnit.MINUTES);
  }

}
