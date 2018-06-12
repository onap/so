package org.openecomp.mso;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*Test.class"})
public class AllTestSuites {
  // the class remains empty,
  // used only as a holder for the above annotations
}
