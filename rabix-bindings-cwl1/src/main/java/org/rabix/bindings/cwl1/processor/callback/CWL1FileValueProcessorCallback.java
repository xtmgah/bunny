package org.rabix.bindings.cwl1.processor.callback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.cwl1.bean.CWL1InputPort;
import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.bean.CWL1OutputPort;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionResolver;
import org.rabix.bindings.cwl1.helper.CWL1BindingHelper;
import org.rabix.bindings.cwl1.helper.CWL1FileValueHelper;
import org.rabix.bindings.cwl1.helper.CWL1SchemaHelper;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorCallback;
import org.rabix.bindings.cwl1.processor.CWL1PortProcessorResult;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;

public class CWL1FileValueProcessorCallback implements CWL1PortProcessorCallback {

  private final CWL1Job job;
  private final Set<String> visiblePorts;
  private final Set<FileValue> fileValues;
  private final boolean generateSecondaryFilePaths;

  protected CWL1FileValueProcessorCallback(CWL1Job job, Set<String> visiblePorts, boolean generateSecondaryFilePaths) {
    this.job = job;
    this.visiblePorts = visiblePorts;
    this.fileValues = new HashSet<>();
    this.generateSecondaryFilePaths = generateSecondaryFilePaths;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public CWL1PortProcessorResult process(Object value, ApplicationPort port) throws Exception {
    if (CWL1SchemaHelper.isFileFromValue(value) && !skip(port.getId())) {
      FileValue fileValue = CWL1FileValueHelper.createFileValue(value);
      
      List<Map<String, Object>> secondaryFiles = CWL1FileValueHelper.getSecondaryFiles(value);
      if (secondaryFiles != null) {
        List<FileValue> secondaryFileValues = new ArrayList<>();
        for (Map<String, Object> secondaryFileValue : secondaryFiles) {
          secondaryFileValues.add(CWL1FileValueHelper.createFileValue(secondaryFileValue));
        }
        fileValue = FileValue.cloneWithSecondaryFiles(fileValue, secondaryFileValues);
      } else {
        // try to create secondary files
        if (generateSecondaryFilePaths) {
          Object binding = null;
          if (port instanceof CWL1InputPort) {
            binding = ((CWL1InputPort) port).getInputBinding();
          } else {
            binding = ((CWL1OutputPort) port).getOutputBinding();
          }
          List<String> secondaryFileSufixes = (List<String>) CWL1BindingHelper.getSecondaryFiles(binding); // TODO check if it's safe
          if (secondaryFileSufixes != null) {
            List<FileValue> secondaryFileValues = new ArrayList<>();
            for (String suffix : secondaryFileSufixes) {
              String secondaryFilePath = CWL1FileValueHelper.getPath(value);

              if (CWL1ExpressionResolver.isExpressionObject(suffix)) {
                secondaryFilePath = CWL1ExpressionResolver.resolve(suffix, job, value);
              } else {
                while (suffix.startsWith("^")) {
                  int extensionIndex = secondaryFilePath.lastIndexOf(".");
                  if (extensionIndex != -1) {
                    secondaryFilePath = secondaryFilePath.substring(0, extensionIndex);
                    suffix = suffix.substring(1);
                  } else {
                    break;
                  }
                }
                secondaryFilePath += suffix.startsWith(".") ? suffix : "." + suffix;
              }
              secondaryFileValues.add(new FileValue(null, secondaryFilePath, null, null, null, null));
            }
            fileValue = FileValue.cloneWithSecondaryFiles(fileValue, secondaryFileValues);
          }
        }
      }
      fileValues.add(fileValue);
      return new CWL1PortProcessorResult(value, true);
    }
    return new CWL1PortProcessorResult(value, false);
  }

  private boolean skip(String portId) {
    return visiblePorts != null && !visiblePorts.contains(CWL1SchemaHelper.normalizeId(portId));
  }

  public Set<FileValue> getFileValues() {
    return fileValues;
  }
}
