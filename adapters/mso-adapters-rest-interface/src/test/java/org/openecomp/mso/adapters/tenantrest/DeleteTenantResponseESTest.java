/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 09:19:42 GMT 2016
 */

package org.openecomp.mso.adapters.tenantrest;

import org.junit.Test;
import static org.junit.Assert.*;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class DeleteTenantResponseESTest extends DeleteTenantResponseESTestscaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      DeleteTenantResponse deleteTenantResponse0 = new DeleteTenantResponse();
      Boolean boolean0 = Boolean.valueOf("");
      deleteTenantResponse0.setTenantDeleted(boolean0);
      Boolean boolean1 = deleteTenantResponse0.getTenantDeleted();
      assertFalse(boolean1);
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      DeleteTenantResponse deleteTenantResponse0 = new DeleteTenantResponse();
      Boolean boolean0 = deleteTenantResponse0.getTenantDeleted();
      assertNull(boolean0);
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      DeleteTenantResponse deleteTenantResponse0 = new DeleteTenantResponse();
      Boolean boolean0 = new Boolean(true);
      deleteTenantResponse0.setTenantDeleted(boolean0);
      Boolean boolean1 = deleteTenantResponse0.getTenantDeleted();
      assertTrue(boolean1);
  }
}
