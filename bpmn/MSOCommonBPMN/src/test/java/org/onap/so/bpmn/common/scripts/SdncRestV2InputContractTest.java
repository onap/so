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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;

public class SdncRestV2InputContractTest {
    private final SDNCAdapterRestV2 script = new SDNCAdapterRestV2();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, String> configuration = new HashMap<>();
    private MockedStatic<UrnPropertiesReader> properties;

    @Before
    public void configureStatefulExecution() {
        variables.put("testProcessKey", "SDNCAdapterRestV2");
        variables.put("mso-request-id", "request-contract");
        variables.put("prefix", "SDNCREST_");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        doAnswer(call -> {
            variables.remove(call.getArgument(0));
            return null;
        }).when(execution).removeVariable(anyString());
        configuration.put("mso.adapters.sdnc.rest.endpoint", "http://adapter.invalid/sdnc///");
        configuration.put("mso.adapters.sdnc.timeout", "PT2M");
        properties = mockStatic(UrnPropertiesReader.class, CALLS_REAL_METHODS);
        properties.when(() -> UrnPropertiesReader.getVariable(anyString()))
                .thenAnswer(call -> configuration.get(call.getArgument(0)));
        properties.when(() -> UrnPropertiesReader.getVariable(anyString(), any(DelegateExecution.class)))
                .thenAnswer(call -> configuration.get(call.getArgument(0)));
    }

    @After
    public void restoreProperties() {
        if (properties != null) {
            properties.close();
        }
    }

