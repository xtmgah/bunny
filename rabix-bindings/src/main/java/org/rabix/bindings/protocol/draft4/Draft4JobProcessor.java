package org.rabix.bindings.protocol.draft4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.protocol.draft4.bean.Draft4DataLink;
import org.rabix.bindings.protocol.draft4.bean.Draft4InputPort;
import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4JobApp;
import org.rabix.bindings.protocol.draft4.bean.Draft4OutputPort;
import org.rabix.bindings.protocol.draft4.bean.Draft4Step;
import org.rabix.bindings.protocol.draft4.bean.Draft4Workflow;
import org.rabix.bindings.protocol.draft4.helper.Draft4BindingHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4SchemaHelper;
import org.rabix.common.json.processor.BeanProcessor;
import org.rabix.common.json.processor.BeanProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BeanProcessor} used for Job processing. It populates some additional fields.
 */
public class Draft4JobProcessor implements BeanProcessor<Draft4Job> {

  private final static Logger logger = LoggerFactory.getLogger(Draft4JobProcessor.class);
  
  public static final String DOT_SEPARATOR = ".";
  public static final String SLASH_SEPARATOR = "/";
  
  public Draft4Job process(Draft4Job job) throws BeanProcessorException {
    try {
      return process(null, job);
    } catch (Draft4Exception e) {
      logger.error("Failed to process Job.", e);
      throw new BeanProcessorException(e);
    }
  }
  
  private Draft4Job process(Draft4Job parentJob, Draft4Job job) throws Draft4Exception {
    if (job.getId() == null) {
      String workflowId = parentJob != null ? parentJob.getId() : null;
      String id = workflowId != null? workflowId + DOT_SEPARATOR + Draft4SchemaHelper.MASTER_JOB_ID : Draft4SchemaHelper.MASTER_JOB_ID;
      job.setId(id);
    }
    processElements(null, job);

    if (job.getApp().isWorkflow()) {
      Draft4Workflow workflow = (Draft4Workflow) job.getApp();
      for (Draft4Step step : workflow.getSteps()) {
        step.setId(Draft2ToDraft4Converter.convertStepID(step.getId()));
        
        Draft4Job stepJob = step.getJob();
        String stepId = job.getId() + DOT_SEPARATOR + Draft4SchemaHelper.normalizeId(step.getId());
        stepJob.setId(stepId);
        processElements(job, stepJob);
        process(job, stepJob);
      }
    }
    return job;
  }
  
  /**
   * Process Job inputs, outputs and data links
   */
  private void processElements(Draft4Job parentJob, Draft4Job job) throws Draft4Exception {
    Draft4JobApp app = job.getApp();
    for (Draft4InputPort port : app.getInputs()) {
      port.setId(Draft2ToDraft4Converter.convertPortID(port.getId()));
    }
    for (Draft4OutputPort port : app.getOutputs()) {
      port.setId(Draft2ToDraft4Converter.convertPortID(port.getId()));
    }
    if (app.isWorkflow()) {
      Draft4Workflow workflow = (Draft4Workflow) app;
      if (CollectionUtils.isEmpty(workflow.getDataLinks())) {
        createDataLinks(workflow);
      }
    }
    processPorts(parentJob, job, app.getInputs());
    processPorts(parentJob, job, app.getOutputs());
  }

  /**
   * Created data links from source properties
   */
  private void createDataLinks(Draft4Workflow workflow) throws Draft4Exception {
    for (Draft4OutputPort port : workflow.getOutputs()) {
      port.setId(Draft2ToDraft4Converter.convertPortID(port.getId()));
      
      List<String> sources = transformSource(port.getSource());
      for (int position = 0; position < sources.size(); position++) {
        String destination = port.getId();
        LinkMerge linkMerge = port.getLinkMerge() != null? LinkMerge.valueOf(port.getLinkMerge()) : LinkMerge.merge_nested;
        
        String source = sources.get(position);
        source = Draft2ToDraft4Converter.convertSource(source);
        source = Draft4SchemaHelper.normalizeId(source);
        Draft4DataLink dataLink = new Draft4DataLink(source, destination, linkMerge, position + 1);
        workflow.addDataLink(dataLink);
      }
    }
    for (Draft4Step step : workflow.getSteps()) {
      step.setId(Draft2ToDraft4Converter.convertStepID(step.getId()));
      
      List<Draft4DataLink> dataLinks = new ArrayList<>();
      for (Map<String, Object> input : step.getInputs()) {
        
        List<String> sources = transformSource(Draft4BindingHelper.getSource(input));
        for (int position = 0; position < sources.size(); position++) {
          String destination = Draft4BindingHelper.getId(input);
          destination = Draft2ToDraft4Converter.convertDestinationId(destination);
          destination = step.getId() + SLASH_SEPARATOR + destination;
          LinkMerge linkMerge = Draft4BindingHelper.getLinkMerge(input) != null ? LinkMerge.valueOf(Draft4BindingHelper.getLinkMerge(input)) : LinkMerge.merge_nested;
          
          String source = sources.get(position);
          source = Draft2ToDraft4Converter.convertSource(source);
          
          source = Draft4SchemaHelper.normalizeId(source);
          Draft4DataLink dataLink = new Draft4DataLink(source, destination, linkMerge, position + 1);
          dataLinks.add(dataLink);
        }
      }
      workflow.addDataLinks(dataLinks);
    }
  }

