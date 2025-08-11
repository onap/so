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
package org.onap.so.asdc.activity;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.*;
import org.junit.Test;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.activity.beans.ActivitySpec;
import org.onap.so.asdc.activity.beans.ActivitySpecCreateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ActivitySpecsActionsTest extends BaseTest {
    @Autowired
    ActivitySpecsActions activitySpecsActions;

    @Test
    public void CreateActivitySpec_Test() throws Exception {
        String HOSTNAME = createURLWithPort("");

        ActivitySpec activitySpec = new ActivitySpec();
        activitySpec.setName("testActivitySpec");
        activitySpec.setDescription("Test Activity Spec");
        ActivitySpecCreateResponse activitySpecCreateResponse = new ActivitySpecCreateResponse();
        activitySpecCreateResponse.setId("testActivityId");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(activitySpecCreateResponse);
        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.OK.value()).withBody(body)));

        String activitySpecId = activitySpecsActions.createActivitySpec(HOSTNAME, activitySpec);
        assertEquals("testActivityId", activitySpecId);
    }

    @Test
    public void CreateActivitySpecReturnsCreated_Test() throws Exception {
        String HOSTNAME = createURLWithPort("");

        ActivitySpec activitySpec = new ActivitySpec();
        activitySpec.setName("testActivitySpec");
        activitySpec.setDescription("Test Activity Spec");
        ActivitySpecCreateResponse activitySpecCreateResponse = new ActivitySpecCreateResponse();
        activitySpecCreateResponse.setId("testActivityId");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(activitySpecCreateResponse);
        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.CREATED.value()).withBody(body)));

        String activitySpecId = activitySpecsActions.createActivitySpec(HOSTNAME, activitySpec);
        assertEquals("testActivityId", activitySpecId);
    }

    @Test
    public void CreateActivitySpecReturnsExists_Test() throws Exception {
        String HOSTNAME = createURLWithPort("");

        ActivitySpec activitySpec = new ActivitySpec();
        activitySpec.setName("testActivitySpec");
        activitySpec.setDescription("Test Activity Spec");
        ActivitySpecCreateResponse activitySpecCreateResponse = new ActivitySpecCreateResponse();
        activitySpecCreateResponse.setId("testActivityId");
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(activitySpecCreateResponse);
        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY.value()).withBody(body)));

        String activitySpecId = activitySpecsActions.createActivitySpec(HOSTNAME, activitySpec);
        assertEquals(null, activitySpecId);
    }

    @Test
    public void CertifyActivitySpec_Test() {
        String HOSTNAME = createURLWithPort("");

        String activitySpecId = "testActivitySpec";
        String urlPath = "/v1.0/activity-spec/testActivitySpec/versions/latest/actions";

        wireMockServer.stubFor(
                put(urlPathMatching(urlPath)).willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.OK.value())));

        boolean certificationResult = activitySpecsActions.certifyActivitySpec(HOSTNAME, activitySpecId);
        assertTrue(certificationResult);
    }

    @Test
    public void CertifyActivitySpecReturnsExists_Test() {
        String HOSTNAME = createURLWithPort("");

        String activitySpecId = "testActivitySpec";
        String urlPath = "/v1.0/activity-spec/testActivitySpec/versions/latest/actions";

        wireMockServer.stubFor(
                put(urlPathMatching(urlPath)).willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY.value())));

        boolean certificationResult = activitySpecsActions.certifyActivitySpec(HOSTNAME, activitySpecId);
        assertFalse(certificationResult);
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + wireMockPort + uri;
    }
}
