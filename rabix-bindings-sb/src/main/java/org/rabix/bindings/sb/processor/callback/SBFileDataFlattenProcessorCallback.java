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

public class SBFileDataFlattenProcessorCallback implements SBPortProcessorCallback {

  private final Set<Map<String, Object>> flattenedFileData;

  protected SBFileDataFlattenProcessorCallback() {
    this.flattenedFileData = new HashSet<>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public SBPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value)) {
      flattenedFileData.add((Map<String, Object>) value);

      List<Map<String, Object>> secondaryFiles = SBFileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          flattenedFileData.add(secondaryFileValue);
        }
      }
      return new SBPortProcessorResult(value, true);
    }
    return new SBPortProcessorResult(value, false);
  }

  public Set<Map<String, Object>> getFlattenedFileData() {
    return flattenedFileData;
  }

}
