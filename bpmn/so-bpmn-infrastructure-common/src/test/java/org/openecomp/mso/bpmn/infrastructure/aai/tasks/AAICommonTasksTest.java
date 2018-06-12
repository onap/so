/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.infrastructure.aai.tasks;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;

public class AAICommonTasksTest  extends BaseTaskTest{
    @Autowired
	private AAICommonTasks aaiCommonTasks;
	
	@Test
	public void getServiceInstanceServiceTypeTest() throws Exception {
		ServiceInstance serviceInstance = setServiceInstance();
		serviceInstance.getModelInfoServiceInstance().setServiceType("Transport");

		Optional<String> serviceType = aaiCommonTasks.getServiceType(execution);
		assertEquals(serviceInstance.getModelInfoServiceInstance().getServiceType(), serviceType.get());
	}
	
	@Test
	public void getServiceInstanceServiceTypeExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		aaiCommonTasks.getServiceType(execution);
	}
	
	@Test
	public void getServiceInstanceServiceTypeNullModelTest() throws Exception {
		ServiceInstance serviceInstance = setServiceInstance();
		serviceInstance.setModelInfoServiceInstance(null);
		Optional<String> serviceType = aaiCommonTasks.getServiceType(execution);
		assertEquals(false, serviceType.isPresent());
	}

}
