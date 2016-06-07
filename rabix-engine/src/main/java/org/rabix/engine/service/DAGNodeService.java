package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.db.DBException;
import org.rabix.engine.db.DAGNodeRepository;
import org.rabix.engine.model.DAGNodeRecord.DAGNodeGraph;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class DAGNodeService {

  private final ApplicationService applicationService;

  private DAGNodeRepository dagNodeRepository;
  
  @Inject
  public DAGNodeService(ApplicationService applicationService, DAGNodeRepository dagNodeRepository) {
    this.dagNodeRepository = dagNodeRepository;
    this.applicationService = applicationService;
  }
  
  @Transactional
  public DAGNodeGraph insert(DAGNode node, String contextId) {
    DAGNodeGraph dagNodeGraph = load(null, node, contextId);
    insert(dagNodeGraph, contextId);
    return dagNodeGraph;
  }
  
  private DAGNodeGraph load(DAGNode parent, DAGNode node, String contextId) {
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
  public void insert(DAGNodeGraph graph, String contextId) {
    try {
      dagNodeRepository.insert(graph, contextId);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  @Transactional
  public DAGNodeGraph find(String id, String contextId) {
    try {
      return dagNodeRepository.find(id, contextId);
    } catch (DBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }
  
}
