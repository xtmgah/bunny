package org.rabix.engine.rest.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.ProtocolType;
import org.rabix.bindings.model.Context;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.impl.InitEvent;
import org.rabix.engine.model.ContextRecord;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.EventProcessor.IterationCallback;
import org.rabix.engine.rest.api.EngineHTTPService;
import org.rabix.engine.rest.db.TaskDB;
import org.rabix.engine.rest.dto.Task;
import org.rabix.engine.service.ContextService;
import org.rabix.engine.service.VariableService;

import com.google.inject.Inject;

public class EngineHTTPServiceImpl implements EngineHTTPService {

  private TaskDB taskDB;
  private EventProcessor eventProcessor;

  @Inject
  public EngineHTTPServiceImpl(TaskDB taskDB, DAGNodeDB dagNodeDB, ContextService contextService, VariableService variableService, EventProcessor eventProcessor) {
    this.taskDB = taskDB;
    this.eventProcessor = eventProcessor;
    
    List<IterationCallback> callbacks = new ArrayList<>();
    callbacks.add(new EndTaskCallback(contextService, taskDB));
    eventProcessor.start(callbacks);
  }
  
  @Override
  public Response start(Task task) {
    try {
      Context context = new Context(UUID.randomUUID().toString(), null);
      task.setId(context.getId());
      taskDB.set(context.getId(), false);

      Bindings bindings = BindingsFactory.create(task.getType());
      DAGNode node = bindings.translateToDAGFromPayload(task.getPayload());
      Object inputs = bindings.translateInputsFromPayload(task.getPayload());
      InitEvent initEvent = new InitEvent(context, node, inputs);
      eventProcessor.send(initEvent);
      return ok(task);
    } catch (Exception e) {
      throw new WebApplicationException(e);
    }
  }
  
  @Override
  public Response get(String id) {
    return ok(new Task(id, null, ProtocolType.DRAFT2, taskDB.get(id)));
  }
  
  @Override
  public Response get() {
    List<Task> tasks = new ArrayList<>();
    for (Entry<String, Boolean> entry : taskDB.getTaskStates().entrySet()) {
      tasks.add(new Task(entry.getKey(), null, ProtocolType.DRAFT2, entry.getValue()));
    }
    return ok(tasks);
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

  private Response ok(Object items) {
    if (items == null) {
      return ok();
    }
    return Response.ok().entity(items).build();
  }

  private Response ok() {
    return Response.ok().build();
  }
}
