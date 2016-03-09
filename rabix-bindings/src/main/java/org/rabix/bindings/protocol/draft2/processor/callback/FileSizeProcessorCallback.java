package org.rabix.bindings.protocol.draft2.processor.callback;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.protocol.draft2.bean.Draft2Port;
import org.rabix.bindings.protocol.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.protocol.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessorResult;
import org.rabix.common.helper.CloneHelper;

public class FileSizeProcessorCallback implements Draft2PortProcessorCallback {

  @Override
  public Draft2PortProcessorResult process(Object value, Draft2Port port) throws Exception {
    if (Draft2SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      String path = Draft2FileValueHelper.getPath(clonedValue);
      Draft2FileValueHelper.setSize(new File(path).length(), clonedValue);

      List<Map<String, Object>> secondaryFiles = Draft2FileValueHelper.getSecondaryFiles(clonedValue);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          String secondaryFilePath = Draft2FileValueHelper.getPath(secondaryFileValue);
          Draft2FileValueHelper.setSize(new File(secondaryFilePath).length(), secondaryFileValue);
        }
      }
      return new Draft2PortProcessorResult(clonedValue, true);
    }
    return new Draft2PortProcessorResult(value, false);
  }

}
