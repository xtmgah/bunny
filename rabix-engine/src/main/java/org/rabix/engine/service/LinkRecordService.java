package org.rabix.engine.service;

import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.db.DBException;
import org.rabix.engine.db.LinkRecordRepository;
import org.rabix.engine.model.LinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LinkRecordService {

  private final static Logger logger = LoggerFactory.getLogger(LinkRecordService.class);
  
  private final LinkRecordRepository linkRecordRepository;

  @Inject
  public LinkRecordService(final LinkRecordRepository linkRecordRepository) {
    this.linkRecordRepository = linkRecordRepository;
  }
  
  @Transactional
  public void create(LinkRecord linkRecord) throws EngineServiceException {
    try {
      linkRecordRepository.insert(linkRecord);
    } catch (DBException e) {
      logger.error("Failed to insert LinkRecord " + linkRecord, e);
      throw new EngineServiceException("Failed to insert LinkRecord " + linkRecord, e);
    }
  }

  @Transactional
  public List<LinkRecord> findBySourceJobId(String jobId, String contextId) throws EngineServiceException {
    try {
      return linkRecordRepository.findBySourceJobId(jobId, contextId);
    } catch (DBException e) {
      logger.error("Failed to find LinkRecords for jobId=" + jobId + " and rootId=" + contextId, e);
      throw new EngineServiceException("Failed to find LinkRecords for jobId=" + jobId + ", rootId=" + contextId, e);
    }
  }
  
  @Transactional
  public List<LinkRecord> findBySourceAndSourceType(String jobId, LinkPortType varType, String contextId) throws EngineServiceException {
    try {
      return linkRecordRepository.findBySourceAndSourceType(jobId, varType, contextId);
    } catch (DBException e) {
      logger.error("Failed to find LinkRecords for jobId=" + jobId + ", linkPortType=" + varType + ", rootId=" + contextId, e);
      throw new EngineServiceException("Failed to find LinkRecords for jobId=" + jobId + ", linkPortType=" + varType + ", rootId=" + contextId, e);
    }
  }
  
  @Transactional
  public List<LinkRecord> findBySource(String jobId, String portId, String contextId) throws EngineServiceException {
    try {
      return linkRecordRepository.findBySource(jobId, portId, contextId);
    } catch (DBException e) {
      logger.error("Failed to find LinkRecords for jobId=" + jobId + ", portId=" + portId + ", rootId=" + contextId, e);
      throw new EngineServiceException("Failed to find LinkRecords for jobId=" + jobId + ", portId=" + portId + ", rootId=" + contextId, e);
    }
  }
  
  @Transactional
  public List<LinkRecord> findBySourceAndDestinationType(String jobId, String portId, LinkPortType varType, String contextId) throws EngineServiceException {
    try {
      return linkRecordRepository.findBySourceAndDestinationType(jobId, varType, contextId);
    } catch (DBException e) {
      logger.error("Failed to find LinkRecords for jobId=" + jobId + ", portId=" + portId + ", linkPortType=" + varType + ", rootId=" + contextId, e);
      throw new EngineServiceException("Failed to find LinkRecords for jobId=" + jobId + ", portId=" + portId + ", linkPortType=" + varType + ", rootId=" + contextId, e);
    }
  }

}
