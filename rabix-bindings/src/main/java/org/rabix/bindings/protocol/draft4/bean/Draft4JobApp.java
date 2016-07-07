package org.rabix.bindings.protocol.draft4.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.protocol.draft4.bean.Draft4InputPort.Draft4InputPortListDeserializer;
import org.rabix.bindings.protocol.draft4.bean.Draft4OutputPort.Draft4OutputPortListDeserializer;
import org.rabix.bindings.protocol.draft4.bean.resource.Draft4Resource;
import org.rabix.bindings.protocol.draft4.bean.resource.Draft4ResourceType;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4CreateFileRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4DockerResource;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4EnvVarRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4InlineJavascriptRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4ResourceRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4SchemaDefRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4ShellCommandRequirement;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = Draft4EmbeddedApp.class)
@JsonSubTypes({ 
	@Type(value = Draft4CommandLineTool.class, name = "CommandLineTool"),
	@Type(value = Draft4ExpressionTool.class, name = "ExpressionTool"),
    @Type(value = Draft4Workflow.class, name = "Workflow"),
    @Type(value = Draft4WagnerPythonTool.class, name = "WagnerPythonTool")})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Draft4JobApp implements Application {

  public static final String DRAFT_4_VERSION = "cwl:draft-4";
  
  @JsonProperty("id")
  protected String id;
  @JsonProperty("@context")
  protected String context;
  @JsonProperty("cwlVersion")
  protected String cwlVersion;
  @JsonProperty("description")
  protected String description;
  @JsonProperty("label")
  protected String label;
  @JsonProperty("contributor")
  protected List<String> contributor = new ArrayList<>();
  @JsonProperty("owner")
  protected List<String> owner = new ArrayList<>();

  @JsonProperty("inputs")
  @JsonDeserialize(using = Draft4InputPortListDeserializer.class)
  protected List<Draft4InputPort> inputs = new ArrayList<>();
  @JsonProperty("outputs")
  @JsonDeserialize(using = Draft4OutputPortListDeserializer.class)
  protected List<Draft4OutputPort> outputs = new ArrayList<>();

  @JsonProperty("hints")
  protected List<Draft4Resource> hints = new ArrayList<>();
  @JsonProperty("requirements")
  protected List<Draft4Resource> requirements = new ArrayList<>();
  
  @JsonProperty("successCodes")
  protected List<Integer> successCodes = new ArrayList<>();

  public String getId() {
    return id;
  }
  
  public String getCwlVersion() {
    return cwlVersion;
  }
  
  public List<Integer> getSuccessCodes() {
    return successCodes;
  }

  @JsonIgnore
  public List<Map<String, Object>> getSchemaDefs() {
    Draft4SchemaDefRequirement schemaDefRequirement = lookForResource(Draft4ResourceType.SCHEMA_DEF_REQUIREMENT, Draft4SchemaDefRequirement.class);
    return schemaDefRequirement != null ? schemaDefRequirement.getSchemaDefs() : null;
  }

  @JsonIgnore
  public Draft4DockerResource getContainerResource() throws IllegalArgumentException {
    Draft4DockerResource dockerResource = lookForResource(Draft4ResourceType.DOCKER_RESOURCE, Draft4DockerResource.class);
    if (dockerResource != null) {
      validateDockerRequirement(dockerResource);
    }
    return dockerResource;
  }

  /**
   * Do some basic validation
   */
  private void validateDockerRequirement(Draft4DockerResource requirement) {
    String imageId = requirement.getImageId();
    String dockerPull = requirement.getDockerPull();

    if (StringUtils.isEmpty(dockerPull) && StringUtils.isEmpty(imageId)) {
      throw new IllegalArgumentException("Docker requirements are empty.");
    }
  }

  @JsonIgnore
  public Draft4ResourceRequirement getResourceRequirement() {
    return lookForResource(Draft4ResourceType.RESOURCE_REQUIREMENT, Draft4ResourceRequirement.class);
  }
  
  @JsonIgnore
  public Draft4InlineJavascriptRequirement getInlineJavascriptRequirement() {
    return lookForResource(Draft4ResourceType.INLINE_JAVASCRIPT_REQUIREMENT, Draft4InlineJavascriptRequirement.class);
  }
  
  @JsonIgnore
  public Draft4ShellCommandRequirement getShellCommandRequirement() {
    return lookForResource(Draft4ResourceType.SHELL_COMMAND_REQUIREMENT, Draft4ShellCommandRequirement.class);
  }
  
  @JsonIgnore
  public Draft4EnvVarRequirement getEnvVarRequirement() {
    return lookForResource(Draft4ResourceType.ENV_VAR_REQUIREMENT, Draft4EnvVarRequirement.class);
  }

  @JsonIgnore
  public Draft4CreateFileRequirement getCreateFileRequirement() {
    return lookForResource(Draft4ResourceType.CREATE_FILE_REQUIREMENT, Draft4CreateFileRequirement.class);
  }

  /**
   * Find one resource by type 
   */
  private <T extends Draft4Resource> T lookForResource(Draft4ResourceType type, Class<T> clazz) {
    List<T> resources = lookForResources(type, clazz);
    return resources != null && resources.size() > 0 ? resources.get(0) : null;
  }
  
  /**
   * Find all resources by type 
   */
  private <T extends Draft4Resource> List<T> lookForResources(Draft4ResourceType type, Class<T> clazz) {
    List<T> resources = getRequirements(type, clazz);
    if (resources == null || resources.size() == 0) {
      resources = getHints(type, clazz);
    }
    return resources;
  }
  
  @JsonIgnore
  private <T extends Draft4Resource> List<T> getRequirements(Draft4ResourceType type, Class<T> clazz) {
    if (requirements == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (Draft4Resource requirement : requirements) {
      if (type.equals(requirement.getType())) {
        result.add(clazz.cast(requirement));
      }
    }
    return result;
  }

  @JsonIgnore
  private <T extends Draft4Resource> List<T> getHints(Draft4ResourceType type, Class<T> clazz) {
    if (hints == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (Draft4Resource hint : hints) {
      if (type.equals(hint.getType())) {
        result.add(clazz.cast(hint));
      }
    }
    return result;
  }

  public ApplicationPort getPort(String id, Class<? extends ApplicationPort> clazz) {
    if (Draft4InputPort.class.equals(clazz)) {
      return getInput(id);
    }
    if (Draft4OutputPort.class.equals(clazz)) {
      return getOutput(id);
    }
    return null;
  }

  @JsonIgnore
  public Draft4InputPort getInput(String id) {
    if (getInputs() == null) {
      return null;
    }
    for (Draft4InputPort input : getInputs()) {
      if (input.getId().toString().equals(id) || input.getId().equals(id)) {
        return input;
      }
    }
    return null;
  }

  @JsonIgnore
  public Draft4OutputPort getOutput(String id) {
    if (getOutputs() == null) {
      return null;
    }
    for (Draft4OutputPort output : getOutputs()) {
      if (output.getId().toString().equals(id) || output.getId().equals(id)) {
        return output;
      }
    }
    return null;
  }

  public String getContext() {
    return context;
  }

  public String getDescription() {
    return description;
  }

  public List<Draft4InputPort> getInputs() {
    return inputs;
  }

  public List<Draft4OutputPort> getOutputs() {
    return outputs;
  }

  public List<Draft4Resource> getRequirements() {
    return requirements;
  }

  public List<Draft4Resource> getHints() {
    return hints;
  }

  public String getLabel() {
    return label;
  }

  public List<String> getContributor() {
    return contributor;
  }

  public List<String> getOwner() {
    return owner;
  }

  @JsonIgnore
  public boolean isWorkflow() {
    return Draft4JobAppType.WORKFLOW.equals(getType());
  }

  @JsonIgnore
  public boolean isCommandLineTool() {
    return Draft4JobAppType.COMMAND_LINE_TOOL.equals(getType());
  }
  
  @JsonIgnore
  public boolean isEmbedded() {
    return Draft4JobAppType.EMBEDDED.equals(getType());
  }
  
  @JsonIgnore
  public boolean isExpressionTool() {
    return Draft4JobAppType.EXPRESSION_TOOL.equals(getType());
  }
  
  @Override
  public String serialize() {
    return BeanSerializer.serializeFull(this);
  }
  
  public abstract Draft4JobAppType getType();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((context == null) ? 0 : context.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((hints == null) ? 0 : hints.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    Draft4JobApp other = (Draft4JobApp) obj;
    if (context == null) {
      if (other.context != null)
        return false;
    } else if (!context.equals(other.context))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (hints == null) {
      if (other.hints != null)
        return false;
    } else if (!hints.equals(other.hints))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "JobApp [id=" + id + ", context=" + context + ", description=" + description + ", label=" + label + ", contributor=" + contributor + ", owner=" + owner + ", hints=" + hints + ", inputs=" + inputs + ", outputs=" + outputs + ", requirements=" + requirements + "]";
  }

}
