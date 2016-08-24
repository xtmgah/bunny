package org.rabix.bindings.draft3.processor.callback;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.processor.Draft3PortProcessor;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorException;
import org.rabix.bindings.model.FileValue;

public class Draft3PortProcessorHelper {

  private final Draft3PortProcessor portProcessor;

  public Draft3PortProcessorHelper(Draft3Job draft3Job) {
    this.portProcessor = new Draft3PortProcessor(draft3Job);
  }

  public Set<String> flattenInputFilePaths(Map<String, Object> inputs) throws Draft3PortProcessorException {
    Draft3FilePathFlattenProcessorCallback callback = new Draft3FilePathFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft3PortProcessorException e) {
      throw new Draft3PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<FileValue> flattenInputFiles(Map<String, Object> inputs) throws Draft3PortProcessorException {
    Draft3FileValueFlattenProcessorCallback callback = new Draft3FileValueFlattenProcessorCallback(null);
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft3PortProcessorException e) {
      throw new Draft3PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<FileValue> flattenOutputFiles(Map<String, Object> outputs, Set<String> visiblePorts) throws Draft3PortProcessorException {
    Draft3FileValueFlattenProcessorCallback callback = new Draft3FileValueFlattenProcessorCallback(visiblePorts);
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (Draft3PortProcessorException e) {
      throw new Draft3PortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<String> flattenOutputFilePaths(Map<String, Object> outputs) throws Draft3PortProcessorException {
    Draft3FilePathFlattenProcessorCallback callback = new Draft3FilePathFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (Draft3PortProcessorException e) {
      throw new Draft3PortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<Map<String, Object>> flattenInputFileData(Map<String, Object> inputs) throws Draft3PortProcessorException {
    Draft3FileDataFlattenProcessorCallback callback = new Draft3FileDataFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (Draft3PortProcessorException e) {
      throw new Draft3PortProcessorException("Failed to flatten input file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<Map<String, Object>> flattenOutputFileData(Map<String, Object> outputs)
      throws Draft3PortProcessorException {
    Draft3FileDataFlattenProcessorCallback callback = new Draft3FileDataFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (Draft3PortProcessorException e) {
      throw new Draft3PortProcessorException("Failed to flatten output file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Map<String, Object> setFileSize(Map<String, Object> inputs) throws Draft3PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new Draft3FileSizeProcessorCallback());
    } catch (Draft3PortProcessorException e) {
      throw new Draft3PortProcessorException("Failed to set input file size", e);
    }
  }

  public Map<String, Object> loadInputContents(Map<String, Object> inputs) throws Draft3PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new Draft3LoadContentsPortProcessorCallback());
    } catch (Draft3PortProcessorException e) {
      throw new Draft3PortProcessorException("Failed to load input contents.", e);
    }
  }

  public Map<String, Object> stageInputFiles(Map<String, Object> inputs, File workingDir)
      throws Draft3PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new Draft3StageInputProcessorCallback(workingDir));
    } catch (Draft3PortProcessorException e) {
      throw new Draft3PortProcessorException("Failed to stage inputs.", e);
    }
  }

}
