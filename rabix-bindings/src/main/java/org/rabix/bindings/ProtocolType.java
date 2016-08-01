package org.rabix.bindings;


public enum ProtocolType {
  DRAFT2("org.rabix.bindings.draft2.Draft2Bindings", 2),
  DRAFT3("org.rabix.bindings.protocol.draft3.Draft3Bindings", 1);

  public final int order;
  public final String bindingsClass;

  private ProtocolType(String bindingsClass, int order) {
    this.order = order;
    this.bindingsClass = bindingsClass;
  }

}
