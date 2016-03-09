package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.Draft2CommandLineBuilder;

public class CommandLineBuilderFactory {

  public static CommandLineBuilder create(ProtocolType type) throws BindingException {
    switch (type) {
      case DRAFT2:
        return new Draft2CommandLineBuilder();
      default:
        throw new BindingException("There is no CommandLineBuilder for type " + type);
    }
  }

}
