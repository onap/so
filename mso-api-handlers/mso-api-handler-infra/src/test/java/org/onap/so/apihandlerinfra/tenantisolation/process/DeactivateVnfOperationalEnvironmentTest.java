/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.tenantisolation.process;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeactivateVnfOperationalEnvironmentTest extends BaseTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private DeactivateVnfOperationalEnvironment deactivate;

    private CloudOrchestrationRequest request = new CloudOrchestrationRequest();
    private String operationalEnvironmentId = "ff3514e3-5a33-55df-13ab-12abad84e7ff";
    private String requestId = "ff3514e3-5a33-55df-13ab-12abad84e7fe";

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void init() {
        wireMockServer.stubFor(post(urlPathEqualTo("/infraActiveRequests/"))
                .withRequestBody(containing("{\"requestId\":\"" + requestId
                        + "\",\"requestStatus\":\"COMPLETE\",\"statusMessage\":\"SUCCESSFUL"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));
    }

    @Test
    public void testDeactivateOperationalEnvironment() throws Exception {
        request.setOperationalEnvironmentId(operationalEnvironmentId);
        request.setRequestDetails(null);

        String json = "{\"operational-environment-status\" : \"ACTIVE\"}";

        wireMockServer.stubFor(
                get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)
                                .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(
                put(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(
                post(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withStatus(HttpStatus.SC_ACCEPTED)));

        InfraActiveRequests iar = new InfraActiveRequests();
        iar.setRequestId(requestId);
        iar.setOperationalEnvName("myOpEnv");
        iar.setRequestScope("create");
        iar.setRequestStatus("PENDING");
        iar.setRequestAction("UNKNOWN");
        wireMockServer.stubFor(get(urlPathEqualTo("/infraActiveRequests/" + requestId))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(iar)).withStatus(HttpStatus.SC_OK)));
        deactivate.execute(requestId, request);
    }

    @Test
    public void testDeactivateInvalidStatus() throws Exception {
        request.setOperationalEnvironmentId(operationalEnvironmentId);
        request.setRequestDetails(null);

        String json = "{\"operational-environment-status\" : \"SUCCESS\"}";

        wireMockServer.stubFor(
                get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)
                                .withStatus(HttpStatus.SC_ACCEPTED)));

        thrown.expect(ValidateException.class);
        thrown.expectMessage(startsWith("Invalid OperationalEnvironmentStatus on OperationalEnvironmentId: "));
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_BAD_REQUEST)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)));

        InfraActiveRequests iar = new InfraActiveRequests();
        iar.setRequestId(requestId);
        iar.setOperationalEnvName("myOpEnv");
        iar.setRequestScope("create");
        iar.setRequestStatus("PENDING");
        iar.setRequestAction("UNKNOWN");
        wireMockServer.stubFor(get(urlPathEqualTo("/infraActiveRequests/" + requestId))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(iar)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/infraActiveRequests/"))
                .withRequestBody(containing("{\"requestId\":\"" + requestId
                        + "\",\"requestStatus\":\"FAILED\",\"statusMessage\":\"FAILURE"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        deactivate.execute(requestId, request);
    }

    @Test
    public void testDeactivateInactiveStatus() throws Exception {
        request.setOperationalEnvironmentId(operationalEnvironmentId);
        request.setRequestDetails(null);

        String json = "{\"operational-environment-status\" : \"INACTIVE\"}";

        wireMockServer.stubFor(
                get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)
                                .withStatus(HttpStatus.SC_ACCEPTED)));

        InfraActiveRequests iar = new InfraActiveRequests();
        iar.setRequestId(requestId);
        iar.setOperationalEnvName("myOpEnv");
        iar.setRequestScope("create");
        iar.setRequestStatus("PENDING");
        iar.setRequestAction("UNKNOWN");
        wireMockServer.stubFor(get(urlPathEqualTo("/infraActiveRequests/" + requestId))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(iar)).withStatus(HttpStatus.SC_OK)));

        deactivate.execute(requestId, request);
    }

    @Test
    public void testDeactivateNullStatus() throws Exception {
        request.setOperationalEnvironmentId(operationalEnvironmentId);
        request.setRequestDetails(null);

        String json = "{\"operational-environment-status\" : \"\"}";

        wireMockServer.stubFor(
                get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)
                                .withStatus(HttpStatus.SC_ACCEPTED)));

        thrown.expect(ValidateException.class);
        thrown.expectMessage(startsWith("OperationalEnvironmentStatus is null on OperationalEnvironmentId: "));
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_BAD_REQUEST)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_DETAILED_SERVICE_ERROR)));
        deactivate.execute(requestId, request);
    }
}

