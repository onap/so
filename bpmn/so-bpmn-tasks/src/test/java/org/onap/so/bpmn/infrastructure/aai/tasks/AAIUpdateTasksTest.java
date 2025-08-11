/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
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

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAICollectionResources;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.client.orchestration.AAINetworkResources;
import org.onap.so.client.orchestration.AAIPnfResources;
import org.onap.so.client.orchestration.AAIServiceInstanceResources;
import org.onap.so.client.orchestration.AAIVfModuleResources;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.client.orchestration.AAIVolumeGroupResources;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import java.util.HashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

@RunWith(MockitoJUnitRunner.Silent.class)
public class AAIUpdateTasksTest extends TestDataSetup {

    @Mock
    protected ExtractPojosForBB extractPojosForBB;
    @Mock
    protected ExceptionBuilder exceptionUtil;
    @Mock
    protected AAIServiceInstanceResources aaiServiceInstanceResources;
    @Mock
    protected AAIPnfResources aaiPnfResources;
    @Mock
    protected AAIVnfResources aaiVnfResources;
    @Mock
    protected AAIVfModuleResources aaiVfModuleResources;
    @Mock
    protected AAIVolumeGroupResources aaiVolumeGroupResources;
    @Mock
    protected AAINetworkResources aaiNetworkResources;
    @Mock
    protected AAICollectionResources aaiCollectionResources;
    @Mock
    protected AAIConfigurationResources aaiConfigurationResources;
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

        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.GENERIC_VNF_ID)))
                .thenReturn(genericVnf);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VF_MODULE_ID))).thenReturn(vfModule);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.NETWORK_ID))).thenReturn(network);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.VOLUME_GROUP_ID)))
                .thenReturn(volumeGroup);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstance);
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.CONFIGURATION_ID)))
                .thenReturn(configuration);


        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
    }

    @Test
    public void updateOrchestrationStatusAssignedServiceTest() {
        doNothing().when(aaiServiceInstanceResources).updateOrchestrationStatusServiceInstance(serviceInstance,
                OrchestrationStatus.ASSIGNED);

        aaiUpdateTasks.updateOrchestrationStatusAssignedService(execution);

        verify(aaiServiceInstanceResources, times(1)).updateOrchestrationStatusServiceInstance(serviceInstance,
                OrchestrationStatus.ASSIGNED);
    }

    @Test
    public void updateOrchestrationStatusAssignedServiceExceptionTest() {
        expectedException.expect(BpmnError.class);

        doThrow(RuntimeException.class).when(aaiServiceInstanceResources)
                .updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ASSIGNED);

        aaiUpdateTasks.updateOrchestrationStatusAssignedService(execution);
    }

    @Test
    public void updateOrchestrationStatusActiveServiceTest() {
        doNothing().when(aaiServiceInstanceResources).updateOrchestrationStatusServiceInstance(serviceInstance,
                OrchestrationStatus.ACTIVE);

        aaiUpdateTasks.updateOrchestrationStatusActiveService(execution);

        verify(aaiServiceInstanceResources, times(1)).updateOrchestrationStatusServiceInstance(serviceInstance,
                OrchestrationStatus.ACTIVE);
    }

    @Test
    public void updateOrchestrationStatusActiveServiceExceptionTest() {
        expectedException.expect(BpmnError.class);

        doThrow(RuntimeException.class).when(aaiServiceInstanceResources)
                .updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ACTIVE);

        aaiUpdateTasks.updateOrchestrationStatusActiveService(execution);
    }

    @Test
    public void updateOrchestrationStatusAssignedPnfTest() throws Exception {
        Pnf pnf = preparePnfAndExtractForPnf();
        doNothing().when(aaiPnfResources).updateOrchestrationStatusPnf(pnf, OrchestrationStatus.ASSIGNED);

        aaiUpdateTasks.updateOrchestrationStatusAssignedPnf(execution);

        verify(aaiPnfResources, times(1)).updateOrchestrationStatusPnf(pnf, OrchestrationStatus.ASSIGNED);
    }

    @Test
    public void updateOrchestrationStatusAssignedPnfExceptionTest() throws Exception {
        Pnf pnf = preparePnfAndExtractForPnf();
        doThrow(RuntimeException.class).when(aaiPnfResources).updateOrchestrationStatusPnf(pnf,
                OrchestrationStatus.ASSIGNED);

        expectedException.expect(BpmnError.class);
        aaiUpdateTasks.updateOrchestrationStatusAssignedPnf(execution);
    }

    @Test
    public void updateOrchestrationStatusInventoriedPnfTest() throws Exception {
        Pnf pnf = preparePnfAndExtractForPnf();
        doNothing().when(aaiPnfResources).updateOrchestrationStatusPnf(pnf, OrchestrationStatus.INVENTORIED);

        aaiUpdateTasks.updateOrchestrationStatusInventoriedPnf(execution);

        verify(aaiPnfResources, times(1)).updateOrchestrationStatusPnf(pnf, OrchestrationStatus.INVENTORIED);
    }

    @Test
    public void updateOrchestrationStatusInventoriedPnfExceptionTest() throws Exception {
        Pnf pnf = preparePnfAndExtractForPnf();
        doThrow(RuntimeException.class).when(aaiPnfResources).updateOrchestrationStatusPnf(pnf,
                OrchestrationStatus.INVENTORIED);

        expectedException.expect(BpmnError.class);
        aaiUpdateTasks.updateOrchestrationStatusInventoriedPnf(execution);
    }

    @Test
    public void updateOrchestrationStatusActivePnfTest() throws Exception {
        Pnf pnf = preparePnfAndExtractForPnf();
        doNothing().when(aaiPnfResources).updateOrchestrationStatusPnf(pnf, OrchestrationStatus.ACTIVE);

        aaiUpdateTasks.updateOrchestrationStatusActivePnf(execution);

        verify(aaiPnfResources, times(1)).updateOrchestrationStatusPnf(pnf, OrchestrationStatus.ACTIVE);
    }

    @Test
    public void updateOrchestrationStatusActivePnfExceptionTest() throws Exception {
        Pnf pnf = preparePnfAndExtractForPnf();
        doThrow(RuntimeException.class).when(aaiPnfResources).updateOrchestrationStatusPnf(pnf,
                OrchestrationStatus.ACTIVE);

        expectedException.expect(BpmnError.class);
        aaiUpdateTasks.updateOrchestrationStatusActivePnf(execution);
    }

    @Test
    public void updateOrchestrationStatusRegisterPnfTest() throws Exception {
        Pnf pnf = preparePnfAndExtractForPnf();
        doNothing().when(aaiPnfResources).updateOrchestrationStatusPnf(pnf, OrchestrationStatus.REGISTER);

        aaiUpdateTasks.updateOrchestrationStatusRegisterPnf(execution);

        verify(aaiPnfResources, times(1)).updateOrchestrationStatusPnf(pnf, OrchestrationStatus.REGISTER);
    }

    @Test
    public void updateOrchestrationStatusRegisteredPnfTest() throws Exception {
        Pnf pnf = preparePnfAndExtractForPnf();
        doNothing().when(aaiPnfResources).updateOrchestrationStatusPnf(pnf, OrchestrationStatus.REGISTERED);

        aaiUpdateTasks.updateOrchestrationStatusRegisteredPnf(execution);

        verify(aaiPnfResources, times(1)).updateOrchestrationStatusPnf(pnf, OrchestrationStatus.REGISTERED);
    }

    @Test
    public void updateOrchestrationStatusAssignedVnfTest() {
        doNothing().when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ASSIGNED);

        aaiUpdateTasks.updateOrchestrationStatusAssignedVnf(execution);

        verify(aaiVnfResources, times(1)).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ASSIGNED);
    }

    @Test
    public void updateOrchestrationStatusAssignedVnfExceptionTest() {
        expectedException.expect(BpmnError.class);

        doThrow(RuntimeException.class).when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf,
                OrchestrationStatus.ASSIGNED);

        aaiUpdateTasks.updateOrchestrationStatusAssignedVnf(execution);
    }

    @Test
    public void updateOrchestrationStatusActiveVnfTest() {
        doNothing().when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ACTIVE);

        aaiUpdateTasks.updateOrchestrationStatusActiveVnf(execution);

        verify(aaiVnfResources, times(1)).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.ACTIVE);
    }

    @Test
    public void updateOrchestrationStatusActiveVnfExceptionTest() {
        expectedException.expect(BpmnError.class);

        doThrow(RuntimeException.class).when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf,
                OrchestrationStatus.ACTIVE);

        aaiUpdateTasks.updateOrchestrationStatusActiveVnf(execution);
    }

    @Test
    public void updateOrchestrationStatusAssignVfModuleTest() {
        doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.ASSIGNED);
        aaiUpdateTasks.updateOrchestrationStatusAssignedVfModule(execution);
        verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.ASSIGNED);
        assertEquals("", vfModule.getHeatStackId());
    }

    @Test
    public void updateOrchestrationStatusAssignVfModuleExceptionTest() {
        doThrow(RuntimeException.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule,
                genericVnf, OrchestrationStatus.ASSIGNED);

        expectedException.expect(BpmnError.class);

        aaiUpdateTasks.updateOrchestrationStatusAssignedVfModule(execution);
    }

    @Test
    public void updateOrchestrationStatusCreatedVfModuleTest() {
        doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.CREATED);
        aaiUpdateTasks.updateOrchestrationStatusCreatedVfModule(execution);
        verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.CREATED);
    }

    @Test
    public void updateOrchestrationStatusCreatedVfModuleExceptionTest() {
        doThrow(RuntimeException.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule,
                genericVnf, OrchestrationStatus.CREATED);

        expectedException.expect(BpmnError.class);

        aaiUpdateTasks.updateOrchestrationStatusCreatedVfModule(execution);
    }

    @Test
    public void updateOrchestrationStatusPendingActivatefModuleTest() {
        doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.PENDING_ACTIVATION);

        aaiUpdateTasks.updateOrchestrationStatusPendingActivationVfModule(execution);

        verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.PENDING_ACTIVATION);
    }

    @Test
    public void updateOrchestrationStatusPendingActivatefModuleExceptionTest() {
        doThrow(RuntimeException.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule,
                genericVnf, OrchestrationStatus.PENDING_ACTIVATION);

        expectedException.expect(BpmnError.class);

        aaiUpdateTasks.updateOrchestrationStatusPendingActivationVfModule(execution);
    }

    @Test
    public void updateOrchestrationStatusDectivateVfModuleTest() {
        doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.CREATED);

        aaiUpdateTasks.updateOrchestrationStatusDeactivateVfModule(execution);

        verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.CREATED);
    }

    @Test
    public void updateOrchestrationStatusDectivateVfModuleExceptionTest() {
        doThrow(RuntimeException.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule,
                genericVnf, OrchestrationStatus.CREATED);

        expectedException.expect(BpmnError.class);

        aaiUpdateTasks.updateOrchestrationStatusDeactivateVfModule(execution);
    }

    @Test
    public void updateHeatStackIdVfModuleTest() {
        execution.setVariable("heatStackId", "newHeatStackId");
        doNothing().when(aaiVfModuleResources).updateHeatStackIdVfModule(vfModule, genericVnf);

        aaiUpdateTasks.updateHeatStackIdVfModule(execution);

        verify(aaiVfModuleResources, times(1)).updateHeatStackIdVfModule(vfModule, genericVnf);
        assertEquals("newHeatStackId", vfModule.getHeatStackId());
    }

    @Test
    public void updateHeatStackIdVfModuleToNullTest() {
        execution.setVariable("heatStackId", null);
        doNothing().when(aaiVfModuleResources).updateHeatStackIdVfModule(vfModule, genericVnf);

        aaiUpdateTasks.updateHeatStackIdVfModule(execution);

        verify(aaiVfModuleResources, times(1)).updateHeatStackIdVfModule(vfModule, genericVnf);
        assertEquals("", vfModule.getHeatStackId());
    }

    @Test
    public void updateHeatStackIdVfModuleExceptionTest() {
        doThrow(RuntimeException.class).when(aaiVfModuleResources).updateHeatStackIdVfModule(vfModule, genericVnf);

        expectedException.expect(BpmnError.class);

        aaiUpdateTasks.updateHeatStackIdVfModule(execution);
    }

    @Test
    public void updateOrchestrationStatusActiveVolumeGroupTest() {
        doNothing().when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion,
                OrchestrationStatus.ACTIVE);

        aaiUpdateTasks.updateOrchestrationStatusActiveVolumeGroup(execution);

        verify(aaiVolumeGroupResources, times(1)).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion,
                OrchestrationStatus.ACTIVE);
    }

    @Test
    public void updateOrchestrationStatusActiveVolumeGroupExceptionTest() {
        expectedException.expect(BpmnError.class);
        doThrow(RuntimeException.class).when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup,
                cloudRegion, OrchestrationStatus.ACTIVE);
        aaiUpdateTasks.updateOrchestrationStatusActiveVolumeGroup(execution);
    }

    @Test
    public void updateOrchestrationStatusCreatedVolumeGroupTest() {
        doNothing().when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion,
                OrchestrationStatus.CREATED);

        aaiUpdateTasks.updateOrchestrationStatusCreatedVolumeGroup(execution);

        verify(aaiVolumeGroupResources, times(1)).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion,
                OrchestrationStatus.CREATED);
    }

    @Test
    public void updateOrchestrationStatusCreatedVolumeGroupExceptionTest() {
        expectedException.expect(BpmnError.class);
        doThrow(RuntimeException.class).when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup,
                cloudRegion, OrchestrationStatus.CREATED);
        aaiUpdateTasks.updateOrchestrationStatusCreatedVolumeGroup(execution);
    }

    @Test
    public void test_updateOrchestrationStatusAssignedVolumeGroup() {
        doNothing().when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion,
                OrchestrationStatus.ASSIGNED);

        aaiUpdateTasks.updateOrchestrationStatusAssignedVolumeGroup(execution);

        verify(aaiVolumeGroupResources, times(1)).updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion,
                OrchestrationStatus.ASSIGNED);
        assertEquals("", volumeGroup.getHeatStackId());
    }

    @Test
    public void test_updateOrchestrationStatusAssignedVolumeGroup_exception() {
        expectedException.expect(BpmnError.class);
        doThrow(RuntimeException.class).when(aaiVolumeGroupResources).updateOrchestrationStatusVolumeGroup(volumeGroup,
                cloudRegion, OrchestrationStatus.ASSIGNED);
        aaiUpdateTasks.updateOrchestrationStatusAssignedVolumeGroup(execution);
    }

    @Test
    public void updateHeatStackIdVolumeGroupTest() {
        execution.setVariable("heatStackId", "newHeatStackId");
        doNothing().when(aaiVolumeGroupResources).updateHeatStackIdVolumeGroup(volumeGroup, cloudRegion);

        aaiUpdateTasks.updateHeatStackIdVolumeGroup(execution);

        verify(aaiVolumeGroupResources, times(1)).updateHeatStackIdVolumeGroup(volumeGroup, cloudRegion);
        assertEquals("newHeatStackId", volumeGroup.getHeatStackId());
    }

    @Test
    public void updateHeatStackIdVolumeGroupToNullTest() {
        execution.setVariable("heatStackId", null);
        doNothing().when(aaiVolumeGroupResources).updateHeatStackIdVolumeGroup(volumeGroup, cloudRegion);

        aaiUpdateTasks.updateHeatStackIdVolumeGroup(execution);

        verify(aaiVolumeGroupResources, times(1)).updateHeatStackIdVolumeGroup(volumeGroup, cloudRegion);
        assertEquals("", volumeGroup.getHeatStackId());
    }

    @Test
    public void updateHeatStackIdVolumeGroupExceptionTest() {
        expectedException.expect(BpmnError.class);
        doThrow(RuntimeException.class).when(aaiVolumeGroupResources).updateHeatStackIdVolumeGroup(volumeGroup,
                cloudRegion);
        aaiUpdateTasks.updateHeatStackIdVolumeGroup(execution);
    }

    @Test
    public void updateNetworkExceptionTest() {
        expectedException.expect(BpmnError.class);

        doThrow(RuntimeException.class).when(aaiNetworkResources).updateNetwork(network);

        aaiUpdateTasks.updateNetwork(execution, OrchestrationStatus.ACTIVE);
    }

    @Test
    public void updateOstatusActivedNetworkCollectionTest() {
        doNothing().when(aaiCollectionResources).updateCollection(serviceInstance.getCollection());
        aaiUpdateTasks.updateOrchestrationStatusActiveNetworkCollection(execution);
        verify(aaiCollectionResources, times(1)).updateCollection(serviceInstance.getCollection());
    }

    @Test
    public void updateOstatusActiveNetworkColectionExceptionTest() {
        expectedException.expect(BpmnError.class);
        doThrow(RuntimeException.class).when(aaiCollectionResources).updateCollection(serviceInstance.getCollection());
        aaiUpdateTasks.updateOrchestrationStatusActiveNetworkCollection(execution);
    }

    @Test
    public void updateOrchestrationStatusActivateVfModuleTest() {
        doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.ACTIVE);

        aaiUpdateTasks.updateOrchestrationStatusActivateVfModule(execution);

        verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.ACTIVE);
    }

    @Test
    public void updateOrchestrationStatusActivateVfModuleExceptionTest() {
        doThrow(RuntimeException.class).when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule,
                genericVnf, OrchestrationStatus.ACTIVE);

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
    public void updateNetworkUpdatedTest() throws Exception {
        UpdateNetworkResponse updateNetworkResponse = new UpdateNetworkResponse();
        updateNetworkResponse.setNeutronNetworkId("testNeutronNetworkId");
        HashMap<String, String> subnetMap = new HashMap<>();
        subnetMap.put("testSubnetId", "testNeutronSubnetId");
        updateNetworkResponse.setSubnetMap(subnetMap);

        network.getSubnets().add(subnet);

        execution.setVariable("updateNetworkResponse", updateNetworkResponse);

        doNothing().when(aaiNetworkResources).updateNetwork(network);
        doNothing().when(aaiNetworkResources).updateSubnet(network, subnet);

        aaiUpdateTasks.updateNetworkUpdated(execution);
        verify(aaiNetworkResources, times(1)).updateNetwork(network);
        verify(aaiNetworkResources, times(1)).updateSubnet(network, subnet);

        String neutronSubnetId = updateNetworkResponse.getSubnetMap().entrySet().iterator().next().getValue();
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
    public void updateOrchestrationStatusDeleteVfModuleTest() {
        doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.ASSIGNED);

        aaiUpdateTasks.updateOrchestrationStatusDeleteVfModule(execution);

        verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.ASSIGNED);
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
    public void updateOrchestrationStatusDeactivateFabricConfigurationTest() {
        gBBInput = execution.getGeneralBuildingBlock();
        doNothing().when(aaiConfigurationResources).updateOrchestrationStatusConfiguration(configuration,
                OrchestrationStatus.ASSIGNED);

        aaiUpdateTasks.updateOrchestrationStatusDeactivateFabricConfiguration(execution);

        verify(aaiConfigurationResources, times(1)).updateOrchestrationStatusConfiguration(configuration,
                OrchestrationStatus.ASSIGNED);
    }

    @Test
    public void updateOrchestrationStatusActivateFabricConfigurationTest() {
        gBBInput = execution.getGeneralBuildingBlock();
        doNothing().when(aaiConfigurationResources).updateOrchestrationStatusConfiguration(configuration,
                OrchestrationStatus.ACTIVE);

        aaiUpdateTasks.updateOrchestrationStatusActivateFabricConfiguration(execution);

        verify(aaiConfigurationResources, times(1)).updateOrchestrationStatusConfiguration(configuration,
                OrchestrationStatus.ACTIVE);
    }

    @Test
    public void updateOrchestrationStatusAssignedFabricConfigurationTest() {
        gBBInput = execution.getGeneralBuildingBlock();
        doNothing().when(aaiConfigurationResources).updateOrchestrationStatusConfiguration(configuration,
                OrchestrationStatus.ASSIGNED);

        aaiUpdateTasks.updateOrchestrationStatusAssignFabricConfiguration(execution);

        verify(aaiConfigurationResources, times(1)).updateOrchestrationStatusConfiguration(configuration,
                OrchestrationStatus.ASSIGNED);
    }

    @Test
    public void updateContrailServiceInstanceFqdnVfModuleTest() {
        execution.setVariable("contrailServiceInstanceFqdn", "newContrailServiceInstanceFqdn");
        doNothing().when(aaiVfModuleResources).updateContrailServiceInstanceFqdnVfModule(vfModule, genericVnf);

        aaiUpdateTasks.updateContrailServiceInstanceFqdnVfModule(execution);

        verify(aaiVfModuleResources, times(1)).updateContrailServiceInstanceFqdnVfModule(vfModule, genericVnf);
        assertEquals("newContrailServiceInstanceFqdn", vfModule.getContrailServiceInstanceFqdn());
    }

    @Test
    public void updateContrailServiceInstanceFqdnVfModuleNoUpdateTest() {
        aaiUpdateTasks.updateContrailServiceInstanceFqdnVfModule(execution);
        verify(aaiVfModuleResources, times(0)).updateContrailServiceInstanceFqdnVfModule(vfModule, genericVnf);
    }

    @Test
    public void updateIpv4OamAddressVnfTest() {
        execution.setVariable("oamManagementV4Address", "newIpv4OamAddress");
        doNothing().when(aaiVnfResources).updateObjectVnf(genericVnf);

        aaiUpdateTasks.updateIpv4OamAddressVnf(execution);

        verify(aaiVnfResources, times(1)).updateObjectVnf(genericVnf);
        assertEquals("newIpv4OamAddress", genericVnf.getIpv4OamAddress());
    }

    @Test
    public void updateIpv4OamAddressVnfNoUpdateTest() {
        aaiUpdateTasks.updateIpv4OamAddressVnf(execution);
        verify(aaiVnfResources, times(0)).updateObjectVnf(genericVnf);
    }

    @Test
    public void updateManagementV6AddressVnfTest() {
        execution.setVariable("oamManagementV6Address", "newManagementV6Address");
        doNothing().when(aaiVnfResources).updateObjectVnf(genericVnf);

        aaiUpdateTasks.updateManagementV6AddressVnf(execution);

        verify(aaiVnfResources, times(1)).updateObjectVnf(genericVnf);
        assertEquals("newManagementV6Address", genericVnf.getManagementV6Address());
    }

    @Test
    public void updateManagementV6AddressVnfNoUpdateTest() {
        aaiUpdateTasks.updateManagementV6AddressVnf(execution);
        verify(aaiVnfResources, times(0)).updateObjectVnf(genericVnf);
    }

    @Test
    public void updateOrchestrationStatusVnfConfigureTest() {
        doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.CONFIGURE);

        assertDoesNotThrow(() -> aaiUpdateTasks.updateOrchestrationStatusConfigDeployConfigureVnf(execution));
    }

    @Test
    public void updateOrchestrationStatusVnfConfiguredTest() {
        doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.CONFIGURED);

        assertDoesNotThrow(() -> aaiUpdateTasks.updateOrchestrationStatusConfigDeployConfiguredVnf(execution));
    }

    private Pnf preparePnfAndExtractForPnf() throws BBObjectNotFoundException {
        Pnf pnf = buildPnf();
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.PNF))).thenReturn(pnf);
        return pnf;
    }

    @Test
    public void updateOrchestrationStatusVnfConfigAssignedTest() {
        doNothing().when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.CONFIGASSIGNED);

        aaiUpdateTasks.updateOrchestrationStatus(execution, "vnf", "config-assign");

        verify(aaiVnfResources, times(1)).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.CONFIGASSIGNED);
    }

    @Test
    public void updateOrchestrationStatusVnfConfigDeployedTest() {
        doNothing().when(aaiVnfResources).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.CONFIGDEPLOYED);

        aaiUpdateTasks.updateOrchestrationStatus(execution, "vnf", "config-deploy");

        verify(aaiVnfResources, times(1)).updateOrchestrationStatusVnf(genericVnf, OrchestrationStatus.CONFIGDEPLOYED);
    }

    @Test
    public void updateOrchestrationStatusVfModuleConfigDeployedTest() {
        doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.CONFIGDEPLOYED);
        aaiUpdateTasks.updateOrchestrationStatus(execution, "vfmodule", "config-deploy");
        verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.CONFIGDEPLOYED);
    }

    @Test
    public void updateOrchestrationStatusVfModuleConfigAssignedTest() {
        doNothing().when(aaiVfModuleResources).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.CONFIGASSIGNED);
        aaiUpdateTasks.updateOrchestrationStatus(execution, "vfmodule", "config-assign");
        verify(aaiVfModuleResources, times(1)).updateOrchestrationStatusVfModule(vfModule, genericVnf,
                OrchestrationStatus.CONFIGASSIGNED);
    }
}
