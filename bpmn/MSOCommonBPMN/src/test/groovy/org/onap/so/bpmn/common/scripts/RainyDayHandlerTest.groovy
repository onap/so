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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.core.UrnPropertiesReader

import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.Silent.class)
class RainyDayHandlerTest extends MsoGroovyTest {

    @Spy
    RainyDayHandler rainyDayHandler

    @Before
    void init() throws IOException {
        super.init("RainyDayHandler")
        MockitoAnnotations.initMocks(this)
    }

    @Test
    void testPreProcessRequest_setsPrefix() {
        ExecutionEntity mockExecution = setupMock("RainyDayHandler")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("serviceType")).thenReturn("vFW")
        when(mockExecution.getVariable("vnfType")).thenReturn("vFW-type")
        when(mockExecution.getVariable("currentActivity")).thenReturn("CreateVfModule")
        when(mockExecution.getVariable("workStep")).thenReturn("buildObject")
        when(mockExecution.getVariable("failedActivity")).thenReturn("CreateVfModule")
        when(mockExecution.getVariable("errorCode")).thenReturn("7000")
        when(mockExecution.getVariable("errorText")).thenReturn("test error")
        when(mockExecution.getId()).thenReturn("test-exec-id")

        rainyDayHandler.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", "RDH_")
    }

    @Test
    void testQueryPolicy_withDefaultDisposition() {
        ExecutionEntity mockExecution = setupMock("RainyDayHandler")
        when(mockExecution.getVariable("serviceType")).thenReturn("vFW")
        when(mockExecution.getVariable("vnfType")).thenReturn("vFW-type")
        when(mockExecution.getVariable("errorCode")).thenReturn("7000")
        when(mockExecution.getVariable("currentActivity")).thenReturn("CreateVfModule")
        when(mockExecution.getVariable("workStep")).thenReturn("buildObject")
        when(mockExecution.getVariable("defaultPolicyDisposition")).thenReturn("Skip")
        when(mockExecution.getId()).thenReturn("test-exec-id")

        rainyDayHandler.queryPolicy(mockExecution)

        verify(mockExecution).setVariable("handlingCode", "Skip")
        verify(mockExecution).setVariable("validResponses", "rollback, skip, manual, abort")
    }

    @Test
    void testQueryPolicy_withNullDefaultDisposition_setsAbort() {
        ExecutionEntity mockExecution = setupMock("RainyDayHandler")
        when(mockExecution.getVariable("serviceType")).thenReturn("vFW")
        when(mockExecution.getVariable("vnfType")).thenReturn("vFW-type")
        when(mockExecution.getVariable("errorCode")).thenReturn("7000")
        when(mockExecution.getVariable("currentActivity")).thenReturn("CreateVfModule")
        when(mockExecution.getVariable("workStep")).thenReturn("buildObject")
        when(mockExecution.getVariable("defaultPolicyDisposition")).thenReturn(null)
        when(mockExecution.getId()).thenReturn("test-exec-id")

        rainyDayHandler.queryPolicy(mockExecution)

        // When policy client throws exception and no default, disposition should be "Abort"
        verify(mockExecution).setVariable("handlingCode", "Abort")
    }

    @Test
    void testQueryPolicy_withEmptyDefaultDisposition_setsAbort() {
        ExecutionEntity mockExecution = setupMock("RainyDayHandler")
        when(mockExecution.getVariable("serviceType")).thenReturn("vFW")
        when(mockExecution.getVariable("vnfType")).thenReturn("vFW-type")
        when(mockExecution.getVariable("errorCode")).thenReturn("7000")
        when(mockExecution.getVariable("currentActivity")).thenReturn("CreateVfModule")
        when(mockExecution.getVariable("workStep")).thenReturn("buildObject")
        when(mockExecution.getVariable("defaultPolicyDisposition")).thenReturn("")
        when(mockExecution.getId()).thenReturn("test-exec-id")

        rainyDayHandler.queryPolicy(mockExecution)

        verify(mockExecution).setVariable("handlingCode", "Abort")
    }
}
