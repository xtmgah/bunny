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

class CWL1FilePathFlattenProcessorCallback implements CWL1PortProcessorCallback {

  private Set<String> flattenedPaths;

  protected CWL1FilePathFlattenProcessorCallback() {
    this.flattenedPaths = new HashSet<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public CWL1PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWL1SchemaHelper.isFileFromValue(value)) {
      Map<String, Object> valueMap = (Map<String, Object>) value;
      flattenedPaths.add(CWL1FileValueHelper.getPath(valueMap).trim());

      List<Map<String, Object>> secondaryFiles = CWL1FileValueHelper.getSecondaryFiles(valueMap);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          flattenedPaths.add(CWL1FileValueHelper.getPath(secondaryFileValue).trim());
        }
      }
      return new CWL1PortProcessorResult(value, true);
    }
    return new CWL1PortProcessorResult(value, false);
  }

  public Set<String> getFlattenedPaths() {
    return flattenedPaths;
  }
}
