package org.rabix.bindings;

import java.io.File;

public interface Bindings extends CommandLineBuilder, ProtocolJobHelper, ProtocolValueOperator, ProtocolProcessor, ProtocolTranslator, RequirementProvider, ResultCollector {

  /**
   * Loads application from the file 
   */
  String loadApp(String appURI) throws BindingException;
  
  /**
   * Returns {@link ProtocolType} 
   */
  ProtocolType getProtocolType();

}
