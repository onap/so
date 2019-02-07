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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.TreeSet;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;

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
	
	@Captor
	ArgumentCaptor<NetworkPolicy> networkPolicyCaptor;
	
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
		
		VfModule newVfModule = setVfModule(false);
		newVfModule.setModuleIndex(null);
		newVfModule.getModelInfoVfModule().setModelInvariantUUID("testModelInvariantUUID1");
		doNothing().when(aaiVfModuleResources).createVfModule(newVfModule, genericVnf);
		when(extractPojosForBB.extractByKey(any(),ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID), any())).thenReturn(newVfModule);

		assertEquals(null, newVfModule.getModuleIndex());
		aaiCreateTasks.createVfModule(execution);
		assertEquals(1, newVfModule.getModuleIndex().intValue());
		verify(aaiVfModuleResources, times(1)).createVfModule(newVfModule, genericVnf);
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
	@Test
	public void createNetworkPolicyNeedToCreateAllTest() throws Exception {	
		execution.setVariable("heatStackId", "testHeatStackId");
		execution.setVariable("contrailNetworkPolicyFqdnList", "ABC123,ED456");
		Optional<NetworkPolicy> networkPolicy = Optional.empty();
		doReturn(networkPolicy).when(aaiNetworkResources).getNetworkPolicy(any(AAIResourceUri.class));
		doNothing().when(aaiNetworkResources).createNetworkPolicy(any(NetworkPolicy.class));
		aaiCreateTasks.createNetworkPolicies(execution);
		verify(aaiNetworkResources, times(2)).createNetworkPolicy(networkPolicyCaptor.capture());
		assertEquals("ABC123", networkPolicyCaptor.getAllValues().get(0).getNetworkPolicyFqdn());
		assertEquals("ED456", networkPolicyCaptor.getAllValues().get(1).getNetworkPolicyFqdn());
		assertEquals("testHeatStackId", networkPolicyCaptor.getAllValues().get(0).getHeatStackId());
		assertEquals("testHeatStackId", networkPolicyCaptor.getAllValues().get(1).getHeatStackId());
	}
	
	@Test
	public void createNetworkPolicyNeedToCreateNoneTest() throws Exception {
		execution.setVariable("heatStackId", "testHeatStackId");
		execution.setVariable("contrailNetworkPolicyFqdnList", "ABC123");
		NetworkPolicy networkPolicy = new NetworkPolicy();		
		doReturn(Optional.of(networkPolicy)).when(aaiNetworkResources).getNetworkPolicy(any(AAIResourceUri.class));
		doNothing().when(aaiNetworkResources).createNetworkPolicy(any(NetworkPolicy.class));
		aaiCreateTasks.createNetworkPolicies(execution);
		verify(aaiNetworkResources, times(0)).createNetworkPolicy(any(NetworkPolicy.class));
	}
	
	@Test
	public void createNetworkPolicyNoNetworkPoliciesTest() throws Exception {
		execution.setVariable("heatStackId", "testHeatStackId");	
		aaiCreateTasks.createNetworkPolicies(execution);
		verify(aaiNetworkResources, times(0)).createNetworkPolicy(any(NetworkPolicy.class));
	}
	
	@Test
	public void createVfModuleGetLowestIndexTest() throws Exception {
		GenericVnf vnf = new GenericVnf();
		ModelInfoGenericVnf vnfInfo = new ModelInfoGenericVnf();
		vnf.setModelInfoGenericVnf(vnfInfo);
		vnfInfo.setModelInvariantUuid("my-uuid");
		
		ModelInfoVfModule infoA = new ModelInfoVfModule();
		infoA.setIsBaseBoolean(false);
		infoA.setModelInvariantUUID("A");

		ModelInfoVfModule infoB = new ModelInfoVfModule();
		infoB.setIsBaseBoolean(false);
		infoB.setModelInvariantUUID("B");
		
		ModelInfoVfModule infoC = new ModelInfoVfModule();
		infoC.setIsBaseBoolean(false);
		infoC.setModelInvariantUUID("C");

		VfModule newVfModuleA = new VfModule();
		newVfModuleA.setVfModuleId("a");
		VfModule newVfModuleB = new VfModule();
		newVfModuleB.setVfModuleId("b");
		VfModule newVfModuleC = new VfModule();
		newVfModuleC.setVfModuleId("c");
		
		VfModule vfModule = new VfModule();
		vnf.getVfModules().add(vfModule);
		vfModule.setVfModuleId("1");
		
		VfModule vfModule2 = new VfModule();
		vnf.getVfModules().add(vfModule2);
		vfModule2.setVfModuleId("2");
		
		VfModule vfModule3 = new VfModule();
		vnf.getVfModules().add(vfModule3);
		vfModule3.setVfModuleId("3");
		
		VfModule vfModule4 = new VfModule();
		vnf.getVfModules().add(vfModule4);
		vfModule4.setVfModuleId("4");
		
		VfModule vfModule5 = new VfModule();
		vnf.getVfModules().add(vfModule5);
		vfModule5.setVfModuleId("5");
		
		//modules are included in the vnf already
		vnf.getVfModules().add(newVfModuleA);
		vnf.getVfModules().add(newVfModuleB);
		vnf.getVfModules().add(newVfModuleC);

		//A
		newVfModuleA.setModelInfoVfModule(infoA);
		vfModule.setModelInfoVfModule(infoA);
		vfModule2.setModelInfoVfModule(infoA);
		vfModule3.setModelInfoVfModule(infoA);

		//B

		newVfModuleB.setModelInfoVfModule(infoB);
		vfModule4.setModelInfoVfModule(infoB);
		vfModule5.setModelInfoVfModule(infoB);

		//C
		newVfModuleC.setModelInfoVfModule(infoC);

		
		//A
		vfModule.setModuleIndex(2);
		vfModule2.setModuleIndex(0);
		vfModule3.setModuleIndex(3);
		
		//B
		vfModule4.setModuleIndex(null);
		vfModule5.setModuleIndex(1);

		assertEquals(1, aaiCreateTasks.getLowestUnusedVfModuleIndexFromAAIVnfResponse(vnf, newVfModuleA));

		assertEquals(2, aaiCreateTasks.getLowestUnusedVfModuleIndexFromAAIVnfResponse(vnf, newVfModuleB));
		
		assertEquals(0, aaiCreateTasks.getLowestUnusedVfModuleIndexFromAAIVnfResponse(vnf, newVfModuleC));
		
	}
	
	@Test
	public void calculateUnusedIndexTest() {
		
		TreeSet<Integer> a = new TreeSet<>(Arrays.asList(0,1,3));
		TreeSet<Integer> b = new TreeSet<>(Arrays.asList(0,1,8));
		TreeSet<Integer> c = new TreeSet<>(Arrays.asList(0,2,4));
		assertEquals(2, aaiCreateTasks.calculateUnusedIndex(a, 0));
		assertEquals(5, aaiCreateTasks.calculateUnusedIndex(a, 2));
		
		assertEquals(4, aaiCreateTasks.calculateUnusedIndex(b, 2));
		assertEquals(3, aaiCreateTasks.calculateUnusedIndex(b, 1));
		
		assertEquals(5, aaiCreateTasks.calculateUnusedIndex(c, 2));
		assertEquals(9, aaiCreateTasks.calculateUnusedIndex(c, 6));
		assertEquals(1, aaiCreateTasks.calculateUnusedIndex(c, 0));

	}
}
