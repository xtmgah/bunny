package org.rabix.bindings.model;

import java.io.IOException;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.common.helper.JSONHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public interface Application {

  @JsonIgnore
  String serialize();

  @JsonIgnore
  ApplicationPort getInput(String name);
  
  @JsonIgnore
  ApplicationPort getOutput(String name);
  
  @JsonIgnore
  List<? extends ApplicationPort> getInputs();
  
  @JsonIgnore
  List<? extends ApplicationPort> getOutputs();
  
  public static class ApplicationDeserializer extends JsonDeserializer<Application> {
    @Override
    public Application deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonNode tree = p.getCodec().readTree(p);
      if (tree.isNull()) {
        return null;
      }
      String appUrl = URIHelper.createDataURI(JSONHelper.writeObject(tree));
      try {
        return BindingsFactory.create(appUrl).loadAppObject(appUrl);
      } catch (BindingException e) {
        throw new IOException("Failed to deserialize Application " + tree);
      }
    }
  }
  
  public static class ApplicationSerializer extends JsonSerializer<Application> {
    @Override
    public void serialize(Application value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
      if (value == null) {
        gen.writeNull();
      }
      JsonNode node = JSONHelper.readJsonNode(value.serialize());
      gen.writeTree(node);
    }
  }
}
