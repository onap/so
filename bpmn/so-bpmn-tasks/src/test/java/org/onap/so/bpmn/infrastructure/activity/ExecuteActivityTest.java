/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.activity;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionBBFailure;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.client.exception.ExceptionBuilder;

public class ExecuteActivityTest extends BaseTaskTest {
    @InjectMocks
    protected ExecuteActivity executeActivity = new ExecuteActivity();

    @InjectMocks
    @Spy
    private ExceptionBuilder exceptionBuilder;

    @Mock
    private WorkflowActionBBFailure workflowActionBBFailure;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DelegateExecution execution;

    @Before
    public void before() throws Exception {
        execution = new DelegateExecutionFake();
        execution.setVariable("vnfType", "testVnfType");
        execution.setVariable("requestAction", "testRequestAction");
        execution.setVariable("mso-request-id", "testMsoRequestId");
        execution.setVariable("vnfId", "testVnfId");
        execution.setVariable("serviceInstanceId", "testServiceInstanceId");
        String bpmnRequest =
                new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);
    }

    @Test
    public void buildBuildingBlock_Test() {
        BuildingBlock bb = executeActivity.buildBuildingBlock("testActivityName");
        assertEquals(bb.getBpmnFlowName(), "testActivityName");
        assertEquals(bb.getKey(), "");
    }

    @Test
    public void executeBuildingBlock_Test() throws Exception {
        BuildingBlock bb = executeActivity.buildBuildingBlock("testActivityName");
        ExecuteBuildingBlock ebb = executeActivity.buildExecuteBuildingBlock(execution, "testMsoRequestId", bb);
        assertEquals(ebb.getVnfType(), "testVnfType");
        assertEquals(ebb.getRequestAction(), "testRequestAction");
        assertEquals(ebb.getRequestId(), "testMsoRequestId");
        assertEquals(ebb.getWorkflowResourceIds().getVnfId(), "testVnfId");
        assertEquals(ebb.getWorkflowResourceIds().getServiceInstanceId(), "testServiceInstanceId");
        assertEquals(ebb.getBuildingBlock(), bb);
    }

    @Test
    public void executeException_Test() throws Exception {
        execution.setVariable("workflowSyncAckSent", true);
        execution.setVariable("testProcessKey", "testProcessKeyValue");
        thrown.expect(BpmnError.class);
        executeActivity.execute(execution);
        String errorMessage = (String) execution.getVariable("ExecuteActivityErrorMessage");
        assertEquals(errorMessage, "not implemented");
        WorkflowException workflowException = (WorkflowException) execution.getVariable("WorkflowException");
        assertEquals(workflowException.getErrorMessage(), "not implemented");
        assertEquals(workflowException.getErrorCode(), 7000);
    }

    @Test
    public void buildAndThrowException_Test() throws Exception {
        doNothing().when(workflowActionBBFailure).updateRequestStatusToFailed(execution);
        doReturn("Process key").when(exceptionBuilder).getProcessKey(execution);
        thrown.expect(BpmnError.class);
        executeActivity.buildAndThrowException(execution, "TEST EXCEPTION MSG");
        String errorMessage = (String) execution.getVariable("ExecuteActivityErrorMessage");
        assertEquals(errorMessage, "TEST EXCEPTION MSG");
        WorkflowException workflowException = (WorkflowException) execution.getVariable("WorkflowException");
        assertEquals(workflowException.getErrorMessage(), "TEST EXCEPTION MSG");
        assertEquals(workflowException.getErrorCode(), 7000);
    }

}
