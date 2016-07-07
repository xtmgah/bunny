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

public class Draft4FileDataFlattenProcessorCallback implements Draft4PortProcessorCallback {

  private final Set<Map<String, Object>> flattenedFileData;

  protected Draft4FileDataFlattenProcessorCallback() {
    this.flattenedFileData = new HashSet<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Draft4PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft4SchemaHelper.isFileFromValue(value)) {
      flattenedFileData.add((Map<String, Object>) value);

      List<Map<String, Object>> secondaryFiles = Draft4FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          flattenedFileData.add(secondaryFileValue);
        }
      }
      return new Draft4PortProcessorResult(value, true);
    }
    return new Draft4PortProcessorResult(value, false);
  }

  public Set<Map<String, Object>> getFlattenedFileData() {
    return flattenedFileData;
  }

}
