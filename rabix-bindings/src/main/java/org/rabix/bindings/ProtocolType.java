package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.Draft2Bindings;
import org.rabix.bindings.protocol.draft3.Draft3Bindings;
import org.rabix.bindings.protocol.draft4.Draft4Bindings;
import org.rabix.bindings.protocol.zero.ZeroBindings;

public enum ProtocolType {
  DRAFT2(Draft2Bindings.class, 3),
  DRAFT3(Draft3Bindings.class, 2),
  DRAFT4(Draft4Bindings.class, 1),
  ZERO(ZeroBindings.class, 0);

  public final int order;
  public final Class<? extends Bindings> bindingsClass;

  private ProtocolType(Class<? extends Bindings> bindingsClass, int order) {
    this.order = order;
    this.bindingsClass = bindingsClass;
  }

}
