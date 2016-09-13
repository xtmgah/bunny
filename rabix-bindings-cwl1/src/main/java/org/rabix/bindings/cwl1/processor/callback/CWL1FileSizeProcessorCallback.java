package org.rabix.bindings.cwl1.processor.callback;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl1.helper.CWL1FileValueHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorCallback;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class CWL1FileSizeProcessorCallback implements CWL1PortProcessorCallback {

  @Override
  public CWL1PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWL1SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);

      String path = CWL1FileValueHelper.getPath(clonedValue);
      CWL1FileValueHelper.setSize(new File(path).length(), clonedValue);

      List<Map<String, Object>> secondaryFiles = CWL1FileValueHelper.getSecondaryFiles(clonedValue);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          String secondaryFilePath = CWL1FileValueHelper.getPath(secondaryFileValue);
          CWL1FileValueHelper.setSize(new File(secondaryFilePath).length(), secondaryFileValue);
        }
      }
      return new CWL1PortProcessorResult(clonedValue, true);
    }
    return new CWL1PortProcessorResult(value, false);
  }

}
