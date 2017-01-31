/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 09:24:56 GMT 2016
 */

package org.openecomp.mso.adapters.vnfrest;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;

import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class VfModuleExceptionResponseESTest extends VfModuleExceptionResponseESTestscaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      MsoExceptionCategory msoExceptionCategory0 = MsoExceptionCategory.OPENSTACK;
      VfModuleExceptionResponse vfModuleExceptionResponse0 = new VfModuleExceptionResponse("2m", msoExceptionCategory0, false, "2m");
      assertEquals(MsoExceptionCategory.OPENSTACK, vfModuleExceptionResponse0.getCategory());
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      VfModuleExceptionResponse vfModuleExceptionResponse0 = new VfModuleExceptionResponse();
      assertNull(vfModuleExceptionResponse0.getCategory());
      
      MsoExceptionCategory msoExceptionCategory0 = MsoExceptionCategory.USERDATA;
      Boolean boolean0 = new Boolean(false);
      vfModuleExceptionResponse0.setMessage("jaxb.formatted.output");
      vfModuleExceptionResponse0.setRolledBack(boolean0);
      vfModuleExceptionResponse0.setMessage("jaxb.formatted.output");
      vfModuleExceptionResponse0.setMessageId("jaxb.formatted.output");
      Boolean.logicalXor(false, false);
      Boolean.getBoolean((String) null);
      vfModuleExceptionResponse0.setRolledBack(boolean0);
      vfModuleExceptionResponse0.setCategory(msoExceptionCategory0);
      vfModuleExceptionResponse0.setMessageId("OPENSTACK");
      VfModuleExceptionResponse vfModuleExceptionResponse1 = new VfModuleExceptionResponse();
      Boolean boolean1 = vfModuleExceptionResponse0.getRolledBack();
      assertFalse(boolean1);
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      MsoExceptionCategory msoExceptionCategory0 = MsoExceptionCategory.USERDATA;
      VfModuleExceptionResponse vfModuleExceptionResponse0 = new VfModuleExceptionResponse("", msoExceptionCategory0, true, "");
      vfModuleExceptionResponse0.setMessageId("");
      Boolean boolean0 = vfModuleExceptionResponse0.getRolledBack();
      assertTrue(boolean0);
      
      Boolean.logicalAnd(false, false);
      MsoExceptionCategory msoExceptionCategory1 = MsoExceptionCategory.USERDATA;
      Boolean.logicalAnd(true, true);
      vfModuleExceptionResponse0.setRolledBack(boolean0);
      VfModuleExceptionResponse vfModuleExceptionResponse1 = new VfModuleExceptionResponse("&g.0W4Nah+8,", msoExceptionCategory1, true, "");
      vfModuleExceptionResponse0.setMessageId((String) null);
      VfModuleExceptionResponse vfModuleExceptionResponse2 = new VfModuleExceptionResponse((String) null, msoExceptionCategory1, false, (String) null);
      VfModuleExceptionResponse vfModuleExceptionResponse3 = new VfModuleExceptionResponse("&g.0W4Nah+8,");
      vfModuleExceptionResponse0.getMessage();
      VfModuleExceptionResponse vfModuleExceptionResponse4 = new VfModuleExceptionResponse();
      vfModuleExceptionResponse1.setRolledBack((Boolean) null);
      assertEquals("&g.0W4Nah+8,", vfModuleExceptionResponse1.getMessage());
  }

  @Test(timeout = 4000)
  public void test3()  throws Throwable  {
      VfModuleExceptionResponse vfModuleExceptionResponse0 = new VfModuleExceptionResponse("org.openecomp.mso.openstack.exceptions.MsoExceptionCategory");
      MsoExceptionCategory msoExceptionCategory0 = MsoExceptionCategory.INTERNAL;
      vfModuleExceptionResponse0.setCategory(msoExceptionCategory0);
      String string0 = vfModuleExceptionResponse0.getMessage();
      assertEquals("org.openecomp.mso.openstack.exceptions.MsoExceptionCategory", string0);
  }

  @Test(timeout = 4000)
  public void test4()  throws Throwable  {
      MsoExceptionCategory msoExceptionCategory0 = MsoExceptionCategory.IO;
      String string0 = "";
      VfModuleExceptionResponse vfModuleExceptionResponse0 = new VfModuleExceptionResponse("XOGaF|t", msoExceptionCategory0, true, "");
      // Undeclared exception!
      try { 
        vfModuleExceptionResponse0.toJsonString();
        fail("Expecting exception: VerifyError");
      
      } catch(VerifyError e) {
         //
         // (class: org/codehaus/jackson/map/MapperConfig, method: <clinit> signature: ()V) Bad type in putfield/putstatic
         //
         verifyException("org.codehaus.jackson.map.ObjectMapper", e);
      }
  }

  @Test(timeout = 4000)
  public void test5()  throws Throwable  {
      VfModuleExceptionResponse vfModuleExceptionResponse0 = new VfModuleExceptionResponse();
      vfModuleExceptionResponse0.setMessage("");
      vfModuleExceptionResponse0.setMessage("");
      vfModuleExceptionResponse0.toXmlString();
      vfModuleExceptionResponse0.setMessage("org.openecomp.mso.adapters.vnfrest.VfModuleExceptionResponse");
      VfModuleExceptionResponse vfModuleExceptionResponse1 = new VfModuleExceptionResponse("org.openecomp.mso.adapters.vnfrest.VfModuleExceptionResponse");
      MsoExceptionCategory msoExceptionCategory0 = MsoExceptionCategory.INTERNAL;
      vfModuleExceptionResponse1.toXmlString();
      vfModuleExceptionResponse1.setCategory(msoExceptionCategory0);
      vfModuleExceptionResponse0.setMessage((String) null);
      MsoExceptionCategory msoExceptionCategory1 = vfModuleExceptionResponse1.getCategory();
      VfModuleExceptionResponse vfModuleExceptionResponse2 = new VfModuleExceptionResponse();
      vfModuleExceptionResponse1.setCategory(msoExceptionCategory1);
      VfModuleExceptionResponse vfModuleExceptionResponse3 = new VfModuleExceptionResponse("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<vfModuleException>\n    <message>org.openecomp.mso.adapters.vnfrest.VfModuleExceptionResponse</message>\n</vfModuleException>\n");
      vfModuleExceptionResponse3.getRolledBack();
      vfModuleExceptionResponse3.getMessage();
      vfModuleExceptionResponse3.setRolledBack((Boolean) null);
      Boolean boolean0 = Boolean.TRUE;
      Boolean.getBoolean("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<vfModuleException>\n    <message>org.openecomp.mso.adapters.vnfrest.VfModuleExceptionResponse</message>\n</vfModuleException>\n");
      vfModuleExceptionResponse0.setRolledBack(boolean0);
      vfModuleExceptionResponse0.getCategory();
  }

  @Test(timeout = 4000)
  public void test6()  throws Throwable  {
      VfModuleExceptionResponse vfModuleExceptionResponse0 = new VfModuleExceptionResponse();
      vfModuleExceptionResponse0.setMessageId("cO)VBma");
      vfModuleExceptionResponse0.getMessage();
      VfModuleExceptionResponse vfModuleExceptionResponse1 = new VfModuleExceptionResponse();
      Boolean boolean0 = vfModuleExceptionResponse0.getRolledBack();
      assertNull(boolean0);
  }

  @Test(timeout = 4000)
  public void test7()  throws Throwable  {
      MsoExceptionCategory msoExceptionCategory0 = MsoExceptionCategory.OPENSTACK;
      VfModuleExceptionResponse vfModuleExceptionResponse0 = new VfModuleExceptionResponse("IxX(PnBaVq=pz", msoExceptionCategory0, false, "");
      Boolean boolean0 = Boolean.valueOf(false);
      Boolean.logicalAnd(true, false);
      vfModuleExceptionResponse0.setRolledBack(boolean0);
      assertEquals("IxX(PnBaVq=pz", vfModuleExceptionResponse0.getMessage());
  }
}
