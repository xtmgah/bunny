package org.rabix.bindings.protocol.draft4.processor.callback;

import java.util.HashSet;
import java.util.Set;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.protocol.draft4.helper.Draft4FileValueHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4SchemaHelper;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorCallback;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorResult;

public class Draft4FileValueFlattenProcessorCallback implements Draft4PortProcessorCallback {

  private final Set<FileValue> fileValues;

  protected Draft4FileValueFlattenProcessorCallback() {
    this.fileValues = new HashSet<>();
  }

  @Override
  public Draft4PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft4SchemaHelper.isFileFromValue(value)) {
      fileValues.add(Draft4FileValueHelper.createFileValue(value));
      return new Draft4PortProcessorResult(value, true);
    }
    return new Draft4PortProcessorResult(value, false);
  }

  public Set<FileValue> getFlattenedFileData() {
    return fileValues;
  }

}
