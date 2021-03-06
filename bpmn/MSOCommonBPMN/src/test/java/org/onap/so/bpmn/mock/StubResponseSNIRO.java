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

package org.onap.so.bpmn.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * Please describe the StubResponseSNIRO.java class
 *
 */
public class StubResponseSNIRO {

    public static void setupAllMocks() {

    }

    public static void mockSNIRO(WireMockServer wireMockServer) {
        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/v2/placement"))
                .willReturn(aResponse().withStatus(202).withHeader("Content-Type", "application/json")));
    }

    public static void mockSNIRO(WireMockServer wireMockServer, String responseFile) {
        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/v2/placement")).willReturn(
                aResponse().withStatus(202).withHeader("Content-Type", "application/json").withBodyFile(responseFile)));
    }

    public static void mockSNIRO_400(WireMockServer wireMockServer) {
        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/v2/placement"))
                .willReturn(aResponse().withStatus(400).withHeader("Content-Type", "application/json")));
    }

    public static void mockSNIRO_500(WireMockServer wireMockServer) {
        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/v2/placement"))
                .willReturn(aResponse().withStatus(500).withHeader("Content-Type", "application/json")));
    }

}
