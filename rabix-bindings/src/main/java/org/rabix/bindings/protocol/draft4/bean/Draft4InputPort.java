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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Draft4InputPort extends ApplicationPort {

  public static enum StageInput {
    COPY("copy"),
    LINK("link");

    private String value;

    private StageInput(String value) {
      this.value = value;
    }

    public static StageInput get(String value) {
      Preconditions.checkNotNull(value);
      for (StageInput stageInput : values()) {
        if (value.compareToIgnoreCase(stageInput.value) == 0) {
          return stageInput;
        }
      }
      throw new IllegalArgumentException("Wrong stageInput value " + value);
    }
  }

  @JsonProperty("format")
  protected Object format;
  @JsonProperty("streamable")
  protected Boolean streamable;

  @JsonProperty("sbg:stageInput")
  protected final String stageInput;
  @JsonProperty("inputBinding")
  protected final Object inputBinding;

  @JsonCreator
  public Draft4InputPort(@JsonProperty("id") String id, @JsonProperty("default") Object defaultValue,
      @JsonProperty("type") Object schema, @JsonProperty("inputBinding") Object inputBinding,
      @JsonProperty("streamable") Boolean streamable, @JsonProperty("format") Object format,
      @JsonProperty("scatter") Boolean scatter, @JsonProperty("sbg:stageInput") String stageInput,
      @JsonProperty("linkMerge") String linkMerge) {
    super(id, defaultValue, schema, scatter, linkMerge);
    this.format = format;
    this.streamable = streamable;
    this.stageInput = stageInput;
    this.inputBinding = inputBinding;
  }

  @Override
  @JsonIgnore
  public boolean isList() {
    return Draft4SchemaHelper.isArrayFromSchema(schema);
  }

  public Object getInputBinding() {
    return inputBinding;
  }

  public String getStageInput() {
    return stageInput;
  }

  public Boolean getStreamable() {
    return streamable;
  }

  public Object getFormat() {
    return format;
  }
  
  public static class Draft4InputPortListDeserializer extends JsonDeserializer<List<Draft4InputPort>> {
    @Override
    public List<Draft4InputPort> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      List<Draft4InputPort> ports = new ArrayList<>();

      JsonNode node = p.getCodec().readTree(p);
      if (node.isArray()) {
        for (JsonNode subnode : node) {
          Draft4InputPort draft4InputPort = JSONHelper.readObject(subnode, Draft4InputPort.class);
          ports.add(draft4InputPort);
        }
        return ports;
      } else if (node.isObject()) {
        Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
        while (iterator.hasNext()) {
          Map.Entry<String, JsonNode> subnodeEntry = iterator.next();
          Draft4InputPort draft4InputPort = JSONHelper.readObject(subnodeEntry.getValue(), Draft4InputPort.class);
          draft4InputPort.setId(subnodeEntry.getKey());
          ports.add(draft4InputPort);
        }
        return ports;
      }
      return ports;
    }
  }

  @Override
  public String toString() {
    return "Draft4InputPort [inputBinding=" + inputBinding + ", id=" + getId() + ", schema=" + getSchema() + ", scatter=" + getScatter() + "]";
  }

}
