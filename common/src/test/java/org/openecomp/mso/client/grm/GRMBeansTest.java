package org.openecomp.mso.client.grm;

import java.util.List;

import org.junit.Test;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.affirm.Affirm;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class GRMBeansTest {
	private static final int EXPECTED_CLASS_COUNT = 10;
	private static final String POJO_PACKAGE = "org.openecomp.mso.client.grm.beans";

	@Test
	public void ensureExpectedPojoCount() {
		List<PojoClass> pojoClasses = PojoClassFactory.getPojoClasses(	POJO_PACKAGE, new FilterPackageInfo());
		Affirm.affirmEquals("Classes added / removed?", EXPECTED_CLASS_COUNT, pojoClasses.size());
	}

	@Test
	public void testPojoStructureAndBehavior() {
		Validator validator = ValidatorBuilder.create()
								.with(new GetterMustExistRule())
								.with(new SetterMustExistRule())
								.with(new SetterTester())
								.with(new GetterTester())
								.build();

		validator.validate(POJO_PACKAGE, new FilterPackageInfo());
	}
}
