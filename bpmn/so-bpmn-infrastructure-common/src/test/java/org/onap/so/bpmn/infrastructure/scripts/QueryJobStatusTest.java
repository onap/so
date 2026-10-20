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
package org.onap.so.bpmn.infrastructure.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.core.WorkflowException;

public class QueryJobStatusTest {
    private final QueryJobStatus script = new QueryJobStatus();
    private final Map<String, Object> variables = new HashMap<>();
    private final DelegateExecution execution = mock(DelegateExecution.class);

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "QueryJobStatus");
        variables.put("msoRequestId", "request-1");
        variables.put("responseId", "response-1");
        variables.put("jobId", "job-1");
        variables.put("esrInfo", "{\"name\":\"Grüße\",\"enabled\":false}");
        variables.put("serviceInfo", "{\"count\":0,\"items\":[1,2],\"optional\":null}");
        variables.put("mso.adapters.nssmf.endpoint", "http://localhost:8080/nssmf");
        variables.put("NSSMF_ResponseCode", 200);
        variables.put("startTime", 4102444800000L);
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void preprocessingBuildsTheJobUrlAndTypedRequest() {
        script.preProcessRequest(execution);
        assertEquals("http://localhost:8080/nssmf/api/rest/provMns/v1/NSS/jobs/job-1",
                variables.get("NSSMF_AdapterEndpoint"));
        assertTrue(variables.get("esrInfo") instanceof Map);
        assertTrue(variables.get("serviceInfo") instanceof Map);
        JSONObject request = new JSONObject((String) variables.get("NSSMF_AdapterRequest"));
        assertEquals("response-1", request.getString("responseId"));
        assertEquals("Grüße", request.getJSONObject("esrInfo").getString("name"));
        assertEquals(Boolean.FALSE, request.getJSONObject("esrInfo").get("enabled"));
        JSONObject service = request.getJSONObject("serviceInfo");
        assertEquals(Integer.valueOf(0), service.get("count"));
        assertEquals(2, service.getJSONArray("items").length());
        assertEquals(1, service.getJSONArray("items").getInt(0));
        assertEquals(2, service.getJSONArray("items").getInt(1));
        assertFalse(service.has("optional"));
        Map<?, ?> serviceInfo = (Map<?, ?>) variables.get("serviceInfo");
        assertTrue(serviceInfo.containsKey("optional"));
        assertNull(serviceInfo.get("optional"));
        assertFalse(service.has("absent"));
        assertEquals(Long.class, variables.get("startTime").getClass());
    }

    @Test
    public void invalidPreprocessingInputsBecomeWorkflowErrors() {
        for (String name : new String[] {"esrInfo", "serviceInfo"}) {
            Object original = variables.put(name, "not-json");
            try {
                script.preProcessRequest(execution);
                fail("Expected invalid " + name + " to fail");
            } catch (BpmnError error) {
                assertEquals("7000", error.getErrorCode());
                WorkflowException stored = (WorkflowException) variables.get("WorkflowException");
                assertEquals(7000, stored.getErrorCode());
                assertEquals("QueryJobStatus", stored.getProcessKey());
                assertTrue(stored.getErrorMessage().startsWith("Exception in QueryJobStatus.preProcessRequest "));
            }
            variables.put(name, original);
            variables.put("esrInfo", "{\"name\":\"Grüße\",\"enabled\":false}");
            variables.put("serviceInfo", "{\"count\":0,\"items\":[1,2],\"optional\":null}");
        }
    }

    @Test
    public void terminalStatusesUseUppercaseStringFlagsAndKeepResponseDetails() {
        for (String status : new String[] {"finished", "FINISHED", "failed", "FaIlEd"}) {
            String response = response(status);
            variables.put("NSSMF_Response", response);
            script.checkJobStatus(execution);
            assertEquals("TRUE", variables.get("JobStatusCompleted"));
            assertEquals(response, variables.get("NSSMF_Response"));
        }
    }

    @Test
    public void processingAndUnknownStatusesKeepPolling() {
        for (String status : new String[] {"processing", "PROCESSING", "queued"}) {
            variables.put("NSSMF_Response", response(status));
            script.checkJobStatus(execution);
            assertEquals("FALSE", variables.get("JobStatusCompleted"));
        }
    }

    @Test
    public void configuredTimeoutProducesAFailedDescriptor() {
        variables.put("NSSMF_Response", response("processing"));
        variables.put("startTime", 0L);
        variables.put("timeout", 1L);
        script.checkJobStatus(execution);
        assertTimedOut();
    }

    @Test
    public void absentTimeoutUsesTheDefaultAndExpiresAnOldJob() {
        variables.put("NSSMF_Response", response("processing"));
        variables.put("startTime", 0L);
        assertFalse(variables.containsKey("timeout"));
        script.checkJobStatus(execution);
        assertTimedOut();
    }

    @Test
    public void terminalFailureIsNotReplacedByTimeout() {
        String response = response("failed");
        variables.put("NSSMF_Response", response);
        variables.put("startTime", 0L);
        variables.put("timeout", 0L);
        script.checkJobStatus(execution);
        assertEquals("TRUE", variables.get("JobStatusCompleted"));
        assertEquals(response, variables.get("NSSMF_Response"));
    }

    @Test
    public void responseDescriptorExtractionRetainsObjectAndArrayValues() {
        variables.put("NSSMF_Response", "{\"responseDescriptor\":{\"status\":\"finished\","
                + "\"count\":0,\"enabled\":false,\"items\":[\"Grüße\"],\"optional\":null}}");
        script.updateJobStatusDetails(execution);
        assertTrue(variables.get("responseDescriptor") instanceof String);
        JSONObject descriptor = new JSONObject((String) variables.get("responseDescriptor"));
        assertEquals("finished", descriptor.getString("status"));
        assertEquals(Integer.valueOf(0), descriptor.get("count"));
        assertEquals(Boolean.FALSE, descriptor.get("enabled"));
        assertEquals("Grüße", descriptor.getJSONArray("items").getString(0));
        assertTrue(descriptor.has("optional"));
        assertTrue(descriptor.isNull("optional"));
    }

    @Test
    public void absentResponseMarksTheJobCompleteAndRaisesTheBusinessError() {
        assertFalse(variables.containsKey("NSSMF_Response"));
        try {
            script.checkJobStatus(execution);
            fail("Expected missing NSSMF response to fail");
        } catch (BpmnError error) {
            assertEquals("7000", error.getErrorCode());
            Object stored = variables.get("WorkflowException");
            assertTrue(stored instanceof WorkflowException);
            WorkflowException workflowError = (WorkflowException) stored;
            assertEquals("QueryJobStatus", workflowError.getProcessKey());
            assertEquals(7000, workflowError.getErrorCode());
            assertEquals("Received a Bad Response from NSSMF.", workflowError.getErrorMessage());
            assertEquals("TRUE", variables.get("JobStatusCompleted"));
        }
    }

    @Test
    public void absentDescriptorIsWrittenAsExplicitNull() {
        variables.put("NSSMF_Response", "{}");
        script.updateJobStatusDetails(execution);
        assertTrue(variables.containsKey("responseDescriptor"));
        assertNull(variables.get("responseDescriptor"));
    }

    private void assertTimedOut() {
        assertEquals("TRUE", variables.get("JobStatusCompleted"));
        JSONObject descriptor =
                new JSONObject((String) variables.get("NSSMF_Response")).getJSONObject("responseDescriptor");
        assertEquals("failed", descriptor.getString("status"));
        assertEquals("timeout", descriptor.getString("statusDescription"));
    }

    private static String response(String status) {
        return "{\"responseDescriptor\":{\"status\":\"" + status + "\",\"statusDescription\":\"job-result\"}}";
    }
}
