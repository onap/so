/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 09:27:07 GMT 2016
 */

package org.openecomp.mso.adapters.nwrest;

import org.junit.Test;
import static org.junit.Assert.*;

import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class RollbackNetworkErrorESTest extends RollbackNetworkErrorESTestscaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      MsoExceptionCategory msoExceptionCategory0 = MsoExceptionCategory.USERDATA;
      RollbackNetworkError rollbackNetworkError0 = new RollbackNetworkError("$/e2Fa;", msoExceptionCategory0, false, "org.openecomp.mso.openstack.exceptions.MsoExceptionCategory");
      assertEquals("$/e2Fa;", rollbackNetworkError0.getMessage());
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      RollbackNetworkError rollbackNetworkError0 = new RollbackNetworkError();
      assertNull(rollbackNetworkError0.getMessageId());
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      RollbackNetworkError rollbackNetworkError0 = new RollbackNetworkError("6.4B5l)6k@iZM");
      assertNull(rollbackNetworkError0.getCategory());
  }
}
