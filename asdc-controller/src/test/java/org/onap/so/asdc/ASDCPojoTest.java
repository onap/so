/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.asdc;

import org.junit.Test;
import org.onap.so.asdc.client.FinalDistributionStatusMessage;
import org.onap.so.asdc.client.test.emulators.ArtifactInfoImpl;
import org.onap.so.asdc.client.test.emulators.DistributionClientEmulator;
import org.onap.so.asdc.client.test.emulators.JsonStatusData;
import org.onap.so.asdc.client.test.emulators.JsonVfModuleMetaData;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
import org.onap.so.asdc.client.test.emulators.ResourceInfoImpl;
import org.onap.so.asdc.installer.VfResourceStructure;

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
