/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.task.Task
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.Silent.class)
class ManualHandlingTest extends MsoGroovyTest {

    @Spy
    ManualHandling manualHandling

    @Before
    void init() throws IOException {
        super.init("ManualHandling")
        MockitoAnnotations.initMocks(this)
    }

    @Test
    void testPreProcessRequest_setsPrefix() {
        ExecutionEntity mockExecution = setupMock("ManualHandling")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("serviceType")).thenReturn("vFW")
        when(mockExecution.getVariable("vnfType")).thenReturn("vFW-type")
        when(mockExecution.getVariable("currentActivity")).thenReturn("CreateVfModule")
        when(mockExecution.getVariable("workStep")).thenReturn("buildObject")
        when(mockExecution.getVariable("failedActivity")).thenReturn("CreateVfModule")
        when(mockExecution.getVariable("errorCode")).thenReturn("7000")
        when(mockExecution.getVariable("errorText")).thenReturn("test error")
        when(mockExecution.getVariable("requestorId")).thenReturn("testUser")
        when(mockExecution.getVariable("validResponses")).thenReturn("rollback,abort")

        manualHandling.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", "MH_")
    }

    @Test
    void testCreateManualTask_createsTaskWithVariables() {
        ExecutionEntity mockExecution = setupMock("ManualHandling")
        TaskService mockTaskService = mock(TaskService.class)
        Task mockTask = mock(Task.class)
        ProcessEngineServices mockPES = mock(ProcessEngineServices.class)

        when(mockExecution.getProcessEngineServices()).thenReturn(mockPES)
        when(mockPES.getTaskService()).thenReturn(mockTaskService)
        when(mockTaskService.newTask(anyString())).thenReturn(mockTask)

        when(mockExecution.getVariable("vnfType")).thenReturn("vFW-type")
        when(mockExecution.getVariable("serviceType")).thenReturn("vFW")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("requestorId")).thenReturn("testUser")
        when(mockExecution.getVariable("failedActivity")).thenReturn("CreateVfModule")
        when(mockExecution.getVariable("errorCode")).thenReturn("7000")
        when(mockExecution.getVariable("errorText")).thenReturn("test error")
        when(mockExecution.getVariable("currentActivity")).thenReturn("CreateVfModule")
        when(mockExecution.getVariable("workStep")).thenReturn("buildObject")
        when(mockExecution.getVariable("validResponses")).thenReturn("rollback,abort")

        manualHandling.createManualTask(mockExecution)

        verify(mockExecution).setVariable(eq("taskId"), anyString())
        verify(mockTaskService).saveTask(mockTask)
        verify(mockTaskService).setVariables(anyString(), any(Map.class))
    }

    @Test
    void testCompleteTask_uppercasesFirstLetter() {
        DelegateTask mockTask = mock(DelegateTask.class)
        DelegateExecution mockExecution = setupMock("ManualHandling")
        TaskService mockTaskService = mock(TaskService.class)
        ProcessEngineServices mockPES = mock(ProcessEngineServices.class)

        when(mockTask.getExecution()).thenReturn(mockExecution)
        when(mockTask.getId()).thenReturn("task-123")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockPES)
        when(mockPES.getTaskService()).thenReturn(mockTaskService)

        Map<String, String> taskVars = new HashMap<>()
        taskVars.put("responseValue", "retry")
        when(mockTaskService.getVariables("task-123")).thenReturn(taskVars)

        manualHandling.completeTask(mockTask)

        verify(mockExecution).setVariable("responseValue", "Retry")
    }

    @Test
    void testCompleteTask_alreadyUppercase() {
        DelegateTask mockTask = mock(DelegateTask.class)
        DelegateExecution mockExecution = setupMock("ManualHandling")
        TaskService mockTaskService = mock(TaskService.class)
        ProcessEngineServices mockPES = mock(ProcessEngineServices.class)

        when(mockTask.getExecution()).thenReturn(mockExecution)
        when(mockTask.getId()).thenReturn("task-456")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockPES)
        when(mockPES.getTaskService()).thenReturn(mockTaskService)

        Map<String, String> taskVars = new HashMap<>()
        taskVars.put("responseValue", "Abort")
        when(mockTaskService.getVariables("task-456")).thenReturn(taskVars)

        manualHandling.completeTask(mockTask)

        verify(mockExecution).setVariable("responseValue", "Abort")
    }

    @Test
    void testPrepareRequestsDBStatusUpdate() {
        ExecutionEntity mockExecution = setupMock("ManualHandling")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getId()).thenReturn("test-exec-id")

        manualHandling.prepareRequestsDBStatusUpdate(mockExecution, "PENDING_MANUAL_TASK")

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class)
        verify(mockExecution).setVariable(eq("setUpdateDBstatusPayload"), captor.capture())
        String payload = captor.getValue()
        assertNotNull(payload)
        assertTrue(payload.contains("req-123"))
        assertTrue(payload.contains("PENDING_MANUAL_TASK"))
        assertTrue(payload.contains("ManualHandling"))
    }
}
