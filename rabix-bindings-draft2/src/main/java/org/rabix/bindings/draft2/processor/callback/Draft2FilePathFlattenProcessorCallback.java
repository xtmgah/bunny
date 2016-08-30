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

class Draft2FilePathFlattenProcessorCallback implements Draft2PortProcessorCallback {

  private Set<String> flattenedPaths;

  protected Draft2FilePathFlattenProcessorCallback() {
    this.flattenedPaths = new HashSet<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft2SchemaHelper.isFileFromValue(value)) {
      Map<String, Object> valueMap = (Map<String, Object>) value;
      flattenedPaths.add(Draft2FileValueHelper.getPath(valueMap).trim());

      List<Map<String, Object>> secondaryFiles = Draft2FileValueHelper.getSecondaryFiles(valueMap);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          flattenedPaths.add(Draft2FileValueHelper.getPath(secondaryFileValue).trim());
        }
      }
      return new Draft2PortProcessorResult(value, true);
    }
    return new Draft2PortProcessorResult(value, false);
  }

  public Set<String> getFlattenedPaths() {
    return flattenedPaths;
  }
}
