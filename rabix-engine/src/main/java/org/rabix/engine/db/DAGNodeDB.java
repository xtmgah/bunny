package org.rabix.engine.db;

import java.util.HashMap;
import java.util.Map;

import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;

/**
 * In-memory {@link DAGNode} repository
 */
public class DAGNodeDB {

  private final Map<String, Map<String, DAGNode>> nodes;
  
  public DAGNodeDB() {
    this.nodes = new HashMap<>();
  }
  
  /**
   * Gets node from the repository 
   */
  public synchronized DAGNode get(String id, String contextId) {
    Map<String, DAGNode> contextNodes = nodes.get(contextId);
    return contextNodes == null ? null : contextNodes.get(id);
  }
  
  /**
   * Loads node into the repository recursively
   */
  public synchronized void loadDB(DAGNode node, String contextId) {
    Map<String, DAGNode> contextNodes = nodes.get(contextId);
    if (contextNodes == null) {
      contextNodes = new HashMap<>();
      nodes.put(contextId, contextNodes);
    }
    add(node, contextId);
    
    if (node instanceof DAGContainer) {
      for (DAGNode child : ((DAGContainer) node).getChildren()) {
        loadDB(child, contextId);
      }
    }
  }
  
  /**
   *  Adds one node to the recursively
   */
  private void add(DAGNode node, String contextId) {
    Map<String, DAGNode> contextNodes = nodes.get(contextId);
    if (contextNodes == null) {
      contextNodes = new HashMap<>();
      nodes.put(contextId, contextNodes);
    }
    contextNodes.put(node.getId(), node);
  }
  
}
