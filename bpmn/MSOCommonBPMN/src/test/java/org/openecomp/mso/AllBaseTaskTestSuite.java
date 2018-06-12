package org.openecomp.mso;

import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/common/aai/tasks/*Test.class", "**/bpmn/sdno/tasks/*Test.class", "**/buildingblock/SniroHomingTest.class"})
public class AllBaseTaskTestSuite {
	// the class remains empty,
	// used only as a holder for the above annotations
}
