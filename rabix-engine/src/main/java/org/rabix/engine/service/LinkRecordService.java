package org.rabix.engine.service;

import java.util.List;

import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.db.DBException;
import org.rabix.engine.db.LinkRecordRepository;
import org.rabix.engine.model.LinkRecord;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LinkRecordService {

  private final LinkRecordRepository linkRecordRepository;

  @Inject
  public LinkRecordService(final LinkRecordRepository linkRecordRepository) {
    this.linkRecordRepository = linkRecordRepository;
  }
  
  @Transactional
  public void create(LinkRecord linkRecord) {
    try {
      linkRecordRepository.insert(linkRecord);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Transactional
  public List<LinkRecord> findBySourceJobId(String jobId, String contextId) {
    try {
      return linkRecordRepository.findBySourceJobId(jobId, contextId);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }
  
  @Transactional
  public List<LinkRecord> findBySourceAndSourceType(String jobId, LinkPortType varType, String contextId) {
    try {
      return linkRecordRepository.findBySourceAndSourceType(jobId, varType, contextId);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }
  
  @Transactional
  public List<LinkRecord> findBySource(String jobId, String portId, String contextId) {
    try {
      return linkRecordRepository.findBySource(jobId, portId, contextId);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }
  
  @Transactional
  public List<LinkRecord> findBySourceAndDestinationType(String jobId, String portId, LinkPortType varType, String contextId) {
    try {
      return linkRecordRepository.findBySourceAndDestinationType(jobId, varType, contextId);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

}
