/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 08:51:02 GMT 2016
 */

package org.openecomp.mso.openstack.exceptions;

import org.junit.Test;
import static org.junit.Assert.*;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class MsoAdapterExceptionESTest extends MsoAdapterExceptionESTestscaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      MsoAdapterException msoAdapterException0 = new MsoAdapterException("");
      MsoAdapterException msoAdapterException1 = new MsoAdapterException("", (Throwable) msoAdapterException0);
      assertFalse(msoAdapterException1.equals((Object)msoAdapterException0));
  }
}
