package org.rabix.bindings.sb.processor.callback;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;

public class SBFileValueUpdateProcessorCallback implements SBPortProcessorCallback {

  private Set<FileValue> fileValues;

  public SBFileValueUpdateProcessorCallback(Set<FileValue> fileValues) {
    this.fileValues = fileValues;
  }
  
  @Override
  public SBPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value)) {
      String path = SBFileValueHelper.getPath(value);
      FileValue fileValue = findFileValueByPath(path);
      
      if (fileValue != null && !StringUtils.isEmpty(fileValue.getRelocatedPath())) {
        SBFileValueHelper.setPath(fileValue.getRelocatedPath(), value);
      }
      List<Map<String, Object>> secondaryFiles = SBFileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryValue : secondaryFiles) {
          String secondaryPath = SBFileValueHelper.getPath(secondaryValue);
          FileValue secondaryFileValue = findFileValueByPath(secondaryPath);
          if (secondaryFileValue != null && !StringUtils.isEmpty(secondaryFileValue.getRelocatedPath())) {
            SBFileValueHelper.setPath(secondaryFileValue.getRelocatedPath(), secondaryValue);
          }
        }
      }
      return new SBPortProcessorResult(value, true);
    }
    return new SBPortProcessorResult(value, false);
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
