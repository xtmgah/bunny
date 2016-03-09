package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.Draft2ProtocolProcessor;

public class ProtocolProcessorFactory {

  public static ProtocolProcessor create(ProtocolType type) throws BindingException {
    switch (type) {
      case DRAFT2:
        return new Draft2ProtocolProcessor();
      default:
        throw new BindingException("There is no ProtocolProcessor for protocol " + type);
    }
  }

}
