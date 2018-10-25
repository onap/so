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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

public class AAIUpdateTasksTest extends BaseTaskTest{
	
	@InjectMocks
	private AAIUpdateTasks aaiUpdateTasks = new AAIUpdateTasks();
	
	private L3Network network;
	private ServiceInstance serviceInstance;
	private VfModule vfModule;
	private GenericVnf genericVnf;
	private VolumeGroup volumeGroup;
	private CloudRegion cloudRegion;
	private Configuration configuration;
	private Subnet subnet;
	
	@Before
	public void before() throws BBObjectNotFoundException {
		serviceInstance = setServiceInstance();
		genericVnf = setGenericVnf();
		vfModule = setVfModule();
		volumeGroup = setVolumeGroup();
		cloudRegion = setCloudRegion();
		network = setL3Network();
		configuration = setConfiguration();
		subnet = buildSubnet();

		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID), any())).thenReturn(genericVnf);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID), any())).thenReturn(vfModule);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.NETWORK_ID), any())).thenReturn(network);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VOLUME_GROUP_ID), any())).thenReturn(volumeGroup);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID), any())).thenReturn(serviceInstance);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.CONFIGURATION_ID), any())).thenReturn(configuration);
		

		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
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
		
		doThrow(RuntimeException.class).when(aaiServiceInstanceResources).updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ASSIGNED);

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
		
		doThrow(RuntimeException.class).when(aaiServiceInstanceResources).updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ACTIVE);

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
		
		doThrow(RuntimeException.class).when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ASSIGNED);

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
		
		doThrow(RuntimeException.class).when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ACTIVE);

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
		doThrow(RuntimeException.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ASSIGNED);
		
		expectedException.expect(BpmnError.class);
		
		aaiUpdateTasks.updateOrchestrationStatusAssignedVfModule(execution);
	}
	
	@Test
	public void updateOrchestrationStatusAssignedOrPendingActivationVfModuleNoMultiStageTest() throws Exception {
		ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
		modelInfoGenericVnf.setMultiStageDesign("false");
		genericVnf.setModelInfoGenericVnf(modelInfoGenericVnf);
		doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ASSIGNED);
		aaiUpdateTasks.updateOrchestrationStatusAssignedOrPendingActivationVfModule(execution);
		verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ASSIGNED);
		assertEquals("", vfModule.getHeatStackId());
	}
	
	@Test
	public void updateOrchestrationStatusAssignedOrPendingActivationVfModuleWithMultiStageTest() throws Exception {
		ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
		modelInfoGenericVnf.setMultiStageDesign("true");
		genericVnf.setModelInfoGenericVnf(modelInfoGenericVnf);
		doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.PENDING_ACTIVATION);
		aaiUpdateTasks.updateOrchestrationStatusAssignedOrPendingActivationVfModule(execution);
		verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.PENDING_ACTIVATION);
		assertEquals("", vfModule.getHeatStackId());
	}
	
	@Test
	public void updateOrchestrationStatusAssignedOrPendingActivationVfModuleExceptionTest() throws Exception {
		doThrow(RuntimeException.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ASSIGNED);
		
		expectedException.expect(BpmnError.class);
		
		aaiUpdateTasks.updateOrchestrationStatusAssignedOrPendingActivationVfModule(execution);
	}
	
	@Test
	public void updateOrchestrationStatusCreatedVfModuleTest() throws Exception {		
		doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.CREATED);
		aaiUpdateTasks.updateOrchestrationStatusCreatedVfModule(execution);
		verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.CREATED);
	}
	
	@Test
	public void updateOrchestrationStatusCreatedVfModuleExceptionTest() throws Exception {
		doThrow(RuntimeException.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.CREATED);
		
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
		doThrow(RuntimeException.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.PENDING_ACTIVATION);
		
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
		doThrow(RuntimeException.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.CREATED);
		
		expectedException.expect(BpmnError.class);
	
		aaiUpdateTasks.updateOrchestrationStatusDeactivateVfModule(execution);
	}
	
	@Test
	public void updateHeatStackIdVfModuleTest() throws Exception {
		execution.setVariable("heatStackId", "newHeatStackId");
		doNothing().when(aaiVfModuleResources).updateHeatStackIdVfModule(vfModule, genericVnf);

		aaiUpdateTasks.updateHeatStackIdVfModule(execution);

		verify(aaiVfModuleResources, times(1)).updateHeatStackIdVfModule(vfModule, genericVnf);
		assertEquals("newHeatStackId", vfModule.getHeatStackId());
	}
	
	@Test
	public void updateHeatStackIdVfModuleExceptionTest() throws Exception {
		doThrow(RuntimeException.class).when(aaiVfModuleResources).updateHeatStackIdVfModule(vfModule, genericVnf);
		
		expectedException.expect(BpmnError.class);
	
		aaiUpdateTasks.updateHeatStackIdVfModule(execution);
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
		doThrow(RuntimeException.class).when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.ACTIVE);
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
		doThrow(RuntimeException.class).when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.CREATED);
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
		doThrow(RuntimeException.class).when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.ASSIGNED);
		aaiUpdateTasks.updateOrchestrationStatusAssignedVolumeGroup(execution);
	}
	@Test
	public void updateHeatStackIdVolumeGroupTest() throws Exception {
		execution.setVariable("heatStackId", "newHeatStackId");
		doNothing().when(aaiVolumeGroupResources).updateHeatStackIdVolumeGroup(volumeGroup, cloudRegion);

		aaiUpdateTasks.updateHeatStackIdVolumeGroup(execution);

		verify(aaiVolumeGroupResources, times(1)).updateHeatStackIdVolumeGroup(volumeGroup, cloudRegion);
		assertEquals("newHeatStackId", volumeGroup.getHeatStackId());
	}
	
	@Test
	public void updateHeatStackIdVolumeGroupExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(RuntimeException.class).when(aaiVolumeGroupResources).updateHeatStackIdVolumeGroup(volumeGroup, cloudRegion);
		aaiUpdateTasks.updateHeatStackIdVolumeGroup(execution);
	}

	@Test
	public void updateNetworkExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);

		doThrow(RuntimeException.class).when(aaiNetworkResources).updateNetwork(network);
		
		aaiUpdateTasks.updateNetwork(execution, OrchestrationStatus.ACTIVE);
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
		doThrow(RuntimeException.class).when(aaiCollectionResources).updateCollection(serviceInstance.getCollection());
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
		doThrow(RuntimeException.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf, OrchestrationStatus.ACTIVE);
		
		expectedException.expect(BpmnError.class);
		
		aaiUpdateTasks.updateOrchestrationStatusActivateVfModule(execution);
	}
	
	@Test
	public void updateNetworkCreatedTest() throws Exception {
		CreateNetworkResponse createNetworkResponse = new CreateNetworkResponse();
		createNetworkResponse.setNetworkFqdn("testNetworkFqdn");
		createNetworkResponse.setNetworkStackId("testNetworkStackId");
		HashMap<String, String> subnetMap = new HashMap<>();
		subnetMap.put("testSubnetId", "testNeutronSubnetId");
		createNetworkResponse.setSubnetMap(subnetMap);
		
		network.getSubnets().add(subnet);
		
		execution.setVariable("createNetworkResponse", createNetworkResponse);
		
		doNothing().when(aaiNetworkResources).updateNetwork(network);
		doNothing().when(aaiNetworkResources).updateSubnet(network, subnet);

		aaiUpdateTasks.updateNetworkCreated(execution);
		verify(aaiNetworkResources, times(1)).updateNetwork(network);
		verify(aaiNetworkResources, times(1)).updateSubnet(network, subnet);
		
		assertEquals(createNetworkResponse.getNetworkFqdn(), network.getContrailNetworkFqdn());
		assertEquals(OrchestrationStatus.CREATED, network.getOrchestrationStatus());
		assertEquals(createNetworkResponse.getNetworkStackId(), network.getHeatStackId());
		assertEquals(createNetworkResponse.getNeutronNetworkId(), network.getNeutronNetworkId());
		String neutronSubnetId = createNetworkResponse.getSubnetMap().entrySet().iterator().next().getValue();
		assertEquals(neutronSubnetId, network.getSubnets().get(0).getNeutronSubnetId());
	}

	@Test
	public void updateOrchestrationStatusNetworkTest() {
		AAIUpdateTasks spy = Mockito.spy(new AAIUpdateTasks());
		doNothing().when(spy).updateNetwork(eq(execution), any());
		spy.updateOrchestrationStatusActiveNetwork(execution);
		verify(spy, times(1)).updateNetwork(execution, OrchestrationStatus.ACTIVE);
		spy.updateOrchestrationStatusAssignedNetwork(execution);
		verify(spy, times(1)).updateNetwork(execution, OrchestrationStatus.ASSIGNED);
		spy.updateOrchestrationStatusCreatedNetwork(execution);
		verify(spy, times(1)).updateNetwork(execution, OrchestrationStatus.CREATED);
	}
	
	@Test
	public void updateNetworkAAITest() {
		
		L3Network spy = spy(new L3Network());
		L3Network shallowCopy = mock(L3Network.class);
		Subnet mockSubnet = mock(Subnet.class);
		Subnet shallowCopySubnet = mock(Subnet.class);
		when(mockSubnet.shallowCopyId()).thenReturn(shallowCopySubnet);
		doReturn(shallowCopy).when(spy).shallowCopyId();
				
		doNothing().when(aaiNetworkResources).updateNetwork(network);
		doNothing().when(aaiNetworkResources).updateSubnet(network, subnet);
		
		spy.getSubnets().add(mockSubnet);
		aaiUpdateTasks.updateNetworkAAI(spy, OrchestrationStatus.CREATED);
			
		verify(shallowCopy, times(1)).setOrchestrationStatus(OrchestrationStatus.CREATED);
		verify(spy, times(1)).setOrchestrationStatus(OrchestrationStatus.CREATED);
		verify(shallowCopySubnet, times(1)).setOrchestrationStatus(OrchestrationStatus.CREATED);
	}
	@Test
	public void updateNetworkCreatedkExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		doThrow(RuntimeException.class).when(aaiNetworkResources).updateNetwork(network);
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
		
		doThrow(RuntimeException.class).when(aaiNetworkResources).updateNetwork(network);
		
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
		doThrow(RuntimeException.class).when(aaiServiceInstanceResources).updateServiceInstance(serviceInstance);
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
		doThrow(RuntimeException.class).when(aaiVnfResources).updateObjectVnf(genericVnf);
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
		doThrow(RuntimeException.class).when(aaiVfModuleResources).changeAssignVfModule(vfModule, genericVnf);
		aaiUpdateTasks.updateModelVfModule(execution);
	}
	
	@Test
	public void updateOrchestrationStatusDeactivateFabricConfigurationTest() throws Exception {
		gBBInput = execution.getGeneralBuildingBlock();
		doNothing().when(aaiConfigurationResources).updateOrchestrationStatusConfiguration(configuration, OrchestrationStatus.ASSIGNED);

		aaiUpdateTasks.updateOrchestrationStatusDeactivateFabricConfiguration(execution);

		verify(aaiConfigurationResources, times(1)).updateOrchestrationStatusConfiguration(configuration, OrchestrationStatus.ASSIGNED);
	}
	@Test
	public void updateOrchestrationStatusActivateFabricConfigurationTest() throws Exception {
		gBBInput = execution.getGeneralBuildingBlock();
		doNothing().when(aaiConfigurationResources).updateOrchestrationStatusConfiguration(configuration, OrchestrationStatus.ACTIVE);

		aaiUpdateTasks.updateOrchestrationStatusActivateFabricConfiguration(execution);

		verify(aaiConfigurationResources, times(1)).updateOrchestrationStatusConfiguration(configuration, OrchestrationStatus.ACTIVE);
	}
}
