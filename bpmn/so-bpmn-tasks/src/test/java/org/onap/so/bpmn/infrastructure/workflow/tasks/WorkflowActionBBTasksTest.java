/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.aai.domain.yang.Configuration;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.InstanceGroup;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.listener.flowmanipulator.FlowManipulatorListenerRunner;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.db.catalog.beans.BuildingBlockRollback;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.springframework.core.env.Environment;

public class WorkflowActionBBTasksTest extends BaseTaskTest {

    private static final String SAMPLE_MSO_REQUEST_ID = "00f704ca-c5e5-4f95-a72c-6889db7b0688";
    private static final String SAMPLE_REQUEST_ACTION = "Delete-Network-Collection";
    private static final int SAMPLE_SEQUENCE = 0;
    private static final String EMPTY_STRING = "";
    @Mock
    protected WorkflowAction workflowAction;

    @Mock
    protected WorkflowActionBBFailure workflowActionBBFailure;

    @InjectMocks
    @Spy
    protected WorkflowActionBBTasks workflowActionBBTasks;

    @Mock
    InfraActiveRequests reqMock;

    private DelegateExecution execution;

    @Mock
    protected Environment environment;

    @Mock
    private FlowManipulatorListenerRunner flowManipulatorListenerRunner;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private DelegateExecution mockExecution;

    @Before
    public void initCatalogDBRollbackTable() {
        when(catalogDbClient.getBuildingBlockRollbackEntries()).thenReturn(getRollbackBuildingBlockList());
    }

    @Before
    public void before() throws Exception {
        execution = new DelegateExecutionFake();
        ServiceInstance servInstance = new ServiceInstance();
        servInstance.setServiceInstanceId("TEST");
        when(bbSetupUtils.getAAIServiceInstanceByName(anyString(), any())).thenReturn(servInstance);
        workflowAction.setBbInputSetupUtils(bbSetupUtils);
        workflowAction.setBbInputSetup(bbInputSetup);
    }

    @Test
    public void selectBBTest() {
        String vnfCustomizationUUID = "1234567";
        String modelUuid = "1234567";
        prepareDelegateExecution();
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();

        BuildingBlock buildingBlock =
                new BuildingBlock().setBpmnFlowName("ConfigAssignVnfBB").setKey(vnfCustomizationUUID);
        RequestDetails rd = new RequestDetails();
        ModelInfo mi = new ModelInfo();
        mi.setModelUuid(modelUuid);
        rd.setModelInfo(mi);
        ExecuteBuildingBlock ebb = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock).setRequestDetails(rd);
        flowsToExecute.add(ebb);

        List<VnfResourceCustomization> vnfResourceCustomizations = new ArrayList<>();
        VnfResourceCustomization vrc = new VnfResourceCustomization();
        vrc.setSkipPostInstConf(false);
        vrc.setModelCustomizationUUID(vnfCustomizationUUID);
        vnfResourceCustomizations.add(vrc);
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setModelCustomizationId(vnfCustomizationUUID);
        doReturn(vnfResourceCustomizations).when(catalogDbClient).getVnfResourceCustomizationByModelUuid(modelUuid);
        doReturn(vrc).when(catalogDbClient).findVnfResourceCustomizationInList(vnfCustomizationUUID,
                vnfResourceCustomizations);

