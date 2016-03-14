package org.rabix.bindings.dag.validations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGLink;
import org.rabix.bindings.model.dag.DAGNode;

public class DAGValidations {

  public static void DFSDetectLoop(final DAGContainer containerNode) throws BindingException {
    Set<DAGNode> marked = new HashSet<DAGNode>();
    Set<DAGNode> stack = new HashSet<DAGNode>();
    List<DAGNode> root = rootNodes(containerNode);
    for (DAGNode node : root) {
      if (!marked.contains(node)) {
        checkForLoop(containerNode, node, marked, stack);
      }
    }
  }

  private static void checkForLoop(DAGContainer containerNode, DAGNode dagNode, Set<DAGNode> marked, Set<DAGNode> stack)
      throws BindingException {
    marked.add(dagNode);
    stack.add(dagNode);
    List<DAGNode> adjacentNodes = adjacentNodes(containerNode, dagNode);
    for (DAGNode s : adjacentNodes) {
      if (!marked.contains(s)) {
        checkForLoop(containerNode, s, marked, stack);
      } else if (stack.contains(s)) {
        throw new BindingException("Container node contains loop");
      }
    }
    stack.remove(dagNode);
  }

  private static List<DAGNode> rootNodes(DAGContainer containerNode) {
    List<DAGNode> rootNodes = new ArrayList<DAGNode>();
    for (DAGNode node : containerNode.getChildren()) {
      for (DAGLink dataLink : containerNode.getLinks()) {
        if (dataLink.getDestination().getNodeId().equals(node.getId())
            && dataLink.getSource().getNodeId().equals(containerNode.getId())) {
          rootNodes.add(node);
          break;
        }
      }
    }
    return rootNodes;
  }

  private static List<DAGNode> adjacentNodes(DAGContainer containerNode, DAGNode dagNode) throws BindingException {
    List<DAGNode> adjacentNodes = new ArrayList<DAGNode>();
    for (DAGLink dataLink : containerNode.getLinks()) {
      String nodeId = dataLink.getSource().getNodeId();
      if (nodeId.equals(dagNode.getId()) && dataLink.getDestination().getNodeId() != containerNode.getId()) {
        adjacentNodes.add(getNodeFromId(containerNode, dataLink.getDestination().getNodeId()));
      }
    }
    return adjacentNodes;
  }

  private static DAGNode getNodeFromId(DAGContainer containerNode, String id) throws BindingException {
    for (DAGNode node : containerNode.getChildren()) {
      if (node.getId().equals(id)) {
        return node;
      }
    }
    throw new BindingException(String.format("Con't find DAGNode with id %s", id));
  }
}
