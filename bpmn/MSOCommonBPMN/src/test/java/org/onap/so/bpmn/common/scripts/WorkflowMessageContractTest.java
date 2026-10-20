/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.onap.so.bpmn.common.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.core.WorkflowException;

public class WorkflowMessageContractTest {
    private final ReceiveWorkflowMessage script = new ReceiveWorkflowMessage();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "ReceiveWorkflowMessage");
        variables.put("RCVWFMSG_timeout", "PT1M");
        variables.put("RCVWFMSG_messageType", "SDNC");
        variables.put("RCVWFMSG_correlator", "request-1");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void preprocessingUsesTheExactMessageSpecificCorrelatorAndResetsSuccess() {
        WorkflowException original = new WorkflowException("Parent", 5100, "original error");
        variables.put("WorkflowException", original);
        variables.put("RCVWFMSG_SuccessIndicator", Boolean.TRUE);
        variables.put("RCVWFMSG_correlator", " request & 東京 ");
        script.preProcessRequest(execution);
        assertEquals("RCVWFMSG_", variables.get("prefix"));
        assertEquals(" request & 東京 ", variables.get("SDNC_CORRELATOR"));
        assertSame(Boolean.FALSE, variables.get("RCVWFMSG_SuccessIndicator"));
        assertSame(original, variables.get("WorkflowException"));
        assertFalse(variables.containsKey("WorkflowResponse"));
    }

    @Test
    public void everyAbsentOrEmptyRequiredInputFailsBeforePublishingACorrelator() {
        Map<String, Object> valid = new HashMap<>(variables);
        for (String field : new String[] {"RCVWFMSG_timeout", "RCVWFMSG_messageType", "RCVWFMSG_correlator"}) {
            for (String invalid : new String[] {null, ""}) {
                variables.clear();
                variables.putAll(valid);
                variables.put(field, invalid);
                try {
                    script.preProcessRequest(execution);
                    fail("Expected missing input error for " + field);
                } catch (BpmnError error) {
                    assertEquals("2000", error.getErrorCode());
                    assertError(2000, field);
                    assertSame(Boolean.FALSE, variables.get("RCVWFMSG_SuccessIndicator"));
                    assertFalse(variables.containsKey("SDNC_CORRELATOR"));
                }
            }
        }
    }

    @Test
    public void receivedMessagesUseTheRequestedMessageTypeAndPreserveOpaqueContent() {
        String response = "<response>Grüße &amp; 東京</response>";
        variables.put("RCVWFMSG_messageType", "VNF");
        variables.put("VNF_MESSAGE", response);
        variables.put("SDNC_MESSAGE", "unrelated response");
        script.preProcessRequest(execution);
        script.processReceivedMessage(execution);
        assertEquals(response, variables.get("WorkflowResponse"));
        assertSame(Boolean.TRUE, variables.get("RCVWFMSG_SuccessIndicator"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void absentAndEmptyReceivedBodiesRemainDistinct() {
        script.preProcessRequest(execution);
        script.processReceivedMessage(execution);
        assertTrue(variables.containsKey("WorkflowResponse"));
        assertNull(variables.get("WorkflowResponse"));
        assertSame(Boolean.TRUE, variables.get("RCVWFMSG_SuccessIndicator"));
        variables.put("SDNC_MESSAGE", "");
        script.processReceivedMessage(execution);
        assertEquals("", variables.get("WorkflowResponse"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void anUnreadableMessageRecordsTheProcessingFailureWithoutReplacingAnExistingResponse() {
        script.preProcessRequest(execution);
        variables.put("WorkflowResponse", "previous response");
        when(execution.getVariable("SDNC_MESSAGE")).thenThrow(new IllegalStateException("message unavailable"));
        script.processReceivedMessage(execution);
        assertError(7020, "NONE");
        assertSame(Boolean.FALSE, variables.get("RCVWFMSG_SuccessIndicator"));
        assertEquals("previous response", variables.get("WorkflowResponse"));
    }

    private void assertError(int code, String detail) {
        assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals(code, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().contains(detail));
    }
}
