package org.rabix.bindings.protocol.draft4.processor.callback;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessor;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorException;

public class Draft4PortProcessorHelper {

  private final Draft4PortProcessor portProcessor;

  public Draft4PortProcessorHelper(Draft4Job draft4Job) {
    this.portProcessor = new Draft4PortProcessor(draft4Job);
  }

  public Set<String> flattenInputFilePaths(Map<String, Object> inputs) throws Draft4PortProcessorException {
    Draft4FilePathFlattenProcessorCallback callback = new Draft4FilePathFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft4PortProcessorException e) {
      throw new Draft4PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<FileValue> flattenInputFiles(Map<String, Object> inputs) throws Draft4PortProcessorException {
    Draft4FileValueFlattenProcessorCallback callback = new Draft4FileValueFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft4PortProcessorException e) {
      throw new Draft4PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<FileValue> flattenOutputFiles(Map<String, Object> inputs) throws Draft4PortProcessorException {
    Draft4FileValueFlattenProcessorCallback callback = new Draft4FileValueFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(inputs, callback);
    } catch (Draft4PortProcessorException e) {
      throw new Draft4PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<String> flattenOutputFilePaths(Map<String, Object> outputs) throws Draft4PortProcessorException {
    Draft4FilePathFlattenProcessorCallback callback = new Draft4FilePathFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (Draft4PortProcessorException e) {
      throw new Draft4PortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<Map<String, Object>> flattenInputFileData(Map<String, Object> inputs) throws Draft4PortProcessorException {
    Draft4FileDataFlattenProcessorCallback callback = new Draft4FileDataFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft4PortProcessorException e) {
      throw new Draft4PortProcessorException("Failed to flatten input file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<Map<String, Object>> flattenOutputFileData(Map<String, Object> outputs)
      throws Draft4PortProcessorException {
    Draft4FileDataFlattenProcessorCallback callback = new Draft4FileDataFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (Draft4PortProcessorException e) {
      throw new Draft4PortProcessorException("Failed to flatten output file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Map<String, Object> setFileSize(Map<String, Object> inputs) throws Draft4PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new Draft4FileSizeProcessorCallback());
    } catch (Draft4PortProcessorException e) {
      throw new Draft4PortProcessorException("Failed to set input file size", e);
    }
  }

  public Map<String, Object> loadInputContents(Map<String, Object> inputs) throws Draft4PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new Draft4LoadContentsPortProcessorCallback());
    } catch (Draft4PortProcessorException e) {
      throw new Draft4PortProcessorException("Failed to load input contents.", e);
    }
  }

  public Map<String, Object> stageInputFiles(Map<String, Object> inputs, File workingDir)
      throws Draft4PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new Draft4StageInputProcessorCallback(workingDir));
    } catch (Draft4PortProcessorException e) {
      throw new Draft4PortProcessorException("Failed to stage inputs.", e);
    }
  }

}
