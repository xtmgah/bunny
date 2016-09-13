package org.rabix.bindings;


public enum ProtocolType {
  DRAFT2("org.rabix.bindings.draft2.Draft2Bindings", 3),
  SB("org.rabix.bindings.sb.SBBindings", 2),
  DRAFT3("org.rabix.bindings.draft3.Draft3Bindings", 1),
  CWL1("org.rabix.bindings.cwl1.CWL1Bindings", 0);

  public final int order;
  public final String bindingsClass;

  private ProtocolType(String bindingsClass, int order) {
    this.order = order;
    this.bindingsClass = bindingsClass;
  }

}
