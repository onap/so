package org.openecomp.mso.bpmn;

import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"!**/service/*Test.class", "!**/subprocess/*Test.class", "!**/process/*Test.class", 
	"!**/tasks/*Test.class", "!**/infrastructure/aai/*Test.class", 
	"!**/scripts/*Test.class", "**/*Test.class"})
public class AllTestsTestSuite {
	// the class remains empty,
	// used only as a holder for the above annotations
}
