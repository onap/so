package org.openecomp.mso.bpmn.servicedecomposition;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.filters.FilterNonConcrete;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import org.junit.Test;
import org.openecomp.mso.openpojo.rules.EqualsAndHashCodeTester;
import org.openecomp.mso.openpojo.rules.HasEqualsAndHashCodeRule;

public class BBDecompPojoTest {

	private PojoClassFilter filterTestClasses = new FilterTestClasses();

	@Test
	public void pojoStructure() {
		test("org.openecomp.mso.bpmn.servicedecomposition.bbobjects");
	}

	private void test(String pojoPackage) {
		Validator validator = ValidatorBuilder.create()
				.with(new EqualsAndHashCodeTester())
				.with(new HasEqualsAndHashCodeRule())
				.build();
		validator.validate(pojoPackage, new FilterPackageInfo(), filterTestClasses, new FilterNonConcrete());
	}

	private static class FilterTestClasses implements PojoClassFilter {
		public boolean include(PojoClass pojoClass) {
			return !pojoClass.getSourcePath().contains("/test-classes/");
		}
	}
}
