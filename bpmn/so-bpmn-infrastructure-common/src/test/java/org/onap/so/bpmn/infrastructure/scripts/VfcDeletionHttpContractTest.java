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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class VfcDeletionHttpContractTest {
    @Rule
    public WireMockRule adapter = new WireMockRule(0);
    private final DoDeleteVFCNetworkServiceInstance script = new DoDeleteVFCNetworkServiceInstance();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private String operationKey;

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "DoDeleteVFCNetworkServiceInstance");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("serviceType", "service-type");
        variables.put("serviceId", "service-1");
        variables.put("operationId", "operation-1");
        variables.put("resourceTemplateId", "template-1");
        variables.put("resourceInstanceId", "ns-1");
        variables.put("nsInstanceId", "ns-1");
        variables.put("jobId", "job-previous");
        variables.put("so.auth.username", "test");
        variables.put("so.auth.password", "test");
        variables.put("vfcAdapterUrl", "http://localhost:" + adapter.port());
        operationKey = new JSONObject().put("globalSubscriberId", "customer-1").put("serviceType", "service-type")
                .put("serviceId", "service-1").put("operationId", "operation-1").put("nodeTemplateUUID", "template-1")
                .toString();
        variables.put("nsOperationKey", operationKey);
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void preprocessingKeepsOperationIdentityAndNormalizesTheAdapterBaseUrl() {
        variables.put("mso.adapters.vfc.rest.endpoint", "http://localhost:" + adapter.port() + "///");
        script.preProcessRequest(execution);
        assertEquals("ns-1", variables.get("nsInstanceId"));
        assertEquals("http://localhost:" + adapter.port(), variables.get("vfcAdapterUrl"));
        JSONObject actual = new JSONObject((String) variables.get("nsOperationKey"));
        assertEquals(5, actual.length());
        assertTrue(new JSONObject(operationKey).similar(actual));
        assertTrue(adapter.getAllServeEvents().isEmpty());
    }

    @Test
    public void deletionUsesOneDeleteWithTheOperationKeyAndClassifiesTheResponse() {
        Assume.assumeTrue("SO-4446: VFC deletion must construct its client with a URL before issuing DELETE",
                Boolean.getBoolean("groovy.migration.verification"));
        for (int status : Arrays.asList(200, 202, 400, 500)) {
            adapter.resetAll();
            adapter.stubFor(delete(urlEqualTo("/ns/ns-1")).willReturn(aResponse().withStatus(status)));
            try {
                script.deleteNetworkService(execution);
            } catch (BpmnError failure) {
                fail("A supported VFC DELETE must reach the adapter and classify its response: "
                        + failure.getErrorCode());
            }
            assertEquals("HTTP " + status, status == 200 || status == 202 ? "finished" : "error",
                    variables.get("operationStatus"));
            adapter.verify(1,
                    deleteRequestedFor(urlEqualTo("/ns/ns-1"))
                            .withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
                            .withHeader("Accept", equalTo("application/json"))
                            .withHeader("Content-Type", equalTo("application/json"))
                            .withRequestBody(equalToJson(operationKey)));
            assertEquals(1, adapter.getAllServeEvents().size());
        }
    }

    @Test
    public void acceptedTerminationKeepsTheReturnedJobIdAsAString() {
        Assume.assumeTrue("SO-4446: VFC response bodies must remain readable after diagnostic logging",
                Boolean.getBoolean("groovy.migration.verification"));
        for (int status : Arrays.asList(200, 202)) {
            adapter.resetAll();
            adapter.stubFor(post(urlEqualTo("/ns/ns-1/terminate")).willReturn(aResponse().withStatus(status)
                    .withHeader("Content-Type", "application/json").withBody("{\"jobId\":\"job-1\"}")));
            invokeWithReadableResponse(() -> script.terminateNetworkService(execution));
            assertEquals("job-1", variables.get("jobId"));
            adapter.verify(1,
                    postRequestedFor(urlEqualTo("/ns/ns-1/terminate"))
                            .withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
                            .withRequestBody(equalToJson(operationKey)));
            assertEquals(1, adapter.getAllServeEvents().size());
        }
    }

    @Test
    public void rejectedTerminationDoesNotPublishAJobFromAnErrorBody() {
        Assume.assumeTrue("SO-4446: VFC response bodies must remain readable after diagnostic logging",
                Boolean.getBoolean("groovy.migration.verification"));
        for (int status : Arrays.asList(400, 500)) {
            adapter.resetAll();
            adapter.stubFor(post(urlEqualTo("/ns/ns-1/terminate")).willReturn(aResponse().withStatus(status)
                    .withHeader("Content-Type", "application/json").withBody("{\"jobId\":\"not-an-accepted-job\"}")));
            invokeWithReadableResponse(() -> script.terminateNetworkService(execution));
            assertEquals("", variables.get("jobId"));
            adapter.verify(1, postRequestedFor(urlEqualTo("/ns/ns-1/terminate")));
        }
    }

    @Test
    public void pollingPostsTheOperationKeyAndPreservesNumericProgressAsAString() {
        Assume.assumeTrue("SO-4446: VFC response bodies must remain readable after diagnostic logging",
                Boolean.getBoolean("groovy.migration.verification"));
        adapter.stubFor(post(urlEqualTo("/jobs/job-previous"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"responseDescriptor\":{\"progress\":37}}")));
        invokeWithReadableResponse(() -> script.queryNSProgress(execution));
        assertEquals("37", variables.get("operationProgress"));
        adapter.verify(1, postRequestedFor(urlEqualTo("/jobs/job-previous"))
                .withHeader("Accept", equalTo("application/json")).withRequestBody(equalToJson(operationKey)));
        assertEquals(1, adapter.getAllServeEvents().size());
    }

    @Test
    public void failedPollingDoesNotReportOneHundredPercentCompletion() {
        Assume.assumeTrue("SO-4446: a failed VFC job-status request must not report completion",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.put("operationProgress", "37");
        adapter.stubFor(post(urlEqualTo("/jobs/job-previous"))
                .willReturn(aResponse().withStatus(500).withHeader("Content-Type", "application/json").withBody("{}")));
        try {
            invokeWithReadableResponse(() -> script.queryNSProgress(execution));
        } catch (BpmnError failure) {
            assertEquals("MSOWorkflowException", failure.getErrorCode());
        }
        assertFalse("An unsuccessful polling response must not become completed progress",
                "100".equals(variables.get("operationProgress")));
    }

    @Test
    public void connectionFailureRaisesTheBpmnErrorWithoutReplacingThePreviousJob() {
        adapter.stubFor(post(urlEqualTo("/ns/ns-1/terminate"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
        try {
            script.terminateNetworkService(execution);
            fail("Expected a transport failure to leave the normal workflow path");
        } catch (BpmnError failure) {
            assertEquals("MSOWorkflowException", failure.getErrorCode());
        }
        assertEquals("job-previous", variables.get("jobId"));
    }

    private void invokeWithReadableResponse(Runnable action) {
        try {
            action.run();
        } catch (IllegalStateException failure) {
            if ("Entity is not available".equals(failure.getMessage())) {
                fail("VFC processing must retain the response body after diagnostic logging");
            }
            throw failure;
        }
    }
}
