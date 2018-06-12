package org.openecomp.mso.openstack.beans;

import org.junit.Test;
import org.openecomp.mso.BaseTest;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class OpenstackBeansPojoTest extends BaseTest {
	@Test
	public void pojoStructure() {
		test(PojoClassFactory.getPojoClass(VnfRollback.class));
		test(PojoClassFactory.getPojoClass(NeutronCacheEntry.class));
		test(PojoClassFactory.getPojoClass(HeatCacheEntry.class));
	}
	
	private void test(PojoClass pojoClass) {
		Validator validator = ValidatorBuilder.create()
				.with(new SetterTester())
				.with(new GetterTester())
				.build();
		validator.validate(pojoClass);
	}
}
