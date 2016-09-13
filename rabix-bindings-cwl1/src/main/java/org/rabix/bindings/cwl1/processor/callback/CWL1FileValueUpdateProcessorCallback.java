package org.rabix.bindings.cwl1.processor.callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.cwl1.helper.CWL1FileValueHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorCallback;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.common.helper.CloneHelper;

public class CWL1FileValueUpdateProcessorCallback implements CWL1PortProcessorCallback {

  private Set<FileValue> fileValues;

  public CWL1FileValueUpdateProcessorCallback(Set<FileValue> fileValues) {
    this.fileValues = fileValues;
  }
  
  @Override
  public CWL1PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWL1SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      FileValue fileValue = findFileValueByPath(CWL1FileValueHelper.getPath(clonedValue));
      if (fileValue == null) {
        return new CWL1PortProcessorResult(value, false);
      }
      if (fileValue != null) {
        clonedValue = CWL1FileValueHelper.createFileRaw(fileValue);
      }
      
      if (fileValue.getSecondaryFiles() != null) {
        List<Map<String, Object>> secondaryFiles = new ArrayList<>();

        for (FileValue secondaryFileValue : fileValue.getSecondaryFiles()) {
          secondaryFiles.add(CWL1FileValueHelper.createFileRaw(secondaryFileValue));
        }
        CWL1FileValueHelper.setSecondaryFiles(secondaryFiles, clonedValue);
      }
      return new CWL1PortProcessorResult(clonedValue, true);
    }
    return new CWL1PortProcessorResult(value, false);
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
