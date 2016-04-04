package org.rabix.bindings.protocol.draft2.reference;

import java.io.File;
import java.io.IOException;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.protocol.draft2.Draft2DocumentReferenceResolver;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.helper.ResourceHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;

@Test(groups = { "functional" })
public class Draft2ReferenceResolverTest {

  @Test
  public void test() throws Exception {
    String filePath = Draft2ReferenceResolverTest.class.getResource("count-lines1-wf.cwl.json").getFile();
        
    Draft2DocumentReferenceResolver referenceResolver = new Draft2DocumentReferenceResolver();
    JsonNode resolvedRoot = readJsonNode("count-lines1-wf-resolved.cwl.json");
    Assert.assertEquals(referenceResolver.resolve(new File(filePath)), JSONHelper.writeObject(resolvedRoot));
  }

  @Test
  public void testRegular() throws Exception {
    String filePath = Draft2ReferenceResolverTest.class.getResource("regular.json").getFile();
    
    Draft2DocumentReferenceResolver referenceResolver = new Draft2DocumentReferenceResolver();
    JsonNode resolvedRoot = readJsonNode("regular-resolved.json");
    Assert.assertEquals(referenceResolver.resolve(new File(filePath)), JSONHelper.writeObject(resolvedRoot));
  }

  @Test
  public void testRegularJsonPointer() throws Exception {
    String filePath = Draft2ReferenceResolverTest.class.getResource("regular-json-pointer.json").getFile();
    
    Draft2DocumentReferenceResolver referenceResolver = new Draft2DocumentReferenceResolver();
    JsonNode resolvedRoot = readJsonNode("regular-json-pointer-resolved.json");
    Assert.assertEquals(referenceResolver.resolve(new File(filePath)), JSONHelper.writeObject(resolvedRoot));
  }

  @Test(expectedExceptions = { BindingException.class })
  public void testCircular() throws Exception {
    String filePath = Draft2ReferenceResolverTest.class.getResource("circular.json").getFile();
    
    Draft2DocumentReferenceResolver referenceResolver = new Draft2DocumentReferenceResolver();
    referenceResolver.resolve(new File(filePath));
  }
  
  private JsonNode readJsonNode(String inputFile) throws IOException {
    String input = ResourceHelper.readResource(this.getClass(), inputFile);
    return JSONHelper.readJsonNode(input);
  }
  
}
