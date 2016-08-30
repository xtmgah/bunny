package org.rabix.bindings.sb.service;

import java.util.Map;

import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.SBOutputPort;
import org.rabix.bindings.sb.expression.SBExpressionException;

public interface SBMetadataService {

  Map<String, Object> processMetadata(SBJob job, Object value, SBOutputPort outputPort, Object outputBinding);
 
  Object evaluateMetadataExpressions(SBJob job, Object self, Object metadata) throws SBExpressionException;
  
}
