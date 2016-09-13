package org.rabix.bindings.cwl1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolCommandLineBuilder;
import org.rabix.bindings.cwl1.bean.CWL1CommandLineTool;
import org.rabix.bindings.cwl1.bean.CWL1InputPort;
import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionException;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionResolver;
import org.rabix.bindings.cwl1.helper.CWL1BindingHelper;
import org.rabix.bindings.cwl1.helper.CWL1FileValueHelper;
import org.rabix.bindings.cwl1.helper.CWL1JobHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

public class CWL1CommandLineBuilder implements ProtocolCommandLineBuilder {

  public static final String PART_SEPARATOR = "\u0020";

  private final static Logger logger = LoggerFactory.getLogger(CWL1CommandLineBuilder.class);
  
  public static final Escaper SHELL_ESCAPE;
  static {
      final Escapers.Builder builder = Escapers.builder();
      builder.addEscape('\'', "'\"'\"'");
      SHELL_ESCAPE = builder.build();
  }
  
  @Override
  public String buildCommandLine(Job job) throws BindingException {
    CWL1Job draft2Job = CWL1JobHelper.getCWL1Job(job);
    if (draft2Job.getApp().isExpressionTool()) {
      return null;
    }
    return buildCommandLine(draft2Job);
  }
  
  @Override
  public List<String> buildCommandLineParts(Job job) throws BindingException {
    CWL1Job draft2Job = CWL1JobHelper.getCWL1Job(job);
    if (!draft2Job.getApp().isCommandLineTool()) {
      return null;
    }
    return Lists.transform(buildCommandLineParts(draft2Job), new Function<Object, String>() {
      public String apply(Object obj) {
        return obj.toString();
      }
    });
  }
  
  /**
   * Builds command line string with both STDIN and STDOUT
   */
  public String buildCommandLine(CWL1Job job) throws BindingException {
    CWL1CommandLineTool commandLineTool = (CWL1CommandLineTool) job.getApp();
    
    List<Object> commandLineParts = buildCommandLineParts(job);
    StringBuilder builder = new StringBuilder();
    for (Object commandLinePart : commandLineParts) {
      builder.append(commandLinePart).append(PART_SEPARATOR);
    }

    String stdin = null;
    try {
      stdin = commandLineTool.getStdin(job);
    } catch (CWL1ExpressionException e) {
      logger.error("Failed to extract standard input.", e);
      throw new BindingException("Failed to extract standard input.", e);
    }
    if (!StringUtils.isEmpty(stdin)) {
      builder.append(PART_SEPARATOR).append("<").append(PART_SEPARATOR).append(stdin);
    }

    String stdout = null;
    try {
      stdout = commandLineTool.getStdout(job);
    } catch (CWL1ExpressionException e) {
      logger.error("Failed to extract standard output.", e);
      throw new BindingException("Failed to extract standard outputs.", e);
    }
    if (!StringUtils.isEmpty(stdout)) {
      builder.append(PART_SEPARATOR).append(">").append(PART_SEPARATOR).append(stdout);
    }

    String commandLine = normalizeCommandLine(builder.toString());
    logger.info("Command line built. CommandLine = {}", commandLine);
    return commandLine;
  }

  /**
   * Normalize command line (remove multiple spaces, etc.)
   */
  private String normalizeCommandLine(String commandLine) {
    return commandLine.trim().replaceAll(PART_SEPARATOR + "+", PART_SEPARATOR);
  }

