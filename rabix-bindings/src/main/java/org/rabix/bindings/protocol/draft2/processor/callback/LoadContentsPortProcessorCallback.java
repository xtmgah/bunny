package org.rabix.bindings.protocol.draft2.processor.callback;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.protocol.draft2.bean.Draft2InputPort;
import org.rabix.bindings.protocol.draft2.helper.Draft2BindingHelper;
import org.rabix.bindings.protocol.draft2.helper.Draft2FileValueHelper;
import org.rabix.bindings.protocol.draft2.helper.Draft2SchemaHelper;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessorCallback;
import org.rabix.bindings.protocol.draft2.processor.Draft2PortProcessorResult;
import org.rabix.common.helper.CloneHelper;

public class LoadContentsPortProcessorCallback implements Draft2PortProcessorCallback {

  @Override
  public Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft2SchemaHelper.isFileFromValue(value) && port instanceof Draft2InputPort) {
      Object clonedValue = CloneHelper.deepCopy(value);
      
      Object inputBinding = ((Draft2InputPort) port).getInputBinding();
      if (inputBinding == null) {
        return new Draft2PortProcessorResult(clonedValue, true);
      }

      boolean loadContents = Draft2BindingHelper.loadContents(inputBinding);
      if (loadContents) {
        Draft2FileValueHelper.setContents(clonedValue);
        return new Draft2PortProcessorResult(clonedValue, true);
      }
    }
    return new Draft2PortProcessorResult(value, false);
  }

}
