package org.openecomp.mso.bpmn.common.adapter.vnf;

import org.junit.Test;
import org.openecomp.mso.openpojo.rules.ToStringTester;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class VnfAdapterPojoTest {
	@Test
	public void pojoStructure() {
		test(PojoClassFactory.getPojoClass(CreateVnfNotification.class));
		test(PojoClassFactory.getPojoClass(DeleteVnfNotification.class));
		test(PojoClassFactory.getPojoClass(MsoRequest.class));
		test(PojoClassFactory.getPojoClass(QueryVnfNotification.class));
		test(PojoClassFactory.getPojoClass(RollbackVnfNotification.class));
		test(PojoClassFactory.getPojoClass(UpdateVnfNotification.class));
		test(PojoClassFactory.getPojoClass(VnfRollback.class));
		test(PojoClassFactory.getPojoClass(org.openecomp.mso.bpmn.common.adapter.vnf.QueryVnfNotification.Outputs.class));
		test(PojoClassFactory.getPojoClass(org.openecomp.mso.bpmn.common.adapter.vnf.CreateVnfNotification.Outputs.class));
		test(PojoClassFactory.getPojoClass(org.openecomp.mso.bpmn.common.adapter.vnf.UpdateVnfNotification.Outputs.class));
		test(PojoClassFactory.getPojoClass(org.openecomp.mso.bpmn.common.adapter.vnf.QueryVnfNotification.Outputs.Entry.class));
		test(PojoClassFactory.getPojoClass(org.openecomp.mso.bpmn.common.adapter.vnf.CreateVnfNotification.Outputs.Entry.class));
		test(PojoClassFactory.getPojoClass(org.openecomp.mso.bpmn.common.adapter.vnf.UpdateVnfNotification.Outputs.Entry.class));
		testToString(PojoClassFactory.getPojoClass(CreateVnfNotification.class));
		testToString(PojoClassFactory.getPojoClass(DeleteVnfNotification.class));
		testToString(PojoClassFactory.getPojoClass(MsoRequest.class));
		testToString(PojoClassFactory.getPojoClass(QueryVnfNotification.class));
		testToString(PojoClassFactory.getPojoClass(RollbackVnfNotification.class));
		testToString(PojoClassFactory.getPojoClass(UpdateVnfNotification.class));
		testToString(PojoClassFactory.getPojoClass(VnfRollback.class));
	}
	
	private void test(PojoClass pojoClass) {
		Validator validator = ValidatorBuilder.create()
				.with(new SetterTester())
				.with(new GetterTester())
				.build();
		validator.validate(pojoClass);
	}
	
	private void testToString(PojoClass pojoClass) {
		Validator validator = ValidatorBuilder.create()
				.with(new ToStringTester())
				.build();
		validator.validate(pojoClass);
	}
}
