package org.rabix.bindings;

import org.rabix.bindings.protocol.draft2.Draft2DocumentReferenceResolver;

public class DocumentReferenceResolverFactory {

  public static DocumentReferenceResolver create(ProtocolType type) throws BindingException {
    switch (type) {
      case DRAFT2:
        return new Draft2DocumentReferenceResolver();
      default:
        throw new BindingException("There is no DocumentReferenceResolver for protocol " + type);
    }
  }
  
}
