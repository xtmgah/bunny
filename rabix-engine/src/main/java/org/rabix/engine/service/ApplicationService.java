package org.rabix.engine.service;

import java.util.HashMap;
import java.util.Map;

import org.rabix.bindings.model.Application;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;

public class ApplicationService {

  private Map<String, String> applications = new HashMap<>();
  
  public ApplicationService() {
  }

  public String put(Application app) {
    String hash = hash(app);
    applications.put(hash, app.serialize());
    return hash;
  }
  
  public String get(String hash) {
    return applications.get(hash);
  }
  
  private String hash(Application app) {
    String serializedApp = JSONHelper.writeSortedWithoutIdentation(JSONHelper.readJsonNode(app.serialize()));
    return ChecksumHelper.checksum(serializedApp, HashAlgorithm.SHA1);
  }
  
}
