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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.client.HttpClient;
import org.onap.so.client.HttpClientFactory;

public class CnfAdapterContractTest {
    private static final String ENDPOINT = "http://cnf.example.invalid:1234/api/v1/resources?name=one%20two";
    private static final String PAYLOAD = "{\"name\":\"München & 雪\",\"enabled\":false,\"count\":0}";
    private final CNFAdapterAsync adapter = new CNFAdapterAsync();

    @Test
    public void missingApiPathsProduceTheDefinedValidationError() {
        assertRequired("apiPath", "API PATH");
    }

    @Test
    public void missingPayloadsProduceTheDefinedValidationError() {
        assertRequired("cnfRequestPayload", "cnfRequestPayload");
    }

    @Test
    public void missingCorrelatorsProduceTheDefinedValidationError() {
        assertRequired("correlator", "correlator");
    }

    @Test
    public void missingMessageTypesProduceTheDefinedValidationError() {
        assertRequired("messageType", "messageType");
    }

    @Test
    public void missingOrBlankConfiguredTimeoutsUseTheStringDefault() {
        for (String configured : Arrays.asList(null, "", " \t")) {
            Map<String, Object> values = values();
            values.put("timeout", "old value");
            DelegateExecution execution = execution(values);
            try (MockedStatic<UrnPropertiesReader> properties = mockStatic(UrnPropertiesReader.class)) {
                properties.when(() -> UrnPropertiesReader.getVariable("mso.adapters.cnf.timeout", execution))
                        .thenReturn(configured);
                adapter.preProcessRequest(execution);
                assertEquals("PT30M", values.get("timeout"));
                assertEquals(String.class, values.get("timeout").getClass());
                assertEquals(PAYLOAD, values.get("cnfRequestPayload"));
                properties.verify(() -> UrnPropertiesReader.getVariable("mso.adapters.cnf.timeout", execution));
            }
        }
    }

    @Test
    public void anExplicitTimeoutAndCorrelationInputsRemainUnchanged() {
        Map<String, Object> values = values();
        DelegateExecution execution = execution(values);
        try (MockedStatic<UrnPropertiesReader> properties = mockStatic(UrnPropertiesReader.class)) {
            properties.when(() -> UrnPropertiesReader.getVariable("mso.adapters.cnf.timeout", execution))
                    .thenReturn("PT45S");
            adapter.preProcessRequest(execution);
            assertEquals("PT45S", values.get("timeout"));
            assertEquals("cnf-correlation-1", values.get("correlator"));
            assertEquals("cnf-health-response", values.get("messageType"));
            assertEquals(ENDPOINT, values.get("apiPath"));
            assertEquals(PAYLOAD, values.get("cnfRequestPayload"));
            assertFalse(values.containsKey("WorkflowException"));
        }
    }

    @Test
    public void everySuccessfulStatusAcceptsTheExactPayloadWithoutCreatingAnError() throws Exception {
        for (int status : new int[] {200, 201, 202, 204, 206, 299}) {
            Map<String, Object> values = values();
            HttpClient client = mock(HttpClient.class);
            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(status);
            when(client.post(PAYLOAD)).thenReturn(response);
            try (MockedConstruction<HttpClientFactory> factories = clientFactory(client)) {
                adapter.callCnfAdapter(execution(values));
                assertRequest(factories, client);
                assertFalse(values.containsKey("WorkflowException"));
                assertEquals(PAYLOAD, values.get("cnfRequestPayload"));
            }
        }
    }

    @Test
    public void nonSuccessfulStatusesKeepTheirCodeAndBusinessError() throws Exception {
        for (int status : new int[] {199, 300, 400, 401, 404, 409, 500, 503}) {
            Map<String, Object> values = values();
            HttpClient client = mock(HttpClient.class);
            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(status);
            when(client.post(PAYLOAD)).thenReturn(response);
            try (MockedConstruction<HttpClientFactory> factories = clientFactory(client)) {
                BpmnError failure = assertThrows(BpmnError.class, () -> adapter.callCnfAdapter(execution(values)));
                assertEquals(String.valueOf(status), failure.getErrorCode());
                assertError(values, status, "Received a Bad Sync Response from CNF.");
                assertRequest(factories, client);
            }
        }
    }

