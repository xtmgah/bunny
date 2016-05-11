package org.rabix.bindings.protocol.draft2.processor.callback;

import java.util.HashSet;
import java.util.Set;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.protocol.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.protocol.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessorResult;

public class Draft2FileValueFlattenProcessorCallback implements Draft2PortProcessorCallback {

  private final Set<FileValue> fileValues;

  protected Draft2FileValueFlattenProcessorCallback() {
    this.fileValues = new HashSet<>();
  }

  @Override
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft2SchemaHelper.isFileFromValue(value)) {
      fileValues.add(Draft2FileValueHelper.createFileValue(value));
      return new Draft2PortProcessorResult(value, true);
    }
    return new Draft2PortProcessorResult(value, false);
  }

  public Set<FileValue> getFlattenedFileData() {
    return fileValues;
  }

}
