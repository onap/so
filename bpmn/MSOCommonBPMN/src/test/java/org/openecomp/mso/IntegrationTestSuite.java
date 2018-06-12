package org.openecomp.mso;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*IT.class"})
public class IntegrationTestSuite {
  // the class remains empty,
  // used only as a holder for the above annotations
}
