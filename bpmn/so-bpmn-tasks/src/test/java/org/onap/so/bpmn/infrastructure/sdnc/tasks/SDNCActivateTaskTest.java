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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;


public class SDNCActivateTaskTest extends BaseTaskTest{
	
	@InjectMocks
	private SDNCActivateTasks sdncActivateTasks = new SDNCActivateTasks();
	
	private L3Network network;
	private ServiceInstance serviceInstance;
	private RequestContext requestContext;
	private CloudRegion cloudRegion;
	private GenericVnf genericVnf;
	private VfModule vfModule;
	private Customer customer;
	
	@Before
	public void before() throws BBObjectNotFoundException {
		serviceInstance = setServiceInstance();
		network = setL3Network();
		genericVnf = setGenericVnf();
		vfModule = setVfModule();
		cloudRegion = setCloudRegion();
		requestContext = setRequestContext();
		customer = setCustomer();

		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID), any())).thenReturn(genericVnf);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID), any())).thenReturn(vfModule);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.NETWORK_ID), any())).thenReturn(network);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID), any())).thenReturn(serviceInstance);

	}

	@Test
	public void activateVnfTest() throws Exception {
		doReturn("success").when(sdncVnfResources).activateVnf(genericVnf,serviceInstance, customer, cloudRegion,requestContext);
		sdncActivateTasks.activateVnf(execution);
		verify(sdncVnfResources, times(1)).activateVnf(genericVnf,serviceInstance, customer,cloudRegion,requestContext);
	}
	
	@Test
	public void activateVnfTestException() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(RuntimeException.class).when(sdncVnfResources).activateVnf(genericVnf,serviceInstance, customer,cloudRegion,requestContext);
		sdncActivateTasks.activateVnf(execution);
	}
	
	@Test
	public void activateNetworkTest() throws Exception {
		doReturn("response").when(sdncNetworkResources).activateNetwork(isA(L3Network.class), isA(ServiceInstance.class), isA(Customer.class), isA(RequestContext.class), isA(CloudRegion.class));
		sdncActivateTasks.activateNetwork(execution);
		verify(sdncNetworkResources, times(1)).activateNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
	}
	
	@Test
	public void activateNetworkExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(RuntimeException.class).when(sdncNetworkResources).activateNetwork(isA(L3Network.class), isA(ServiceInstance.class), isA(Customer.class), isA(RequestContext.class), isA(CloudRegion.class));
		sdncActivateTasks.activateNetwork(execution);
	}
	
	@Test
	public void activateVfModuleTest() throws Exception {
		doReturn("success").when(sdncVfModuleResources).activateVfModule(vfModule, genericVnf, serviceInstance, customer, cloudRegion, requestContext);

		sdncActivateTasks.activateVfModule(execution);

		verify(sdncVfModuleResources, times(1)).activateVfModule(vfModule, genericVnf, serviceInstance, customer, cloudRegion, requestContext);
	}
	
	@Test
	public void activateVfModuleTestException() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(RuntimeException.class).when(sdncVfModuleResources).activateVfModule(vfModule, genericVnf, serviceInstance, customer, cloudRegion, requestContext);
		sdncActivateTasks.activateVfModule(execution);
	}
}
