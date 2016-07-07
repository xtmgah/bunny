package org.rabix.bindings.protocol.draft4.processor.callback;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.protocol.draft4.bean.Draft4InputPort;
import org.rabix.bindings.protocol.draft4.helper.Draft4BindingHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4FileValueHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4SchemaHelper;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorCallback;
import org.rabix.bindings.protocol.draft4.processor.Draft4PortProcessorResult;
import org.rabix.common.helper.CloneHelper;

public class Draft4LoadContentsPortProcessorCallback implements Draft4PortProcessorCallback {

  @Override
  public Draft4PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (Draft4SchemaHelper.isFileFromValue(value) && port instanceof Draft4InputPort) {
      Object clonedValue = CloneHelper.deepCopy(value);
      
      Object inputBinding = ((Draft4InputPort) port).getInputBinding();
      if (inputBinding == null) {
        return new Draft4PortProcessorResult(clonedValue, true);
      }

      boolean loadContents = Draft4BindingHelper.loadContents(inputBinding);
      if (loadContents) {
        Draft4FileValueHelper.setContents(clonedValue);
        return new Draft4PortProcessorResult(clonedValue, true);
      }
    }
    return new Draft4PortProcessorResult(value, false);
  }

}
