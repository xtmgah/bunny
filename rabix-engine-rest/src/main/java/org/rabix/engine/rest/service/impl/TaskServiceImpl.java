package org.rabix.engine.rest.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.EventProcessor.IterationCallback;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.rest.db.TaskDB;
import org.rabix.engine.rest.dto.Task;
import org.rabix.engine.rest.plugin.BackendPluginDispatcher;
import org.rabix.engine.rest.service.TaskService;
import org.rabix.engine.rest.service.TaskServiceException;
import org.rabix.engine.service.ContextService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.LinkService;
import org.rabix.engine.service.VariableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class TaskServiceImpl implements TaskService {

  private final static Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

  private final TaskDB taskDB;
  private final EventProcessor eventProcessor;

  @Inject
  public TaskServiceImpl(TaskDB taskDB, JobService jobService, VariableService variableService, LinkService linkService, DAGNodeDB nodeDB, ExecutableServiceImpl executableService, ContextService contextService, BackendPluginDispatcher backendPluginDispatcher, EventProcessor eventProcessor) {
    this.taskDB = taskDB;
    this.eventProcessor = eventProcessor;

    List<IterationCallback> callbacks = new ArrayList<>();
    callbacks.add(new EndTaskCallback(contextService, taskDB));
    callbacks.add(new SendExecutablesCallback(executableService, backendPluginDispatcher));
    this.eventProcessor.start(callbacks);
  }

  public String create(Task task) throws TaskServiceException {
    String contextId = Context.createUniqueID();
    
    Context context = task.getContext() != null? task.getContext() : createContext(contextId);
    task.setId(contextId);
    context.setId(contextId);
    task.setContext(context);
    taskDB.add(task);

    Bindings bindings;
    try {
      bindings = BindingsFactory.create(task.getType());

      DAGNode node = bindings.translateToDAGFromPayload(task.getPayload());
      Object inputs = bindings.translateInputsFromPayload(task.getPayload());
      InitEvent initEvent = new InitEvent(context, node, inputs);

      eventProcessor.send(initEvent);
      return context.getId();
    } catch (BindingException e) {
      logger.error("Failed to create Bindings", e);
      throw new TaskServiceException("Failed to create Bindings", e);
    } catch (EventHandlerException e) {
      throw new TaskServiceException("Failed to start task", e);
    }
  }

  private Context createContext(String contextId) {
    Map<String, String> contextConfig = new HashMap<String, String>();
    contextConfig.put("backend.type", "WAGNER");    // use WAGNER by default
    return new Context(contextId, contextConfig);
  }
  
  public Set<Task> get() {
    return taskDB.getTasks();
  }
  
  public Task get(String id) {
    return taskDB.get(id);
  }

  private static class SendExecutablesCallback implements IterationCallback {

    private ExecutableServiceImpl executableService;
    private BackendPluginDispatcher backendPluginDispatcher;

    public SendExecutablesCallback(ExecutableServiceImpl executableService, BackendPluginDispatcher backendPluginDispatcher) {
      this.executableService = executableService;
      this.backendPluginDispatcher = backendPluginDispatcher;
    }

    @Override
    public void call(EventProcessor eventProcessor, String contextId, int iteration) throws Exception {
      List<Executable> executables = executableService.getReady(eventProcessor, contextId);
      backendPluginDispatcher.send(executables);
    }

  }

  private static class EndTaskCallback implements IterationCallback {
    private TaskDB taskDB;
    private ContextService contextService;

    public EndTaskCallback(ContextService contextService, TaskDB taskDB) {
      this.taskDB = taskDB;
      this.contextService = contextService;
    }

    @Override
    public void call(EventProcessor eventProcessor, String contextId, int iteration) {
      ContextRecord context = contextService.find(contextId);
      if (context.getStatus().equals(ContextStatus.COMPLETED)) {
        Task task = taskDB.get(contextId);
        task.setCompleted(true);
        taskDB.update(task);
      }
    }
  }

}
