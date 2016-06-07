package org.rabix.engine.service;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Application;
import org.rabix.common.helper.ChecksumHelper;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.JSONHelper;
import org.rabix.db.DBException;
import org.rabix.engine.db.ApplicationPayloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ApplicationPayloadService {

  private final static Logger logger = LoggerFactory.getLogger(ApplicationPayloadService.class);
  
  private final ApplicationPayloadRepository applicationRepository;
  
  @Inject
  public ApplicationPayloadService(ApplicationPayloadRepository applicationRepository) {
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
  public String find(String id) throws EngineServiceException {
    try {
      return applicationRepository.find(id);
    } catch (DBException e) {
      logger.error("Failed to find Application for id=" + id, e);
      throw new EngineServiceException("Failed to find Application for id=" + id, e);
    }
  }
  
  @Transactional
  public Application findObject(String id) throws EngineServiceException {
    String appPayload = find(id);
    
    String appPayloadDataUri = URIHelper.createDataURI(appPayload);
    if (appPayloadDataUri == null) {
      return null;
    }
    
    try {
      Bindings bindings = BindingsFactory.create(appPayloadDataUri);
      return bindings.loadAppObject(appPayloadDataUri);
    } catch (BindingException e) {
      logger.error("Failed to find Bindings for application with id=" + id, e);
      throw new EngineServiceException("Failed to find Bindings for application with id=" + id, e);
    }
  }
  
  private String hash(Application app) {
    String serializedApp = JSONHelper.writeSortedWithoutIdentation(JSONHelper.readJsonNode(app.serialize()));
    return ChecksumHelper.checksum(serializedApp, HashAlgorithm.SHA1);
  }
  
}
