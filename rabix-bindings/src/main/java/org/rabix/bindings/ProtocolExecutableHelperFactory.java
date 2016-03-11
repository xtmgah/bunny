package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.helper.Draft2ProtocolExecutableHelper;

public class ProtocolExecutableHelperFactory {

  public static ProtocolExecutableHelper create(ProtocolType type) throws BindingException {
    switch (type) {
      case DRAFT2:
        return new Draft2ProtocolExecutableHelper();
      default:
        throw new BindingException("There is no ProtocolExecutableHelper for protocol " + type);
    }
  }
  
}
