package org.rabix.bindings.protocol.draft4.processor.callback;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.protocol.draft4.helper.Draft4FileValueHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4SchemaHelper;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorCallback;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorResult;
import org.rabix.common.helper.CloneHelper;

public class Draft4FileSizeProcessorCallback implements Draft4PortProcessorCallback {

  @Override
  public Draft4PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft4SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      String path = Draft4FileValueHelper.getPath(clonedValue);
      Draft4FileValueHelper.setSize(new File(path).length(), clonedValue);

      List<Map<String, Object>> secondaryFiles = Draft4FileValueHelper.getSecondaryFiles(clonedValue);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          String secondaryFilePath = Draft4FileValueHelper.getPath(secondaryFileValue);
          Draft4FileValueHelper.setSize(new File(secondaryFilePath).length(), secondaryFileValue);
        }
      }
      return new Draft4PortProcessorResult(clonedValue, true);
    }
    return new Draft4PortProcessorResult(value, false);
  }

}
