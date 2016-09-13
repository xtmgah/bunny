package org.rabix.bindings.cwl1.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.rabix.bindings.cwl1.bean.resource.CWL1Resource;
import org.rabix.bindings.cwl1.bean.resource.CWL1ResourceType;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1CreateFileRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1DockerResource;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1EnvVarRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1InlineJavascriptRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1ResourceRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1SchemaDefRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1ShellCommandRequirement;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.common.json.BeanSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "class", defaultImpl = CWL1EmbeddedApp.class)
@JsonSubTypes({ 
	@Type(value = CWL1CommandLineTool.class, name = "CommandLineTool"),
	@Type(value = CWL1ExpressionTool.class, name = "ExpressionTool"),
    @Type(value = CWL1Workflow.class, name = "Workflow"),
    @Type(value = CWL1WagnerPythonTool.class, name = "WagnerPythonTool")})
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CWL1JobApp implements Application {

  public static final String CWL_1_VERSION = "v1.0";
  
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
  protected List<CWL1InputPort> inputs = new ArrayList<>();
  @JsonProperty("outputs")
  protected List<CWL1OutputPort> outputs = new ArrayList<>();

  @JsonProperty("hints")
  protected List<CWL1Resource> hints = new ArrayList<>();
  @JsonProperty("requirements")
  protected List<CWL1Resource> requirements = new ArrayList<>();
  
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
    CWL1SchemaDefRequirement schemaDefRequirement = lookForResource(CWL1ResourceType.SCHEMA_DEF_REQUIREMENT, CWL1SchemaDefRequirement.class);
    return schemaDefRequirement != null ? schemaDefRequirement.getSchemaDefs() : null;
  }

  @JsonIgnore
  public CWL1DockerResource getContainerResource() throws IllegalArgumentException {
    CWL1DockerResource dockerResource = lookForResource(CWL1ResourceType.DOCKER_RESOURCE, CWL1DockerResource.class);
    if (dockerResource != null) {
      validateDockerRequirement(dockerResource);
    }
    return dockerResource;
  }

  /**
   * Do some basic validation
   */
  private void validateDockerRequirement(CWL1DockerResource requirement) {
    String imageId = requirement.getImageId();
    String dockerPull = requirement.getDockerPull();

    if (StringUtils.isEmpty(dockerPull) && StringUtils.isEmpty(imageId)) {
      throw new IllegalArgumentException("Docker requirements are empty.");
    }
  }

  @JsonIgnore
  public CWL1ResourceRequirement getResourceRequirement() {
    return lookForResource(CWL1ResourceType.RESOURCE_REQUIREMENT, CWL1ResourceRequirement.class);
  }
  
  @JsonIgnore
  public CWL1InlineJavascriptRequirement getInlineJavascriptRequirement() {
    return lookForResource(CWL1ResourceType.INLINE_JAVASCRIPT_REQUIREMENT, CWL1InlineJavascriptRequirement.class);
  }
  
  @JsonIgnore
  public CWL1ShellCommandRequirement getShellCommandRequirement() {
    return lookForResource(CWL1ResourceType.SHELL_COMMAND_REQUIREMENT, CWL1ShellCommandRequirement.class);
  }
  
  @JsonIgnore
  public CWL1EnvVarRequirement getEnvVarRequirement() {
    return lookForResource(CWL1ResourceType.ENV_VAR_REQUIREMENT, CWL1EnvVarRequirement.class);
  }

  @JsonIgnore
  public CWL1CreateFileRequirement getCreateFileRequirement() {
    return lookForResource(CWL1ResourceType.CREATE_FILE_REQUIREMENT, CWL1CreateFileRequirement.class);
  }

  /**
   * Find one resource by type 
   */
  private <T extends CWL1Resource> T lookForResource(CWL1ResourceType type, Class<T> clazz) {
    List<T> resources = lookForResources(type, clazz);
    return resources != null && resources.size() > 0 ? resources.get(0) : null;
  }
  
  /**
   * Find all resources by type 
   */
  private <T extends CWL1Resource> List<T> lookForResources(CWL1ResourceType type, Class<T> clazz) {
    List<T> resources = getRequirements(type, clazz);
    if (resources == null || resources.size() == 0) {
      resources = getHints(type, clazz);
    }
    return resources;
  }
  
  @JsonIgnore
  private <T extends CWL1Resource> List<T> getRequirements(CWL1ResourceType type, Class<T> clazz) {
    if (requirements == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (CWL1Resource requirement : requirements) {
      if (type.equals(requirement.getType())) {
        result.add(clazz.cast(requirement));
      }
    }
    return result;
  }

  @JsonIgnore
  private <T extends CWL1Resource> List<T> getHints(CWL1ResourceType type, Class<T> clazz) {
    if (hints == null) {
      return null;
    }
    List<T> result = new ArrayList<>();
    for (CWL1Resource hint : hints) {
      if (type.equals(hint.getType())) {
        result.add(clazz.cast(hint));
      }
    }
    return result;
  }

  public ApplicationPort getPort(String id, Class<? extends ApplicationPort> clazz) {
    if (CWL1InputPort.class.equals(clazz)) {
      return getInput(id);
    }
    if (CWL1OutputPort.class.equals(clazz)) {
      return getOutput(id);
    }
    return null;
  }

  @JsonIgnore
  public CWL1InputPort getInput(String id) {
    if (getInputs() == null) {
      return null;
    }
    for (CWL1InputPort input : getInputs()) {
      if (input.getId().toString().equals(id) || input.getId().equals(id)) {
        return input;
      }
    }
    return null;
  }

  @JsonIgnore
  public CWL1OutputPort getOutput(String id) {
    if (getOutputs() == null) {
      return null;
    }
    for (CWL1OutputPort output : getOutputs()) {
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

  public List<CWL1InputPort> getInputs() {
    return inputs;
  }

  public List<CWL1OutputPort> getOutputs() {
    return outputs;
  }

  public List<CWL1Resource> getRequirements() {
    return requirements;
  }

  public List<CWL1Resource> getHints() {
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
    return CWL1JobAppType.WORKFLOW.equals(getType());
  }

  @JsonIgnore
  public boolean isCommandLineTool() {
    return CWL1JobAppType.COMMAND_LINE_TOOL.equals(getType());
  }
  
  @JsonIgnore
  public boolean isEmbedded() {
    return CWL1JobAppType.EMBEDDED.equals(getType());
  }
  
  @JsonIgnore
  public boolean isExpressionTool() {
    return CWL1JobAppType.EXPRESSION_TOOL.equals(getType());
  }
  
  @Override
  public String serialize() {
    return BeanSerializer.serializeFull(this);
  }
  
  public abstract CWL1JobAppType getType();

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
    CWL1JobApp other = (CWL1JobApp) obj;
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
    return "JobApp [id=" + id + ", context=" + context + ", description=" + description + ", label=" + label
        + ", contributor=" + contributor + ", owner=" + owner + ", hints=" + hints + ", inputs=" + inputs + ", outputs="
        + outputs + ", requirements=" + requirements + "]";
  }

}
