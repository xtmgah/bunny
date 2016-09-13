package org.rabix.bindings.cwl1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.rabix.bindings.cwl1.bean.CWL1DataLink;
import org.rabix.bindings.cwl1.bean.CWL1InputPort;
import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.bean.CWL1JobApp;
import org.rabix.bindings.cwl1.bean.CWL1OutputPort;
import org.rabix.bindings.cwl1.bean.CWL1Step;
import org.rabix.bindings.cwl1.bean.CWL1Workflow;
import org.rabix.bindings.cwl1.helper.CWL1BindingHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.common.helper.InternalSchemaHelper;
import org.rabix.common.json.processor.BeanProcessor;
import org.rabix.common.json.processor.BeanProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BeanProcessor} used for Job processing. It populates some additional fields.
 */
public class CWL1JobProcessor implements BeanProcessor<CWL1Job> {

  private final static Logger logger = LoggerFactory.getLogger(CWL1JobProcessor.class);
  
  public static final String DOT_SEPARATOR = ".";
  public static final String SLASH_SEPARATOR = "/";
  
  public CWL1Job process(CWL1Job job) throws BeanProcessorException {
    try {
      return process(null, job);
    } catch (CWL1Exception e) {
      logger.error("Failed to process Job.", e);
      throw new BeanProcessorException(e);
    }
  }
  
