package org.rabix.bindings.protocol.zero;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.model.LinkMerge;
import org.rabix.bindings.model.dag.DAGLinkPort;
import org.rabix.bindings.model.dag.DAGLinkPort.LinkPortType;
import org.rabix.bindings.protocol.zero.bean.ZeroJob;
import org.rabix.bindings.protocol.zero.bean.ZeroJobApp;
import org.rabix.bindings.protocol.zero.bean.ZeroPort;
import org.rabix.bindings.model.dag.DAGNode;

public class ZeroTranslator {

  public DAGNode translateToGeneric(Job job) throws BindingException {
    ZeroJob rabixJob = translateToRabixJob(job);
    return createDAGNode(rabixJob);
  }

  private DAGNode createDAGNode(ZeroJob job) {
    List<DAGLinkPort> inputPorts = new ArrayList<>();

    for (ZeroPort port : job.getApp().getInputs()) {
      DAGLinkPort linkPort = new DAGLinkPort(port.getId(), job.getId(), LinkPortType.INPUT, LinkMerge.merge_nested,
          false);
      inputPorts.add(linkPort);
    }
    List<DAGLinkPort> outputPorts = new ArrayList<>();
    for (ZeroPort port : job.getApp().getOutputs()) {
      DAGLinkPort linkPort = new DAGLinkPort(port.getId(), job.getId(), LinkPortType.OUTPUT, LinkMerge.merge_nested,
          false);
      outputPorts.add(linkPort);
    }
    return new DAGNode(job.getId(), inputPorts, outputPorts, null, job.getApp(), job.getInputs());
  }

  public ZeroJob translateToRabixJob(Job job) throws BindingException {
    String app = null;
    try {
      app = URIHelper.getData(job.getApp());
    } catch (IOException e) {
      throw new BindingException(e);
    }
    ZeroJobApp rabixJobApp = (ZeroJobApp) ZeroAppProcessor.loadAppObject(job.getApp(), app);
    
    return new ZeroJob(rabixJobApp.getId(), rabixJobApp, job.getInputs(), job.getOutputs());
  }
}
