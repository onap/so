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

package org.onap.so.bpmn.infrastructure.aai.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class AAIUpdateTasksTest extends BaseTaskTest{
	@Autowired
	private AAIUpdateTasks aaiUpdateTasks;
	
	private L3Network network;
	private ServiceInstance serviceInstance;
	private VfModule vfModule;
	private GenericVnf genericVnf;
	private VolumeGroup volumeGroup;
	private CloudRegion cloudRegion;
	
	@Before
	public void before() {
		serviceInstance = setServiceInstance();
		genericVnf = setGenericVnf();
		vfModule = setVfModule();
		volumeGroup = setVolumeGroup();
		cloudRegion = setCloudRegion();
		network = setL3Network();
	}
	
	@Test
	public void updateOrchestrationStatusAssignedServiceTest() throws Exception {
		doNothing().when(aaiServiceInstanceResources).updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ASSIGNED);

		aaiUpdateTasks.updateOrchestrationStatusAssignedService(execution);

		verify(aaiServiceInstanceResources, times(1)).updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ASSIGNED);
	}
	
	@Test
	public void updateOrchestrationStatusAssignedServiceExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(aaiServiceInstanceResources).updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ASSIGNED);

		aaiUpdateTasks.updateOrchestrationStatusAssignedService(execution);
	}
	
	@Test
	public void updateOrchestrationStatusActiveServiceTest() throws Exception {
		doNothing().when(aaiServiceInstanceResources).updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ACTIVE);

		aaiUpdateTasks.updateOrchestrationStatusActiveService(execution);

		verify(aaiServiceInstanceResources, times(1)).updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ACTIVE);
	}
	
	@Test
	public void updateOrchestrationStatusActiveServiceExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(aaiServiceInstanceResources).updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ACTIVE);

		aaiUpdateTasks.updateOrchestrationStatusActiveService(execution);
	}

	@Test
	public void updateOrchestrationStatusAssignedVnfTest() throws Exception {
		doNothing().when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ASSIGNED);

		aaiUpdateTasks.updateOrchestrationStatusAssignedVnf(execution);

		verify(aaiVnfResources, times(1)).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ASSIGNED);
	}
	
	@Test
	public void updateOrchestrationStatusAssignedVnfExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ASSIGNED);

		aaiUpdateTasks.updateOrchestrationStatusAssignedVnf(execution);
	}
	
	@Test
	public void updateOrchestrationStatusActiveVnfTest() throws Exception {
		doNothing().when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ACTIVE);

		aaiUpdateTasks.updateOrchestrationStatusActiveVnf(execution);

		verify(aaiVnfResources, times(1)).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ACTIVE);
	}
	
	@Test
	public void updateOrchestrationStatusActiveVnfExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ACTIVE);

		aaiUpdateTasks.updateOrchestrationStatusActiveVnf(execution);
	}
	
	@Test
	public void updateOrchestrationStatusAssignVfModuleTest() throws Exception {		
		doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ASSIGNED);
		aaiUpdateTasks.updateOrchestrationStatusAssignedVfModule(execution);
		verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ASSIGNED);
		assertEquals("", vfModule.getHeatStackId());
	}
	
	@Test
	public void updateOrchestrationStatusAssignVfModuleExceptionTest() throws Exception {
		doThrow(Exception.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ASSIGNED);
		
		expectedException.expect(BpmnError.class);
		
		aaiUpdateTasks.updateOrchestrationStatusAssignedVfModule(execution);
	}
	
	@Test
	public void updateOrchestrationStatusCreatedVfModuleTest() throws Exception {		
		doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.CREATED);
		aaiUpdateTasks.updateOrchestrationStatusCreatedVfModule(execution);
		verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.CREATED);
	}
	
	@Test
	public void updateOrchestrationStatusCreatedVfModuleExceptionTest() throws Exception {
		doThrow(Exception.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.CREATED);
		
		expectedException.expect(BpmnError.class);
		
		aaiUpdateTasks.updateOrchestrationStatusCreatedVfModule(execution);
	}
	
	@Test
	public void updateOrchestrationStatusPendingActivatefModuleTest() throws Exception {
		doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.PENDING_ACTIVATION);

		aaiUpdateTasks.updateOrchestrationStatusPendingActivationVfModule(execution);

		verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.PENDING_ACTIVATION);
	}
	
	@Test
	public void updateOrchestrationStatusPendingActivatefModuleExceptionTest() throws Exception {
		doThrow(Exception.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.PENDING_ACTIVATION);
		
		expectedException.expect(BpmnError.class);
	
		aaiUpdateTasks.updateOrchestrationStatusPendingActivationVfModule(execution);
	}
	
	@Test
	public void updateOrchestrationStatusDectivateVfModuleTest() throws Exception {
		doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.CREATED);

		aaiUpdateTasks.updateOrchestrationStatusDeactivateVfModule(execution);

		verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.CREATED);
	}
	
	@Test
	public void updateOrchestrationStatusDectivateVfModuleExceptionTest() throws Exception {
		doThrow(Exception.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.CREATED);
		
		expectedException.expect(BpmnError.class);
	
		aaiUpdateTasks.updateOrchestrationStatusDeactivateVfModule(execution);
	}
	
	@Test
	public void updateOrchestrationStatusActiveVolumeGroupTest() throws Exception {
		doNothing().when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.ACTIVE);

		aaiUpdateTasks.updateOrchestrationStatusActiveVolumeGroup(execution);

		verify(aaiVolumeGroupResources, times(1)).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.ACTIVE);
	}
	
	@Test
	public void updateOrchestrationStatusActiveVolumeGroupExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.ACTIVE);
		aaiUpdateTasks.updateOrchestrationStatusActiveVolumeGroup(execution);
	}
	
	@Test
	public void updateOrchestrationStatusCreatedVolumeGroupTest() throws Exception {
		doNothing().when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.CREATED);

		aaiUpdateTasks.updateOrchestrationStatusCreatedVolumeGroup(execution);

		verify(aaiVolumeGroupResources, times(1)).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.CREATED);
	}
	
	@Test
	public void updateOrchestrationStatusCreatedVolumeGroupExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.CREATED);
		aaiUpdateTasks.updateOrchestrationStatusCreatedVolumeGroup(execution);
	}	
	
	@Test
	public void test_updateOrchestrationStatusAssignedVolumeGroup() throws Exception {
		doNothing().when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.ASSIGNED);

		aaiUpdateTasks.updateOrchestrationStatusAssignedVolumeGroup(execution);

		verify(aaiVolumeGroupResources, times(1)).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.ASSIGNED);
		assertEquals("", volumeGroup.getHeatStackId());
	}
	
	@Test
	public void test_updateOrchestrationStatusAssignedVolumeGroup_exception() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.ASSIGNED);
		aaiUpdateTasks.updateOrchestrationStatusAssignedVolumeGroup(execution);
	}	
	@Test
	public void updateOstatusAssignedNetworkTest() throws Exception {
		doNothing().when(aaiNetworkResources).updateNetwork(network);

		aaiUpdateTasks.updateOrchestrationStatusAssignedNetwork(execution);

		verify(aaiNetworkResources, times(1)).updateNetwork(network);
		assertEquals("", network.getHeatStackId());
	}

	@Test
	public void updateOstatusAssignedNetworkExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(aaiNetworkResources).updateNetwork(network);
		
		aaiUpdateTasks.updateOrchestrationStatusAssignedNetwork(execution);
	}
	
	@Test
	public void updateOstatusActivedNetworkTest() throws Exception {
		doNothing().when(aaiNetworkResources).updateNetwork(network);

		aaiUpdateTasks.updateOrchestrationStatusActiveNetwork(execution);

		verify(aaiNetworkResources, times(1)).updateNetwork(network);
	}
	
	@Test
	public void updateOstatusCreatedNetworkTest() throws Exception {
		doNothing().when(aaiNetworkResources).updateNetwork(network);

		aaiUpdateTasks.updateOrchestrationStatusActiveNetwork(execution);

		verify(aaiNetworkResources, times(1)).updateNetwork(network);
	}

	@Test
	public void updateOstatusActiveNetworkExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);

		doThrow(Exception.class).when(aaiNetworkResources).updateNetwork(network);
		
		aaiUpdateTasks.updateOrchestrationStatusActiveNetwork(execution);
	}
	
	@Test
	public void updateOstatusActivedNetworkCollectionTest() throws Exception {
		doNothing().when(aaiCollectionResources).updateCollection(serviceInstance.getCollection());
		aaiUpdateTasks.updateOrchestrationStatusActiveNetworkCollection(execution);
		verify(aaiCollectionResources, times(1)).updateCollection(serviceInstance.getCollection());
	}

	@Test
	public void updateOstatusActiveNetworkColectionExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiCollectionResources).updateCollection(serviceInstance.getCollection());
		aaiUpdateTasks.updateOrchestrationStatusActiveNetworkCollection(execution);
	}

	@Test
	public void updateOrchestrationStatusActivateVfModuleTest() throws Exception {
		doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ACTIVE);

		aaiUpdateTasks.updateOrchestrationStatusActivateVfModule(execution);

		verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ACTIVE);
	}
	
	@Test
	public void updateOrchestrationStatusActivateVfModuleExceptionTest() throws Exception {
		doThrow(Exception.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ACTIVE);
		
		expectedException.expect(BpmnError.class);
		
		aaiUpdateTasks.updateOrchestrationStatusActivateVfModule(execution);
	}
	
	@Test
	public void updateNetworkCreatedTest() throws Exception {
		CreateNetworkResponse createNetworkResponse = new CreateNetworkResponse();
		createNetworkResponse.setNetworkFqdn("testNetworkFqdn");
		createNetworkResponse.setNetworkStackId("testNetworkStackId");
		
		execution.setVariable("createNetworkResponse", createNetworkResponse);
		
		doNothing().when(aaiNetworkResources).updateNetwork(network);
		aaiUpdateTasks.updateNetworkCreated(execution);
		verify(aaiNetworkResources, times(1)).updateNetwork(network);
		
		assertEquals(createNetworkResponse.getNetworkFqdn(), network.getContrailNetworkFqdn());
		assertEquals(OrchestrationStatus.CREATED, network.getOrchestrationStatus());
		assertEquals(createNetworkResponse.getNetworkStackId(), network.getHeatStackId());
		assertEquals(createNetworkResponse.getNeutronNetworkId(), network.getNeutronNetworkId());
	}

	@Test
	public void updateNetworkCreatedkExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiNetworkResources).updateNetwork(network);
		aaiUpdateTasks.updateNetworkCreated(execution);
	}
	
	@Test
	public void updateObjectNetworkTest() {
		doNothing().when(aaiNetworkResources).updateNetwork(network);
		
		aaiUpdateTasks.updateObjectNetwork(execution);
		
		verify(aaiNetworkResources, times(1)).updateNetwork(network);
	}
	
	@Test
	public void updateObjectNetworkExceptionText() {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(aaiNetworkResources).updateNetwork(network);
		
		aaiUpdateTasks.updateObjectNetwork(execution);
	}
	
	@Test
	public void test_updateServiceInstance() {
		doNothing().when(aaiServiceInstanceResources).updateServiceInstance(serviceInstance);
		aaiUpdateTasks.updateServiceInstance(execution);
		verify(aaiServiceInstanceResources, times(1)).updateServiceInstance(serviceInstance);
	}

	@Test
	public void test_updateServiceInstance_exception() {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiServiceInstanceResources).updateServiceInstance(serviceInstance);
		aaiUpdateTasks.updateServiceInstance(execution);
	}
	
	@Test
	public void updateObjectVnfTest() {
		doNothing().when(aaiVnfResources).updateObjectVnf(genericVnf);
		
		aaiUpdateTasks.updateObjectVnf(execution);
		
		verify(aaiVnfResources, times(1)).updateObjectVnf(genericVnf);
	}
	
	@Test
	public void updateObjectVnfExceptionTest() {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiVnfResources).updateObjectVnf(genericVnf);
		aaiUpdateTasks.updateObjectVnf(execution);
	}
	
	@Test
	public void updateOrchestrationStatusDeleteVfModuleTest() throws Exception {
		doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ASSIGNED);

		aaiUpdateTasks.updateOrchestrationStatusDeleteVfModule(execution);

		verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ASSIGNED);
		assertEquals("", vfModule.getHeatStackId());
	}
	
	@Test
	public void updateModelVfModuleTest() {
		doNothing().when(aaiVfModuleResources).changeAssignVfModule(vfModule, genericVnf);
		aaiUpdateTasks.updateModelVfModule(execution);
		verify(aaiVfModuleResources, times(1)).changeAssignVfModule(vfModule, genericVnf);
	}
	
	@Test
	public void updateModelVfModuleExceptionTest() {
		expectedException.expect(BpmnError.class);
		doThrow(Exception.class).when(aaiVfModuleResources).changeAssignVfModule(vfModule, genericVnf);
		aaiUpdateTasks.updateModelVfModule(execution);
	}
}
