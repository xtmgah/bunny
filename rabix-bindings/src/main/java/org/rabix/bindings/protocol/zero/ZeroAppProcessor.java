package org.rabix.bindings.protocol.zero;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.visitor.BaseVisitor;
import org.rabix.bindings.BindingException;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.protocol.zero.bean.ZeroJobApp;

public class ZeroAppProcessor {
  
  
  public static String loadApp(String appURI) throws BindingException{
    String app = null;
    try {
      app = URIHelper.getData(appURI);
    } catch (IOException e) {
      throw new BindingException(e);
    }
    return app;
  }
  
  public static Application loadAppObject(String appId, String app) throws BindingException {
    String template = getTemplate(app);
    Set<String> inputs = getInputsFromApp(appId, template);
    Set<String> outputs = getOutputsFromApp(template);
    return new ZeroJobApp(appId, app, template, inputs, outputs);
  }
  
  private static Set<String> getInputsFromApp(String appName, String template) {
    VelocityEngine ve = new VelocityEngine();
    ve.setProperty(Velocity.RESOURCE_LOADER, "string");
    ve.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
    ve.addProperty("string.resource.loader.repository.static", "false");
    ve.init();
    StringResourceRepository repo = (StringResourceRepository) ve
        .getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
    repo.putStringResource(appName, template);
    Template temp = ve.getTemplate(appName);
    final List<String> keys = new ArrayList<String>();
    SimpleNode node = (SimpleNode) temp.getData();

    BaseVisitor myVisitor = new BaseVisitor() {
      @Override
      public Object visit(ASTReference node, Object data) {
        String key = node.literal();
        keys.add(normalizePortId(key));
        return super.visit(node, data);
      }
    };
    node.jjtAccept(myVisitor, new Object());
    Set<String> keySet = new HashSet<String>(keys);
    return keySet;
  }
  
  private static Set<String> getOutputsFromApp(String template) {
    String[] output = template.split(">");
    String stdout = output[1].split(" ")[1];
    Set<String> outputs = new HashSet<String>();
    outputs.add(stdout);
    return outputs;
  }
  
  private static String normalizePortId(String portId) {
    return portId.substring(2, portId.length()-1);
  }
  
  private static String getTemplate(String app) throws BindingException {
    String[] lines = app.split("\\n");
    if(lines.length < 2 || !lines[0].equals("#rabix:SimpleRabixTool")) {
      throw new BindingException("Invalid RabixApp");
    }
    String template = lines[1];
    return template;
  }
  

}
