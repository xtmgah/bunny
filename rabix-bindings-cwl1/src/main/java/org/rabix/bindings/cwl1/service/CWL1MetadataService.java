package org.rabix.bindings.cwl1.service;

import java.util.Map;

import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.bean.CWL1OutputPort;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionException;

public interface CWL1MetadataService {

  Map<String, Object> processMetadata(CWL1Job job, Object value, CWL1OutputPort outputPort, Object outputBinding);
 
  Object evaluateMetadataExpressions(CWL1Job job, Object self, Object metadata) throws CWL1ExpressionException;
  
}
