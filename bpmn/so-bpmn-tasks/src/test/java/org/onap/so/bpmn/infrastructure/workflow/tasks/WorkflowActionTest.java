/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2021 Orange
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader.ServiceEBBLoader;
import org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader.UserParamsServiceTraversal;
import org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader.VnfEBBLoader;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.DuplicateNameException;
import org.onap.so.client.orchestration.AAIEntityNotFoundException;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.core.env.Environment;

public class WorkflowActionTest extends BaseTaskTest {

    private static final String MACRO_ACTIVATE_DELETE_UNASSIGN_JSON = "Macro/ServiceMacroActivateDeleteUnassign.json";
    private static final String MACRO_ASSIGN_JSON = "Macro/ServiceMacroAssign.json";
    private static final String MACRO_ASSIGN_NO_CLOUD_JSON = "Macro/ServiceMacroAssignNoCloud.json";
    private static final String VF_MODULE_CREATE_WITH_FABRIC_JSON = "VfModuleCreateWithFabric.json";
    private static final String VF_MODULE_CREATE_WITH_FABRIC_NO_PARAMS_JSON = "VfModuleCreateWithFabricNoParams.json";
    private static final String VF_MODULE_REPLACE_REBUILD_VOLUME_GROUPS_JSON =
            "VfModuleReplaceRebuildVolumeGroups.json";
    private static final String MACRO_CREATE_NETWORK_COLLECTION_JSON = "Macro/CreateNetworkCollection.json";
    private static final String MACRO_VNF_MACRO_REPLACE_JSON = "Macro/VnfMacroReplace.json";
    private static final String MACRO_CREATE_JSON = "Macro/ServiceMacroAssignVnfAndPnf.json";
    private static final String MACRO_CREATE_SERVICE_MULTIPLE_SAME_MODEL_VNF_VFMODULE =
            "Macro/ServiceMacroCreateMultipleSameModelVnfsAndVfModules.json";

    @Mock
    protected Environment environment;
    @Mock
    protected UserParamsServiceTraversal userParamsServiceTraversal;

    @Mock
    private AaiResourceIdValidator aaiResourceIdValidator;
    @InjectMocks
    protected WorkflowAction workflowAction;

    private DelegateExecution execution;

    @InjectMocks
    @Spy
    protected WorkflowAction SPY_workflowAction;

    @Spy
    protected ExecuteBuildingBlockBuilder executeBuildingBlockBuilder;

    @InjectMocks
    @Spy
    protected VnfEBBLoader vnfEBBLoaderSpy;

    @InjectMocks
    @Spy
    protected ServiceEBBLoader serviceEBBLoader;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String RESOURCE_PATH = "src/test/resources/__files/";

    private List<OrchestrationFlow> replaceVfModuleOrchFlows =
            createFlowList("DeactivateVfModuleBB", "DeleteVfModuleATTBB", "DeactivateVolumeGroupBB",
                    "DeleteVolumeGroupBB", "UnassignVFModuleBB", "UnassignVolumeGroupBB", "AssignVolumeGroupBB",
                    "AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB", "CreateVolumeGroupBB",
                    "ActivateVolumeGroupBB", "ChangeModelVnfBB", "ChangeModelServiceInstanceBB");
    private List<OrchestrationFlow> replaceRetainAssignmentsVfModuleOrchFlows = createFlowList("DeactivateVfModuleBB",
            "DeleteVfModuleATTBB", "DeactivateVolumeGroupBB", "DeleteVolumeGroupBB", "UnassignVolumeGroupBB",
            "AssignVolumeGroupBB", "ChangeModelVfModuleBB", "CreateVolumeGroupBB", "ActivateVolumeGroupBB",
            "CreateVfModuleBB", "ActivateVfModuleBB", "ChangeModelVnfBB", "ChangeModelServiceInstanceBB");
    private List<OrchestrationFlow> replaceVfModuleWithFabricOrchFlows = createFlowList("DeleteFabricConfigurationBB",
            "DeactivateVfModuleBB", "DeleteVfModuleATTBB", "DeactivateVolumeGroupBB", "DeleteVolumeGroupBB",
            "UnassignVFModuleBB", "UnassignVolumeGroupBB", "AssignVolumeGroupBB", "AssignVfModuleBB",
            "CreateVfModuleBB", "ActivateVfModuleBB", "AddFabricConfigurationBB", "CreateVolumeGroupBB",
            "ActivateVolumeGroupBB", "ChangeModelVnfBB", "ChangeModelServiceInstanceBB");

    @Before
    public void before() throws Exception {
        execution = new DelegateExecutionFake();
        org.onap.aai.domain.yang.ServiceInstance servInstance = new org.onap.aai.domain.yang.ServiceInstance();
        servInstance.setServiceInstanceId("TEST");
        when(bbSetupUtils.getAAIServiceInstanceByName(anyString(), any())).thenReturn(servInstance);
        workflowAction.setBbInputSetupUtils(bbSetupUtils);
        workflowAction.setBbInputSetup(bbInputSetup);

    }

    /**
     * ALACARTE TESTS
     */
    @Test
    public void selectExecutionListALaCarteNetworkCreateTest() throws Exception {
        String gAction = "createInstance";
        String resource = "Network";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ASSIGN_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri", "v6/networks/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows =
                createFlowList("AssignNetwork1802BB", "CreateNetworkBB", "ActivateNetworkBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);

        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignNetwork1802BB", "CreateNetworkBB", "ActivateNetworkBB");
    }

    @Test
    public void selectExecutionListALaCarteNetworkDeleteTest() throws Exception {
        String gAction = "deleteInstance";
        String resource = "Network";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ASSIGN_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri", "v6/networks/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows =
                createFlowList("DeactivateNetworkBB", "DeleteNetworkBB", "UnassignNetwork1802BB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);

        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "DeactivateNetworkBB", "DeleteNetworkBB", "UnassignNetwork1802BB");
    }

    @Test
    public void selectExecutionListALaCarteServiceCreateTest() throws Exception {
        String gAction = "createInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ASSIGN_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri", "v6/serviceInstances/123");


        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB", "ActivateServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignServiceInstanceBB", "ActivateServiceInstanceBB");
    }

    @Test
    public void selectExecutionListDuplicateNameExceptionTest() throws Exception {
        String gAction = "createInstance";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ASSIGN_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri", "v6/serviceInstances");

        doThrow(new DuplicateNameException(
                "serviceInstance with name (instanceName) and different version id (3c40d244-808e-42ca-b09a-256d83d19d0a) already exists. The name must be unique."))
                        .when(aaiResourceIdValidator).validateResourceIdInAAI(anyString(), eq(WorkflowType.SERVICE),
                                eq("test"), any(RequestDetails.class), any(WorkflowResourceIds.class));

        SPY_workflowAction.selectExecutionList(execution);
        assertEquals(execution.getVariable("WorkflowActionErrorMessage"),
                "Exception while setting execution list. serviceInstance with name (instanceName) and different version id (3c40d244-808e-42ca-b09a-256d83d19d0a) already exists. The name must be unique.");

        // verify default values are present in failure case
        assertEquals(true, execution.getVariable("isTopLevelFlow"));
        assertEquals(false, execution.getVariable("sentSyncResponse"));
    }

