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
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
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
