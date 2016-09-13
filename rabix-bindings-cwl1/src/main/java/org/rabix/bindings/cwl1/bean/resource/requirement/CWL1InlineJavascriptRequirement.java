package org.rabix.bindings.cwl1.bean.resource.requirement;

import java.util.List;

import org.rabix.bindings.cwl1.bean.resource.CWL1Resource;
import org.rabix.bindings.cwl1.bean.resource.CWL1ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CWL1InlineJavascriptRequirement extends CWL1Resource {

  public final static String KEY_EXPRESSION_LIB = "expressionLib";

  @JsonIgnore
  public List<String> getExpressionLib() {
    return getValue(KEY_EXPRESSION_LIB);
  }

  @Override
  public CWL1ResourceType getType() {
    return CWL1ResourceType.INLINE_JAVASCRIPT_REQUIREMENT;
  }

}