    /**
     * SERVICE MACRO TESTS
     */
    @Test
    public void selectExecutionListServiceMacroAssignTest() throws Exception {
        String gAction = "assignInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB", "AssignNetworkBB", "AssignVnfBB",
                "AssignVolumeGroupBB", "AssignVfModuleBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setModelCustomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        HeatEnvironment volumeHeatEnv = new HeatEnvironment();
        vfModuleCustomization.setVolumeHeatEnv(volumeHeatEnv);
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        HeatTemplate volumeHeatTemplate = new HeatTemplate();
        vfModule.setVolumeHeatTemplate(volumeHeatTemplate);
        vfModuleCustomization.setVfModule(vfModule);

        VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
        vfModuleCustomization2.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
        HeatEnvironment heatEnvironment = new HeatEnvironment();
        vfModuleCustomization2.setHeatEnvironment(heatEnvironment);
        org.onap.so.db.catalog.beans.VfModule vfModule2 = new org.onap.so.db.catalog.beans.VfModule();
        HeatTemplate moduleHeatTemplate = new HeatTemplate();
        vfModule2.setModuleHeatTemplate(moduleHeatTemplate);
        vfModuleCustomization2.setVfModule(vfModule2);

        VfModuleCustomization vfModuleCustomization3 = vfModuleCustomization2;
        vfModuleCustomization3.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");

        when(userParamsServiceTraversal.getResourceListFromUserParams(any(), anyList(), anyString(), anyString()))
                .thenReturn(prepareListWithResources(false, false));
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f"))
                .thenReturn(vfModuleCustomization);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8"))
                .thenReturn(vfModuleCustomization2);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969"))
                .thenReturn(vfModuleCustomization3);

        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignServiceInstanceBB", "AssignVnfBB", "AssignVolumeGroupBB",
                "AssignVfModuleBB", "AssignVfModuleBB", "AssignVfModuleBB");
    }

    @Test
    public void selectExecutionListServiceMacroAssignNoCloudTest() throws Exception {
        String gAction = "assignInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ASSIGN_NO_CLOUD_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB", "AssignNetworkBB", "AssignVnfBB",
                "AssignVolumeGroupBB", "AssignVfModuleBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setModelCustomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        HeatEnvironment volumeHeatEnv = new HeatEnvironment();
        vfModuleCustomization.setVolumeHeatEnv(volumeHeatEnv);
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        HeatTemplate volumeHeatTemplate = new HeatTemplate();
        vfModule.setVolumeHeatTemplate(volumeHeatTemplate);
        vfModuleCustomization.setVfModule(vfModule);

        VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
        vfModuleCustomization2.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
        HeatEnvironment heatEnvironment = new HeatEnvironment();
        vfModuleCustomization2.setHeatEnvironment(heatEnvironment);
        org.onap.so.db.catalog.beans.VfModule vfModule2 = new org.onap.so.db.catalog.beans.VfModule();
        HeatTemplate moduleHeatTemplate = new HeatTemplate();
        vfModule2.setModuleHeatTemplate(moduleHeatTemplate);
        vfModuleCustomization2.setVfModule(vfModule2);

        VfModuleCustomization vfModuleCustomization3 = vfModuleCustomization2;
        vfModuleCustomization3.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");

        when(userParamsServiceTraversal.getResourceListFromUserParams(any(), anyList(), anyString(), anyString()))
                .thenReturn(prepareListWithResources(false, false));
        when(environment.getProperty("org.onap.so.cloud-owner")).thenReturn("att-aic");
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "att-aic")).thenReturn(northBoundRequest);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f"))
                .thenReturn(vfModuleCustomization);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8"))
                .thenReturn(vfModuleCustomization2);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969"))
                .thenReturn(vfModuleCustomization3);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignServiceInstanceBB", "AssignVnfBB", "AssignVolumeGroupBB",
                "AssignVfModuleBB", "AssignVfModuleBB", "AssignVfModuleBB");
    }

    @Test
    public void selectExecutionListServiceMacroActivateTest() throws Exception {
        String gAction = "activateInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ACTIVATE_DELETE_UNASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/si0");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows =
                createFlowList("CreateNetworkBB", "ActivateNetworkBB", "CreateVolumeGroupBB", "ActivateVolumeGroupBB",
                        "CreateVfModuleBB", "ActivateVfModuleBB", "ActivateVnfBB", "ActivateServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId("si0");
        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
        org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf();
        vnf.setVnfId("vnf0");

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule = buildVfModule();
        vnf.getVfModules().add(vfModule);
        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule2 = buildVfModule();
        vnf.getVfModules().add(vfModule2);

        org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroup0");
        vnf.getVolumeGroups().add(volumeGroup);

        serviceInstanceMSO.getVnfs().add(vnf);

        VfModule aaiVfModule = new VfModule();
        aaiVfModule.setIsBaseVfModule(false);

        doReturn(aaiVfModule).when(bbSetupUtils).getAAIVfModule(any(), any());
        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("si0");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        doReturn(Mockito.mock(GenericVnf.class)).when(bbSetupUtils).getAAIGenericVnf(any());
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "CreateVolumeGroupBB", "ActivateVolumeGroupBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB", "ActivateVnfBB",
                "ActivateServiceInstanceBB");
        assertEquals("volumeGroup0", ebbs.get(0).getWorkflowResourceIds().getVolumeGroupId());
        assertEquals("volumeGroup0", ebbs.get(1).getWorkflowResourceIds().getVolumeGroupId());
        assertEquals("testVfModuleId1", ebbs.get(2).getWorkflowResourceIds().getVfModuleId());
        assertEquals("testVfModuleId1", ebbs.get(3).getWorkflowResourceIds().getVfModuleId());
        assertEquals("testVfModuleId2", ebbs.get(4).getWorkflowResourceIds().getVfModuleId());
        assertEquals("testVfModuleId2", ebbs.get(5).getWorkflowResourceIds().getVfModuleId());
        assertEquals("vnf0", ebbs.get(6).getWorkflowResourceIds().getVnfId());
        assertEquals("si0", ebbs.get(7).getWorkflowResourceIds().getServiceInstanceId());
    }

    @Test
    public void selectExecutionListServiceMacroDeactivateTest() throws Exception {
        String gAction = "deactivateInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ACTIVATE_DELETE_UNASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("DeactivateServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(), "DeactivateServiceInstanceBB");
    }

    @Test
    public void selectExecutionListServiceMacroEmptyServiceTest() throws Exception {
        String gAction = "createInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ACTIVATE_DELETE_UNASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setIsToplevelflow(true);
        List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB", "CreateNetworkCollectionBB",
                "AssignNetworkBB", "AssignVnfBB", "AssignVolumeGroupBB", "AssignVfModuleBB", "CreateNetworkBB",
                "ActivateNetworkBB", "CreateVolumeGroupBB", "ActivateVolumeGroupBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB", "ActivateVnfBB",
                "ActivateNetworkCollectionBB", "ActivateServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        Service service = new Service();
        doReturn(service).when(catalogDbClient).getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a");
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignServiceInstanceBB", "ActivateServiceInstanceBB");
    }

    @Test
    public void selectExecutionListServiceMacroCreateJustNetworkTest() throws Exception {
        String gAction = "createInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ACTIVATE_DELETE_UNASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setIsToplevelflow(true);
        List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB", "CreateNetworkCollectionBB",
                "AssignNetworkBB", "AssignVnfBB", "AssignVolumeGroupBB", "AssignVfModuleBB", "CreateNetworkBB",
                "ActivateNetworkBB", "CreateVolumeGroupBB", "ActivateVolumeGroupBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB", "ActivateVnfBB",
                "ActivateNetworkCollectionBB", "ActivateServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        Service service = new Service();
        NetworkResourceCustomization networkCustomization = new NetworkResourceCustomization();
        networkCustomization.setModelCustomizationUUID("1234");
        service.getNetworkCustomizations().add(networkCustomization);
        doReturn(service).when(catalogDbClient).getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a");
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);

        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignServiceInstanceBB", "AssignNetworkBB", "CreateNetworkBB",
                "ActivateNetworkBB", "ActivateServiceInstanceBB");
    }

    @Test
    public void selectExecutionListServiceMacroCreateWithNetworkCollectionTest() throws Exception {
        String gAction = "createInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ACTIVATE_DELETE_UNASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setIsToplevelflow(true);
        List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB", "CreateNetworkCollectionBB",
                "AssignNetworkBB", "AssignVnfBB", "AssignVolumeGroupBB", "AssignVfModuleBB", "CreateNetworkBB",
                "ActivateNetworkBB", "CreateVolumeGroupBB", "ActivateVolumeGroupBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB", "ActivateVnfBB",
                "ActivateNetworkCollectionBB", "ActivateServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        Service service = new Service();
        List<NetworkResourceCustomization> networkCustomizations = new ArrayList<>();
        NetworkResourceCustomization networkCust = new NetworkResourceCustomization();
        networkCust.setModelCustomizationUUID("123");
        networkCustomizations.add(networkCust);
        service.setNetworkCustomizations(networkCustomizations);
        NetworkCollectionResourceCustomization collectionResourceCustomization =
                new NetworkCollectionResourceCustomization();
        collectionResourceCustomization.setModelCustomizationUUID("123");

        CollectionResource collectionResource = new CollectionResource();
        collectionResource.setToscaNodeType("NetworkCollection");
        InstanceGroup instanceGroup = new InstanceGroup();
        instanceGroup.setToscaNodeType("NetworkCollectionResource");
        instanceGroup.setCollectionNetworkResourceCustomizations(new ArrayList<>());
        CollectionNetworkResourceCustomization collectionNetworkResourceCust =
                new CollectionNetworkResourceCustomization();
        collectionNetworkResourceCust.setModelCustomizationUUID("123");
        collectionNetworkResourceCust.setNetworkResourceCustomization(collectionResourceCustomization);
        instanceGroup.getCollectionNetworkResourceCustomizations().add(collectionNetworkResourceCust);
        List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupCustomizations = new ArrayList<>();
        CollectionResourceInstanceGroupCustomization collectionInstanceGroupCustomization =
                new CollectionResourceInstanceGroupCustomization();
        collectionInstanceGroupCustomization.setModelCustomizationUUID("123");
        collectionInstanceGroupCustomization.setSubInterfaceNetworkQuantity(3);
        collectionInstanceGroupCustomizations.add(collectionInstanceGroupCustomization);
        collectionInstanceGroupCustomization.setInstanceGroup(instanceGroup);
        collectionInstanceGroupCustomization.setCollectionResourceCust(collectionResourceCustomization);
        instanceGroup.setCollectionInstanceGroupCustomizations(collectionInstanceGroupCustomizations);
        collectionResource.setInstanceGroup(instanceGroup);
        collectionResourceCustomization.setCollectionResource(collectionResource);;
        service.getCollectionResourceCustomizations().add(collectionResourceCustomization);
        doReturn(service).when(catalogDbClient).getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a");
        doReturn(collectionResourceCustomization).when(catalogDbClient)
                .getNetworkCollectionResourceCustomizationByID("123");
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignServiceInstanceBB", "CreateNetworkCollectionBB", "AssignNetworkBB",
                "CreateNetworkBB", "ActivateNetworkBB", "AssignNetworkBB", "CreateNetworkBB", "ActivateNetworkBB",
                "AssignNetworkBB", "CreateNetworkBB", "ActivateNetworkBB", "ActivateNetworkCollectionBB",
                "ActivateServiceInstanceBB");
        assertEquals("Network id not empty", !ebbs.get(2).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
        assertEquals("Network id not empty", !ebbs.get(3).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
        assertEquals("Network id not empty", !ebbs.get(4).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
        assertEquals("Network id same for AssignNetworkBB CreateNetworkBB ActivateNetworkBB",
                ebbs.get(2).getWorkflowResourceIds().getNetworkId() == ebbs.get(3).getWorkflowResourceIds()
                        .getNetworkId()
                        && ebbs.get(3).getWorkflowResourceIds().getNetworkId() == ebbs.get(4).getWorkflowResourceIds()
                                .getNetworkId(),
                true);
        assertEquals("Network id not empty", !ebbs.get(5).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
        assertEquals("Network id not empty", !ebbs.get(6).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
        assertEquals("Network id not empty", !ebbs.get(7).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
        assertEquals("Network id same for AssignNetworkBB CreateNetworkBB ActivateNetworkBB",
                ebbs.get(5).getWorkflowResourceIds().getNetworkId() == ebbs.get(6).getWorkflowResourceIds()
                        .getNetworkId()
                        && ebbs.get(6).getWorkflowResourceIds().getNetworkId() == ebbs.get(7).getWorkflowResourceIds()
                                .getNetworkId(),
                true);
        assertEquals("Network id not empty", !ebbs.get(8).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
        assertEquals("Network id not empty", !ebbs.get(9).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
        assertEquals("Network id not empty", !ebbs.get(10).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
        assertEquals("Network id same for AssignNetworkBB CreateNetworkBB ActivateNetworkBB",
                ebbs.get(8).getWorkflowResourceIds().getNetworkId() == ebbs.get(9).getWorkflowResourceIds()
                        .getNetworkId()
                        && ebbs.get(9).getWorkflowResourceIds().getNetworkId() == ebbs.get(10).getWorkflowResourceIds()
                                .getNetworkId(),
                true);
    }

    @Test
    public void selectExecutionListServiceMacroCreateWithUserParams() throws Exception {
        String gAction = "createInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB", "CreateNetworkCollectionBB",
                "AssignNetworkBB", "AssignVnfBB", "AssignVolumeGroupBB", "AssignVfModuleBB", "CreateNetworkBB",
                "ActivateNetworkBB", "CreateVolumeGroupBB", "ActivateVolumeGroupBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB", "ActivateVnfBB",
                "ActivateNetworkCollectionBB", "ActivateServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        Service service = new Service();
        service.setModelUUID("3c40d244-808e-42ca-b09a-256d83d19d0a");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setModelCustomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        HeatEnvironment volumeHeatEnv = new HeatEnvironment();
        vfModuleCustomization.setVolumeHeatEnv(volumeHeatEnv);
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        HeatTemplate volumeHeatTemplate = new HeatTemplate();
        vfModule.setVolumeHeatTemplate(volumeHeatTemplate);
        vfModuleCustomization.setVfModule(vfModule);

        VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
        vfModuleCustomization2.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
        HeatEnvironment heatEnvironment = new HeatEnvironment();
        vfModuleCustomization2.setHeatEnvironment(heatEnvironment);
        org.onap.so.db.catalog.beans.VfModule vfModule2 = new org.onap.so.db.catalog.beans.VfModule();
        HeatTemplate moduleHeatTemplate = new HeatTemplate();
        vfModule2.setModuleHeatTemplate(moduleHeatTemplate);
        vfModuleCustomization2.setVfModule(vfModule2);

        VfModuleCustomization vfModuleCustomization3 = vfModuleCustomization2;
        vfModuleCustomization3.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");

        when(userParamsServiceTraversal.getResourceListFromUserParams(any(), anyList(), anyString(), anyString()))
                .thenReturn(prepareListWithResources(false, false));
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f"))
                .thenReturn(vfModuleCustomization);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8"))
                .thenReturn(vfModuleCustomization2);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969"))
                .thenReturn(vfModuleCustomization3);
        when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignServiceInstanceBB", "AssignVnfBB", "AssignVolumeGroupBB",
                "AssignVfModuleBB", "AssignVfModuleBB", "AssignVfModuleBB", "CreateVolumeGroupBB",
                "ActivateVolumeGroupBB", "CreateVfModuleBB", "ActivateVfModuleBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB", "ActivateVnfBB",
                "ActivateServiceInstanceBB");

        assertEquals(3, ebbs.get(0).getWorkflowResourceIds().getServiceInstanceId().length());
        int randomUUIDLength = UUID.randomUUID().toString().length();
        assertEquals(randomUUIDLength, ebbs.get(1).getWorkflowResourceIds().getVnfId().length());
        assertEquals(randomUUIDLength, ebbs.get(2).getWorkflowResourceIds().getVolumeGroupId().length());
        assertEquals(randomUUIDLength, ebbs.get(3).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(4).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(5).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(6).getWorkflowResourceIds().getVolumeGroupId().length());
        assertEquals(randomUUIDLength, ebbs.get(7).getWorkflowResourceIds().getVolumeGroupId().length());
        assertEquals(randomUUIDLength, ebbs.get(8).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(9).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(10).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(11).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(12).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(13).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(14).getWorkflowResourceIds().getVnfId().length());
        assertEquals(3, ebbs.get(0).getWorkflowResourceIds().getServiceInstanceId().length());
        assertEquals(true, execution.getVariable("homing"));
    }

    @Test
    public void selectExecutionListServiceMacroCreateWithUserParamsAndPriorities() throws Exception {
        String gAction = "createInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_CREATE_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB", "CreateNetworkCollectionBB",
                "AssignNetworkBB", "AssignVnfBB", "AssignVolumeGroupBB", "AssignVfModuleBB", "AssignPnfBB",
                "WaitForPnfReadyBB", "ActivatePnfBB", "CreateNetworkBB", "ActivateNetworkBB", "CreateVolumeGroupBB",
                "ActivateVolumeGroupBB", "CreateVfModuleBB", "ActivateVfModuleBB", "AssignFabricConfigurationBB",
                "ActivateFabricConfigurationBB", "ActivateVnfBB", "ActivateNetworkCollectionBB",
                "ActivateServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        Service service = new Service();
        service.setModelUUID("3c40d244-808e-42ca-b09a-256d83d19d0a");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setModelCustomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
        HeatEnvironment volumeHeatEnv = new HeatEnvironment();
        vfModuleCustomization.setVolumeHeatEnv(volumeHeatEnv);
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        HeatTemplate volumeHeatTemplate = new HeatTemplate();
        vfModule.setVolumeHeatTemplate(volumeHeatTemplate);
        vfModuleCustomization.setVfModule(vfModule);

        VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
        vfModuleCustomization2.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
        HeatEnvironment heatEnvironment = new HeatEnvironment();
        vfModuleCustomization2.setHeatEnvironment(heatEnvironment);
        org.onap.so.db.catalog.beans.VfModule vfModule2 = new org.onap.so.db.catalog.beans.VfModule();
        HeatTemplate moduleHeatTemplate = new HeatTemplate();
        vfModule2.setModuleHeatTemplate(moduleHeatTemplate);
        vfModuleCustomization2.setVfModule(vfModule2);

        VfModuleCustomization vfModuleCustomization3 = vfModuleCustomization2;
        vfModuleCustomization3.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");

        when(userParamsServiceTraversal.getResourceListFromUserParams(any(), anyList(), anyString(), anyString()))
                .thenReturn(prepareListWithResources(true, true));
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f"))
                .thenReturn(vfModuleCustomization);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8"))
                .thenReturn(vfModuleCustomization2);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969"))
                .thenReturn(vfModuleCustomization3);
        when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignServiceInstanceBB", "AssignVnfBB", "AssignVolumeGroupBB",
                "AssignVfModuleBB", "AssignVfModuleBB", "AssignVfModuleBB", "AssignPnfBB", "WaitForPnfReadyBB",
                "ActivatePnfBB", "CreateVolumeGroupBB", "ActivateVolumeGroupBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "ActivateVnfBB", "ActivateServiceInstanceBB");

        assertEquals(3, ebbs.get(0).getWorkflowResourceIds().getServiceInstanceId().length());
        int randomUUIDLength = UUID.randomUUID().toString().length();
        assertEquals(randomUUIDLength, ebbs.get(1).getWorkflowResourceIds().getVnfId().length());
        assertEquals(randomUUIDLength, ebbs.get(2).getWorkflowResourceIds().getVolumeGroupId().length());
        assertEquals(randomUUIDLength, ebbs.get(3).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(4).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(5).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals("72d9d1cd-f46d-447a-abdb-451d6fb05fa8", ebbs.get(3).getBuildingBlock().getKey());
        assertEquals("da4d4327-fb7d-4311-ac7a-be7ba60cf969", ebbs.get(4).getBuildingBlock().getKey());
        assertEquals("3c40d244-808e-42ca-b09a-256d83d19d0a", ebbs.get(5).getBuildingBlock().getKey());
        assertEquals(randomUUIDLength, ebbs.get(6).getWorkflowResourceIds().getPnfId().length());
        assertEquals(randomUUIDLength, ebbs.get(7).getWorkflowResourceIds().getPnfId().length());
        assertEquals(randomUUIDLength, ebbs.get(8).getWorkflowResourceIds().getPnfId().length());
        assertEquals(randomUUIDLength, ebbs.get(9).getWorkflowResourceIds().getVolumeGroupId().length());
        assertEquals(randomUUIDLength, ebbs.get(10).getWorkflowResourceIds().getVolumeGroupId().length());
        assertEquals(randomUUIDLength, ebbs.get(11).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(12).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(13).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(14).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(15).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(16).getWorkflowResourceIds().getVfModuleId().length());
        assertEquals(randomUUIDLength, ebbs.get(17).getWorkflowResourceIds().getVnfId().length());
        assertEquals(3, ebbs.get(0).getWorkflowResourceIds().getServiceInstanceId().length());
        assertEquals(true, execution.getVariable("homing"));
    }

    @Test
    public void selectExecutionListServiceMacroCreateWithMultipleSameModelVnfAndVfModules() throws Exception {
        String gAction = "createInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_CREATE_SERVICE_MULTIPLE_SAME_MODEL_VNF_VFMODULE);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v7/serviceInstances");
        execution.setVariable("serviceInstanceId", UUID.randomUUID().toString());

        // Service-Macro-Create
        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB", "CreateNetworkCollectionBB",
                "AssignNetworkBB", "AssignVnfBB", "AssignVolumeGroupBB", "AssignVfModuleBB", "ControllerExecutionBB",
                "AssignPnfBB", "WaitForPnfReadyBB", "ControllerExecutionBB", "ControllerExecutionBB", "ActivatePnfBB",
                "CreateNetworkBB", "ActivateNetworkBB", "CreateVolumeGroupBB", "ActivateVolumeGroupBB",
                "CreateVfModuleBB", "ActivateVfModuleBB", "ControllerExecutionBB", "ActivateVnfBB",
                "ActivateNetworkCollectionBB", "ActivateServiceInstanceBB");
        orchFlows.get(6).setBpmnAction("config-assign");
        orchFlows.get(6).setBpmnScope("vnf");
        orchFlows.get(9).setBpmnAction("config-assign");
        orchFlows.get(9).setBpmnScope("pnf");
        orchFlows.get(10).setBpmnAction("config-deploy");
        orchFlows.get(10).setBpmnScope("pnf");
        orchFlows.get(18).setBpmnAction("config-deploy");
        orchFlows.get(18).setBpmnScope("vnf");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        Service service = new Service();
        service.setModelUUID("f2444885-3c76-4ddc-8668-7741c0631495");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setModelCustomizationUUID("3bd19000-6d21-49f1-9eb3-ea76a6eac5e0");
        vfModuleCustomization.setVolumeHeatEnv(new HeatEnvironment());
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        vfModule.setVolumeHeatTemplate(new HeatTemplate());
        vfModuleCustomization.setVfModule(vfModule);

        VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
        vfModuleCustomization2.setModelCustomizationUUID("83677d89-428a-407b-b4ec-738e68275d84");
        vfModuleCustomization2.setHeatEnvironment(new HeatEnvironment());
        org.onap.so.db.catalog.beans.VfModule vfModule2 = new org.onap.so.db.catalog.beans.VfModule();
        vfModule2.setModuleHeatTemplate(new HeatTemplate());
        vfModuleCustomization2.setVfModule(vfModule2);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "DEFAULT")).thenReturn(northBoundRequest);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("3bd19000-6d21-49f1-9eb3-ea76a6eac5e0"))
                .thenReturn(vfModuleCustomization);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("83677d89-428a-407b-b4ec-738e68275d84"))
                .thenReturn(vfModuleCustomization2);
        when(catalogDbClient.getServiceByID("f2444885-3c76-4ddc-8668-7741c0631495")).thenReturn(service);


        Resource serviceResource =
                new Resource(WorkflowType.SERVICE, "f2444885-3c76-4ddc-8668-7741c0631495", false, null);
        Resource vnfResource1 =
                new Resource(WorkflowType.VNF, "0d0ba1ee-6b7f-47fe-8266-2967993b2c08", false, serviceResource);
        vnfResource1.setInstanceName("vnf-instanceName-1");
        Resource vfmResource1 =
                new Resource(WorkflowType.VFMODULE, "3bd19000-6d21-49f1-9eb3-ea76a6eac5e0", false, vnfResource1);
        vfmResource1.setInstanceName("demo-network-1");
        Resource vfmResource2 =
                new Resource(WorkflowType.VFMODULE, "83677d89-428a-407b-b4ec-738e68275d84", false, vnfResource1);
        vfmResource2.setInstanceName("demo-1");
        Resource vnfResource2 =
                new Resource(WorkflowType.VNF, "0d0ba1ee-6b7f-47fe-8266-2967993b2c08", false, serviceResource);
        vnfResource2.setInstanceName("vnf-instanceName-2");
        Resource vfmResource3 =
                new Resource(WorkflowType.VFMODULE, "83677d89-428a-407b-b4ec-738e68275d84", false, vnfResource2);
        vfmResource3.setInstanceName("demo-2");
        Resource vfmResource4 =
                new Resource(WorkflowType.VFMODULE, "83677d89-428a-407b-b4ec-738e68275d84", false, vnfResource2);
        vfmResource4.setInstanceName("demo-3");

        when(userParamsServiceTraversal.getResourceListFromUserParams(any(), anyList(), anyString(), any()))
                .thenReturn(Arrays.asList(serviceResource, vnfResource1, vnfResource2, vfmResource1, vfmResource2,
                        vfmResource3, vfmResource4));

        workflowAction.selectExecutionList(execution);

        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        Map<String, List<ExecuteBuildingBlock>> flowNamesToEbbList =
                ebbs.stream().collect(Collectors.groupingBy(e -> e.getBuildingBlock().getBpmnFlowName()));

        assertEquals(1, flowNamesToEbbList.get("AssignServiceInstanceBB").size());
        assertEquals(2, flowNamesToEbbList.get("AssignVnfBB").size());
        assertEquals(4, flowNamesToEbbList.get("AssignVfModuleBB").size());
        assertEquals(4, flowNamesToEbbList.get("ControllerExecutionBB").size());
        assertEquals(4, flowNamesToEbbList.get("CreateVfModuleBB").size());
        assertEquals(4, flowNamesToEbbList.get("ActivateVfModuleBB").size());
        assertEquals(2, flowNamesToEbbList.get("ActivateVnfBB").size());
        assertEquals(1, flowNamesToEbbList.get("ActivateServiceInstanceBB").size());

        String vnfInstanceId1 = flowNamesToEbbList.get("AssignVnfBB").get(0).getWorkflowResourceIds().getVnfId();
        String vnfInstanceId2 = flowNamesToEbbList.get("AssignVnfBB").get(1).getWorkflowResourceIds().getVnfId();

        // should be 3 = 1 AssignVfModuleBB + 1 CreateVfModuleBB + 1 ActivateVfModuleBB
        boolean allEbbsForVfModule1HaveCorrectParentVnfId =
                3 == ebbs.stream().map(ExecuteBuildingBlock::getWorkflowResourceIds)
                        .filter(w -> "demo-network-1".equals(w.getVfModuleInstanceName())
                                && vnfInstanceId1.equals(w.getVnfId()))
                        .count();
        boolean allEbbsForVfModule2HaveCorrectParentVnfId = 3 == ebbs.stream()
                .map(ExecuteBuildingBlock::getWorkflowResourceIds)
                .filter(w -> "demo-1".equals(w.getVfModuleInstanceName()) && vnfInstanceId1.equals(w.getVnfId()))
                .count();
        boolean allEbbsForVfModule3HaveCorrectParentVnfId = 3 == ebbs.stream()
                .map(ExecuteBuildingBlock::getWorkflowResourceIds)
                .filter(w -> "demo-2".equals(w.getVfModuleInstanceName()) && vnfInstanceId2.equals(w.getVnfId()))
                .count();
        boolean allEbbsForVfModule4HaveCorrectParentVnfId = 3 == ebbs.stream()
                .map(ExecuteBuildingBlock::getWorkflowResourceIds)
                .filter(w -> "demo-3".equals(w.getVfModuleInstanceName()) && vnfInstanceId2.equals(w.getVnfId()))
                .count();
        assertTrue(allEbbsForVfModule1HaveCorrectParentVnfId);
        assertTrue(allEbbsForVfModule2HaveCorrectParentVnfId);
        assertTrue(allEbbsForVfModule3HaveCorrectParentVnfId);
        assertTrue(allEbbsForVfModule4HaveCorrectParentVnfId);

        boolean controllerExecutionBBsforVnf1HaveCorrectVnfId = flowNamesToEbbList.get("ControllerExecutionBB").stream()
                .filter(e -> vnfInstanceId1.equals(e.getWorkflowResourceIds().getVnfId()))
                .map(ExecuteBuildingBlock::getBuildingBlock).map(BuildingBlock::getBpmnAction)
                .collect(Collectors.toSet()).containsAll(Set.of("config-assign", "config-deploy"));
        assertTrue(controllerExecutionBBsforVnf1HaveCorrectVnfId);

        boolean controllerExecutionBBsforVnf2HaveCorrectVnfId = flowNamesToEbbList.get("ControllerExecutionBB").stream()
                .filter(e -> vnfInstanceId2.equals(e.getWorkflowResourceIds().getVnfId()))
                .map(ExecuteBuildingBlock::getBuildingBlock).map(BuildingBlock::getBpmnAction)
                .collect(Collectors.toSet()).containsAll(Set.of("config-assign", "config-deploy"));
        assertTrue(controllerExecutionBBsforVnf2HaveCorrectVnfId);
    }

    @Test
    public void selectExecutionListServiceMacroDeleteTest() throws Exception {
        String gAction = "deleteInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ACTIVATE_DELETE_UNASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("DeactivateVfModuleBB", "DeleteVfModuleBB",
                "DeactivateVolumeGroupBB", "DeleteVolumeGroupBB", "DeactivateVnfBB", "DeactivatePnfBB",
                "DeactivateNetworkBB", "DeleteNetworkBB", "DeleteNetworkCollectionBB", "DeactivateServiceInstanceBB",
                "UnassignVfModuleBB", "UnassignVolumeGroupBB", "UnassignVnfBB", "UnassignNetworkBB",
                "UnassignServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId("aaisi123");
        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
        org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf();
        org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf pnf =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf();
        vnf.setVnfId("vnfId123");
        pnf.setPnfId("pnfId123");

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule = buildVfModule();
        vnf.getVfModules().add(vfModule);
        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule2 = buildVfModule();
        vnf.getVfModules().add(vfModule2);

        org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup();
        volumeGroup.setVolumeGroupId("vg123");
        vnf.getVolumeGroups().add(volumeGroup);

        serviceInstanceMSO.getVnfs().add(vnf);
        serviceInstanceMSO.getPnfs().add(pnf);

        VfModule aaiVfModule = new VfModule();
        aaiVfModule.setIsBaseVfModule(false);

        doReturn(aaiVfModule).when(bbSetupUtils).getAAIVfModule(any(), any());
        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        doReturn(Mockito.mock(GenericVnf.class)).when(bbSetupUtils).getAAIGenericVnf(any());
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleBB", "DeactivateVfModuleBB",
                "DeleteVfModuleBB", "DeactivateVolumeGroupBB", "DeleteVolumeGroupBB", "DeactivateVnfBB",
                "DeactivatePnfBB", "DeactivateServiceInstanceBB", "UnassignVfModuleBB", "UnassignVfModuleBB",
                "UnassignVolumeGroupBB", "UnassignVnfBB", "UnassignServiceInstanceBB");
    }

    @Test
    public void selectExecutionListServiceMacroDeleteWithPnfTest() throws Exception {
        String gAction = "deleteInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ACTIVATE_DELETE_UNASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("DeactivateVfModuleBB", "DeleteVfModuleBB",
                "DeactivateVolumeGroupBB", "DeleteVolumeGroupBB", "DeactivateVnfBB", "DeactivatePnfBB",
                "DeactivateNetworkBB", "DeleteNetworkBB", "DeleteNetworkCollectionBB", "DeactivateServiceInstanceBB",
                "UnassignVfModuleBB", "UnassignVolumeGroupBB", "UnassignVnfBB", "UnassignNetworkBB",
                "UnassignServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId("aaisi123");
        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
        org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf pnf =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf();
        pnf.setPnfId("pnfId123");

        serviceInstanceMSO.getPnfs().add(pnf);

        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "DeactivatePnfBB", "DeactivateServiceInstanceBB", "UnassignServiceInstanceBB");
    }

    @Test
    public void selectExecutionListServiceMacroUnassignTest() throws Exception {
        String gAction = "unassignInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ACTIVATE_DELETE_UNASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("UnassignVfModuleBB", "UnassignVolumeGroupBB",
                "UnassignVnfBB", "UnassignNetworkBB", "UnassignServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId("aaisi123");
        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
        org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf();
        vnf.setVnfId("vnfId123");

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule = buildVfModule();
        vnf.getVfModules().add(vfModule);
        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule2 = buildVfModule();
        vnf.getVfModules().add(vfModule2);

        org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup();
        volumeGroup.setVolumeGroupId("vg123");
        vnf.getVolumeGroups().add(volumeGroup);

        serviceInstanceMSO.getVnfs().add(vnf);

        VfModule aaiVfModule = new VfModule();
        aaiVfModule.setIsBaseVfModule(false);

        doReturn(aaiVfModule).when(bbSetupUtils).getAAIVfModule(any(), any());
        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        doReturn(Mockito.mock(GenericVnf.class)).when(bbSetupUtils).getAAIGenericVnf(any());
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "UnassignVfModuleBB", "UnassignVfModuleBB", "UnassignVolumeGroupBB",
                "UnassignVnfBB", "UnassignServiceInstanceBB");
    }

    @Test
    public void selectExecutionListServiceMacroDeleteNetworkCollectionTest() throws Exception {
        String gAction = "deleteInstance";
        String resource = "Service";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ACTIVATE_DELETE_UNASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("DeactivateVfModuleBB", "DeleteVfModuleBB",
                "DeactivateVolumeGroupBB", "DeleteVolumeGroupBB", "DeactivateVnfBB", "DeactivateNetworkBB",
                "DeleteNetworkBB", "DeleteNetworkCollectionBB", "DeactivateServiceInstanceBB", "UnassignVfModuleBB",
                "UnassignVolumeGroupBB", "UnassignVnfBB", "UnassignNetworkBB", "UnassignServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId("aaisi123");
        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();

        org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network network =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network();
        network.setNetworkId("123");
        serviceInstanceMSO.getNetworks().add(network);
        org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network network2 =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network();
        network2.setNetworkId("321");
        serviceInstanceMSO.getNetworks().add(network2);

        Collection collection = new Collection();
        serviceInstanceMSO.setCollection(collection);

        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "DeactivateNetworkBB", "DeleteNetworkBB", "UnassignNetworkBB",
                "DeactivateNetworkBB", "DeleteNetworkBB", "UnassignNetworkBB", "DeleteNetworkCollectionBB",
                "DeactivateServiceInstanceBB", "UnassignServiceInstanceBB");
    }

    @Test
    public void selectExecutionListVnfMacroRecreateTest() throws Exception {
        String gAction = "recreateInstance";
        String resource = "Vnf";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_VNF_MACRO_REPLACE_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v7/serviceInstances/123/vnfs/1234/recreate");
        execution.setVariable("serviceInstanceId", "123");
        execution.setVariable("vnfId", "1234");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AAICheckVnfInMaintBB", "AAISetVnfInMaintBB",
                "DeactivateFabricConfigurationBB", "UnassignFabricConfigurationBB", "DeactivateVfModuleBB",
                "DeleteVfModuleBB", "DeactivateVnfBB", "CreateVfModuleBB", "ActivateVfModuleBB",
                "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB", "ActivateVnfBB", "SDNOVnfHealthCheckBB",
                "AAIUnsetVnfInMaintBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId("123");
        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
        org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf();
        vnf.setVnfId("1234");

        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModule1");
        vnf.getVfModules().add(vfModule);
        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule2 =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule2.setVfModuleId("vfModule2");
        vnf.getVfModules().add(vfModule2);

        serviceInstanceMSO.getVnfs().add(vnf);

        org.onap.aai.domain.yang.VfModule aaiVfModule = new org.onap.aai.domain.yang.VfModule();
        aaiVfModule.setIsBaseVfModule(false);

        doReturn(aaiVfModule).when(bbSetupUtils).getAAIVfModule(any(), any());
        doReturn(new org.onap.aai.domain.yang.GenericVnf()).when(bbSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AAICheckVnfInMaintBB", "AAISetVnfInMaintBB", "DeactivateVfModuleBB",
                "DeleteVfModuleBB", "DeactivateVfModuleBB", "DeleteVfModuleBB", "DeactivateVnfBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB", "ActivateVnfBB", "SDNOVnfHealthCheckBB",
                "AAIUnsetVnfInMaintBB");
    }

    @Test
    public void selectExecutionListVnfMacroReplaceTest() throws Exception {
        String gAction = "replaceInstance";
        String resource = "Vnf";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_VNF_MACRO_REPLACE_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v7/serviceInstances/123/vnfs/1234/replace");
        execution.setVariable("serviceInstanceId", "123");
        execution.setVariable("vnfId", "1234");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AAICheckVnfInMaintBB", "AAISetVnfInMaintBB",
                "DeactivateFabricConfigurationBB", "UnassignFabricConfigurationBB", "DeactivateVfModuleBB",
                "DeleteVfModuleBB", "DeactivateVnfBB", "ChangeModelVfModuleBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB",
                "ChangeModelVnfBB", "ActivateVnfBB", "ChangeModelServiceInstanceBB", "SDNOVnfHealthCheckBB",
                "AAIUnsetVnfInMaintBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId("123");
        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
        org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf();
        vnf.setVnfId("1234");
        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModule1");
        vnf.getVfModules().add(vfModule);
        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule2 =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule2.setVfModuleId("vfModule2");
        vnf.getVfModules().add(vfModule2);
        serviceInstanceMSO.getVnfs().add(vnf);
        VfModule vfModuleAAI1 = new VfModule();
        vfModuleAAI1.setIsBaseVfModule(false);
        VfModule vfModuleAAI2 = new VfModule();
        vfModuleAAI2.setIsBaseVfModule(false);
        vfModuleAAI2.setVfModuleId("vfModule2");
        RelationshipList relationshipList = new RelationshipList();
        Relationship relationship = new Relationship();
        relationshipList.getRelationship().add(relationship);
        vfModuleAAI2.setRelationshipList(relationshipList);
        Relationships relationships = new Relationships("abc");
        Configuration config = new Configuration();
        config.setConfigurationId("configId");
        Optional<Configuration> configOp = Optional.of(config);
        Optional<Relationships> relationshipsOp = Optional.of(relationships);

        doReturn(new org.onap.aai.domain.yang.GenericVnf()).when(bbSetupUtils).getAAIGenericVnf(vnf.getVnfId());
        doReturn(relationshipsOp).when(workflowActionUtils).extractRelationshipsVnfc(isA(Relationships.class));
        doReturn(configOp).when(workflowActionUtils).extractRelationshipsConfiguration(isA(Relationships.class));
        doReturn(vfModuleAAI1).when(bbSetupUtils).getAAIVfModule("1234", "vfModule1");
        doReturn(vfModuleAAI2).when(bbSetupUtils).getAAIVfModule("1234", "vfModule2");
        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AAICheckVnfInMaintBB", "AAISetVnfInMaintBB", "DeactivateVfModuleBB",
                "DeleteVfModuleBB", "DeactivateFabricConfigurationBB", "UnassignFabricConfigurationBB",
                "DeactivateVfModuleBB", "DeleteVfModuleBB", "DeactivateVnfBB", "ChangeModelVfModuleBB",
                "CreateVfModuleBB", "ActivateVfModuleBB", "ChangeModelVfModuleBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB",
                "ChangeModelVnfBB", "ActivateVnfBB", "ChangeModelServiceInstanceBB", "SDNOVnfHealthCheckBB",
                "AAIUnsetVnfInMaintBB");
        for (ExecuteBuildingBlock executeBuildingBlock : ebbs) {
            assertEquals("123", executeBuildingBlock.getWorkflowResourceIds().getServiceInstanceId());
        }
    }

    @Ignore
    @Test
    public void selectExecutionListNetworkCollectionMacroCreate() throws Exception {
        String gAction = "createInstance";
        String resource = "NetworkCollection";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_CREATE_NETWORK_COLLECTION_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123/networkCollections/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("CreateNetworkCollectionBB", "AssignNetworkBB",
                "CreateNetworkBB", "ActivateNetworkBB", "ActivateNetworkCollectionBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        Service service = new Service();
        CollectionResourceCustomization collectionResourceCustomization = new CollectionResourceCustomization();
        CollectionResource collectionResource = new CollectionResource();
        collectionResource.setToscaNodeType("Data NetworkCollection Data");
        collectionResourceCustomization.setCollectionResource(collectionResource);
        service.getCollectionResourceCustomizations().add(collectionResourceCustomization);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "CreateNetworkCollectionBB", "AssignNetworkBB", "CreateNetworkBB",
                "ActivateNetworkBB", "AssignNetworkBB", "CreateNetworkBB", "ActivateNetworkBB",
                "ActivateNetworkCollectionBB");
    }

    @Ignore
    @Test
    public void selectExecutionListNetworkCollectionMacroDelete() throws Exception {
        String gAction = "deleteInstance";
        String resource = "NetworkCollection";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_CREATE_NETWORK_COLLECTION_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123/networkCollections/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("DeactivateNetworkBB", "DeleteNetworkBB",
                "UnassignNetworkBB", "DeactivateNetworkCollectionBB", "DeleteNetworkCollectionBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        Service service = new Service();
        CollectionResourceCustomization collectionResourceCustomization = new CollectionResourceCustomization();
        CollectionResource collectionResource = new CollectionResource();
        collectionResource.setToscaNodeType("Data NetworkCollection Data");
        collectionResourceCustomization.setCollectionResource(collectionResource);
        service.getCollectionResourceCustomizations().add(collectionResourceCustomization);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "DeactivateNetworkBB", "DeleteNetworkBB", "UnassignNetworkBB",
                "DeactivateNetworkBB", "DeleteNetworkBB", "UnassignNetworkBB", "DeactivateNetworkCollectionBB",
                "DeleteNetworkCollectionBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleNoFabricCreateTest() throws Exception {
        String gAction = "createInstance";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB",
                "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleFabricCreateTest() throws Exception {
        String gAction = "createInstance";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB",
                "AddFabricConfigurationBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        List<CvnfcCustomization> cvnfcCustomizations = new ArrayList<CvnfcCustomization>();
        CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
        CvnfcConfigurationCustomization vnfVfmoduleCvnfcConfigurationCustomization =
                new CvnfcConfigurationCustomization();
        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setToscaNodeType("FabricConfiguration");
        vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationResource(configurationResource);
        vnfVfmoduleCvnfcConfigurationCustomization.setModelInstanceName("modelInstanceName1");
        vnfVfmoduleCvnfcConfigurationCustomization.setCvnfcCustomization(cvnfcCustomization);
        List<CvnfcConfigurationCustomization> custSet = new ArrayList<CvnfcConfigurationCustomization>();
        custSet.add(vnfVfmoduleCvnfcConfigurationCustomization);
        cvnfcCustomization.setCvnfcConfigurationCustomization(custSet);
        cvnfcCustomization.setDescription("description");
        cvnfcCustomizations.add(cvnfcCustomization);

        CvnfcCustomization cvnfcCustomization2 = new CvnfcCustomization();
        CvnfcConfigurationCustomization vnfVfmoduleCvnfcConfigurationCustomization2 =
                new CvnfcConfigurationCustomization();
        ConfigurationResource configurationResource2 = new ConfigurationResource();
        configurationResource2.setToscaNodeType("FabricConfiguration");
        vnfVfmoduleCvnfcConfigurationCustomization2.setConfigurationResource(configurationResource2);
        vnfVfmoduleCvnfcConfigurationCustomization2.setModelInstanceName("modelInstanceName2");
        vnfVfmoduleCvnfcConfigurationCustomization2.setCvnfcCustomization(cvnfcCustomization2);
        List<CvnfcConfigurationCustomization> custSet2 = new ArrayList<CvnfcConfigurationCustomization>();
        custSet2.add(vnfVfmoduleCvnfcConfigurationCustomization2);
        cvnfcCustomization2.setCvnfcConfigurationCustomization(custSet2);
        cvnfcCustomization2.setDescription("description2");
        cvnfcCustomizations.add(cvnfcCustomization2);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);

        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB");
    }



    @Test
    public void selectExecutionListALaCarteVfModuleNoVolumeGroupReplaceTest() throws Exception {
        String gAction = "replaceInstance";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules/1234");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setOrchestrationFlowList(replaceVfModuleOrchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleATTBB", "UnassignVFModuleBB",
                "AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB", "ChangeModelVnfBB",
                "ChangeModelServiceInstanceBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleNoVolumeGroupReplaceRetainAssignmentsTest() throws Exception {
        String gAction = "replaceInstanceRetainAssignments";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules/1234");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setOrchestrationFlowList(replaceRetainAssignmentsVfModuleOrchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleATTBB", "ChangeModelVfModuleBB",
                "CreateVfModuleBB", "ActivateVfModuleBB", "ChangeModelVnfBB", "ChangeModelServiceInstanceBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleVolumeGroupToNoVolumeGroupReplaceTest() throws Exception {
        String gAction = "replaceInstance";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules/1234");
        execution.setVariable("vnfId", "b80b16a5-f80d-4ffa-91c8-bd47c7438a3d");
        execution.setVariable("vfModuleId", "1234");

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        doReturn(Optional.of(volumeGroup)).when(bbSetupUtils)
                .getRelatedVolumeGroupFromVfModule("b80b16a5-f80d-4ffa-91c8-bd47c7438a3d", "1234");
        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setOrchestrationFlowList(replaceVfModuleOrchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleATTBB", "DeactivateVolumeGroupBB",
                "DeleteVolumeGroupBB", "UnassignVFModuleBB", "UnassignVolumeGroupBB", "AssignVfModuleBB",
                "CreateVfModuleBB", "ActivateVfModuleBB", "ChangeModelVnfBB", "ChangeModelServiceInstanceBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleVolumeGroupToNoVolumeGroupReplaceRetainAssignmentsTest()
            throws Exception {
        String gAction = "replaceInstanceRetainAssignments";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules/1234");
        execution.setVariable("vnfId", "b80b16a5-f80d-4ffa-91c8-bd47c7438a3d");
        execution.setVariable("vfModuleId", "1234");

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        doReturn(Optional.of(volumeGroup)).when(bbSetupUtils)
                .getRelatedVolumeGroupFromVfModule("b80b16a5-f80d-4ffa-91c8-bd47c7438a3d", "1234");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setOrchestrationFlowList(replaceRetainAssignmentsVfModuleOrchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleATTBB", "DeactivateVolumeGroupBB",
                "DeleteVolumeGroupBB", "UnassignVolumeGroupBB", "ChangeModelVfModuleBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "ChangeModelVnfBB", "ChangeModelServiceInstanceBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleKeepVolumeGroupReplaceTest() throws Exception {
        String gAction = "replaceInstance";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules/1234");
        execution.setVariable("vnfId", "b80b16a5-f80d-4ffa-91c8-bd47c7438a3d");
        execution.setVariable("vfModuleId", "1234");

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        doReturn(Optional.of(volumeGroup)).when(bbSetupUtils)
                .getRelatedVolumeGroupFromVfModule("b80b16a5-f80d-4ffa-91c8-bd47c7438a3d", "1234");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setVolumeHeatEnv(new HeatEnvironment());
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        vfModule.setVolumeHeatTemplate(new HeatTemplate());
        vfModuleCustomization.setVfModule(vfModule);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("9a6d01fd-19a7-490a-9800-460830a12e0b"))
                .thenReturn(vfModuleCustomization);

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setOrchestrationFlowList(replaceVfModuleOrchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleATTBB", "UnassignVFModuleBB",
                "AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB", "ChangeModelVnfBB",
                "ChangeModelServiceInstanceBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleWithFabricKeepVolumeGroupReplaceTest() throws Exception {
        String gAction = "replaceInstance";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules/1234");
        execution.setVariable("vnfId", "b80b16a5-f80d-4ffa-91c8-bd47c7438a3d");
        execution.setVariable("vfModuleId", "1234");

        org.onap.aai.domain.yang.GenericVnf vnf = new org.onap.aai.domain.yang.GenericVnf();
        vnf.setVnfId("b80b16a5-f80d-4ffa-91c8-bd47c7438a3d");
        vnf.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIGenericVnf(any())).thenReturn(vnf);

        org.onap.aai.domain.yang.VfModule vfModuleFromAAI = new org.onap.aai.domain.yang.VfModule();
        vfModuleFromAAI.setModelCustomizationId("modelCustomizationId");
        vfModuleFromAAI.setVfModuleId("1234");
        when(bbSetupUtils.getAAIVfModule(any(), any())).thenReturn(vfModuleFromAAI);

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        doReturn(Optional.of(volumeGroup)).when(bbSetupUtils)
                .getRelatedVolumeGroupFromVfModule("b80b16a5-f80d-4ffa-91c8-bd47c7438a3d", "1234");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setVolumeHeatEnv(new HeatEnvironment());
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        vfModule.setVolumeHeatTemplate(new HeatTemplate());
        vfModuleCustomization.setVfModule(vfModule);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("9a6d01fd-19a7-490a-9800-460830a12e0b"))
                .thenReturn(vfModuleCustomization);
        List<org.onap.aai.domain.yang.Vnfc> vnfcs = new ArrayList<org.onap.aai.domain.yang.Vnfc>();
        org.onap.aai.domain.yang.Vnfc vnfc = new org.onap.aai.domain.yang.Vnfc();
        vnfc.setModelInvariantId("modelInvariantId");
        vnfc.setVnfcName("testVnfcName");
        vnfcs.add(vnfc);
        doReturn(vnfcs).when(SPY_workflowAction).getRelatedResourcesInVfModule(any(), any(), any(), any());

        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("configurationId");
        configuration.setModelCustomizationId("modelCustimizationId");
        configuration.setConfigurationName("testConfigurationName");
        doReturn(configuration).when(SPY_workflowAction).getRelatedResourcesInVnfc(any(), any(), any());

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setOrchestrationFlowList(replaceVfModuleWithFabricOrchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        SPY_workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        assertEqualsBulkFlowName(ebbs, "DeleteFabricConfigurationBB", "DeactivateVfModuleBB", "DeleteVfModuleATTBB",
                "UnassignVFModuleBB", "AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB", "ChangeModelVnfBB",
                "ChangeModelServiceInstanceBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleKeepVolumeGroupReplaceRetainAssignmentsTest() throws Exception {
        String gAction = "replaceInstanceRetainAssignments";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules/1234");
        execution.setVariable("vnfId", "b80b16a5-f80d-4ffa-91c8-bd47c7438a3d");
        execution.setVariable("vfModuleId", "1234");

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        doReturn(Optional.of(volumeGroup)).when(bbSetupUtils)
                .getRelatedVolumeGroupFromVfModule("b80b16a5-f80d-4ffa-91c8-bd47c7438a3d", "1234");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setVolumeHeatEnv(new HeatEnvironment());
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        vfModule.setVolumeHeatTemplate(new HeatTemplate());
        vfModuleCustomization.setVfModule(vfModule);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("9a6d01fd-19a7-490a-9800-460830a12e0b"))
                .thenReturn(vfModuleCustomization);

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setOrchestrationFlowList(replaceRetainAssignmentsVfModuleOrchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleATTBB", "ChangeModelVfModuleBB",
                "CreateVfModuleBB", "ActivateVfModuleBB", "ChangeModelVnfBB", "ChangeModelServiceInstanceBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleNoVolumeGroupToVolumeGroupReplaceTest() throws Exception {
        String gAction = "replaceInstance";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules/1234");
        execution.setVariable("vnfId", "b80b16a5-f80d-4ffa-91c8-bd47c7438a3d");
        execution.setVariable("vfModuleId", "1234");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setVolumeHeatEnv(new HeatEnvironment());
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        vfModule.setVolumeHeatTemplate(new HeatTemplate());
        vfModuleCustomization.setVfModule(vfModule);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("9a6d01fd-19a7-490a-9800-460830a12e0b"))
                .thenReturn(vfModuleCustomization);

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setOrchestrationFlowList(replaceVfModuleOrchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleATTBB", "UnassignVFModuleBB",
                "AssignVolumeGroupBB", "AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB",
                "CreateVolumeGroupBB", "ActivateVolumeGroupBB", "ChangeModelVnfBB", "ChangeModelServiceInstanceBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleNoVolumeGroupToVolumeGroupReplaceRetainAssignmentsTest()
            throws Exception {
        String gAction = "replaceInstanceRetainAssignments";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules/1234");
        execution.setVariable("vnfId", "b80b16a5-f80d-4ffa-91c8-bd47c7438a3d");
        execution.setVariable("vfModuleId", "1234");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setVolumeHeatEnv(new HeatEnvironment());
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        vfModule.setVolumeHeatTemplate(new HeatTemplate());
        vfModuleCustomization.setVfModule(vfModule);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("9a6d01fd-19a7-490a-9800-460830a12e0b"))
                .thenReturn(vfModuleCustomization);

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setOrchestrationFlowList(replaceRetainAssignmentsVfModuleOrchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleATTBB", "AssignVolumeGroupBB",
                "ChangeModelVfModuleBB", "CreateVolumeGroupBB", "ActivateVolumeGroupBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "ChangeModelVnfBB", "ChangeModelServiceInstanceBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleRebuildVolumeGroupReplaceTest() throws Exception {
        String gAction = "replaceInstance";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_REPLACE_REBUILD_VOLUME_GROUPS_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules/1234");
        execution.setVariable("vnfId", "b80b16a5-f80d-4ffa-91c8-bd47c7438a3d");
        execution.setVariable("vfModuleId", "1234");

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        doReturn(Optional.of(volumeGroup)).when(bbSetupUtils)
                .getRelatedVolumeGroupFromVfModule("b80b16a5-f80d-4ffa-91c8-bd47c7438a3d", "1234");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setVolumeHeatEnv(new HeatEnvironment());
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        vfModule.setVolumeHeatTemplate(new HeatTemplate());
        vfModuleCustomization.setVfModule(vfModule);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("9a6d01fd-19a7-490a-9800-460830a12e0b"))
                .thenReturn(vfModuleCustomization);

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setOrchestrationFlowList(replaceVfModuleOrchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleATTBB", "DeactivateVolumeGroupBB",
                "DeleteVolumeGroupBB", "UnassignVFModuleBB", "UnassignVolumeGroupBB", "AssignVolumeGroupBB",
                "AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB", "CreateVolumeGroupBB",
                "ActivateVolumeGroupBB", "ChangeModelVnfBB", "ChangeModelServiceInstanceBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleRebuildVolumeGroupReplaceRetainAssignmentsTest() throws Exception {
        String gAction = "replaceInstanceRetainAssignments";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_REPLACE_REBUILD_VOLUME_GROUPS_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules/1234");
        execution.setVariable("vnfId", "b80b16a5-f80d-4ffa-91c8-bd47c7438a3d");
        execution.setVariable("vfModuleId", "1234");

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId");
        doReturn(Optional.of(volumeGroup)).when(bbSetupUtils)
                .getRelatedVolumeGroupFromVfModule("b80b16a5-f80d-4ffa-91c8-bd47c7438a3d", "1234");

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setVolumeHeatEnv(new HeatEnvironment());
        org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
        vfModule.setVolumeHeatTemplate(new HeatTemplate());
        vfModuleCustomization.setVfModule(vfModule);
        when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("9a6d01fd-19a7-490a-9800-460830a12e0b"))
                .thenReturn(vfModuleCustomization);

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        northBoundRequest.setOrchestrationFlowList(replaceRetainAssignmentsVfModuleOrchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");

        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleATTBB", "DeactivateVolumeGroupBB",
                "DeleteVolumeGroupBB", "UnassignVolumeGroupBB", "AssignVolumeGroupBB", "ChangeModelVfModuleBB",
                "CreateVolumeGroupBB", "ActivateVolumeGroupBB", "CreateVfModuleBB", "ActivateVfModuleBB",
                "ChangeModelVnfBB", "ChangeModelServiceInstanceBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleFabricDeleteTest() throws Exception {
        String gAction = "deleteInstance";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("DeactivateVfModuleBB", "DeleteVfModuleBB",
                "UnassignVfModuleBB", "DeactivateFabricConfigurationBB", "UnassignFabricConfigurationBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);

        org.onap.aai.domain.yang.GenericVnf vnf = new org.onap.aai.domain.yang.GenericVnf();
        vnf.setVnfId("vnf0");
        vnf.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIGenericVnf(any())).thenReturn(vnf);

        org.onap.aai.domain.yang.VfModule vfModule = new org.onap.aai.domain.yang.VfModule();
        vfModule.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIVfModule(any(), any())).thenReturn(vfModule);

        List<org.onap.aai.domain.yang.Vnfc> vnfcs = new ArrayList<org.onap.aai.domain.yang.Vnfc>();
        org.onap.aai.domain.yang.Vnfc vnfc = new org.onap.aai.domain.yang.Vnfc();
        vnfc.setModelInvariantId("modelInvariantId");
        vnfc.setVnfcName("testVnfcName");
        vnfcs.add(vnfc);
        doReturn(vnfcs).when(SPY_workflowAction).getRelatedResourcesInVfModule(any(), any(), any(),
                any());

        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("configurationId");
        configuration.setModelCustomizationId("modelCustimizationId");
        configuration.setConfigurationName("testConfigurationName");
        doReturn(configuration).when(SPY_workflowAction).getRelatedResourcesInVnfc(any(), any(),
                any());

        SPY_workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "DeactivateFabricConfigurationBB", "UnassignFabricConfigurationBB",
                "DeactivateVfModuleBB", "DeleteVfModuleBB", "UnassignVfModuleBB");
    }

    @Test
    public void selectExecutionListALaCarteNoRequestParametersTest() throws Exception {
        String gAction = "createInstance";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_NO_PARAMS_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB",
                "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB");
    }

    @Test
    public void getConfigBuildingBlocksNoVfModuleFabricDeleteTest() throws Exception {
        String gAction = "deleteInstance";
        ObjectMapper mapper = new ObjectMapper();
        WorkflowType resourceType = WorkflowType.VFMODULE;
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("vnfId", "1234");
        execution.setVariable("vfModuleId", "vfModuleId1234");
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");
        ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);
        RequestDetails requestDetails = sIRequest.getRequestDetails();
        String requestAction = "deleteInstance";
        String requestId = "9c944122-d161-4280-8594-48c06a9d96d5";
        boolean aLaCarte = true;
        String apiVersion = "7";
        String vnfType = "vnfType";
        String key = "00d15ebb-c80e-43c1-80f0-90c40dde70b0";
        String resourceId = "d1d35800-783d-42d3-82f6-d654c5054a6e";
        Resource resourceKey = new Resource(resourceType, key, aLaCarte, null);
        WorkflowResourceIds workflowResourceIds = SPY_workflowAction.populateResourceIdsFromApiHandler(execution);

        thrown.expect(AAIEntityNotFoundException.class);
        thrown.expectMessage(containsString(
                "No matching VfModule is found in Generic-Vnf in AAI for vnfId: 1234 and vfModuleId : vfModuleId1234"));

        List<OrchestrationFlow> orchFlows = createFlowList("DeactivateVfModuleBB", "DeleteVfModuleBB",
                "UnassignVfModuleBB", "DeactivateFabricConfigurationBB", "UnassignFabricConfigurationBB");

        ConfigBuildingBlocksDataObject dataObj = new ConfigBuildingBlocksDataObject().setsIRequest(sIRequest)
                .setOrchFlows(orchFlows).setRequestId(requestId).setResourceKey(resourceKey).setApiVersion(apiVersion)
                .setResourceId(resourceId).setRequestAction(requestAction).setaLaCarte(aLaCarte).setVnfType(vnfType)
                .setWorkflowResourceIds(workflowResourceIds).setRequestDetails(requestDetails).setExecution(execution);

        org.onap.aai.domain.yang.GenericVnf vnf = new org.onap.aai.domain.yang.GenericVnf();
        vnf.setVnfId("vnf0");
        vnf.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIGenericVnf(any())).thenReturn(vnf);

        org.onap.aai.domain.yang.VfModule vfModule = new org.onap.aai.domain.yang.VfModule();
        vfModule.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIVfModule(any(), any())).thenReturn(null);

        SPY_workflowAction.getConfigBuildingBlocks(dataObj);
    }

    @Test
    public void getConfigBuildingBlocksTest() throws Exception {
        String gAction = "deleteInstance";
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);

        WorkflowType resourceType = WorkflowType.VFMODULE;
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("vnfId", "1234");
        execution.setVariable("vfModuleId", "vfModuleId1234");
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");
        ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);
        RequestDetails requestDetails = sIRequest.getRequestDetails();
        String requestAction = "deleteInstance";
        String requestId = "9c944122-d161-4280-8594-48c06a9d96d5";
        boolean aLaCarte = true;
        String apiVersion = "7";
        String vnfType = "vnfType";
        String key = "00d15ebb-c80e-43c1-80f0-90c40dde70b0";
        String resourceId = "d1d35800-783d-42d3-82f6-d654c5054a6e";
        Resource resourceKey = new Resource(resourceType, key, aLaCarte, null);
        WorkflowResourceIds workflowResourceIds = SPY_workflowAction.populateResourceIdsFromApiHandler(execution);

        List<OrchestrationFlow> orchFlows = createFlowList("DeactivateVfModuleBB", "DeleteVfModuleBB",
                "UnassignVfModuleBB", "DeleteFabricConfigurationBB");

        ConfigBuildingBlocksDataObject dataObj = new ConfigBuildingBlocksDataObject().setsIRequest(sIRequest)
                .setOrchFlows(orchFlows).setRequestId(requestId).setResourceKey(resourceKey).setApiVersion(apiVersion)
                .setResourceId(resourceId).setRequestAction(requestAction).setaLaCarte(aLaCarte).setVnfType(vnfType)
                .setWorkflowResourceIds(workflowResourceIds).setRequestDetails(requestDetails).setExecution(execution);

        org.onap.aai.domain.yang.GenericVnf vnf = new org.onap.aai.domain.yang.GenericVnf();
        vnf.setVnfId("vnf0");
        vnf.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIGenericVnf(any())).thenReturn(vnf);

        org.onap.aai.domain.yang.VfModule vfModule = new org.onap.aai.domain.yang.VfModule();
        vfModule.setModelCustomizationId("modelCustomizationId");

        org.onap.aai.domain.yang.Configuration config1 = new org.onap.aai.domain.yang.Configuration();
        config1.setConfigurationId("config1");
        org.onap.aai.domain.yang.Configuration config2 = new org.onap.aai.domain.yang.Configuration();
        config2.setConfigurationId("config2");

        List<org.onap.aai.domain.yang.Vnfc> vnfcs = new ArrayList<org.onap.aai.domain.yang.Vnfc>();
        org.onap.aai.domain.yang.Vnfc vnfc1 = new org.onap.aai.domain.yang.Vnfc();
        vnfc1.setVnfcName("zauk53avetd02svm001");
        org.onap.aai.domain.yang.Vnfc vnfc2 = new org.onap.aai.domain.yang.Vnfc();
        vnfc2.setVnfcName("zauk53avetd02tvm001");
        vnfcs.add(vnfc1);
        vnfcs.add(vnfc2);

        when(bbSetupUtils.getAAIVfModule(any(), any())).thenReturn(vfModule);
        doReturn(vnfcs).when(SPY_workflowAction).getRelatedResourcesInVfModule(any(), any(),
                eq(org.onap.aai.domain.yang.Vnfc.class), eq(Types.VNFC));
        doReturn(config1).when(SPY_workflowAction).getRelatedResourcesInVnfc(eq(vnfc1),
                eq(org.onap.aai.domain.yang.Configuration.class), eq(Types.CONFIGURATION));
        doReturn(config2).when(SPY_workflowAction).getRelatedResourcesInVnfc(eq(vnfc2),
                eq(org.onap.aai.domain.yang.Configuration.class), eq(Types.CONFIGURATION));

        List<ExecuteBuildingBlock> results = SPY_workflowAction.getConfigBuildingBlocks(dataObj);

        assertFalse(results.isEmpty());
        assertEquals(2, results.size());
        assertEquals("config1", results.get(0).getWorkflowResourceIds().getConfigurationId());
        assertEquals("config2", results.get(1).getWorkflowResourceIds().getConfigurationId());
        assertEquals("zauk53avetd02svm001", results.get(0).getConfigurationResourceKeys().getVnfcName());
        assertEquals("zauk53avetd02tvm001", results.get(1).getConfigurationResourceKeys().getVnfcName());
    }

    @Test
    public void getConfigBuildingBlocksNullConfigurationTest() throws Exception {
        String gAction = "deleteInstance";
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);

        WorkflowType resourceType = WorkflowType.VFMODULE;
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("vnfId", "1234");
        execution.setVariable("vfModuleId", "vfModuleId1234");
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");
        ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);
        RequestDetails requestDetails = sIRequest.getRequestDetails();
        String requestAction = "deleteInstance";
        String requestId = "9c944122-d161-4280-8594-48c06a9d96d5";
        boolean aLaCarte = true;
        String apiVersion = "7";
        String vnfType = "vnfType";
        String key = "00d15ebb-c80e-43c1-80f0-90c40dde70b0";
        String resourceId = "d1d35800-783d-42d3-82f6-d654c5054a6e";
        Resource resourceKey = new Resource(resourceType, key, aLaCarte, null);
        WorkflowResourceIds workflowResourceIds = SPY_workflowAction.populateResourceIdsFromApiHandler(execution);

        List<OrchestrationFlow> orchFlows = createFlowList("DeactivateVfModuleBB", "DeleteVfModuleBB",
                "UnassignVfModuleBB", "DeleteFabricConfigurationBB");

        ConfigBuildingBlocksDataObject dataObj = new ConfigBuildingBlocksDataObject().setsIRequest(sIRequest)
                .setOrchFlows(orchFlows).setRequestId(requestId).setResourceKey(resourceKey).setApiVersion(apiVersion)
                .setResourceId(resourceId).setRequestAction(requestAction).setaLaCarte(aLaCarte).setVnfType(vnfType)
                .setWorkflowResourceIds(workflowResourceIds).setRequestDetails(requestDetails).setExecution(execution);

        org.onap.aai.domain.yang.GenericVnf vnf = new org.onap.aai.domain.yang.GenericVnf();
        vnf.setVnfId("vnf0");
        vnf.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIGenericVnf(any())).thenReturn(vnf);

        org.onap.aai.domain.yang.VfModule vfModule = new org.onap.aai.domain.yang.VfModule();
        vfModule.setModelCustomizationId("modelCustomizationId");

        /* this is a test case where configuration for vnfc is null */
        org.onap.aai.domain.yang.Configuration config1 = null;
        org.onap.aai.domain.yang.Configuration config2 = new org.onap.aai.domain.yang.Configuration();
        config2.setConfigurationId("config2");

        List<org.onap.aai.domain.yang.Vnfc> vnfcs = new ArrayList<org.onap.aai.domain.yang.Vnfc>();
        org.onap.aai.domain.yang.Vnfc vnfc1 = new org.onap.aai.domain.yang.Vnfc();
        vnfc1.setVnfcName("zauk53avetd02svm001");
        org.onap.aai.domain.yang.Vnfc vnfc2 = new org.onap.aai.domain.yang.Vnfc();
        vnfc2.setVnfcName("zauk53avetd02tvm001");
        vnfcs.add(vnfc1);
        vnfcs.add(vnfc2);

        when(bbSetupUtils.getAAIVfModule(any(), any())).thenReturn(vfModule);
        doReturn(vnfcs).when(SPY_workflowAction).getRelatedResourcesInVfModule(any(), any(),
                eq(org.onap.aai.domain.yang.Vnfc.class), eq(Types.VNFC));
        doReturn(config1).when(SPY_workflowAction).getRelatedResourcesInVnfc(eq(vnfc1),
                eq(org.onap.aai.domain.yang.Configuration.class), eq(Types.CONFIGURATION));
        doReturn(config2).when(SPY_workflowAction).getRelatedResourcesInVnfc(eq(vnfc2),
                eq(org.onap.aai.domain.yang.Configuration.class), eq(Types.CONFIGURATION));

        List<ExecuteBuildingBlock> results = SPY_workflowAction.getConfigBuildingBlocks(dataObj);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("config2", results.get(0).getWorkflowResourceIds().getConfigurationId());
        assertEquals("zauk53avetd02tvm001", results.get(0).getConfigurationResourceKeys().getVnfcName());
    }

    @Test
    public void selectExecutionListALaCarteVfModuleNoFabricDeleteTest() throws Exception {
        String gAction = "deleteInstance";
        String resource = "VfModule";
        String bpmnRequest = readBpmnRequestFromFile(VF_MODULE_CREATE_WITH_FABRIC_JSON);
        initExecution(gAction, bpmnRequest, true);
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("DeactivateVfModuleBB", "DeleteVfModuleBB",
                "UnassignVfModuleBB", "DeleteFabricConfigurationBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);

        org.onap.aai.domain.yang.GenericVnf vnf = new org.onap.aai.domain.yang.GenericVnf();
        vnf.setVnfId("vnf0");
        vnf.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIGenericVnf(any())).thenReturn(vnf);

        org.onap.aai.domain.yang.VfModule vfModule = new org.onap.aai.domain.yang.VfModule();
        vfModule.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIVfModule(any(), any())).thenReturn(vfModule);

        List<org.onap.aai.domain.yang.Vnfc> vnfcs = new ArrayList<org.onap.aai.domain.yang.Vnfc>();

        doReturn(vnfcs).when(SPY_workflowAction).getRelatedResourcesInVfModule(any(), any(), any(),
                any());

        SPY_workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleBB", "UnassignVfModuleBB");
    }

    @Test
    public void selectExecutionListMacroResumeTest() throws Exception {
        String gAction = "createInstance";
        String bpmnRequest = readBpmnRequestFromFile(MACRO_ASSIGN_JSON);
        initExecution(gAction, bpmnRequest, false);
        execution.setVariable("requestUri", "v6/serviceInstances/123");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        Service service = new Service();
        service.setModelUUID("3c40d244-808e-42ca-b09a-256d83d19d0a");

        ServiceInstance si = new ServiceInstance();

        when(bbSetupUtils.getAAIServiceInstanceById("123")).thenReturn(si);
        when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);

        String flowsToExecuteString =
                "[{\"buildingBlock\":{\"mso-id\":\"2f9ddc4b-4dcf-4129-a35f-be1625ae0176\",\"bpmn-flow-name\":\"AssignServiceInstanceBB\",\"key\":\"7adc7c29-21a3-40a2-b8b6-5d4ad08b68e9\",\"is-virtual-link\":false,\"virtual-link-key\":null},\"requestId\":\"9c944122-d161-4280-8594-48c06a9d96d5\",\"apiVersion\":\"7\",\"resourceId\":\"d1d35800-783d-42d3-82f6-d654c5054a6e\",\"requestAction\":\"deleteInstance\",\"vnfType\":\"\",\"aLaCarte\":true,\"homing\":false,\"workflowResourceIds\":{\"serviceInstanceId\":\"ff9dae72-05bb-4277-ad2b-1b082467c138\",\"vnfId\":\"84a29830-e533-4f20-a838-910c740bf24c\",\"networkId\":\"\",\"volumeGroupId\":\"\",\"vfModuleId\":\"d1d35800-783d-42d3-82f6-d654c5054a6e\",\"networkCollectionId\":null,\"configurationId\":\"10f8a3a3-91bf-4821-9515-c01b2864dff0\",\"instanceGroupId\":\"\"},\"requestDetails\":{\"modelInfo\":{\"modelCustomizationName\":\"McmrNcUpVnf20191..cr_mccm_fc_base..module-0\",\"modelInvariantId\":\"8028fcc0-96dc-427d-a4de-4536245943da\",\"modelType\":\"vfModule\",\"modelId\":\"00d15ebb-c80e-43c1-80f0-90c40dde70b0\",\"modelName\":\"McmrNcUpVnf20191..cr_mccm_fc_base..module-0\",\"modelVersion\":\"1\",\"modelCustomizationUuid\":\"7adc7c29-21a3-40a2-b8b6-5d4ad08b68e9\",\"modelVersionId\":\"00d15ebb-c80e-43c1-80f0-90c40dde70b0\",\"modelCustomizationId\":\"7adc7c29-21a3-40a2-b8b6-5d4ad08b68e9\",\"modelUuid\":\"00d15ebb-c80e-43c1-80f0-90c40dde70b0\",\"modelInvariantUuid\":\"8028fcc0-96dc-427d-a4de-4536245943da\",\"modelInstanceName\":\"McmrNcUpVnf20191..cr_mccm_fc_base..module-0\"},\"requestInfo\":{\"source\":\"VID\",\"suppressRollback\":false,\"requestorId\":\"pj8646\"},\"cloudConfiguration\":{\"tenantId\":\"e2a6af59d1cb43b2874e943bbbf8470a\",\"cloudOwner\":\"att-nc\",\"lcpCloudRegionId\":\"auk51b\"},\"requestParameters\":{\"testApi\":\"GR_API\"}},\"configurationResourceKeys\":{\"vfModuleCustomizationUUID\":\"7adc7c29-21a3-40a2-b8b6-5d4ad08b68e9\",\"vnfResourceCustomizationUUID\":\"a80f05b8-d651-44af-b999-8ed78fb4582f\",\"cvnfcCustomizationUUID\":\"69cce457-9ffd-4359-962b-0596a1e83ad1\",\"vnfcName\":\"zauk51bmcmr01mcm001\"}}]";
        ObjectMapper om = new ObjectMapper();
        List<ExecuteBuildingBlock> flowsToExecute = null;
        try {
            ExecuteBuildingBlock[] asArray = om.readValue(flowsToExecuteString, ExecuteBuildingBlock[].class);
            flowsToExecute = Arrays.asList(asArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        when(bbSetupUtils.loadOriginalFlowExecutionPath(anyString())).thenReturn(flowsToExecute);

        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignServiceInstanceBB");
    }

    @Test
    public void getRelatedResourcesInVfModuleTest() throws Exception {
        org.onap.aai.domain.yang.Vnfc vnfc = new org.onap.aai.domain.yang.Vnfc();
        vnfc.setModelInvariantId("modelInvariantId");
        vnfc.setVnfcName("testVnfcName");

        String vfncPayload =
                new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "/BuildingBlocks/vnfcResponse.json")));
        AAIResultWrapper vfncWrapper = new AAIResultWrapper(vfncPayload);

        String configurationPayload =
                new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "/BuildingBlocks/configurationResponse.json")));
        AAIResultWrapper configurationWrapper = new AAIResultWrapper(configurationPayload);
        List<AAIResultWrapper> configurationResultWrappers = new ArrayList<AAIResultWrapper>();
        configurationResultWrappers.add(configurationWrapper);

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().vnfc(vnfc.getVnfcName()));
        when(bbSetupUtils.getAAIResourceDepthOne(uri)).thenReturn(vfncWrapper);

        doReturn(configurationResultWrappers).when(SPY_workflowAction).getResultWrappersFromRelationships(any(),
                any());
        org.onap.aai.domain.yang.Configuration configuration = SPY_workflowAction.getRelatedResourcesInVnfc(vnfc,
                org.onap.aai.domain.yang.Configuration.class, Types.CONFIGURATION);
        assertEquals("testConfigurationId", configuration.getConfigurationId());
    }

    /**
     * WorkflowActionBB Tests
     */

    @Test
    public void pluralTest() {
        List<String> items = Arrays.asList("serviceInstances, Service", "vnfs, Vnf", "vfModules, VfModule",
                "networks, Network", "invalidNames, invalidNames");
        items.forEach(item -> {
            String[] split = item.split(",");
            String type = split[0].trim();
            String expected = split[1].trim();
            assertThat(workflowAction.convertTypeFromPlural(type), equalTo(expected));
        });
    }

    @Test
    public void sortExecutionPathByObjectForVlanTaggingCreateTest() {
        List<ExecuteBuildingBlock> executeFlows = new ArrayList<>();

        BuildingBlock bb = new BuildingBlock().setBpmnFlowName("AssignNetworkBB").setKey("0");
        ExecuteBuildingBlock ebb = new ExecuteBuildingBlock().setBuildingBlock(bb);
        executeFlows.add(ebb);

        BuildingBlock bb2 = new BuildingBlock().setBpmnFlowName("AssignNetworkBB").setKey("1");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(bb2);
        executeFlows.add(ebb2);

        BuildingBlock bb3 = new BuildingBlock().setBpmnFlowName("CreateNetworkBB").setKey("0");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(bb3);
        executeFlows.add(ebb3);

        BuildingBlock bb4 = new BuildingBlock().setBpmnFlowName("CreateNetworkBB").setKey("1");
        ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock().setBuildingBlock(bb4);
        executeFlows.add(ebb4);

        BuildingBlock bb5 = new BuildingBlock().setBpmnFlowName("ActivateNetworkBB").setKey("0");
        ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock().setBuildingBlock(bb5);
        executeFlows.add(ebb5);

        BuildingBlock bb6 = new BuildingBlock().setBpmnFlowName("ActivateNetworkBB").setKey("1");
        ExecuteBuildingBlock ebb6 = new ExecuteBuildingBlock().setBuildingBlock(bb6);
        executeFlows.add(ebb6);

        List<ExecuteBuildingBlock> sorted =
                workflowAction.sortExecutionPathByObjectForVlanTagging(executeFlows, "createInstance");
        assertEqualsBulkFlowName(sorted, "AssignNetworkBB", "CreateNetworkBB", "ActivateNetworkBB", "AssignNetworkBB",
                "CreateNetworkBB", "ActivateNetworkBB");
    }

    @Test
    public void sortExecutionPathByObjectForVlanTaggingDeleteTest() {
        List<ExecuteBuildingBlock> executeFlows = new ArrayList<>();

        BuildingBlock bb = new BuildingBlock().setBpmnFlowName("DeactivateNetworkBB").setKey("0");
        ExecuteBuildingBlock ebb = new ExecuteBuildingBlock().setBuildingBlock(bb);
        executeFlows.add(ebb);

        BuildingBlock bb2 = new BuildingBlock().setBpmnFlowName("DeactivateNetworkBB").setKey("1");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(bb2);
        executeFlows.add(ebb2);

        BuildingBlock bb3 = new BuildingBlock().setBpmnFlowName("DeleteNetworkBB").setKey("0");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(bb3);
        executeFlows.add(ebb3);

        BuildingBlock bb4 = new BuildingBlock().setBpmnFlowName("DeleteNetworkBB").setKey("1");
        ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock().setBuildingBlock(bb4);
        executeFlows.add(ebb4);

        BuildingBlock bb5 = new BuildingBlock().setBpmnFlowName("UnassignNetworkBB").setKey("0");
        ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock().setBuildingBlock(bb5);
        executeFlows.add(ebb5);

        BuildingBlock bb6 = new BuildingBlock().setBpmnFlowName("UnassignNetworkBB").setKey("1");
        ExecuteBuildingBlock ebb6 = new ExecuteBuildingBlock().setBuildingBlock(bb6);
        executeFlows.add(ebb6);

        List<ExecuteBuildingBlock> sorted =
                workflowAction.sortExecutionPathByObjectForVlanTagging(executeFlows, "deleteInstance");
        assertEqualsBulkFlowName(sorted, "DeactivateNetworkBB", "DeleteNetworkBB", "UnassignNetworkBB",
                "DeactivateNetworkBB", "DeleteNetworkBB", "UnassignNetworkBB");
    }

    @Test
    public void queryNorthBoundRequestCatalogDbNestedTest() {
        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AAICheckVnfInMaintBB", "AAISetVnfInMaintBB",
                "VNF-Macro-Replace", "SDNOVnfHealthCheckBB", "AAIUnsetVnfInMaintBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);
        List<OrchestrationFlow> macroFlows = createFlowList("DeactivateVfModuleBB", "DeleteVfModuleBB",
                "DeactivateVnfBB", "CreateVfModuleBB", "ActivateVfModuleBB", "ActivateVnfBB");

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner("replaceInstance",
                "Vnf", false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        when(catalogDbClient.getOrchestrationFlowByAction("VNF-Macro-Replace")).thenReturn(macroFlows);
        List<OrchestrationFlow> flows = workflowAction.queryNorthBoundRequestCatalogDb(execution, "replaceInstance",
                WorkflowType.VNF, false, "my-custom-cloud-owner");
        assertEquals(flows.get(0).getFlowName(), "AAICheckVnfInMaintBB");
        assertEquals(flows.get(1).getFlowName(), "AAISetVnfInMaintBB");
        assertEquals(flows.get(2).getFlowName(), "DeactivateVfModuleBB");
        assertEquals(flows.get(3).getFlowName(), "DeleteVfModuleBB");
        assertEquals(flows.get(4).getFlowName(), "DeactivateVnfBB");
        assertEquals(flows.get(5).getFlowName(), "CreateVfModuleBB");
        assertEquals(flows.get(6).getFlowName(), "ActivateVfModuleBB");
        assertEquals(flows.get(7).getFlowName(), "ActivateVnfBB");
        assertEquals(flows.get(8).getFlowName(), "SDNOVnfHealthCheckBB");
        assertEquals(flows.get(9).getFlowName(), "AAIUnsetVnfInMaintBB");
    }

    @Test
    public void queryNorthBoundRequestCatalogDbTransportTest() {
        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwnerAndServiceType(
                "createInstance", "Service", true, "my-custom-cloud-owner", "TRANSPORT")).thenReturn(northBoundRequest);

        List<OrchestrationFlow> flows = workflowAction.queryNorthBoundRequestCatalogDb(execution, "createInstance",
                WorkflowType.SERVICE, true, "my-custom-cloud-owner", "TRANSPORT");
        assertEquals(flows.get(0).getFlowName(), "AssignServiceInstanceBB");
    }

    @Test
    public void extractResourceIdAndTypeFromUriTest() {
        String uri = "/v6/serviceInstances/123";
        String uri2 = "/v6/serviceInstances/123/vnfs/1234";
        String uri3 = "/v6/serviceInstances";
        String uri4 = "/v6/serviceInstances/assign";
        String uri5 = "'/v6/serviceInstances/123/vnfs";
        String uri6 = "/v6/serviceInstances/123/vnfs/1234/someAction";
        String uri7 = "/v6/serviceInstances/123/vnfs/1234/vfModules/5678/replace";
        String uri8 = "/v6/serviceInstances/123/vnfs/1234/vfModules/scaleOut";
        Resource expected1 = new Resource(WorkflowType.SERVICE, "123", true, null);
        Resource expected2 = new Resource(WorkflowType.VNF, "1234", false, expected1);
        Resource expected3 = new Resource(WorkflowType.VNF, "1234", false, expected1);
        Resource expected4 = new Resource(WorkflowType.VFMODULE, "5678", false, expected2);
        Resource result = workflowAction.extractResourceIdAndTypeFromUri(uri);
        assertEquals(expected1.getResourceId(), result.getResourceId());
        assertEquals(expected1.getResourceType(), result.getResourceType());
        result = workflowAction.extractResourceIdAndTypeFromUri(uri2);
        assertEquals(expected2.getResourceId(), result.getResourceId());
        assertEquals(expected2.getResourceType(), result.getResourceType());
        result = workflowAction.extractResourceIdAndTypeFromUri(uri3);
        assertEquals("Service", result.getResourceType().toString());
        assertEquals(UUID.randomUUID().toString().length(), result.getResourceId().length());
        result = workflowAction.extractResourceIdAndTypeFromUri(uri4);
        assertEquals("Service", result.getResourceType().toString());
        assertEquals(UUID.randomUUID().toString().length(), result.getResourceId().length());
        result = workflowAction.extractResourceIdAndTypeFromUri(uri5);
        assertEquals("Vnf", result.getResourceType().toString());
        assertEquals(UUID.randomUUID().toString().length(), result.getResourceId().length());
        result = workflowAction.extractResourceIdAndTypeFromUri(uri6);
        assertEquals(expected3.getResourceId(), result.getResourceId());
        assertEquals(expected3.getResourceType(), result.getResourceType());
        result = workflowAction.extractResourceIdAndTypeFromUri(uri7);
        assertEquals(expected4.getResourceId(), result.getResourceId());
        assertEquals(expected4.getResourceType(), result.getResourceType());
        result = workflowAction.extractResourceIdAndTypeFromUri(uri8);
        assertEquals(UUID.randomUUID().toString().length(), result.getResourceId().length());
        assertEquals("VfModule", result.getResourceType().toString());

    }

    @Test
    public void extractResourceIdAndTypeFromUriInvalidTypeTest() {
        this.expectedException.expect(IllegalArgumentException.class);
        this.expectedException.expectMessage(containsString("Uri could not be parsed. No type found."));
        workflowAction.extractResourceIdAndTypeFromUri("/v6/serviceInstances/123/vnfs/1234/vfmodules/5678/replace");
    }

    @Test
    public void extractResourceIdAndTypeFromUriInvalidUriTest() {
        this.expectedException.expect(IllegalArgumentException.class);
        this.expectedException.expectMessage(containsString("Uri could not be parsed:"));
        workflowAction.extractResourceIdAndTypeFromUri("something that doesn't match anything");
    }

    @Test
    public void extractResourceIdAndTypeFromUriResumeTest() {
        Resource resource = workflowAction.extractResourceIdAndTypeFromUri(
                "http://localhost:9100/onap/so/infra/serviceInstantiation/v7/serviceInstances/4ff87c63-461b-4d83-8121-d351e6db216c/vnfs/eea9b93b-b5b9-4fad-9c35-12d52e4b683f/vfModules/33cb74cd-9cb3-4090-a3c0-1b8c8e235847/resume");
        assertEquals(resource.getResourceId(), "33cb74cd-9cb3-4090-a3c0-1b8c8e235847");
    }

    @Test
    public void extractResourceIdAndTypeFromUriReplaceResumeTest() {
        Resource resource = workflowAction.extractResourceIdAndTypeFromUri(
                "http://localhost:9100/onap/so/infra/serviceInstantiation/v7/serviceInstances/4ff87c63-461b-4d83-8121-d351e6db216c/vnfs/eea9b93b-b5b9-4fad-9c35-12d52e4b683f/vfModules/33cb74cd-9cb3-4090-a3c0-1b8c8e235847/deactivateAndCloudDelete/resume");
        assertEquals(resource.getResourceId(), "33cb74cd-9cb3-4090-a3c0-1b8c8e235847");
    }

    @Test
    public void extractResourceIdAndTypeFromUriDeActiveResumeTest() {
        Resource resource = workflowAction.extractResourceIdAndTypeFromUri(
                "http://localhost:9100/onap/so/infra/serviceInstantiation/v7/serviceInstances/4ff87c63-461b-4d83-8121-d351e6db216c/vnfs/eea9b93b-b5b9-4fad-9c35-12d52e4b683f/vfModules/33cb74cd-9cb3-4090-a3c0-1b8c8e235847/replace/resume");
        assertEquals(resource.getResourceId(), "33cb74cd-9cb3-4090-a3c0-1b8c8e235847");
    }

    @Test
    public void extractResourceIdAndTypeFromUriResumeIdOnly() {
        Resource resource = workflowAction.extractResourceIdAndTypeFromUri(
                "http://localhost:9100/onap/so/infra/serviceInstantiation/v7/serviceInstances/4ff87c63-461b-4d83-8121-d351e6db216c/vnfs/eea9b93b-b5b9-4fad-9c35-12d52e4b683f/vfModules/33cb74cd-9cb3-4090-a3c0-1b8c8e235847/resume");
        assertEquals(resource.getResourceId(), "33cb74cd-9cb3-4090-a3c0-1b8c8e235847");
    }

    @Test
    public void isUriResumeTest() {
        assertTrue(workflowAction.isUriResume(
                "http://localhost:9100/onap/so/infra/orchestrationRequests/v7/requests/2f8ab587-ef6a-4456-b7b2-d73f9363dabd/resume"));
        assertTrue(workflowAction.isUriResume(
                "        http://localhost:9100/onap/so/infra/serviceInstantiation/v7/serviceInstances/4ff87c63-461b-4d83-8121-d351e6db216c/vnfs/eea9b93b-b5b9-4fad-9c35-12d52e4b683f/vfModules/33cb74cd-9cb3-4090-a3c0-1b8c8e235847/resume"));

        assertFalse(workflowAction.isUriResume("/v6/serviceInstances/123/vnfs/1234/vfmodules/5678/replace"));
    }

    @Test
    public void isRequestMacroServiceResumeTest() {
        ServiceInstance si = new ServiceInstance();
        when(bbSetupUtils.getAAIServiceInstanceById(anyString())).thenReturn(si);
        assertFalse(workflowAction.isRequestMacroServiceResume(false, WorkflowType.SERVICE, "createInstance", ""));
        assertTrue(workflowAction.isRequestMacroServiceResume(false, WorkflowType.SERVICE, "createInstance", "123"));
    }

    @Test
    public void populateResourceIdsFromApiHandlerTest() {
        execution.setVariable("serviceInstanceId", "123");
        execution.setVariable("vnfId", "888");
        WorkflowResourceIds x = workflowAction.populateResourceIdsFromApiHandler(execution);
        assertEquals("123", x.getServiceInstanceId());
        assertEquals("888", x.getVnfId());
        assertNull(x.getVolumeGroupId());
    }

    @Test
    public void handleRuntimeExceptionTest() {
        execution.setVariable("BPMN_javaExpMsg", "test runtime error message");
        execution.setVariable("testProcessKey", "testProcessKeyValue");
        try {
            workflowAction.handleRuntimeException(execution);
        } catch (BpmnError wfe) {
            assertEquals("MSOWorkflowException", wfe.getErrorCode());
        }
    }

    private List<OrchestrationFlow> createFlowList(String... flowNames) {
        List<OrchestrationFlow> result = new ArrayList<>();
        int sequenceNumber = 1;
        for (String flowName : flowNames) {
            OrchestrationFlow orchFlow = new OrchestrationFlow();
            orchFlow.setFlowName(flowName);
            orchFlow.setSequenceNumber(sequenceNumber++);
            result.add(orchFlow);
        }
        return result;
    }

    private void assertEqualsBulkFlowName(List<ExecuteBuildingBlock> ebbs, String... flowNames) {
        for (int i = 0; i < ebbs.size(); i++) {
            assertEquals(ebbs.get(i).getBuildingBlock().getBpmnFlowName(), flowNames[i]);
        }
        assertEquals(ebbs.size(), flowNames.length);
    }

    private void initExecution(String gAction, String bpmnRequest, boolean isAlaCarte) {
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", isAlaCarte);
        execution.setVariable("apiVersion", "7");
    }

    private String readBpmnRequestFromFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/__files/" + fileName)));
    }

    private List<Resource> prepareListWithResources(boolean addPnf, boolean addPriorites) {
        List<Resource> resourceList = new ArrayList<>();
        Resource r1 = new Resource(WorkflowType.SERVICE, "3c40d244-808e-42ca-b09a-256d83d19d0a", false, null);
        resourceList.add(r1);
        Resource r2 = new Resource(WorkflowType.VNF, "ab153b6e-c364-44c0-bef6-1f2982117f04", false, r1);
        resourceList.add(r2);
        Resource r3 = new Resource(WorkflowType.VOLUMEGROUP, "a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f", false, r2);
        resourceList.add(r3);
        Resource r4 = new Resource(WorkflowType.VFMODULE, "72d9d1cd-f46d-447a-abdb-451d6fb05fa8", false, r2);
        resourceList.add(r4);
        Resource r5 = new Resource(WorkflowType.VFMODULE, "3c40d244-808e-42ca-b09a-256d83d19d0a", false, r2);
        resourceList.add(r5);
        Resource r6 = new Resource(WorkflowType.VFMODULE, "da4d4327-fb7d-4311-ac7a-be7ba60cf969", false, r2);
        resourceList.add(r6);
        if (addPnf) {
            Resource r7 = new Resource(WorkflowType.PNF, "aa153b6e-c364-44c0-bef6-1f2982117f04", false, r1);
            resourceList.add(r7);
        }
        if (addPriorites) {
            r3.setProcessingPriority(2);
            r4.setProcessingPriority(1);
            r5.setProcessingPriority(4);
            r5.setBaseVfModule(true);
            r6.setProcessingPriority(3);
        }
        return resourceList;
    }
}
