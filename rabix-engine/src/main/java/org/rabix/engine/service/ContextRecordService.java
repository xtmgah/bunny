package org.rabix.engine.service;

import org.rabix.db.DBException;
import org.rabix.engine.db.ContextRecordRepository;
import org.rabix.engine.model.ContextRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ContextRecordService {

  private final static Logger logger = LoggerFactory.getLogger(ContextRecordService.class);
  
  private final ContextRecordRepository contextRecordRepository;
  
  @Inject
  public ContextRecordService(ContextRecordRepository contextRecordRepository) {
    this.contextRecordRepository = contextRecordRepository;
  }
  
  @Transactional
  public void create(ContextRecord contextRecord) throws EngineServiceException {
    try {
      contextRecordRepository.insert(contextRecord);
    } catch (DBException e) {
      logger.error("Failed to insert ContextRecord " + contextRecord, e);
      throw new EngineServiceException("Failed to insert ContextRecord " + contextRecord, e);
    }
  }
  
  @Transactional
  public void update(ContextRecord contextRecord) throws EngineServiceException {
    try {
      contextRecordRepository.update(contextRecord);
    } catch (DBException e) {
      logger.error("Failed to update ContextRecord " + contextRecord, e);
      throw new EngineServiceException("Failed to update ContextRecord " + contextRecord, e);
    }
  }
  
  @Transactional
  public ContextRecord find(String id) throws EngineServiceException {
    try {
      return contextRecordRepository.find(id);
    } catch (DBException e) {
      logger.error("Failed to find ContextRecord for id=" + id, e);
      throw new EngineServiceException("Failed to find ContextRecord for id=" + id, e);
    }
  }
  
}
