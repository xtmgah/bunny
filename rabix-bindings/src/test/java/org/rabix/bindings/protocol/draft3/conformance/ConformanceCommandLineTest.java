package org.rabix.bindings.protocol.draft3.conformance;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.BindingException;
import org.rabix.bindings.model.Job;
import org.rabix.bindings.protocol.draft3.Draft3Bindings;
import org.rabix.common.helper.JSONHelper;
import org.rabix.common.helper.ResourceHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = { "functional" })
public class ConformanceCommandLineTest {

  @Test
  public void testBwaMemCmdLine() throws IOException {
    try {
      String inputsStr = ResourceHelper.readResource(this.getClass(), "bwa-mem-job.json");
      Map<String, Object> inputs = JSONHelper.readMap(inputsStr);
      
      String commandLineToolStr = ResourceHelper.readResource(this.getClass(), "bwa-mem-tool.cwl");
      
      List<Object> expectedList = new LinkedList<Object>();
      expectedList.add("bwa");
      expectedList.add("mem");
      expectedList.add("-t");
      expectedList.add("4");
      expectedList.add("-I");
      expectedList.add("1,2,3,4");
      expectedList.add("-m");
      expectedList.add("3");
      expectedList.add("rabix/tests/test-files/chr20.fa");
      expectedList.add("rabix/tests/test-files/example_human_Illumina.pe_1.fastq");
      expectedList.add("rabix/tests/test-files/example_human_Illumina.pe_2.fastq");

      Job job = new Job(commandLineToolStr, inputs);
      List<?> resultList = new Draft3Bindings().buildCommandLineParts(job);
      Assert.assertNotNull(resultList);
      Assert.assertEquals(resultList.size(), expectedList.size());
      Assert.assertEquals(resultList, expectedList);
    } catch (BindingException e) {
      Assert.fail(e.getMessage());
    }
  }

}
