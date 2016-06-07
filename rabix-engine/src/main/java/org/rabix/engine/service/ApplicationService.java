package org.rabix.engine.service;

import org.rabix.bindings.model.Application;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.db.DBException;
import org.rabix.engine.db.ApplicationRepository;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  
  @Inject
  public ApplicationService(ApplicationRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }
  
  @Transactional
  public String insert(Application app) {
    String hash = hash(app);
    try {
      applicationRepository.insert(hash, app.serialize());
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return hash;
  }
  
  @Transactional
  public String get(String hash) {
    try {
      return applicationRepository.find(hash);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }
  
  private String hash(Application app) {
    String serializedApp = JSONHelper.writeSortedWithoutIdentation(JSONHelper.readJsonNode(app.serialize()));
    return ChecksumHelper.checksum(serializedApp, HashAlgorithm.SHA1);
  }
  
}
