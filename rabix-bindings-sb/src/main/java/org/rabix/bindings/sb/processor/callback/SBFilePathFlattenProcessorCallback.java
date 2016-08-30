package org.rabix.bindings.sb.processor.callback;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;

class SBFilePathFlattenProcessorCallback implements SBPortProcessorCallback {

  private Set<String> flattenedPaths;

  protected SBFilePathFlattenProcessorCallback() {
    this.flattenedPaths = new HashSet<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public SBPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value)) {
      Map<String, Object> valueMap = (Map<String, Object>) value;
      flattenedPaths.add(SBFileValueHelper.getPath(valueMap).trim());

      List<Map<String, Object>> secondaryFiles = SBFileValueHelper.getSecondaryFiles(valueMap);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          flattenedPaths.add(SBFileValueHelper.getPath(secondaryFileValue).trim());
        }
      }
      return new SBPortProcessorResult(value, true);
    }
    return new SBPortProcessorResult(value, false);
  }

  public Set<String> getFlattenedPaths() {
    return flattenedPaths;
  }
}
