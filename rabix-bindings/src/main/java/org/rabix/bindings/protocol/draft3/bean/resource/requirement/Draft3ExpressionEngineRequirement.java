package org.rabix.bindings.protocol.draft3.bean.resource.requirement;

import java.util.List;

import org.rabix.bindings.protocol.draft3.bean.resource.Draft3Resource;
import org.rabix.bindings.protocol.draft3.bean.resource.Draft3ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft3ExpressionEngineRequirement extends Draft3Resource {

  public final static int DEFAULT_VALUE = 0;

  public final static String KEY_ID = "id";
  public final static String KEY_ENGINE_CONFIG = "engineConfig";
  
  @JsonIgnore
  public String getId() {
    return getValue(KEY_ID);
  }  
  
  @JsonIgnore
  public List<String> getEngineConfigs(String engineId) {
    if (engineId.equals(getId())) {
      return getValue(KEY_ENGINE_CONFIG);
    }
    return null;
  }
  
  @Override
  @JsonIgnore
  public Draft3ResourceType getType() {
    return Draft3ResourceType.EXPRESSION_ENGINE_REQUIREMENT;
  }

  @Override
  public String toString() {
    return "ExpressionEngineRequirement [" + raw + "]";
  }

}
