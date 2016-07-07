package org.rabix.bindings.protocol.draft4.service;

import java.util.Map;

import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4OutputPort;
import org.rabix.bindings.protocol.draft4.expression.Draft4ExpressionException;

public interface Draft4MetadataService {

  Map<String, Object> processMetadata(Draft4Job job, Object value, Draft4OutputPort outputPort, Object outputBinding);
 
  Object evaluateMetadataExpressions(Draft4Job job, Object self, Object metadata) throws Draft4ExpressionException;
  
}
