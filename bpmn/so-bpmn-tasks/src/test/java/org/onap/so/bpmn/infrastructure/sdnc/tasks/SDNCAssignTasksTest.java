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
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BBObjectNotFoundException;


public class SDNCAssignTasksTest extends BaseTaskTest{
	@InjectMocks
	private SDNCAssignTasks sdncAssignTasks = new SDNCAssignTasks();

	private L3Network network;
	private ServiceInstance serviceInstance;
	private RequestContext requestContext;
	private CloudRegion cloudRegion;
	private GenericVnf genericVnf;
	private VfModule vfModule;
	private VolumeGroup volumeGroup;
	private Customer customer;

	@Before
	public void before() throws BBObjectNotFoundException {
		customer = setCustomer();
		serviceInstance = setServiceInstance();
		network = setL3Network();
		cloudRegion = setCloudRegion();
		requestContext = setRequestContext();
		genericVnf = setGenericVnf();
		vfModule = setVfModule();
		volumeGroup = setVolumeGroup();
		
		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID), any())).thenReturn(genericVnf);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID), any())).thenReturn(vfModule);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.NETWORK_ID), any())).thenReturn(network);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID), any())).thenReturn(serviceInstance);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VOLUME_GROUP_ID), any())).thenReturn(volumeGroup);
	}

	@Test
	public void assignServiceInstanceTest() throws Exception {
		doReturn("response").when(sdncServiceInstanceResources).assignServiceInstance(serviceInstance, customer, requestContext);

		sdncAssignTasks.assignServiceInstance(execution);

		verify(sdncServiceInstanceResources, times(1)).assignServiceInstance(serviceInstance, customer, requestContext);
		assertTrue(execution.getVariable("SDNCResponse").equals("response"));
	}

	@Test
	public void assignServiceInstanceExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);

		doThrow(RuntimeException.class).when(sdncServiceInstanceResources).assignServiceInstance(serviceInstance, customer, requestContext);

		sdncAssignTasks.assignServiceInstance(execution);
	}

	@Test
	public void assignVnfTest() throws Exception {
		doReturn("response").when(sdncVnfResources).assignVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext, false);

		execution.setVariable("generalBuildingBlock", gBBInput);
		sdncAssignTasks.assignVnf(execution);

		verify(sdncVnfResources, times(1)).assignVnf(genericVnf, serviceInstance,customer, cloudRegion, requestContext, false);
		assertTrue(execution.getVariable("SDNCResponse").equals("response"));
	}

	@Test
	public void assignVnfExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);

		doThrow(RuntimeException.class).when(sdncVnfResources).assignVnf(genericVnf, serviceInstance, customer, cloudRegion, requestContext, false);

		sdncAssignTasks.assignVnf(execution);
	}

	@Test
	public void assignVfModuleTest() throws Exception {
		doReturn("response").when(sdncVfModuleResources).assignVfModule(vfModule, volumeGroup, genericVnf, serviceInstance, customer, cloudRegion, requestContext);

		sdncAssignTasks.assignVfModule(execution);

		verify(sdncVfModuleResources, times(1)).assignVfModule(vfModule, volumeGroup, genericVnf, serviceInstance, customer, cloudRegion, requestContext);
		assertTrue(execution.getVariable("SDNCAssignResponse_" + vfModule.getVfModuleId()).equals("response"));
	}

	@Test
	public void assignVfModuleExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);

		doThrow(RuntimeException.class).when(sdncVfModuleResources).assignVfModule(vfModule, volumeGroup, genericVnf, serviceInstance, customer, cloudRegion, requestContext);

		sdncAssignTasks.assignVfModule(execution);
	}

	@Test
	public void assignNetworkTest() throws Exception {
		doReturn("response").when(sdncNetworkResources).assignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);

		sdncAssignTasks.assignNetwork(execution);

		verify(sdncNetworkResources, times(1)).assignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
	}

	@Test
	public void assignNetworkExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);

		doThrow(RuntimeException.class).when(sdncNetworkResources).assignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);

		sdncAssignTasks.assignNetwork(execution);
	}
}
