package org.rabix.bindings.protocol.draft3.bean.resource.requirement;

import org.rabix.bindings.protocol.draft3.bean.Draft3Job;
import org.rabix.bindings.protocol.draft3.bean.resource.Draft3Resource;
import org.rabix.bindings.protocol.draft3.bean.resource.Draft3ResourceType;
import org.rabix.bindings.protocol.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.protocol.draft3.expression.Draft3ExpressionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Draft3ResourceRequirement extends Draft3Resource {

  public final static String KEY_CORES_MIN = "coresMin";
  public final static String KEY_CORES_MAX = "coresMax";
  public final static String KEY_RAM_MIN = "ramMin";
  public final static String KEY_RAM_MAX = "ramMax";
  public final static String KEY_TMPDIR_MIN = "tmpdirMin";
  public final static String KEY_TMPDIR_MAX = "tmpdirMax";
  public final static String KEY_OUTDIR_MIN = "outdirMin";
  public final static String KEY_OUTDIR_MAX = "outdirMax";

  @JsonIgnore
  public Long getCoresMin(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_CORES_MIN, Long.class);
  }

  @JsonIgnore
  public Integer getCoresMax(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_CORES_MAX, Integer.class);
  }

  @JsonIgnore
  public Long getRamMin(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_RAM_MIN, Long.class);
  }

  @JsonIgnore
  public Long getRamMax(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_RAM_MAX, Long.class);
  }

  @JsonIgnore
  public Long getTmpdirMin(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_TMPDIR_MIN, Long.class);
  }

  @JsonIgnore
  public Long getTmpdirMax(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_TMPDIR_MAX, Long.class);
  }
  
  @JsonIgnore
  public Long getOutdirMin(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_OUTDIR_MIN, Long.class);
  }
  
  @JsonIgnore
  public Long getOutdirMax(Draft3Job job) throws Draft3ExpressionException {
    return getValue(job, KEY_OUTDIR_MAX, Long.class);
  }

  @JsonIgnore
  public <T> T getValue(Draft3Job job, String key, Class<T> clazz) throws Draft3ExpressionException {
    Object value = getValue(key);
    value = Draft3ExpressionResolver.evaluate((String) value); // TODO
    if (!clazz.isInstance(value)) {
      throw new Draft3ExpressionException(value + " couldn't be cast to " + clazz.getName());
    }
    return clazz.cast(value);
  }

  @Override
  public Draft3ResourceType getType() {
    return Draft3ResourceType.RESOURCE_REQUIREMENT;
  }

}
