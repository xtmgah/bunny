package org.rabix.bindings.cwl1.bean.resource.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.bean.resource.CWL1Resource;
import org.rabix.bindings.cwl1.bean.resource.CWL1ResourceType;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionException;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

public class CWL1CreateFileRequirement extends CWL1Resource {

  public final static String KEY_FILE_DEF = "fileDef";
  public final static String KEY_FILENAME = "filename";
  public final static String KEY_FILE_CONTENT = "fileContent";

  @JsonIgnore
  public List<CWL1FileRequirement> getFileRequirements() {
    List<Map<String, Object>> fileDefs = getValue(KEY_FILE_DEF);

    if (fileDefs == null) {
      return null;
    }

    List<CWL1FileRequirement> fileRequirements = new ArrayList<>();

    for (Map<String, Object> fileDef : fileDefs) {
      Object filename = getFilename(fileDef);
      Object content = getFileContent(fileDef);
      fileRequirements.add(new CWL1FileRequirement(filename, content));
    }
    return fileRequirements;
  }

  @JsonIgnore
  private Object getFilename(Map<String, Object> fileDef) {
    Preconditions.checkNotNull(fileDef);
    return fileDef.get(KEY_FILENAME);
  }

  @JsonIgnore
  private Object getFileContent(Map<String, Object> fileDef) {
    Preconditions.checkNotNull(fileDef);
    return fileDef.get(KEY_FILE_CONTENT);
  }

  /**
   * Single file requirement
   */
  public class CWL1FileRequirement {
    private Object filename;
    private Object content;

    public CWL1FileRequirement(Object filename, Object content) {
      this.filename = filename;
      this.content = content;
    }

    public Object getContent(CWL1Job job) throws CWL1ExpressionException {
      return CWL1ExpressionResolver.resolve(content, job, null);
    }

    public Object getFilename(CWL1Job job) throws CWL1ExpressionException {
      return CWL1ExpressionResolver.resolve(filename, job, null);
    }

    @Override
    public String toString() {
      return "CWL1FileRequirement [filename=" + filename + ", content=" + content + "]";
    }

  }

  @Override
  @JsonIgnore
  public CWL1ResourceType getType() {
    return CWL1ResourceType.CREATE_FILE_REQUIREMENT;
  }

  @Override
  public String toString() {
    return "CWL1CreateFileRequirement [" + raw + "]";
  }
}
