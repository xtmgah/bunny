package org.rabix.engine.service.scatter;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.model.DAGNodeRecord.DAGNodeGraph;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.model.scatter.ScatterStrategy;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.DAGNodeService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.EngineServiceException;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.scatter.strategy.ScatterStrategyHandler;
import org.rabix.engine.service.scatter.strategy.ScatterStrategyHandlerFactory;

import com.google.inject.Inject;

public class ScatterService {

  private final DAGNodeService dagNodeService;
  private final EventProcessor eventProcessor;
  
  private final JobRecordService jobRecordService;
  private final LinkRecordService linkRecordService;
  private final VariableRecordService variableRecordService;
  private final ScatterStrategyHandlerFactory scatterStrategyHandlerFactory;
  
  @Inject
  public ScatterService(final DAGNodeService dagNodeService, final JobRecordService jobRecordService, final VariableRecordService variableRecordService, final LinkRecordService linkRecordService, final EventProcessor eventProcessor, final ScatterStrategyHandlerFactory scatterStrategyHandlerFactory) {
    this.dagNodeService = dagNodeService;
    this.eventProcessor = eventProcessor;
    this.jobRecordService = jobRecordService;
    this.linkRecordService = linkRecordService;
    this.variableRecordService = variableRecordService;
    this.scatterStrategyHandlerFactory = scatterStrategyHandlerFactory;
  }
  
  /**
   * Scatters port
   */
  @SuppressWarnings("unchecked")
  public void scatterPort(JobRecord job, String portId, Object value, Integer position, Integer numberOfScatteredFromEvent, boolean isLookAhead, boolean isFromEvent) throws EventHandlerException, EngineServiceException {
    DAGNodeGraph node = dagNodeService.find(InternalSchemaHelper.normalizeId(job.getId()), job.getRootId());

    ScatterStrategyHandler scatterStrategyHandler = scatterStrategyHandlerFactory.create(node.getScatterMethod());
    if (job.getScatterStrategy() == null) {
      job.setScatterStrategy(scatterStrategyHandler.initialize(node));
    }

    if (isLookAhead) {
      int numberOfScattered = getScatteredCount(scatterStrategyHandler, job, numberOfScatteredFromEvent);
      createScatteredJobs(job, portId, value, node, numberOfScattered, position);
      return;
    }

    List<Object> values = null;
    boolean usePositionFromEvent = true;
    if (isFromEvent || !(value instanceof List<?>)) {
      int numberOfInputPortIncoming = jobRecordService.getInputPortIncoming(job, portId);
      if (numberOfInputPortIncoming == 1) {
        usePositionFromEvent = false;
        values = (List<Object>) value;
      } else {
        values = new ArrayList<>();
        values.add(value);
      }
    } else {
      usePositionFromEvent = false;
      values = (List<Object>) value;
    }
    
    for (int i = 0; i < values.size(); i++) {
      createScatteredJobs(job, portId, values.get(i), node, values.size(), usePositionFromEvent ? position : i + 1);
    }
  }
  
  public JobRecord createJobRecord(String id, String parentId, DAGNodeGraph node, boolean isScattered, String contextId) {
    boolean isBlocking = false;
    for (LinkMerge linkMerge : node.getLinkMergeSet(LinkPortType.INPUT)) {
      if (LinkMerge.isBlocking(linkMerge)) {
        isBlocking = true;
        break;
      }
    }
    if (ScatterMethod.isBlocking(node.getScatterMethod())) {
      isBlocking = true;
    }
    return new JobRecord(contextId, id, JobRecordService.generateUniqueId(), parentId, JobState.PENDING, node.isContainer(), isScattered, isBlocking);
  }
  
