package org.rabix.bindings.draft3.processor.callback;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorCallback;
import org.rabix.bindings.draft3.processor.Draft3PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;

public class Draft3FileValueUpdateProcessorCallback implements Draft3PortProcessorCallback {

  private Set<FileValue> fileValues;

  public Draft3FileValueUpdateProcessorCallback(Set<FileValue> fileValues) {
    this.fileValues = fileValues;
  }
  
  @Override
  public Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft3SchemaHelper.isFileFromValue(value)) {
      String path = Draft3FileValueHelper.getPath(value);
      FileValue fileValue = findFileValueByPath(path);
      
      if (fileValue != null && !StringUtils.isEmpty(fileValue.getRelocatedPath())) {
        Draft3FileValueHelper.setPath(fileValue.getRelocatedPath(), value);
      }
      List<Map<String, Object>> secondaryFiles = Draft3FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryValue : secondaryFiles) {
          String secondaryPath = Draft3FileValueHelper.getPath(secondaryValue);
          FileValue secondaryFileValue = findFileValueByPath(secondaryPath);
          if (secondaryFileValue != null && !StringUtils.isEmpty(secondaryFileValue.getRelocatedPath())) {
            Draft3FileValueHelper.setPath(secondaryFileValue.getRelocatedPath(), secondaryValue);
          }
        }
      }
      return new Draft3PortProcessorResult(value, true);
    }
    return new Draft3PortProcessorResult(value, false);
  }
  
  private FileValue findFileValueByPath(String path) {
    for(FileValue file : fileValues) {
      if (path.equals(file.getPath())) {
        return file;
      }
    }
    return null;
  }

}
