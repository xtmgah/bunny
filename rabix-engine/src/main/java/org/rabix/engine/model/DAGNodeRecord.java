package org.rabix.engine.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.ScatterMethod;
import org.rabix.bindings.model.dag.DAGLink;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DAGNodeRecord {

  private DAGNodeGraph dagNode;
  private String contextId;
  
  public DAGNodeRecord(DAGNodeGraph dagNode, String contextId) {
    super();
    this.dagNode = dagNode;
    this.contextId = contextId;
  }

  public DAGNodeGraph getDagNode() {
    return dagNode;
  }

  public void setDagNode(DAGNodeGraph dagNode) {
    this.dagNode = dagNode;
  }

  public String getContextId() {
    return contextId;
  }

  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  @Override
  public String toString() {
    return "DAGNodeRecord [dagNode=" + dagNode + ", contextId=" + contextId + "]";
  }

  public static class DAGNodeGraph {

    @JsonProperty("id")
    private String id;
    @JsonProperty("appHash")
    private String appHash;
    @JsonProperty("scatterMethod")
    private ScatterMethod scatterMethod;
    @JsonProperty("inputPorts")
    private List<DAGLinkPort> inputPorts;
    @JsonProperty("outputPorts")
    private List<DAGLinkPort> outputPorts;
    @JsonProperty("defaults")
    private Map<String, Object> defaults;
    
    @JsonProperty("isContainer")
    private boolean isContainer;
    @JsonProperty("links")
    private List<DAGLink> links;
    @JsonProperty("children")
    private List<DAGNodeGraph> children;
    
    @JsonCreator
    public DAGNodeGraph(
        @JsonProperty("id") String id, 
        @JsonProperty("appHash") String appHash, 
        @JsonProperty("scatterMethod") ScatterMethod scatterMethod, 
        @JsonProperty("inputPorts") List<DAGLinkPort> inputPorts,
        @JsonProperty("outputPorts") List<DAGLinkPort> outputPorts, 
        @JsonProperty("isContainer") boolean isContainer, 
        @JsonProperty("links") List<DAGLink> links, 
        @JsonProperty("children") List<DAGNodeGraph> children,
        @JsonProperty("defaults") Map<String, Object> defaults) {
      this.id = id;
      this.appHash = appHash;
      this.scatterMethod = scatterMethod;
      this.inputPorts = inputPorts;
      this.outputPorts = outputPorts;
      this.isContainer = isContainer;
      this.links = links;
      this.children = children;
      this.defaults = defaults;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getAppHash() {
      return appHash;
    }

    public void setAppHash(String appHash) {
      this.appHash = appHash;
    }

    public ScatterMethod getScatterMethod() {
      return scatterMethod;
    }

    public void setScatterMethod(ScatterMethod scatterMethod) {
      this.scatterMethod = scatterMethod;
    }

    public List<DAGLinkPort> getInputPorts() {
      return inputPorts;
    }

    public void setInputPorts(List<DAGLinkPort> inputPorts) {
      this.inputPorts = inputPorts;
    }

    public List<DAGLinkPort> getOutputPorts() {
      return outputPorts;
    }

    public void setOutputPorts(List<DAGLinkPort> outputPorts) {
      this.outputPorts = outputPorts;
    }

    public boolean isContainer() {
      return isContainer;
    }

    public void setContainer(boolean isContainer) {
      this.isContainer = isContainer;
    }

    public List<DAGLink> getLinks() {
      return links;
    }

    public void setLinks(List<DAGLink> links) {
      this.links = links;
    }

    public List<DAGNodeGraph> getChildren() {
      return children;
    }

    public void setChildren(List<DAGNodeGraph> children) {
      this.children = children;
    }

    public Map<String, Object> getDefaults() {
      return defaults;
    }

    public void setDefaults(Map<String, Object> defaults) {
      this.defaults = defaults;
    }

    @JsonIgnore
    public LinkMerge getLinkMerge(String portId, LinkPortType linkPortType) {
      switch (linkPortType) {
      case INPUT:
        for (DAGLinkPort inputPort : inputPorts) {
          if (inputPort.getId().equals(portId)) {
            return inputPort.getLinkMerge();
          }
        }
        break;
      case OUTPUT:
        for (DAGLinkPort inputPort : outputPorts) {
          if (inputPort.getId().equals(portId)) {
            return inputPort.getLinkMerge();
          }
        }
        break;
      default:
        break;
      }
      return null;
    }
    
    @JsonIgnore
    public Set<LinkMerge> getLinkMergeSet(LinkPortType linkPortType) {
      Set<LinkMerge> linkMergeSet = new HashSet<>();
      
      switch (linkPortType) {
      case INPUT:
        for (DAGLinkPort inputPort : inputPorts) {
          linkMergeSet.add(inputPort.getLinkMerge());
        }
        break;
      case OUTPUT:
        for (DAGLinkPort outputPort : outputPorts) {
          linkMergeSet.add(outputPort.getLinkMerge());
        }
        break;
      default:
        break;
      }
      return linkMergeSet;
    }
    
    @Override
    public String toString() {
      return "DAGNode [id=" + id + ", appHash=" + appHash + ", scatterMethod=" + scatterMethod + ", inputPorts=" + inputPorts + ", outputPorts=" + outputPorts + ", isContainer=" + isContainer + ", links=" + links + ", children=" + children + "]";
    }

  }

}
