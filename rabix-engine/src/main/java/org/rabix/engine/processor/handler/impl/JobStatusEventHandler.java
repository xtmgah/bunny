package org.rabix.engine.processor.handler.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGLink;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.ContextStatusEvent;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.event.impl.OutputUpdateEvent;
import org.rabix.engine.model.ContextRecord.ContextStatus;
import org.rabix.engine.model.DAGNodeRecord.DAGNodeGraph;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.JobRecord.PortCounter;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.DAGNodeService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.scatter.ScatterService;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class JobStatusEventHandler implements EventHandler<JobStatusEvent> {

  private final DAGNodeService dagNodeService;
  private final ScatterService scatterHelper;
  private final EventProcessor eventProcessor;
  
  private final JobRecordService jobRecordService;
  private final LinkRecordService linkRecordService;
  private final VariableRecordService variableRecordService;

  @Inject
  public JobStatusEventHandler(final DAGNodeService dagNodeService, final JobRecordService jobRecordService, final LinkRecordService linkRecordService, final VariableRecordService variableRecordService, final EventProcessor eventProcessor, final ScatterService scatterHelper) {
    this.dagNodeService = dagNodeService;
    this.scatterHelper = scatterHelper;
    this.eventProcessor = eventProcessor;
    
    this.jobRecordService = jobRecordService;
    this.linkRecordService = linkRecordService;
    this.variableRecordService = variableRecordService;
  }

  @Override
  @Transactional
  public void handle(JobStatusEvent event) throws EventHandlerException {
    JobRecord jobRecord = jobRecordService.find(event.getJobId(), event.getContextId());

    switch (event.getState()) {
    case READY:
      jobRecord.setState(JobState.READY);
      ready(jobRecord, event.getContextId());
      jobRecordService.update(jobRecord);
      break;
    case RUNNING:
      jobRecord.setState(JobState.RUNNING);
      jobRecordService.update(jobRecord);
      break;
    case COMPLETED:
      for (PortCounter portCounter : jobRecord.getOutputCounters()) {
        Object output = event.getResult().get(portCounter.getPort());
        eventProcessor.addToQueue(new OutputUpdateEvent(jobRecord.getRootId(), jobRecord.getId(), portCounter.getPort(), output, 1));
      }
      break;
    case FAILED:
      jobRecord.setState(JobState.FAILED);
      jobRecordService.update(jobRecord);
      eventProcessor.addToQueue(new ContextStatusEvent(event.getContextId(), ContextStatus.FAILED));
      break;
    default:
      break;
    }
  }
  
  /**
   * Job is ready
   */
  public void ready(JobRecord job, String contextId) throws EventHandlerException {
    job.setState(JobState.READY);
    
    if (job.isContainer()) {
      job.setState(JobState.RUNNING);

      DAGNodeGraph containerNode;
      if (job.isScattered()) {
        containerNode = dagNodeService.get(InternalSchemaHelper.getJobIdFromScatteredId(job.getId()), contextId);
      } else {
        containerNode = dagNodeService.get(job.getId(), contextId);
      }
      rollOutContainer(job, containerNode, contextId);

      List<LinkRecord> containerLinks = linkRecordService.findBySourceAndSourceType(job.getId(), LinkPortType.INPUT, contextId);
      if (containerLinks.isEmpty()) {
        Set<String> immediateReadyNodeIds = findImmediateReadyNodes(containerNode);
        for (String readyNodeId : immediateReadyNodeIds) {
          JobRecord childJobRecord = jobRecordService.find(readyNodeId, contextId);
          ready(childJobRecord, contextId);
        }
      } else {
        for (LinkRecord link : containerLinks) {
          VariableRecord sourceVariable = variableRecordService.find(link.getSourceJobId(), link.getSourceJobPort(), LinkPortType.INPUT, contextId);
          VariableRecord destinationVariable = variableRecordService.find(link.getDestinationJobId(), link.getDestinationJobPort(), LinkPortType.INPUT, contextId);
          
          Object value = variableRecordService.transformValue(sourceVariable);
          Event updateEvent = new InputUpdateEvent(contextId, destinationVariable.getJobId(), destinationVariable.getPortId(), value, link.getPosition());
          eventProcessor.send(updateEvent);
        }
      }
    } else {
      List<String> scatterPortIds = jobRecordService.getScatterPorts(job);

      if (!job.isScattered() && scatterPortIds.size() > 0) {
        job.setState(JobState.RUNNING);

        for (String port : scatterPortIds) {
          VariableRecord variable = variableRecordService.find(job.getId(), port, LinkPortType.INPUT, contextId);
          Object value = variableRecordService.transformValue(variable);
          scatterHelper.scatterPort(job, port, value, 1, null, false, false);
        }
      }
    }
  }
  
  private Set<String> findImmediateReadyNodes(DAGNodeGraph node) {
    if (node.isContainer()) {
      Set<String> nodesWithoutDestination = new HashSet<>();
      for (DAGNodeGraph child : node.getChildren()) {
        nodesWithoutDestination.add(child.getId());
      }
      
      for (DAGLink link : node.getLinks()) {
        nodesWithoutDestination.remove(link.getDestination().getNodeId());
      }
      return nodesWithoutDestination;
    }
    return Collections.<String>emptySet();
  }
  
  /**
   * Unwraps {@link DAGContainer}
   */
  private void rollOutContainer(JobRecord job, DAGNodeGraph containerNode, String contextId) {
    for (DAGNodeGraph node : containerNode.getChildren()) {
      String newJobId = InternalSchemaHelper.concatenateIds(job.getId(), InternalSchemaHelper.getLastPart(node.getId()));

      JobRecord childJob = scatterHelper.createJobRecord(newJobId, job.getExternalId(), node, false, contextId);
      jobRecordService.create(childJob);

      for (DAGLinkPort port : node.getInputPorts()) {
        Object defaultValue = node.getDefaults().get(port.getId());
        VariableRecord childVariable = new VariableRecord(contextId, newJobId, port.getId(), LinkPortType.INPUT, defaultValue, node.getLinkMerge(port.getId(), port.getType()));
        variableRecordService.create(childVariable);
      }

      for (DAGLinkPort port : node.getOutputPorts()) {
        VariableRecord childVariable = new VariableRecord(contextId, newJobId, port.getId(), LinkPortType.OUTPUT, null, node.getLinkMerge(port.getId(), port.getType()));
        variableRecordService.create(childVariable);
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
      linkRecordService.create(childLink);

      handleLinkPort(jobRecordService.find(sourceNodeId, contextId), link.getSource());
      handleLinkPort(jobRecordService.find(destinationNodeId, contextId), link.getDestination());
    }
  }
  
  /**
   * Handle links for roll-out 
   */
  private void handleLinkPort(JobRecord job, DAGLinkPort linkPort) {
    if (linkPort.getType().equals(LinkPortType.INPUT)) {
      if (job.getState().equals(JobState.PENDING)) {
        jobRecordService.incrementPortCounter(job, linkPort, LinkPortType.INPUT);
        jobRecordService.increaseInputPortIncoming(job, linkPort.getId());
        
        int numberOfIncomingLinks = jobRecordService.getInputPortIncoming(job, linkPort.getId());
        if (numberOfIncomingLinks > 1) {
          if (LinkMerge.isBlocking(linkPort.getLinkMerge())) {
            job.setBlocking(true);
          }
        }
      }
    } else {
      jobRecordService.incrementPortCounter(job, linkPort, LinkPortType.OUTPUT);
      jobRecordService.increaseOutputPortIncoming(job, linkPort.getId());
    }
    jobRecordService.update(job);
  }
  
}
