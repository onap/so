/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.adapters.sdnc.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertNotNull;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.adapters.sdnc.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertTrue;


public class SDNCRestClientTest extends BaseTest {

    @Autowired
    private SDNCRestClient sdncClient;

    @Test
    public void getSdncRespTestException() {

        RequestTunables rt = new RequestTunables("", "", "", "");
        rt.setTimeout("1000");
        rt.setReqMethod("POST");
        rt.setSdncUrl("http://localhost:" + wireMockPort + "/sdnc");

        stubFor(post(urlPathEqualTo("/sdnc"))
                .willReturn(aResponse().withHeader("Content-Type", "application/xml").withBody("").withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        SDNCResponse response = sdncClient.getSdncResp("", rt);
        assertNotNull(response);
    }

    @Test
    public void executeRequestInterrupted() {
        Thread.currentThread().interrupt();
        sdncClient.executeRequest(null);
        assertTrue(Thread.interrupted());
    }
}
