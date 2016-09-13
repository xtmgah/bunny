package org.rabix.bindings.cwl1.processor.callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.cwl1.helper.Draft3FileValueHelper;
import org.rabix.bindings.cwl1.helper.Draft3SchemaHelper;
import org.rabix.bindings.cwl1.processor.Draft3PortProcessorCallback;
import org.rabix.bindings.cwl1.processor.Draft3PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.common.helper.CloneHelper;

public class Draft3FileValueUpdateProcessorCallback implements Draft3PortProcessorCallback {

  private Set<FileValue> fileValues;

  public Draft3FileValueUpdateProcessorCallback(Set<FileValue> fileValues) {
    this.fileValues = fileValues;
  }
  
  @Override
  public Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft3SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      FileValue fileValue = findFileValueByPath(Draft3FileValueHelper.getPath(clonedValue));
      if (fileValue == null) {
        return new Draft3PortProcessorResult(value, false);
      }
      if (fileValue != null) {
        clonedValue = Draft3FileValueHelper.createFileRaw(fileValue);
      }
      
      if (fileValue.getSecondaryFiles() != null) {
        List<Map<String, Object>> secondaryFiles = new ArrayList<>();

        for (FileValue secondaryFileValue : fileValue.getSecondaryFiles()) {
          secondaryFiles.add(Draft3FileValueHelper.createFileRaw(secondaryFileValue));
        }
        Draft3FileValueHelper.setSecondaryFiles(secondaryFiles, clonedValue);
      }
      return new Draft3PortProcessorResult(clonedValue, true);
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
