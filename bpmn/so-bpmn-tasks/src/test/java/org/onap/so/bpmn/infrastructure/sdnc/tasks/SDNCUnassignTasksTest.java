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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class SDNCUnassignTasksTest extends BaseTaskTest{
	@InjectMocks
	private SDNCUnassignTasks sdncUnassignTasks = new SDNCUnassignTasks();
	
	private ServiceInstance serviceInstance;
	private RequestContext requestContext;
	private GenericVnf genericVnf;
	private VfModule vfModule;
	private CloudRegion cloudRegion;
	private L3Network network;
	private Customer customer;
	
	@Before
	public void before() throws BBObjectNotFoundException {
		customer = setCustomer();
		serviceInstance = setServiceInstance();
		requestContext = setRequestContext();
		genericVnf = setGenericVnf();
		vfModule = setVfModule();
		cloudRegion = setCloudRegion();
		network = setL3Network();
		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID), any())).thenReturn(genericVnf);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.NETWORK_ID), any())).thenReturn(network);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID), any())).thenReturn(vfModule);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID), any())).thenReturn(serviceInstance);
	}
	
	@Test
	public void unassignServiceInstanceTest() throws Exception {
		doReturn("test").when(sdncServiceInstanceResources).unassignServiceInstance(serviceInstance, customer, requestContext);
		
		sdncUnassignTasks.unassignServiceInstance(execution);
		
		verify(sdncServiceInstanceResources, times(1)).unassignServiceInstance(serviceInstance, customer, requestContext);
	}

	@Test
	public void unassignServiceInstanceTest_inventoried() throws Exception {
		doReturn("test").when(sdncServiceInstanceResources).unassignServiceInstance(serviceInstance, customer, requestContext);
		
		serviceInstance.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
		
		sdncUnassignTasks.unassignServiceInstance(execution);
		
		verify(sdncServiceInstanceResources, times(0)).unassignServiceInstance(serviceInstance, customer, requestContext);
	}

	@Test
	public void unassignServiceInstanceExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(RuntimeException.class).when(sdncServiceInstanceResources).unassignServiceInstance(serviceInstance, customer, requestContext);
		
		sdncUnassignTasks.unassignServiceInstance(execution);
	}	
		
	@Test
	public void unassignVfModuleTest() throws Exception {
		doReturn("response").when(sdncVfModuleResources).unassignVfModule(vfModule, genericVnf, serviceInstance);

		sdncUnassignTasks.unassignVfModule(execution);

		verify(sdncVfModuleResources, times(1)).unassignVfModule(vfModule, genericVnf, serviceInstance);
		assertEquals("response", execution.getVariable("SDNCResponse"));
	}
	
	@Test
	public void unassignVfModuleTest_inventoried() throws Exception {
		vfModule.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
		
		sdncUnassignTasks.unassignVfModule(execution);

		verify(sdncVfModuleResources, times(0)).unassignVfModule(vfModule, genericVnf, serviceInstance);
		assertNull(execution.getVariable("SDNCResponse"));
	}
	
	@Test
	public void unassignVfModuleTest_pendingCreate() throws Exception {
		vfModule.setOrchestrationStatus(OrchestrationStatus.PENDING_CREATE);
		
		sdncUnassignTasks.unassignVfModule(execution);

		verify(sdncVfModuleResources, times(0)).unassignVfModule(vfModule, genericVnf, serviceInstance);
		assertNull(execution.getVariable("SDNCResponse"));
	}
	
	@Test
	public void unassignVfModuleExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(RuntimeException.class).when(sdncVfModuleResources).unassignVfModule(vfModule, genericVnf, serviceInstance);

		sdncUnassignTasks.unassignVfModule(execution);
	}
	
	@Test
	public void unassignVnfTest() throws Exception {
		doReturn("response").when(sdncVnfResources).unassignVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext);

		sdncUnassignTasks.unassignVnf(execution);

		verify(sdncVnfResources, times(1)).unassignVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext);
		assertTrue(execution.getVariable("sdncUnassignVnfResponse").equals("response"));
	}
	
	@Test
	public void unassignVnfTest_inventoried() throws Exception {
		genericVnf.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
		
		sdncUnassignTasks.unassignVnf(execution);

		verify(sdncVnfResources, times(0)).unassignVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext);
		assertNull(execution.getVariable("sdncUnassignVnfResponse"));
	}
	
	@Test
	public void unassignVnfTest_created() throws Exception {
		genericVnf.setOrchestrationStatus(OrchestrationStatus.CREATED);
		
		sdncUnassignTasks.unassignVnf(execution);

		verify(sdncVnfResources, times(0)).unassignVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext);
		assertNull(execution.getVariable("sdncUnassignVnfResponse"));
	}
	
	@Test
	public void unassignVnfExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(RuntimeException.class).when(sdncVnfResources).unassignVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext);
		sdncUnassignTasks.unassignVnf(execution);
	}

	@Test
	public void unassignNetworkTest() throws Exception {
		String cloudRegionSdnc = "AAIAIC25";
		
		cloudRegion.setCloudRegionVersion("2.5");
		
		execution.setVariable("cloudRegionSdnc", cloudRegionSdnc);
		
		doReturn("response").when(sdncNetworkResources).unassignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
		
		assertNotEquals(cloudRegionSdnc, cloudRegion.getLcpCloudRegionId());
		sdncUnassignTasks.unassignNetwork(execution);

		verify(sdncNetworkResources, times(1)).unassignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
		assertEquals("response", execution.getVariable("SDNCUnAssignNetworkResponse"));
		assertEquals(cloudRegionSdnc, cloudRegion.getLcpCloudRegionId());
	}
	
	@Test
	public void unassignNetworkTest_inventoried() throws Exception {
		network.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
		
		sdncUnassignTasks.unassignNetwork(execution);

		verify(sdncNetworkResources, times(0)).unassignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
		assertNull(execution.getVariable("SDNCUnAssignNetworkResponse"));
	}
}
