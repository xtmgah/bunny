package org.rabix.bindings.protocol.draft3.processor.callback;

import java.util.HashSet;
import java.util.Set;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.protocol.draft3.helper.Draft3FileValueHelper;
import org.rabix.bindings.protocol.draft3.helper.Draft3SchemaHelper;
import org.rabix.bindings.protocol.draft3.processor.Draft3PortProcessorCallback;
import org.rabix.bindings.protocol.draft3.processor.Draft3PortProcessorResult;

public class Draft3FileValueFlattenProcessorCallback implements Draft3PortProcessorCallback {

  private final Set<FileValue> fileValues;

  protected Draft3FileValueFlattenProcessorCallback() {
    this.fileValues = new HashSet<>();
  }

  @Override
  public Draft3PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft3SchemaHelper.isFileFromValue(value)) {
      fileValues.add(Draft3FileValueHelper.createFileValue(value));
      return new Draft3PortProcessorResult(value, true);
    }
    return new Draft3PortProcessorResult(value, false);
  }

  public Set<FileValue> getFlattenedFileData() {
    return fileValues;
  }

}
