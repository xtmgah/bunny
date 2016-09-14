package org.rabix.bindings.draft2.processor.callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.common.helper.CloneHelper;

public class Draft2FileValueUpdateProcessorCallback implements Draft2PortProcessorCallback {

  private Set<FileValue> fileValues;

  public Draft2FileValueUpdateProcessorCallback(Set<FileValue> fileValues) {
    this.fileValues = fileValues;
  }
  
  @Override
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft2SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      FileValue fileValue = findFileValueByPath(Draft2FileValueHelper.getPath(clonedValue));
      if (fileValue == null) {
        return new Draft2PortProcessorResult(value, false);
      }
      if (fileValue != null) {
        clonedValue = Draft2FileValueHelper.createFileRaw(fileValue);
      }
      
      if (fileValue.getSecondaryFiles() != null) {
        List<Map<String, Object>> secondaryFiles = new ArrayList<>();

        for (FileValue secondaryFileValue : fileValue.getSecondaryFiles()) {
          secondaryFiles.add(Draft2FileValueHelper.createFileRaw(secondaryFileValue));
        }
        Draft2FileValueHelper.setSecondaryFiles(secondaryFiles, clonedValue);
      }
      return new Draft2PortProcessorResult(clonedValue, true);
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
