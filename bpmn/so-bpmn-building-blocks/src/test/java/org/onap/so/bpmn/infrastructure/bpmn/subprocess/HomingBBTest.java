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

package org.onap.so.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.core.WorkflowException;
import org.springframework.beans.factory.annotation.Autowired;


public class HomingBBTest extends BaseBPMNTest {

    @Autowired
    private HistoryService historyService;

    @Test
    public void testHomingV2_success() {
        mockSubprocess("ReceiveWorkflowMessage", "Mock ReceiveWorkflowMessage", "GenericStub");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("HomingBB", variables);
        assertThat(pi).isNotNull();
        assertThat(pi).isStarted().hasPassedInOrder("start", "callHoming", "receiveAsyncCallback",
                "processHomingSolution", "end");
        assertThat(pi).isEnded();
    }

    @Test
    public void testHomingV2_error_bpmnError() {
        doThrow(new BpmnError("MSOWorkflowException")).when(homing).callHoming(any(BuildingBlockExecution.class));
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("HomingBB", variables);
        assertThat(pi).isNotNull();
        assertThat(pi).isStarted()
                .hasPassed("start", "callHoming", "startBpmnError", "bpmnErrorSubprocess",
                        "processMsoWorkflowException", "endBpmnError")
                .hasNotPassed("receiveAsyncCallback", "processHomingSolution");
        assertThat(pi).isEnded();
        WorkflowException error = workflowError(pi);
        assertEquals("HomingBB", error.getProcessKey());
        assertEquals(2500, error.getErrorCode());
        assertEquals("Internal Error - Occured During HomingBB", error.getErrorMessage());
    }

    @Test
    public void testHomingV2_error_preservesOriginalWorkflowException() {
        variables.put("WorkflowException",
                new WorkflowException("SelectHomingSolution", 7000, "No matching solution", "select"));
        doThrow(new BpmnError("MSOWorkflowException")).when(homing).callHoming(any(BuildingBlockExecution.class));

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("HomingBB", variables);

        assertThat(pi).hasPassed("processMsoWorkflowException", "endBpmnError")
                .hasNotPassed("receiveAsyncCallback", "processHomingSolution").isEnded();
        WorkflowException error = workflowError(pi);
        assertEquals("SelectHomingSolution", error.getProcessKey());
        assertEquals(7000, error.getErrorCode());
        assertEquals("No matching solution", error.getErrorMessage());
        assertEquals("select", error.getWorkStep());
    }

    @Test
    public void testHomingV2_error_javaException() {
        variables.put("BPMN_javaExpMsg", "Cannot select a homing solution");
        doThrow(new RuntimeException("Test")).when(homing).callHoming(any(BuildingBlockExecution.class));
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("HomingBB", variables);
        assertThat(pi).isNotNull();
        assertThat(pi)
                .isStarted().hasPassed("start", "callHoming", "startJavaError", "processJavaException",
                        "javaExceptionSubProcess", "endJavaError")
                .hasNotPassed("receiveAsyncCallback", "processHomingSolution");
        assertThat(pi).isEnded();
        WorkflowException error = workflowError(pi);
        assertEquals("HomingBB", error.getProcessKey());
        assertEquals(2500, error.getErrorCode());
        assertEquals("Catch a Java Lang Exception in HomingBB: Cannot select a homing solution",
                error.getErrorMessage());
    }

    private WorkflowException workflowError(ProcessInstance process) {
        HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName("WorkflowException").singleResult();
        assertNotNull("The error must be retained after the process ends", variable);
        assertTrue(variable.getValue() instanceof WorkflowException);
        return (WorkflowException) variable.getValue();
    }

}
