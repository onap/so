package org.openecomp.mso.bpmn;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/tasks/*Test.class","**/infrastructure/aai/*Test.class"})
public class AllTasksTestsTestSuite {
  // the class remains empty,
  // used only as a holder for the above annotations
}
