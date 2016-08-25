package org.rabix.bindings.draft2.processor.callback;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;

public class Draft2FileValueUpdateProcessorCallback implements Draft2PortProcessorCallback {

  private Set<FileValue> fileValues;

  public Draft2FileValueUpdateProcessorCallback(Set<FileValue> fileValues) {
    this.fileValues = fileValues;
  }
  
  @Override
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft2SchemaHelper.isFileFromValue(value)) {
      String path = Draft2FileValueHelper.getPath(value);
      FileValue fileValue = findFileValueByPath(path);
      
      if (fileValue != null && !StringUtils.isEmpty(fileValue.getRelocatedPath())) {
        Draft2FileValueHelper.setPath(fileValue.getRelocatedPath(), value);
      }
      List<Map<String, Object>> secondaryFiles = Draft2FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryValue : secondaryFiles) {
          String secondaryPath = Draft2FileValueHelper.getPath(secondaryValue);
          FileValue secondaryFileValue = findFileValueByPath(secondaryPath);
          if (secondaryFileValue != null && !StringUtils.isEmpty(secondaryFileValue.getRelocatedPath())) {
            Draft2FileValueHelper.setPath(secondaryFileValue.getRelocatedPath(), secondaryValue);
          }
        }
      }
      return new Draft2PortProcessorResult(value, true);
    }
    return new Draft2PortProcessorResult(value, false);
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
