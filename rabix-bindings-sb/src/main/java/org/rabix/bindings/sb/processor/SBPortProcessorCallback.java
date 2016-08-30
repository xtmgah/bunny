package org.rabix.bindings.sb.processor;

import org.rabix.bindings.model.ApplicationPort;

public interface SBPortProcessorCallback {

  SBPortProcessorResult process(Object value, ApplicationPort port) throws Exception;
  
}
