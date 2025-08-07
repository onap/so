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

package org.onap.so.adapters.sdnc.sdncrest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertTrue;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.adapters.sdnc.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;

public class BPRestCallbackTest extends BaseTest {

    @Autowired
    private BPRestCallback bpRestCallback;

    @Test
    public void sendTest() {
        String response = "<errors xmlns=\"urn:ietf:params:xml:ns:yang:ietf-restconf\">\n" + "\t\t//   <error>\n"
                + "\t\t//     <error-type>protocol</error-type>\n"
                + "\t\t//     <error-tag>malformed-message</error-tag>\n"
                + "\t\t//     <error-message>Error parsing input: The element type \"input\" must be terminated by the matching end-tag \"&lt;/input&gt;\".</error-message>\n"
                + "\t\t//   </error>\n" + "\t\t// </errors>";

        wireMockServer.stubFor(
                post(urlPathEqualTo("/sdnc")).willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(response).withStatus(HttpStatus.SC_MULTIPLE_CHOICES)));

        boolean responseCommon = bpRestCallback.send("http://localhost:" + wireMockPort + "/sdnc", "Test");
        assertTrue(responseCommon);
    }
}
