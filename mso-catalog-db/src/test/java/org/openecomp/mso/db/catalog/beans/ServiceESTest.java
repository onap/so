/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 08:19:44 GMT 2016
 */

package org.openecomp.mso.db.catalog.beans;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.shaded.org.mockito.Mockito.*;
import static org.evosuite.runtime.EvoAssertions.*;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.evosuite.runtime.ViolatedAssumptionAnswer;
import org.evosuite.runtime.mock.java.time.MockClock;
import org.evosuite.runtime.mock.java.time.MockLocalDateTime;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class ServiceESTest extends ServiceESTestscaffolding {

  @Test(timeout = 4000)
  public void test00()  throws Throwable  {
      Service service0 = new Service();
      service0.setServiceVersion("RECIPE: ");
      String string0 = service0.getServiceVersion();
      assertEquals("RECIPE: ", string0);
  }

  @Test(timeout = 4000)
  public void test01()  throws Throwable  {
      Service service0 = new Service();
      service0.setServiceNameVersionId(">{PeD}EDcITG;{Z%FH");
      String string0 = service0.getServiceNameVersionId();
      assertEquals(">{PeD}EDcITG;{Z%FH", string0);
  }

  @Test(timeout = 4000)
  public void test02()  throws Throwable  {
      Service service0 = new Service();
      service0.setServiceName("RECIPE: ");
      String string0 = service0.getServiceName();
      assertEquals("RECIPE: ", string0);
  }

  @Test(timeout = 4000)
  public void test03()  throws Throwable  {
      Service service0 = new Service();
      service0.setServiceName("");
      String string0 = service0.getServiceName();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test04()  throws Throwable  {
      Service service0 = new Service();
      HashMap<String, ServiceRecipe> hashMap0 = new HashMap<String, ServiceRecipe>();
      ServiceRecipe serviceRecipe0 = new ServiceRecipe();
      hashMap0.put(",created=", serviceRecipe0);
      service0.setRecipes(hashMap0);
      Map<String, ServiceRecipe> map0 = service0.getRecipes();
      assertFalse(map0.isEmpty());
  }

  @Test(timeout = 4000)
  public void test05()  throws Throwable  {
      Service service0 = new Service();
      service0.setModelInvariantUUID("BZ@s");
      String string0 = service0.getModelInvariantUUID();
      assertEquals("BZ@s", string0);
  }

  @Test(timeout = 4000)
  public void test06()  throws Throwable  {
      Service service0 = new Service();
      service0.setId(1033);
      int int0 = service0.getId();
      assertEquals(1033, int0);
  }

  @Test(timeout = 4000)
  public void test07()  throws Throwable  {
      Service service0 = new Service();
      service0.setId((-164));
      int int0 = service0.getId();
      assertEquals((-164), int0);
  }

  @Test(timeout = 4000)
  public void test08()  throws Throwable  {
      Service service0 = new Service();
      service0.setHttpMethod(",description=");
      String string0 = service0.getHttpMethod();
      assertEquals(",description=", string0);
  }

  @Test(timeout = 4000)
  public void test09()  throws Throwable  {
      Service service0 = new Service();
      service0.setDescription("");
      String string0 = service0.getDescription();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test10()  throws Throwable  {
      Service service0 = new Service();
      Timestamp timestamp0 = new Timestamp(0L);
      service0.setCreated(timestamp0);
      Timestamp timestamp1 = service0.getCreated();
      assertEquals(0, timestamp1.getNanos());
  }

  @Test(timeout = 4000)
  public void test11()  throws Throwable  {
      Service service0 = new Service();
      Clock clock0 = MockClock.systemUTC();
      LocalDateTime localDateTime0 = MockLocalDateTime.now(clock0);
      Timestamp timestamp0 = Timestamp.valueOf(localDateTime0);
      service0.setCreated(timestamp0);
      Timestamp timestamp1 = service0.getCreated();
      assertSame(timestamp1, timestamp0);
  }

  @Test(timeout = 4000)
  public void test12()  throws Throwable  {
      Service service0 = new Service();
      // Undeclared exception!
      try { 
        service0.toString();
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.openecomp.mso.db.catalog.beans.Service", e);
      }
  }

  @Test(timeout = 4000)
  public void test13()  throws Throwable  {
      Service service0 = new Service();
      HashMap<String, ServiceRecipe> hashMap0 = new HashMap<String, ServiceRecipe>();
      ServiceRecipe serviceRecipe0 = new ServiceRecipe();
      hashMap0.put(",created=", serviceRecipe0);
      service0.setRecipes(hashMap0);
      String string0 = service0.toString();
      assertEquals("SERVICE: id=0,name=null,version=null,description=null,modelInvariantUUID=null\nRECIPE: null,uri=null", string0);
  }

  @Test(timeout = 4000)
  public void test14()  throws Throwable  {
      Service service0 = new Service();
      service0.setServiceNameVersionId("83y*#72]},kHXOT");
      service0.setServiceNameVersionId("f6t}qujI)DMM>b=J");
      Map<String, ServiceRecipe> map0 = (Map<String, ServiceRecipe>) mock(Map.class, new ViolatedAssumptionAnswer());
      doReturn((String) null).when(map0).toString();
      doReturn((Set) null).when(map0).keySet();
      service0.setRecipes(map0);
      service0.isTheSameVersion("83y*#72]},kHXOT");
      service0.isMoreRecentThan(")0");
      service0.getRecipes();
      // Undeclared exception!
      try { 
        service0.toString();
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.openecomp.mso.db.catalog.beans.Service", e);
      }
  }

  @Test(timeout = 4000)
  public void test15()  throws Throwable  {
      Service service0 = new Service();
      String string0 = service0.getDescription();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test16()  throws Throwable  {
      Service service0 = new Service();
      HashMap<String, ServiceRecipe> hashMap0 = new HashMap<String, ServiceRecipe>();
      service0.setRecipes(hashMap0);
      Map<String, ServiceRecipe> map0 = service0.getRecipes();
      assertEquals(0, map0.size());
  }

  @Test(timeout = 4000)
  public void test17()  throws Throwable  {
      Service service0 = new Service();
      int int0 = service0.getId();
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test18()  throws Throwable  {
      Service service0 = new Service();
      String string0 = service0.getServiceName();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test19()  throws Throwable  {
      Service service0 = new Service();
      service0.setHttpMethod("");
      String string0 = service0.getHttpMethod();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test20()  throws Throwable  {
      Service service0 = new Service();
      String string0 = service0.getModelInvariantUUID();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test21()  throws Throwable  {
      Service service0 = new Service();
      String string0 = service0.getServiceVersion();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test22()  throws Throwable  {
      Service service0 = new Service();
      service0.setServiceNameVersionId("");
      String string0 = service0.getServiceNameVersionId();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test23()  throws Throwable  {
      Service service0 = new Service();
      String string0 = service0.getHttpMethod();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test24()  throws Throwable  {
      Service service0 = new Service();
      Map<String, ServiceRecipe> map0 = service0.getRecipes();
      assertNull(map0);
  }

  @Test(timeout = 4000)
  public void test25()  throws Throwable  {
      Service service0 = new Service();
      service0.setDescription("Ir%#'ua8B=h&yW\"(|");
      String string0 = service0.getDescription();
      assertEquals("Ir%#'ua8B=h&yW\"(|", string0);
  }

  @Test(timeout = 4000)
  public void test26()  throws Throwable  {
      Service service0 = new Service();
      String string0 = service0.getServiceNameVersionId();
      assertNull(string0);
  }

  @Test(timeout = 4000)
  public void test27()  throws Throwable  {
      Service service0 = new Service();
      service0.setServiceVersion("");
      String string0 = service0.getServiceVersion();
      assertEquals("", string0);
  }

  @Test(timeout = 4000)
  public void test28()  throws Throwable  {
      Service service0 = new Service();
      Timestamp timestamp0 = service0.getCreated();
      service0.setCreated(timestamp0);
      assertNull(service0.getServiceName());
  }

  @Test(timeout = 4000)
  public void test29()  throws Throwable  {
      Service service0 = new Service();
      service0.setModelInvariantUUID("");
      String string0 = service0.getModelInvariantUUID();
      assertEquals("", string0);
  }
}
