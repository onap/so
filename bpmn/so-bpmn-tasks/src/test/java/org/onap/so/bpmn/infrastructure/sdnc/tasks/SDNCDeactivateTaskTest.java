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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

public class SDNCDeactivateTaskTest extends BaseTaskTest {
	@Autowired
	private SDNCDeactivateTasks sdncDeactivateTasks;
	
	private ServiceInstance serviceInstance;
	private CloudRegion cloudRegion;
	private RequestContext requestContext;
	private GenericVnf genericVnf;
	private VfModule vfModule;
	private L3Network network;
	private Customer customer;
	
	@Before
	public void before() {
		customer = setCustomer();
		serviceInstance = setServiceInstance();
		cloudRegion = setCloudRegion();
		requestContext = setRequestContext();
		genericVnf = setGenericVnf();
		vfModule = setVfModule();
		network = setL3Network();

	}
	
	@Test
	public void deactivateVfModuleTest() throws Exception {
		doReturn("success").when(sdncVfModuleResources).deactivateVfModule(vfModule, genericVnf, serviceInstance, customer, cloudRegion, requestContext);

		sdncDeactivateTasks.deactivateVfModule(execution);

		verify(sdncVfModuleResources, times(1)).deactivateVfModule(vfModule, genericVnf, serviceInstance, customer, cloudRegion, requestContext);
	}
	
	@Test
	public void deactivateVfModuleExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(sdncVfModuleResources).deactivateVfModule(vfModule, genericVnf, serviceInstance, customer, cloudRegion, requestContext);

		sdncDeactivateTasks.deactivateVfModule(execution);
	}
	
	@Test
	public void deactivateVnfTest() throws Exception {
		doReturn("success").when(sdncVnfResources).deactivateVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext);

		sdncDeactivateTasks.deactivateVnf(execution);

		verify(sdncVnfResources, times(1)).deactivateVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext);
	}
	
	@Test
	public void deactivateVnfExceptionTest() throws Exception {
		doThrow(Exception.class).when(sdncVnfResources).deactivateVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext);
		expectedException.expect(BpmnError.class);
		sdncDeactivateTasks.deactivateVnf(execution);
	}
	
	@Test
	public void deactivateServiceInstanceTest() throws Exception {
		doReturn("response").when(sdncServiceInstanceResources).deactivateServiceInstance(serviceInstance, customer, requestContext);

		sdncDeactivateTasks.deactivateServiceInstance(execution);

		verify(sdncServiceInstanceResources, times(1)).deactivateServiceInstance(serviceInstance, customer, requestContext);
		assertEquals("response", execution.getVariable("deactivateServiceInstanceSDNCResponse"));
		assertTrue(execution.getVariable("sdncServiceInstanceRollback"));
	}
	
	@Test
	public void deactivateServiceInstanceExceptionTest() throws Exception {
		doThrow(Exception.class).when(sdncServiceInstanceResources).deactivateServiceInstance(serviceInstance, customer, requestContext);
		expectedException.expect(BpmnError.class);
		sdncDeactivateTasks.deactivateServiceInstance(execution);
	}
	
	@Test
	public void test_deactivateNetwork() throws Exception {
		String expectedResponse = "return";
		
		doReturn(expectedResponse).when(sdncNetworkResources).deactivateNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
		
		sdncDeactivateTasks.deactivateNetwork(execution);
		
		verify(sdncNetworkResources, times(1)).deactivateNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
		
		assertEquals(expectedResponse, execution.getVariable("SDNCDeactivateTasks.deactivateNetwork.response"));
		
		assertTrue(execution.getVariable("SDNCDeactivateTasks.deactivateNetwork.rollback"));
	}
	
	@Test
	public void test_deactivateNetwork_exception() throws Exception {
		expectedException.expect(BpmnError.class);
		
		try {
			lookupKeyMap.remove(ResourceKey.NETWORK_ID);
			
			sdncDeactivateTasks.deactivateNetwork(execution);
		} finally {
			verify(sdncNetworkResources, times(0)).deactivateNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
			
			assertNull(execution.getVariable("SDNCDeactivateTasks.deactivateNetwork.response"));
			
			assertFalse(execution.getVariable("SDNCDeactivateTasks.deactivateNetwork.rollback"));
		}
	}

}
