package org.rabix.bindings.protocol.draft4.bean.resource.requirement;

import java.util.List;

import org.rabix.bindings.protocol.draft4.bean.resource.Draft4Resource;
import org.rabix.bindings.protocol.draft4.bean.resource.Draft4ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft4InlineJavascriptRequirement extends Draft4Resource {

  public final static String KEY_EXPRESSION_LIB = "expressionLib";

  @JsonIgnore
  public List<String> getExpressionLib() {
    return getValue(KEY_EXPRESSION_LIB);
  }

  @Override
  public Draft4ResourceType getType() {
    return Draft4ResourceType.INLINE_JAVASCRIPT_REQUIREMENT;
  }

}
