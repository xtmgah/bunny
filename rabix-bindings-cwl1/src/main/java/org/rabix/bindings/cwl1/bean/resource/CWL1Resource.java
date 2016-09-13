package org.rabix.bindings.cwl1.bean.resource;

import java.util.HashMap;
import java.util.Map;

import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1CreateFileRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1DockerResource;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1EnvVarRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1InlineJavascriptRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1ResourceRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1SchemaDefRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1ShellCommandRequirement;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = CWL1Resource.class)
@JsonSubTypes({ @Type(value = CWL1DockerResource.class, name = "DockerRequirement"),
    @Type(value = CWL1InlineJavascriptRequirement.class, name = "InlineJavascriptRequirement"),
    @Type(value = CWL1ShellCommandRequirement.class, name = "ShellCommandRequirement"),
    @Type(value = CWL1ResourceRequirement.class, name = "ResourceRequirement"),
    @Type(value = CWL1SchemaDefRequirement.class, name = "SchemaDefRequirement"),
    @Type(value = CWL1CreateFileRequirement.class, name = "CreateFileRequirement"),
    @Type(value = CWL1EnvVarRequirement.class, name = "EnvVarRequirement") })
@JsonInclude(Include.NON_NULL)
public class CWL1Resource {
  protected Map<String, Object> raw = new HashMap<>();

  public CWL1Resource() {
  }

  @SuppressWarnings("unchecked")
  @JsonIgnore
  public <T> T getValue(String key) {
    if (raw == null) {
      return null;
    }

    Object value = raw.get(key);
    return value != null ? (T) value : null;
  }

  @JsonAnySetter
  public void add(String key, Object value) {
    raw.put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getRaw() {
    return raw;
  }

  @JsonIgnore
  public CWL1ResourceType getType() {
    return CWL1ResourceType.OTHER;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((raw == null) ? 0 : raw.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CWL1Resource other = (CWL1Resource) obj;
    if (raw == null) {
      if (other.raw != null)
        return false;
    } else if (!raw.equals(other.raw))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Hint [" + raw + "]";
  }
}
