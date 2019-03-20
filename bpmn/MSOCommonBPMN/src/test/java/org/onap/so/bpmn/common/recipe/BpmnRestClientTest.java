/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.recipe;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;

public class BpmnRestClientTest extends BaseTest{

    @Autowired
    private BpmnRestClient bpmnRestClient;

    @Test
    public void postTest() throws IOException, Exception{
        wireMockServer.stubFor(post(urlPathMatching("/testRecipeUri"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(org.springframework.http.HttpStatus.OK.value()).withBody("{}")));

        HttpResponse httpResponse = bpmnRestClient.post(
                "http://localhost:" + wireMockPort +"/testRecipeUri",
                "test-req-id",
                1000,
                "testRequestAction",
                "1234",
                "testServiceType",
                "testRequestDetails",
                "testRecipeparamXsd");

        assertNotNull(httpResponse);
        assertEquals(HttpStatus.SC_OK,httpResponse.getStatusLine().getStatusCode());
    }
}