  private void createScatteredJobs(JobRecord job, String port, Object value, DAGNodeGraph node, Integer scatteredCount, Integer position) throws EventHandlerException, EngineServiceException {
    ScatterStrategy scatterStrategy = job.getScatterStrategy();
    
    ScatterStrategyHandler scatterStrategyHandler = scatterStrategyHandlerFactory.create(node.getScatterMethod());
    scatterStrategyHandler.enable(scatterStrategy, port, value, position);
    
    List<RowMapping> mappings = null;
    try {
      mappings = scatterStrategyHandler.enabled(scatterStrategy);
    } catch (BindingException e) {
      throw new EventHandlerException("Failed to enable ScatterStrategy for node " + node.getId(), e);
    }
    scatterStrategyHandler.commit(scatterStrategy, mappings);
    
    for (RowMapping mapping : mappings) {
      job.setState(JobState.RUNNING);
      jobRecordService.update(job);

      List<Event> events = new ArrayList<>();

      String jobNId = InternalSchemaHelper.scatterId(job.getId(), mapping.getIndex());
      JobRecord jobN = createJobRecord(jobNId, job.getExternalId(), node, true, job.getRootId());
          
      for (DAGLinkPort inputPort : node.getInputPorts()) {
        Object defaultValue = node.getDefaults().get(inputPort.getId());
        VariableRecord variableN = new VariableRecord(job.getRootId(), jobNId, inputPort.getId(), LinkPortType.INPUT, defaultValue, node.getLinkMerge(inputPort.getId(), inputPort.getType()));
        variableN.setGlobalsCount(getScatteredCount(scatterStrategyHandler, job, scatteredCount));
        variableRecordService.create(variableN);

        if (jobN.getState().equals(JobState.PENDING)) {
          jobRecordService.incrementPortCounter(jobN, inputPort, LinkPortType.INPUT);
        }
        LinkRecord link = new LinkRecord(job.getRootId(), job.getId(), inputPort.getId(), LinkPortType.INPUT, jobNId, inputPort.getId(), LinkPortType.INPUT, 1);
        linkRecordService.create(link);

        if (inputPort.isScatter()) {
          Event eventInputPort = new InputUpdateEvent(job.getRootId(), jobNId, inputPort.getId(), mapping.getValue(inputPort.getId()), 1);
          events.add(eventInputPort);
        } else {
          boolean isInputPortReady = jobRecordService.isInputPortReady(job, inputPort.getId());
          if (isInputPortReady) {
            VariableRecord variable = variableRecordService.find(job.getId(), inputPort.getId(), LinkPortType.INPUT, job.getRootId());
            events.add(new InputUpdateEvent(job.getRootId(), jobNId, inputPort.getId(), variableRecordService.transformValue(variable), 1));
          }
        }
      }
      for (DAGLinkPort outputPort : node.getOutputPorts()) {
        VariableRecord variableN = new VariableRecord(job.getRootId(), jobNId, outputPort.getId(), LinkPortType.OUTPUT, null, node.getLinkMerge(outputPort.getId(), outputPort.getType()));
        variableN.setGlobalsCount(getScatteredCount(scatterStrategyHandler, job, scatteredCount));
        variableRecordService.create(variableN);
        jobRecordService.incrementPortCounter(jobN, outputPort, LinkPortType.OUTPUT);

        LinkRecord link = new LinkRecord(job.getRootId(), jobNId, outputPort.getId(), LinkPortType.OUTPUT, job.getId(), outputPort.getId(), LinkPortType.OUTPUT, null);
        linkRecordService.create(link);
      }

      job.setState(JobState.RUNNING);
      job.setScatterWrapper(true);
      
      jobRecordService.resetOutputPortCounters(job, getScatteredCount(scatterStrategyHandler, job, scatteredCount));
      jobRecordService.update(job);
      
      jobN.setGlobalOutputsCount(getScatteredCount(scatterStrategyHandler, job, scatteredCount));
      jobRecordService.create(jobN);

      for (Event subevent : events) {
        eventProcessor.send(subevent);
      }
    }
  }
  
  /**
   * Get number of scattered jobs 
   */
  private int getScatteredCount(ScatterStrategyHandler scatterStrategyHandler, JobRecord job, Integer scatteredNodes) {
    if (scatteredNodes != null) {
      return Math.max(scatteredNodes, scatterStrategyHandler.enabledCount(job.getScatterStrategy()));
    } else {
      return scatterStrategyHandler.enabledCount(job.getScatterStrategy());
    }
  }
  
}
