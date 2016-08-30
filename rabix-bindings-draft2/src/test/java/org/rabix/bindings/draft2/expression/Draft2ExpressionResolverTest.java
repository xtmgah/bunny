package org.rabix.bindings.draft2.expression;

import java.util.Arrays;
import java.util.List;

import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.common.helper.ResourceHelper;
import org.rabix.common.json.BeanSerializer;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class Draft2ExpressionResolverTest {

  @Test
  public void executorInt() throws Draft2ExpressionException {
    assertJavaScript(Integer.class, "8*5", 40);
  }

  @Test
  public void executorDouble() throws Exception {
    assertJavaScript(Double.class, "5.1 * 5.0", 25.5);
  }

  @Test
  public void executorBoolean() throws Exception {
    assertJavaScript(Boolean.class, "8 > 5", true);
    assertJavaScript(Boolean.class, "8 < 5", false);
  }

  @Test
  public void executorString() throws Exception {
    assertJavaScript(String.class, "'two' + 3", "two3");
  }

  @Test
  public void executorEngineConfig() throws Exception {
    String expression = "{return x + 1;}";

    String inputJson = ResourceHelper.readResource(this.getClass(), "expression-job.json");
    Draft2Job job = BeanSerializer.deserialize(inputJson, Draft2Job.class);
    assertJavaScript(Integer.class, job, expression, 7);
  }

  @Test
  public void executorUndefined() throws Exception {
    assertJavaScript(String.class, "{ return undefined ;}", null);
  }

  @Test
  public void executorStatement() throws Exception {
    assertJavaScript(String.class, "{if(4<5){return \"a\"}else{return\"b\"}}", "a");
    assertJavaScript(String.class, "{if(4>5){return \"a\"}else{return\"b\"}}", "b");
  }

  @Test
  public void testJSPointerInt() throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "expression-job.json");
    Draft2Job job = BeanSerializer.deserialize(inputJson, Draft2Job.class);
    assertJSPointer(Integer.class, job, "job/reads/1/path", "rabix/tests/test-files/example_human_Illumina.pe_2.fastq");
  }

  @Test(expectedExceptions = { Draft2ExpressionException.class })
  public void testJSPointerException() throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "expression-job.json");
    Draft2Job job = BeanSerializer.deserialize(inputJson, Draft2Job.class);
    assertJSPointer(Integer.class, job, "/key_1/2/key_2", 7);
  }

  @Test
  public void testJSPointerArray() throws Exception {
    String inputJson = ResourceHelper.readResource(this.getClass(), "expression-job.json");
    Draft2Job job = BeanSerializer.deserialize(inputJson, Draft2Job.class);
    assertJSPointer(List.class, job, "job/reads/*/path", Arrays.asList("rabix/tests/test-files/example_human_Illumina.pe_1.fastq", "rabix/tests/test-files/example_human_Illumina.pe_2.fastq"));
    assertJSPointer(List.class, job, "job/min_std_max_min/*", Arrays.asList(1, 2, 3, 4));
    assertJSPointer(Integer.class, job, "job/min_std_max_min/*/1", 2);
  }

  @Test
  public void execute() throws Draft2ExpressionException {
    assertJavaScript(String.class, "if ('something' == null) { '1m'; } else { 'something'; }", "something");
  }

  @Test(expectedExceptions = { Draft2ExpressionException.class })
  public void tryNativeClasses() throws Exception {
    String expression = "{return new java.lang.String('foo') + ' bar'}";
    Draft2ExpressionResolver.evaluate(expression);
  }

  @Test(expectedExceptions = { Draft2ExpressionException.class })
  public void executorTimeout() throws Exception {
    String expression = "{while(1>0){}}";
    Draft2ExpressionResolver.evaluate(expression);
  }

  @Test(expectedExceptions = { Draft2ExpressionException.class })
  public void testStackOverflow() throws Exception {
    String expression = "{function t() { return t(); } t();}";
    Draft2ExpressionResolver.evaluate(expression);
  }

  @Test
  public void testExceptionMessage() {
    String expression = "{.map()}";
    try {
      Draft2ExpressionResolver.evaluate(expression);
    } catch (Draft2ExpressionException e) {
      Assert.assertEquals(e.toString(), "org.rabix.bindings.draft2.expression.Draft2ExpressionException: Failed evaluating expression {.map()}.");
    }
  }
  
  @Test
  public void executorSelfNull() throws Exception {
    String expression = "{ if ($self == null) { return 1; } else { return 7; }; }";
    Object result = Draft2ExpressionResolver.evaluate(expression, false);
    Assert.assertEquals(result, 1);
  }

  @Test
  public void executorTemplate() throws Exception {
    String expression = "_.some([null, 0, 'yes', false]);";
    Object result = Draft2ExpressionResolver.evaluate(expression, true);
    Assert.assertEquals(result, true);
  }

  @Test(expectedExceptions = { Draft2ExpressionException.class })
  public void executorTemplateWithoutEngine() throws Exception {
    String expression = "_.some([null, 0, 'yes', false]);";
    Draft2ExpressionResolver.evaluate(expression);
  }
  
  private void assertJavaScript(Class<?> clazz, String expression, Object expected) throws Draft2ExpressionException {
    Object result = evaluateJavaScript(clazz, null, expression);
    Assert.assertEquals(result, expected);
  }

  private void assertJavaScript(Class<?> clazz, Draft2Job context, String expression, Object expected) throws Draft2ExpressionException {
    Object result = evaluateJavaScript(clazz, context, expression);
    Assert.assertEquals(result, expected);
  }

  private void assertJSPointer(Class<?> clazz, Draft2Job context, String expression, Object expected)
      throws Draft2ExpressionException {
    Object result = evaluateJSPointer(clazz, context, expression);
    Assert.assertEquals(result, expected);
  }

  private Object evaluateJavaScript(Class<?> clazz, Draft2Job context, String expression) throws Draft2ExpressionException {
    return Draft2ExpressionResolver.evaluate(expression, context, null);
  }

  private Object evaluateJSPointer(Class<?> clazz, Draft2Job context, String expression) throws Draft2ExpressionException {
    return Draft2ExpressionResolver.evaluate(expression, context, null, "cwl:JsonPointer");
  }

}
