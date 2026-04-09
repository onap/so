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

import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.Silent.class)
class ReceiveWorkflowMessageTest extends MsoGroovyTest {

    @Spy
    ReceiveWorkflowMessage receiveWorkflowMessage

    @Before
    void init() throws IOException {
        super.init("ReceiveWorkflowMessage")
        MockitoAnnotations.initMocks(this)
    }

    @Test
    void testPreProcessRequest_success() {
        ExecutionEntity mockExecution = setupMock("ReceiveWorkflowMessage")
        when(mockExecution.getVariable("RCVWFMSG_timeout")).thenReturn("PT5M")
        when(mockExecution.getVariable("RCVWFMSG_messageType")).thenReturn("SDNCAEvent")
        when(mockExecution.getVariable("RCVWFMSG_correlator")).thenReturn("correlator-123")
        when(mockExecution.getId()).thenReturn("test-exec-id")

        receiveWorkflowMessage.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", "RCVWFMSG_")
        verify(mockExecution).setVariable("SDNCAEvent_CORRELATOR", "correlator-123")
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequest_missingTimeout() {
        ExecutionEntity mockExecution = setupMock("ReceiveWorkflowMessage")
        when(mockExecution.getVariable("RCVWFMSG_timeout")).thenReturn(null)
        when(mockExecution.getId()).thenReturn("test-exec-id")

        receiveWorkflowMessage.preProcessRequest(mockExecution)
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequest_missingMessageType() {
        ExecutionEntity mockExecution = setupMock("ReceiveWorkflowMessage")
        when(mockExecution.getVariable("RCVWFMSG_timeout")).thenReturn("PT5M")
        when(mockExecution.getVariable("RCVWFMSG_messageType")).thenReturn("")
        when(mockExecution.getId()).thenReturn("test-exec-id")

        receiveWorkflowMessage.preProcessRequest(mockExecution)
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequest_missingCorrelator() {
        ExecutionEntity mockExecution = setupMock("ReceiveWorkflowMessage")
        when(mockExecution.getVariable("RCVWFMSG_timeout")).thenReturn("PT5M")
        when(mockExecution.getVariable("RCVWFMSG_messageType")).thenReturn("SDNCAEvent")
        when(mockExecution.getVariable("RCVWFMSG_correlator")).thenReturn(null)
        when(mockExecution.getId()).thenReturn("test-exec-id")

        receiveWorkflowMessage.preProcessRequest(mockExecution)
    }

    @Test
    void testProcessReceivedMessage_success() {
        ExecutionEntity mockExecution = setupMock("ReceiveWorkflowMessage")
        when(mockExecution.getVariable("RCVWFMSG_messageType")).thenReturn("SDNCAEvent")
        when(mockExecution.getVariable("SDNCAEvent_MESSAGE")).thenReturn("<response>OK</response>")
        when(mockExecution.getId()).thenReturn("test-exec-id")

        receiveWorkflowMessage.processReceivedMessage(mockExecution)

        verify(mockExecution).setVariable("WorkflowResponse", "<response>OK</response>")
    }

    @Test
    void testProcessReceivedMessage_nullMessage() {
        ExecutionEntity mockExecution = setupMock("ReceiveWorkflowMessage")
        when(mockExecution.getVariable("RCVWFMSG_messageType")).thenReturn("SDNCAEvent")
        when(mockExecution.getVariable("SDNCAEvent_MESSAGE")).thenReturn(null)
        when(mockExecution.getId()).thenReturn("test-exec-id")

        receiveWorkflowMessage.processReceivedMessage(mockExecution)

        verify(mockExecution).setVariable("WorkflowResponse", null)
    }
}
