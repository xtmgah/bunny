package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.Draft2ProtocolTranslator;

public class ProtocolTranslatorFactory {

  public static ProtocolTranslator create(ProtocolType type) throws BindingException {
    switch (type) {
    case DRAFT2:
      return new Draft2ProtocolTranslator();
    default:
      throw new BindingException("There is no ProtocolTranslator for protocol " + type);
    }
  }
  
}
