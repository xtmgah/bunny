package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.Draft2RequirementProvider;

public class RequirementProviderFactory {

  public static RequirementProvider create(ProtocolType type) throws BindingException {
    switch (type) {
    case DRAFT2:
      return new Draft2RequirementProvider();
    default:
      throw new BindingException("There is no RequirementProvider for protocol " + type);
    }
  }
  
}
