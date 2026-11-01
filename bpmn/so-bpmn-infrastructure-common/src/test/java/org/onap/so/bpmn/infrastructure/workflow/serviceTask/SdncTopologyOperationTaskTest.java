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
package org.onap.so.bpmn.infrastructure.workflow.serviceTask;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.HeaderUtil;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

public class SdncTopologyOperationTaskTest {

    private static final String SERVICE_TOPOLOGY =
            "/restconf/operations/GENERIC-RESOURCE-API:service-topology-operation";
    private static final String NETWORK_TOPOLOGY =
            "/restconf/operations/GENERIC-RESOURCE-API:network-topology-operation";
    private static final String REQUESTS_DB = "/dbadapters/RequestsDbAdapter";
    private static final String RESOURCE_OPERATIONS = "/v1/request-db/resource-operations";

    @Rule
    public WireMockRule wireMock = new WireMockRule(0);

    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void setUp() {
        variables.put("serviceInstanceId", "service-1");
        variables.put("operationId", "operation-1");
        variables.put("resourceUUID", "resource-1");
        variables.put("msoRequestId", "request-1");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        wireMock.stubFor(post(urlEqualTo(REQUESTS_DB)).willReturn(aResponse().withStatus(200)));
    }

    @Test
    public void restNetworkDelegatePreservesResourceFieldsAndSdncRequest() throws Exception {
        configureRestResource(200);
        wireMock.stubFor(post(urlEqualTo(NETWORK_TOPOLOGY)).willReturn(aResponse().withStatus(200)));

        restTask("SdncNetworkTopologyRequestDbHttpTask").execute(execution);

        assertEquals(true, variables.get("SDNCA_SuccessIndicator"));
        verifyRestProgress(3);
        wireMock.verify(1, putRequestedFor(urlPathEqualTo(RESOURCE_OPERATIONS))
                .withRequestBody(matchingJsonPath("$.progress", equalTo("100"))));
        wireMock.verify(1, postRequestedFor(urlEqualTo(NETWORK_TOPOLOGY)));
    }

    @Test
    public void restServiceDelegatePreservesStatusWhenOnlyProgressChanges() throws Exception {
        configureRestResource(200);
        wireMock.stubFor(post(urlEqualTo(SERVICE_TOPOLOGY)).willReturn(aResponse().withStatus(200)));

        restTask("SdncServiceTopologyRequestDbHttpTask").execute(execution);

        assertEquals(true, variables.get("SDNCA_SuccessIndicator"));
        verifyRestProgress(2);
        wireMock.verify(2, putRequestedFor(urlPathEqualTo(RESOURCE_OPERATIONS))
                .withRequestBody(matchingJsonPath("$.status", equalTo("existing-status"))));
        wireMock.verify(1, postRequestedFor(urlEqualTo(SERVICE_TOPOLOGY)));
    }

    @Test
    public void restMissingResourceRemainsBestEffort() throws Exception {
        configureRestResource(404);
        wireMock.stubFor(post(urlEqualTo(NETWORK_TOPOLOGY)).willReturn(aResponse().withStatus(200)));

        restTask("SdncNetworkTopologyRequestDbHttpTask").execute(execution);

        assertEquals(true, variables.get("SDNCA_SuccessIndicator"));
        wireMock.verify(3, getRequestedFor(urlPathEqualTo(RESOURCE_OPERATIONS)));
        wireMock.verify(0, putRequestedFor(urlPathEqualTo(RESOURCE_OPERATIONS)));
        wireMock.verify(0, postRequestedFor(urlEqualTo(REQUESTS_DB)));
    }

    @Test
    public void restSdncFailureKeepsFailureIndicatorAndTerminalProgress() throws Exception {
        configureRestResource(200);
        wireMock.stubFor(post(urlEqualTo(NETWORK_TOPOLOGY)).willReturn(aResponse().withStatus(500)));

        restTask("SdncNetworkTopologyRequestDbHttpTask").execute(execution);

        assertEquals(false, variables.get("SDNCA_SuccessIndicator"));
        verifyRestProgress(3);
        wireMock.verify(1,
                putRequestedFor(urlPathEqualTo(RESOURCE_OPERATIONS))
                        .withRequestBody(matchingJsonPath("$.status", equalTo("error")))
                        .withRequestBody(matchingJsonPath("$.progress", equalTo("100"))));
    }

