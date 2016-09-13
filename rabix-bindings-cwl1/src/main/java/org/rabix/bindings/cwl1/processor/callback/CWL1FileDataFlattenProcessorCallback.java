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

public class CWL1FileDataFlattenProcessorCallback implements CWL1PortProcessorCallback {

  private final Set<Map<String, Object>> flattenedFileData;

  protected CWL1FileDataFlattenProcessorCallback() {
    this.flattenedFileData = new HashSet<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public CWL1PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWL1SchemaHelper.isFileFromValue(value)) {
      flattenedFileData.add((Map<String, Object>) value);

      List<Map<String, Object>> secondaryFiles = CWL1FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          flattenedFileData.add(secondaryFileValue);
        }
      }
      return new CWL1PortProcessorResult(value, true);
    }
    return new CWL1PortProcessorResult(value, false);
  }

  public Set<Map<String, Object>> getFlattenedFileData() {
    return flattenedFileData;
  }

}
