package org.rabix.bindings.protocol.helper;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.DAGValidationHelper;
import org.rabix.bindings.model.dag.DAGContainer;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class DAGValidationHelperTest {

  @Test(expectedExceptions = BindingException.class)
  public void testDetectingLoopInWorkflow() throws Exception {
    String appText = ResourceHelper.readResource(this.getClass(), "grep-wf-loop.cwl.json");
    Bindings bindings = BindingsFactory.createFromAppText(appText);
    DAGNode node = bindings.translateToDAG(appText, "{}");
    DAGValidationHelper.detectLoop((DAGContainer) node);
  }

  @Test
  public void testNoLoopInWorkflow() throws Exception {
    String appText = ResourceHelper.readResource(this.getClass(), "grep-wf.cwl.json");
    Bindings bindings = BindingsFactory.createFromAppText(appText);
    DAGNode node = bindings.translateToDAG(appText, "{}");
    DAGValidationHelper.detectLoop((DAGContainer) node);
  }

}
