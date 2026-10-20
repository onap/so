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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.common.workflow.context.WorkflowCallbackResponse;
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder;
import org.onap.so.bpmn.core.WorkflowException;

public class ServiceTaskProcessorContractTest {
    private final Processor processor = new Processor();
    private final Map<String, Object> variables = new HashMap<>();
    private final DelegateExecution execution = mock(DelegateExecution.class);

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "CreateService");
        variables.put("prefix", "CS_");
        variables.put("mso-request-id", "request-1");
        variables.put("CS_Request", "{\"enabled\":false}");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void prefixedRequestTakesPrecedenceWithoutMovingFallbackRequests() {
        variables.put("CreateServiceRequest", "process request");
        variables.put("bpmnRequest", "global request");
        assertEquals("{\"enabled\":false}", processor.validateRequest(execution));
        verify(execution, never()).setVariable(anyString(), any());
    }

    @Test
    public void processRequestIsMovedIntoJavaSerializedPayloadMaps() {
        variables.remove("CS_Request");
        variables.put("CreateServiceRequest", "process request");
        variables.put("bpmnRequest", "global request");
        assertEquals("process request", processor.validateRequest(execution));
        assertPayload("CS_Request", "process request");
        assertPayload("CreateServiceRequest", null);
        assertPayload("bpmnRequest", null);
        verify(execution, never()).removeVariable(anyString());
    }

    @Test
    public void globalRequestIsUsedWhenBothSpecificRequestsAreAbsent() {
        variables.remove("CS_Request");
        variables.put("bpmnRequest", "global request");
        assertEquals("global request", processor.validateRequest(execution));
        assertPayload("CS_Request", "global request");
    }

    @Test
    public void falseZeroAndWhitespaceArePresentRequiredValues() {
        variables.put("enabled", false);
        variables.put("count", 0);
        variables.put("name", " ");
        assertEquals("{\"enabled\":false}", processor.validateRequest(execution, "enabled", "count", "name", "count"));
        assertSame(Boolean.FALSE, variables.get("enabled"));
        assertEquals(Integer.class, variables.get("count").getClass());
    }

    @Test
    public void absentNullAndEmptyRequiredValuesProduceTheBusinessError() {
        for (Object value : Arrays.asList(null, "")) {
            variables.put("model", value);
            assertValidationError("CreateService request was received with no 'model' variable", "model");
        }
        variables.remove("model");
        assertValidationError("CreateService request was received with no 'model' variable", "model");
    }

    @Test
    public void requestIdIsAlwaysRequired() {
        variables.remove("mso-request-id");
        assertValidationError("CreateService request was received with no 'mso-request-id' variable");
    }

    @Test
    public void missingPrefixAndMissingRequestHaveDistinctErrors() {
        variables.remove("prefix");
        assertValidationError("CreateService prefix is null");
        variables.put("prefix", "CS_");
        variables.remove("CS_Request");
        assertValidationError("CreateService request is null");
    }

    @Test
    public void jsonRetainsFalseZeroNullUnicodeAndArraySemantics() {
        variables.put("CS_Request",
                "{\"enabled\":false,\"count\":0,\"optional\":null," + "\"name\":\"Grüße \\\"SO\\\"\",\"items\":[1,2]}");
        Map<?, ?> request = processor.validateJSONReq(execution);
        assertSame(Boolean.FALSE, request.get("enabled"));
        assertEquals(Integer.valueOf(0), request.get("count"));
        assertTrue(request.containsKey("optional"));
        assertNull(request.get("optional"));
        assertFalse(request.containsKey("absent"));
        assertEquals("Grüße \"SO\"", request.get("name"));
        assertEquals(Arrays.asList(1, 2), request.get("items"));
    }

    @Test
    public void jsonReadsSerializedPayloadAndKeepsFallbackVariables() {
        variables.remove("CS_Request");
        processor.setVariable(execution, "CreateServiceRequest", "{\"count\":1}");
        Object stored = variables.get("CreateServiceRequest");
        assertEquals(Integer.valueOf(1), processor.validateJSONReq(execution).get("count"));
        assertSame(stored, variables.get("CreateServiceRequest"));
        assertEquals("{\"count\":1}", variables.get("CS_Request"));
    }

    @Test
    public void savingWorkflowErrorPreservesIdentityAndExplicitlySetsNull() {
        WorkflowException original = new WorkflowException("CreateService", 7000, "Failure");
        variables.put("WorkflowException", original);
        processor.saveWorkflowException(execution, "originalError");
        assertSame(original, variables.get("originalError"));
        assertTrue(variables.containsKey("WorkflowException"));
        assertNull(variables.get("WorkflowException"));
        verify(execution, never()).removeVariable(anyString());
    }

    @Test(expected = NullPointerException.class)
    public void savingWorkflowErrorRequiresADestination() {
        processor.saveWorkflowException(execution, null);
    }

    @Test
    public void responseCodesKeepStringVariablesAndTypedCallbackCodes() {
        for (int code : new int[] {100, 199, 200, 299, 300, 400, 599}) {
            variables.remove("CreateServiceWorkflowResponseSent");
            variables.put("isAsyncProcess", true);
            WorkflowContextHolder holder = mock(WorkflowContextHolder.class);
            try (MockedStatic<WorkflowContextHolder> singleton = mockStatic(WorkflowContextHolder.class)) {
                singleton.when(WorkflowContextHolder::getInstance).thenReturn(holder);
                processor.respond(execution, code, "response Grüße");
                String status = code >= 200 && code <= 299 ? "Success" : "Fail";
                assertEquals(String.valueOf(code), variables.get("CreateServiceResponseCode"));
                assertEquals(status, variables.get("CreateServiceStatus"));
                assertEquals("response Grüße", variables.get("CreateServiceResponse"));
                assertEquals("response Grüße", variables.get("WorkflowResponse"));
                assertEquals("true", variables.get("CreateServiceWorkflowResponseSent"));
                ArgumentCaptor<WorkflowCallbackResponse> response =
                        ArgumentCaptor.forClass(WorkflowCallbackResponse.class);
                verify(holder).processCallback(eq("CreateService"), eq("process-1"), eq("request-1"),
                        response.capture());
                assertEquals(code, response.getValue().getStatusCode());
                assertEquals(status, response.getValue().getMessage());
                assertEquals("response Grüße", response.getValue().getResponse());
                processor.respond(execution, code, "duplicate");
                verifyNoMoreInteractions(holder);
                assertEquals("response Grüße", variables.get("WorkflowResponse"));
            }
        }
    }

    @Test
    public void responseSentLookupAcceptsTrueStringAndBooleanOnly() {
        for (Object value : Arrays.asList(null, false, "false", "TRUE", 1)) {
            variables.put("CreateServiceWorkflowResponseSent", value);
            assertFalse(processor.responseSent(execution));
        }
        for (Object value : Arrays.asList(true, "true")) {
            variables.put("CreateServiceWorkflowResponseSent", value);
            assertTrue(processor.responseSent(execution));
        }
    }

    private void assertPayload(String name, Object expected) {
        assertTrue(variables.get(name) instanceof VariableMap);
        VariableMap map = (VariableMap) variables.get(name);
        ObjectValue payload = map.getValueTyped("payload");
        assertEquals(expected, payload.getValue());
        assertEquals("application/x-java-serialized-object", payload.getSerializationDataFormat());
        assertEquals(expected, AbstractServiceTaskProcessor.getVariable(execution, name));
    }

    private void assertValidationError(String message, String... required) {
        try {
            processor.validateRequest(execution, required);
            fail("Expected validation error");
        } catch (BpmnError error) {
            assertEquals("1002", error.getErrorCode());
            Object stored = variables.get("WorkflowException");
            assertTrue(stored instanceof WorkflowException);
            assertEquals(1002, ((WorkflowException) stored).getErrorCode());
            assertEquals(message, ((WorkflowException) stored).getErrorMessage());
        }
    }

    private static class Processor extends AbstractServiceTaskProcessor {
        @Override
        public void preProcessRequest(DelegateExecution execution) {}

        void respond(DelegateExecution execution, Object code, String body) {
            sendWorkflowResponse(execution, code, body);
        }

        boolean responseSent(DelegateExecution execution) {
            return isWorkflowResponseSent(execution);
        }
    }
}
