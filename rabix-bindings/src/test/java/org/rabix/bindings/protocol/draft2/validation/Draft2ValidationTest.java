package org.rabix.bindings.protocol.draft2.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.BindingsFactory;
import org.rabix.bindings.BindingsFactory.BindingsPair;
import org.rabix.bindings.helper.URIHelper;
import org.rabix.bindings.model.Job;
import org.rabix.common.helper.ResourceHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class Draft2ValidationTest {

  @Test(expectedExceptions = { BindingException.class }, expectedExceptionsMessageRegExp = "Missing inputs: reference")
  public void testRequiredInputsFail() throws BindingException {
    try {
      String appText = ResourceHelper.readResource(this.getClass(), "bwa-validation-job.json");

      String appURI = URIHelper.createDataURI(appText);

      BindingsPair pair = BindingsFactory.create(appURI);

      Map<String, Object> inputs = new HashMap<>();

      List<Object> reads = new ArrayList<>();
      reads.add(createFile("path_1"));
      reads.add(createFile("path_2"));
      inputs.put("reads", reads);

      List<Integer> minStdMaxMin = new ArrayList<>();
      minStdMaxMin.add(1);
      minStdMaxMin.add(2);
      minStdMaxMin.add(3);
      minStdMaxMin.add(4);
      inputs.put("min_std_max_min", minStdMaxMin);
      inputs.put("minimum_seed_length", 7);

      Job job = new Job(appURI, inputs);
      pair.getBindings().validate(job);
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testRequiredInputsSuccess() {
    try {
      String appText = ResourceHelper.readResource(this.getClass(), "bwa-validation-job.json");

      String appURI = URIHelper.createDataURI(appText);

      BindingsPair pair = BindingsFactory.create(appURI);

      Map<String, Object> inputs = new HashMap<>();
      inputs.put("reference", createFile("path_0"));
      
      List<Object> reads = new ArrayList<>();
      reads.add(createFile("path_1"));
      reads.add(createFile("path_2"));
      inputs.put("reads", reads);

      List<Integer> minStdMaxMin = new ArrayList<>();
      minStdMaxMin.add(1);
      minStdMaxMin.add(2);
      minStdMaxMin.add(3);
      minStdMaxMin.add(4);
      inputs.put("min_std_max_min", minStdMaxMin);
      inputs.put("minimum_seed_length", 7);

      Job job = new Job(appURI, inputs);
      pair.getBindings().validate(job);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }
  
  private Map<String, Object> createFile(String path) {
    Map<String, Object> file = new HashMap<>();
    file.put("class", "File");
    file.put("path", path);
    return file;
  }

}