        execution.setVariable("flowsToExecute", flowsToExecute);
        workflowActionBBTasks.selectBB(execution);
        boolean success = (boolean) execution.getVariable("completed");
        int currentSequence = (int) execution.getVariable("gCurrentSequence");
        assertFalse(success);
        assertEquals(1, currentSequence);
    }

    @Test
    public void select2BBTest() {
        String vnfCustomizationUUID = "1234567";
        String modelUuid = "1234567";

        prepareDelegateExecution();
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        BuildingBlock buildingBlock =
                new BuildingBlock().setBpmnFlowName("ConfigDeployVnfBB").setKey(vnfCustomizationUUID);
        RequestDetails rd = new RequestDetails();
        ModelInfo mi = new ModelInfo();
        mi.setModelUuid(modelUuid);
        rd.setModelInfo(mi);
        ExecuteBuildingBlock ebb = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock).setRequestDetails(rd);
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();

        flowsToExecute.add(ebb);

        List<VnfResourceCustomization> vnfResourceCustomizations = new ArrayList<>();
        VnfResourceCustomization vrc = new VnfResourceCustomization();

        vrc.setSkipPostInstConf(false);
        vrc.setModelCustomizationUUID(vnfCustomizationUUID);
        vnfResourceCustomizations.add(vrc);
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setModelCustomizationId(vnfCustomizationUUID);

        doReturn(vnfResourceCustomizations).when(catalogDbClient).getVnfResourceCustomizationByModelUuid(modelUuid);
        doReturn(vrc).when(catalogDbClient).findVnfResourceCustomizationInList(vnfCustomizationUUID,
                vnfResourceCustomizations);

        flowsToExecute.add(ebb2);
        execution.setVariable("flowsToExecute", flowsToExecute);
        workflowActionBBTasks.selectBB(execution);
        boolean success = (boolean) execution.getVariable("completed");
        int currentSequence = (int) execution.getVariable("gCurrentSequence");
        assertFalse(success);
        assertEquals(1, currentSequence);
    }

    @Test
    public void updateRequestStatusToCompleteTest() {
        String reqId = "reqId123";
        execution.setVariable("mso-request-id", reqId);
        execution.setVariable("requestAction", "createInstance");
        execution.setVariable("resourceName", "Service");
        execution.setVariable("aLaCarte", true);
        InfraActiveRequests req = new InfraActiveRequests();
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
        doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
        workflowActionBBTasks.updateRequestStatusToComplete(execution);
        assertEquals("ALaCarte-Service-createInstance request was executed correctly.",
                execution.getVariable("finalStatusMessage"));
    }

    @Test
    public void rollbackExecutionPathTest() {
        execution.setVariable("handlingCode", "Rollback");
        execution.setVariable("isRollback", false);
        execution.setVariable("requestAction", EMPTY_STRING);
        execution.setVariable("resourceName", EMPTY_STRING);
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("AssignVfModuleBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("CreateVfModuleBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 3);
        doNothing().when(workflowActionBBFailure).updateRequestErrorStatusMessage(isA(DelegateExecution.class));

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(), "DeactivateVfModuleBB");
        assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(), "DeleteVfModuleBB");
        assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(), "UnassignVfModuleBB");
        assertEquals(0, execution.getVariable("gCurrentSequence"));
    }

    @Test
    public void rollbackExecutionPathUnfinishedFlowTest() {
        execution.setVariable("handlingCode", "Rollback");
        execution.setVariable("isRollback", false);
        execution.setVariable("requestAction", EMPTY_STRING);
        execution.setVariable("resourceName", EMPTY_STRING);
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("AssignVfModuleBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("CreateVfModuleBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 2);
        doNothing().when(workflowActionBBFailure).updateRequestErrorStatusMessage(isA(DelegateExecution.class));

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(), "DeleteVfModuleBB");
        assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(), "UnassignVfModuleBB");
        assertEquals(0, execution.getVariable("gCurrentSequence"));
        assertEquals(0, execution.getVariable("retryCount"));
    }

    @Test
    public void rollbackExecutionTest() {
        execution.setVariable("handlingCode", "Rollback");
        execution.setVariable("isRollback", false);
        execution.setVariable("requestAction", EMPTY_STRING);
        execution.setVariable("resourceName", EMPTY_STRING);
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("AssignServiceInstanceBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("CreateNetworkCollectionBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("AssignNetworkBB");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        BuildingBlock buildingBlock4 = new BuildingBlock().setBpmnFlowName("CreateNetworkBB");
        ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock4);
        flowsToExecute.add(ebb4);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 3);
        doNothing().when(workflowActionBBFailure).updateRequestErrorStatusMessage(isA(DelegateExecution.class));

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(), "UnassignNetworkBB");
        assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(), "DeleteNetworkCollectionBB");
        assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(), "UnassignServiceInstanceBB");
        assertEquals(0, execution.getVariable("gCurrentSequence"));
    }

    @Test
    public void rollbackExecutionRollbackToAssignedTest() {
        execution.setVariable("isRollback", false);
        execution.setVariable("handlingCode", "RollbackToAssigned");
        execution.setVariable("requestAction", EMPTY_STRING);
        execution.setVariable("resourceName", EMPTY_STRING);
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();

        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("AssignVfModuleBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("CreateVfModuleBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 2);

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals("DeleteVfModuleBB", ebbs.get(0).getBuildingBlock().getBpmnFlowName());
        assertEquals(0, execution.getVariable("gCurrentSequence"));
        assertEquals(1, ebbs.size());
    }

    @Test
    public void rollbackExecutionPathChangeBBForReplaceVFModuleTest() {
        execution.setVariable("handlingCode", "Rollback");
        execution.setVariable("isRollback", false);
        execution.setVariable("requestAction", "replaceInstance");
        execution.setVariable("resourceName", "VfModule");
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("AssignVfModuleBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("CreateVfModuleBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);


        BuildingBlock buildingBlock4 = new BuildingBlock().setBpmnFlowName("ChangeModelVnfBB");
        ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock4);
        flowsToExecute.add(ebb4);

        BuildingBlock buildingBlock5 = new BuildingBlock().setBpmnFlowName("ChangeModelServiceInstanceBB");
        ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock5);
        flowsToExecute.add(ebb5);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 5);
        doNothing().when(workflowActionBBFailure).updateRequestErrorStatusMessage(isA(DelegateExecution.class));

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(), "ChangeModelVnfBB");
        assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(), "ChangeModelServiceInstanceBB");
        assertEquals(0, execution.getVariable("gCurrentSequence"));
    }

    @Test
    public void rollbackExecutionRollbackToAssignedWithFabricTest() {
        execution.setVariable("isRollback", false);
        execution.setVariable("handlingCode", "RollbackToAssigned");
        execution.setVariable("requestAction", EMPTY_STRING);
        execution.setVariable("resourceName", EMPTY_STRING);
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();

        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("AssignVfModuleBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("CreateVfModuleBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        BuildingBlock buildingBlock4 = new BuildingBlock().setBpmnFlowName("AddFabricConfigurationBB");
        ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock4);
        flowsToExecute.add(ebb4);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 4);

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals(0, execution.getVariable("gCurrentSequence"));
        assertEquals(3, ebbs.size());
        assertEquals("DeleteFabricConfigurationBB", ebbs.get(0).getBuildingBlock().getBpmnFlowName());
        assertEquals("DeactivateVfModuleBB", ebbs.get(1).getBuildingBlock().getBpmnFlowName());
        assertEquals("DeleteVfModuleBB", ebbs.get(2).getBuildingBlock().getBpmnFlowName());

    }

    @Test
    public void rollbackExecutionRollbackToCreatedWithFabricTest() {
        execution.setVariable("isRollback", false);
        execution.setVariable("handlingCode", "RollbackToCreated");
        execution.setVariable("requestAction", EMPTY_STRING);
        execution.setVariable("resourceName", EMPTY_STRING);
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();

        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("AssignVfModuleBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("CreateVfModuleBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        BuildingBlock buildingBlock4 = new BuildingBlock().setBpmnFlowName("AddFabricConfigurationBB");
        ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock4);
        flowsToExecute.add(ebb4);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 4);

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals(0, execution.getVariable("gCurrentSequence"));
        assertEquals(2, ebbs.size());
        assertEquals("DeleteFabricConfigurationBB", ebbs.get(0).getBuildingBlock().getBpmnFlowName());
        assertEquals("DeactivateVfModuleBB", ebbs.get(1).getBuildingBlock().getBpmnFlowName());

    }

    @Test
    public void rollbackExecutionRollbackToCreatedNoConfigurationWithFabricTest() {
        execution.setVariable("isRollback", false);
        execution.setVariable("handlingCode", "RollbackToCreatedNoConfiguration");
        execution.setVariable("requestAction", EMPTY_STRING);
        execution.setVariable("resourceName", EMPTY_STRING);
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();

        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("AssignVfModuleBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("CreateVfModuleBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        BuildingBlock buildingBlock4 = new BuildingBlock().setBpmnFlowName("AddFabricConfigurationBB");
        ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock4);
        flowsToExecute.add(ebb4);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 4);

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals(0, execution.getVariable("gCurrentSequence"));
        assertEquals(1, ebbs.size());
        assertEquals("DeactivateVfModuleBB", ebbs.get(0).getBuildingBlock().getBpmnFlowName());
    }

    @Test
    public void rollbackExecutionRollbackToCreatedTest() {
        execution.setVariable("isRollback", false);
        execution.setVariable("handlingCode", "RollbackToCreated");
        execution.setVariable("requestAction", EMPTY_STRING);
        execution.setVariable("resourceName", EMPTY_STRING);
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("AssignVfModuleBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("CreateVfModuleBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 3);

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals("DeactivateVfModuleBB", ebbs.get(0).getBuildingBlock().getBpmnFlowName());
        assertEquals(0, execution.getVariable("gCurrentSequence"));
        assertEquals(1, ebbs.size());
    }

    @Test
    public void rollbackExecutionRollbackInPlaceSoftwareUpdateTest() {
        execution.setVariable("isRollback", false);
        execution.setVariable("handlingCode", "Rollback");
        execution.setVariable("requestAction", EMPTY_STRING);
        execution.setVariable("resourceName", EMPTY_STRING);
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("VNFCheckPserversLockedFlagActivity");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("VNFCheckInMaintFlagActivity");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("VNFSetInMaintFlagActivity");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        BuildingBlock buildingBlock4 = new BuildingBlock().setBpmnFlowName("VNFCheckClosedLoopDisabledFlagActivity");
        ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock4);
        flowsToExecute.add(ebb4);

        BuildingBlock buildingBlock5 = new BuildingBlock().setBpmnFlowName("VNFSetClosedLoopDisabledFlagActivity");
        ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock5);
        flowsToExecute.add(ebb5);

        BuildingBlock buildingBlock6 = new BuildingBlock().setBpmnFlowName("VNFLockActivity");
        ExecuteBuildingBlock ebb6 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock6);
        flowsToExecute.add(ebb6);

        BuildingBlock buildingBlock7 = new BuildingBlock().setBpmnFlowName("VNFUpgradePreCheckActivity");
        ExecuteBuildingBlock ebb7 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock7);
        flowsToExecute.add(ebb7);

        BuildingBlock buildingBlock8 = new BuildingBlock().setBpmnFlowName("VNFQuiesceTrafficActivity");
        ExecuteBuildingBlock ebb8 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock8);
        flowsToExecute.add(ebb8);

        BuildingBlock buildingBlock9 = new BuildingBlock().setBpmnFlowName("VNFStopActivity");
        ExecuteBuildingBlock ebb9 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock9);
        flowsToExecute.add(ebb9);

        BuildingBlock buildingBlock10 = new BuildingBlock().setBpmnFlowName("VNFSnapShotActivity");
        ExecuteBuildingBlock ebb10 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock10);
        flowsToExecute.add(ebb10);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 10);

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals("VNFStartActivity", ebbs.get(0).getBuildingBlock().getBpmnFlowName());
        assertEquals("VNFResumeTrafficActivity", ebbs.get(1).getBuildingBlock().getBpmnFlowName());
        assertEquals("VNFUnlockActivity", ebbs.get(2).getBuildingBlock().getBpmnFlowName());
        assertEquals("VNFUnsetClosedLoopDisabledFlagActivity", ebbs.get(3).getBuildingBlock().getBpmnFlowName());
        assertEquals("VNFUnsetInMaintFlagActivity", ebbs.get(4).getBuildingBlock().getBpmnFlowName());
        assertEquals(0, execution.getVariable("gCurrentSequence"));
        assertEquals(5, ebbs.size());
    }

    @Test
    public void rollbackExecutionRollbackConfigModifyTest() {
        execution.setVariable("isRollback", false);
        execution.setVariable("handlingCode", "Rollback");
        execution.setVariable("requestAction", EMPTY_STRING);
        execution.setVariable("resourceName", EMPTY_STRING);
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("VNFCheckPserversLockedFlagActivity");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("VNFCheckInMaintFlagActivity");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("VNFSetInMaintFlagActivity");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        BuildingBlock buildingBlock4 = new BuildingBlock().setBpmnFlowName("VNFCheckClosedLoopDisabledFlagActivity");
        ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock4);
        flowsToExecute.add(ebb4);

        BuildingBlock buildingBlock5 = new BuildingBlock().setBpmnFlowName("VNFSetClosedLoopDisabledFlagActivity");
        ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock5);
        flowsToExecute.add(ebb5);

        BuildingBlock buildingBlock6 = new BuildingBlock().setBpmnFlowName("VNFHealthCheckActivity");
        ExecuteBuildingBlock ebb6 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock6);
        flowsToExecute.add(ebb6);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 6);

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals("VNFUnsetClosedLoopDisabledFlagActivity", ebbs.get(0).getBuildingBlock().getBpmnFlowName());
        assertEquals("VNFUnsetInMaintFlagActivity", ebbs.get(1).getBuildingBlock().getBpmnFlowName());
        assertEquals(0, execution.getVariable("gCurrentSequence"));
        assertEquals(2, ebbs.size());
    }

    @Test
    public void rollbackExecutionRollbackControllerExecutionBBTest() {
        execution.setVariable("isRollback", false);
        execution.setVariable("handlingCode", "Rollback");
        execution.setVariable("requestAction", EMPTY_STRING);
        execution.setVariable("resourceName", EMPTY_STRING);
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("AssignServiceInstanceBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("AssignNetworkBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("AssignVnfBB");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        BuildingBlock buildingBlock4 = new BuildingBlock().setBpmnFlowName("AssignVfModuleBB");
        ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock4);
        flowsToExecute.add(ebb4);

        BuildingBlock buildingBlock5 = new BuildingBlock().setBpmnFlowName("ControllerExecutionBB");
        buildingBlock5.setBpmnScope("vnf");
        buildingBlock5.setBpmnAction("config-assign");
        ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock5);
        flowsToExecute.add(ebb5);

        BuildingBlock buildingBlock6 = new BuildingBlock().setBpmnFlowName("CreateVfModuleBB");
        ExecuteBuildingBlock ebb6 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock6);
        flowsToExecute.add(ebb6);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 5);

        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        BuildingBlock bb = ebbs.get(0).getBuildingBlock();
        assertEquals("ControllerExecutionBB", bb.getBpmnFlowName());
        assertEquals("vnf", bb.getBpmnScope());
        assertEquals("config-unassign", bb.getBpmnAction());
        assertEquals(0, execution.getVariable("gCurrentSequence"));
        assertEquals(5, ebbs.size());
    }

    @Test
    public void postProcessingExecuteBBActivateVfModuleNotReplaceInstanceTest() {
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("1");
        workflowResourceIds.setVnfId("1");

        BuildingBlock bbActivateVfModule = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebbActivateVfModule = new ExecuteBuildingBlock().setBuildingBlock(bbActivateVfModule);
        ebbActivateVfModule.setWorkflowResourceIds(workflowResourceIds);
        ebbActivateVfModule.setResourceId("1");

        ServiceInstance service = new ServiceInstance();
        service.setServiceInstanceName("name");
        service.setModelVersionId("1");
        doReturn(service).when(bbSetupUtils).getAAIServiceInstanceById("1");

        GenericVnf vnf = new GenericVnf();
        vnf.setVnfName("name");
        vnf.setModelCustomizationId("1");
        doReturn(vnf).when(bbSetupUtils).getAAIGenericVnf("1");

        VfModule vfModule = new VfModule();
        vfModule.setVfModuleName("name");
        vfModule.setModelCustomizationId("1");
        doReturn(vfModule).when(bbSetupUtils).getAAIVfModule("1", "1");

        List<org.onap.aai.domain.yang.Vnfc> vnfcs = new ArrayList<org.onap.aai.domain.yang.Vnfc>();
        org.onap.aai.domain.yang.Vnfc vnfc = new org.onap.aai.domain.yang.Vnfc();
        vnfc.setModelInvariantId("1");
        vnfc.setVnfcName("name");
        vnfc.setModelCustomizationId("2");
        vnfcs.add(vnfc);
        doReturn(vnfcs).when(workflowAction).getRelatedResourcesInVfModule(any(), any(), any(), any());

        CvnfcConfigurationCustomization vfModuleCustomization = new CvnfcConfigurationCustomization();
        ConfigurationResource configuration = new ConfigurationResource();
        configuration.setToscaNodeType("FabricConfiguration");
        configuration.setModelUUID("1");
        vfModuleCustomization.setConfigurationResource(configuration);

        doReturn(vfModuleCustomization).when(catalogDbClient).getCvnfcCustomization("1", "1", "1", "2");

        prepareDelegateExecution();
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        flowsToExecute.add(ebbActivateVfModule);

        execution.setVariable("requestAction", "createInstance");
        execution.setVariable("completed", true);

        ArgumentCaptor<DelegateExecution> executionCaptor = ArgumentCaptor.forClass(DelegateExecution.class);
        ArgumentCaptor<ExecuteBuildingBlock> bbCaptor = ArgumentCaptor.forClass(ExecuteBuildingBlock.class);
        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        workflowActionBBTasks.postProcessingExecuteBBActivateVfModule(execution, ebbActivateVfModule, flowsToExecute);
        verify(workflowActionBBTasks, times(1)).postProcessingExecuteBBActivateVfModule(executionCaptor.capture(),
                bbCaptor.capture(), listCaptor.capture());
        assertEquals(false, executionCaptor.getAllValues().get(0).getVariable("completed"));
        assertEquals(2, ((ArrayList) executionCaptor.getAllValues().get(0).getVariable("flowsToExecute")).size());
        assertEquals("2",
                ((ExecuteBuildingBlock) ((ArrayList) executionCaptor.getAllValues().get(0)
                        .getVariable("flowsToExecute")).get(1)).getConfigurationResourceKeys()
                                .getCvnfcCustomizationUUID());
        assertEquals("AddFabricConfigurationBB", ((ExecuteBuildingBlock) ((ArrayList) executionCaptor.getAllValues()
                .get(0).getVariable("flowsToExecute")).get(1)).getBuildingBlock().getBpmnFlowName());
    }

    @Test
    public void postProcessingExecuteBBActivateVfModuleReplaceInstanceHasConfigurationTest() {
        RequestDetails reqDetails = new RequestDetails();
        RelatedInstanceList[] list = new RelatedInstanceList[2];
        RelatedInstanceList vnfList = new RelatedInstanceList();
        RelatedInstanceList serviceList = new RelatedInstanceList();
        list[0] = vnfList;
        list[1] = serviceList;
        RelatedInstance vnfInstance = new RelatedInstance();
        RelatedInstance serviceInstance = new RelatedInstance();
        ModelInfo vnfModelInfo = new ModelInfo();
        vnfModelInfo.setModelType(ModelType.vnf);
        vnfModelInfo.setModelCustomizationId("1");
        ModelInfo serviceModelInfo = new ModelInfo();
        serviceModelInfo.setModelType(ModelType.service);
        serviceModelInfo.setModelVersionId("1");
        vnfInstance.setModelInfo(vnfModelInfo);
        serviceInstance.setModelInfo(serviceModelInfo);
        reqDetails.setRelatedInstanceList(list);
        vnfList.setRelatedInstance(vnfInstance);
        serviceList.setRelatedInstance(serviceInstance);
        ModelInfo vfModuleInfo = new ModelInfo();
        vfModuleInfo.setModelCustomizationId("1");
        reqDetails.setModelInfo(vfModuleInfo);
        BuildingBlock bbAddFabric = new BuildingBlock().setBpmnFlowName("AddFabricConfigurationBB");
        ExecuteBuildingBlock ebbAddFabric = new ExecuteBuildingBlock().setBuildingBlock(bbAddFabric);
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("1");
        workflowResourceIds.setVnfId("1");
        ebbAddFabric.setWorkflowResourceIds(workflowResourceIds);
        ebbAddFabric.setResourceId("1");

        BuildingBlock bbActivateVfModule = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebbActivateVfModule = new ExecuteBuildingBlock().setBuildingBlock(bbActivateVfModule);
        ebbActivateVfModule.setWorkflowResourceIds(workflowResourceIds);
        ebbActivateVfModule.setResourceId("1");
        ConfigurationResourceKeys configurationResourceKeys = new ConfigurationResourceKeys();
        ebbAddFabric.setConfigurationResourceKeys(configurationResourceKeys);
        ebbActivateVfModule.setRequestDetails(reqDetails);

        ServiceInstance service = new ServiceInstance();
        service.setServiceInstanceName("name");
        service.setModelVersionId("1");
        doReturn(service).when(bbSetupUtils).getAAIServiceInstanceById("1");

        GenericVnf vnf = new GenericVnf();
        vnf.setVnfName("name");
        vnf.setModelCustomizationId("1");
        doReturn(vnf).when(bbSetupUtils).getAAIGenericVnf("1");

        VfModule vfModule = new VfModule();
        vfModule.setVfModuleName("name");
        vfModule.setModelCustomizationId("1");
        doReturn(vfModule).when(bbSetupUtils).getAAIVfModule("1", "1");

        List<org.onap.aai.domain.yang.Vnfc> vnfcs = new ArrayList<org.onap.aai.domain.yang.Vnfc>();
        org.onap.aai.domain.yang.Vnfc vnfc = new org.onap.aai.domain.yang.Vnfc();
        vnfc.setModelInvariantId("1");
        vnfc.setVnfcName("name");
        vnfc.setModelCustomizationId("2");
        vnfcs.add(vnfc);
        doReturn(vnfcs).when(workflowAction).getRelatedResourcesInVfModule(any(), any(), any(), any());

        CvnfcConfigurationCustomization vfModuleCustomization = new CvnfcConfigurationCustomization();
        ConfigurationResource configuration = new ConfigurationResource();
        configuration.setToscaNodeType("FabricConfiguration");
        configuration.setModelUUID("1");
        vfModuleCustomization.setConfigurationResource(configuration);

        doReturn(vfModuleCustomization).when(catalogDbClient).getCvnfcCustomization("1", "1", "1", "2");

        prepareDelegateExecution();
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        flowsToExecute.add(ebbActivateVfModule);

        ArgumentCaptor<DelegateExecution> executionCaptor = ArgumentCaptor.forClass(DelegateExecution.class);
        ArgumentCaptor<ExecuteBuildingBlock> bbCaptor = ArgumentCaptor.forClass(ExecuteBuildingBlock.class);
        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);

        execution.setVariable("requestAction", "replaceInstance");
        execution.setVariable("completed", true);
        workflowActionBBTasks.postProcessingExecuteBBActivateVfModule(execution, ebbActivateVfModule, flowsToExecute);
        verify(workflowActionBBTasks, times(1)).postProcessingExecuteBBActivateVfModule(executionCaptor.capture(),
                bbCaptor.capture(), listCaptor.capture());
        assertEquals(false, executionCaptor.getAllValues().get(0).getVariable("completed"));
        assertEquals(2, ((ArrayList) executionCaptor.getAllValues().get(0).getVariable("flowsToExecute")).size());
        assertEquals("2",
                ((ExecuteBuildingBlock) ((ArrayList) executionCaptor.getAllValues().get(0)
                        .getVariable("flowsToExecute")).get(1)).getConfigurationResourceKeys()
                                .getCvnfcCustomizationUUID());
    }

    @Test
    public void postProcessingExecuteBBActivateVfModuleReplaceInstanceHasNoConfigurationTest() {

        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId("1");
        workflowResourceIds.setVnfId("1");

        BuildingBlock bbActivateVfModule = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebbActivateVfModule = new ExecuteBuildingBlock().setBuildingBlock(bbActivateVfModule);
        ebbActivateVfModule.setWorkflowResourceIds(workflowResourceIds);
        ebbActivateVfModule.setResourceId("1");

        ServiceInstance service = new ServiceInstance();
        service.setServiceInstanceName("name");
        service.setModelVersionId("1");
        doReturn(service).when(bbSetupUtils).getAAIServiceInstanceById("1");

        GenericVnf vnf = new GenericVnf();
        vnf.setVnfName("name");
        vnf.setModelCustomizationId("1");
        doReturn(vnf).when(bbSetupUtils).getAAIGenericVnf("1");

        VfModule vfModule = new VfModule();
        vfModule.setVfModuleName("name");
        vfModule.setModelCustomizationId("1");
        doReturn(vfModule).when(bbSetupUtils).getAAIVfModule("1", "1");

        List<org.onap.aai.domain.yang.Vnfc> vnfcs = new ArrayList<org.onap.aai.domain.yang.Vnfc>();
        org.onap.aai.domain.yang.Vnfc vnfc = new org.onap.aai.domain.yang.Vnfc();
        vnfc.setModelInvariantId("1");
        vnfc.setVnfcName("name");
        vnfc.setModelCustomizationId("2");
        vnfcs.add(vnfc);
        doReturn(vnfcs).when(workflowAction).getRelatedResourcesInVfModule(any(), any(), any(), any());

        CvnfcConfigurationCustomization vfModuleCustomization = new CvnfcConfigurationCustomization();
        ConfigurationResource configuration = new ConfigurationResource();
        configuration.setToscaNodeType("FabricConfiguration");
        configuration.setModelUUID("1");
        vfModuleCustomization.setConfigurationResource(configuration);

        doReturn(vfModuleCustomization).when(catalogDbClient).getCvnfcCustomization("1", "1", "1", "2");

        prepareDelegateExecution();
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        flowsToExecute.add(ebbActivateVfModule);

        ArgumentCaptor<DelegateExecution> executionCaptor = ArgumentCaptor.forClass(DelegateExecution.class);
        ArgumentCaptor<ExecuteBuildingBlock> bbCaptor = ArgumentCaptor.forClass(ExecuteBuildingBlock.class);
        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);

        execution.setVariable("requestAction", "replaceInstance");
        execution.setVariable("completed", true);

        workflowActionBBTasks.postProcessingExecuteBBActivateVfModule(execution, ebbActivateVfModule, flowsToExecute);
        verify(workflowActionBBTasks, times(1)).postProcessingExecuteBBActivateVfModule(executionCaptor.capture(),
                bbCaptor.capture(), listCaptor.capture());
        assertEquals(true, executionCaptor.getAllValues().get(0).getVariable("completed"));
    }



    @Test
    public void getExecuteBBForConfigTest() {
        BuildingBlock bbActivateVfModule = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebbActivateVfModule = new ExecuteBuildingBlock().setBuildingBlock(bbActivateVfModule);

        WorkflowResourceIds resourceIdsActivateVfModule = new WorkflowResourceIds();
        resourceIdsActivateVfModule.setServiceInstanceId("test-service-inbstance-id");
        resourceIdsActivateVfModule.setVnfId("test-vnf-id");
        resourceIdsActivateVfModule.setVfModuleId("test-vf-module-id");
        resourceIdsActivateVfModule.setConfigurationId("");

        RequestDetails requestDetails = new RequestDetails();

        ebbActivateVfModule.setApiVersion("7");
        ebbActivateVfModule.setaLaCarte(true);
        ebbActivateVfModule.setRequestAction("createInstance");
        ebbActivateVfModule.setVnfType("test-vnf-type");
        ebbActivateVfModule.setRequestId("f6c00ae2-a205-4cbd-b055-02e553efde12");
        ebbActivateVfModule.setRequestDetails(requestDetails);
        ebbActivateVfModule.setWorkflowResourceIds(resourceIdsActivateVfModule);

        ConfigurationResourceKeys configurationResourceKeys = new ConfigurationResourceKeys();
        configurationResourceKeys.setCvnfcCustomizationUUID("07d64cd2-4427-4156-b11d-d14b96b3e4cb");
        configurationResourceKeys.setVfModuleCustomizationUUID("50b61075-6ebb-4aab-a9fc-bedad9a2aa76");
        configurationResourceKeys.setVnfResourceCustomizationUUID("a1d0e36e-34a9-431b-b5ba-4bbb72f63c7c");
        configurationResourceKeys.setVnfcName("rdm54bvbgw5001vm018pim001");

        ExecuteBuildingBlock ebbAddFabricConfig =
                workflowActionBBTasks.getExecuteBBForConfig("AddFabricConfigurationBB", ebbActivateVfModule,
                        "cc7e12f9-967c-4362-8d14-e5b2bf0608a4", configurationResourceKeys);

        assertEquals("7", ebbAddFabricConfig.getApiVersion());
        assertTrue(ebbAddFabricConfig.isaLaCarte());
        assertEquals("createInstance", ebbAddFabricConfig.getRequestAction());
        assertEquals("test-vnf-type", ebbAddFabricConfig.getVnfType());
        assertEquals("f6c00ae2-a205-4cbd-b055-02e553efde12", ebbAddFabricConfig.getRequestId());
        assertEquals(requestDetails, ebbAddFabricConfig.getRequestDetails());
        assertEquals("cc7e12f9-967c-4362-8d14-e5b2bf0608a4",
                ebbAddFabricConfig.getWorkflowResourceIds().getConfigurationId());
        assertEquals("test-service-inbstance-id", ebbAddFabricConfig.getWorkflowResourceIds().getServiceInstanceId());
        assertEquals("test-vnf-id", ebbAddFabricConfig.getWorkflowResourceIds().getVnfId());
        assertEquals("test-vf-module-id", ebbAddFabricConfig.getWorkflowResourceIds().getVfModuleId());

        assertThat(ebbAddFabricConfig.getConfigurationResourceKeys()).isEqualTo(configurationResourceKeys);
        assertThat(ebbAddFabricConfig.getWorkflowResourceIds())
                .isNotEqualTo(ebbActivateVfModule.getWorkflowResourceIds());
        assertThat(ebbAddFabricConfig.getWorkflowResourceIds().getConfigurationId())
                .isNotEqualTo(ebbActivateVfModule.getWorkflowResourceIds().getConfigurationId());
    }

    @Test
    public void checkRetryStatusTest() {
        String reqId = "reqId123";
        execution.setVariable("mso-request-id", reqId);
        doNothing().when(workflowActionBBFailure).updateRequestErrorStatusMessage(isA(DelegateExecution.class));
        doReturn("6").when(environment).getProperty("mso.rainyDay.maxRetries");
        execution.setVariable("handlingCode", "Retry");
        execution.setVariable("retryCount", 1);
        execution.setVariable("gCurrentSequence", 1);
        InfraActiveRequests req = new InfraActiveRequests();
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
        workflowActionBBTasks.checkRetryStatus(execution);
        assertEquals(0, execution.getVariable("gCurrentSequence"));
    }

    @Test
    public void checkRetryStatusTestExceededMaxRetries() {
        String reqId = "reqId123";
        execution.setVariable("mso-request-id", reqId);
        doNothing().when(workflowActionBBFailure).updateRequestErrorStatusMessage(isA(DelegateExecution.class));
        doReturn("6").when(environment).getProperty("mso.rainyDay.maxRetries");
        execution.setVariable("handlingCode", "Retry");
        execution.setVariable("retryCount", 6);
        execution.setVariable("gCurrentSequence", 1);
        InfraActiveRequests req = new InfraActiveRequests();
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
        try {
            workflowActionBBTasks.checkRetryStatus(execution);
        } catch (BpmnError e) {
            WorkflowException exception = (WorkflowException) execution.getVariable("WorkflowException");
            assertEquals("Exceeded maximum retries. Ending flow with status Abort", exception.getErrorMessage());
        }
    }

    @Test
    public void checkRetryStatusNoRetryTest() {
        String reqId = "reqId123";
        execution.setVariable("mso-request-id", reqId);
        execution.setVariable("retryCount", 3);
        execution.setVariable("handlingCode", "Success");
        execution.setVariable("gCurrentSequence", 1);
        InfraActiveRequests req = new InfraActiveRequests();
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
        workflowActionBBTasks.checkRetryStatus(execution);
        assertEquals(0, execution.getVariable("retryCount"));
    }

    @Test
    public void updateInstanceId() {
        String reqId = "req123";
        String instanceId = "123123123";
        execution.setVariable("mso-request-id", reqId);
        execution.setVariable("resourceId", instanceId);
        execution.setVariable("resourceType", WorkflowType.SERVICE);
        doReturn(reqMock).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
        doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
        workflowActionBBTasks.updateInstanceId(execution);
        Mockito.verify(reqMock, Mockito.times(1)).setServiceInstanceId(instanceId);
    }

    @Test
    public void getConfigurationId() throws Exception {
        org.onap.aai.domain.yang.Vnfc vnfc = new org.onap.aai.domain.yang.Vnfc();
        vnfc.setModelInvariantId("modelInvariantId");
        vnfc.setVnfcName("testVnfcName");
        List<Configuration> configurations = new ArrayList<>();
        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("configurationId");
        configuration.setModelCustomizationId("modelCustimizationId");
        configuration.setConfigurationName("testConfigurationName");
        configurations.add(configuration);
        doReturn(configurations.get(0).getConfigurationId()).when(workflowActionBBTasks).getConfigurationId(vnfc);
        assertEquals(workflowActionBBTasks.getConfigurationId(vnfc), "configurationId");
    }

    @Test
    public void setServiceInstanceNameTest() {
        String resourceId = "40bc4ebd-11df-4610-8055-059f7441ec1c";
        WorkflowType resourceType = WorkflowType.SERVICE;
        InfraActiveRequests request = new InfraActiveRequests();
        ServiceInstance service = new ServiceInstance();
        service.setServiceInstanceName("serviceInstanceName");
        doReturn(service).when(bbSetupUtils).getAAIServiceInstanceById(resourceId);

        workflowActionBBTasks.setInstanceName(resourceId, resourceType, request);
        assertEquals("serviceInstanceName", request.getServiceInstanceName());
    }

    @Test
    public void setVnfNameTest() {
        String resourceId = "40bc4ebd-11df-4610-8055-059f7441ec1c";
        WorkflowType resourceType = WorkflowType.VNF;
        InfraActiveRequests request = new InfraActiveRequests();
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfName("vnfName");
        doReturn(vnf).when(bbSetupUtils).getAAIGenericVnf(resourceId);

        workflowActionBBTasks.setInstanceName(resourceId, resourceType, request);
        assertEquals("vnfName", request.getVnfName());
    }

    @Test
    public void setVfModuleNameTest() {
        String resourceId = "40bc4ebd-11df-4610-8055-059f7441ec1c";
        WorkflowType resourceType = WorkflowType.VFMODULE;
        InfraActiveRequests request = new InfraActiveRequests();
        request.setVnfId("ae5cc3e8-c13c-4d88-aaf6-694ab4977b0e");
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleName("vfModuleName");
        doReturn(vfModule).when(bbSetupUtils).getAAIVfModule("ae5cc3e8-c13c-4d88-aaf6-694ab4977b0e", resourceId);

        workflowActionBBTasks.setInstanceName(resourceId, resourceType, request);
        assertEquals("vfModuleName", request.getVfModuleName());
    }

    @Test
    public void setNetworkNameTest() {
        String resourceId = "40bc4ebd-11df-4610-8055-059f7441ec1c";
        WorkflowType resourceType = WorkflowType.NETWORK;
        InfraActiveRequests request = new InfraActiveRequests();
        L3Network network = new L3Network();
        network.setNetworkName("networkName");
        doReturn(network).when(bbSetupUtils).getAAIL3Network(resourceId);

        workflowActionBBTasks.setInstanceName(resourceId, resourceType, request);
        assertEquals("networkName", request.getNetworkName());
    }

    @Test
    public void setConfigurationNameTest() {
        String resourceId = "40bc4ebd-11df-4610-8055-059f7441ec1c";
        WorkflowType resourceType = WorkflowType.CONFIGURATION;
        InfraActiveRequests request = new InfraActiveRequests();
        Configuration configuration = new Configuration();
        configuration.setConfigurationName("configurationName");
        doReturn(configuration).when(bbSetupUtils).getAAIConfiguration(resourceId);

        workflowActionBBTasks.setInstanceName(resourceId, resourceType, request);
        assertEquals("configurationName", request.getConfigurationName());
    }

    @Test
    public void setInstanceGroupNameTest() {
        String resourceId = "40bc4ebd-11df-4610-8055-059f7441ec1c";
        WorkflowType resourceType = WorkflowType.INSTANCE_GROUP;
        InfraActiveRequests request = new InfraActiveRequests();
        InstanceGroup instanceGroup = new InstanceGroup();
        instanceGroup.setInstanceGroupName("instanceGroupName");
        doReturn(instanceGroup).when(bbSetupUtils).getAAIInstanceGroup(resourceId);

        workflowActionBBTasks.setInstanceName(resourceId, resourceType, request);
        assertEquals("instanceGroupName", request.getInstanceGroupName());
    }

    @Test
    public void setVolumeGroupNameTest() {
        String resourceId = "40bc4ebd-11df-4610-8055-059f7441ec1c";
        WorkflowType resourceType = WorkflowType.VOLUMEGROUP;
        InfraActiveRequests request = new InfraActiveRequests();
        request.setVnfId("4aa72c90-21eb-4465-8847-997e27af6c3e");
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupName("volumeGroupName");
        Optional<VolumeGroup> returnVolumeGroup = Optional.of(volumeGroup);

        doReturn(returnVolumeGroup).when(bbSetupUtils).getRelatedVolumeGroupByIdFromVnf(request.getVnfId(), resourceId);
        workflowActionBBTasks.setInstanceName(resourceId, resourceType, request);

        assertEquals("volumeGroupName", request.getVolumeGroupName());
    }

    private void prepareDelegateExecution() {
        execution.setVariable("mso-request-id", SAMPLE_MSO_REQUEST_ID);
        execution.setVariable("requestAction", SAMPLE_REQUEST_ACTION);
        execution.setVariable("gCurrentSequence", SAMPLE_SEQUENCE);
        execution.setVariable("homing", false);
        execution.setVariable("calledHoming", false);
    }

    private List<BuildingBlockRollback> getRollbackBuildingBlockList() {
        List<BuildingBlockRollback> rollbackBBList = Collections.unmodifiableList(Arrays.asList(
                new BuildingBlockRollback(1, "ActivateNetworkBB", null, "DeactivateNetworkBB", null),
                new BuildingBlockRollback(2, "ActivatePnfBB", null, "DeactivatePnfBB", null),
                new BuildingBlockRollback(3, "ActivateServiceInstanceBB", null, "DeactivateServiceInstanceBB", null),
                new BuildingBlockRollback(4, "ActivateVfModuleBB", null, "DeactivateVfModuleBB", null),
                new BuildingBlockRollback(5, "ActivateVnfBB", null, "DeactivateVnfBB", null),
                new BuildingBlockRollback(6, "ActivateVolumeGroupBB", null, "DeactivateVolumeGroupBB", null),
                new BuildingBlockRollback(7, "AssignNetworkBB", null, "UnassignNetworkBB", null),
                new BuildingBlockRollback(8, "AssignServiceInstanceBB", null, "UnassignServiceInstanceBB", null),
                new BuildingBlockRollback(9, "AssignVfModuleBB", null, "UnassignVfModuleBB", null),
                new BuildingBlockRollback(10, "AssignVnfBB", null, "UnassignVnfBB", null),
                new BuildingBlockRollback(11, "AssignVolumeGroupBB", null, "UnassignVolumeGroupBB", null),
                new BuildingBlockRollback(12, "ControllerExecutionBB", "config-assign", "ControllerExecutionBB",
                        "config-unassign"),
                new BuildingBlockRollback(13, "ControllerExecutionBB", "config-deploy", "ControllerExecutionBB",
                        "config-undeploy"),
                new BuildingBlockRollback(14, "ControllerExecutionBB", "service-config-deploy", "ControllerExecutionBB",
                        "service-config-undeploy"),
                new BuildingBlockRollback(15, "CreateNetworkBB", null, "DeleteNetworkBB", null),
                new BuildingBlockRollback(16, "CreateNetworkCollectionBB", null, "DeleteNetworkCollectionBB", null),
                new BuildingBlockRollback(17, "CreateVfModuleBB", null, "DeleteVfModuleBB", null),
                new BuildingBlockRollback(18, "CreateVolumeGroupBB", null, "DeleteVolumeGroupBB", null),
                new BuildingBlockRollback(19, "VNFSetInMaintFlagActivity", null, "VNFUnsetInMaintFlagActivity", null),
                new BuildingBlockRollback(20, "VNFSetClosedLoopDisabledFlagActivity", null,
                        "VNFUnsetClosedLoopDisabledFlagActivity", null),
                new BuildingBlockRollback(21, "VNFLockActivity", null, "VNFUnlockActivity", null),
                new BuildingBlockRollback(22, "VNFStopActivity", null, "VNFStartActivity", null),
                new BuildingBlockRollback(23, "VNFQuiesceTrafficActivity", null, "VNFResumeTrafficActivity", null),
                new BuildingBlockRollback(24, "EtsiVnfInstantiateBB", null, "EtsiVnfDeleteBB", null),
                // AddFabricConfigurationBB this does not seem to be present as a bpmn in Guilin
                new BuildingBlockRollback(25, "AddFabricConfigurationBB", null, "DeleteFabricConfigurationBB", null)));
        return rollbackBBList;
    }

}
