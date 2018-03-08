/*
 * ============LICENSE_START======================================================= 
 * ONAP - SO 
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

package org.openecomp.mso.bpmn.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

/**
 * Reusable Mock StubResponses for Policy
 */
public class StubResponsePolicy {

    public static void setupAllMocks() {

    }

    // start of Policy mocks
    public static void MockPolicyAbort() {
        stubFor(post(urlEqualTo("/pdp/api/getDecision"))
                .withRequestBody(containing("BB1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("policyAbortResponse.json")));

        stubFor(post(urlEqualTo("/pdp/api/getDecision"))
                .withRequestBody(containing("UPDVnfI"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("policyAbortResponse.json")));

        stubFor(post(urlEqualTo("/pdp/api/getDecision"))
                .withRequestBody(containing("RPLVnfI"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("policyAbortResponse.json")));


    }

    public static void MockPolicySkip() {
        stubFor(post(urlEqualTo("/pdp/api/getDecision"))
                .withRequestBody(containing("BB1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("Policy/policySkipResponse.json")));

        stubFor(post(urlEqualTo("/pdp/api/getDecision"))
                .withRequestBody(containing("UPDVnfI"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("Policy/policySkipResponse.json")));

        stubFor(post(urlEqualTo("/pdp/api/getDecision"))
                .withRequestBody(containing("RPLVnfI"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("Policy/policySkipResponse.json")));


    }


}