  private CWL1Job process(CWL1Job parentJob, CWL1Job job) throws CWL1Exception {
    if (job.getId() == null) {
      String workflowId = parentJob != null ? parentJob.getId() : null;
      String id = workflowId != null? workflowId + DOT_SEPARATOR + InternalSchemaHelper.ROOT_NAME : InternalSchemaHelper.ROOT_NAME;
      job.setId(id);
    }
    processElements(null, job);

    if (job.getApp().isWorkflow()) {
      CWL1Workflow workflow = (CWL1Workflow) job.getApp();
      for (CWL1Step step : workflow.getSteps()) {
        step.setId(Draft2ToCWL1Converter.convertStepID(step.getId()));
        
        CWL1Job stepJob = step.getJob();
        String stepId = job.getId() + DOT_SEPARATOR + CWL1SchemaHelper.normalizeId(step.getId());
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
  private void processElements(CWL1Job parentJob, CWL1Job job) throws CWL1Exception {
    CWL1JobApp app = job.getApp();
    for (CWL1InputPort port : app.getInputs()) {
      port.setId(Draft2ToCWL1Converter.convertPortID(port.getId()));
    }
    for (CWL1OutputPort port : app.getOutputs()) {
      port.setId(Draft2ToCWL1Converter.convertPortID(port.getId()));
    }
    if (app.isWorkflow()) {
      CWL1Workflow workflow = (CWL1Workflow) app;
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
  private void createDataLinks(CWL1Workflow workflow) throws CWL1Exception {
    for (CWL1OutputPort port : workflow.getOutputs()) {
      port.setId(Draft2ToCWL1Converter.convertPortID(port.getId()));
      
      List<String> sources = transformSource(port.getSource());
      for (int position = 0; position < sources.size(); position++) {
        String destination = port.getId();
        LinkMerge linkMerge = port.getLinkMerge() != null? LinkMerge.valueOf(port.getLinkMerge()) : LinkMerge.merge_nested;
        
        String source = sources.get(position);
        source = Draft2ToCWL1Converter.convertSource(source);
        source = CWL1SchemaHelper.normalizeId(source);
        CWL1DataLink dataLink = new CWL1DataLink(source, destination, linkMerge, position + 1);
        workflow.addDataLink(dataLink);
      }
    }
    for (CWL1Step step : workflow.getSteps()) {
      step.setId(Draft2ToCWL1Converter.convertStepID(step.getId()));
      
      List<CWL1DataLink> dataLinks = new ArrayList<>();
      for (Map<String, Object> input : step.getInputs()) {
        
        List<String> sources = transformSource(CWL1BindingHelper.getSource(input));
        for (int position = 0; position < sources.size(); position++) {
          String destination = CWL1BindingHelper.getId(input);
          destination = Draft2ToCWL1Converter.convertDestinationId(destination);
          destination = step.getId() + SLASH_SEPARATOR + destination;
          LinkMerge linkMerge = CWL1BindingHelper.getLinkMerge(input) != null ? LinkMerge.valueOf(CWL1BindingHelper.getLinkMerge(input)) : LinkMerge.merge_nested;
          
          String source = sources.get(position);
          source = Draft2ToCWL1Converter.convertSource(source);
          
          source = CWL1SchemaHelper.normalizeId(source);
          CWL1DataLink dataLink = new CWL1DataLink(source, destination, linkMerge, position + 1);
          dataLinks.add(dataLink);
        }
      }
      workflow.addDataLinks(dataLinks);
    }
  }

  @SuppressWarnings("unchecked")
  private List<String> transformSource(Object source) throws CWL1Exception {
    if (source == null) {
      return Collections.<String> emptyList();
    }
    List<String> transformed = new ArrayList<>();
    if (source instanceof String) {
      transformed.add((String) source);
    } else if (source instanceof List<?>) {
      transformed.addAll((List<? extends String>) source);
    } else {
      throw new CWL1Exception("Failed to process source properties. Invalid application structure.");
    }
    return transformed;
  }

  /**
   * Process input or output ports
   */
  private void processPorts(CWL1Job parentJob, CWL1Job job, List<? extends ApplicationPort> ports) throws CWL1Exception {
    for (ApplicationPort port : ports) {
      String prefix = job.getId().substring(job.getId().lastIndexOf(DOT_SEPARATOR) + 1) + SLASH_SEPARATOR;
      setScatter(job, prefix, port);  // if it's a container
      if (parentJob != null) {
        // it it's an embedded container
        setScatter(parentJob, prefix, port);
      }
      
      if (parentJob != null && parentJob.getApp().isWorkflow()) {
        // if it's a container
        CWL1Workflow workflowApp = (CWL1Workflow) parentJob.getApp();
        processDataLinks(workflowApp.getDataLinks(), port, job, true);
      }
      if (job != null && job.getApp().isWorkflow()) {
        CWL1Workflow workflowApp = (CWL1Workflow) job.getApp();
        processDataLinks(workflowApp.getDataLinks(), port, job, false);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private void setScatter(CWL1Job job, String prefix, ApplicationPort port) throws CWL1Exception {
    Object scatterObj = job.getScatter();;
    if (scatterObj != null) {
      List<String> scatterList = new ArrayList<>();
      if (scatterObj instanceof List<?>) {
        for (String scatter : ((List<String>) scatterObj)) {
          scatterList.add(CWL1SchemaHelper.normalizeId(scatter));
        }
      } else if (scatterObj instanceof String) {
        scatterList.add(CWL1SchemaHelper.normalizeId((String) scatterObj));
      } else {
        throw new CWL1Exception("Failed to process scatter properties. Invalid application structure.");
      }

      // TODO fix
      for (String scatterStr : scatterList) {
        if (scatterStr.startsWith(prefix)) {
          if ((prefix + CWL1SchemaHelper.normalizeId(port.getId())).equals(scatterStr)) {
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
  private void processDataLinks(List<CWL1DataLink> dataLinks, ApplicationPort port, CWL1Job job, boolean strip) {
    for (CWL1DataLink dataLink : dataLinks) {
      String source = dataLink.getSource();
      String destination = dataLink.getDestination();
      
      String scatter = null;
      if (job.getId().contains(DOT_SEPARATOR)) {
        String mod = job.getId().substring(job.getId().indexOf(DOT_SEPARATOR) + 1);
        if (strip) {
          mod = mod.substring(mod.indexOf(DOT_SEPARATOR) + 1);
        }
        scatter = mod + SLASH_SEPARATOR + CWL1SchemaHelper.normalizeId(port.getId());
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