    private void configureRestResource(int status) {
        variables.put("mso.adapters.requestDb.endpoint", wireMock.baseUrl());
        variables.put("so.auth.username", "test-user");
        variables.put("so.auth.password", java.util.UUID.randomUUID().toString());
        wireMock.stubFor(get(urlPathEqualTo(RESOURCE_OPERATIONS))
                .willReturn(aResponse().withStatus(status).withHeader("Content-Type", "application/json")
                        .withBody("{\"serviceId\":\"service-1\",\"operationId\":\"operation-1\","
                                + "\"resourceTemplateUUID\":\"resource-1\",\"operType\":\"CREATE\",\"jobId\":\"job-1\","
                                + "\"status\":\"existing-status\",\"errorCode\":\"existing-error\"}")));
        wireMock.stubFor(put(urlPathEqualTo(RESOURCE_OPERATIONS)).willReturn(aResponse().withStatus(204)));
    }

    private AbstractSdncOperationTask restTask(String name) throws Exception {
        AbstractSdncOperationTask task = spy((AbstractSdncOperationTask) Class
                .forName("org.onap.so.bpmn.infrastructure.workflow.serviceTask." + name).getDeclaredConstructor()
                .newInstance());
        doReturn(wireMock.baseUrl()).when(task).getSdncHost();
        doReturn("127.0.0.1").when(task).getSdncIp();
        doReturn(String.valueOf(wireMock.port())).when(task).getSdncPort();
        return task;
    }

    private void verifyRestProgress(int calls) {
        wireMock.verify(calls,
                getRequestedFor(urlPathEqualTo(RESOURCE_OPERATIONS)).withQueryParam("serviceId", equalTo("service-1"))
                        .withQueryParam("operationId", equalTo("operation-1"))
                        .withQueryParam("resourceTemplateUUID", equalTo("resource-1")));
        wireMock.verify(calls,
                putRequestedFor(urlPathEqualTo(RESOURCE_OPERATIONS))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization",
                                equalTo(org.onap.so.utils.BasicAuth.header("test-user",
                                        (String) variables.get("so.auth.password"))))
                        .withRequestBody(matchingJsonPath("$.operType", equalTo("CREATE")))
                        .withRequestBody(matchingJsonPath("$.jobId", equalTo("job-1")))
                        .withRequestBody(matchingJsonPath("$.errorCode", equalTo("existing-error"))));
        wireMock.verify(0, postRequestedFor(urlEqualTo(REQUESTS_DB)));
    }

    @Test
    public void serviceTopologyOperationIsPostedToSdncHost() {
        wireMock.stubFor(post(urlEqualTo(SERVICE_TOPOLOGY)).willReturn(aResponse().withStatus(200)));

        withEnvironment(new SdncServiceTopologyOperationTask() {
            @Override
            protected String getSdncHost() {
                return wireMock.baseUrl();
            }
        }).execute(execution);

        assertEquals(true, variables.get("SDNCA_SuccessIndicator"));
        wireMock.verify(1,
                postRequestedFor(urlEqualTo(SERVICE_TOPOLOGY))
                        .withHeader("Authorization", equalTo(HeaderUtil.DefaulAuth))
                        .withRequestBody(matchingJsonPath(
                                "$['GENERIC-RESOURCE-API:input']['GENERIC-RESOURCE-API:sdnc-request-header']"
                                        + "['GENERIC-RESOURCE-API:svc-request-id']",
                                equalTo("request-1"))));
    }

    @Test
    public void networkTopologyOperationIsPostedToSdncIpAndPort() {
        wireMock.stubFor(post(urlEqualTo(NETWORK_TOPOLOGY)).willReturn(aResponse().withStatus(200)));

        withEnvironment(networkTask()).execute(execution);

        assertEquals(true, variables.get("SDNCA_SuccessIndicator"));
        wireMock.verify(1, postRequestedFor(urlEqualTo(NETWORK_TOPOLOGY)).withHeader("Authorization",
                equalTo(HeaderUtil.DefaulAuth)));
    }

    @Test
    public void sdncErrorStatusFailsTheTask() {
        wireMock.stubFor(post(urlEqualTo(NETWORK_TOPOLOGY)).willReturn(aResponse().withStatus(500)));

        withEnvironment(networkTask()).execute(execution);

        assertEquals(false, variables.get("SDNCA_SuccessIndicator"));
    }

    private SdncNetworkTopologyOperationTask networkTask() {
        return new SdncNetworkTopologyOperationTask() {
            @Override
            protected String getSdncIp() {
                return "127.0.0.1";
            }

            @Override
            protected String getSdncPort() {
                return String.valueOf(wireMock.port());
            }
        };
    }

    private <T extends AbstractSdncOperationTask> T withEnvironment(T task) {
        ReflectionTestUtils.setField(task, AbstractSdncOperationTask.class, "env", new MockEnvironment()
                .withProperty("mso.adapters.openecomp.db.endpoint", wireMock.baseUrl() + REQUESTS_DB), null);
        return task;
    }
}
