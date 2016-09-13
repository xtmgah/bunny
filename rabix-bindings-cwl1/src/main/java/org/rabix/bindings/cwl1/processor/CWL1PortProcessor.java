package org.rabix.bindings.cwl1.processor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.cwl1.bean.CWL1InputPort;
import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.bean.CWL1OutputPort;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.model.ApplicationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CWL1PortProcessor {

  private final static Logger logger = LoggerFactory.getLogger(CWL1PortProcessor.class);
  
  private CWL1Job job;
  
  public CWL1PortProcessor(CWL1Job job) {
    this.job = job;
  }
  
  /**
   * Process inputs and return
   */
  public Map<String, Object> processInputs(Map<String, Object> inputs, CWL1PortProcessorCallback portProcessor) throws CWL1PortProcessorException {
    return processValues(inputs, job.getApp().getInputs(), CWL1InputPort.class, portProcessor);
  }

  /**
   * Process outputs and return 
   */
  public Map<String, Object> processOutputs(Map<String, Object> outputs, CWL1PortProcessorCallback portProcessor) throws CWL1PortProcessorException {
    return processValues(outputs, job.getApp().getOutputs(), CWL1OutputPort.class, portProcessor);
  }

  private Map<String, Object> processValues(Map<String, Object> values, List<? extends ApplicationPort> ports, Class<? extends ApplicationPort> clazz, CWL1PortProcessorCallback portProcessor) throws CWL1PortProcessorException {
    if (values == null) {
      return null;
    }
    Map<String, Object> mappedValues = new HashMap<>();
    for (Entry<String, Object> entry : values.entrySet()) {
      String id = entry.getKey();
      Object value = entry.getValue();

      ApplicationPort port = job.getApp().getPort(CWL1SchemaHelper.normalizeId(id), clazz);
      if (port == null) {
        throw new CWL1PortProcessorException("Port with ID=" + CWL1SchemaHelper.normalizeId(id) + " doesn't exist.");
      }
      Object mappedValue = null;
      try {
        mappedValue = processValue(value, port, port.getSchema(), CWL1SchemaHelper.normalizeId(id), portProcessor);
      } catch (Exception e) {
        throw new CWL1PortProcessorException("Failed to process value " + value, e);
      }
      if (mappedValue != null) {
        mappedValues.put(entry.getKey(), mappedValue);
      }
    }
    return mappedValues;
  }

  @SuppressWarnings("unchecked")
  private Object processValue(Object value, ApplicationPort port, Object schema, String key, CWL1PortProcessorCallback portProcessor) throws Exception {
    logger.debug("Process value {} and schema {}", value, schema);

    if (value == null) {
      return null;
    }

    CWL1PortProcessorResult portProcessorResult = portProcessor.process(value, port);
    if (portProcessorResult.isProcessed()) {
      return portProcessorResult.getValue();
    }
    
    if (CWL1SchemaHelper.isAnyFromSchema(schema)) {
      return value;
    }
    
    if (CWL1SchemaHelper.isFileFromValue(value)) {
      return value;
    }

    if (value instanceof Map<?, ?>) {
      Map<String, Object> result = new HashMap<>();

      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        Map<?, ?> field = CWL1SchemaHelper.getField(entry.getKey(), CWL1SchemaHelper.getSchemaForRecordField(job.getApp().getSchemaDefs(), schema));

        if (field == null) {
          logger.info("Field {} not found in schema {}", entry.getKey(), schema);
          continue;
        }

        Object singleResult = processValue(entry.getValue(), port, schema, entry.getKey(), portProcessor);
        result.put(entry.getKey(), singleResult);
      }
      return result;
    }

    if (value instanceof List<?>) {
      List<Object> result = new LinkedList<>();

      for (Object item : ((List<?>) value)) {
        Object arrayItemSchema = CWL1SchemaHelper.getSchemaForArrayItem(job.getApp().getSchemaDefs(), schema);
        Object singleResult = processValue(item, port, arrayItemSchema, key, portProcessor);
        result.add(singleResult);
      }
      return result;
    }
    return value;
  }
}