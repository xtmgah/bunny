package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.Draft2ProtocolValueExtractor;

public class ProtocolValueOperatorFactory {

  public static ProtocolValueOperator create(ProtocolType type) throws BindingException {
    switch (type) {
      case DRAFT2:
        return new Draft2ProtocolValueExtractor();
      default:
        throw new BindingException("There is no ProtocolValueOperator for type " + type);
    }
  }

}
