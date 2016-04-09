package org.rabix.bindings.protocol.rabix;

import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.protocol.rabix.bean.RabixJob;
import org.rabix.bindings.protocol.rabix.bean.RabixJobApp;
import org.rabix.bindings.protocol.rabix.bean.RabixPort;

public class RabixTranslator {

  public DAGNode translateToGeneric(Job job) throws BindingException {
    RabixJob rabixJob = translateToRabixJob(job);
    return createDAGNode(rabixJob);
  }

  private DAGNode createDAGNode(RabixJob job) {
    List<DAGLinkPort> inputPorts = new ArrayList<>();

    for (RabixPort port : job.getApp().getInputs()) {
      DAGLinkPort linkPort = new DAGLinkPort(port.getId(), job.getId(), LinkPortType.INPUT, LinkMerge.merge_nested,
          false);
      inputPorts.add(linkPort);
    }
    List<DAGLinkPort> outputPorts = new ArrayList<>();
    for (RabixPort port : job.getApp().getOutputs()) {
      DAGLinkPort linkPort = new DAGLinkPort(port.getId(), job.getId(), LinkPortType.OUTPUT, LinkMerge.merge_nested,
          false);
      outputPorts.add(linkPort);
    }
    return new DAGNode(job.getId(), inputPorts, outputPorts, null, job.getApp(), job.getInputs());
  }

  public RabixJob translateToRabixJob(Job job) throws BindingException {
    RabixJobApp rabixJobApp = (RabixJobApp) RabixAppProcessor.loadAppObject(job.getId(), job.getApp());
    return new RabixJob(job.getId(), rabixJobApp, job.getInputs(), job.getOutputs());
  }
}
