/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import java.sql.Timestamp;
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
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;

public class WorkflowActionBBFailureTest extends BaseTaskTest {

    @Mock
    protected WorkflowAction workflowAction;

    @InjectMocks
    @Spy
    protected WorkflowActionBBFailure workflowActionBBFailure;

    @Mock
    InfraActiveRequests reqMock;

    private DelegateExecution execution;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() throws Exception {
        execution = new DelegateExecutionFake();
        org.onap.aai.domain.yang.ServiceInstance servInstance = new org.onap.aai.domain.yang.ServiceInstance();
        servInstance.setServiceInstanceId("TEST");
        when(bbSetupUtils.getAAIServiceInstanceByName(anyString(), isA(Customer.class))).thenReturn(servInstance);
        workflowAction.setBbInputSetupUtils(bbSetupUtils);
        workflowAction.setBbInputSetup(bbInputSetup);
    }

    @Test
    public void updateRequestStatusToFailed_Null_Rollback() {
        String reqId = "reqId123";
        execution.setVariable("mso-request-id", reqId);
        execution.setVariable("retryCount", 3);
        execution.setVariable("handlingCode", "Success");
        execution.setVariable("gCurrentSequence", 1);
        WorkflowException we = new WorkflowException("WorkflowAction", 1231, "Error Case");
        execution.setVariable("WorkflowException", we);

        doReturn(reqMock).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
        workflowActionBBFailure.updateRequestStatusToFailed(execution);
        Mockito.verify(reqMock, Mockito.times(1)).setStatusMessage("Error Case");
        Mockito.verify(reqMock, Mockito.times(1)).setRequestStatus("FAILED");
        Mockito.verify(reqMock, Mockito.times(1)).setProgress(Long.valueOf(100));
        Mockito.verify(reqMock, Mockito.times(1)).setLastModifiedBy("CamundaBPMN");
        Mockito.verify(reqMock, Mockito.times(1)).setEndTime(any(Timestamp.class));
    }

    @Test
    public void updateRequestStatusToFailed() {
        execution.setVariable("mso-request-id", "123");
        execution.setVariable("isRollbackComplete", false);
        execution.setVariable("isRollback", false);
        InfraActiveRequests req = new InfraActiveRequests();
        WorkflowException wfe = new WorkflowException("processKey123", 1, "error in test case");
        execution.setVariable("WorkflowException", wfe);
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId("123");
        doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
        workflowActionBBFailure.updateRequestStatusToFailed(execution);
        String errorMsg = (String) execution.getVariable("ErrorMessage");
        assertEquals("error in test case", errorMsg);
        assertEquals(Status.FAILED.toString(), req.getRequestStatus());
    }

    @Test
    public void updateRequestStatusToAborted() {
        execution.setVariable("mso-request-id", "123");
        execution.setVariable("isRollbackComplete", false);
        execution.setVariable("isRollback", false);
        execution.setVariable("handlingCode", "Abort");
        InfraActiveRequests req = new InfraActiveRequests();
        WorkflowException wfe = new WorkflowException("processKey123", 1, "error in test case");
        execution.setVariable("WorkflowException", wfe);
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId("123");
        doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
        workflowActionBBFailure.updateRequestStatusToFailed(execution);
        String errorMsg = (String) execution.getVariable("ErrorMessage");
        assertEquals("error in test case", errorMsg);
        assertEquals(Status.ABORTED.toString(), req.getRequestStatus());
    }

    @Test
    public void updateRequestStatusToFailedRollback() {
        execution.setVariable("mso-request-id", "123");
        execution.setVariable("isRollbackComplete", false);
        execution.setVariable("isRollback", true);
        InfraActiveRequests req = new InfraActiveRequests();
        WorkflowException wfe = new WorkflowException("processKey123", 1, "error in rollback");
        execution.setVariable("WorkflowException", wfe);
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId("123");
        doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
        workflowActionBBFailure.updateRequestStatusToFailed(execution);
        String errorMsg = (String) execution.getVariable("RollbackErrorMessage");
        assertEquals("error in rollback", errorMsg);
        assertEquals(Status.FAILED.toString(), req.getRequestStatus());
    }

    @Test
    public void updateRequestStatusToFailedRollbackFabric() {
        BuildingBlock bb = new BuildingBlock().setBpmnFlowName("UnassignFabricConfigurationBB");
        ExecuteBuildingBlock ebb = new ExecuteBuildingBlock();
        ebb.setBuildingBlock(bb);
        execution.setVariable("buildingBlock", ebb);
        execution.setVariable("mso-request-id", "123");
        execution.setVariable("isRollbackComplete", false);
        execution.setVariable("isRollback", true);
        InfraActiveRequests req = new InfraActiveRequests();
        req.setStatusMessage("PINC failure.");
        WorkflowException wfe = new WorkflowException("processKey123", 1, "error in rollback");
        execution.setVariable("WorkflowException", wfe);
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId("123");
        doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
        workflowActionBBFailure.updateRequestStatusToFailed(execution);
        String errorMsg = (String) execution.getVariable("RollbackErrorMessage");
        assertEquals("error in rollback", errorMsg);
        assertEquals(
                "PINC failure. Warning: The vf-module is active but configuration was not removed completely for one or more VMs.",
                req.getStatusMessage());
        assertEquals(Status.FAILED.toString(), req.getRequestStatus());
    }

