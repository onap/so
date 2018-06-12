package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import org.junit.Test;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.NoNestedClassRule;
import com.openpojo.validation.rule.impl.NoPrimitivesRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.DefaultValuesNullTester;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class ClientEntityPojoTest {
	@Test
	public void pojoStructure() {
		test(PojoClassFactory.getPojoClass(NetworkInformationEntity.class));
		test(PojoClassFactory.getPojoClass(NetworkInputParametersEntity.class));
		test(PojoClassFactory.getPojoClass(NetworkRequestInputEntity.class));
		test(PojoClassFactory.getPojoClass(NetworkResponseInformationEntity.class));
		test(PojoClassFactory.getPojoClass(NetworkTopologyOperationInputEntity.class));
		test(PojoClassFactory.getPojoClass(NetworkTopologyOperationOutputEntity.class));
		test(PojoClassFactory.getPojoClass(OnapModelInformationEntity.class));
		test(PojoClassFactory.getPojoClass(ParamEntity.class));
		test(PojoClassFactory.getPojoClass(RequestInformationEntity.class));
		test(PojoClassFactory.getPojoClass(RpcNetworkTopologyOperationInputEntity.class));
		test(PojoClassFactory.getPojoClass(SdncRequestHeaderEntity.class));
		test(PojoClassFactory.getPojoClass(RpcServiceTopologyOperationInputEntity.class));
		test(PojoClassFactory.getPojoClass(RpcServiceTopologyOperationOutputEntity.class));
		test(PojoClassFactory.getPojoClass(ServiceInformationEntity.class));
		test(PojoClassFactory.getPojoClass(ServiceInputParametersEntity.class));
		test(PojoClassFactory.getPojoClass(ServiceRequestInputEntity.class));
		test(PojoClassFactory.getPojoClass(ServiceResponseInformationEntity.class));
		test(PojoClassFactory.getPojoClass(ServiceTopologyOperationInputEntity.class));
		test(PojoClassFactory.getPojoClass(ServiceTopologyOperationOutputEntity.class));
		test(PojoClassFactory.getPojoClass(RpcNetworkTopologyOperationOutputEntity.class));
	}
	
	private void test(PojoClass pojoClass) {
		Validator validator = ValidatorBuilder.create()
				.with(new GetterMustExistRule())
				.with(new SetterMustExistRule())
				.with(new NoNestedClassRule())
				.with(new NoPrimitivesRule())
				.with(new NoPublicFieldsRule())
				.with(new SetterTester())
				.with(new GetterTester())
				.with(new DefaultValuesNullTester())
				.build();
		validator.validate(pojoClass);
	}
}
