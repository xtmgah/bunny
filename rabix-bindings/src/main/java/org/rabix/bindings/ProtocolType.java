package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.Draft2Bindings;

public enum ProtocolType {
  DRAFT2(Draft2Bindings.class);

  private Class<? extends Bindings> bindingsClass;

  private ProtocolType(Class<? extends Bindings> bindingsClass) {
    this.bindingsClass = bindingsClass;
  }

  public Class<? extends Bindings> getBindingsClass() {
    return bindingsClass;
  }
}
