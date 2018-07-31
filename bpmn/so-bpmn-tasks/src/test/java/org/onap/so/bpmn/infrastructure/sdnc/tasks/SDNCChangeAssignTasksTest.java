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
package org.onap.so.bpmn.infrastructure.sdnc.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

public class SDNCChangeAssignTasksTest extends BaseTaskTest{
	@Autowired
	private SDNCChangeAssignTasks sdncChangeAssignTasks;
	
	private ServiceInstance serviceInstance;
	private RequestContext requestContext;
	private CloudRegion cloudRegion;
	private VfModule vfModule;
	private GenericVnf genericVnf;
	private Customer customer;
	
	@Before
	public void before() {
		customer = setCustomer();
		serviceInstance = setServiceInstance();
		genericVnf = setGenericVnf();
		vfModule = setVfModule();
		cloudRegion = setCloudRegion();
		requestContext = setRequestContext();

	}
	
	@Test
	public void changeModelVnfTest() throws Exception {
		String response = "sdncChangeModelServiceInstance";
		
		doReturn(response).when(sdncServiceInstanceResources).changeModelServiceInstance(serviceInstance, customer, requestContext);
		
		sdncChangeAssignTasks.changeModelServiceInstance(execution);
		
		verify(sdncServiceInstanceResources, times(1)).changeModelServiceInstance(serviceInstance, customer, requestContext);
		
		assertEquals(response, execution.getVariable("SDNCChangeAssignTasks.changeModelServiceInstance.response"));
	}
	
	@Test
	public void changeModelVnfExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(sdncVnfResources).changeModelVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext);
		sdncChangeAssignTasks.changeModelVnf(execution);
	}
	
	@Test
	public void changeAssignModelVfModuleTest() throws Exception {
		String response = "response";
		doReturn(response).when(sdncVfModuleResources).changeAssignVfModule(vfModule, genericVnf, serviceInstance, customer, cloudRegion, requestContext);
		
		sdncChangeAssignTasks.changeAssignModelVfModule(execution);
		
		verify(sdncVfModuleResources, times(1)).changeAssignVfModule(vfModule, genericVnf, serviceInstance, customer, cloudRegion, requestContext);
		assertTrue(execution.getVariable("SDNCChangeAssignVfModuleResponse").equals(response));
	}
	
	@Test
	public void changeAssignModelVfModuleExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(sdncVfModuleResources).changeAssignVfModule(vfModule, genericVnf, serviceInstance, customer, cloudRegion, requestContext);
		sdncChangeAssignTasks.changeAssignModelVfModule(execution);
	}
}
