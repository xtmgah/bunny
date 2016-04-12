package org.rabix.engine.processor.handler.impl;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGLink;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.db.DAGNodeDB;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.model.scatter.RowMapping;
import org.rabix.engine.model.scatter.ScatterMapping;
import org.rabix.engine.model.scatter.impl.ScatterCartesianMapping;
import org.rabix.engine.model.scatter.impl.ScatterOneToOneMapping;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;

import com.google.inject.Inject;

/**
 * Handles {@link InputUpdateEvent} events.
 */
public class InputEventHandler implements EventHandler<InputUpdateEvent> {

  private final DAGNodeDB nodeDB;
  private final JobRecordService jobService;
  private final LinkRecordService linkService;
  private final VariableRecordService variableService;
  
  private final EventProcessor eventProcessor;

  @Inject
  public InputEventHandler(EventProcessor eventProcessor, JobRecordService jobService, VariableRecordService variableService, LinkRecordService linkService, DAGNodeDB nodeDB) {
    this.nodeDB = nodeDB;
    this.jobService = jobService;
    this.linkService = linkService;
    this.variableService = variableService;
    this.eventProcessor = eventProcessor;
  }
  
  @Override
  public void handle(InputUpdateEvent event) throws EventHandlerException {
    JobRecord job = jobService.find(event.getJobId(), event.getContextId());
    VariableRecord variable = variableService.find(event.getJobId(), event.getPortId(), LinkPortType.INPUT, event.getContextId());

    DAGNode node = nodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), event.getContextId());

    if (event.isLookAhead()) {
      if (job.isBlocking() || (job.getInputPortIncoming(event.getPortId()) > 1)) {
        return; // guard: should not happen
      } else {
        job.resetInputPortCounters(event.getNumberOfScattered());
      }
    } else if ((job.getInputPortIncoming(event.getPortId()) > 1) && job.isScatterPort(event.getPortId()) && !LinkMerge.isBlocking(node.getLinkMerge(event.getPortId(), LinkPortType.INPUT))) {
      job.resetOutputPortCounters(job.getInputPortIncoming(event.getPortId()));
    }
    
    variable.addValue(event.getValue(), event.getPosition());
    job.decrementPortCounter(event.getPortId(), LinkPortType.INPUT);
    
    // scatter
    if (!job.isBlocking() && !job.isScattered()) {
      if (job.isScatterPort(event.getPortId())) {
        if ((job.getInputPortIncoming(event.getPortId()) > 1) && LinkMerge.isBlocking(node.getLinkMerge(event.getPortId(), LinkPortType.INPUT))) {
          // it's blocking
          if (job.isInputPortReady(event.getPortId())) {
            scatterPort(job, event.getPortId(), variable.getValue(), event.getPosition(), event.getNumberOfScattered(), event.isLookAhead());
            update(job, variable);
            return;
          }
        } else {
          // it's not blocking
          scatterPort(job, event.getPortId(), event.getValue(), event.getPosition(), event.getNumberOfScattered(), event.isLookAhead());
          update(job, variable);
          return;
        }
      } else if (job.isScatterWrapper()) {
        sendValuesToScatteredJobs(job, variable, event);
        update(job, variable);
        return;
      }
    }
    
    if (job.isReady()) {
      ready(job, event);
    }
    
    update(job, variable);
  }
  
  /**
   * Scatters port
   */
  @SuppressWarnings("unchecked")
  private void scatterPort(JobRecord job, String portId, Object value, Integer position, Integer numberOfScatteredFromEvent, boolean isLookAhead) throws EventHandlerException {
    DAGNode node = nodeDB.get(InternalSchemaHelper.normalizeId(job.getId()), job.getContextId());

    if (job.getScatterMapping() == null) {
      job.setScatterMapping(getScatterMapping(node));
    }

    if (isLookAhead) {
      int numberOfScattered = getNumberOfScattered(job, numberOfScatteredFromEvent);
      createScatteredJobs(job, portId, value, node, numberOfScattered, position);
      return;
    }

    List<Object> values = null;
    boolean isPortReady = job.isInputPortReady(portId);
    boolean isPortBlocking = job.getInputPortIncoming(portId) > 1;
    if (value instanceof List<?> && (!isPortBlocking || (isPortBlocking && isPortReady))) {
      values = (List<Object>) value;
    } else {
      values = new ArrayList<>();
      values.add(value);
    }

    for (int i = 0; i < values.size(); i++) {
      createScatteredJobs(job, portId, values.get(i), node, values.size(), isPortBlocking && !isPortReady? position : i + 1);
    }
  }
  
  private void createScatteredJobs(JobRecord job, String port, Object value, DAGNode node, Integer numberOfScattered, Integer position) throws EventHandlerException {
    ScatterMapping scatterMapping = job.getScatterMapping();
    scatterMapping.enable(port, value, position);
    
    List<RowMapping> mappings = null;
    try {
      mappings = scatterMapping.getEnabledRows();
    } catch (BindingException e) {
      throw new EventHandlerException("Failed to enable ScatterMapping for node " + node.getId(), e);
    }
    scatterMapping.commit(mappings);
    
    for (RowMapping mapping : mappings) {
      job.setState(JobState.RUNNING);
      jobService.update(job);

      List<Event> events = new ArrayList<>();

      String jobNId = InternalSchemaHelper.scatterId(job.getId(), mapping.getIndex());
      JobRecord jobN = createJobRecord(jobNId, job.getId(), node, true, job.getContextId());
          
      for (DAGLinkPort inputPort : node.getInputPorts()) {
        VariableRecord variableN = new VariableRecord(job.getContextId(), jobNId, inputPort.getId(), LinkPortType.INPUT, null, node.getLinkMerge(inputPort.getId(), inputPort.getType()));
        variableN.setNumberGlobals(getNumberOfScattered(job, numberOfScattered));
        variableService.create(variableN);

        if (jobN.getState().equals(JobState.PENDING)) {
          jobN.incrementPortCounter(inputPort, LinkPortType.INPUT);
        }
        LinkRecord link = new LinkRecord(job.getContextId(), job.getId(), inputPort.getId(), LinkPortType.INPUT, jobNId, inputPort.getId(), LinkPortType.INPUT, 1);
        linkService.create(link);

        if (inputPort.isScatter()) {
          Event eventInputPort = new InputUpdateEvent(job.getContextId(), jobNId, inputPort.getId(), mapping.getValue(inputPort.getId()), 1);
          events.add(eventInputPort);
        } else {
          if (job.isInputPortReady(inputPort.getId())) {
            VariableRecord variable = variableService.find(job.getId(), inputPort.getId(), LinkPortType.INPUT, job.getContextId());
            events.add(new InputUpdateEvent(job.getContextId(), jobNId, inputPort.getId(), variable.getValue(), 1));
          }
        }
      }
      for (DAGLinkPort outputPort : node.getOutputPorts()) {
        VariableRecord variableN = new VariableRecord(job.getContextId(), jobNId, outputPort.getId(), LinkPortType.OUTPUT, null, node.getLinkMerge(outputPort.getId(), outputPort.getType()));
        variableN.setNumberGlobals(getNumberOfScattered(job, numberOfScattered));
        variableService.create(variableN);
        jobN.incrementPortCounter(outputPort, LinkPortType.OUTPUT);

        LinkRecord link = new LinkRecord(job.getContextId(), jobNId, outputPort.getId(), LinkPortType.OUTPUT, job.getId(), outputPort.getId(), LinkPortType.OUTPUT, null);
        linkService.create(link);
      }

      job.setState(JobState.RUNNING);
      job.setScatterWrapper(true);
      
      job.resetOutputPortCounters(getNumberOfScattered(job, numberOfScattered));
      jobService.update(job);
      
      jobN.setNumberOfGlobalOutputs(getNumberOfScattered(job, numberOfScattered));
      jobService.create(jobN);

      for (Event subevent : events) {
        eventProcessor.send(subevent);
      }
    }
  }
  
  /**
   * Get number of scattered jobs 
   */
  private int getNumberOfScattered(JobRecord job, Integer scatteredNodes) {
    if (scatteredNodes != null) {
      return Math.max(scatteredNodes, job.getScatterMapping().getNumberOfRows());
    } else {
      return job.getScatterMapping().getNumberOfRows();
    }
  }
  
  private ScatterMapping getScatterMapping(DAGNode dagNode) throws EventHandlerException {
    ScatterMethod scatterMethod = dagNode.getScatterMethod();
    if (scatterMethod == null) {
      return new ScatterOneToOneMapping(dagNode);
    }
    switch (scatterMethod) {
    case dotproduct:
      return new ScatterOneToOneMapping(dagNode);
    case flat_crossproduct:
      return new ScatterCartesianMapping(dagNode);
    default:
      throw new EventHandlerException("Scatter method " + scatterMethod + " is not supported.");
    }
  }
  
  private void update(JobRecord job, VariableRecord variable) {
    jobService.update(job);
    variableService.update(variable);
  }
  
  /**
   * Send events from scatter wrapper to scattered jobs
   */
  private void sendValuesToScatteredJobs(JobRecord job, VariableRecord variable, InputUpdateEvent event) throws EventHandlerException {
    List<LinkRecord> links = linkService.findBySourceAndDestinationType(job.getId(), event.getPortId(), LinkPortType.INPUT, event.getContextId());

    List<Event> events = new ArrayList<>();
    for (LinkRecord link : links) {
      VariableRecord destinationVariable = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), LinkPortType.INPUT, event.getContextId());

      Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), variable.getValue(), event.getPosition());
      events.add(updateInputEvent);
    }
    for (Event subevent : events) {
      eventProcessor.send(subevent);
    }
  }

  /**
   * Job is ready
   */
  private void ready(JobRecord job, InputUpdateEvent event) throws EventHandlerException {
    job.setState(JobState.READY);
    
    if (job.isContainer()) {
      job.setState(JobState.RUNNING);

      DAGContainer containerNode;
      if (job.isScattered()) {
        containerNode = (DAGContainer) nodeDB.get(InternalSchemaHelper.getJobIdFromScatteredId(job.getId()), event.getContextId());
      } else {
        containerNode = (DAGContainer) nodeDB.get(job.getId(), event.getContextId());
      }
      rollOutContainer(job, containerNode, event.getContextId());

      List<LinkRecord> containerLinks = linkService.findBySourceAndSourceType(event.getJobId(), LinkPortType.INPUT, event.getContextId());
      for (LinkRecord link : containerLinks) {
        VariableRecord sourceVariable = variableService.find(link.getSourceJobId(), link.getSourceJobPort(), LinkPortType.INPUT, event.getContextId());
        VariableRecord destinationVariable = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), LinkPortType.INPUT, event.getContextId());

        Event updateEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), sourceVariable.getValue(), link.getPosition());
        eventProcessor.send(updateEvent);
      }
    } else if (!job.isScattered() && job.getScatterPorts().size() > 0) {
      job.setState(JobState.RUNNING);
      
      for (String port : job.getScatterPorts()) {
        VariableRecord variable = variableService.find(job.getId(), port, LinkPortType.INPUT, event.getContextId());
        scatterPort(job, port, variable.getValue(), 1, null, false);
      }
    }
    
  }
  
  /**
   * Unwraps {@link DAGContainer}
   */
  private void rollOutContainer(JobRecord job, DAGContainer containerNode, String contextId) {
    for (DAGNode node : containerNode.getChildren()) {
      String newJobId = InternalSchemaHelper.concatenateIds(job.getId(), InternalSchemaHelper.getLastPart(node.getId()));

      JobRecord childJob = createJobRecord(newJobId, job.getExternalId(), node, false, contextId);
      jobService.create(childJob);

      for (DAGLinkPort port : node.getInputPorts()) {
        VariableRecord childVariable = new VariableRecord(contextId, newJobId, port.getId(), LinkPortType.INPUT, null, node.getLinkMerge(port.getId(), port.getType()));
        variableService.create(childVariable);
      }

      for (DAGLinkPort port : node.getOutputPorts()) {
        VariableRecord childVariable = new VariableRecord(contextId, newJobId, port.getId(), LinkPortType.OUTPUT, null, node.getLinkMerge(port.getId(), port.getType()));
        variableService.create(childVariable);
      }
    }
    for (DAGLink link : containerNode.getLinks()) {
      String originalJobID = InternalSchemaHelper.normalizeId(job.getId());

      String sourceNodeId = originalJobID;
      String linkSourceNodeId = link.getSource().getNodeId();
      if (linkSourceNodeId.startsWith(originalJobID)) {
        if (linkSourceNodeId.equals(sourceNodeId)) {
          sourceNodeId = job.getId();
        } else {
          sourceNodeId = InternalSchemaHelper.concatenateIds(job.getId(), InternalSchemaHelper.getLastPart(linkSourceNodeId));
        }
      }
      String destinationNodeId = originalJobID;
      String linkDestinationNodeId = link.getDestination().getNodeId();
      if (linkDestinationNodeId.startsWith(originalJobID)) {
        if (linkDestinationNodeId.equals(destinationNodeId)) {
          destinationNodeId = job.getId();
        } else {
          destinationNodeId = InternalSchemaHelper.concatenateIds(job.getId(), InternalSchemaHelper.getLastPart(linkDestinationNodeId));
        }
      }
      LinkRecord childLink = new LinkRecord(contextId, sourceNodeId, link.getSource().getId(), LinkPortType.valueOf(link.getSource().getType().toString()), destinationNodeId, link.getDestination().getId(), LinkPortType.valueOf(link.getDestination().getType().toString()), link.getPosition());
      linkService.create(childLink);

      handleLinkPort(jobService.find(sourceNodeId, contextId), link.getSource());
      handleLinkPort(jobService.find(destinationNodeId, contextId), link.getDestination());
    }
  }
  
  /**
   * Handle links for roll-out 
   */
  private void handleLinkPort(JobRecord job, DAGLinkPort linkPort) {
    if (linkPort.getType().equals(LinkPortType.INPUT)) {
      if (job.getState().equals(JobState.PENDING)) {
        job.incrementPortCounter(linkPort, LinkPortType.INPUT);
        job.increaseInputPortIncoming(linkPort.getId());
        
        if (job.getInputPortIncoming(linkPort.getId()) > 1) {
          if (LinkMerge.isBlocking(linkPort.getLinkMerge())) {
            job.setBlocking(true);
          }
        }
      }
    } else {
      job.incrementPortCounter(linkPort, LinkPortType.OUTPUT);
      job.increaseOutputPortIncoming(linkPort.getId());
    }
    jobService.update(job);
  }
  
  private JobRecord createJobRecord(String id, String parentId, DAGNode node, boolean isScattered, String contextId) {
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
    return new JobRecord(contextId, id, JobRecordService.generateUniqueId(), parentId, JobState.PENDING, node instanceof DAGContainer, isScattered, false, isBlocking);
  }
}
