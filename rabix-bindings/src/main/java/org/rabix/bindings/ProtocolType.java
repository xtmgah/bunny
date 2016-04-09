package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.Draft2Bindings;
import org.rabix.bindings.protocol.rabix.RabixBindings;

public enum ProtocolType {
  DRAFT2(Draft2Bindings.class),
  RABIX(RabixBindings.class);

  private Class<? extends Bindings> bindingsClass;

  private ProtocolType(Class<? extends Bindings> bindingsClass) {
    this.bindingsClass = bindingsClass;
  }

  public Class<? extends Bindings> getBindingsClass() {
    return bindingsClass;
  }
}
