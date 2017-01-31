/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 09:19:02 GMT 2016
 */

package org.openecomp.mso.adapters.tenantrest;

import org.junit.Test;
import static org.junit.Assert.*;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class RollbackTenantResponseESTest extends RollbackTenantResponseESTestscaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      RollbackTenantResponse rollbackTenantResponse0 = new RollbackTenantResponse();
      Boolean boolean0 = Boolean.TRUE;
      rollbackTenantResponse0.setTenantRolledback(boolean0);
      Boolean boolean1 = rollbackTenantResponse0.getTenantRolledback();
      assertTrue(boolean1);
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      RollbackTenantResponse rollbackTenantResponse0 = new RollbackTenantResponse();
      Boolean boolean0 = Boolean.valueOf(":;W(ksM>u2+");
      rollbackTenantResponse0.setTenantRolledback(boolean0);
      Boolean boolean1 = rollbackTenantResponse0.getTenantRolledback();
      assertFalse(boolean1);
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      RollbackTenantResponse rollbackTenantResponse0 = new RollbackTenantResponse();
      Boolean boolean0 = rollbackTenantResponse0.getTenantRolledback();
      assertNull(boolean0);
  }
}
