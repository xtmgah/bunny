package org.rabix.bindings.protocol.draft4.processor.callback;

import java.util.List;
import java.util.Map;

import org.rabix.bindings.filemapper.FileMapper;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.protocol.draft4.helper.Draft4FileValueHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4SchemaHelper;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorCallback;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorException;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorResult;
import org.rabix.common.helper.CloneHelper;

public class Draft4FilePathMapProcessorCallback implements Draft4PortProcessorCallback {

  private final FileMapper filePathMapper;

  public Draft4FilePathMapProcessorCallback(FileMapper filePathMapper) {
    this.filePathMapper = filePathMapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Draft4PortProcessorResult process(Object value, ApplicationPort port) throws Draft4PortProcessorException {
    if (value == null) {
      return new Draft4PortProcessorResult(value, false);
    }
    try {
      Object clonedValue = CloneHelper.deepCopy(value);
      
      if (Draft4SchemaHelper.isFileFromValue(clonedValue)) {
        Map<String, Object> valueMap = (Map<String, Object>) clonedValue;
        String path = Draft4FileValueHelper.getPath(valueMap);

        if (path != null && filePathMapper != null) {
          Draft4FileValueHelper.setPath(filePathMapper.map(path), valueMap);

          List<Map<String, Object>> secondaryFiles = Draft4FileValueHelper.getSecondaryFiles(valueMap);

          if (secondaryFiles != null) {
            for (Map<String, Object> secondaryFile : secondaryFiles) {
              String secondaryFilePath = Draft4FileValueHelper.getPath(secondaryFile);
              Draft4FileValueHelper.setPath(filePathMapper.map(secondaryFilePath), secondaryFile);
            }
          }
          return new Draft4PortProcessorResult(valueMap, true);
        }
      }
      return new Draft4PortProcessorResult(clonedValue, false);
    } catch (Exception e) {
      throw new Draft4PortProcessorException(e);
    }
    
  }

}
