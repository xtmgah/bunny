package org.rabix.bindings.sb.processor.callback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;

public class SBFileValueProcessorCallback implements SBPortProcessorCallback {

  private final Set<String> visiblePorts;
  private final Set<FileValue> fileValues;

  protected SBFileValueProcessorCallback(Set<String> visiblePorts) {
    this.visiblePorts = visiblePorts;
    this.fileValues = new HashSet<>();
  }
  
  @Override
  public SBPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value) && !skip(port.getId())) {
      FileValue fileValue = SBFileValueHelper.createFileValue(value);
      
      List<Map<String, Object>> secondaryFiles = SBFileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        List<FileValue> secondaryFileValues = new ArrayList<>();
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          secondaryFileValues.add(SBFileValueHelper.createFileValue(secondaryFileValue));
        }
        fileValue = FileValue.cloneWithSecondaryFiles(fileValue, secondaryFileValues);
      }
      fileValues.add(fileValue);
      return new SBPortProcessorResult(value, true);
    }
    return new SBPortProcessorResult(value, false);
  }

  private boolean skip(String portId) {
    return visiblePorts != null && !visiblePorts.contains(SBSchemaHelper.normalizeId(portId));
  }

  public Set<FileValue> getFileValues() {
    return fileValues;
  }
}
