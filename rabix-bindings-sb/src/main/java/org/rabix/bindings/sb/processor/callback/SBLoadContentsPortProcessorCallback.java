package org.rabix.bindings.sb.processor.callback;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.sb.bean.SBInputPort;
import org.rabix.bindings.sb.helper.SBBindingHelper;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;
import org.rabix.common.helper.CloneHelper;

public class SBLoadContentsPortProcessorCallback implements SBPortProcessorCallback {

  @Override
  public SBPortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (SBSchemaHelper.isFileFromValue(value) && port instanceof SBInputPort) {
      Object clonedValue = CloneHelper.deepCopy(value);
      
      Object inputBinding = ((SBInputPort) port).getInputBinding();
      if (inputBinding == null) {
        return new SBPortProcessorResult(clonedValue, true);
      }

      boolean loadContents = SBBindingHelper.loadContents(inputBinding);
      if (loadContents) {
        SBFileValueHelper.setContents(clonedValue);
        return new SBPortProcessorResult(clonedValue, true);
      }
    }
    return new SBPortProcessorResult(value, false);
  }

}