  @SuppressWarnings("unchecked")
  private List<String> transformSource(Object source) throws Draft4Exception {
    if (source == null) {
      return Collections.<String> emptyList();
    }
    List<String> transformed = new ArrayList<>();
    if (source instanceof String) {
      transformed.add((String) source);
    } else if (source instanceof List<?>) {
      transformed.addAll((List<? extends String>) source);
    } else {
      throw new Draft4Exception("Failed to process source properties. Invalid application structure.");
    }
    return transformed;
  }

  /**
   * Process input or output ports
   */
  private void processPorts(Draft4Job parentJob, Draft4Job job, List<? extends ApplicationPort> ports) throws Draft4Exception {
    for (ApplicationPort port : ports) {
      String prefix = job.getId().substring(job.getId().lastIndexOf(DOT_SEPARATOR) + 1) + SLASH_SEPARATOR;
      setScatter(job, prefix, port);  // if it's a container
      if (parentJob != null) {
        // it it's an embedded container
        setScatter(parentJob, prefix, port);
      }
      
      if (parentJob != null && parentJob.getApp().isWorkflow()) {
        // if it's a container
        Draft4Workflow workflowApp = (Draft4Workflow) parentJob.getApp();
        processDataLinks(workflowApp.getDataLinks(), port, job, true);
      }
      if (job != null && job.getApp().isWorkflow()) {
        Draft4Workflow workflowApp = (Draft4Workflow) job.getApp();
        processDataLinks(workflowApp.getDataLinks(), port, job, false);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private void setScatter(Draft4Job job, String prefix, ApplicationPort port) throws Draft4Exception {
    Object scatterObj = job.getScatter();;
    if (scatterObj != null) {
      List<String> scatterList = new ArrayList<>();
      if (scatterObj instanceof List<?>) {
        for (String scatter : ((List<String>) scatterObj)) {
          scatterList.add(Draft4SchemaHelper.normalizeId(scatter));
        }
      } else if (scatterObj instanceof String) {
        scatterList.add(Draft4SchemaHelper.normalizeId((String) scatterObj));
      } else {
        throw new Draft4Exception("Failed to process scatter properties. Invalid application structure.");
      }

      // TODO fix
      for (String scatterStr : scatterList) {
        if (scatterStr.startsWith(prefix)) {
          if ((prefix + Draft4SchemaHelper.normalizeId(port.getId())).equals(scatterStr)) {
            if (!(port.getScatter() != null && port.getScatter())) {
              port.setScatter(true);              
            }
            break;
          }
        }
      }
    }
  }

  /**
   * Process data links
   */
  private void processDataLinks(List<Draft4DataLink> dataLinks, ApplicationPort port, Draft4Job job, boolean strip) {
    for (Draft4DataLink dataLink : dataLinks) {
      String source = dataLink.getSource();
      String destination = dataLink.getDestination();
      
      String scatter = null;
      if (job.getId().contains(DOT_SEPARATOR)) {
        String mod = job.getId().substring(job.getId().indexOf(DOT_SEPARATOR) + 1);
        if (strip) {
          mod = mod.substring(mod.indexOf(DOT_SEPARATOR) + 1);
        }
        scatter = mod + SLASH_SEPARATOR + Draft4SchemaHelper.normalizeId(port.getId());
      } else {
        scatter = port.getId();
      }
      
      // TODO fix
      if ((source.equals(scatter) || destination.equals(scatter)) && (dataLink.getScattered() == null || !dataLink.getScattered())) {
        dataLink.setScattered(port.getScatter());
      }
    }
  }
}
