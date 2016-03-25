package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.helper.Draft2ProtocolJobHelper;

public class ProtocolJobHelperFactory {

  public static ProtocolJobHelper create(ProtocolType type) throws BindingException {
    switch (type) {
      case DRAFT2:
        return new Draft2ProtocolJobHelper();
      default:
        throw new BindingException("There is no ProtocolJobHelper for protocol " + type);
    }
  }
  
}
