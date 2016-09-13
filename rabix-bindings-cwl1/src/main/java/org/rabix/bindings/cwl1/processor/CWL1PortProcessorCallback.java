package org.rabix.bindings.cwl1.processor;

import org.rabix.bindings.model.ApplicationPort;

public interface CWL1PortProcessorCallback {

  CWL1PortProcessorResult process(Object value, ApplicationPort port) throws Exception;
  
}
