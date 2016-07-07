package org.rabix.bindings.protocol.draft4.bean.resource.requirement;

import java.util.List;
import java.util.Map;

import org.rabix.bindings.protocol.draft4.bean.resource.Draft4Resource;
import org.rabix.bindings.protocol.draft4.bean.resource.Draft4ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft4SchemaDefRequirement extends Draft4Resource {

  public final static String KEY_SCHEMA_DEFS = "types";

  @JsonIgnore
  public List<Map<String, Object>> getSchemaDefs() {
    return this.<List<Map<String, Object>>> getValue(KEY_SCHEMA_DEFS);
  }
  
  @Override
  @JsonIgnore
  public Draft4ResourceType getType() {
    return Draft4ResourceType.SCHEMA_DEF_REQUIREMENT;
  }
  
  @Override
  public String toString() {
    return "SchemaDefRequirement [" + raw + "]";
  }
  
}
