package org.openecomp.mso.cloud;

import org.junit.Test;
import org.openecomp.mso.openpojo.rules.EqualsAndHashCodeTester;
import org.openecomp.mso.openpojo.rules.ToStringTester;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.EqualsAndHashCodeMatchRule;
import com.openpojo.validation.rule.impl.NoPrimitivesRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class CloudPojoTest {
	@Test
	public void pojoStructure() {
		test(PojoClassFactory.getPojoClass(CloudIdentity.class));
		test(PojoClassFactory.getPojoClass(CloudifyManager.class));
		test(PojoClassFactory.getPojoClass(CloudSite.class));
		test(PojoClassFactory.getPojoClass(CloudConfig.class));
	}
	
	private void test(PojoClass pojoClass) {
		Validator validator = ValidatorBuilder.create()
				.with(new EqualsAndHashCodeMatchRule())
				.with(new NoPrimitivesRule())
				.with(new NoPublicFieldsRule())
				.with(new SetterTester())
				.with(new GetterTester())
				.with(new ToStringTester())
				.with(new EqualsAndHashCodeTester())
				.build();
		validator.validate(pojoClass);
	}
}