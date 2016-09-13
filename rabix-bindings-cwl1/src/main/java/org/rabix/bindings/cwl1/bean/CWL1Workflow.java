package org.rabix.bindings.cwl1.bean;

import java.util.ArrayList;
import java.util.List;

import org.rabix.common.json.BeanPropertyView;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

public class CWL1Workflow extends CWL1JobApp {

  @JsonProperty("steps")
  private List<CWL1Step> steps;

  @JsonProperty("dataLinks")
  @JsonView(BeanPropertyView.Full.class)
  private List<CWL1DataLink> dataLinks;

  public CWL1Workflow() {
    this.steps = new ArrayList<>();
    this.dataLinks = new ArrayList<>();
  }

  public CWL1Workflow(List<CWL1Step> steps, List<CWL1DataLink> dataLinks) {
    this.steps = steps;
    this.dataLinks = dataLinks;
  }

  @JsonIgnore
  public void addDataLink(CWL1DataLink dataLink) {
    this.dataLinks.add(dataLink);
  }

  @JsonIgnore
  public void addDataLinks(List<CWL1DataLink> dataLinks) {
    this.dataLinks.addAll(dataLinks);
  }

  public List<CWL1Step> getSteps() {
    return steps;
  }

  public List<CWL1DataLink> getDataLinks() {
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
  public CWL1JobAppType getType() {
    return CWL1JobAppType.WORKFLOW;
  }

}
