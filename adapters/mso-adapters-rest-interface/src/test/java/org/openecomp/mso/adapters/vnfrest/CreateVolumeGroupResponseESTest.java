/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 09:22:14 GMT 2016
 */

package org.openecomp.mso.adapters.vnfrest;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class CreateVolumeGroupResponseESTest extends CreateVolumeGroupResponseESTestscaffolding {

  @Test(timeout = 4000)
  public void test00()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      createVolumeGroupResponse0.setVolumeGroupStackId("#soQ+\"O.VGnL");
      String string0 = createVolumeGroupResponse0.getVolumeGroupStackId();
      assertEquals("#soQ+\"O.VGnL", string0);
  }

  @Test(timeout = 4000)
  public void test01()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      createVolumeGroupResponse0.setVolumeGroupOutputs((Map<String, String>) null);
      Map<String, String> map0 = createVolumeGroupResponse0.getVolumeGroupOutputs();
      assertNull(map0);
  }

  @Test(timeout = 4000)
  public void test02()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      HashMap<String, String> hashMap0 = new HashMap<String, String>();
      hashMap0.put(",MyEf", ",MyEf");
      createVolumeGroupResponse0.setVolumeGroupOutputs(hashMap0);
      Map<String, String> map0 = createVolumeGroupResponse0.getVolumeGroupOutputs();
      assertFalse(map0.isEmpty());
  }

  @Test(timeout = 4000)
  public void test03()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      createVolumeGroupResponse0.setVolumeGroupId(".X(v-Tvwzh&");
      String string0 = createVolumeGroupResponse0.getVolumeGroupId();
      assertEquals(".X(v-Tvwzh&", string0);
  }

  @Test(timeout = 4000)
  public void test04()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      createVolumeGroupResponse0.setVolumeGroupId("");
      String string0 = createVolumeGroupResponse0.getVolumeGroupId();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test05()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      Boolean boolean0 = Boolean.valueOf(true);
      createVolumeGroupResponse0.setVolumeGroupCreated(boolean0);
      Boolean boolean1 = createVolumeGroupResponse0.getVolumeGroupCreated();
      assertTrue(boolean1);
  }

  @Test(timeout = 4000)
  public void test06()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      Boolean boolean0 = new Boolean("^MG80I,4g3M>=01Xp");
      createVolumeGroupResponse0.setVolumeGroupCreated(boolean0);
      Boolean boolean1 = createVolumeGroupResponse0.getVolumeGroupCreated();
      assertFalse(boolean1);
  }

  @Test(timeout = 4000)
  public void test07()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      createVolumeGroupResponse0.setVolumeGroupStackId("");
      String string0 = createVolumeGroupResponse0.getVolumeGroupStackId();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test08()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      VolumeGroupRollback volumeGroupRollback0 = createVolumeGroupResponse0.getVolumeGroupRollback();
      volumeGroupRollback0.setVolumeGroupCreated(true);
      VolumeGroupRollback volumeGroupRollback1 = createVolumeGroupResponse0.getVolumeGroupRollback();
      assertSame(volumeGroupRollback1, volumeGroupRollback0);
  }

  @Test(timeout = 4000)
  public void test09()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      createVolumeGroupResponse0.setVolumeGroupRollback((VolumeGroupRollback) null);
      VolumeGroupRollback volumeGroupRollback0 = createVolumeGroupResponse0.getVolumeGroupRollback();
      assertNull(volumeGroupRollback0);
  }

  @Test(timeout = 4000)
  public void test10()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      Boolean boolean0 = createVolumeGroupResponse0.getVolumeGroupCreated();
      assertNull(boolean0);
  }

  @Test(timeout = 4000)
  public void test11()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      String string0 = createVolumeGroupResponse0.getVolumeGroupStackId();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test12()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      String string0 = createVolumeGroupResponse0.getVolumeGroupId();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test13()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      Map<String, String> map0 = createVolumeGroupResponse0.getVolumeGroupOutputs();
      createVolumeGroupResponse0.setVolumeGroupOutputs(map0);
      assertNull(createVolumeGroupResponse0.getVolumeGroupId());
  }

  @Test(timeout = 4000)
  public void test14()  throws Throwable  {
      CreateVolumeGroupResponse createVolumeGroupResponse0 = new CreateVolumeGroupResponse();
      Map<String, String> map0 = createVolumeGroupResponse0.getVolumeGroupOutputs();
      CreateVolumeGroupResponse createVolumeGroupResponse1 = new CreateVolumeGroupResponse("", "", (Boolean) null, map0, (VolumeGroupRollback) null, "");
      assertEquals("", createVolumeGroupResponse1.getVolumeGroupStackId());
  }
}
