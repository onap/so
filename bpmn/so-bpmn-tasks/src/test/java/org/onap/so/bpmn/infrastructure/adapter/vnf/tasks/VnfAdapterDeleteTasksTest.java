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

package org.onap.so.bpmn.infrastructure.adapter.vnf.tasks;

import static org.junit.Assert.assertEquals;
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
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;

public class VnfAdapterDeleteTasksTest extends BaseTaskTest{
	@InjectMocks
	private VnfAdapterDeleteTasks vnfAdapterDeleteTasks = new VnfAdapterDeleteTasks();

	private VolumeGroup volumeGroup;
	private VfModule vfModule;
	private GenericVnf genericVnf;
	private RequestContext requestContext;
	private CloudRegion cloudRegion;
	private ServiceInstance serviceInstance;
	private OrchestrationContext orchestrationContext;

	@Before
	public void before() throws Exception {
		requestContext = setRequestContext();

		serviceInstance = setServiceInstance();

		cloudRegion = setCloudRegion();

		genericVnf = setGenericVnf();

		vfModule = setVfModule();
		
		volumeGroup = setVolumeGroup();

		orchestrationContext = setOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);
		
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(genericVnf);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VOLUME_GROUP_ID))).thenReturn(volumeGroup);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID))).thenReturn(serviceInstance);
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
	}

	@Test
	public void test_deleteVfModule() throws Exception {
		DeleteVfModuleRequest deleteVfModuleRequest = new DeleteVfModuleRequest();
		deleteVfModuleRequest.setVfModuleId("vfModuleId");
		
		doReturn(deleteVfModuleRequest).when(vnfAdapterVfModuleResources).deleteVfModuleRequest(requestContext, cloudRegion, serviceInstance, genericVnf, vfModule);
		
		vnfAdapterDeleteTasks.deleteVfModule(execution);
		
		verify(vnfAdapterVfModuleResources, times(1)).deleteVfModuleRequest(ArgumentMatchers.eq(requestContext), ArgumentMatchers.eq(cloudRegion), ArgumentMatchers.eq(serviceInstance), ArgumentMatchers.eq(genericVnf),ArgumentMatchers.eq(vfModule));
		assertEquals(execution.getVariable("VNFREST_Request"), deleteVfModuleRequest.toXmlString());
	}

	@Test 
	public void deleteVfModuleExceptionTest() throws Exception {		
		expectedException.expect(BpmnError.class);
		
		doThrow(RuntimeException.class).when(vnfAdapterVfModuleResources).deleteVfModuleRequest(requestContext, cloudRegion, serviceInstance, genericVnf, vfModule);
		
		vnfAdapterDeleteTasks.deleteVfModule(execution);
	}
	
	@Test
	public void test_deleteVolumeGroup() throws Exception {
		DeleteVolumeGroupRequest deleteVolumeGroupRequest = new DeleteVolumeGroupRequest();
		deleteVolumeGroupRequest.setVolumeGroupId("volumeGroupId");
		
		doReturn(deleteVolumeGroupRequest).when(vnfAdapterVolumeGroupResources).deleteVolumeGroupRequest(requestContext, cloudRegion, serviceInstance, volumeGroup);
		
		vnfAdapterDeleteTasks.deleteVolumeGroup(execution);
		
		verify(vnfAdapterVolumeGroupResources, times(1)).deleteVolumeGroupRequest(ArgumentMatchers.eq(requestContext), ArgumentMatchers.eq(cloudRegion), ArgumentMatchers.eq(serviceInstance), ArgumentMatchers.eq(volumeGroup));
		assertEquals(execution.getVariable("VNFREST_Request"), deleteVolumeGroupRequest.toXmlString());
	}
	
	@Test
	public void deleteVolumeGroupExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(RuntimeException.class).when(vnfAdapterVolumeGroupResources).deleteVolumeGroupRequest(requestContext, cloudRegion, serviceInstance, volumeGroup);
	
		vnfAdapterDeleteTasks.deleteVolumeGroup(execution);
	}
}
