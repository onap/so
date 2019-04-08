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
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * Reusable Mock StubResponses for Policy
 *
 */
public class StubResponsePolicy {

    public static void setupAllMocks() {

    }

    // start of Policy mocks
    public static void MockPolicyAbort(WireMockServer wireMockServer) {
        wireMockServer.stubFor(post(urlEqualTo("/pdp/api/getDecision")).withRequestBody(containing("BB1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("policyAbortResponse.json")));

        wireMockServer.stubFor(post(urlEqualTo("/pdp/api/getDecision")).withRequestBody(containing("UPDVnfI"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("policyAbortResponse.json")));

        wireMockServer.stubFor(post(urlEqualTo("/pdp/api/getDecision")).withRequestBody(containing("RPLVnfI"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("policyAbortResponse.json")));

        wireMockServer.stubFor(post(urlEqualTo("/pdp/api/getDecision")).withRequestBody(containing("VnfIPU"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("policyAbortResponse.json")));

        wireMockServer.stubFor(post(urlEqualTo("/pdp/api/getDecision")).withRequestBody(containing("VnfCU"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("policyAbortResponse.json")));



    }

    public static void MockPolicySkip(WireMockServer wireMockServer) {
        wireMockServer.stubFor(post(urlEqualTo("/pdp/api/getDecision")).withRequestBody(containing("BB1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("Policy/policySkipResponse.json")));

        wireMockServer.stubFor(post(urlEqualTo("/pdp/api/getDecision")).withRequestBody(containing("UPDVnfI"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("Policy/policySkipResponse.json")));

        wireMockServer.stubFor(post(urlEqualTo("/pdp/api/getDecision")).withRequestBody(containing("RPLVnfI"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("Policy/policySkipResponse.json")));

        wireMockServer.stubFor(post(urlEqualTo("/pdp/api/getDecision")).withRequestBody(containing("VnfIPU"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("Policy/policySkipResponse.json")));

        wireMockServer.stubFor(post(urlEqualTo("/pdp/api/getDecision")).withRequestBody(containing("VnfCU"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("Policy/policySkipResponse.json")));


    }


}
