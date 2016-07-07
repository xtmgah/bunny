package org.rabix.bindings.protocol.draft4.bean;

import java.io.IOException;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.Bindings;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Application;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class Draft4CommandLineToolTest {

  @Test
  public void testBwaMemJob() throws IOException, BindingException {
    String appJson = ResourceHelper.readResource(this.getClass(), "bwa-mem-tool.cwl");
    String inputJson = ResourceHelper.readResource(this.getClass(), "bwa-mem-job.json");
    
    String encodedApp = URIHelper.createDataURI(appJson);
    Map<String, Object> inputs = JSONHelper.readMap(inputJson);
    Job job = new Job("id", "id", "id", "id", encodedApp, null, inputs, null, null, null);
    
    Bindings bindings = BindingsFactory.create(job);
    
    Application application = bindings.loadAppObject(encodedApp);
  }

}
