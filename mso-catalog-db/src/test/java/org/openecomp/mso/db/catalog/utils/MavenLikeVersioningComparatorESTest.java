/*
 * This file was automatically generated by EvoSuite
 * Mon Nov 14 08:20:06 GMT 2016
 */

package org.openecomp.mso.db.catalog.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, useJEE = true) 
public class MavenLikeVersioningComparatorESTest extends MavenLikeVersioningComparatorESTestscaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      MavenLikeVersioningComparator mavenLikeVersioningComparator0 = new MavenLikeVersioningComparator();
      MavenLikeVersioning mavenLikeVersioning0 = new MavenLikeVersioning();
      mavenLikeVersioning0.setVersion("");
      // Undeclared exception!
      try { 
        mavenLikeVersioningComparator0.compare(mavenLikeVersioning0, mavenLikeVersioning0);
        fail("Expecting exception: NumberFormatException");
      
      } catch(NumberFormatException e) {
         //
         // For input string: \"\"
         //
         verifyException("java.lang.NumberFormatException", e);
      }
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      MavenLikeVersioningComparator mavenLikeVersioningComparator0 = new MavenLikeVersioningComparator();
      MavenLikeVersioning mavenLikeVersioning0 = new MavenLikeVersioning();
      // Undeclared exception!
      try { 
        mavenLikeVersioningComparator0.compare((MavenLikeVersioning) null, mavenLikeVersioning0);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.openecomp.mso.db.catalog.utils.MavenLikeVersioningComparator", e);
      }
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      MavenLikeVersioningComparator mavenLikeVersioningComparator0 = new MavenLikeVersioningComparator();
      MavenLikeVersioning mavenLikeVersioning0 = new MavenLikeVersioning();
      int int0 = mavenLikeVersioningComparator0.compare(mavenLikeVersioning0, mavenLikeVersioning0);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test3()  throws Throwable  {
      MavenLikeVersioningComparator mavenLikeVersioningComparator0 = new MavenLikeVersioningComparator();
      MavenLikeVersioning mavenLikeVersioning0 = new MavenLikeVersioning();
      mavenLikeVersioning0.setVersion("");
      MavenLikeVersioning mavenLikeVersioning1 = new MavenLikeVersioning();
      int int0 = mavenLikeVersioningComparator0.compare(mavenLikeVersioning0, mavenLikeVersioning1);
      assertEquals((-1), int0);
  }
}
