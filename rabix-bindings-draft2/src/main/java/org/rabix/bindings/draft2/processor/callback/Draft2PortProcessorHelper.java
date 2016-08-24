package org.rabix.bindings.draft2.processor.callback;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.processor.Draft2PortProcessor;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorException;
import org.rabix.bindings.model.FileValue;

public class Draft2PortProcessorHelper {

  private final Draft2PortProcessor portProcessor;

  public Draft2PortProcessorHelper(Draft2Job draft2Job) {
    this.portProcessor = new Draft2PortProcessor(draft2Job);
  }

  public Set<String> flattenInputFilePaths(Map<String, Object> inputs) throws Draft2PortProcessorException {
    Draft2FilePathFlattenProcessorCallback callback = new Draft2FilePathFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<FileValue> flattenInputFiles(Map<String, Object> inputs) throws Draft2PortProcessorException {
    Draft2FileValueFlattenProcessorCallback callback = new Draft2FileValueFlattenProcessorCallback(null);
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<FileValue> flattenOutputFiles(Map<String, Object> outputs, Set<String> visiblePorts) throws Draft2PortProcessorException {
    Draft2FileValueFlattenProcessorCallback callback = new Draft2FileValueFlattenProcessorCallback(visiblePorts);
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten outputs file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<String> flattenOutputFilePaths(Map<String, Object> outputs) throws Draft2PortProcessorException {
    Draft2FilePathFlattenProcessorCallback callback = new Draft2FilePathFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<Map<String, Object>> flattenInputFileData(Map<String, Object> inputs) throws Draft2PortProcessorException {
    Draft2FileDataFlattenProcessorCallback callback = new Draft2FileDataFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten input file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<Map<String, Object>> flattenOutputFileData(Map<String, Object> outputs)
      throws Draft2PortProcessorException {
    Draft2FileDataFlattenProcessorCallback callback = new Draft2FileDataFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten output file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Map<String, Object> setFileSize(Map<String, Object> inputs) throws Draft2PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new Draft2FileSizeProcessorCallback());
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to set input file size", e);
    }
  }
  
  public Map<String, Object> fixOutputMetadata(Map<String, Object> inputs, Map<String, Object> outputs) throws Draft2PortProcessorException {
    try {
      Draft2MetadataCallback callback = new Draft2MetadataCallback(inputs);
      Map<String, Object> fixedOutputs = portProcessor.processOutputs(outputs, callback);
      fixedOutputs = portProcessor.processOutputs(fixedOutputs, callback); // call twice on purpose
      return fixedOutputs;
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to fix metadata", e);
    }
  }

  public Map<String, Object> loadInputContents(Map<String, Object> inputs) throws Draft2PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new Draft2LoadContentsPortProcessorCallback());
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to load input contents.", e);
    }
  }

  public Map<String, Object> stageInputFiles(Map<String, Object> inputs, File workingDir)
      throws Draft2PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new Draft2StageInputProcessorCallback(workingDir));
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to stage inputs.", e);
    }
  }

}
