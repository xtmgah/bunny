package org.rabix.bindings.cwl1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolRequirementProvider;
import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.bean.CWL1JobApp;
import org.rabix.bindings.cwl1.bean.resource.CWL1Resource;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1CreateFileRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1DockerResource;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1EnvVarRequirement;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1EnvVarRequirement.EnvironmentDef;
import org.rabix.bindings.cwl1.bean.resource.requirement.CWL1ResourceRequirement;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionException;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionResolver;
import org.rabix.bindings.cwl1.helper.CWL1FileValueHelper;
import org.rabix.bindings.cwl1.helper.CWL1JobHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;

public class CWL1RequirementProvider implements ProtocolRequirementProvider {

  private DockerContainerRequirement getDockerRequirement(CWL1DockerResource cwl1DockerResource) {
    if (cwl1DockerResource == null) {
      return null;
    }
    return new DockerContainerRequirement(cwl1DockerResource.getDockerPull(), cwl1DockerResource.getImageId());
  }

  private EnvironmentVariableRequirement getEnvironmentVariableRequirement(CWL1Job cwl1Job, CWL1EnvVarRequirement envVarRequirement) throws BindingException {
    if (envVarRequirement == null) {
      return null;
    }

    List<EnvironmentDef> envDefinitions = envVarRequirement.getEnvironmentDefinitions();
    if (envDefinitions == null) {
      return new EnvironmentVariableRequirement(Collections.<String, String> emptyMap());
    }
    Map<String, String> result = new HashMap<>();
    for (EnvironmentDef envDef : envDefinitions) {
      String key = envDef.getName();
      Object value = envDef.getValue();

      try {
        value = CWL1ExpressionResolver.resolve(value, cwl1Job, null);
      } catch (CWL1ExpressionException e) {
        throw new BindingException(e);
      }
      if (value == null) {
        throw new BindingException("Environment variable for " + key + " is empty.");
      }
      result.put(key, value.toString());
    }
    return new EnvironmentVariableRequirement(result);

  }

  private FileRequirement getFileRequirement(CWL1Job cwl1Job, CWL1CreateFileRequirement createFileRequirement)
      throws BindingException {
    if (createFileRequirement == null) {
      return null;
    }

    List<CWL1CreateFileRequirement.CWL1FileRequirement> fileRequirements = createFileRequirement
        .getFileRequirements();
    if (fileRequirements == null) {
      return null;
    }

    List<SingleFileRequirement> result = new ArrayList<>();
    for (CWL1CreateFileRequirement.CWL1FileRequirement fileRequirement : fileRequirements) {
      try {
        String filename = (String) fileRequirement.getFilename(cwl1Job);

        Object content = fileRequirement.getContent(cwl1Job);

        if (CWL1SchemaHelper.isFileFromValue(content)) {
          FileValue fileValue = CWL1FileValueHelper.createFileValue(content);
          result.add(new FileRequirement.SingleInputFileRequirement(filename, fileValue));
        } else {
          result.add(new FileRequirement.SingleTextFileRequirement(filename, (String) content));
        }
      } catch (CWL1ExpressionException e) {
        throw new BindingException(e);
      }
    }
    return new FileRequirement(result);
  }

  @Override
  public List<Requirement> getRequirements(Job job) throws BindingException {
    CWL1Job cwl1Job = CWL1JobHelper.getCWL1Job(job);
    CWL1JobApp cwl1JobApp = cwl1Job.getApp();
    return convertRequirements(job, cwl1JobApp.getRequirements());
  }

  @Override
  public List<Requirement> getHints(Job job) throws BindingException {
    CWL1Job cwl1Job = CWL1JobHelper.getCWL1Job(job);
    CWL1JobApp cwl1JobApp = cwl1Job.getApp();
    return convertRequirements(job, cwl1JobApp.getHints());
  }

  private List<Requirement> convertRequirements(Job job, List<CWL1Resource> resources) throws BindingException {
    if (resources == null) {
      return Collections.<Requirement> emptyList();
    }
    CWL1Job cwl1Job = CWL1JobHelper.getCWL1Job(job);

    List<Requirement> result = new ArrayList<>();
    for (CWL1Resource cwl1Resource : resources) {
      if (cwl1Resource instanceof CWL1DockerResource) {
        result.add(getDockerRequirement((CWL1DockerResource) cwl1Resource));
        continue;
      }
      if (cwl1Resource instanceof CWL1EnvVarRequirement) {
        result.add(getEnvironmentVariableRequirement(cwl1Job, (CWL1EnvVarRequirement) cwl1Resource));
        continue;
      }
      if (cwl1Resource instanceof CWL1CreateFileRequirement) {
        result.add(getFileRequirement(cwl1Job, (CWL1CreateFileRequirement) cwl1Resource));
        continue;
      }
    }
    return result;
  }

  @Override
  public ResourceRequirement getResourceRequirement(Job job) throws BindingException {
    CWL1Job cwl1Job = CWL1JobHelper.getCWL1Job(job);
    
    CWL1ResourceRequirement cwl1ResourceRequirement = cwl1Job.getApp().getResourceRequirement();

    if (cwl1ResourceRequirement == null) {
      return null;
    }
    try {
      return new ResourceRequirement(cwl1ResourceRequirement.getCoresMin(cwl1Job), null, cwl1ResourceRequirement.getRamMin(cwl1Job), null, null, null, null);
    } catch (CWL1ExpressionException e) {
      throw new BindingException(e);
    }
  }

}
