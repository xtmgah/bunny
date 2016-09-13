package org.rabix.bindings.cwl1.processor.callback;

import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl1.helper.CWL1FileValueHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorCallback;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorException;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorResult;
import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class CWL1FilePathMapProcessorCallback implements CWL1PortProcessorCallback {

  private final FileMapper filePathMapper;
  private final Map<String, Object> config;

  public CWL1FilePathMapProcessorCallback(FileMapper filePathMapper, Map<String, Object> config) {
    this.config = config;
    this.filePathMapper = filePathMapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public CWL1PortProcessorResult process(Object value, ApplicationPort port) throws CWL1PortProcessorException {
    if (value == null) {
      return new CWL1PortProcessorResult(value, false);
    }
    try {
      Object clonedValue = CloneHelper.deepCopy(value);
      
      if (CWL1SchemaHelper.isFileFromValue(clonedValue)) {
        Map<String, Object> valueMap = (Map<String, Object>) clonedValue;
        String path = CWL1FileValueHelper.getPath(valueMap);

        if (path != null && filePathMapper != null) {
          CWL1FileValueHelper.setPath(filePathMapper.map(path, config), valueMap);

          List<Map<String, Object>> secondaryFiles = CWL1FileValueHelper.getSecondaryFiles(valueMap);

          if (secondaryFiles != null) {
            for (Map<String, Object> secondaryFile : secondaryFiles) {
              String secondaryFilePath = CWL1FileValueHelper.getPath(secondaryFile);
              CWL1FileValueHelper.setPath(filePathMapper.map(secondaryFilePath, config), secondaryFile);
            }
          }
          return new CWL1PortProcessorResult(valueMap, true);
        }
      }
      return new CWL1PortProcessorResult(clonedValue, false);
    } catch (Exception e) {
      throw new CWL1PortProcessorException(e);
    }
    
  }

}
