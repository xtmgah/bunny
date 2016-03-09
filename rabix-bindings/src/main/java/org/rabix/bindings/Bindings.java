package org.rabix.bindings;

import org.rabix.bindings.model.Executable;

public interface Bindings extends CommandLineBuilder, ProtocolValueOperator, ProtocolProcessor, ProtocolTranslator, RequirementProvider, ResultCollector {

  /**
   * Gets native application 
   */
  Object getApp(Executable executable) throws BindingException;

  /**
   * Returns {@link ProtocolType} 
   */
  ProtocolType getProtocolType();

}
