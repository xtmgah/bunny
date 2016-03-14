package org.rabix.bindings.protocol.draft2;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.model.Executable;
import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.bindings.protocol.draft2.bean.Draft2Job;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class Draft2ResultCollectionServiceTest {

  private File workingDir;

  @BeforeMethod
  public void before() throws IOException {
    File baseDir = new File("target/worker/workingDir_" + System.currentTimeMillis());
    if (baseDir.exists()) {
      baseDir.delete();
    }

    baseDir.mkdirs();
    workingDir = new File(baseDir, "/workingDir");
    workingDir.mkdirs();

    File file1 = new File(workingDir, "file1.txt");
    file1.createNewFile();

    File file2 = new File(workingDir, "file2.txt");
    file2.createNewFile();

    File file3 = new File(workingDir, "file3.dat");
    file3.createNewFile();

    File file4 = new File(workingDir, "file4.dat");
    file4.createNewFile();
  }

  @Test
  public void testCommandLineTool() throws Exception {
    String inputJson = ResourceHelper.readResource(Draft2ResultCollectionServiceTest.class, "output-collection-job.json");

    Draft2Job job = BeanSerializer.deserialize(inputJson, Draft2Job.class);
    DAGNode dagNode = new DAGNode("id", null, null, null, null, job.getApp());
    Executable executable = new Executable("id", "id", dagNode, null, job.getInputs(), null, null);
    
    Bindings bindings = BindingsFactory.create(executable);
    executable = bindings.populateOutputs(executable, workingDir);
    
    Assert.assertTrue(executable.getOutputs() instanceof Map<?,?>);
    Assert.assertTrue((executable.getOutputs(Map.class)).containsKey("single"));
    Assert.assertTrue((executable.getOutputs(Map.class)).containsKey("array"));
    Assert.assertTrue((executable.getOutputs(Map.class)).containsKey("record"));
  }
  
  @Test
  public void testExpressionTool() throws Exception {
    String inputJson = ResourceHelper.readResource(Draft2ResultCollectionServiceTest.class, "bean/expression-job.json");

    Draft2Job job = BeanSerializer.deserialize(inputJson, Draft2Job.class);
    DAGNode dagNode = new DAGNode("id", null, null, null, null, job.getApp());
    Executable executable = new Executable("id", "id", dagNode, null, job.getInputs(), null, null);
    
    Bindings bindings = BindingsFactory.create(executable);
    executable = bindings.populateOutputs(executable, workingDir);
    
    Assert.assertTrue(executable.getOutputs() instanceof Map<?,?>);
    Assert.assertTrue((executable.getOutputs(Map.class)).containsKey("output"));
  }

}
