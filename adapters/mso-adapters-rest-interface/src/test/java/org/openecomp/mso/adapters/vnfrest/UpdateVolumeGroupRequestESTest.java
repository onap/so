/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 09:13:54 GMT 2016
 */

package org.openecomp.mso.adapters.vnfrest;

import org.junit.Test;
import static org.junit.Assert.*;

import org.openecomp.mso.entity.MsoRequest;
import java.util.HashMap;
import java.util.Map;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class UpdateVolumeGroupRequestESTest extends UpdateVolumeGroupRequestESTestscaffolding {

  @Test(timeout = 4000)
  public void test00()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setVolumeGroupStackId("");
      String string0 = updateVolumeGroupRequest0.getVolumeGroupStackId();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test01()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setVolumeGroupParams((Map<String, String>) null);
      Map<String, String> map0 = updateVolumeGroupRequest0.getVolumeGroupParams();
      assertNull(map0);
  }

  @Test(timeout = 4000)
  public void test02()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      HashMap<String, String> hashMap0 = new HashMap<String, String>();
      hashMap0.put("x; 6d0BPfz`", "x; 6d0BPfz`");
      updateVolumeGroupRequest0.setVolumeGroupParams(hashMap0);
      Map<String, String> map0 = updateVolumeGroupRequest0.getVolumeGroupParams();
      assertFalse(map0.isEmpty());
  }

  @Test(timeout = 4000)
  public void test03()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setVolumeGroupId("jaxb.formatted.output");
      String string0 = updateVolumeGroupRequest0.getVolumeGroupId();
      assertEquals("jaxb.formatted.output", string0);
  }

  @Test(timeout = 4000)
  public void test04()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setVnfVersion("VE");
      String string0 = updateVolumeGroupRequest0.getVnfVersion();
      assertEquals("VE", string0);
  }

  @Test(timeout = 4000)
  public void test05()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setVnfType(";z<~4]OiR");
      String string0 = updateVolumeGroupRequest0.getVnfType();
      assertEquals(";z<~4]OiR", string0);
  }

  @Test(timeout = 4000)
  public void test06()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setVnfType("");
      String string0 = updateVolumeGroupRequest0.getVnfType();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test07()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setVfModuleType("jaxb.formatted.output");
      String string0 = updateVolumeGroupRequest0.getVfModuleType();
      assertEquals("jaxb.formatted.output", string0);
  }

  @Test(timeout = 4000)
  public void test08()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setTenantId("*\"Y)Ey _n!jPx[,gv");
      String string0 = updateVolumeGroupRequest0.getTenantId();
      assertEquals("*\"Y)Ey _n!jPx[,gv", string0);
  }

  @Test(timeout = 4000)
  public void test09()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setCloudSiteId("kaFalmm'SWu=)x");
      String string0 = updateVolumeGroupRequest0.getCloudSiteId();
      assertEquals("kaFalmm'SWu=)x", string0);
  }

  @Test(timeout = 4000)
  public void test10()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      String string0 = updateVolumeGroupRequest0.getVnfVersion();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test11()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      String string0 = updateVolumeGroupRequest0.getVfModuleType();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test12()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      String string0 = updateVolumeGroupRequest0.getCloudSiteId();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test13()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      MsoRequest msoRequest0 = updateVolumeGroupRequest0.getMsoRequest();
      assertNull(msoRequest0.getRequestId());
  }

  @Test(timeout = 4000)
  public void test14()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      String string0 = updateVolumeGroupRequest0.getVnfType();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test15()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      String string0 = updateVolumeGroupRequest0.getTenantId();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test16()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setTenantId("");
      String string0 = updateVolumeGroupRequest0.getTenantId();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test17()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setVolumeGroupStackId("%CmN&s3>7F)p/0");
      String string0 = updateVolumeGroupRequest0.getVolumeGroupStackId();
      assertEquals("%CmN&s3>7F)p/0", string0);
  }

  @Test(timeout = 4000)
  public void test18()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setVfModuleType("");
      String string0 = updateVolumeGroupRequest0.getVfModuleType();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test19()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setCloudSiteId("");
      String string0 = updateVolumeGroupRequest0.getCloudSiteId();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test20()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setVnfVersion("");
      String string0 = updateVolumeGroupRequest0.getVnfVersion();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test21()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setMsoRequest((MsoRequest) null);
      MsoRequest msoRequest0 = updateVolumeGroupRequest0.getMsoRequest();
      assertNull(msoRequest0);
  }

  @Test(timeout = 4000)
  public void test22()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      updateVolumeGroupRequest0.setVolumeGroupId("");
      String string0 = updateVolumeGroupRequest0.getVolumeGroupId();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test23()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      Map<String, String> map0 = updateVolumeGroupRequest0.getVolumeGroupParams();
      updateVolumeGroupRequest0.setVolumeGroupParams(map0);
      assertNull(updateVolumeGroupRequest0.getCloudSiteId());
  }

  @Test(timeout = 4000)
  public void test24()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      String string0 = updateVolumeGroupRequest0.getVolumeGroupStackId();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test25()  throws Throwable  {
      UpdateVolumeGroupRequest updateVolumeGroupRequest0 = new UpdateVolumeGroupRequest();
      String string0 = updateVolumeGroupRequest0.getVolumeGroupId();
      assertNull(string0);
  }
}
