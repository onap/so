/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.onap.so.apihandlerinfra;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import jakarta.ws.rs.core.MediaType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.ResourceAccessException;

public class CamundaRequestHandlerTest extends BaseTest {

    @Autowired
    private CamundaRequestHandler camundaRequestHandler;

    @Value("${wiremock.server.port}")
    private String wiremockPort;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void timeoutTest() {
        wireMockServer.stubFor(get(
                ("/sobpmnengine/history/process-instance?processInstanceBusinessKey=6718de35-b9a5-4670-b19f-a0f4ac22bfaf&active=true&maxResults=1"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile("Camunda/HistoryCheckResponse.json")
                                .withStatus(org.apache.http.HttpStatus.SC_OK).withFixedDelay(40000)));

        thrown.expect(ResourceAccessException.class);
        camundaRequestHandler.getCamundaProcessInstanceHistory("6718de35-b9a5-4670-b19f-a0f4ac22bfaf", false, true,
                false);
    }
}