  /**
   * Build command line arguments
   */
  public List<Object> buildCommandLineParts(CWL1Job job) throws BindingException {
    logger.info("Building command line parts...");

    CWL1CommandLineTool commandLineTool = (CWL1CommandLineTool) job.getApp();
    List<CWL1InputPort> inputPorts = commandLineTool.getInputs();
    List<Object> result = new LinkedList<>();

    try {
      List<Object> baseCmds = commandLineTool.getBaseCmd(job);
      result.addAll(baseCmds);

      List<CWL1CommandLinePart> commandLineParts = new LinkedList<>();

      if (commandLineTool.hasArguments()) {
        for (int i = 0; i < commandLineTool.getArguments().size(); i++) {
          Object argBinding = commandLineTool.getArguments().get(i);
          if (argBinding instanceof String) {
            CWL1CommandLinePart commandLinePart = new CWL1CommandLinePart.Builder(0, false).part(argBinding).keyValue("").build();
            commandLinePart.setArgsArrayOrder(i);
            commandLineParts.add(commandLinePart);
            continue;
          }
          Object argValue = commandLineTool.getArgument(job, argBinding);
          Map<String, Object> emptySchema = new HashMap<>();
          CWL1CommandLinePart commandLinePart = buildCommandLinePart(job, null, argBinding, argValue, emptySchema, null);
          if (commandLinePart != null) {
            commandLinePart.setArgsArrayOrder(i);
            commandLineParts.add(commandLinePart);
          }
        }
      }

      for (CWL1InputPort inputPort : inputPorts) {
        String key = inputPort.getId();
        Object schema = inputPort.getSchema();

        CWL1CommandLinePart part = buildCommandLinePart(job, inputPort, inputPort.getInputBinding(), job.getInputs().get(CWL1SchemaHelper.normalizeId(key)), schema, key);
        if (part != null) {
          commandLineParts.add(part);
        }
      }
      Collections.sort(commandLineParts, new CWL1CommandLinePart.CommandLinePartComparator());

      for (CWL1CommandLinePart part : commandLineParts) {
        List<Object> flattenedObjects = part.flatten();
        for (Object obj : flattenedObjects) {
          result.add(obj);
        }
      }
    } catch (CWL1ExpressionException e) {
      logger.error("Failed to build command line.", e);
      throw new BindingException("Failed to build command line.", e);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private CWL1CommandLinePart buildCommandLinePart(CWL1Job job, CWL1InputPort inputPort, Object inputBinding, Object value, Object schema, String key) throws BindingException {
    logger.debug("Building command line part for value {} and schema {}", value, schema);

    CWL1CommandLineTool commandLineTool = (CWL1CommandLineTool) job.getApp();
    
    if (inputBinding == null) {
      return null;
    }

    int position = CWL1BindingHelper.getPosition(inputBinding);
    String separator = CWL1BindingHelper.getSeparator(inputBinding);
    String prefix = CWL1BindingHelper.getPrefix(inputBinding);
    String itemSeparator = CWL1BindingHelper.getItemSeparator(inputBinding);
    String keyValue = inputPort != null ? inputPort.getId() : "";

    Object valueFrom = CWL1BindingHelper.getValueFrom(inputBinding);
    if (valueFrom != null) {
      try {
        value = CWL1ExpressionResolver.resolve(valueFrom, job, null);
      } catch (CWL1ExpressionException e) {
        throw new BindingException(e);
      }
    }

    boolean isFile = CWL1SchemaHelper.isFileFromValue(value);
    if (isFile) {
      value = CWL1FileValueHelper.getPath(value);
    }

    if (value == null) {
      return null;
    }

    if (value instanceof Boolean) {
      if (((Boolean) value)) {
        if (prefix == null) {
          throw new BindingException("Missing prefix for " + inputPort.getId() + " input.");
        }
        return new CWL1CommandLinePart.Builder(position, isFile).part(prefix).keyValue(keyValue).build();
      } else {
        return null;
      }
    }

    if (value instanceof Map<?, ?>) {
      CWL1CommandLinePart.Builder commandLinePartBuilder = new CWL1CommandLinePart.Builder(position, isFile);
      commandLinePartBuilder.keyValue(keyValue);
      
      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        String fieldKey = entry.getKey();
        Object fieldValue = entry.getValue();

        Object field = CWL1SchemaHelper.getField(fieldKey, CWL1SchemaHelper.getSchemaForRecordField(job.getApp().getSchemaDefs(), schema));
        if (field == null) {
          logger.info("Field {} not found in schema {}", fieldKey, schema);
          continue;
        }

        Object fieldBinding = CWL1SchemaHelper.getInputBinding(field);
        Object fieldType = CWL1SchemaHelper.getType(field);
        Object fieldSchema = CWL1SchemaHelper.findSchema(commandLineTool.getSchemaDefs(), fieldType);

        CWL1CommandLinePart fieldCommandLinePart = buildCommandLinePart(job, inputPort, fieldBinding, fieldValue, fieldSchema, fieldKey);

        if (fieldCommandLinePart != null) {
          fieldCommandLinePart.setKeyValue(fieldKey);
          commandLinePartBuilder.part(fieldCommandLinePart);
        }
      }
      return commandLinePartBuilder.build().sort();
    }

    if (value instanceof List<?>) {
      CWL1CommandLinePart.Builder commandLinePartBuilder = new CWL1CommandLinePart.Builder(position, isFile);
      commandLinePartBuilder.keyValue(keyValue);
      
      for (Object item : ((List<?>) value)) {
        Object arrayItemSchema = CWL1SchemaHelper.getSchemaForArrayItem(commandLineTool.getSchemaDefs(), schema);
        Object arrayItemInputBinding = new HashMap<>();
        if (schema != null && CWL1SchemaHelper.getInputBinding(schema) != null) {
          arrayItemInputBinding = (Map<String, Object>) CWL1SchemaHelper.getInputBinding(schema);
        }
        
        CWL1CommandLinePart subpart = buildCommandLinePart(job, inputPort, arrayItemInputBinding, item, arrayItemSchema, key);
        if (subpart != null) {
          commandLinePartBuilder.part(subpart);
        }
      }

      CWL1CommandLinePart commandLinePart = commandLinePartBuilder.build();

      List<Object> flattenedValues = commandLinePart.flatten();

      if (itemSeparator != null) {
        String joinedItems = Joiner.on(itemSeparator).join(flattenedValues);
        if (prefix == null) {
          return new CWL1CommandLinePart.Builder(position, isFile).part(joinedItems).build();
        }
        if (StringUtils.isWhitespace(separator)) {
          return new CWL1CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix).part(joinedItems).build();
        } else {
          return new CWL1CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix + separator + joinedItems).build();
        }
      }
      if (prefix == null) {
        return new CWL1CommandLinePart.Builder(position, isFile).keyValue(keyValue).parts(flattenedValues).build();
      }
      List<Object> prefixedValues = new ArrayList<>();
      for (Object arrayItem : flattenedValues) {
        prefixedValues.add(prefix + separator + arrayItem);
      }
      return new CWL1CommandLinePart.Builder(position, isFile).keyValue(keyValue).parts(prefixedValues).build();
    }

    if (prefix == null) {
      return new CWL1CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(value).build();
    }
    if (CWL1BindingHelper.DEFAULT_SEPARATOR.equals(separator)) {
      return new CWL1CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix).part(value).build();
    }
    return new CWL1CommandLinePart.Builder(position, isFile).keyValue(keyValue).part(prefix + separator + value).build();
  }

}