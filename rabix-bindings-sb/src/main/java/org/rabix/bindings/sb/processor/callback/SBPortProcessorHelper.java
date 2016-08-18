package org.rabix.bindings.sb.processor.callback;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.processor.SBPortProcessor;
import org.rabix.bindings.sb.processor.SBPortProcessorException;

public class SBPortProcessorHelper {

  private final SBPortProcessor portProcessor;

  public SBPortProcessorHelper(SBJob sbJob) {
    this.portProcessor = new SBPortProcessor(sbJob);
  }

  public Set<String> flattenInputFilePaths(Map<String, Object> inputs) throws SBPortProcessorException {
    SBFilePathFlattenProcessorCallback callback = new SBFilePathFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<FileValue> flattenInputFiles(Map<String, Object> inputs) throws SBPortProcessorException {
    SBFileValueFlattenProcessorCallback callback = new SBFileValueFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<FileValue> flattenOutputFiles(Map<String, Object> inputs) throws SBPortProcessorException {
    SBFileValueFlattenProcessorCallback callback = new SBFileValueFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(inputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<String> flattenOutputFilePaths(Map<String, Object> outputs) throws SBPortProcessorException {
    SBFilePathFlattenProcessorCallback callback = new SBFilePathFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<Map<String, Object>> flattenInputFileData(Map<String, Object> inputs) throws SBPortProcessorException {
    SBFileDataFlattenProcessorCallback callback = new SBFileDataFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten input file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<Map<String, Object>> flattenOutputFileData(Map<String, Object> outputs)
      throws SBPortProcessorException {
    SBFileDataFlattenProcessorCallback callback = new SBFileDataFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to flatten output file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Map<String, Object> setFileSize(Map<String, Object> inputs) throws SBPortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new SBFileSizeProcessorCallback());
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to set input file size", e);
    }
  }
  
  public Map<String, Object> fixOutputMetadata(Map<String, Object> inputs, Map<String, Object> outputs) throws SBPortProcessorException {
    try {
      SBMetadataCallback callback = new SBMetadataCallback(inputs);
      Map<String, Object> fixedOutputs = portProcessor.processOutputs(outputs, callback);
      fixedOutputs = portProcessor.processOutputs(fixedOutputs, callback); // call twice on purpose
      return fixedOutputs;
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to fix metadata", e);
    }
  }

  public Map<String, Object> loadInputContents(Map<String, Object> inputs) throws SBPortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new SBLoadContentsPortProcessorCallback());
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to load input contents.", e);
    }
  }

  public Map<String, Object> stageInputFiles(Map<String, Object> inputs, File workingDir)
      throws SBPortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new SBStageInputProcessorCallback(workingDir));
    } catch (SBPortProcessorException e) {
      throw new SBPortProcessorException("Failed to stage inputs.", e);
    }
  }

}
