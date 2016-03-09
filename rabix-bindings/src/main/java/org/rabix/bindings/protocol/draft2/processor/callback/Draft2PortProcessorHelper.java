package org.rabix.bindings.protocol.draft2.processor.callback;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessor;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessorException;

public class Draft2PortProcessorHelper {

  private final Draft2PortProcessor portProcessor;

  public Draft2PortProcessorHelper(Draft2Job draft2Job) {
    this.portProcessor = new Draft2PortProcessor(draft2Job);
  }

  public Set<String> flattenInputFilePaths(Map<String, Object> inputs) throws Draft2PortProcessorException {
    FilePathFlattenProcessorCallback callback = new FilePathFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<FileValue> flattenInputFiles(Map<String, Object> inputs) throws Draft2PortProcessorException {
    Draft2FileValueFlattenProcessorCallback callback = new Draft2FileValueFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<FileValue> flattenOutputFiles(Map<String, Object> inputs) throws Draft2PortProcessorException {
    Draft2FileValueFlattenProcessorCallback callback = new Draft2FileValueFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(inputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<String> flattenOutputFilePaths(Map<String, Object> outputs) throws Draft2PortProcessorException {
    FilePathFlattenProcessorCallback callback = new FilePathFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<Map<String, Object>> flattenInputFileData(Map<String, Object> inputs) throws Draft2PortProcessorException {
    FileDataFlattenProcessorCallback callback = new FileDataFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten input file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<Map<String, Object>> flattenOutputFileData(Map<String, Object> outputs)
      throws Draft2PortProcessorException {
    FileDataFlattenProcessorCallback callback = new FileDataFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to flatten output file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Map<String, Object> setFileSize(Map<String, Object> inputs) throws Draft2PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new FileSizeProcessorCallback());
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to set input file size", e);
    }
  }

  public Map<String, Object> loadInputContents(Map<String, Object> inputs) throws Draft2PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new LoadContentsPortProcessorCallback());
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to load input contents.", e);
    }
  }

  public Map<String, Object> stageInputFiles(Map<String, Object> inputs, File workingDir)
      throws Draft2PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new StageInputProcessorCallback(workingDir));
    } catch (Draft2PortProcessorException e) {
      throw new Draft2PortProcessorException("Failed to stage inputs.", e);
    }
  }

}
