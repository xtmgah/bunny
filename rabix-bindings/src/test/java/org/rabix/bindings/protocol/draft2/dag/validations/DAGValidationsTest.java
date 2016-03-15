package org.rabix.bindings.protocol.draft2.dag.validations;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.dag.validations.DAGValidations;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.testng.annotations.Test;

public class DAGValidationsTest {

  static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  @Test(expectedExceptions = BindingException.class)
  public void testDetectingLoopInWorkflow() throws Exception {
    File appFile = new File("src/test/resources/org/rabix/bindings/protocol/draft2/bean/grep-wf-loop.cwl.json");
    String appText;
    appText = readFile(appFile.getAbsolutePath(), Charset.defaultCharset());
    Bindings bindings = BindingsFactory.createFromAppText(appText);
    DAGNode node = bindings.translateToDAG(appText, "{}");
    DAGValidations.DFSDetectLoop((DAGContainer) node);
  }
  
  @Test
  public void testNoLoopInWorkflow() throws Exception {
    File appFile = new File("src/test/resources/org/rabix/bindings/protocol/draft2/bean/grep-wf.cwl.json");
    String appText;
    appText = readFile(appFile.getAbsolutePath(), Charset.defaultCharset());
    Bindings bindings = BindingsFactory.createFromAppText(appText);
    DAGNode node = bindings.translateToDAG(appText, "{}");
    DAGValidations.DFSDetectLoop((DAGContainer) node);
  }

}
