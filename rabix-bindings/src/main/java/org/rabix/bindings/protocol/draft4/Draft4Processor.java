package org.rabix.bindings.protocol.draft4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolProcessor;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4CommandLineTool;
import org.rabix.bindings.protocol.draft4.bean.Draft4ExpressionTool;
import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4JobApp;
import org.rabix.bindings.protocol.draft4.bean.Draft4OutputPort;
import org.rabix.bindings.protocol.draft4.expression.Draft4ExpressionException;
import org.rabix.bindings.protocol.draft4.expression.Draft4ExpressionResolver;
import org.rabix.bindings.protocol.draft4.expression.javascript.Draft4ExpressionJavascriptResolver;
import org.rabix.bindings.protocol.draft4.helper.Draft4BindingHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4FileValueHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4JobHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4SchemaHelper;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorException;
import org.rabix.bindings.protocol.draft4.processor.callback.Draft4PortProcessorHelper;
import org.rabix.bindings.protocol.draft4.service.Draft4GlobException;
import org.rabix.bindings.protocol.draft4.service.Draft4GlobService;
import org.rabix.bindings.protocol.draft4.service.Draft4MetadataService;
import org.rabix.bindings.protocol.draft4.service.impl.Draft4GlobServiceImpl;
import org.rabix.bindings.protocol.draft4.service.impl.Draft4MetadataServiceImpl;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Draft4Processor implements ProtocolProcessor {

  public final static int DEFAULT_SUCCESS_CODE = 0;
  
  private final static String JOB_FILE = "job.json";
  private final static String resultFilename = "cwl.output.json";
  
  private final static Logger logger = LoggerFactory.getLogger(Draft4Processor.class);

  private final Draft4GlobService globService;
  private final Draft4MetadataService metadataService;

  public Draft4Processor() {
    this.globService = new Draft4GlobServiceImpl();
    this.metadataService = new Draft4MetadataServiceImpl();
  }

  @Override
  public Job preprocess(final Job job, final File workingDir) throws BindingException {
    Draft4Job draft2Job = Draft4JobHelper.getDraft4Job(job);
    Draft4PortProcessorHelper portProcessorHelper = new Draft4PortProcessorHelper(draft2Job);
    try {
      File jobFile = new File(workingDir, JOB_FILE);
      String serializedJob = BeanSerializer.serializePartial(Draft4JobHelper.getDraft4Job(job));
      FileUtils.writeStringToFile(jobFile, serializedJob);
      
      Map<String, Object> inputs = job.getInputs();
      inputs = portProcessorHelper.setFileSize(inputs);
      inputs = portProcessorHelper.loadInputContents(inputs);
      inputs = portProcessorHelper.stageInputFiles(inputs, workingDir);
      return Job.cloneWithInputs(job, inputs);
    } catch (Draft4PortProcessorException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    Draft4Job draft2Job = Draft4JobHelper.getDraft4Job(job);
    List<Integer> successCodes = draft2Job.getApp().getSuccessCodes();

    if (successCodes == null) {
      successCodes = new ArrayList<>();
    }
    if (successCodes.isEmpty()) {
      successCodes.add(DEFAULT_SUCCESS_CODE);
    }
    for (Integer successCode : successCodes) {
      if (successCode.intValue() == statusCode) {
        return true;
      }
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Job postprocess(Job job, File workingDir) throws BindingException {
    Draft4Job draft2Job = Draft4JobHelper.getDraft4Job(job);
    try {
      Map<String, Object> outputs = null;

      if (draft2Job.getApp().isExpressionTool()) {
        Draft4ExpressionTool expressionTool = (Draft4ExpressionTool) draft2Job.getApp();
        try {
          outputs = (Map<String, Object>) Draft4ExpressionJavascriptResolver.evaluate(draft2Job.getInputs(), null, (String) expressionTool.getScript(), null);
        } catch (Draft4ExpressionException e) {
          throw new BindingException("Failed to populate outputs", e);
        }
      } else {
        outputs = collectOutputs(draft2Job, workingDir, null);
      }
      return Job.cloneWithOutputs(job, outputs);
    } catch (Draft4GlobException | Draft4ExpressionException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  private Map<String, Object> collectOutputs(Draft4Job job, File workingDir, HashAlgorithm hashAlgorithm) throws Draft4GlobException, Draft4ExpressionException, IOException, BindingException {
    File resultFile = new File(workingDir, resultFilename);
    
    if (resultFile.exists()) {
      String resultStr = FileUtils.readFileToString(resultFile);
      return JSONHelper.readMap(resultStr);
    }
    
    Map<String, Object> result = new HashMap<>();
    Draft4CommandLineTool commandLineTool = (Draft4CommandLineTool) job.getApp();
    for (Draft4OutputPort outputPort : commandLineTool.getOutputs()) {
      Object singleResult = collectOutput(job, workingDir, hashAlgorithm, outputPort.getSchema(), outputPort.getOutputBinding(), outputPort);
      if (singleResult != null) {
        result.put(Draft4SchemaHelper.normalizeId(outputPort.getId()), singleResult);
      }
    }
    BeanSerializer.serializePartial(resultFile, result);
    return result;
  }

  @SuppressWarnings("unchecked")
  private Object collectOutput(Draft4Job job, File workingDir, HashAlgorithm hashAlgorithm, Object schema, Object binding, Draft4OutputPort outputPort) throws Draft4GlobException, Draft4ExpressionException, BindingException {
    if (binding == null) {
      binding = Draft4SchemaHelper.getOutputBinding(schema);
    }
    logger.debug("Collecting outputs for {}.", job.getId());

    if (schema == null) {
      schema = Draft4SchemaHelper.TYPE_JOB_FILE;
    }

    Object result = null;
    if (Draft4SchemaHelper.isArrayFromSchema(schema)) {
      Draft4JobApp app = job.getApp();
      Object itemSchema = Draft4SchemaHelper.getSchemaForArrayItem(app.getSchemaDefs(), schema);
      if (itemSchema == null) {
        return null;
      }

      if (itemSchema.equals(Draft4SchemaHelper.TYPE_JOB_FILE) || Draft4SchemaHelper.isFileFromSchema(itemSchema)) {
        Object itemBinding = Draft4SchemaHelper.getOutputBinding(itemSchema);
        if (itemBinding != null) {
          binding = itemBinding;
        }
        result = globFiles(job, workingDir, hashAlgorithm, outputPort, binding);
      } else {
        result = collectOutput(job, workingDir, hashAlgorithm, itemSchema, binding, outputPort);
      }
    } else if (Draft4SchemaHelper.isRecordFromSchema(schema)) {
      Map<String, Object> record = new HashMap<>();
      Object fields = Draft4SchemaHelper.getFields(schema);

      if (fields instanceof List<?>) {
        List<Object> fieldList = (List<Object>) fields;
        for (Object field : fieldList) {
          Map<String, Object> fieldMap = (Map<String, Object>) field;

          String id = (String) fieldMap.get(Draft4SchemaHelper.KEY_SCHEMA_NAME);
          Object fieldSchema = fieldMap.get(Draft4SchemaHelper.KEY_SCHEMA_TYPE);
          Object fieldBinding = Draft4SchemaHelper.getOutputBinding(fieldMap);
          if (fieldBinding != null) {
            binding = fieldBinding;
          }
          Object singleResult = collectOutput(job, workingDir, hashAlgorithm, fieldSchema, binding, outputPort);
          if (singleResult != null) {
            record.put(id, singleResult);
          }
        }
      }
      result = record;
    } else {
      result = globFiles(job, workingDir, hashAlgorithm, outputPort, binding);
    }
    Object outputEval = Draft4BindingHelper.getOutputEval(binding);
    if (outputEval != null) {
      result = Draft4BindingHelper.evaluateOutputEval(job, result, binding);
      logger.info("OutputEval transformed result into {}.", result);
    }
    if (Draft4SchemaHelper.isFileFromSchema(schema)) {
      if (result instanceof List<?>) {
        switch (((List<?>) result).size()) {
        case 0:
          result = null;
          break;
        case 1:
          result = ((List<?>) result).get(0);
          break;
        default:
          throw new BindingException("Invalid file format " + result);
        }
      }
    }
    return result;

  }

  /**
   * Extracts files from a directory based on GLOB expression
   */
  private List<Map<String, Object>> globFiles(final Draft4Job job, final File workingDir, HashAlgorithm hashAlgorithm, final Draft4OutputPort outputPort, Object outputBinding) throws Draft4GlobException {
    if (outputPort.getOutputBinding() != null) {
      outputBinding = outputPort.getOutputBinding(); // override
    }
    if (outputBinding == null) {
      logger.debug("Output binding is empty. Skip output extraction.");
      return null;
    }

    Object glob = Draft4BindingHelper.getGlob(outputBinding);
    if (glob == null) {
      logger.debug("GLOB does not exist. Skip output extraction.");
      return null;
    }

    Set<File> paths = globService.glob(job, workingDir, glob);
    if (paths == null) {
      logger.info("Glob service didn't find any files.");
      return null;
    }
    logger.debug("Glob service returned result {}", paths);

    final List<Map<String, Object>> result = new ArrayList<>();
    for (File path : paths) {
      try {
        logger.info("Processing {}.", path);
        File file = path;
        Map<String, Object> fileData = new HashMap<>();
        Draft4FileValueHelper.setFileType(fileData);
        if (hashAlgorithm != null) {
          Draft4FileValueHelper.setChecksum(file, fileData, hashAlgorithm);
        }
        Draft4FileValueHelper.setSize(file.length(), fileData);
        Draft4FileValueHelper.setName(file.getName(), fileData);
        Draft4FileValueHelper.setPath(file.getAbsolutePath(), fileData);

        List<?> secondaryFiles = getSecondaryFiles(job, hashAlgorithm, fileData, file.getAbsolutePath(), outputBinding);
        if (secondaryFiles != null) {
          Draft4FileValueHelper.setSecondaryFiles(secondaryFiles, fileData);
        }
        Object metadata = Draft4BindingHelper.getMetadata(outputBinding);
        metadata = metadataService.evaluateMetadataExpressions(job, fileData, metadata);
        logger.info("Metadata expressions evaluated. Metadata is {}.", metadata);
        if (metadata != null) {
          Draft4FileValueHelper.setMetadata(metadata, fileData);
        }
        metadata = metadataService.processMetadata(job, fileData, outputPort, outputBinding);
        if (metadata != null) {
          logger.info("Metadata for {} resolved. Metadata is {}", outputPort.getId(), metadata);
          Draft4FileValueHelper.setMetadata(metadata, fileData);
        } else {
          logger.info("Metadata for {} output is empty.", outputPort.getId());
        }
        result.add(fileData);

        boolean loadContents = Draft4BindingHelper.loadContents(outputBinding);
        if (loadContents) {
          Draft4FileValueHelper.setContents(fileData);
        }
      } catch (Exception e) {
        logger.error("Failed to extract outputs", e);
        throw new Draft4GlobException("Failed to extract outputs.", e);
      }
    }
    return result;
  }

  /**
   * Gets secondary files (absolute paths)
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getSecondaryFiles(Draft4Job job, HashAlgorithm hashAlgorithm, Map<String, Object> fileValue, String fileName, Object binding) throws Draft4ExpressionException {
    Object secondaryFilesObj = Draft4BindingHelper.getSecondaryFiles(binding);

    if (secondaryFilesObj == null) {
      return null;
    }

    List<Object> secondaryFilesList = new ArrayList<>();
    if (secondaryFilesObj instanceof List<?>) {
      secondaryFilesList.addAll((Collection<? extends Object>) secondaryFilesObj);
    }
    
    List<Map<String, Object>> secondaryFileMaps = new ArrayList<>();
    for (Object suffixObj : secondaryFilesList) {
      String suffix = Draft4ExpressionResolver.resolve(suffixObj, job, fileValue);
      String secondaryFilePath = fileName.toString();

      while (suffix.startsWith("^")) {
        int extensionIndex = secondaryFilePath.lastIndexOf(".");
        if (extensionIndex != -1) {
          secondaryFilePath = secondaryFilePath.substring(0, extensionIndex);
          suffixObj = suffix.substring(1);
        } else {
          break;
        }
      }
      secondaryFilePath += suffix.startsWith(".") ? suffixObj : "." + suffixObj;
      File secondaryFile = new File(secondaryFilePath);
      if (secondaryFile.exists()) {
        Map<String, Object> secondaryFileMap = new HashMap<>();
        Draft4FileValueHelper.setFileType(secondaryFileMap);
        Draft4FileValueHelper.setPath(secondaryFile.getAbsolutePath(), secondaryFileMap);
        Draft4FileValueHelper.setSize(secondaryFile.length(), secondaryFileMap);
        Draft4FileValueHelper.setName(secondaryFile.getName(), secondaryFileMap);
        if (hashAlgorithm != null) {
          Draft4FileValueHelper.setChecksum(secondaryFile, secondaryFileMap, hashAlgorithm);
        }
        secondaryFileMaps.add(secondaryFileMap);
      }
    }
    return secondaryFileMaps;
  }

  @Override
  public Job postprocess(Job job, File workingDir, HashAlgorithm hashAlgorithm, boolean setFilename, boolean setSize,
      HashAlgorithm secondaryFilesHashAlgorithm, boolean secondaryFilesSetFilename, boolean secondaryFilesSetSize)
          throws BindingException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object transformInputs(Object value, Job job, Object transform) throws BindingException {
    // TODO Auto-generated method stub
    return null;
  }

}
