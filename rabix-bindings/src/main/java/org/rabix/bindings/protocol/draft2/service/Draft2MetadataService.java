package org.rabix.bindings.protocol.draft2.service;

import java.util.Map;

import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.bindings.protocol.draft2.bean.Draft2OutputPort;
import org.rabix.bindings.protocol.draft2.expression.Draft2ExpressionException;

public interface Draft2MetadataService {

  Map<String, Object> processMetadata(Draft2Job job, Object value, Draft2OutputPort outputPort, Object outputBinding);
 
  Object evaluateMetadataExpressions(Draft2Job job, Object self, Object metadata) throws Draft2ExpressionException;
  
}
