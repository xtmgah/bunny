package org.rabix.bindings.protocol.draft4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.ProtocolRequirementProvider;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.requirement.DockerContainerRequirement;
import org.rabix.bindings.model.requirement.EnvironmentVariableRequirement;
import org.rabix.bindings.model.requirement.FileRequirement;
import org.rabix.bindings.model.requirement.FileRequirement.SingleFileRequirement;
import org.rabix.bindings.model.requirement.Requirement;
import org.rabix.bindings.model.requirement.ResourceRequirement;
import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4JobApp;
import org.rabix.bindings.protocol.draft4.bean.resource.Draft4Resource;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4CreateFileRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4DockerResource;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4EnvVarRequirement;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4EnvVarRequirement.EnvironmentDef;
import org.rabix.bindings.protocol.draft4.bean.resource.requirement.Draft4ResourceRequirement;
import org.rabix.bindings.protocol.draft4.expression.Draft4ExpressionException;
import org.rabix.bindings.protocol.draft4.expression.Draft4ExpressionResolver;
import org.rabix.bindings.protocol.draft4.helper.Draft4FileValueHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4JobHelper;
import org.rabix.bindings.protocol.draft4.helper.Draft4SchemaHelper;

public class Draft4RequirementProvider implements ProtocolRequirementProvider {

  private DockerContainerRequirement getDockerRequirement(Draft4DockerResource draft4DockerResource) {
    if (draft4DockerResource == null) {
      return null;
    }
    return new DockerContainerRequirement(draft4DockerResource.getDockerPull(), draft4DockerResource.getImageId());
  }

  private EnvironmentVariableRequirement getEnvironmentVariableRequirement(Draft4Job draft4Job,
      Draft4EnvVarRequirement envVarRequirement) throws BindingException {
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
        value = Draft4ExpressionResolver.resolve(value, draft4Job, null);
      } catch (Draft4ExpressionException e) {
        throw new BindingException(e);
      }
      if (value == null) {
        throw new BindingException("Environment variable for " + key + " is empty.");
      }
      result.put(key, value.toString());
    }
    return new EnvironmentVariableRequirement(result);

  }

  private FileRequirement getFileRequirement(Draft4Job draft4Job, Draft4CreateFileRequirement createFileRequirement)
      throws BindingException {
    if (createFileRequirement == null) {
      return null;
    }

    List<Draft4CreateFileRequirement.Draft4FileRequirement> fileRequirements = createFileRequirement
        .getFileRequirements();
    if (fileRequirements == null) {
      return null;
    }

    List<SingleFileRequirement> result = new ArrayList<>();
    for (Draft4CreateFileRequirement.Draft4FileRequirement fileRequirement : fileRequirements) {
      try {
        String filename = (String) fileRequirement.getFilename(draft4Job);

        Object content = fileRequirement.getContent(draft4Job);

        if (Draft4SchemaHelper.isFileFromValue(content)) {
          FileValue fileValue = Draft4FileValueHelper.createFileValue(content);
          result.add(new FileRequirement.SingleInputFileRequirement(filename, fileValue));
        } else {
          result.add(new FileRequirement.SingleTextFileRequirement(filename, (String) content));
        }
      } catch (Draft4ExpressionException e) {
        throw new BindingException(e);
      }
    }
    return new FileRequirement(result);
  }

  @Override
  public List<Requirement> getRequirements(Job job) throws BindingException {
    Draft4Job draft4Job = Draft4JobHelper.getDraft4Job(job);
    Draft4JobApp draft4JobApp = draft4Job.getApp();
    return convertRequirements(job, draft4JobApp.getRequirements());
  }

  @Override
  public List<Requirement> getHints(Job job) throws BindingException {
    Draft4Job draft4Job = Draft4JobHelper.getDraft4Job(job);
    Draft4JobApp draft4JobApp = draft4Job.getApp();
    return convertRequirements(job, draft4JobApp.getHints());
  }

  private List<Requirement> convertRequirements(Job job, List<Draft4Resource> resources) throws BindingException {
    if (resources == null) {
      return Collections.<Requirement> emptyList();
    }
    Draft4Job draft4Job = Draft4JobHelper.getDraft4Job(job);

    List<Requirement> result = new ArrayList<>();
    for (Draft4Resource draft4Resource : resources) {
      if (draft4Resource instanceof Draft4DockerResource) {
        result.add(getDockerRequirement((Draft4DockerResource) draft4Resource));
        continue;
      }
      if (draft4Resource instanceof Draft4EnvVarRequirement) {
        result.add(getEnvironmentVariableRequirement(draft4Job, (Draft4EnvVarRequirement) draft4Resource));
        continue;
      }
      if (draft4Resource instanceof Draft4CreateFileRequirement) {
        result.add(getFileRequirement(draft4Job, (Draft4CreateFileRequirement) draft4Resource));
        continue;
      }
    }
    return result;
  }

  @Override
  public ResourceRequirement getResourceRequirement(Job job) throws BindingException {
    Draft4Job draft4Job = Draft4JobHelper.getDraft4Job(job);
    
    Draft4ResourceRequirement draft4ResourceRequirement = draft4Job.getApp().getResourceRequirement();

    if (draft4ResourceRequirement == null) {
      return null;
    }
    try {
      return new ResourceRequirement(draft4ResourceRequirement.getCoresMin(draft4Job), null, draft4ResourceRequirement.getRamMin(draft4Job), null, null, null, null);
    } catch (Draft4ExpressionException e) {
      throw new BindingException(e);
    }
  }

}
