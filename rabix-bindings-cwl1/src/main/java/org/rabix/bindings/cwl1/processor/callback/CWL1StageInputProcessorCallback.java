package org.rabix.bindings.cwl1.processor.callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.cwl1.bean.CWL1InputPort;
import org.rabix.bindings.cwl1.bean.CWL1InputPort.StageInput;
import org.rabix.bindings.cwl1.helper.CWL1FileValueHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorCallback;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CWL1StageInputProcessorCallback implements CWL1PortProcessorCallback {

  private static final Logger logger = LoggerFactory.getLogger(CWL1StageInputProcessorCallback.class);

  private final File workingDir;

  public CWL1StageInputProcessorCallback(File workingDir) {
    this.workingDir = workingDir;
  }

  @Override
  public CWL1PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (!(port instanceof CWL1InputPort)) {
      throw new RuntimeException("Inputs only can be staged!");
    }
    CWL1InputPort inputPort = (CWL1InputPort) port;
    String stageInputStr = inputPort.getStageInput();
    if (stageInputStr == null) {
      return new CWL1PortProcessorResult(value, true);
    }
    return new CWL1PortProcessorResult(stage(value, StageInput.get(stageInputStr)), true);
  }

  @SuppressWarnings("unchecked")
  public Object stage(Object value, StageInput stageInput) throws BindingException {
    if (value == null) {
      return null;
    }
    if (CWL1SchemaHelper.isFileFromValue(value)) {
      return stageSingle(value, stageInput);
    } else if (value instanceof List<?>) {
      List<Object> stagedValues = new ArrayList<>();
      for (Object subvalue : ((List<?>) value)) {
        stagedValues.add(stage(subvalue, stageInput));
      }
      return stagedValues;
    } else if (value instanceof Map<?, ?>) {
      Map<String, Object> stagedValues = new HashMap<>();
      for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
        stagedValues.put(entry.getKey(), stage(entry.getValue(), stageInput));
      }
      return stagedValues;
    }
    return value;
  }

  private Object stageSingle(Object value, StageInput stageInput) throws BindingException {
    if (CWL1SchemaHelper.isFileFromValue(value)) {
      String originalPath = CWL1FileValueHelper.getPath(value);
      String path = stagePath(originalPath, stageInput);
      CWL1FileValueHelper.setPath(path, value);
      CWL1FileValueHelper.setOriginalPath(originalPath, value);;

      List<Map<String, Object>> secondaryFileValues = CWL1FileValueHelper.getSecondaryFiles(value);
      if (secondaryFileValues != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFileValues) {
          String secondaryFilePath = stagePath(CWL1FileValueHelper.getPath(secondaryFileValue), stageInput);
          CWL1FileValueHelper.setPath(secondaryFilePath, secondaryFileValue);
        }
      }
    }
    return value;
  }

  private String stagePath(String path, StageInput stageInput) throws BindingException {
    switch (stageInput) { // just copy for now
    case COPY:
    case LINK:
      File file = new File(path);
      if (!file.exists()) {
        throw new BindingException("Failed to stage input file path " + path);
      }
      File destinationFile = new File(workingDir, file.getName());
      if (destinationFile.exists()) {
        throw new BindingException("Failed to stage input file path " + path + ". File with the same name already exists.");
      }
      logger.info("Stage input file {} to {}.", file, destinationFile);
      try {
        if (file.isFile()) {
          FileUtils.copyFile(file, destinationFile);
        } else {
          FileUtils.copyDirectory(file, destinationFile);
        }
      } catch (IOException e) {
        throw new BindingException(e);
      }
      return destinationFile.getAbsolutePath();
    default:
      throw new BindingException("Failed to stage input files. StageInput " + stageInput + " is not supported");
    }
  }
}
