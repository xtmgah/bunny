package org.rabix.bindings.protocol.draft2.bean;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Draft2Port {

  public static final String KEY_SCHEMA = "type";

  @JsonProperty("id")
  protected String id;
  
  @JsonProperty("type")
  protected Object schema;
  
  @JsonProperty("scatter")
  protected Boolean scatter;
  
  protected Map<String, Object> raw = new HashMap<>();

  @JsonCreator
  public Draft2Port(@JsonProperty("id") String id, @JsonProperty("type") Object schema, @JsonProperty("scatter") Boolean scatter) {
    this.id = id;
    this.schema = schema;
    this.scatter = scatter;
  }

  @JsonAnySetter
  public void add(String key, Object value) {
    raw.put(key, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getRaw() {
    return raw;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public Object getSchema() {
    return schema;
  }

  public Boolean getScatter() {
    return scatter;
  }

  public void setScatter(Boolean scatter) {
    this.scatter = scatter;
  }

}
