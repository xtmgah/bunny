package org.rabix.bindings.cwl1;

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
import org.rabix.bindings.cwl1.bean.CWL1CommandLineTool;
import org.rabix.bindings.cwl1.bean.CWL1ExpressionTool;
import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.bean.CWL1JobApp;
import org.rabix.bindings.cwl1.bean.CWL1OutputPort;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionException;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionResolver;
import org.rabix.bindings.cwl1.expression.javascript.CWL1ExpressionJavascriptResolver;
import org.rabix.bindings.cwl1.helper.CWL1BindingHelper;
import org.rabix.bindings.cwl1.helper.CWL1DirectoryValueHelper;
import org.rabix.bindings.cwl1.helper.CWL1FileValueHelper;
import org.rabix.bindings.cwl1.helper.CWL1JobHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorException;
import org.rabix.bindings.cwl1.processor.callback.CWL1PortProcessorHelper;
import org.rabix.bindings.cwl1.service.CWL1GlobException;
import org.rabix.bindings.cwl1.service.CWL1GlobService;
import org.rabix.bindings.cwl1.service.CWL1MetadataService;
import org.rabix.bindings.cwl1.service.impl.CWL1GlobServiceImpl;
import org.rabix.bindings.cwl1.service.impl.CWL1MetadataServiceImpl;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.json.BeanSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CWL1Processor implements ProtocolProcessor {

  public final static int DEFAULT_SUCCESS_CODE = 0;
  
  public final static String JOB_FILE = "job.json";
  public final static String RESULT_FILENAME = "cwl.output.json";
  
  private final static Logger logger = LoggerFactory.getLogger(CWL1Processor.class);

  private final CWL1GlobService globService;
  private final CWL1MetadataService metadataService;

  public CWL1Processor() {
    this.globService = new CWL1GlobServiceImpl();
    this.metadataService = new CWL1MetadataServiceImpl();
  }

  @Override
  public Job preprocess(final Job job, final File workingDir) throws BindingException {
    CWL1Job draft2Job = CWL1JobHelper.getCWL1Job(job);
    CWL1PortProcessorHelper portProcessorHelper = new CWL1PortProcessorHelper(draft2Job);
    try {
      File jobFile = new File(workingDir, JOB_FILE);
      String serializedJob = BeanSerializer.serializePartial(CWL1JobHelper.getCWL1Job(job));
      FileUtils.writeStringToFile(jobFile, serializedJob);
      
      Map<String, Object> inputs = job.getInputs();
      inputs = portProcessorHelper.setFileSize(inputs);
      inputs = portProcessorHelper.loadInputContents(inputs);
      inputs = portProcessorHelper.stageInputFiles(inputs, workingDir);
      return Job.cloneWithInputs(job, inputs);
    } catch (CWL1PortProcessorException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  @Override
  public boolean isSuccessful(Job job, int statusCode) throws BindingException {
    CWL1Job draft2Job = CWL1JobHelper.getCWL1Job(job);
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
    CWL1Job draft2Job = CWL1JobHelper.getCWL1Job(job);
    try {
      Map<String, Object> outputs = null;

      if (draft2Job.getApp().isExpressionTool()) {
        CWL1ExpressionTool expressionTool = (CWL1ExpressionTool) draft2Job.getApp();
        try {
          outputs = (Map<String, Object>) CWL1ExpressionJavascriptResolver.evaluate(draft2Job.getInputs(), null, (String) expressionTool.getScript(), null);
        } catch (CWL1ExpressionException e) {
          throw new BindingException("Failed to populate outputs", e);
        }
      } else {
        outputs = collectOutputs(draft2Job, workingDir, null);
      }
      return Job.cloneWithOutputs(job, outputs);
    } catch (CWL1GlobException | CWL1ExpressionException | IOException e) {
      throw new BindingException(e);
    }
  }
  
  private Map<String, Object> collectOutputs(CWL1Job job, File workingDir, HashAlgorithm hashAlgorithm) throws CWL1GlobException, CWL1ExpressionException, IOException, BindingException {
    File resultFile = new File(workingDir, RESULT_FILENAME);
    
    if (resultFile.exists()) {
      String resultStr = FileUtils.readFileToString(resultFile);
      return JSONHelper.readMap(resultStr);
    }
    
    Map<String, Object> result = new HashMap<>();
    CWL1CommandLineTool commandLineTool = (CWL1CommandLineTool) job.getApp();
    for (CWL1OutputPort outputPort : commandLineTool.getOutputs()) {
      Object singleResult = collectOutput(job, workingDir, hashAlgorithm, outputPort.getSchema(), outputPort.getOutputBinding(), outputPort);
      if (singleResult != null) {
        result.put(CWL1SchemaHelper.normalizeId(outputPort.getId()), singleResult);
      }
    }
    BeanSerializer.serializePartial(resultFile, result);
    return result;
  }

  @SuppressWarnings("unchecked")
  private Object collectOutput(CWL1Job job, File workingDir, HashAlgorithm hashAlgorithm, Object schema, Object binding, CWL1OutputPort outputPort) throws CWL1GlobException, CWL1ExpressionException, BindingException {
    if (binding == null) {
      binding = CWL1SchemaHelper.getOutputBinding(schema);
    }
    logger.debug("Collecting outputs for {}.", job.getId());

    if (schema == null) {
      schema = CWL1SchemaHelper.TYPE_JOB_FILE;
    }

    Object result = null;
    if (CWL1SchemaHelper.isArrayFromSchema(schema)) {
      CWL1JobApp app = job.getApp();
      Object itemSchema = CWL1SchemaHelper.getSchemaForArrayItem(app.getSchemaDefs(), schema);
      if (itemSchema == null) {
        return null;
      }

      if (itemSchema.equals(CWL1SchemaHelper.TYPE_JOB_FILE) || CWL1SchemaHelper.isFileFromSchema(itemSchema)) {
        Object itemBinding = CWL1SchemaHelper.getOutputBinding(itemSchema);
        if (itemBinding != null) {
          binding = itemBinding;
        }
        result = globFiles(job, workingDir, hashAlgorithm, outputPort, binding);
      } else {
        return collectOutput(job, workingDir, hashAlgorithm, itemSchema, binding, outputPort);
      }
    } else if (CWL1SchemaHelper.isRecordFromSchema(schema)) {
      Map<String, Object> record = new HashMap<>();
      Object fields = CWL1SchemaHelper.getFields(schema);

      if (fields instanceof List<?>) {
        List<Object> fieldList = (List<Object>) fields;
        for (Object field : fieldList) {
          Map<String, Object> fieldMap = (Map<String, Object>) field;

          String id = (String) fieldMap.get(CWL1SchemaHelper.KEY_SCHEMA_NAME);
          Object fieldSchema = fieldMap.get(CWL1SchemaHelper.KEY_SCHEMA_TYPE);
          Object fieldBinding = CWL1SchemaHelper.getOutputBinding(fieldMap);
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
    Object outputEval = CWL1BindingHelper.getOutputEval(binding);
    if (outputEval != null) {
      result = CWL1BindingHelper.evaluateOutputEval(job, result, binding);
      logger.info("OutputEval transformed result into {}.", result);
    }
    if (result instanceof List<?>) {
      if (CWL1SchemaHelper.isFileFromSchema(schema)) {
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
  private List<Map<String, Object>> globFiles(final CWL1Job job, final File workingDir, HashAlgorithm hashAlgorithm, final CWL1OutputPort outputPort, Object outputBinding) throws CWL1GlobException {
    if (outputPort.getOutputBinding() != null) {
      outputBinding = outputPort.getOutputBinding(); // override
    }
    if (outputBinding == null) {
      logger.debug("Output binding is empty. Skip output extraction.");
      return null;
    }

    Object glob = CWL1BindingHelper.getGlob(outputBinding);
    if (glob == null) {
      logger.debug("GLOB does not exist. Skip output extraction.");
      return null;
    }

    Set<File> files = globService.glob(job, workingDir, glob);
    if (files == null) {
      logger.info("Glob service didn't find any files.");
      return null;
    }
    logger.debug("Glob service returned result {}", files);

    final List<Map<String, Object>> result = new ArrayList<>();
    for (File file : files) {
      try {
        result.add(formFileValue(file, job, outputBinding, outputPort, hashAlgorithm));
      } catch (Exception e) {
        logger.error("Failed to extract outputs", e);
        throw new CWL1GlobException("Failed to extract outputs.", e);
      }
    }
    return result;
  }
  
  public Map<String, Object> formFileValue(File file, CWL1Job job, Object outputBinding, CWL1OutputPort outputPort, HashAlgorithm hashAlgorithm) throws CWL1ExpressionException, IOException {
    if (file.isDirectory()) {
      logger.info("Processing directory {}.", file);
      
      Map<String, Object> directory = new HashMap<>();
      CWL1DirectoryValueHelper.setDirectoryType(directory);
      CWL1DirectoryValueHelper.setSize(file.length(), directory);
      CWL1DirectoryValueHelper.setName(file.getName(), directory);
      CWL1DirectoryValueHelper.setPath(file.getAbsolutePath(), directory);
      
      File[] list = file.listFiles();
      
      List<Object> listing = new ArrayList<>();
      for (File subfile : list) {
        listing.add(formFileValue(subfile, job, outputBinding, outputPort, hashAlgorithm));
      }
      CWL1DirectoryValueHelper.setListing(listing, directory);
      return directory;
    }

    Map<String, Object> fileData = new HashMap<>();
    CWL1FileValueHelper.setFileType(fileData);
    if (hashAlgorithm != null) {
      CWL1FileValueHelper.setChecksum(file, fileData, hashAlgorithm);
    }
    CWL1FileValueHelper.setSize(file.length(), fileData);
    CWL1FileValueHelper.setName(file.getName(), fileData);
    CWL1FileValueHelper.setPath(file.getAbsolutePath(), fileData);

    List<?> secondaryFiles = getSecondaryFiles(job, hashAlgorithm, fileData, file.getAbsolutePath(), outputBinding);
    if (secondaryFiles != null) {
      CWL1FileValueHelper.setSecondaryFiles(secondaryFiles, fileData);
    }
    Object metadata = CWL1BindingHelper.getMetadata(outputBinding);
    metadata = metadataService.evaluateMetadataExpressions(job, fileData, metadata);
    logger.info("Metadata expressions evaluated. Metadata is {}.", metadata);
    if (metadata != null) {
      CWL1FileValueHelper.setMetadata(metadata, fileData);
    }
    metadata = metadataService.processMetadata(job, fileData, outputPort, outputBinding);
    if (metadata != null) {
      logger.info("Metadata for {} resolved. Metadata is {}", outputPort.getId(), metadata);
      CWL1FileValueHelper.setMetadata(metadata, fileData);
    } else {
      logger.info("Metadata for {} output is empty.", outputPort.getId());
    }
    boolean loadContents = CWL1BindingHelper.loadContents(outputBinding);
    if (loadContents) {
      CWL1FileValueHelper.setContents(fileData);
    }
    return fileData;
  }

  /**
   * Gets secondary files (absolute paths)
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getSecondaryFiles(CWL1Job job, HashAlgorithm hashAlgorithm, Map<String, Object> fileValue, String fileName, Object binding) throws CWL1ExpressionException {
    Object secondaryFilesObj = CWL1BindingHelper.getSecondaryFiles(binding);

    if (secondaryFilesObj == null) {
      return null;
    }

    List<Object> secondaryFilesList = new ArrayList<>();
    if (secondaryFilesObj instanceof List<?>) {
      secondaryFilesList.addAll((Collection<? extends Object>) secondaryFilesObj);
    }
    
    List<Map<String, Object>> secondaryFileMaps = new ArrayList<>();
    for (Object suffixObj : secondaryFilesList) {
      String suffix = CWL1ExpressionResolver.resolve(suffixObj, job, fileValue);
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
        CWL1FileValueHelper.setFileType(secondaryFileMap);
        CWL1FileValueHelper.setPath(secondaryFile.getAbsolutePath(), secondaryFileMap);
        CWL1FileValueHelper.setSize(secondaryFile.length(), secondaryFileMap);
        CWL1FileValueHelper.setName(secondaryFile.getName(), secondaryFileMap);
        if (hashAlgorithm != null) {
          CWL1FileValueHelper.setChecksum(secondaryFile, secondaryFileMap, hashAlgorithm);
        }
        secondaryFileMaps.add(secondaryFileMap);
      }
    }
    return secondaryFileMaps;
  }

}
