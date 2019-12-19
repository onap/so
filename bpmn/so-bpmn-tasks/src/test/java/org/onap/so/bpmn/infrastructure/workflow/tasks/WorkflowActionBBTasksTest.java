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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.listener.flowmanipulator.FlowManipulatorListenerRunner;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.springframework.core.env.Environment;

public class WorkflowActionBBTasksTest extends BaseTaskTest {

    private static final String SAMPLE_MSO_REQUEST_ID = "00f704ca-c5e5-4f95-a72c-6889db7b0688";
    private static final String SAMPLE_REQUEST_ACTION = "Delete-Network-Collection";
    private static final int SAMPLE_SEQUENCE = 0;
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

    @Before
    public void before() throws Exception {
        execution = new DelegateExecutionFake();
        ServiceInstance servInstance = new ServiceInstance();
        servInstance.setServiceInstanceId("TEST");
        when(bbSetupUtils.getAAIServiceInstanceByName(anyString(), anyObject())).thenReturn(servInstance);
        workflowAction.setBbInputSetupUtils(bbSetupUtils);
        workflowAction.setBbInputSetup(bbInputSetup);
    }

    @Test
    public void selectBBTest() {
        String vnfCustomizationUUID = "1234567";
        String modelUuid = "1234567";
        prepareDelegateExecution();
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();

        BuildingBlock buildingBlock =
                new BuildingBlock().setBpmnFlowName("ConfigAssignVnfBB").setKey(vnfCustomizationUUID);
        RequestDetails rd = new RequestDetails();
        ModelInfo mi = new ModelInfo();
        mi.setModelUuid(modelUuid);
        rd.setModelInfo(mi);
        ExecuteBuildingBlock ebb = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock).setRequestDetails(rd);
        flowsToExecute.add(ebb);

        List<VnfResourceCustomization> vnfResourceCustomizations = new ArrayList();
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
        assertTrue(success);
        assertEquals(1, currentSequence);
    }

    private void prepareDelegateExecution() {
        prepareDelegateExecution(SAMPLE_MSO_REQUEST_ID, SAMPLE_REQUEST_ACTION, SAMPLE_SEQUENCE, false, false);
    }

    private void prepareDelegateExecution(String msoRequestId, String requestAction, int currentSequence,
            Boolean enableHoming, Boolean callHoming) {
        execution.setVariable("mso-request-id", msoRequestId);
        execution.setVariable("requestAction", requestAction);
        execution.setVariable("gCurrentSequence", currentSequence);
        execution.setVariable("homing", enableHoming);
        execution.setVariable("calledHoming", callHoming);
    }

    @Test
    public void select2BBTest() {
        String vnfCustomizationUUID = "1234567";
        String modelUuid = "1234567";

        prepareDelegateExecution();
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
        BuildingBlock buildingBlock =
                new BuildingBlock().setBpmnFlowName("ConfigDeployVnfBB").setKey(vnfCustomizationUUID);
        RequestDetails rd = new RequestDetails();
        ModelInfo mi = new ModelInfo();
        mi.setModelUuid(modelUuid);
        rd.setModelInfo(mi);
        ExecuteBuildingBlock ebb = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock).setRequestDetails(rd);
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();

        flowsToExecute.add(ebb);

        List<VnfResourceCustomization> vnfResourceCustomizations = new ArrayList();
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
        assertEquals(false, success);
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
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
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
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
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
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
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
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();

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
    public void rollbackExecutionRollbackToAssignedWithFabricTest() {
        execution.setVariable("isRollback", false);
        execution.setVariable("handlingCode", "RollbackToAssigned");
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();

        BuildingBlock buildingBlock1 = new BuildingBlock().setBpmnFlowName("AssignVfModuleBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock1);
        flowsToExecute.add(ebb1);

        BuildingBlock buildingBlock2 = new BuildingBlock().setBpmnFlowName("CreateVfModuleBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock2);
        flowsToExecute.add(ebb2);

        BuildingBlock buildingBlock3 = new BuildingBlock().setBpmnFlowName("ActivateVfModuleBB");
        ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock3);
        flowsToExecute.add(ebb3);

        BuildingBlock buildingBlock4 = new BuildingBlock().setBpmnFlowName("AssignFabricConfigurationBB");
        ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock4);
        flowsToExecute.add(ebb4);

        BuildingBlock buildingBlock5 = new BuildingBlock().setBpmnFlowName("ActivateFabricConfigurationBB");
        ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock5);
        flowsToExecute.add(ebb5);

        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("gCurrentSequence", 5);


        workflowActionBBTasks.rollbackExecutionPath(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEquals(0, execution.getVariable("gCurrentSequence"));
        assertEquals(4, ebbs.size());
        assertEquals("DeactivateFabricConfigurationBB", ebbs.get(0).getBuildingBlock().getBpmnFlowName());
        assertEquals("UnassignFabricConfigurationBB", ebbs.get(1).getBuildingBlock().getBpmnFlowName());
        assertEquals("DeactivateVfModuleBB", ebbs.get(2).getBuildingBlock().getBpmnFlowName());
        assertEquals("DeleteVfModuleBB", ebbs.get(3).getBuildingBlock().getBpmnFlowName());

    }

    @Test
    public void rollbackExecutionRollbackToCreatedTest() {
        execution.setVariable("isRollback", false);
        execution.setVariable("handlingCode", "RollbackToCreated");
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
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
    public void getConfigurationId() {
        org.onap.aai.domain.yang.Vnfc vnfc = new org.onap.aai.domain.yang.Vnfc();
        vnfc.setModelInvariantId("modelInvariantId");
        vnfc.setVnfcName("testVnfcName");
        List<org.onap.aai.domain.yang.Configuration> configurations =
                new ArrayList<org.onap.aai.domain.yang.Configuration>();
        org.onap.aai.domain.yang.Configuration configuration = new org.onap.aai.domain.yang.Configuration();
        configuration.setConfigurationId("configurationId");
        configuration.setModelCustomizationId("modelCustimizationId");
        configuration.setConfigurationName("testConfigurationName");
        configurations.add(configuration);
        doReturn(configurations.get(0).getConfigurationId()).when(workflowActionBBTasks).getConfigurationId(vnfc);
        assertEquals(workflowActionBBTasks.getConfigurationId(vnfc), "configurationId");
    }


}
