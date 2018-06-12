package org.openecomp.mso.bpmn;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/service/*Test.class", "**/process/*Test.class", "**/subprocess/*Test.class"})
public class AllBPMNTestSuites {
  // the class remains empty,
  // used only as a holder for the above annotations
}
