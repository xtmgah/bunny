package org.rabix.bindings.protocol.draft3.expression;

import java.io.IOException;

import org.rabix.bindings.protocol.draft3.bean.Draft3Job;
import org.rabix.bindings.protocol.draft3.bean.resource.requirement.Draft3InlineJavascriptRequirement;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class Draft3ExpressionResolverTest {

  @Test
  public void test() {
    String inputJson;
    try {
      inputJson = ResourceHelper.readResource(this.getClass(), "draft3-bwa-mem-job.json");

      Draft3Job job = BeanSerializer.deserialize(inputJson, Draft3Job.class);

      Assert.assertEquals(Draft3ExpressionResolver.evaluate("$(inputs['min_std_max_min'][0])", job, null), 1);
      Assert.assertEquals(Draft3ExpressionResolver.evaluate(Draft3ExpressionResolver.evaluate("${ return inputs.reference.path + '.tmp' }", job, null), job, null), "${ return inputs.reference.path + '.tmp' }");
      job.getApp().getRequirements().add(new Draft3InlineJavascriptRequirement());
      Assert.assertEquals(Draft3ExpressionResolver.evaluate("${ return inputs.reference.path + '.tmp' }", job, null), "rabix/tests/test-files/chr20.fa.tmp");
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    } catch (Draft3ExpressionException e) {
      Assert.fail(e.getMessage());
    }
  }

}
