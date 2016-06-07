package org.rabix.engine.service;

import org.rabix.bindings.model.Application;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.db.DBException;
import org.rabix.engine.db.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ApplicationService {

  private final static Logger logger = LoggerFactory.getLogger(ApplicationService.class);
  
  private final ApplicationRepository applicationRepository;
  
  @Inject
  public ApplicationService(ApplicationRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }
  
  @Transactional
  public String insert(Application app) throws EngineServiceException {
    String hash = hash(app);
    try {
      applicationRepository.insert(hash, app.serialize());
    } catch (DBException e) {
      logger.error("Failed to insert Application " + app.serialize(), e);
      throw new EngineServiceException("Failed to insert Application " + app.serialize(), e);
    }
    return hash;
  }
  
  @Transactional
  public String get(String hash) throws EngineServiceException {
    try {
      return applicationRepository.find(hash);
    } catch (DBException e) {
      logger.error("Failed to find Application for " + hash, e);
      throw new EngineServiceException("Failed to find Application for " + hash, e);
    }
  }
  
  private String hash(Application app) {
    String serializedApp = JSONHelper.writeSortedWithoutIdentation(JSONHelper.readJsonNode(app.serialize()));
    return ChecksumHelper.checksum(serializedApp, HashAlgorithm.SHA1);
  }
  
}
