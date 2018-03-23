package org.openecomp.mso.adapters.vdu;

import org.junit.Test;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class BeansTest {

	private PojoClassFilter filterTestClasses = new FilterTestClasses();

	@Test
	public void pojoStructure() {
		test("org.openecomp.mso.adapters.vdu");
	}

	private void test(String pojoPackage) {
		Validator validator = ValidatorBuilder.create()
				.with(new GetterMustExistRule())
				.with(new SetterTester())
				.with(new GetterTester())
				.build();
		validator.validate(pojoPackage, new FilterPackageInfo(), filterTestClasses);
	}
	private static class FilterTestClasses implements PojoClassFilter {
		public boolean include(PojoClass pojoClass) {
			return !pojoClass.getSourcePath().contains("/test-classes/");
		}
	}
}
