package org.rabix.bindings.sb.processor.callback;

import java.util.HashSet;
import java.util.Set;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;

public class SBFileValueFlattenProcessorCallback implements SBPortProcessorCallback {

  private final Set<FileValue> fileValues;

  protected SBFileValueFlattenProcessorCallback() {
    this.fileValues = new HashSet<>();
  }

  @Override
  public SBPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value)) {
      fileValues.add(SBFileValueHelper.createFileValue(value));
      return new SBPortProcessorResult(value, true);
    }
    return new SBPortProcessorResult(value, false);
  }

  public Set<FileValue> getFlattenedFileData() {
    return fileValues;
  }

}
