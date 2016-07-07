package org.rabix.bindings.protocol.draft4.bean.resource.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.bindings.protocol.draft4.bean.resource.Draft4Resource;
import org.rabix.bindings.protocol.draft4.bean.resource.Draft4ResourceType;
import org.rabix.bindings.protocol.draft4.expression.Draft4ExpressionException;
import org.rabix.bindings.protocol.draft4.expression.Draft4ExpressionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

public class Draft4CreateFileRequirement extends Draft4Resource {

  public final static String KEY_FILE_DEF = "fileDef";
  public final static String KEY_FILENAME = "filename";
  public final static String KEY_FILE_CONTENT = "fileContent";

  @JsonIgnore
  public List<Draft4FileRequirement> getFileRequirements() {
    List<Map<String, Object>> fileDefs = getValue(KEY_FILE_DEF);

    if (fileDefs == null) {
      return null;
    }

    List<Draft4FileRequirement> fileRequirements = new ArrayList<>();

    for (Map<String, Object> fileDef : fileDefs) {
      Object filename = getFilename(fileDef);
      Object content = getFileContent(fileDef);
      fileRequirements.add(new Draft4FileRequirement(filename, content));
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
  public class Draft4FileRequirement {
    private Object filename;
    private Object content;

    public Draft4FileRequirement(Object filename, Object content) {
      this.filename = filename;
      this.content = content;
    }

    public Object getContent(Draft4Job job) throws Draft4ExpressionException {
      return Draft4ExpressionResolver.resolve(content, job, null);
    }

    public Object getFilename(Draft4Job job) throws Draft4ExpressionException {
      return Draft4ExpressionResolver.resolve(filename, job, null);
    }

    @Override
    public String toString() {
      return "Draft4FileRequirement [filename=" + filename + ", content=" + content + "]";
    }

  }

  @Override
  @JsonIgnore
  public Draft4ResourceType getType() {
    return Draft4ResourceType.CREATE_FILE_REQUIREMENT;
  }

  @Override
  public String toString() {
    return "Draft4CreateFileRequirement [" + raw + "]";
  }
}
