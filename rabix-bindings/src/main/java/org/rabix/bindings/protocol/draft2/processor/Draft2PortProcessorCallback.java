package org.rabix.bindings.protocol.draft2.processor;

import org.rabix.bindings.protocol.draft2.bean.Draft2Port;

public interface Draft2PortProcessorCallback {

  Draft2PortProcessorResult process(Object value, Draft2Port port) throws Exception;
  
}
