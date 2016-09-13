package org.rabix.bindings.cwl1.processor.callback;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessor;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorException;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.FileValue;

public class CWL1PortProcessorHelper {

  private final CWL1Job cwl1Job;
  private final CWL1PortProcessor portProcessor;

  public CWL1PortProcessorHelper(CWL1Job cwl1Job) {
    this.cwl1Job = cwl1Job;
    this.portProcessor = new CWL1PortProcessor(cwl1Job);
  }
  
  public Set<FileValue> getInputFiles(Map<String, Object> inputs, FileMapper fileMapper, Map<String, Object> config) throws CWL1PortProcessorException {
    if (fileMapper != null) {
      CWL1FilePathMapProcessorCallback fileMapperCallback = new CWL1FilePathMapProcessorCallback(fileMapper, config);
      inputs = portProcessor.processInputs(inputs, fileMapperCallback);
    }
    
    CWL1FileValueProcessorCallback callback = new CWL1FileValueProcessorCallback(cwl1Job, null, true);
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to get input files.", e);
    }
    return callback.getFileValues();
  }
  
  public Set<FileValue> getOutputFiles(Map<String, Object> outputs, Set<String> visiblePorts) throws CWL1PortProcessorException {
    CWL1FileValueProcessorCallback callback = new CWL1FileValueProcessorCallback(cwl1Job, visiblePorts, false);
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to get output files.", e);
    }
    return callback.getFileValues();
  }

  public Set<String> flattenInputFilePaths(Map<String, Object> inputs) throws CWL1PortProcessorException {
    CWL1FilePathFlattenProcessorCallback callback = new CWL1FilePathFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Set<FileValue> flattenInputFiles(Map<String, Object> inputs) throws CWL1PortProcessorException {
    CWL1FileValueFlattenProcessorCallback callback = new CWL1FileValueFlattenProcessorCallback(null);
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to flatten input file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<FileValue> flattenOutputFiles(Map<String, Object> outputs, Set<String> visiblePorts) throws CWL1PortProcessorException {
    CWL1FileValueFlattenProcessorCallback callback = new CWL1FileValueFlattenProcessorCallback(visiblePorts);
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<String> flattenOutputFilePaths(Map<String, Object> outputs) throws CWL1PortProcessorException {
    CWL1FilePathFlattenProcessorCallback callback = new CWL1FilePathFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to flatten output file paths.", e);
    }
    return callback.getFlattenedPaths();
  }

  public Map<String, Object> updateInputFiles(Map<String, Object> inputs, Set<FileValue> fileValues) throws CWL1PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new CWL1FileValueUpdateProcessorCallback(fileValues));
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to set input file size", e);
    }
  }
  
  public Map<String, Object> updateOutputFiles(Map<String, Object> outputs, Set<FileValue> fileValues) throws CWL1PortProcessorException {
    try {
      return portProcessor.processOutputs(outputs, new CWL1FileValueUpdateProcessorCallback(fileValues));
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to set input file size", e);
    }
  }
  
  public Set<Map<String, Object>> flattenInputFileData(Map<String, Object> inputs) throws CWL1PortProcessorException {
    CWL1FileDataFlattenProcessorCallback callback = new CWL1FileDataFlattenProcessorCallback();
    try {
      portProcessor.processInputs(inputs, callback);
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to flatten input file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Set<Map<String, Object>> flattenOutputFileData(Map<String, Object> outputs)
      throws CWL1PortProcessorException {
    CWL1FileDataFlattenProcessorCallback callback = new CWL1FileDataFlattenProcessorCallback();
    try {
      portProcessor.processOutputs(outputs, callback);
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to flatten output file data.", e);
    }
    return callback.getFlattenedFileData();
  }

  public Map<String, Object> setFileSize(Map<String, Object> inputs) throws CWL1PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new CWL1FileSizeProcessorCallback());
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to set input file size", e);
    }
  }

  public Map<String, Object> loadInputContents(Map<String, Object> inputs) throws CWL1PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new CWL1LoadContentsPortProcessorCallback());
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to load input contents.", e);
    }
  }

  public Map<String, Object> stageInputFiles(Map<String, Object> inputs, File workingDir)
      throws CWL1PortProcessorException {
    try {
      return portProcessor.processInputs(inputs, new CWL1StageInputProcessorCallback(workingDir));
    } catch (CWL1PortProcessorException e) {
      throw new CWL1PortProcessorException("Failed to stage inputs.", e);
    }
  }

}