    @Test
    public void requestPreparationPreservesThePayloadAndSeparatesItsCallbackId() {
        String body = request("PT1M");
        variables.put("bpmnRequest", body);
        script.preProcessRequest(execution);
        assertEquals(body, variables.get("SDNCREST_sdncAdapterRequest"));
        assertEquals("http://adapter.invalid/sdnc", variables.get("SDNCREST_sdncAdapterUrl"));
        assertEquals("POST", variables.get("SDNCREST_sdncAdapterMethod"));
        assertEquals("callback-contract", variables.get("SDNCAResponse_CORRELATOR"));
        assertEquals("request-contract", variables.get("mso-request-id"));
        assertEquals("PT1M", variables.get("SDNCREST_timeout"));
        assertSame(Boolean.FALSE, variables.get("SDNCREST_SuccessIndicator"));
        assertTrue(variables.get("bpmnRequest") instanceof org.camunda.bpm.engine.variable.VariableMap);
        assertNull(AbstractServiceTaskProcessor.getVariable(execution, "bpmnRequest"));
        assertTrue(variables.get("SDNCREST_Request") instanceof org.camunda.bpm.engine.variable.VariableMap);
        assertEquals(body, AbstractServiceTaskProcessor.getVariable(execution, "SDNCREST_Request"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void invalidOrMissingRequestTimeoutUsesConfiguredDuration() {
        for (String timeout : new String[] {"", "not-duration", "PT1M2S"}) {
            variables.put("SDNCREST_Request", request(timeout));
            script.preProcessRequest(execution);
            assertEquals("PT2M", variables.get("SDNCREST_timeout"));
        }
        variables.put("SDNCREST_Request", request("PT1M").replace(",\"bpTimeout\":\"PT1M\"", ""));
        script.preProcessRequest(execution);
        assertEquals("PT2M", variables.get("SDNCREST_timeout"));
    }

    @Test
    public void missingTimeoutConfigurationUsesTheTenSecondFallback() {
        configuration.remove("mso.adapters.sdnc.timeout");
        variables.put("SDNCREST_Request", request("invalid"));
        script.preProcessRequest(execution);
        assertEquals("PT10S", variables.get("SDNCREST_timeout"));
    }

    @Test
    public void missingEndpointFailsBeforeProducingAnOutboundRequest() {
        for (String endpoint : new String[] {null, ""}) {
            configuration.put("mso.adapters.sdnc.rest.endpoint", endpoint);
            variables.put("SDNCREST_Request", request("PT1M"));
            assertRejected(2000, "URN mapping is not defined");
            assertFalse(variables.containsKey("SDNCREST_sdncAdapterRequest"));
        }
    }

    @Test
    public void invalidIdentityCallbackAndRequestTypeFailValidation() {
        for (String invalid : new String[] {request("PT1M").replace("callback-contract", ""),
                request("PT1M").replace("http://callback.invalid/contract", ""), "{\"UnsupportedRequest\":{}}"}) {
            variables.put("SDNCREST_Request", invalid);
            assertRejected(2000, "SDNCAdapterRestV2");
            assertFalse(variables.containsKey("SDNCREST_sdncAdapterRequest"));
        }
    }

    @Test
    public void absentRequestAndMissingMsoRequestIdRetainValidationCodes() {
        assertRejected(1002, "request is null");
        variables.put("SDNCREST_Request", request("PT1M"));
        variables.remove("mso-request-id");
        assertRejected(1002, "mso-request-id");
    }

    @Test
    public void nonfinalCallbacksSelectOnlyTheirMatchingInterimNotification() {
        String notification =
                "{\"variableList\":[{\"variable\":{\"name\":\"notice\",\"value\":\"Gr\\u00fc\\u00dfe\"}}]}";
        variables.put("SDNCREST_InterimNotification1", notification);
        variables.put("SDNCAResponse_MESSAGE", "{\"SDNCServiceResponse\":{\"ackFinalIndicator\":\"N\"}}");
        script.processCallback(execution);
        assertEquals("N", variables.get("SDNCREST_ackFinalIndicator"));
        assertSame(Boolean.TRUE, variables.get("SDNCREST_doInterimNotification"));
        assertEquals(notification, variables.get("SDNCREST_interimNotification"));
        assertFalse(variables.containsKey("SDNCAResponse_MESSAGE"));
        script.prepareInterimNotification(execution);
        assertEquals("Gr\u00fc\u00dfe", variables.get("notice"));
        String last = "{\"SDNCServiceResponse\":{}}";
        variables.put("SDNCAResponse_MESSAGE", last);
        script.processCallback(execution);
        assertEquals("Y", variables.get("SDNCREST_ackFinalIndicator"));
        assertSame(Boolean.FALSE, variables.get("SDNCREST_doInterimNotification"));
        assertNull(variables.get("SDNCREST_interimNotification"));
        assertEquals(last, variables.get("SDNCREST_callback2"));
        assertEquals(last, script.getLastCallback(execution));
    }

    @Test
    public void explicitErrorCallbacksRetainMappedCodesAndMessages() {
        String body = "{\"SDNCServiceError\":{\"responseCode\":\"500\",\"responseMessage\":\"controller failed\"}}";
        variables.put("SDNCAResponse_MESSAGE", body);
        script.processCallback(execution);
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals(5310, error.getErrorCode());
        assertEquals(
                "Received SDNCServiceError from SDNCAdapter: responseCode='500' responseMessage='controller failed'",
                error.getErrorMessage());
        assertEquals(body, variables.get("SDNCREST_callback1"));
        assertFalse(variables.containsKey("SDNCAResponse_MESSAGE"));
    }

    @Test
    public void configuredCredentialsProduceTheBasicAuthorizationValue() {
        configuration.put("so.auth.username", "contract");
        configuration.put("so.auth.password", "contract");
        variables.put("SDNCREST_Request", request("PT1M"));
        script.preProcessRequest(execution);
        Object header = variables.get("SDNCREST_basicAuthHeaderValue");
        assertEquals(String.class, header.getClass());
        assertEquals("Basic Y29udHJhY3Q6Y29udHJhY3Q=", header);
        assertFalse(variables.containsKey("WorkflowException"));
    }

    private void assertRejected(int code, String message) {
        try {
            script.preProcessRequest(execution);
            fail("Expected BPMN error " + code);
        } catch (BpmnError error) {
            assertEquals(Integer.toString(code), error.getErrorCode());
        }
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals(code, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().contains(message));
        assertSame(Boolean.FALSE, variables.get("SDNCREST_SuccessIndicator"));
    }

    private String request(String timeout) {
        return "{\"SDNCServiceRequest\":{\"sdncRequestId\":\"callback-contract\","
                + "\"bpNotificationUrl\":\"http://callback.invalid/contract\",\"bpTimeout\":\"" + timeout
                + "\",\"capacity\":0,\"enabled\":false}}";
    }
}