    @Test
    public void updateRequestStatusToRolledback() {
        execution.setVariable("mso-request-id", "123");
        execution.setVariable("isRollbackComplete", true);
        execution.setVariable("isRollback", true);
        execution.setVariable("rollbackTargetState", "ROLLED_BACK");
        InfraActiveRequests req = new InfraActiveRequests();
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId("123");
        doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
        workflowActionBBFailure.updateRequestStatusToFailed(execution);
        String errorMsg = (String) execution.getVariable("RollbackErrorMessage");
        assertEquals("Rollback has been completed successfully.", errorMsg);
        assertEquals(Status.ROLLED_BACK.toString(), req.getRequestStatus());
    }

    @Test
    public void updateRequestStatusToRolledbackToAssigned() {
        execution.setVariable("mso-request-id", "123");
        execution.setVariable("isRollbackComplete", true);
        execution.setVariable("isRollback", true);
        execution.setVariable("rollbackTargetState", "ROLLED_BACK_TO_ASSIGNED");
        InfraActiveRequests req = new InfraActiveRequests();
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId("123");
        doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
        workflowActionBBFailure.updateRequestStatusToFailed(execution);
        String errorMsg = (String) execution.getVariable("RollbackErrorMessage");
        assertEquals("Rollback has been completed successfully.", errorMsg);
        assertEquals(Status.ROLLED_BACK_TO_ASSIGNED.toString(), req.getRequestStatus());
    }

    @Test
    public void updateRequestStatusToRolledbackToCreated() {
        execution.setVariable("mso-request-id", "123");
        execution.setVariable("isRollbackComplete", true);
        execution.setVariable("isRollback", true);
        execution.setVariable("rollbackTargetState", "ROLLED_BACK_TO_CREATED");
        InfraActiveRequests req = new InfraActiveRequests();
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId("123");
        doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
        workflowActionBBFailure.updateRequestStatusToFailed(execution);
        String errorMsg = (String) execution.getVariable("RollbackErrorMessage");
        assertEquals("Rollback has been completed successfully.", errorMsg);
        assertEquals(Status.ROLLED_BACK_TO_CREATED.toString(), req.getRequestStatus());
    }

    @Test
    public void updateRequestStatusToFailedNoWorkflowException() {
        execution.setVariable("mso-request-id", "123");
        execution.setVariable("isRollbackComplete", false);
        execution.setVariable("isRollback", false);
        execution.setVariable("WorkflowExceptionErrorMessage", "error in test case");
        InfraActiveRequests req = new InfraActiveRequests();
        doReturn(req).when(requestsDbClient).getInfraActiveRequestbyRequestId("123");
        doNothing().when(requestsDbClient).updateInfraActiveRequests(isA(InfraActiveRequests.class));
        workflowActionBBFailure.updateRequestStatusToFailed(execution);
        String errorMsg = (String) execution.getVariable("ErrorMessage");
        assertEquals("error in test case", errorMsg);
    }

    @Test
    public void updateRequestErrorStatusMessageTest() {
        String reqId = "reqId123";
        execution.setVariable("mso-request-id", reqId);
        WorkflowException we = new WorkflowException("WorkflowAction", 1231, "Error Case");
        execution.setVariable("WorkflowException", we);

        doReturn(reqMock).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
        workflowActionBBFailure.updateRequestErrorStatusMessage(execution);
        Mockito.verify(reqMock, Mockito.times(1)).setStatusMessage("Error Case");
        Mockito.verify(reqMock, Mockito.times(1)).setProgress(Long.valueOf(100));
        Mockito.verify(reqMock, Mockito.times(1)).setLastModifiedBy("CamundaBPMN");
        Mockito.verify(reqMock, Mockito.times(1)).setEndTime(any(Timestamp.class));
    }

    @Test
    public void updateRequestRollbackErrorStatusMessageTest() {
        String reqId = "reqId123";
        execution.setVariable("mso-request-id", reqId);
        WorkflowException we = new WorkflowException("WorkflowAction", 1231, "Error Case");
        execution.setVariable("WorkflowException", we);
        execution.setVariable("isRollback", true);

        doReturn(reqMock).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
        workflowActionBBFailure.updateRequestErrorStatusMessage(execution);
        Mockito.verify(reqMock, Mockito.times(0)).setStatusMessage("Error Case");
        Mockito.verify(reqMock, Mockito.times(1)).setRollbackStatusMessage("Error Case");
        Mockito.verify(reqMock, Mockito.times(1)).setProgress(Long.valueOf(100));
        Mockito.verify(reqMock, Mockito.times(1)).setLastModifiedBy("CamundaBPMN");
        Mockito.verify(reqMock, Mockito.times(1)).setEndTime(any(Timestamp.class));
    }

    @Test
    public void updateRequestNotRollbackErrorStatusMessageTest() {
        String reqId = "reqId123";
        execution.setVariable("mso-request-id", reqId);
        WorkflowException we = new WorkflowException("WorkflowAction", 1231, "Error Case");
        execution.setVariable("WorkflowException", we);
        execution.setVariable("isRollback", false);

        doReturn(reqMock).when(requestsDbClient).getInfraActiveRequestbyRequestId(reqId);
        workflowActionBBFailure.updateRequestErrorStatusMessage(execution);
        Mockito.verify(reqMock, Mockito.times(1)).setStatusMessage("Error Case");
        Mockito.verify(reqMock, Mockito.times(0)).setRollbackStatusMessage("Error Case");
        Mockito.verify(reqMock, Mockito.times(1)).setProgress(Long.valueOf(100));
        Mockito.verify(reqMock, Mockito.times(1)).setLastModifiedBy("CamundaBPMN");
        Mockito.verify(reqMock, Mockito.times(1)).setEndTime(any(Timestamp.class));
    }
}
