package org.rabix.bindings.cwl1.processor.callback;

import org.rabix.bindings.cwl1.bean.CWL1InputPort;
import org.rabix.bindings.cwl1.helper.CWL1BindingHelper;
import org.rabix.bindings.cwl1.helper.CWL1FileValueHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorCallback;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.helper.CloneHelper;

public class CWL1LoadContentsPortProcessorCallback implements CWL1PortProcessorCallback {

  @Override
  public CWL1PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWL1SchemaHelper.isFileFromValue(value) && port instanceof CWL1InputPort) {
      Object clonedValue = CloneHelper.deepCopy(value);
      
      Object inputBinding = ((CWL1InputPort) port).getInputBinding();
      if (inputBinding == null) {
        return new CWL1PortProcessorResult(clonedValue, true);
      }

      boolean loadContents = CWL1BindingHelper.loadContents(inputBinding);
      if (loadContents) {
        CWL1FileValueHelper.setContents(clonedValue);
        return new CWL1PortProcessorResult(clonedValue, true);
      }
    }
    return new CWL1PortProcessorResult(value, false);
  }

}
