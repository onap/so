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
package org.onap.asdc.activity;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.activity.DeployActivitySpecs;
import org.onap.so.asdc.activity.beans.ActivitySpecCreateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;

public class DeployActivitySpecsITTest extends BaseTest {
    @Mock
    protected Environment env;

    @Value("${wiremock.server.port}")
    private String wiremockPort;

    @Autowired
    private DeployActivitySpecs deployActivitySpecs;

    @InjectMocks
    @Spy
    DeployActivitySpecs deployActivitySpecsM;

    @Test
    public void deployActivitySpecsIT_Test() throws Exception {
        ActivitySpecCreateResponse activitySpecCreateResponse = new ActivitySpecCreateResponse();
        activitySpecCreateResponse.setId("testActivityId");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(activitySpecCreateResponse);

        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.OK.value()).withBody(body)));

        when(env.getProperty("mso.asdc.config.activity.endpoint")).thenReturn("http://localhost:8090");

        String urlPath = "/v1.0/activity-spec/testActivityId/versions/latest/actions";

        wireMockServer.stubFor(
                put(urlPathMatching(urlPath)).willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.OK.value())));

        deployActivitySpecs.deployActivities();
        assertTrue(activitySpecCreateResponse.getId().equals("testActivityId"));
    }

    @Test
    public void deployActivitySpecsIT_SDCEndpointDown_Test() throws Exception {
        ActivitySpecCreateResponse activitySpecCreateResponse = new ActivitySpecCreateResponse();
        activitySpecCreateResponse.setId("testActivityId");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(activitySpecCreateResponse);

        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.OK.value()).withBody(body)));

        when(env.getProperty("mso.asdc.config.activity.endpoint")).thenReturn("http://localhost:8090");

        String urlPath = "/v1.0/activity-spec/testActivityId/versions/latest/actions";

        wireMockServer.stubFor(
                put(urlPathMatching(urlPath)).willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.OK.value())));

        String host = "http://localhost:8090";
        when(deployActivitySpecsM.checkHttpServerUp(host)).thenReturn(false);
        deployActivitySpecsM.deployActivities();
        verify(0, putRequestedFor(urlEqualTo(urlPath)));
    }

}
