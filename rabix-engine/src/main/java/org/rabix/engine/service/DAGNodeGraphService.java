package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.db.DBException;
import org.rabix.engine.db.DAGNodeGraphRepository;
import org.rabix.engine.model.DAGNodeRecord.DAGNodeGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class DAGNodeGraphService {

  private final static Logger logger = LoggerFactory.getLogger(DAGNodeGraphService.class);
  
  private final DAGNodeGraphRepository dagNodeRepository;
  
  private final ApplicationPayloadService applicationService;
  
  @Inject
  public DAGNodeGraphService(ApplicationPayloadService applicationService, DAGNodeGraphRepository dagNodeRepository) {
    this.dagNodeRepository = dagNodeRepository;
    this.applicationService = applicationService;
  }
  
  @Transactional
  public DAGNodeGraph insert(DAGNode node, String contextId) throws EngineServiceException {
    DAGNodeGraph dagNodeGraph = load(null, node, contextId);
    insert(dagNodeGraph, contextId);
    return dagNodeGraph;
  }
  
  private DAGNodeGraph load(DAGNode parent, DAGNode node, String contextId) throws EngineServiceException {
    if (node instanceof DAGContainer) {
      List<DAGNodeGraph> children = new ArrayList<>();
      for (DAGNode child : ((DAGContainer) node).getChildren()) {
        children.add(load(node, child, contextId));
      }
      return new DAGNodeGraph(node.getId(), null, node.getScatterMethod(), node.getInputPorts(), node.getOutputPorts(), true, ((DAGContainer) node).getLinks(), children, node.getDefaults());      
    } else {
      String appHash = applicationService.insert(node.getApp());
      return new DAGNodeGraph(node.getId(), appHash, node.getScatterMethod(), node.getInputPorts(), node.getOutputPorts(), false, null, null, node.getDefaults());
    }
  }
  
  @Transactional
  public void insert(DAGNodeGraph graph, String contextId) throws EngineServiceException {
    try {
      dagNodeRepository.insert(graph, contextId);
    } catch (DBException e) {
      logger.error("Failed to insert DAGNodeGraph " + graph, e);
      throw new EngineServiceException("Failed to insert DAGNodeGraph " + graph, e);
    }
  }
  
  @Transactional
  public DAGNodeGraph find(String id, String rootId) throws EngineServiceException {
    try {
      return dagNodeRepository.find(id, rootId);
    } catch (DBException e) {
      logger.error("Failed to find DAGNodeGraph for id=" + id + " and rootId=" + rootId, e);
      throw new EngineServiceException("Failed to find DAGNodeGraph for id=" + id + " and rootId=" + rootId, e);
    }
  }
  
}
