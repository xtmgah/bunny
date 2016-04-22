package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.Draft2Bindings;
import org.rabix.bindings.protocol.draft3.Draft3Bindings;
import org.rabix.bindings.protocol.zero.ZeroBindings;

public enum ProtocolType {
  DRAFT2(Draft2Bindings.class),
  DRAFT3(Draft3Bindings.class),
  ZERO(ZeroBindings.class);

  private Class<? extends Bindings> bindingsClass;

  private ProtocolType(Class<? extends Bindings> bindingsClass) {
    this.bindingsClass = bindingsClass;
  }

  public Class<? extends Bindings> getBindingsClass() {
    return bindingsClass;
  }
}
