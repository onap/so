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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

public class AAICreateTasksTest extends BaseTaskTest{
	
	@InjectMocks
	private AAICreateTasks aaiCreateTasks = new AAICreateTasks();


	private ServiceInstance serviceInstance;
	private L3Network network;
	private GenericVnf genericVnf;
	private VolumeGroup volumeGroup;
	private CloudRegion cloudRegion;
	private VfModule vfModule;
	private Customer customer;
	private Configuration configuration;
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
	@Before
	public void before() throws BBObjectNotFoundException {
		customer = setCustomer();
		serviceInstance = setServiceInstance();
		network = setL3Network();
		genericVnf = setGenericVnf();
		volumeGroup = setVolumeGroup();
		cloudRegion = setCloudRegion();
		vfModule = setVfModule();
		configuration = setConfiguration();
		
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID), any())).thenReturn(genericVnf);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID), any())).thenReturn(vfModule);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.NETWORK_ID), any())).thenReturn(network);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VOLUME_GROUP_ID), any())).thenReturn(volumeGroup);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID), any())).thenReturn(serviceInstance);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.CONFIGURATION_ID), any())).thenReturn(configuration);
		

		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
		doThrow(new BpmnError("BPMN Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));

	}
	
	@Test
	public void createServiceInstanceTest() throws Exception {
		doNothing().when(aaiServiceInstanceResources).createServiceInstance(serviceInstance, customer);
		aaiCreateTasks.createServiceInstance(execution);
		verify(aaiServiceInstanceResources, times(1)).createServiceInstance(serviceInstance, customer);
	}
	
	@Test
	public void createServiceInstanceExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "notfound");
		doThrow(RuntimeException.class).when(aaiServiceInstanceResources).createServiceInstance(serviceInstance, customer);
		aaiCreateTasks.createServiceInstance(execution);
	}
	
	@Test
	public void createVolumeGroupTest() throws Exception {
		volumeGroup.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		
		doNothing().when(aaiVolumeGroupResources).createVolumeGroup(volumeGroup, cloudRegion);
		doNothing().when(aaiVolumeGroupResources).connectVolumeGroupToVnf(genericVnf, volumeGroup, cloudRegion);
		
		aaiCreateTasks.createVolumeGroup(execution);
		
		verify(aaiVolumeGroupResources, times(1)).createVolumeGroup(volumeGroup, cloudRegion);
		verify(aaiVolumeGroupResources, times(1)).connectVolumeGroupToVnf(genericVnf, volumeGroup, cloudRegion);
		verify(aaiVolumeGroupResources, times(1)).connectVolumeGroupToTenant(volumeGroup, cloudRegion);
	}
	
	@Test
	public void createVolumeGroupExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		volumeGroup.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		
		doThrow(RuntimeException.class).when(aaiVolumeGroupResources).createVolumeGroup(volumeGroup, cloudRegion);
		
		aaiCreateTasks.createVolumeGroup(execution);
	}
	
	@Test
	public void createProjectTest() throws Exception {
		doNothing().when(aaiServiceInstanceResources).createProjectandConnectServiceInstance(serviceInstance.getProject(), serviceInstance);
		aaiCreateTasks.createProject(execution);
		verify(aaiServiceInstanceResources, times(1)).createProjectandConnectServiceInstance(serviceInstance.getProject(), serviceInstance);
	}
	
	@Test
	public void createPlatformTest() throws Exception {
		doNothing().when(aaiVnfResources).createPlatformandConnectVnf(genericVnf.getPlatform(), genericVnf);
		aaiCreateTasks.createPlatform(execution);
		verify(aaiVnfResources, times(1)).createPlatformandConnectVnf(genericVnf.getPlatform(), genericVnf);
	}
	
	@Test
	public void createLineOfBusinessTest() throws Exception {
		doNothing().when(aaiVnfResources).createLineOfBusinessandConnectVnf(genericVnf.getLineOfBusiness(), genericVnf);
		aaiCreateTasks.createLineOfBusiness(execution);
		verify(aaiVnfResources, times(1)).createLineOfBusinessandConnectVnf(genericVnf.getLineOfBusiness(), genericVnf);
	}
	
	@Test
	public void createProjectExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "notfound");
		doThrow(RuntimeException.class).when(aaiServiceInstanceResources).createProjectandConnectServiceInstance(serviceInstance.getProject(), serviceInstance);
		aaiCreateTasks.createProject(execution);
	}
	
	@Test
	public void createProjectNullProjectNameTest() throws Exception {
		serviceInstance.getProject().setProjectName(null);
		doNothing().when(aaiServiceInstanceResources).createProjectandConnectServiceInstance(serviceInstance.getProject(), serviceInstance);
		aaiCreateTasks.createProject(execution);
		verify(aaiServiceInstanceResources, times(0)).createProjectandConnectServiceInstance(serviceInstance.getProject(), serviceInstance);
	}
	
	@Test
	public void createProjectEmptyProjectNameTest() throws Exception {
		serviceInstance.getProject().setProjectName("");
		doNothing().when(aaiServiceInstanceResources).createProjectandConnectServiceInstance(serviceInstance.getProject(), serviceInstance);
		aaiCreateTasks.createProject(execution);
		verify(aaiServiceInstanceResources, times(0)).createProjectandConnectServiceInstance(serviceInstance.getProject(), serviceInstance);
	}
	
	@Test
	public void createOwningEntityTest() throws Exception {
		doReturn(true).when(aaiServiceInstanceResources).existsOwningEntity(serviceInstance.getOwningEntity());
		doNothing().when(aaiServiceInstanceResources).connectOwningEntityandServiceInstance(serviceInstance.getOwningEntity(),serviceInstance);
		aaiCreateTasks.createOwningEntity(execution);
		verify(aaiServiceInstanceResources, times(1)).existsOwningEntity(serviceInstance.getOwningEntity());
		verify(aaiServiceInstanceResources, times(1)).connectOwningEntityandServiceInstance(serviceInstance.getOwningEntity(), serviceInstance);
	}
	
	@Test
	public void createOwningEntityNotExistsOwningEntityTest() throws Exception {
		doReturn(false).when(aaiServiceInstanceResources).existsOwningEntity(serviceInstance.getOwningEntity());
		doNothing().when(aaiServiceInstanceResources).createOwningEntityandConnectServiceInstance(serviceInstance.getOwningEntity(),serviceInstance);
		aaiCreateTasks.createOwningEntity(execution);
		verify(aaiServiceInstanceResources, times(1)).existsOwningEntity(serviceInstance.getOwningEntity());
		verify(aaiServiceInstanceResources, times(1)).createOwningEntityandConnectServiceInstance(serviceInstance.getOwningEntity(), serviceInstance);
	}
	
	@Test
	public void createOwningEntityNullOwningEntityIdTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		serviceInstance.getOwningEntity().setOwningEntityId(null);
		
		aaiCreateTasks.createOwningEntity(execution);
	}
	
	@Test
	public void createOwningEntityEmptyOwningEntityIdTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		serviceInstance.getOwningEntity().setOwningEntityId("");
		
		aaiCreateTasks.createOwningEntity(execution);
	}
	
	@Test
	public void createOwningEntityNullOwningEntityNameTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		serviceInstance.getOwningEntity().setOwningEntityName(null);
		
		doReturn(false).when(aaiServiceInstanceResources).existsOwningEntity(serviceInstance.getOwningEntity());
		
		aaiCreateTasks.createOwningEntity(execution);
	}
	
	@Test
	public void createOwningEntityEmptyOwningEntityNameTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		serviceInstance.getOwningEntity().setOwningEntityName("");
		
		doReturn(false).when(aaiServiceInstanceResources).existsOwningEntity(serviceInstance.getOwningEntity());
		
		aaiCreateTasks.createOwningEntity(execution);
	}
	
	@Test
	public void createOwningEntityExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doReturn(true).when(aaiServiceInstanceResources).existsOwningEntity(serviceInstance.getOwningEntity());
		
		doThrow(RuntimeException.class).when(aaiServiceInstanceResources).connectOwningEntityandServiceInstance(serviceInstance.getOwningEntity(), serviceInstance);
		
		aaiCreateTasks.createOwningEntity(execution);
	}
	
	@Test
	public void createVnfTest() throws Exception {
		doNothing().when(aaiVnfResources).createVnfandConnectServiceInstance(genericVnf, serviceInstance);
		aaiCreateTasks.createVnf(execution);
		verify(aaiVnfResources, times(1)).createVnfandConnectServiceInstance(genericVnf, serviceInstance);
	}
	
	@Test
	public void createVnfExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);		
		lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "notfound");
		doThrow(BBObjectNotFoundException.class).when(extractPojosForBB).extractByKey(any(),ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID),eq("notfound"));	
		doNothing().when(aaiVnfResources).createVnfandConnectServiceInstance(genericVnf, serviceInstance);
		aaiCreateTasks.createVnf(execution);
		verify(aaiVnfResources, times(1)).createVnfandConnectServiceInstance(genericVnf, serviceInstance);
	}
	

	@Test
	public void createVfModuleTest() throws Exception {
		doNothing().when(aaiVfModuleResources).createVfModule(vfModule, genericVnf);
		aaiCreateTasks.createVfModule(execution);
		verify(aaiVfModuleResources, times(1)).createVfModule(vfModule, genericVnf);
	}

	@Test
    public void createServiceSubscriptionTest(){
	    doNothing().when(aaiServiceInstanceResources).createServiceSubscription(customer);
	    aaiCreateTasks.createServiceSubscription(execution);
	    verify(aaiServiceInstanceResources, times(1)).createServiceSubscription(customer);
    }

    @Test
    public void createServiceSubscriptionTestExceptionHandling(){
        expectedException.expect(BpmnError.class);
        doThrow(RuntimeException.class).when(aaiServiceInstanceResources).createServiceSubscription(customer);
        aaiCreateTasks.createServiceSubscription(execution);
    }

    @Test
    public void createServiceSubscriptionTestCustomerIsNull(){
        expectedException.expect(BpmnError.class);
        gBBInput.setCustomer(null);
        aaiCreateTasks.createServiceSubscription(execution);
    }

	@Test
	public void createVfModuleExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(RuntimeException.class).when(aaiVfModuleResources).createVfModule(vfModule, genericVnf);
		aaiCreateTasks.createVfModule(execution);
	}
	
	@Test
	public void connectVfModuleToVolumeGroupTest() throws Exception {
		doNothing().when(aaiVfModuleResources).connectVfModuleToVolumeGroup(genericVnf, vfModule, volumeGroup, cloudRegion);
		aaiCreateTasks.connectVfModuleToVolumeGroup(execution);
		verify(aaiVfModuleResources, times(1)).connectVfModuleToVolumeGroup(genericVnf, vfModule, volumeGroup, cloudRegion);
	}
	
	@Test
	public void createNetworkTest() throws Exception {
		network.getModelInfoNetwork().setNeutronNetworkType("PROVIDER");
		
		doNothing().when(aaiNetworkResources).createNetworkConnectToServiceInstance(network,serviceInstance);
		aaiCreateTasks.createNetwork(execution);
		verify(aaiNetworkResources, times(1)).createNetworkConnectToServiceInstance(network, serviceInstance);
	}
	
	@Test
	public void createNetworkExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		lookupKeyMap.put(ResourceKey.NETWORK_ID, "notfound");
		doThrow(RuntimeException.class).when(aaiNetworkResources).createNetworkConnectToServiceInstance(network,serviceInstance);
		aaiCreateTasks.createNetwork(execution);
	}
	
	@Test
	public void createCustomerTest() throws Exception {
		doNothing().when(aaiVpnBindingResources).createCustomer(customer);
		
		aaiCreateTasks.createCustomer(execution);
		
		verify(aaiVpnBindingResources, times(1)).createCustomer(customer);
	}
	
	@Test
	public void createCustomerExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(RuntimeException.class).when(aaiVpnBindingResources).createCustomer(customer);
		
		aaiCreateTasks.createCustomer(execution);
	}
	
	@Test
	public void createNetworkCollectionTest() throws Exception {
		doNothing().when(aaiNetworkResources).createNetworkCollection(serviceInstance.getCollection());
		execution.setVariable("networkCollectionName", "testNetworkCollectionName");
		aaiCreateTasks.createNetworkCollection(execution);
		verify(aaiNetworkResources, times(1)).createNetworkCollection(serviceInstance.getCollection());
	}

	@Test
	public void createNetworkCollectionInstanceGroupTest() throws Exception {
		doNothing().when(aaiNetworkResources).createNetworkInstanceGroup(serviceInstance.getCollection().getInstanceGroup());
		aaiCreateTasks.createNetworkCollectionInstanceGroup(execution);
		verify(aaiNetworkResources, times(1)).createNetworkInstanceGroup(serviceInstance.getCollection().getInstanceGroup());
	}
	
	@Test
	public void connectNetworkToNetworkCollectionServiceInstanceTest() throws Exception {
		doNothing().when(aaiNetworkResources).connectNetworkToNetworkCollectionServiceInstance(network, serviceInstance);
		aaiCreateTasks.connectNetworkToNetworkCollectionServiceInstance(execution);
		verify(aaiNetworkResources, times(1)).connectNetworkToNetworkCollectionServiceInstance(network, serviceInstance);
	}
	
	@Test
	public void connectNetworkToNetworkCollectionInstanceGroupTest() throws Exception {
		doNothing().when(aaiNetworkResources).connectNetworkToNetworkCollectionInstanceGroup(network, serviceInstance.getCollection().getInstanceGroup());
		aaiCreateTasks.connectNetworkToNetworkCollectionInstanceGroup(execution);
		verify(aaiNetworkResources, times(1)).connectNetworkToNetworkCollectionInstanceGroup(network, serviceInstance.getCollection().getInstanceGroup());
	}
	
	@Test
	public void connectNetworkToNullNetworkCollectionInstanceGroupTest() throws Exception {
		//reset test data to have no network collection instance group
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setServiceInstanceId("testServiceInstanceId");
		L3Network network = new L3Network();
		network.setNetworkId("testNetworkId");
		serviceInstance.getNetworks().add(network);
		lookupKeyMap.put(ResourceKey.NETWORK_ID, network.getNetworkId());
		gBBInput.setServiceInstance(serviceInstance);
		lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, serviceInstance.getServiceInstanceId());
		
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID), eq("testServiceInstanceId"))).thenReturn(serviceInstance);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.NETWORK_ID),eq("testNetworkId"))).thenReturn(serviceInstance);
		//verify connection call was not executednetwork
		exception.expect(BpmnError.class);
		aaiCreateTasks.connectNetworkToNetworkCollectionInstanceGroup(execution);
		verify(aaiNetworkResources, never()).connectNetworkToNetworkCollectionInstanceGroup(network, null);
	}
	
	@Test
	public void connectNetworkToCloudRegionTest() throws Exception {
		gBBInput = execution.getGeneralBuildingBlock();
		doNothing().when(aaiNetworkResources).connectNetworkToCloudRegion(network, gBBInput.getCloudRegion());
		aaiCreateTasks.connectNetworkToCloudRegion(execution);
		verify(aaiNetworkResources, times(1)).connectNetworkToCloudRegion(network, gBBInput.getCloudRegion());
	}
	
	@Test
	public void connectNetworkToTenantTest() throws Exception {
		gBBInput = execution.getGeneralBuildingBlock();
		doNothing().when(aaiNetworkResources).connectNetworkToTenant(network, gBBInput.getCloudRegion());
		aaiCreateTasks.connectNetworkToTenant(execution);
		verify(aaiNetworkResources, times(1)).connectNetworkToTenant(network, gBBInput.getCloudRegion());
	}
	
	@Test
	public void createConfigurationTest() throws Exception {
		gBBInput = execution.getGeneralBuildingBlock();
		doNothing().when(aaiConfigurationResources).createConfiguration(configuration);
		aaiCreateTasks.createConfiguration(execution);
		verify(aaiConfigurationResources, times(1)).createConfiguration(configuration);
	}
	
	@Test
	public void connectVnfToCloudRegionTest() throws Exception {
		gBBInput = execution.getGeneralBuildingBlock();
		doNothing().when(aaiVnfResources).connectVnfToCloudRegion(genericVnf, gBBInput.getCloudRegion());
		aaiCreateTasks.connectVnfToCloudRegion(execution);
		verify(aaiVnfResources, times(1)).connectVnfToCloudRegion(genericVnf, gBBInput.getCloudRegion());
	}
	
	@Test
	public void connectNoneToVnfToCloudRegionTest() throws Exception {
		String[] arr = new String[1];
		arr[0] = "test25Region2";
		doReturn(arr).when(env).getProperty("mso.bpmn.cloudRegionIdsToSkipAddingVnfEdgesTo", String[].class);
		gBBInput = execution.getGeneralBuildingBlock();
		gBBInput.getCloudRegion().setLcpCloudRegionId("test25Region2");
		doNothing().when(aaiVnfResources).connectVnfToCloudRegion(genericVnf, gBBInput.getCloudRegion());
		aaiCreateTasks.connectVnfToCloudRegion(execution);
		verify(aaiVnfResources, times(0)).connectVnfToCloudRegion(genericVnf, gBBInput.getCloudRegion());
	}
	
	@Test
	public void connectVnfTenantTest() throws Exception {
		gBBInput = execution.getGeneralBuildingBlock();
		doNothing().when(aaiVnfResources).connectVnfToTenant(genericVnf, gBBInput.getCloudRegion());
		aaiCreateTasks.connectVnfToTenant(execution);
		verify(aaiVnfResources, times(1)).connectVnfToTenant(genericVnf, gBBInput.getCloudRegion());
	}
}
