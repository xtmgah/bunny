package org.rabix.engine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.engine.model.DAGNodeRecord.DAGNodeGraph;

import com.google.inject.Inject;

public class DAGNodeService {

  private final ApplicationService applicationService;

  private final Map<String, DAGNodeGraph> nodes = new HashMap<>();
  
  @Inject
  public DAGNodeService(ApplicationService applicationService) {
    this.applicationService = applicationService;
  }
  
  public synchronized DAGNodeGraph load(DAGNode node, String contextId) {
    DAGNodeGraph dagNodeGraph = load(null, node, contextId);
    nodes.put(contextId, dagNodeGraph);
    return dagNodeGraph;
  }
  
  public synchronized DAGNodeGraph load(DAGNode parent, DAGNode node, String contextId) {
    if (node instanceof DAGContainer) {
      List<DAGNodeGraph> children = new ArrayList<>();
      for (DAGNode child : ((DAGContainer) node).getChildren()) {
        children.add(load(node, child, contextId));
      }
      return new DAGNodeGraph(node.getId(), null, node.getScatterMethod(), node.getInputPorts(), node.getOutputPorts(), true, ((DAGContainer) node).getLinks(), children, node.getDefaults());      
    } else {
      String appHash = applicationService.put(node.getApp());
      return new DAGNodeGraph(node.getId(), appHash, node.getScatterMethod(), node.getInputPorts(), node.getOutputPorts(), false, null, null, node.getDefaults());
    }
  }
  
  public synchronized DAGNodeGraph get(String id, String contextId) {
    return find(nodes.get(contextId), id);
  }
  
  public DAGNodeGraph find(DAGNodeGraph node, String id) {
    if (node.getId().equals(id)) {
      return node;
    }
    if (node.isContainer()) {
      for (DAGNodeGraph child : node.getChildren()) {
        DAGNodeGraph result = find(child, id);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }
  
//  private static JsonNode toJsonNode(DAGNode dagNode) {
//    ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
//
//    rootNode.set("id", JSONHelper.convertToJsonNode(dagNode.getId()));
//    rootNode.set("app", JSONHelper.convertToJsonNode(dagNode.getApp()));
//    rootNode.set("scatterMethod", JSONHelper.convertToJsonNode(dagNode.getScatterMethod().name()));
//    rootNode.set("inputPorts", JSONHelper.convertToJsonNode(dagNode.getInputPorts()));
//    rootNode.set("outputPorts", JSONHelper.convertToJsonNode(dagNode.getOutputPorts()));
//
//    if (dagNode instanceof DAGContainer) {
//      rootNode.set("links", JSONHelper.convertToJsonNode(((DAGContainer) dagNode).getLinks()));
//
//      ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
//      for (DAGNode child : ((DAGContainer) dagNode).getChildren()) {
//        arrayNode.add(toJsonNode(child));
//      }
//      rootNode.set("children", arrayNode);
//    }
//    return rootNode;
//  }
  
}
