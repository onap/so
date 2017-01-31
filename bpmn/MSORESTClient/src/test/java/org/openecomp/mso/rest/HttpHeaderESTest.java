/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 11:47:07 GMT 2016
 */

package org.openecomp.mso.rest;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.evosuite.runtime.PrivateAccess;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class HttpHeaderESTest extends HttpHeaderESTestscaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      HttpHeader httpHeader0 = new HttpHeader("Fw", "WD#>QF/v6_|_A");
      String string0 = httpHeader0.getValue();
      assertEquals("WD#>QF/v6_|_A", string0);
      assertEquals("Fw", httpHeader0.getName());
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      HttpHeader httpHeader0 = new HttpHeader("", "");
      String string0 = httpHeader0.getValue();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      HttpHeader httpHeader0 = new HttpHeader("Nae may no be null.", "Nae may no be null.");
      PrivateAccess.setVariable((Class<HttpHeader>) HttpHeader.class, httpHeader0, "name", (Object) null);
      String string0 = httpHeader0.getName();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test3()  throws Throwable  {
      HttpHeader httpHeader0 = new HttpHeader("", "EIqJp");
      String string0 = httpHeader0.getName();
      assertEquals("EIqJp", httpHeader0.getValue());
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test4()  throws Throwable  {
      HttpHeader httpHeader0 = null;
      try {
        httpHeader0 = new HttpHeader((String) null, (String) null);
        fail("Expecting exception: IllegalArgumentException");
      
      } catch(IllegalArgumentException e) {
         //
         // Name may not be null.
         //
         verifyException("org.openecomp.mso.rest.HttpHeader", e);
      }
  }

  @Test(timeout = 4000)
  public void test5()  throws Throwable  {
      HttpHeader httpHeader0 = new HttpHeader("Nae may no be null.", "Nae may no be null.");
      String string0 = httpHeader0.getName();
      assertEquals("Nae may no be null.", string0);
  }

  @Test(timeout = 4000)
  public void test6()  throws Throwable  {
      HttpHeader httpHeader0 = new HttpHeader("|SJ`pSz:BCB1o8~", (String) null);
      String string0 = httpHeader0.getValue();
      assertNull(string0);
  }
}
