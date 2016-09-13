package org.rabix.bindings.cwl1.bean.resource.requirement;

import java.util.HashMap;
import java.util.Map;

import org.rabix.bindings.cwl1.bean.CWL1Job;
import org.rabix.bindings.cwl1.bean.resource.CWL1Resource;
import org.rabix.bindings.cwl1.bean.resource.CWL1ResourceType;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionException;
import org.rabix.bindings.cwl1.expression.CWL1ExpressionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CWL1ResourceRequirement extends CWL1Resource {

  public final static Long CORES_MIN_DEFAULT = 1L;
  public final static Long CORES_MAX_DEFAULT = 1L;
  public final static Long RAM_MIN_DEFAULT = 1024L;
  public final static Long RAM_MAX_DEFAULT = 1024L;
  public final static Long TMPDIR_MIN_DEFAULT = 1024L;
  public final static Long TMPDIR_MAX_DEFAULT = 1024L;
  public final static Long OUTDIR_MIN_DEFAULT = 1024L;
  public final static Long OUTDIR_MAX_DEFAULT = 1024L;
  
  public final static String KEY_CORES_MIN = "coresMin";
  public final static String KEY_CORES_MAX = "coresMax";
  public final static String KEY_RAM_MIN = "ramMin";
  public final static String KEY_RAM_MAX = "ramMax";
  public final static String KEY_TMPDIR_MIN = "tmpdirMin";
  public final static String KEY_TMPDIR_MAX = "tmpdirMax";
  public final static String KEY_OUTDIR_MIN = "outdirMin";
  public final static String KEY_OUTDIR_MAX = "outdirMax";

  @JsonIgnore
  public Long getCoresMin(CWL1Job job) throws CWL1ExpressionException {
    return getValue(job, KEY_CORES_MIN);
  }

  @JsonIgnore
  public Long getCoresMax(CWL1Job job) throws CWL1ExpressionException {
    return getValue(job, KEY_CORES_MAX);
  }

  @JsonIgnore
  public Long getRamMin(CWL1Job job) throws CWL1ExpressionException {
    return getValue(job, KEY_RAM_MIN);
  }

  @JsonIgnore
  public Long getRamMax(CWL1Job job) throws CWL1ExpressionException {
    return getValue(job, KEY_RAM_MAX);
  }

  @JsonIgnore
  public Long getTmpdirMin(CWL1Job job) throws CWL1ExpressionException {
    return getValue(job, KEY_TMPDIR_MIN);
  }

  @JsonIgnore
  public Long getTmpdirMax(CWL1Job job) throws CWL1ExpressionException {
    return getValue(job, KEY_TMPDIR_MAX);
  }
  
  @JsonIgnore
  public Long getOutdirMin(CWL1Job job) throws CWL1ExpressionException {
    return getValue(job, KEY_OUTDIR_MIN);
  }
  
  @JsonIgnore
  public Long getOutdirMax(CWL1Job job) throws CWL1ExpressionException {
    return getValue(job, KEY_OUTDIR_MAX);
  }

  @JsonIgnore
  public Long getValue(CWL1Job job, String key) throws CWL1ExpressionException {
    Object value = getValue(key);
    value = CWL1ExpressionResolver.resolve(value, job, null);
    if (value == null) {
      return null;
    }
    if (value instanceof Integer) {
      return Long.parseLong(Integer.toString((int) value));
    }
    return (Long) value;
  }
  
  @JsonIgnore
  public Resources build(CWL1Job job) throws CWL1ExpressionException {
    Long coresMin = getCoresMin(job);
    Long coresMax = getCoresMax(job);

    Long cores = coresMin != null ? coresMin : coresMax;
    if (cores == null) {
      cores = CORES_MIN_DEFAULT;
    }

    Long ramMin = getRamMin(job);
    Long ramMax = getRamMax(job);

    Long ram = ramMin != null ? ramMin : ramMax;
    if (ram == null) {
      ram = RAM_MIN_DEFAULT;
    }

    Long tmpdirMin = getTmpdirMin(job);
    Long tmpdirMax = getTmpdirMax(job);

    Long tmpDir = tmpdirMin != null ? tmpdirMin : tmpdirMax;
    if (tmpDir == null) {
      tmpDir = TMPDIR_MIN_DEFAULT;
    }

    Long outdirMin = getOutdirMin(job);
    Long outdirMax = getOutdirMax(job);

    Long outDir = outdirMin != null ? outdirMin : outdirMax;
    if (outDir == null) {
      outDir = OUTDIR_MIN_DEFAULT;
    }
    return new Resources(cores, ram, tmpDir, outDir);
  }
  
  public static class Resources {
    @JsonProperty("cores")
    private final Long cores;
    @JsonProperty("ram")
    private final Long ram;
    @JsonProperty("tmpdir")
    private final Long tmpdir;
    @JsonProperty("outdir")
    private final Long outdir;
    
    public Resources(Long cores, Long ram, Long tmpdir, Long outdir) {
      this.cores = cores;
      this.ram = ram;
      this.tmpdir = tmpdir;
      this.outdir = outdir;
    }
    
    public Long getCores() {
      return cores;
    }
    
    public Long getRam() {
      return ram;
    }

    public Long getTmpdir() {
      return tmpdir;
    }

    public Long getOutdir() {
      return outdir;
    }
    
    @JsonIgnore
    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("cores", cores);
      map.put("ram", ram);
      map.put("tmpdir", tmpdir);
      map.put("outdir", outdir);
      return map;
    }
    
  }
  
  
  @Override
  public CWL1ResourceType getType() {
    return CWL1ResourceType.RESOURCE_REQUIREMENT;
  }

}
