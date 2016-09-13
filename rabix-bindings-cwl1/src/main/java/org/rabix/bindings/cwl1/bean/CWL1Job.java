package org.rabix.bindings.cwl1.bean;

import java.util.Map;

import org.rabix.bindings.cwl1.CWL1JobProcessor;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.common.json.BeanPropertyView;
import org.rabix.common.json.processor.BeanProcessorClass;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@BeanProcessorClass(name = CWL1JobProcessor.class)
public final class CWL1Job {

  @JsonProperty("id")
  private String id;

  @JsonProperty("app")
  private CWL1JobApp app;

  @JsonProperty("inputs")
  private Map<String, Object> inputs;

  @JsonProperty("outputs")
  private Map<String, Object> outputs;

  @JsonProperty("scatter")
  @JsonView(BeanPropertyView.Full.class)
  private Object scatter;
  
  @JsonProperty("scatterMethod")
  @JsonView(BeanPropertyView.Full.class)
  private String scatterMethod;
  
  @JsonProperty("allocatedResources")
  private CWL1Resources resources;

  @JsonCreator
  public CWL1Job(@JsonProperty("app") CWL1JobApp app,
      @JsonProperty("inputs") Map<String, Object> inputs,
      @JsonProperty("outputs") Map<String, Object> outputs,
      @JsonProperty("allocatedResources") CWL1Resources resources,
      @JsonProperty("id") String id, @JsonProperty("scatter") Object scatter, 
      @JsonProperty("scatterMethod") String scatterMethod) {
    this.id = id;
    this.app = app;
    this.inputs = inputs;
    this.outputs = outputs;
    this.resources = resources;
    this.scatter = scatter;
    this.scatterMethod = scatterMethod;
    processPortDefaults();
  }
  
  private void processPortDefaults() {
    if (inputs == null) {
      return;
    }
    for (CWL1InputPort inputPort : app.getInputs()) {
      String normalizedId = CWL1SchemaHelper.normalizeId(inputPort.getId());
      if (!inputs.containsKey(normalizedId) && inputPort.getDefaultValue() != null) {
        inputs.put(normalizedId, inputPort.getDefaultValue());
      }
    }
  }

  public CWL1Job(CWL1JobApp app, Map<String, Object> inputs, Map<String, Object> outputs, Object scatter, String scatterMethod, String id) {
    this.id = id;
    this.app = app;
    this.scatter = scatter;
    this.inputs = inputs;
    this.outputs = outputs;
    this.scatterMethod = scatterMethod;
    processPortDefaults();
  }
  
  public CWL1Job(CWL1JobApp app, Map<String, Object> inputs) {
    this.app = app;
    this.inputs = inputs;
    processPortDefaults();
  }

  public boolean isInlineJavascriptEnabled() {
    return app.getInlineJavascriptRequirement() != null;
  }
  
  public boolean isShellCommandEscapeEnabled() {
    return app.getShellCommandRequirement() != null;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public CWL1JobApp getApp() {
    return app;
  }

  public Map<String, Object> getInputs() {
    return inputs;
  }

  public Map<String, Object> getOutputs() {
    return outputs;
  }

  public void setResources(CWL1Resources resources) {
    this.resources = resources;
  }
  
  public CWL1Resources getResources() {
    return resources;
  }

  public Object getScatter() {
    return scatter;
  }
  
  public String getScatterMethod() {
    return scatterMethod;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CWL1Job other = (CWL1Job) obj;
    if (app == null) {
      if (other.app != null)
        return false;
    } else if (!app.equals(other.app))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((app == null) ? 0 : app.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Job [id=" + id + ", app=" + app + ", inputs=" + inputs + ", outputs=" + outputs + ", scatter=" + scatter + ", resources=" + resources + "]";
  }

}
