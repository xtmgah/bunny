package org.rabix.engine.processor.handler.impl;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.engine.event.Event;
import org.rabix.engine.event.impl.InputUpdateEvent;
import org.rabix.engine.event.impl.JobStatusEvent;
import org.rabix.engine.model.DAGNodeRecord.DAGNodeGraph;
import org.rabix.engine.model.JobRecord;
import org.rabix.engine.model.LinkRecord;
import org.rabix.engine.model.VariableRecord;
import org.rabix.engine.processor.EventProcessor;
import org.rabix.engine.processor.handler.EventHandler;
import org.rabix.engine.processor.handler.EventHandlerException;
import org.rabix.engine.service.DAGNodeGraphService;
import org.rabix.engine.service.JobRecordService;
import org.rabix.engine.service.JobRecordService.JobState;
import org.rabix.engine.service.LinkRecordService;
import org.rabix.engine.service.EngineServiceException;
import org.rabix.engine.service.VariableRecordService;
import org.rabix.engine.service.scatter.ScatterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Handles {@link InputUpdateEvent} events.
 */
public class InputEventHandler implements EventHandler<InputUpdateEvent> {

  private final static Logger logger = LoggerFactory.getLogger(InputEventHandler.class);
  
  private final DAGNodeGraphService dagNodeService;
  private final JobRecordService jobService;
  private final LinkRecordService linkService;
  private final VariableRecordService variableService;
  
  private final ScatterService scatterHelper;
  private final EventProcessor eventProcessor;

  @Inject
  public InputEventHandler(EventProcessor eventProcessor, ScatterService scatterHelper, JobRecordService jobService, VariableRecordService variableService, LinkRecordService linkService, DAGNodeGraphService dagNodeService) {
    this.dagNodeService = dagNodeService;
    this.jobService = jobService;
    this.linkService = linkService;
    this.variableService = variableService;
    
    this.scatterHelper = scatterHelper;
    this.eventProcessor = eventProcessor;
  }
  
  @Override
  @Transactional
  public void handle(InputUpdateEvent event) throws EventHandlerException {
    try {
      JobRecord job = jobService.find(event.getJobId(), event.getContextId());
      VariableRecord variable = variableService.find(event.getJobId(), event.getPortId(), LinkPortType.INPUT, event.getContextId());

      DAGNodeGraph node = dagNodeService.find(InternalSchemaHelper.normalizeId(job.getId()), event.getContextId());

      if (event.isLookAhead()) {
        int numberOfIncomingLinks = jobService.getInputPortIncoming(job, event.getPortId());
        if (job.isBlocking() || (numberOfIncomingLinks > 1)) {
          return; // guard: should not happen
        } else {
          jobService.resetInputPortCounters(job, event.getNumberOfScattered());
        }
      } else {
        boolean isScatter = jobService.isScatterPort(job, event.getPortId());
        int numberOfIncomingLinks = jobService.getInputPortIncoming(job, event.getPortId());
        boolean isBlocking = LinkMerge.isBlocking(node.getLinkMerge(event.getPortId(), LinkPortType.INPUT));

        if (numberOfIncomingLinks > 1 && isScatter && !isBlocking) {
          jobService.resetOutputPortCounters(job, numberOfIncomingLinks);
        }
      }

      variableService.addValue(variable, event.getValue(), event.getPosition());
      jobService.decrementPortCounter(job, event.getPortId(), LinkPortType.INPUT);

      // scatter
      if (!job.isBlocking() && !job.isScattered()) {
        boolean isScatterPort = jobService.isScatterPort(job, event.getPortId());
        boolean isInputPortBlocking = jobService.isInputPortBlocking(job, node, event.getPortId());

        if (isScatterPort) {
          if (isInputPortBlocking) {
            // it's blocking
            boolean isInputPortReady = jobService.isInputPortReady(job, event.getPortId());
            if (isInputPortReady) {
              Object value = variableService.transformValue(variable);
              scatterHelper.scatterPort(job, event.getPortId(), value, event.getPosition(), event.getNumberOfScattered(), event.isLookAhead(), false);
              update(job, variable);
              return;
            }
          } else {
            // it's not blocking
            scatterHelper.scatterPort(job, event.getPortId(), event.getValue(), event.getPosition(), event.getNumberOfScattered(), event.isLookAhead(), true);
            update(job, variable);
            return;
          }
        } else if (job.isScatterWrapper()) {
          sendValuesToScatteredJobs(job, variable, event);
          update(job, variable);
          return;
        }
      }
      update(job, variable);

      boolean isJobReady = jobService.isReady(job);
      if (isJobReady) {
        JobStatusEvent jobStatusEvent = new JobStatusEvent(job.getId(), event.getContextId(), JobState.READY, null);
        eventProcessor.send(jobStatusEvent);
      }
    } catch (EngineServiceException e) {
      logger.error("Failed to handle InputEvent " + event, e);
      throw new EventHandlerException("Failed to handle InputEvent " + event, e);
    }
  }
  
  private void update(JobRecord job, VariableRecord variable) throws EngineServiceException {
    jobService.update(job);
    variableService.update(variable);
  }
  
  /**
   * Send events from scatter wrapper to scattered jobs
   */
  private void sendValuesToScatteredJobs(JobRecord job, VariableRecord variable, InputUpdateEvent event) throws EventHandlerException, EngineServiceException {
    List<LinkRecord> links = linkService.findBySourceAndDestinationType(job.getId(), event.getPortId(), LinkPortType.INPUT, event.getContextId());

    List<Event> events = new ArrayList<>();
    for (LinkRecord link : links) {
      VariableRecord destinationVariable = variableService.find(link.getDestinationJobId(), link.getDestinationJobPort(), LinkPortType.INPUT, event.getContextId());

      Object value = variableService.transformValue(variable);
      Event updateInputEvent = new InputUpdateEvent(event.getContextId(), destinationVariable.getJobId(), destinationVariable.getPortId(), value, event.getPosition());
      events.add(updateInputEvent);
    }
    for (Event subevent : events) {
      eventProcessor.send(subevent);
    }
  }

}
