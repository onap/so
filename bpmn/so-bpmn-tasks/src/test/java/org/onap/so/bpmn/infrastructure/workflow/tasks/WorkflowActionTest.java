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

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.DuplicateNameException;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
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
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.SubscriberInfo;
import org.springframework.core.env.Environment;

public class WorkflowActionTest extends BaseTaskTest {

    @Mock
    protected Environment environment;
    @InjectMocks
    protected WorkflowAction workflowAction;
    private DelegateExecution execution;

    @InjectMocks
    @Spy
    protected WorkflowAction SPY_workflowAction;

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

    @Before
    public void before() throws Exception {
        execution = new DelegateExecutionFake();
        org.onap.aai.domain.yang.ServiceInstance servInstance = new org.onap.aai.domain.yang.ServiceInstance();
        servInstance.setServiceInstanceId("TEST");
        when(bbSetupUtils.getAAIServiceInstanceByName(anyString(), anyObject())).thenReturn(servInstance);
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
    public void selectExecutionListExceptionAlreadyBuiltTest() throws Exception {
        DelegateExecution delegateExecution = new DelegateExecutionFake();
        String gAction = "deleteInstance";
        String resource = "VfModule";
        delegateExecution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        delegateExecution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        delegateExecution.setVariable("bpmnRequest", bpmnRequest);
        delegateExecution.setVariable("aLaCarte", true);
        delegateExecution.setVariable("apiVersion", "7");
        delegateExecution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("DeactivateVfModuleBB", "DeleteVfModuleBB",
                "UnassignVfModuleBB", "DeactivateFabricConfigurationBB", "UnassignFabricConfigurationBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);

        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);

        doAnswer(invocation -> {
            DelegateExecutionFake execution = invocation.getArgument(0);
            execution.setVariable("WorkflowException", "exception");
            execution.setVariable("WorkflowExceptionErrorMessage", "errorMessage");
            throw new BpmnError("WorkflowException");
        }).when(exceptionUtil).buildAndThrowWorkflowException(delegateExecution, 7000,
                "Exception in getConfigBuildingBlock: Multiple relationships exist from VNFC testVnfcName to Configurations");


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
        doReturn(vnfcs).when(SPY_workflowAction).getRelatedResourcesInVfModule(any(), any(), any(), any());

        List<org.onap.aai.domain.yang.Configuration> configurations =
                new ArrayList<org.onap.aai.domain.yang.Configuration>();
        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("configurationId");
        configuration.setModelCustomizationId("modelCustimizationId");
        configuration.setConfigurationName("testConfigurationName");
        configurations.add(configuration);
        org.onap.aai.domain.yang.Configuration configuration1 = new org.onap.aai.domain.yang.Configuration();
        configuration1.setConfigurationId("configurationId");
        configuration1.setModelCustomizationId("modelCustimizationId");
        configuration1.setConfigurationName("testConfigurationName");
        configurations.add(configuration1);
        doReturn(configurations).when(SPY_workflowAction).getRelatedResourcesInVnfc(any(), any(), any());

        doReturn("testName").when(SPY_workflowAction).getVnfcNameForConfiguration(any());

        thrown.expect(BpmnError.class);
        SPY_workflowAction.selectExecutionList(delegateExecution);
        assertEquals(
                "Exception in getConfigBuildingBlock: Multiple relationships exist from VNFC testVnfcName to Configurations",
                delegateExecution.getVariable("WorkflowException"));
    }

    @Test
    public void selectExecutionListDuplicateNameExceptionTest() throws Exception {
        String gAction = "createInstance";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
        execution.setVariable("requestUri", "v6/serviceInstances");
        execution.setVariable("requestAction", gAction);

        doThrow(new DuplicateNameException(
                "serviceInstance with name (instanceName) and different version id (3c40d244-808e-42ca-b09a-256d83d19d0a) already exists. The name must be unique."))
                        .when(SPY_workflowAction).validateResourceIdInAAI(anyString(), eq(WorkflowType.SERVICE),
                                eq("test"), any(RequestDetails.class), any(WorkflowResourceIds.class));

        SPY_workflowAction.selectExecutionList(execution);
        assertEquals(execution.getVariable("WorkflowActionErrorMessage"),
                "Exception while setting execution list. serviceInstance with name (instanceName) and different version id (3c40d244-808e-42ca-b09a-256d83d19d0a) already exists. The name must be unique.");
    }

    /**
     * SERVICE MACRO TESTS
     */
    @Test
    public void selectExecutionListServiceMacroAssignTest() throws Exception {
        String gAction = "assignInstance";
        String resource = "Service";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(
                Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssignNoCloud.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(Files
                .readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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

        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("si0");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "CreateVolumeGroupBB", "ActivateVolumeGroupBB", "CreateVfModuleBB",
                "CreateVfModuleBB", "ActivateVfModuleBB", "ActivateVfModuleBB", "ActivateVnfBB",
                "ActivateServiceInstanceBB");
        assertEquals("volumeGroup0", ebbs.get(0).getWorkflowResourceIds().getVolumeGroupId());
        assertEquals("volumeGroup0", ebbs.get(1).getWorkflowResourceIds().getVolumeGroupId());
        assertEquals("testVfModuleId1", ebbs.get(2).getWorkflowResourceIds().getVfModuleId());
        assertEquals("testVfModuleId2", ebbs.get(3).getWorkflowResourceIds().getVfModuleId());
        assertEquals("testVfModuleId1", ebbs.get(4).getWorkflowResourceIds().getVfModuleId());
        assertEquals("testVfModuleId2", ebbs.get(5).getWorkflowResourceIds().getVfModuleId());
        assertEquals("vnf0", ebbs.get(6).getWorkflowResourceIds().getVnfId());
        assertEquals("si0", ebbs.get(7).getWorkflowResourceIds().getServiceInstanceId());

    }

    @Test
    public void selectExecutionListServiceMacroDeactivateTest() throws Exception {
        String gAction = "deactivateInstance";
        String resource = "Service";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(Files
                .readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(Files
                .readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(Files
                .readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(Files
                .readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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
                "ActivateVolumeGroupBB", "CreateVfModuleBB", "CreateVfModuleBB", "CreateVfModuleBB",
                "ActivateVfModuleBB", "ActivateVfModuleBB", "ActivateVfModuleBB", "ActivateVnfBB",
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
    public void selectExecutionListServiceMacroDeleteTest() throws Exception {
        String gAction = "deleteInstance";
        String resource = "Service";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(Files
                .readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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

        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeactivateVfModuleBB", "DeleteVfModuleBB",
                "DeleteVfModuleBB", "DeactivateVolumeGroupBB", "DeleteVolumeGroupBB", "DeactivateVnfBB",
                "DeactivateServiceInstanceBB", "UnassignVfModuleBB", "UnassignVfModuleBB", "UnassignVolumeGroupBB",
                "UnassignVnfBB", "UnassignServiceInstanceBB");
    }

    @Test
    public void selectExecutionListServiceMacroUnassignTest() throws Exception {
        String gAction = "unassignInstance";
        String resource = "Service";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(Files
                .readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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

        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(Files
                .readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/VnfMacroReplace.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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

        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AAICheckVnfInMaintBB", "AAISetVnfInMaintBB", "DeactivateVfModuleBB",
                "DeactivateVfModuleBB", "DeleteVfModuleBB", "DeleteVfModuleBB", "DeactivateVnfBB", "CreateVfModuleBB",
                "CreateVfModuleBB", "ActivateVfModuleBB", "ActivateVfModuleBB", "ActivateVnfBB", "SDNOVnfHealthCheckBB",
                "AAIUnsetVnfInMaintBB");
    }

    @Test
    public void selectExecutionListVnfMacroReplaceTest() throws Exception {
        String gAction = "replaceInstance";
        String resource = "Vnf";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/VnfMacroReplace.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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
        VfModule vfModuleAAI = new VfModule();
        vfModuleAAI.setVfModuleId("vfModule2");
        RelationshipList relationshipList = new RelationshipList();
        Relationship relationship = new Relationship();
        relationshipList.getRelationship().add(relationship);
        vfModuleAAI.setRelationshipList(relationshipList);
        Relationships relationships = new Relationships("abc");
        Configuration config = new Configuration();
        config.setConfigurationId("configId");
        Optional<Configuration> configOp = Optional.of(config);
        Optional<Relationships> relationshipsOp = Optional.of(relationships);

        doReturn(relationshipsOp).when(workflowActionUtils).extractRelationshipsVnfc(isA(Relationships.class));
        doReturn(configOp).when(workflowActionUtils).extractRelationshipsConfiguration(isA(Relationships.class));
        doReturn(vfModuleAAI).when(bbSetupUtils).getAAIVfModule("1234", "vfModule2");
        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction, resource,
                false, "cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AAICheckVnfInMaintBB", "AAISetVnfInMaintBB", "DeactivateFabricConfigurationBB",
                "UnassignFabricConfigurationBB", "DeactivateVfModuleBB", "DeactivateVfModuleBB", "DeleteVfModuleBB",
                "DeleteVfModuleBB", "DeactivateVnfBB", "ChangeModelVfModuleBB", "ChangeModelVfModuleBB",
                "CreateVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB", "ActivateVfModuleBB",
                "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB", "ChangeModelVnfBB", "ActivateVnfBB",
                "ChangeModelServiceInstanceBB", "SDNOVnfHealthCheckBB", "AAIUnsetVnfInMaintBB");
        for (ExecuteBuildingBlock executeBuildingBlock : ebbs) {
            assertEquals("123", executeBuildingBlock.getWorkflowResourceIds().getServiceInstanceId());
        }
    }

    @Ignore
    @Test
    public void selectExecutionListNetworkCollectionMacroCreate() throws Exception {
        String gAction = "createInstance";
        String resource = "NetworkCollection";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(
                Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/CreateNetworkCollection.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(
                Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/CreateNetworkCollection.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
        execution.setVariable("requestUri",
                "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");

        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB",
                "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB");
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
        // when(catalogDbClient.getCvnfcCustomizationByVnfCustomizationUUIDAndVfModuleCustomizationUUID("fc25201d-36d6-43a3-8d39-fdae88e526ae",
        // "9a6d01fd-19a7-490a-9800-460830a12e0b")).thenReturn(cvnfcCustomizations);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "AssignVfModuleBB", "CreateVfModuleBB", "ActivateVfModuleBB",
                "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB", "AssignFabricConfigurationBB",
                "ActivateFabricConfigurationBB");
    }

    @Test
    public void selectExecutionListALaCarteVfModuleNoVolumeGroupReplaceTest() throws Exception {
        String gAction = "replaceInstance";
        String resource = "VfModule";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
    public void selectExecutionListALaCarteVfModuleKeepVolumeGroupReplaceRetainAssignmentsTest() throws Exception {
        String gAction = "replaceInstanceRetainAssignments";
        String resource = "VfModule";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(
                Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleReplaceRebuildVolumeGroups.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(
                Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleReplaceRebuildVolumeGroups.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        when(bbSetupUtils.getAAIGenericVnf(anyObject())).thenReturn(vnf);

        org.onap.aai.domain.yang.VfModule vfModule = new org.onap.aai.domain.yang.VfModule();
        vfModule.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIVfModule(anyObject(), anyObject())).thenReturn(vfModule);

        List<org.onap.aai.domain.yang.Vnfc> vnfcs = new ArrayList<org.onap.aai.domain.yang.Vnfc>();
        org.onap.aai.domain.yang.Vnfc vnfc = new org.onap.aai.domain.yang.Vnfc();
        vnfc.setModelInvariantId("modelInvariantId");
        vnfc.setVnfcName("testVnfcName");
        vnfcs.add(vnfc);
        doReturn(vnfcs).when(SPY_workflowAction).getRelatedResourcesInVfModule(anyObject(), anyObject(), anyObject(),
                anyObject());

        List<org.onap.aai.domain.yang.Configuration> configurations =
                new ArrayList<org.onap.aai.domain.yang.Configuration>();
        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("configurationId");
        configuration.setModelCustomizationId("modelCustimizationId");
        configuration.setConfigurationName("testConfigurationName");
        configurations.add(configuration);
        doReturn(configurations).when(SPY_workflowAction).getRelatedResourcesInVnfc(anyObject(), anyObject(),
                anyObject());

        doReturn("testName").when(SPY_workflowAction).getVnfcNameForConfiguration(anyObject());

        SPY_workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "DeactivateFabricConfigurationBB", "UnassignFabricConfigurationBB",
                "DeactivateVfModuleBB", "DeleteVfModuleBB", "UnassignVfModuleBB");
    }

    @Test
    public void getConfigBuildingBlocksNoVfModuleFabricDeleteTest() throws Exception {
        String gAction = "deleteInstance";
        ObjectMapper mapper = new ObjectMapper();
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
        Resource resourceKey = new Resource(resourceType, key, aLaCarte);
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
        when(bbSetupUtils.getAAIGenericVnf(anyObject())).thenReturn(vnf);

        org.onap.aai.domain.yang.VfModule vfModule = new org.onap.aai.domain.yang.VfModule();
        vfModule.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIVfModule(anyObject(), anyObject())).thenReturn(null);

        SPY_workflowAction.getConfigBuildingBlocks(dataObj);
    }

    @Test
    public void selectExecutionListALaCarteVfModuleNoFabricDeleteTest() throws Exception {
        String gAction = "deleteInstance";
        String resource = "VfModule";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", true);
        execution.setVariable("apiVersion", "7");
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
        when(bbSetupUtils.getAAIGenericVnf(anyObject())).thenReturn(vnf);

        org.onap.aai.domain.yang.VfModule vfModule = new org.onap.aai.domain.yang.VfModule();
        vfModule.setModelCustomizationId("modelCustomizationId");
        when(bbSetupUtils.getAAIVfModule(anyObject(), anyObject())).thenReturn(vfModule);

        List<org.onap.aai.domain.yang.Vnfc> vnfcs = new ArrayList<org.onap.aai.domain.yang.Vnfc>();
        org.onap.aai.domain.yang.Vnfc vnfc = new org.onap.aai.domain.yang.Vnfc();
        vnfc.setModelInvariantId("modelInvariantId");
        vnfc.setVnfcName("testVnfcName");
        vnfcs.add(vnfc);
        doReturn(vnfcs).when(SPY_workflowAction).getRelatedResourcesInVfModule(anyObject(), anyObject(), anyObject(),
                anyObject());

        List<org.onap.aai.domain.yang.Configuration> configurations =
                new ArrayList<org.onap.aai.domain.yang.Configuration>();
        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        doReturn(configurations).when(SPY_workflowAction).getRelatedResourcesInVnfc(anyObject(), anyObject(),
                anyObject());

        doReturn("testName").when(SPY_workflowAction).getVnfcNameForConfiguration(anyObject());

        SPY_workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs, "DeactivateVfModuleBB", "DeleteVfModuleBB", "UnassignVfModuleBB");
    }

    @Test
    public void selectExecutionListMacroResumeTest() throws Exception {
        String gAction = "createInstance";
        String resource = "Service";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
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

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VNFC, vnfc.getVnfcName());
        when(bbSetupUtils.getAAIResourceDepthOne(uri)).thenReturn(vfncWrapper);

        doReturn(configurationResultWrappers).when(SPY_workflowAction).getResultWrappersFromRelationships(anyObject(),
                anyObject());
        List<org.onap.aai.domain.yang.Configuration> configurationsList = SPY_workflowAction.getRelatedResourcesInVnfc(
                vnfc, org.onap.aai.domain.yang.Configuration.class, AAIObjectType.CONFIGURATION);
        assertEquals(1, configurationsList.size());
        assertEquals("testConfigurationId", configurationsList.get(0).getConfigurationId());
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
    public void sortExecutionPathByObjectForVlanTaggingCreateTest() throws Exception {
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
    public void sortExecutionPathByObjectForVlanTaggingDeleteTest() throws Exception {
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
    public void queryNorthBoundRequestCatalogDbNestedTest() throws MalformedURLException {
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
    public void queryNorthBoundRequestCatalogDbTransportTest() throws MalformedURLException {
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
        Resource expected1 = new Resource(WorkflowType.SERVICE, "123", true);
        Resource expected2 = new Resource(WorkflowType.VNF, "1234", false);
        Resource expected3 = new Resource(WorkflowType.VNF, "1234", false);
        Resource expected4 = new Resource(WorkflowType.VFMODULE, "5678", false);
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

    private RequestDetails setupRequestDetails(String globalSubscriberId, String subscriptionServiceType,
            String modelCustomizationId) {
        RequestDetails reqDetails = new RequestDetails();
        SubscriberInfo subInfo = new SubscriberInfo();
        subInfo.setGlobalSubscriberId(globalSubscriberId);
        reqDetails.setSubscriberInfo(subInfo);
        RequestParameters reqParams = new RequestParameters();
        reqParams.setSubscriptionServiceType(subscriptionServiceType);
        reqDetails.setRequestParameters(reqParams);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(modelCustomizationId);
        reqDetails.setModelInfo(modelInfo);
        return reqDetails;
    }

    @Test
    public void validateResourceIdInAAIVnfTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        // Vnf
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        vnf.setVnfName("vnfName123");
        vnf.setModelCustomizationId("1234567");
        Optional<GenericVnf> opVnf = Optional.of(vnf);
        GenericVnf vnf2 = new GenericVnf();
        vnf2.setVnfId("id123");
        vnf2.setVnfName("vnfName222");
        vnf2.setModelCustomizationId("222");
        Optional<GenericVnf> opVnf2 = Optional.of(vnf2);
        when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123", "vnfName123")).thenReturn(opVnf);
        when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123", "vnfName222")).thenReturn(opVnf2);
        when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123", "111111")).thenReturn(Optional.empty());
        String id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VNF, "vnfName123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);
        String id2 = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VNF, "111111", reqDetails,
                workflowResourceIds);
        assertEquals("generatedId123", id2);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "generic-vnf with name (vnfName222), same parent and different customization id (222) already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VNF, "vnfName222", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAIVnfNotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        // Vnf
        GenericVnfs genericVnfs = new GenericVnfs();
        GenericVnf vnf3 = new GenericVnf();
        vnf3.setVnfId("id123");
        vnf3.setVnfName("vnfName333");
        genericVnfs.getGenericVnf().add(vnf3);
        when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123", "vnfName333")).thenReturn(Optional.empty());
        when(bbSetupUtils.getAAIVnfsGloballyByName("vnfName333")).thenReturn(genericVnfs);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "generic-vnf with name (vnfName333) id (id123) and different parent relationship already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VNF, "vnfName333", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAINetworkTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        // Network
        L3Network network = new L3Network();
        network.setNetworkId("id123");
        network.setNetworkName("name123");
        network.setModelCustomizationId("1234567");
        workflowResourceIds.setServiceInstanceId("siId123");
        Optional<L3Network> opNetwork = Optional.of(network);
        L3Network network2 = new L3Network();
        network2.setNetworkId("id123");
        network2.setNetworkName("networkName222");
        network2.setModelCustomizationId("222");
        Optional<L3Network> opNetwork2 = Optional.of(network2);
        when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("siId123", "name123")).thenReturn(opNetwork);
        when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("siId123", "networkName222"))
                .thenReturn(opNetwork2);
        when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("siId123", "111111")).thenReturn(Optional.empty());
        String id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.NETWORK, "name123",
                reqDetails, workflowResourceIds);
        assertEquals("id123", id);
        String id2 = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.NETWORK, "111111",
                reqDetails, workflowResourceIds);
        assertEquals("generatedId123", id2);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "l3Network with name (networkName222), same parent and different customization id (222) already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.NETWORK, "networkName222", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateNetworkResourceNameExistsInAAITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        // Network
        L3Network network = new L3Network();
        network.setNetworkId("id123");
        network.setNetworkName("name123");
        network.setModelCustomizationId("1234567");
        workflowResourceIds.setServiceInstanceId("siId123");

        when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("networkName333", "111111"))
                .thenReturn(Optional.empty());
        when(bbSetupUtils.existsAAINetworksGloballyByName("networkName333")).thenReturn(true);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "l3Network with name (networkName333) id (siId123) and different parent relationship already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.NETWORK, "networkName333", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAIVfModuleTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        vnf.setVnfName("vnfName123");
        vnf.setModelCustomizationId("222");

        // VfModule
        VfModules vfModules = new VfModules();
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("id123");
        vfModule.setVfModuleName("name123");
        vfModule.setModelCustomizationId("1234567");
        vfModules.getVfModule().add(vfModule);
        vnf.setVfModules(vfModules);
        workflowResourceIds.setVnfId("id123");
        when(bbSetupUtils.getAAIGenericVnf("id123")).thenReturn(vnf);
        String id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VFMODULE, "name123",
                reqDetails, workflowResourceIds);
        assertEquals("id123", id);

        GenericVnf vnf1 = new GenericVnf();
        VfModules vfModules2 = new VfModules();
        VfModule vfModule2 = new VfModule();
        vfModule2.setVfModuleId("id123");
        vfModule2.setVfModuleName("vFModName222");
        vfModule2.setModelCustomizationId("222");
        vfModules2.getVfModule().add(vfModule2);
        vnf1.setVfModules(vfModules2);
        workflowResourceIds.setVnfId("id111");
        when(bbSetupUtils.getAAIGenericVnf("id111")).thenReturn(vnf1);
        String id2 = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VFMODULE, "111111",
                reqDetails, workflowResourceIds);
        assertEquals("generatedId123", id2);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "vfModule with name (vFModName222), same parent and different customization id (1234567) already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VFMODULE, "vFModName222", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAIVfModuleNotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("id111");

        GenericVnf vnf1 = new GenericVnf();
        workflowResourceIds.setVnfId("id111");
        when(bbSetupUtils.getAAIGenericVnf("id111")).thenReturn(vnf1);

        when(bbSetupUtils.existsAAIVfModuleGloballyByName("vFModName333")).thenReturn(true);
        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("vfModule with name vFModName333 already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VFMODULE, "vFModName333", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAIVolumeGroupTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        vnf.setVnfName("vnfName123");
        vnf.setModelCustomizationId("1234567");

        GenericVnf vnf2 = new GenericVnf();
        vnf2.setVnfId("id123");
        vnf2.setVnfName("vnfName123");
        vnf2.setModelCustomizationId("222");

        // VolumeGroup
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("id123");
        volumeGroup.setVolumeGroupName("name123");
        volumeGroup.setVfModuleModelCustomizationId("1234567");
        workflowResourceIds.setVnfId("id123");
        Optional<VolumeGroup> opVolumeGroup = Optional.of(volumeGroup);

        workflowResourceIds.setVnfId("id123");

        when(bbSetupUtils.getAAIGenericVnf("id123")).thenReturn(vnf);
        when(bbSetupUtils.getRelatedVolumeGroupByNameFromVnf("id123", "name123")).thenReturn(opVolumeGroup);
        String id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "name123",
                reqDetails, workflowResourceIds);
        assertEquals("id123", id);

        when(bbSetupUtils.getAAIGenericVnf("id123")).thenReturn(vnf2);
        when(bbSetupUtils.getRelatedVolumeGroupByNameFromVfModule("id123", "id123", "111111"))
                .thenReturn(opVolumeGroup);

        when(bbSetupUtils.getRelatedVolumeGroupByNameFromVnf("id123", "111111")).thenReturn(Optional.empty());
        String id2 = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "111111",
                reqDetails, workflowResourceIds);
        assertEquals("generatedId123", id2);
    }

    @Test
    public void validatesourceIdInAAIVolumeGroupNotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("id123");
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        when(bbSetupUtils.getAAIGenericVnf("id123")).thenReturn(vnf);
        when(bbSetupUtils.getRelatedVolumeGroupByNameFromVnf("id123", "testVolumeGroup")).thenReturn(Optional.empty());

        when(bbSetupUtils.existsAAIVolumeGroupGloballyByName("testVolumeGroup")).thenReturn(true);
        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("volumeGroup with name testVolumeGroup already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "testVolumeGroup",
                reqDetails, workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAIConfigurationTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        // Configuration
        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("id123");
        configuration.setConfigurationName("name123");
        configuration.setModelCustomizationId("1234567");
        Optional<org.onap.aai.domain.yang.Configuration> opConfiguration = Optional.of(configuration);

        org.onap.aai.domain.yang.Configuration configuration2 = new org.onap.aai.domain.yang.Configuration();
        configuration2.setConfigurationId("id123");
        configuration2.setConfigurationName("name123");
        configuration2.setModelCustomizationId("222");
        Optional<org.onap.aai.domain.yang.Configuration> opConfiguration2 = Optional.of(configuration2);

        workflowResourceIds.setVnfId("id123");

        when(bbSetupUtils.getRelatedConfigurationByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(opConfiguration);
        String id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.CONFIGURATION, "name123",
                reqDetails, workflowResourceIds);
        assertEquals("id123", id);

        when(bbSetupUtils.getRelatedConfigurationByNameFromServiceInstance("siId123", "111111"))
                .thenReturn(Optional.empty());
        String id2 = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.CONFIGURATION, "111111",
                reqDetails, workflowResourceIds);
        assertEquals("generatedId123", id2);

        when(bbSetupUtils.getRelatedConfigurationByNameFromServiceInstance("siId123", "name222"))
                .thenReturn(opConfiguration2);
        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "configuration with name (name222), same parent and different customization id (id123) already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.CONFIGURATION, "name222", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAIConfigurationNotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        when(bbSetupUtils.getRelatedConfigurationByNameFromServiceInstance("siId123", "testConfig"))
                .thenReturn(Optional.empty());
        when(bbSetupUtils.existsAAIConfigurationGloballyByName("testConfig")).thenReturn(true);
        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("configuration with name testConfig already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.CONFIGURATION, "testConfig", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAISITest() throws Exception {
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        reqDetails.getModelInfo().setModelVersionId("1234567");

        ServiceInstance si = new ServiceInstance();
        si.setServiceInstanceId("siId123");
        si.setModelVersionId("1234567");
        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(si);
        Optional<ServiceInstance> siOp = Optional.of(si);
        ServiceInstance si2 = new ServiceInstance();
        si2.setServiceInstanceId("siId222");
        si2.setModelVersionId("22222");
        si2.setServiceInstanceName("siName222");
        Optional<ServiceInstance> siOp2 = Optional.of(si2);
        ServiceInstances serviceInstances2 = new ServiceInstances();
        serviceInstances2.getServiceInstance().add(si2);

        when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "siName123")).thenReturn(siOp);
        when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "siName222")).thenReturn(siOp2);
        when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "111111"))
                .thenReturn(Optional.empty());

        when(bbSetupUtils.getAAIServiceInstancesGloballyByName("siName123")).thenReturn(serviceInstances);
        String id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "siName123",
                reqDetails, workflowResourceIds);
        assertEquals("siId123", id);
        String id2 = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "111111",
                reqDetails, workflowResourceIds);
        assertEquals("generatedId123", id2);

        when(bbSetupUtils.getAAIServiceInstancesGloballyByName("siName222")).thenReturn(serviceInstances2);
        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName222) and different version id (1234567) already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "siName222", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAIMultipleSITest() throws Exception {
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        reqDetails.getModelInfo().setModelVersionId("1234567");

        ServiceInstance si = new ServiceInstance();
        si.setServiceInstanceId("siId123");
        si.setModelVersionId("1234567");
        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(si);

        ServiceInstance si2 = new ServiceInstance();
        si2.setServiceInstanceId("siId222");
        si2.setModelVersionId("22222");
        si2.setServiceInstanceName("siName222");
        serviceInstances.getServiceInstance().add(si2);

        when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "siId123"))
                .thenReturn(Optional.empty());

        when(bbSetupUtils.getAAIServiceInstancesGloballyByName("siName123")).thenReturn(serviceInstances);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName123) and multiple combination of model-version-id + service-type + global-customer-id already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "siName123", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateResourceIdInAAISIExistsTest() throws Exception {
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        reqDetails.getModelInfo().setModelVersionId("1234567");

        ServiceInstance si = new ServiceInstance();
        si.setServiceInstanceId("siId123");
        si.setModelVersionId("1234567");
        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(si);

        when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "siId123"))
                .thenReturn(Optional.empty());

        when(bbSetupUtils.getAAIServiceInstancesGloballyByName("siName123")).thenReturn(serviceInstances);

        Map<String, String> uriKeys = new HashMap<>();
        uriKeys.put("global-customer-id", "globalCustomerId");
        uriKeys.put("service-type", "serviceType");

        when(bbSetupUtils.getURIKeysFromServiceInstance("siId123")).thenReturn(uriKeys);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName123) and global-customer-id (globalCustomerId), service-type (serviceType), model-version-id (1234567) already exists. The name must be unique."));
        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "siName123", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateServiceResourceIdInAAINoDupTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        reqDetails.getModelInfo().setModelVersionId("1234567");
        when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "siName123"))
                .thenReturn(Optional.empty());
        when(bbSetupUtils.getAAIServiceInstancesGloballyByName("siName123")).thenReturn(null);
        String id = workflowAction.validateServiceResourceIdInAAI("generatedId123", "siName123", reqDetails);
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateServiceResourceIdInAAISameModelVersionId() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        reqDetails.getModelInfo().setModelVersionId("1234567");

        ServiceInstance si = new ServiceInstance();
        si.setServiceInstanceId("siId123");
        si.setModelVersionId("1234567");
        Optional<ServiceInstance> siOp = Optional.of(si);

        when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "siName123")).thenReturn(siOp);
        String id = workflowAction.validateServiceResourceIdInAAI("generatedId123", "siName123", reqDetails);
        assertEquals("siId123", id);
    }

    @Test
    public void validateServiceResourceIdInAAIDifferentModelVersionId() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        reqDetails.getModelInfo().setModelVersionId("1234567");

        ServiceInstance si = new ServiceInstance();
        si.setServiceInstanceId("siId123");
        si.setModelVersionId("9999999");
        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(si);
        Optional<ServiceInstance> siOp = Optional.of(si);

        when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "siName123")).thenReturn(siOp);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName123) and different version id (1234567) already exists. The name must be unique."));

        String id = workflowAction.validateServiceResourceIdInAAI("generatedId123", "siName123", reqDetails);
        assertEquals("siId123", id);
    }

    @Test
    public void validateServiceResourceIdInAAIDuplicateNameTest() throws Exception {

        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        reqDetails.getModelInfo().setModelVersionId("1234567");

        ServiceInstance si = new ServiceInstance();
        si.setServiceInstanceId("siId123");
        si.setModelVersionId("1234567");

        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(si);

        when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "siName"))
                .thenReturn(Optional.empty());
        when(bbSetupUtils.getAAIServiceInstancesGloballyByName("siName")).thenReturn(serviceInstances);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName) and global-customer-id (null), service-type (null), model-version-id (1234567) already exists. The name must be unique."));

        workflowAction.validateServiceResourceIdInAAI("generatedId123", "siName", reqDetails);
    }

    @Test
    public void validateServiceResourceIdInAAIDuplicateNameMultipleTest() throws Exception {

        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        reqDetails.getModelInfo().setModelVersionId("1234567");

        ServiceInstance si = new ServiceInstance();
        si.setServiceInstanceId("siId123");
        si.setModelVersionId("1234567");

        ServiceInstance si2 = new ServiceInstance();
        si2.setServiceInstanceId("siId222");
        si2.setModelVersionId("22222");
        si2.setServiceInstanceName("siName222");

        ServiceInstances serviceInstances = new ServiceInstances();
        serviceInstances.getServiceInstance().add(si);
        serviceInstances.getServiceInstance().add(si2);

        when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "siName"))
                .thenReturn(Optional.empty());
        when(bbSetupUtils.getAAIServiceInstancesGloballyByName("siName")).thenReturn(serviceInstances);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "serviceInstance with name (siName) and multiple combination of model-version-id + service-type + global-customer-id already exists. The name must be unique."));

        workflowAction.validateServiceResourceIdInAAI("generatedId123", "siName", reqDetails);
    }

    @Test
    public void validateNetworkResourceIdInAAITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(Optional.empty());
        when(bbSetupUtils.existsAAINetworksGloballyByName("name123")).thenReturn(false);

        String id = workflowAction.validateNetworkResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateNetworkResourceIdInAAISameModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        L3Network network = new L3Network();
        network.setNetworkId("id123");
        network.setNetworkName("name123");
        network.setModelCustomizationId("1234567");
        Optional<L3Network> opNetwork = Optional.of(network);

        when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("siId123", "name123")).thenReturn(opNetwork);

        String id = workflowAction.validateNetworkResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);
    }

    @Test
    public void validateNetworkResourceIdInAAIDuplicateNameTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        L3Network network = new L3Network();
        network.setNetworkId("id123");
        network.setNetworkName("name123");
        network.setModelCustomizationId("9999999");
        Optional<L3Network> opNetwork = Optional.of(network);

        when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("siId123", "name123")).thenReturn(opNetwork);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "l3Network with name (name123), same parent and different customization id (9999999) already exists. The name must be unique."));

        workflowAction.validateNetworkResourceIdInAAI("generatedId123", "name123", reqDetails, workflowResourceIds);
    }

    @Test
    public void validateNetworkResourceIdInAAINotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(Optional.empty());
        when(bbSetupUtils.existsAAINetworksGloballyByName("name123")).thenReturn(true);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "l3Network with name (name123) id (siId123) and different parent relationship already exists. The name must be unique."));

        workflowAction.validateNetworkResourceIdInAAI("generatedId123", "name123", reqDetails, workflowResourceIds);
    }

    @Test
    public void validateVnfResourceIdInAAITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123", "vnfName123")).thenReturn(Optional.empty());
        String id = workflowAction.validateVnfResourceIdInAAI("generatedId123", "vnfName123", reqDetails,
                workflowResourceIds);
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateVnfResourceIdInAAISameModelCustomizationIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        vnf.setVnfName("vnfName123");
        vnf.setModelCustomizationId("1234567");
        Optional<GenericVnf> opVnf = Optional.of(vnf);

        when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123", "vnfName123")).thenReturn(opVnf);
        String id = workflowAction.validateVnfResourceIdInAAI("generatedId123", "vnfName123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);
    }

    @Test
    public void validateVnfResourceIdInAAIDiffModelCustomizationIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        vnf.setVnfName("vnfName123");
        vnf.setModelCustomizationId("9999999");
        Optional<GenericVnf> opVnf = Optional.of(vnf);

        when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123", "vnfName123")).thenReturn(opVnf);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "generic-vnf with name (vnfName123), same parent and different customization id (9999999) already exists. The name must be unique."));

        workflowAction.validateVnfResourceIdInAAI("generatedId123", "vnfName123", reqDetails, workflowResourceIds);
    }

    @Test
    public void validateVnfResourceIdInAAINotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");


        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        vnf.setVnfName("vnfName123");
        GenericVnfs genericVnfs = new GenericVnfs();
        genericVnfs.getGenericVnf().add(vnf);

        when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123", "vnfName123")).thenReturn(Optional.empty());
        when(bbSetupUtils.getAAIVnfsGloballyByName("vnfName123")).thenReturn(genericVnfs);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "generic-vnf with name (vnfName123) id (id123) and different parent relationship already exists. The name must be unique."));

        workflowAction.validateVnfResourceIdInAAI("generatedId123", "vnfName123", reqDetails, workflowResourceIds);
    }

    @Test
    public void validateVfModuleResourceIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("vnfId123");

        when(bbSetupUtils.getAAIGenericVnf("id123")).thenReturn(null);
        when(bbSetupUtils.existsAAIVfModuleGloballyByName("name123")).thenReturn(false);

        String id = workflowAction.validateVfModuleResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateVfModuleResourceIdSameModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("vnfId123");

        VfModules vfModules = new VfModules();
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("id123");
        vfModule.setVfModuleName("name123");
        vfModule.setModelCustomizationId("1234567");
        vfModules.getVfModule().add(vfModule);

        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        vnf.setVnfName("vnfName123");
        vnf.setVfModules(vfModules);

        when(bbSetupUtils.getAAIGenericVnf("vnfId123")).thenReturn(vnf);

        String id = workflowAction.validateVfModuleResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);
    }

    @Test
    public void validateVfModuleResourceIdDifferentModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("vnfId123");

        VfModules vfModules = new VfModules();
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("id123");
        vfModule.setVfModuleName("name123");
        vfModule.setModelCustomizationId("9999999");
        vfModules.getVfModule().add(vfModule);

        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("id123");
        vnf.setVnfName("vnfName123");
        vnf.setVfModules(vfModules);

        when(bbSetupUtils.getAAIGenericVnf("vnfId123")).thenReturn(vnf);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "vfModule with name (name123), same parent and different customization id (1234567) already exists. The name must be unique."));

        workflowAction.validateVfModuleResourceIdInAAI("generatedId123", "name123", reqDetails, workflowResourceIds);

    }

    @Test
    public void validateVfModuleResourceIdNotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("vnfId123");

        when(bbSetupUtils.getAAIGenericVnf("id123")).thenReturn(null);
        when(bbSetupUtils.existsAAIVfModuleGloballyByName("name123")).thenReturn(true);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException
                .expectMessage(containsString("vfModule with name name123 already exists. The name must be unique."));

        workflowAction.validateVfModuleResourceIdInAAI("generatedId123", "name123", reqDetails, workflowResourceIds);
    }

    @Test
    public void validateVolumeGroupResourceIdInAAITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("vnfId123");

        when(bbSetupUtils.getRelatedVolumeGroupByNameFromVnf("id123", "name123")).thenReturn(Optional.empty());
        when(bbSetupUtils.existsAAIVolumeGroupGloballyByName("name123")).thenReturn(false);

        String id = workflowAction.validateVolumeGroupResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateVolumeGroupResourceIdInAAISameModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        workflowResourceIds.setVnfId("vnfId123");

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("id123");
        volumeGroup.setVolumeGroupName("name123");
        volumeGroup.setVfModuleModelCustomizationId("1234567");

        Optional<VolumeGroup> opVolumeGroup = Optional.of(volumeGroup);

        when(bbSetupUtils.getRelatedVolumeGroupByNameFromVnf("vnfId123", "name123")).thenReturn(opVolumeGroup);
        String id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "name123",
                reqDetails, workflowResourceIds);

        assertEquals("id123", id);
    }

    @Test
    public void validateVolumeGroupResourceIdInAAIDifferentModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");
        workflowResourceIds.setVnfId("vnfId123");

        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("id123");
        volumeGroup.setVolumeGroupName("name123");
        volumeGroup.setVfModuleModelCustomizationId("9999999");

        Optional<VolumeGroup> opVolumeGroup = Optional.of(volumeGroup);

        when(bbSetupUtils.getRelatedVolumeGroupByNameFromVnf("vnfId123", "name123")).thenReturn(opVolumeGroup);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("volumeGroup with name name123 already exists. The name must be unique."));

        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "name123", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateVolumeGroupResourceIdInAAINotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setVnfId("vnfId123");

        when(bbSetupUtils.getRelatedVolumeGroupByNameFromVnf("vnfId123", "name123")).thenReturn(Optional.empty());
        when(bbSetupUtils.existsAAIVolumeGroupGloballyByName("name123")).thenReturn(true);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("volumeGroup with name name123 already exists. The name must be unique."));

        workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "name123", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateConfigurationResourceIdInAAITest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        when(bbSetupUtils.getRelatedConfigurationByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(Optional.empty());
        when(bbSetupUtils.existsAAIConfigurationGloballyByName("name123")).thenReturn(false);

        String id = workflowAction.validateConfigurationResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
        assertEquals("generatedId123", id);
    }

    @Test
    public void validateConfigurationResourceIdInAAISameModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("id123");
        configuration.setConfigurationName("name123");
        configuration.setModelCustomizationId("1234567");
        Optional<org.onap.aai.domain.yang.Configuration> opConfiguration = Optional.of(configuration);

        when(bbSetupUtils.getRelatedConfigurationByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(opConfiguration);
        when(bbSetupUtils.existsAAIConfigurationGloballyByName("name123")).thenReturn(false);

        String id = workflowAction.validateConfigurationResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
        assertEquals("id123", id);
    }

    @Test
    public void validateConfigurationResourceIdInAAIDifferentModelCustIdTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("id123");
        configuration.setConfigurationName("name123");
        configuration.setModelCustomizationId("9999999");
        Optional<org.onap.aai.domain.yang.Configuration> opConfiguration = Optional.of(configuration);

        when(bbSetupUtils.getRelatedConfigurationByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(opConfiguration);
        when(bbSetupUtils.existsAAIConfigurationGloballyByName("name123")).thenReturn(false);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(containsString(
                "configuration with name (name123), same parent and different customization id (id123) already exists. The name must be unique."));

        workflowAction.validateConfigurationResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
    }

    @Test
    public void validateConfigurationResourceIdInAAINotGloballyUniqueTest() throws Exception {
        RequestDetails reqDetails = setupRequestDetails("id123", "subServiceType123", "1234567");
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("siId123");

        when(bbSetupUtils.getRelatedConfigurationByNameFromServiceInstance("siId123", "name123"))
                .thenReturn(Optional.empty());
        when(bbSetupUtils.existsAAIConfigurationGloballyByName("name123")).thenReturn(true);

        this.expectedException.expect(DuplicateNameException.class);
        this.expectedException.expectMessage(
                containsString("configuration with name name123 already exists. The name must be unique."));

        workflowAction.validateConfigurationResourceIdInAAI("generatedId123", "name123", reqDetails,
                workflowResourceIds);
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

    @Ignore
    @Test
    public void traverseCatalogDbServiceMultipleNetworkTest() throws IOException, VrfBondingServiceException {
        execution.setVariable("testProcessKey", "testProcessKeyValue");
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
        List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupCustomizations = new ArrayList<>();
        CollectionResourceInstanceGroupCustomization collectionInstanceGroupCustomization =
                new CollectionResourceInstanceGroupCustomization();
        collectionInstanceGroupCustomization.setSubInterfaceNetworkQuantity(3);
        collectionInstanceGroupCustomizations.add(collectionInstanceGroupCustomization);
        instanceGroup.setCollectionInstanceGroupCustomizations(collectionInstanceGroupCustomizations);
        collectionResource.setInstanceGroup(instanceGroup);
        collectionResourceCustomization.setCollectionResource(collectionResource);;
        service.setModelUUID("abc");
        service.getCollectionResourceCustomizations().add(collectionResourceCustomization);
        service.getCollectionResourceCustomizations().add(collectionResourceCustomization);
        doReturn(service).when(catalogDbClient).getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a");
        doReturn(collectionResourceCustomization).when(catalogDbClient)
                .getNetworkCollectionResourceCustomizationByID("123");
        String bpmnRequest = new String(Files
                .readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);
        List<Resource> resourceCounter = new ArrayList<>();
        thrown.expect(BpmnError.class);
        List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();
        workflowAction.traverseCatalogDbService(execution, sIRequest, resourceCounter, aaiResourceIds);
    }

    @Test
    public void sortVfModulesByBaseFirstTest() {
        List<Resource> resources = new ArrayList<>();
        Resource resource1 = new Resource(WorkflowType.VFMODULE, "111", false);
        resource1.setBaseVfModule(false);
        resources.add(resource1);
        Resource resource2 = new Resource(WorkflowType.VFMODULE, "222", false);
        resource2.setBaseVfModule(false);
        resources.add(resource2);
        Resource resource3 = new Resource(WorkflowType.VFMODULE, "333", false);
        resource3.setBaseVfModule(true);
        resources.add(resource3);
        List<Resource> result = workflowAction.sortVfModulesByBaseFirst(resources);
        assertEquals("333", result.get(0).getResourceId());
        assertEquals("222", result.get(1).getResourceId());
        assertEquals("111", result.get(2).getResourceId());
    }

    @Test
    public void sortVfModulesByBaseLastTest() {
        List<Resource> resources = new ArrayList<>();
        Resource resource1 = new Resource(WorkflowType.VFMODULE, "111", false);
        resource1.setBaseVfModule(true);
        resources.add(resource1);
        Resource resource2 = new Resource(WorkflowType.VFMODULE, "222", false);
        resource2.setBaseVfModule(false);
        resources.add(resource2);
        Resource resource3 = new Resource(WorkflowType.VFMODULE, "333", false);
        resource3.setBaseVfModule(false);
        resources.add(resource3);
        List<Resource> result = workflowAction.sortVfModulesByBaseLast(resources);
        assertEquals("333", result.get(0).getResourceId());
        assertEquals("222", result.get(1).getResourceId());
        assertEquals("111", result.get(2).getResourceId());
    }

    @Test
    public void findCatalogNetworkCollectionTest() {
        Service service = new Service();
        NetworkCollectionResourceCustomization networkCustomization = new NetworkCollectionResourceCustomization();
        networkCustomization.setModelCustomizationUUID("123");
        service.getCollectionResourceCustomizations().add(networkCustomization);
        doReturn(networkCustomization).when(catalogDbClient).getNetworkCollectionResourceCustomizationByID("123");
        CollectionResourceCustomization customization = workflowAction.findCatalogNetworkCollection(execution, service);
        assertNotNull(customization);
    }

    @Test
    public void findCatalogNetworkCollectionEmptyTest() {
        Service service = new Service();
        NetworkCollectionResourceCustomization networkCustomization = new NetworkCollectionResourceCustomization();
        networkCustomization.setModelCustomizationUUID("123");
        service.getCollectionResourceCustomizations().add(networkCustomization);
        CollectionResourceCustomization customization = workflowAction.findCatalogNetworkCollection(execution, service);
        assertNull(customization);
    }

    @Test
    public void findCatalogNetworkCollectionMoreThanOneTest() {
        Service service = new Service();
        NetworkCollectionResourceCustomization networkCustomization1 = new NetworkCollectionResourceCustomization();
        networkCustomization1.setModelCustomizationUUID("123");
        NetworkCollectionResourceCustomization networkCustomization2 = new NetworkCollectionResourceCustomization();
        networkCustomization2.setModelCustomizationUUID("321");
        service.getCollectionResourceCustomizations().add(networkCustomization1);
        service.getCollectionResourceCustomizations().add(networkCustomization2);
        doReturn(networkCustomization1).when(catalogDbClient).getNetworkCollectionResourceCustomizationByID("123");
        doReturn(networkCustomization2).when(catalogDbClient).getNetworkCollectionResourceCustomizationByID("321");
        workflowAction.findCatalogNetworkCollection(execution, service);
        assertEquals("Found multiple Network Collections in the Service model, only one per Service is supported.",
                execution.getVariable("WorkflowActionErrorMessage"));
    }

    @Test
    public void verifyLackOfNullPointerExceptionForNullResource() {
        ExecuteBuildingBlock result = null;
        try {
            result = workflowAction.buildExecuteBuildingBlock(new OrchestrationFlow(), null, null, null, null, null,
                    false, null, null, null, false, null, null, true);
        } catch (NullPointerException e) {
            fail("NullPointerException should not be thrown when 'resource' is null");
        }
        assertNotNull(result);
    }

    @Test
    public void traverseAAIServiceTest() {
        List<Resource> resourceCounter = new ArrayList<>();
        String resourceId = "si0";
        List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();

        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId(resourceId);

        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstance = setServiceInstance();
        setGenericVnf();
        setVfModule(true);
        setVolumeGroup();
        setL3Network();
        setCollection();
        setConfiguration();

        Configuration config = new Configuration();
        config.setConfigurationId("testConfigurationId2");
        serviceInstance.getConfigurations().add(config);

        Relationship relationship1 = new Relationship();
        relationship1.setRelatedTo("vnfc");
        RelationshipList relationshipList1 = new RelationshipList();
        relationshipList1.getRelationship().add(relationship1);

        Relationship relationship2 = new Relationship();
        relationship2.setRelatedTo("vpn-binding");
        RelationshipList relationshipList2 = new RelationshipList();
        relationshipList2.getRelationship().add(relationship2);

        org.onap.aai.domain.yang.Configuration aaiConfiguration1 = new org.onap.aai.domain.yang.Configuration();
        aaiConfiguration1.setConfigurationId("testConfigurationId");
        aaiConfiguration1.setRelationshipList(relationshipList1);

        org.onap.aai.domain.yang.Configuration aaiConfiguration2 = new org.onap.aai.domain.yang.Configuration();
        aaiConfiguration2.setConfigurationId("testConfigurationId2");
        aaiConfiguration2.setRelationshipList(relationshipList1);

        try {
            doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById(resourceId);
            doReturn(serviceInstance).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
            doReturn(Optional.of(aaiConfiguration1)).when(aaiConfigurationResources)
                    .getConfiguration("testConfigurationId");
            doReturn(Optional.of(aaiConfiguration2)).when(aaiConfigurationResources)
                    .getConfiguration("testConfigurationId2");
            workflowAction.traverseAAIService(execution, resourceCounter, resourceId, aaiResourceIds);
            assertEquals(8, resourceCounter.size());
            assertTrue(resourceCounter.get(2).isBaseVfModule());
            assertThat(aaiResourceIds, sameBeanAs(getExpectedResourceIds()));
        } catch (Exception e) {
            fail("Unexpected exception was thrown.");
        }
    }

    private List<Pair<WorkflowType, String>> getExpectedResourceIds() {
        List<Pair<WorkflowType, String>> resourceIds = new ArrayList<>();
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VNF, "testVnfId1"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VFMODULE, "testVfModuleId1"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VOLUMEGROUP, "testVolumeGroupId1"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.NETWORK, "testNetworkId1"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.NETWORKCOLLECTION, "testId"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.CONFIGURATION, "testConfigurationId"));
        resourceIds.add(new Pair<WorkflowType, String>(WorkflowType.CONFIGURATION, "testConfigurationId2"));
        return resourceIds;
    }

    private List<OrchestrationFlow> createFlowList(String... flowNames) {
        List<OrchestrationFlow> result = new ArrayList<>();
        for (String flowName : flowNames) {
            OrchestrationFlow orchFlow = new OrchestrationFlow();
            orchFlow.setFlowName(flowName);
            result.add(orchFlow);
        }
        return result;
    }

    private void assertEqualsBulkFlowName(List<ExecuteBuildingBlock> ebbs, String... flowNames) {
        for (int i = 0; i < ebbs.size(); i++) {
            assertEquals(ebbs.get(i).getBuildingBlock().getBpmnFlowName(), flowNames[i]);
        }
    }
}
