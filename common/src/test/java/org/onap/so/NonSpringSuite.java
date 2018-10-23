package org.onap.so;

import org.junit.runner.RunWith;
import org.onap.so.test.categories.SpringAware;

import com.googlecode.junittoolbox.ExcludeCategories;
import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

@RunWith(WildcardPatternSuite.class)
@ExcludeCategories({SpringAware.class})
@SuiteClasses({"**/*Test.class"})
public class NonSpringSuite {

}
