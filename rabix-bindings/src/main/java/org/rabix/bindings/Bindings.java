package org.rabix.bindings;

import java.io.File;

public interface Bindings extends CommandLineBuilder, ProtocolJobHelper, ProtocolValueOperator, ProtocolProcessor, ProtocolTranslator, RequirementProvider, ResultCollector {

  /**
   * Loads application from the file 
   */
  String loadAppFromFile(File file) throws BindingException;
  
  /**
   * Returns {@link ProtocolType} 
   */
  ProtocolType getProtocolType();

}
