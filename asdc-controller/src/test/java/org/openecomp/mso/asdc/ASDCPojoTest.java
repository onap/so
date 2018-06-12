package org.openecomp.mso.asdc;

import org.junit.Test;
import org.openecomp.mso.asdc.client.FinalDistributionStatusMessage;
import org.openecomp.mso.asdc.client.test.emulators.ArtifactInfoImpl;
import org.openecomp.mso.asdc.client.test.emulators.DistributionClientEmulator;
import org.openecomp.mso.asdc.client.test.emulators.JsonStatusData;
import org.openecomp.mso.asdc.client.test.emulators.JsonVfModuleMetaData;
import org.openecomp.mso.asdc.client.test.emulators.NotificationDataImpl;
import org.openecomp.mso.asdc.client.test.emulators.ResourceInfoImpl;
import org.openecomp.mso.asdc.installer.VfResourceStructure;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class ASDCPojoTest {
	@Test
	public void pojoStructure() {
		test(PojoClassFactory.getPojoClass(FinalDistributionStatusMessage.class));
		test(PojoClassFactory.getPojoClass(ArtifactInfoImpl.class));
		test(PojoClassFactory.getPojoClass(NotificationDataImpl.class));
		test(PojoClassFactory.getPojoClass(JsonVfModuleMetaData.class));
		test(PojoClassFactory.getPojoClass(ResourceInfoImpl.class));
		test(PojoClassFactory.getPojoClass(DistributionClientEmulator.class));
		test(PojoClassFactory.getPojoClass(JsonStatusData.class));
		test(PojoClassFactory.getPojoClass(VfResourceStructure.class));
	}
	
	private void test(PojoClass pojoClass) {
		Validator validator = ValidatorBuilder.create()
				.with(new SetterTester())
				.with(new GetterTester())
				.build();
		validator.validate(pojoClass);
	}
}
