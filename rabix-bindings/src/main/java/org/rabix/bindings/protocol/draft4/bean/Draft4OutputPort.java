package org.rabix.bindings.protocol.draft4.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.protocol.draft4.helper.Draft4SchemaHelper;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Draft4OutputPort extends ApplicationPort {

  @JsonProperty("outputBinding")
  protected Object outputBinding;
  @JsonProperty("source")
  protected Object source;

  @JsonCreator
  public Draft4OutputPort(@JsonProperty("id") String id, @JsonProperty("default") Object defaultValue,
      @JsonProperty("type") Object schema, @JsonProperty("outputBinding") Object outputBinding,
      @JsonProperty("scatter") Boolean scatter, @JsonProperty("source") Object source, @JsonProperty("linkMerge") String linkMerge) {
    super(id, defaultValue, schema, scatter, linkMerge);
    this.outputBinding = outputBinding;
    this.source = source;
  }

  @Override
  @JsonIgnore
  public boolean isList() {
    return Draft4SchemaHelper.isArrayFromSchema(schema);
  }
  
  public Object getOutputBinding() {
    return outputBinding;
  }

  public Object getSource() {
    return source;
  }
  
  public static class Draft4OutputPortListDeserializer extends JsonDeserializer<List<Draft4OutputPort>> {
    @Override
    public List<Draft4OutputPort> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      List<Draft4OutputPort> ports = new ArrayList<>();

      JsonNode node = p.getCodec().readTree(p);
      if (node.isArray()) {
        for (JsonNode subnode : node) {
          Draft4OutputPort draft4OutputPort = JSONHelper.readObject(subnode, Draft4OutputPort.class);
          ports.add(draft4OutputPort);
        }
        return ports;
      } else if (node.isObject()) {
        Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
        while (iterator.hasNext()) {
          Map.Entry<String, JsonNode> subnodeEntry = iterator.next();
          Draft4OutputPort draft4OutputPort = JSONHelper.readObject(subnodeEntry.getValue(), Draft4OutputPort.class);
          draft4OutputPort.setId(subnodeEntry.getKey());
          ports.add(draft4OutputPort);
        }
        return ports;
      }
      return ports;
    }
  }

  @Override
  public String toString() {
    return "OutputPort [outputBinding=" + outputBinding + ", id=" + getId() + ", schema=" + getSchema() + ", scatter="
        + getScatter() + ", source=" + source + "]";
  }

}
