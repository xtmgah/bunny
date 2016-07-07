package org.rabix.bindings.protocol.draft4.processor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.protocol.draft4.bean.Draft4InputPort;
import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4OutputPort;
import org.rabix.bindings.protocol.draft4.helper.Draft4SchemaHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Draft4PortProcessor {

  private final static Logger logger = LoggerFactory.getLogger(Draft4PortProcessor.class);
  
  private Draft4Job job;
  
  public Draft4PortProcessor(Draft4Job job) {
    this.job = job;
  }
  
  /**
   * Process inputs and return
   */
  public Map<String, Object> processInputs(Map<String, Object> inputs, Draft4PortProcessorCallback portProcessor) throws Draft4PortProcessorException {
    return processValues(inputs, job.getApp().getInputs(), Draft4InputPort.class, portProcessor);
  }

  /**
   * Process outputs and return 
   */
  public Map<String, Object> processOutputs(Map<String, Object> outputs, Draft4PortProcessorCallback portProcessor) throws Draft4PortProcessorException {
    return processValues(outputs, job.getApp().getOutputs(), Draft4OutputPort.class, portProcessor);
  }

  private Map<String, Object> processValues(Map<String, Object> values, List<? extends ApplicationPort> ports, Class<? extends ApplicationPort> clazz, Draft4PortProcessorCallback portProcessor) throws Draft4PortProcessorException {
    if (values == null) {
      return null;
    }
    Map<String, Object> mappedValues = new HashMap<>();
    for (Entry<String, Object> entry : values.entrySet()) {
      String id = entry.getKey();
      Object value = entry.getValue();

      ApplicationPort port = job.getApp().getPort(Draft4SchemaHelper.normalizeId(id), clazz);
      if (port == null) {
        throw new Draft4PortProcessorException("Port with ID=" + Draft4SchemaHelper.normalizeId(id) + " doesn't exist.");
      }
      Object mappedValue = null;
      try {
        mappedValue = processValue(value, port, port.getSchema(), Draft4SchemaHelper.normalizeId(id), portProcessor);
      } catch (Exception e) {
        throw new Draft4PortProcessorException("Failed to process value " + value, e);
      }
      if (mappedValue != null) {
        mappedValues.put(entry.getKey(), mappedValue);
      }
    }
    return mappedValues;
  }

  @SuppressWarnings("unchecked")
  private Object processValue(Object value, ApplicationPort port, Object schema, String key, Draft4PortProcessorCallback portProcessor) throws Exception {
    logger.debug("Process value {} and schema {}", value, schema);

    if (value == null) {
      return null;
    }

    Draft4PortProcessorResult portProcessorResult = portProcessor.process(value, port);
    if (portProcessorResult.isProcessed()) {
      return portProcessorResult.getValue();
    }
    
    if (Draft4SchemaHelper.isAnyFromSchema(schema)) {
      return value;
    }
    
    if (Draft4SchemaHelper.isFileFromValue(value)) {
      return value;
    }

    if (value instanceof Map<?, ?>) {
      Map<String, Object> result = new HashMap<>();

      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        Map<?, ?> field = Draft4SchemaHelper.getField(entry.getKey(), Draft4SchemaHelper.getSchemaForRecordField(job.getApp().getSchemaDefs(), schema));

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
        Object arrayItemSchema = Draft4SchemaHelper.getSchemaForArrayItem(job.getApp().getSchemaDefs(), schema);
        Object singleResult = processValue(item, port, arrayItemSchema, key, portProcessor);
        result.add(singleResult);
      }
      return result;
    }
    return value;
  }
}