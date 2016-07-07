package org.rabix.bindings.protocol.draft4.processor;

import org.rabix.bindings.model.ApplicationPort;

public interface Draft4PortProcessorCallback {

  Draft4PortProcessorResult process(Object value, ApplicationPort port) throws Exception;
  
}
