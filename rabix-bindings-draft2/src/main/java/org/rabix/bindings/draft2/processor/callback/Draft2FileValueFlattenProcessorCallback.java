package org.rabix.bindings.draft2.processor.callback;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;

public class Draft2FileValueFlattenProcessorCallback implements Draft2PortProcessorCallback {

  private final Set<String> visiblePorts;
  private final Set<FileValue> fileValues;

  protected Draft2FileValueFlattenProcessorCallback(Set<String> visiblePorts) {
    this.visiblePorts = visiblePorts;
    this.fileValues = new HashSet<>();
  }

  @Override
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft2SchemaHelper.isFileFromValue(value) && !skip(port.getId())) {
      fileValues.add(Draft2FileValueHelper.createFileValue(value));
      
      List<Map<String, Object>> secondaryFiles = Draft2FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          fileValues.add(Draft2FileValueHelper.createFileValue(secondaryFileValue));
        }
      }
      return new Draft2PortProcessorResult(value, true);
    }
    return new Draft2PortProcessorResult(value, false);
  }
  
  private boolean skip(String portId) {
    return visiblePorts != null && !visiblePorts.contains(Draft2SchemaHelper.normalizeId(portId));
  }

  public Set<FileValue> getFlattenedFileData() {
    return fileValues;
  }

}
