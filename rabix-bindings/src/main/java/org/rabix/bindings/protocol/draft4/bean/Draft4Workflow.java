package org.rabix.bindings.protocol.draft4.bean;

import java.util.ArrayList;
import java.util.List;

import org.rabix.common.json.BeanPropertyView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

public class Draft4Workflow extends Draft4JobApp {

  @JsonProperty("steps")
  private List<Draft4Step> steps;

  @JsonProperty("dataLinks")
  @JsonView(BeanPropertyView.Full.class)
  private List<Draft4DataLink> dataLinks;

  public Draft4Workflow() {
    this.steps = new ArrayList<>();
    this.dataLinks = new ArrayList<>();
  }

  public Draft4Workflow(List<Draft4Step> steps, List<Draft4DataLink> dataLinks) {
    this.steps = steps;
    this.dataLinks = dataLinks;
  }

  @JsonIgnore
  public void addDataLink(Draft4DataLink dataLink) {
    this.dataLinks.add(dataLink);
  }

  @JsonIgnore
  public void addDataLinks(List<Draft4DataLink> dataLinks) {
    this.dataLinks.addAll(dataLinks);
  }

  public List<Draft4Step> getSteps() {
    return steps;
  }

  public List<Draft4DataLink> getDataLinks() {
    return dataLinks;
  }

  @Override
  public String toString() {
    return "Workflow [steps=" + steps + ", dataLinks=" + dataLinks + ", id=" + id + ", context=" + context
        + ", description=" + description + ", inputs=" + getInputs() + ", outputs=" + getOutputs() + ", requirements="
        + requirements + "]";
  }

  @Override
  @JsonIgnore
  public Draft4JobAppType getType() {
    return Draft4JobAppType.WORKFLOW;
  }

}
