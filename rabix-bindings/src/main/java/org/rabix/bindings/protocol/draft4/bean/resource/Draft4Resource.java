package org.rabix.bindings.protocol.draft4.bean.resource;

import java.util.HashMap;
import java.util.Map;

import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4CreateFileRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4DockerResource;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4EnvVarRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4InlineJavascriptRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4ResourceRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4SchemaDefRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4ShellCommandRequirement;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = Draft4Resource.class)
@JsonSubTypes({ @Type(value = Draft4DockerResource.class, name = "DockerRequirement"),
    @Type(value = Draft4InlineJavascriptRequirement.class, name = "InlineJavascriptRequirement"),
    @Type(value = Draft4ShellCommandRequirement.class, name = "ShellCommandRequirement"),
    @Type(value = Draft4ResourceRequirement.class, name = "ResourceRequirement"),
    @Type(value = Draft4SchemaDefRequirement.class, name = "SchemaDefRequirement"),
    @Type(value = Draft4CreateFileRequirement.class, name = "CreateFileRequirement"),
    @Type(value = Draft4EnvVarRequirement.class, name = "EnvVarRequirement") })
@JsonInclude(Include.NON_NULL)
public class Draft4Resource {
  protected Map<String, Object> raw = new HashMap<>();

  public Draft4Resource() {
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
  public Draft4ResourceType getType() {
    return Draft4ResourceType.OTHER;
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
    Draft4Resource other = (Draft4Resource) obj;
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
