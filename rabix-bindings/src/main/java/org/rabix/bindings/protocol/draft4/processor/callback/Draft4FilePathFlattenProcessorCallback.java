package org.rabix.bindings.protocol.draft4.processor.callback;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.protocol.draft4.helper.Draft4FileValueHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4SchemaHelper;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorCallback;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorResult;

class Draft4FilePathFlattenProcessorCallback implements Draft4PortProcessorCallback {

  private Set<String> flattenedPaths;

  protected Draft4FilePathFlattenProcessorCallback() {
    this.flattenedPaths = new HashSet<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Draft4PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft4SchemaHelper.isFileFromValue(value)) {
      Map<String, Object> valueMap = (Map<String, Object>) value;
      flattenedPaths.add(Draft4FileValueHelper.getPath(valueMap).trim());

      List<Map<String, Object>> secondaryFiles = Draft4FileValueHelper.getSecondaryFiles(valueMap);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          flattenedPaths.add(Draft4FileValueHelper.getPath(secondaryFileValue).trim());
        }
      }
      return new Draft4PortProcessorResult(value, true);
    }
    return new Draft4PortProcessorResult(value, false);
  }

  public Set<String> getFlattenedPaths() {
    return flattenedPaths;
  }
}
