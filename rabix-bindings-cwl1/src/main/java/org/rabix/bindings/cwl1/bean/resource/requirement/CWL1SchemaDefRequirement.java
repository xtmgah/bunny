package org.rabix.bindings.cwl1.bean.resource.requirement;

import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl1.bean.resource.CWL1Resource;
import org.rabix.bindings.cwl1.bean.resource.CWL1ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CWL1SchemaDefRequirement extends CWL1Resource {

  public final static String KEY_SCHEMA_DEFS = "types";

  @JsonIgnore
  public List<Map<String, Object>> getSchemaDefs() {
    return this.<List<Map<String, Object>>> getValue(KEY_SCHEMA_DEFS);
  }
  
  @Override
  @JsonIgnore
  public CWL1ResourceType getType() {
    return CWL1ResourceType.SCHEMA_DEF_REQUIREMENT;
  }
  
  @Override
  public String toString() {
    return "SchemaDefRequirement [" + raw + "]";
  }
  
}
