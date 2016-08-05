package org.rabix.bindings.draft2.processor.callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.draft2.processor.Draft2PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class Draft2MetadataCallback implements Draft2PortProcessorCallback {

  private Map<String, Map<String, Object>> pathToMetadata = new HashMap<>();
  private Map<String, Map<String, Object>> outputMetadata = new HashMap<>();
  
  public Draft2MetadataCallback(Map<String, Object> inputs) {
    mapPathsToMetadata(inputs);
  }
  
  /**
   * Extract paths from unknown data
   */
  public void mapPathsToMetadata(Object value) {
    if (value == null) {
      return;
    } else if (Draft2SchemaHelper.isFileFromValue(value)) {
      String path = Draft2FileValueHelper.getPath(value);
      if (!pathToMetadata.containsKey(path)) {
        pathToMetadata.put(path, Draft2FileValueHelper.getMetadata(value));
      }

      List<Map<String, Object>> secondaryFiles = Draft2FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        for (Object subvalue : secondaryFiles) {
          String subpath = Draft2FileValueHelper.getPath(subvalue);
          if (!pathToMetadata.containsKey(subpath)) {
            pathToMetadata.put(subpath, Draft2FileValueHelper.getMetadata(subvalue));
          }
        }
      }
    } else if (value instanceof List<?>) {
      for (Object subvalue : ((List<?>) value)) {
        mapPathsToMetadata(subvalue);
      }
    } else if (value instanceof Map<?, ?>) {
      for (Object subvalue : ((Map<?, ?>) value).values()) {
        mapPathsToMetadata(subvalue);
      }
    }
  }
  
  @Override
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft2SchemaHelper.isFileFromValue(value)) {
      Object clonedValue = CloneHelper.deepCopy(value);
      String path = Draft2FileValueHelper.getPath(clonedValue);
      if (pathToMetadata.containsKey(path)) {
        Map<String, Object> metadata = Draft2FileValueHelper.getMetadata(clonedValue);
        Map<String, Object> newMetadata = pathToMetadata.get(path);
        if (metadata != null) {
          newMetadata.putAll(metadata);
        }
        Draft2FileValueHelper.setMetadata(newMetadata, clonedValue);
      }
      outputMetadata.put(path, Draft2FileValueHelper.getMetadata(clonedValue));

      List<Map<String, Object>> secondaryFiles = Draft2FileValueHelper.getSecondaryFiles(clonedValue);
      if (secondaryFiles != null) {
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          String subpath = Draft2FileValueHelper.getPath(secondaryFileValue);
          
          if (pathToMetadata.containsKey(subpath)) {
            Map<String, Object> metadata = Draft2FileValueHelper.getMetadata(secondaryFileValue);
            Map<String, Object> newMetadata = pathToMetadata.get(path);
            if (metadata != null) {
              newMetadata.putAll(metadata);
            }
            Draft2FileValueHelper.setMetadata(newMetadata, secondaryFileValue);
          }
          Map<String, Object> metadata = Draft2FileValueHelper.getMetadata(secondaryFileValue);
          if ((metadata == null || metadata.isEmpty()) && outputMetadata.containsKey(subpath)) {
            Draft2FileValueHelper.setMetadata(outputMetadata.get(subpath), secondaryFileValue);
          }
        }
      }
      return new Draft2PortProcessorResult(clonedValue, true);
    }
    return new Draft2PortProcessorResult(value, false);
  }
  
}
