package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.Draft2ResultCollector;

public class ResultCollectorFactory {

  public static ResultCollector create(ProtocolType type) throws BindingException {
    switch (type) {
      case DRAFT2:
        return new Draft2ResultCollector();
      default:
        throw new BindingException("There is no ResultCollector for type " + type);
    }
  }

}
