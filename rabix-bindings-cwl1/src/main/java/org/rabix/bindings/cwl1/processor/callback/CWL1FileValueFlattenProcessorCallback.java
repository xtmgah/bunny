package org.rabix.bindings.cwl1.processor.callback;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.cwl1.helper.CWL1FileValueHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorCallback;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;

public class CWL1FileValueFlattenProcessorCallback implements CWL1PortProcessorCallback {

  private final Set<String> visiblePorts;
  private final Set<FileValue> fileValues;

  protected CWL1FileValueFlattenProcessorCallback(Set<String> visiblePorts) {
    this.visiblePorts = visiblePorts;
    this.fileValues = new HashSet<>();
  }

  @Override
  public CWL1PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWL1SchemaHelper.isFileFromValue(value) && !skip(port.getId())) {
      fileValues.add(CWL1FileValueHelper.createFileValue(value));
      
      List<Map<String, Object>> secondaryFiles = CWL1FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          fileValues.add(CWL1FileValueHelper.createFileValue(secondaryFileValue));
        }
      }
      return new CWL1PortProcessorResult(value, true);
    }
    return new CWL1PortProcessorResult(value, false);
  }
  
  private boolean skip(String portId) {
    return visiblePorts != null && !visiblePorts.contains(CWL1SchemaHelper.normalizeId(portId));
  }

  public Set<FileValue> getFlattenedFileData() {
    return fileValues;
  }

}
