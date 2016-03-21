package org.rabix.engine.rest.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.ProtocolType;
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
import org.rabix.engine.service.ContextService;
import org.rabix.engine.service.JobService;
import org.rabix.engine.service.LinkService;
import org.rabix.engine.service.VariableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class TaskService {

  private final static Logger logger = LoggerFactory.getLogger(TaskService.class);

  private final TaskDB taskDB;
  private final EventProcessor eventProcessor;

  @Inject
  public TaskService(TaskDB taskDB, JobService jobService, VariableService variableService, LinkService linkService, DAGNodeDB nodeDB, ExecutableService executableService, ContextService contextService, BackendPluginDispatcher backendPluginDispatcher, EventProcessor eventProcessor) {
    this.taskDB = taskDB;
    this.eventProcessor = eventProcessor;

    List<IterationCallback> callbacks = new ArrayList<>();
    callbacks.add(new EndTaskCallback(contextService, taskDB));
    callbacks.add(new SendExecutablesCallback(executableService, backendPluginDispatcher));
    this.eventProcessor.start(callbacks);
  }

  public String create(Task task) throws TaskServiceException {
    Map<String, String> contextConfig = new HashMap<String, String>();
    contextConfig.put("backend.type", "WAGNER");

    Context context = new Context(Context.createUniqueID(), contextConfig);
    task.setId(context.getId());
    taskDB.set(context.getId(), false);

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

  public List<Task> get() {
    List<Task> tasks = new ArrayList<>();
    for (Entry<String, Boolean> entry : taskDB.getTaskStates().entrySet()) {
      tasks.add(new Task(entry.getKey(), null, ProtocolType.DRAFT2, entry.getValue()));
    }
    return tasks;
  }
  
  public Task get(String id) {
    boolean status = taskDB.getTaskStates().get(id);
    return new Task(id, null, ProtocolType.DRAFT2, status);
  }

  private static class SendExecutablesCallback implements IterationCallback {

    private ExecutableService executableService;
    private BackendPluginDispatcher backendPluginDispatcher;

    public SendExecutablesCallback(ExecutableService executableService, BackendPluginDispatcher backendPluginDispatcher) {
      this.executableService = executableService;
      this.backendPluginDispatcher = backendPluginDispatcher;
    }

    @Override
    public void call(EventProcessor eventProcessor, String contextId, int iteration) throws Exception {
      List<Executable> executables = executableService.getReadyExecutables(eventProcessor, contextId);
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
        taskDB.set(contextId, true);
      }
    }
  }

}
