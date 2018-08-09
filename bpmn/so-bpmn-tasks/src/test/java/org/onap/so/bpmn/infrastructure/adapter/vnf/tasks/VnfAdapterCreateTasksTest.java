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

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Test;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.CreateVolumeGroupRequest;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class VnfAdapterCreateTasksTest extends BaseTaskTest{
	@Autowired
	private VnfAdapterCreateTasks vnfAdapterCreateTasks;
	
	@Test
	public void test_createVolumeGroupRequest() throws Exception {
		RequestContext requestContext = setRequestContext();
		
		ServiceInstance serviceInstance = setServiceInstance();
		
		GenericVnf genericVnf = setGenericVnf();

		VfModule vfModule = setVfModule();
		vfModule.setSelflink("vfModuleSelfLink");
		VolumeGroup volumeGroup = setVolumeGroup();
		volumeGroup.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
		
		CloudRegion cloudRegion = setCloudRegion();
		
		OrchestrationContext orchestrationContext = setOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);

		String sdncVnfQueryResponse = "SDNCVnfQueryResponse";
        execution.setVariable("SDNCQueryResponse_" + vfModule.getVfModuleId(), sdncVnfQueryResponse);

        CreateVolumeGroupRequest request = new CreateVolumeGroupRequest();
        request.setVolumeGroupId("volumeGroupStackId");

        doReturn(request).when(vnfAdapterVolumeGroupResources).createVolumeGroupRequest(requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf, volumeGroup, sdncVnfQueryResponse);

        vnfAdapterCreateTasks.createVolumeGroupRequest(execution);
		
		verify(vnfAdapterVolumeGroupResources, times(1)).createVolumeGroupRequest(requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf,  volumeGroup, sdncVnfQueryResponse);
		
		assertEquals(request.toXmlString(), execution.getVariable("VNFREST_Request"));
	}

	@Test
	public void test_createVolumeGroupRequest_for_alaCarte_flow() throws Exception {
		RequestContext requestContext = setRequestContext();
		ServiceInstance serviceInstance = setServiceInstance();
		GenericVnf genericVnf = setGenericVnf();
		VolumeGroup volumeGroup = setVolumeGroup();
		volumeGroup.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

		CloudRegion cloudRegion = setCloudRegion();

		OrchestrationContext orchestrationContext = setOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);

        CreateVolumeGroupRequest request = new CreateVolumeGroupRequest();
        request.setVolumeGroupId("volumeGroupStackId");

		doReturn(request).when(vnfAdapterVolumeGroupResources).createVolumeGroupRequest(requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf,  volumeGroup, null);

		vnfAdapterCreateTasks.createVolumeGroupRequest(execution);

		verify(vnfAdapterVolumeGroupResources, times(1)).createVolumeGroupRequest(requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf,  volumeGroup, null);

		assertEquals(request.toXmlString(),  execution.getVariable("VNFREST_Request"));
	}
	
	@Test
	public void test_createVolumeGroupRequest_exception() throws Exception {
		// run with no data setup, and it will throw a BBObjectNotFoundException
		expectedException.expect(BpmnError.class);
		
		vnfAdapterCreateTasks.createVolumeGroupRequest(execution);
	}
	
	@Test
	public void test_createVfModule() throws Exception {
		RequestContext requestContext = setRequestContext();
		
		ServiceInstance serviceInstance = setServiceInstance();
		
		GenericVnf genericVnf = setGenericVnf();

		VfModule vfModule = setVfModule();
		
		CloudRegion cloudRegion = setCloudRegion();
		
		OrchestrationContext orchestrationContext = setOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);
		
		CreateVfModuleRequest modRequest = new CreateVfModuleRequest();
		modRequest.setVfModuleId(vfModule.getVfModuleId());
		modRequest.setBaseVfModuleStackId("baseVfModuleStackId");
		modRequest.setVfModuleName(vfModule.getVfModuleName());
		CreateVfModuleRequest createVfModuleRequest = modRequest;
		
		String sdncVfModuleQueryResponse = "{someJson}";
		execution.setVariable("SDNCQueryResponse_" + vfModule.getVfModuleId(), sdncVfModuleQueryResponse);
		
		String sdncVnfQueryResponse = "{someJson}";
		execution.setVariable("SDNCQueryResponse_" + genericVnf.getVnfId(), sdncVnfQueryResponse);
		
		doReturn(createVfModuleRequest).when(vnfAdapterVfModuleResources).createVfModuleRequest(requestContext, cloudRegion, orchestrationContext, serviceInstance, 
				genericVnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);
		
		vnfAdapterCreateTasks.createVfModule(execution);
		
		verify(vnfAdapterVfModuleResources, times(1)).createVfModuleRequest(requestContext, cloudRegion, orchestrationContext, serviceInstance, 
				genericVnf, vfModule, null, sdncVnfQueryResponse, sdncVfModuleQueryResponse);
		
		assertEquals(execution.getVariable("VNFREST_Request"), createVfModuleRequest.toXmlString());
	}
	
	@Test
	public void test_createVfModuleWithVolumeGroup() throws Exception {
		RequestContext requestContext = setRequestContext();
		
		ServiceInstance serviceInstance = setServiceInstance();
		
		GenericVnf genericVnf = setGenericVnf();

		VfModule vfModule = setVfModule();
		
		VolumeGroup volumeGroup = setVolumeGroup();
		
		CloudRegion cloudRegion = setCloudRegion();
		
		OrchestrationContext orchestrationContext = setOrchestrationContext();
		orchestrationContext.setIsRollbackEnabled(true);
		
		CreateVfModuleRequest modRequest = new CreateVfModuleRequest();
		modRequest.setVfModuleId(vfModule.getVfModuleId());
		modRequest.setBaseVfModuleStackId("baseVfModuleStackId");
		modRequest.setVfModuleName(vfModule.getVfModuleName());
		CreateVfModuleRequest createVfModuleRequest = modRequest;
		
		String sdncVfModuleQueryResponse = "{someJson}";
		execution.setVariable("SDNCQueryResponse_" + vfModule.getVfModuleId(), sdncVfModuleQueryResponse);
		
		String sdncVnfQueryResponse = "{someJson}";
		execution.setVariable("SDNCQueryResponse_" + genericVnf.getVnfId(), sdncVnfQueryResponse);
		
		doReturn(createVfModuleRequest).when(vnfAdapterVfModuleResources).createVfModuleRequest(requestContext, cloudRegion, orchestrationContext, serviceInstance, 
				genericVnf, vfModule, volumeGroup, sdncVnfQueryResponse, sdncVfModuleQueryResponse);
		
		vnfAdapterCreateTasks.createVfModule(execution);
		
		verify(vnfAdapterVfModuleResources, times(1)).createVfModuleRequest(requestContext, cloudRegion, orchestrationContext, serviceInstance, 
				genericVnf, vfModule, volumeGroup, sdncVnfQueryResponse, sdncVfModuleQueryResponse);
		
		assertEquals(execution.getVariable("VNFREST_Request"), createVfModuleRequest.toXmlString());
	}
	
	@Test
	public void createVfModuleExceptionTest() throws Exception {
		// run with no data setup, and it will throw a BBObjectNotFoundException
		expectedException.expect(BpmnError.class);
		vnfAdapterCreateTasks.createVolumeGroupRequest(execution);
	}
}
