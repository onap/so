package org.openecomp.mso.client.restproperties;

import org.junit.Test;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.NoPrimitivesRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsRule;
import com.openpojo.validation.test.impl.GetterTester;

public class RestPropertiesPojoTest {
	@Test
	public void pojoStructure() {
		test(PojoClassFactory.getPojoClass(PolicyPropertiesConfiguration.class));
		test(PojoClassFactory.getPojoClass(AaiPropertiesConfiguration.class));
		test(PojoClassFactory.getPojoClass(MsoPropertiesConfiguration.class));
	}
	
	private void test(PojoClass pojoClass) {
		Validator validator = ValidatorBuilder.create()
				.with(new NoPrimitivesRule())
				.with(new NoPublicFieldsRule())
				.with(new GetterTester())
				.build();
		validator.validate(pojoClass);
	}
}
