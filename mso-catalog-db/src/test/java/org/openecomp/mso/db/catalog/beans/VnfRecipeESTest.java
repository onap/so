/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 08:23:26 GMT 2016
 */

package org.openecomp.mso.db.catalog.beans;

import org.junit.Test;
import static org.junit.Assert.*;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class VnfRecipeESTest extends VnfRecipeESTestscaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      VnfRecipe vnfRecipe0 = new VnfRecipe();
      vnfRecipe0.setVnfParamXSD("@!JsE$ &");
      String string0 = vnfRecipe0.toString();
      assertEquals("RECIPE: null,uri=null,vnfParamXSD=@!JsE$ &,serviceType=null,vfModuleId=null", string0);
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      VnfRecipe vnfRecipe0 = new VnfRecipe();
      vnfRecipe0.setVnfType("<[sFL");
      String string0 = vnfRecipe0.getVnfType();
      assertEquals("<[sFL", string0);
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      VnfRecipe vnfRecipe0 = new VnfRecipe();
      vnfRecipe0.setVnfType("");
      String string0 = vnfRecipe0.getVnfType();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test3()  throws Throwable  {
      VnfRecipe vnfRecipe0 = new VnfRecipe();
      vnfRecipe0.setVnfParamXSD("");
      String string0 = vnfRecipe0.getVnfParamXSD();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test4()  throws Throwable  {
      VnfRecipe vnfRecipe0 = new VnfRecipe();
      vnfRecipe0.setVfModuleId("QL");
      String string0 = vnfRecipe0.getVfModuleId();
      assertEquals("QL", string0);
  }

  @Test(timeout = 4000)
  public void test5()  throws Throwable  {
      VnfRecipe vnfRecipe0 = new VnfRecipe();
      vnfRecipe0.setVfModuleId("");
      String string0 = vnfRecipe0.getVfModuleId();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test6()  throws Throwable  {
      VnfRecipe vnfRecipe0 = new VnfRecipe();
      String string0 = vnfRecipe0.getVnfParamXSD();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test7()  throws Throwable  {
      VnfRecipe vnfRecipe0 = new VnfRecipe();
      vnfRecipe0.setVnfParamXSD("RECIPE: null,uri=null,vnfParamXSD=null,serviceType=null,vfModuleId=null");
      String string0 = vnfRecipe0.getVnfParamXSD();
      assertEquals("RECIPE: null,uri=null,vnfParamXSD=null,serviceType=null,vfModuleId=null", string0);
  }

  @Test(timeout = 4000)
  public void test8()  throws Throwable  {
      VnfRecipe vnfRecipe0 = new VnfRecipe();
      String string0 = vnfRecipe0.getVfModuleId();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test9()  throws Throwable  {
      VnfRecipe vnfRecipe0 = new VnfRecipe();
      String string0 = vnfRecipe0.getVnfType();
      assertNull(string0);
  }
}