    @Test
    public void transportFailuresPropagateWithoutReplacingAnExistingWorkflowError() {
        Map<String, Object> values = values();
        WorkflowException original = new WorkflowException("caller", 7300, "original failure");
        values.put("WorkflowException", original);
        HttpClient client = mock(HttpClient.class);
        ProcessingException transport = new ProcessingException("connection unavailable");
        when(client.post(PAYLOAD)).thenThrow(transport);
        try (MockedConstruction<HttpClientFactory> factories = clientFactory(client)) {
            assertSame(transport,
                    assertThrows(ProcessingException.class, () -> adapter.callCnfAdapter(execution(values))));
            assertSame(original, values.get("WorkflowException"));
            verify(client).post(PAYLOAD);
            assertEquals(1, factories.constructed().size());
        }
    }

    @Test
    public void malformedEndpointsFailBeforeConstructingAnHttpClient() {
        Map<String, Object> values = values();
        values.put("apiPath", "://invalid");
        try (MockedConstruction<HttpClientFactory> factories = mockConstruction(HttpClientFactory.class)) {
            assertThrows(MalformedURLException.class, () -> adapter.callCnfAdapter(execution(values)));
            assertEquals(0, factories.constructed().size());
            assertFalse(values.containsKey("WorkflowException"));
        }
    }

    private void assertRequired(String name, String description) {
        for (int variant = 0; variant < 4; variant++) {
            Map<String, Object> values = values();
            if (variant == 0) {
                values.remove(name);
            } else {
                values.put(name, Arrays.asList(null, "", " \t").get(variant - 1));
            }
            DelegateExecution execution = execution(values);
            try (MockedStatic<UrnPropertiesReader> properties = mockStatic(UrnPropertiesReader.class)) {
                BpmnError failure = assertThrows(BpmnError.class, () -> adapter.preProcessRequest(execution));
                assertEquals("500", failure.getErrorCode());
                assertError(values, 500, "Cannot process CNF adapter call : " + description + " is null");
                verify(execution, never()).setVariable(eq("timeout"), any());
                properties.verifyNoInteractions();
            }
        }
    }

    private static MockedConstruction<HttpClientFactory> clientFactory(HttpClient client) {
        return mockConstruction(HttpClientFactory.class,
                (factory, context) -> when(factory.newJsonClient(any(URL.class), eq(ONAPComponents.EXTERNAL)))
                        .thenReturn(client));
    }

    private static void assertRequest(MockedConstruction<HttpClientFactory> factories, HttpClient client)
            throws Exception {
        assertEquals(1, factories.constructed().size());
        ArgumentCaptor<URL> endpoint = ArgumentCaptor.forClass(URL.class);
        verify(factories.constructed().get(0)).newJsonClient(endpoint.capture(), eq(ONAPComponents.EXTERNAL));
        assertEquals(ENDPOINT, endpoint.getValue().toExternalForm());
        verify(client).post(PAYLOAD);
    }

    private static void assertError(Map<String, Object> values, int code, String message) {
        Object raw = values.get("WorkflowException");
        assertEquals(WorkflowException.class, raw.getClass());
        WorkflowException failure = (WorkflowException) raw;
        assertEquals("CNFAdapterAsyncCall", failure.getProcessKey());
        assertEquals(code, failure.getErrorCode());
        assertEquals(message, failure.getErrorMessage());
    }

    private static Map<String, Object> values() {
        Map<String, Object> values = new HashMap<>();
        values.put("testProcessKey", "CNFAdapterAsyncCall");
        values.put("apiPath", ENDPOINT);
        values.put("cnfRequestPayload", PAYLOAD);
        values.put("correlator", "cnf-correlation-1");
        values.put("messageType", "cnf-health-response");
        return values;
    }

    private static DelegateExecution execution(Map<String, Object> values) {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable(anyString())).thenAnswer(call -> values.get(call.getArgument(0)));
        doAnswer(call -> {
            values.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        return execution;
    }
}
