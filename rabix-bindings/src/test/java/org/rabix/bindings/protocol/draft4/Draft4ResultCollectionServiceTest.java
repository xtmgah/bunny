package org.rabix.bindings.protocol.draft4;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft4.bean.Draft4Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class Draft4ResultCollectionServiceTest {

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
    
    File directory5 = new File(workingDir, "directory5");
    directory5.mkdirs();
    
    File file5 = new File(directory5, "file5.dat");
    file5.createNewFile();
  }

  @Test
  public void testCommandLineTool() throws Exception {
    String inputJson = ResourceHelper.readResource(Draft4ResultCollectionServiceTest.class, "output-collection-job.json");

    Draft4Job draft4Job = BeanSerializer.deserialize(inputJson, Draft4Job.class);
    String encodedApp = URIHelper.createDataURI(BeanSerializer.serializeFull(draft4Job.getApp()));
    Job job = new Job("id", "id", "id", "id", encodedApp, null, draft4Job.getInputs(), null, null, null);
    
    Bindings bindings = BindingsFactory.create(job);
    job = bindings.postprocess(job, workingDir);
    
    System.out.println(JSONHelper.writeObject(job.getOutputs()));
    
    Assert.assertTrue(job.getOutputs() instanceof Map<?,?>);
    Assert.assertTrue((job.getOutputs()).containsKey("single"));
    Assert.assertTrue((job.getOutputs()).containsKey("array"));
    Assert.assertTrue((job.getOutputs()).containsKey("record"));
  }
  
}
