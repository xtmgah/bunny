package org.rabix.bindings.protocol.draft2.processor;

import org.rabix.bindings.model.ApplicationPort;

public interface Draft2PortProcessorCallback {

  Draft2PortProcessorResult process(Object value, ApplicationPort port) throws Exception;
  
}
