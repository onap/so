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

package org.onap.so.client.sdnc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.skyscreamer.jsonassert.JSONAssert;

public class SDNCClientIT extends BaseIntegrationTest {
    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/";

    @Test
    public void getTest() throws BadResponseException, MapperException, IOException {
        String responseJson =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "SDNCClientGetResponse.json")));
        String queryLink = "/topologyQuery";

        wireMockServer.stubFor(get(urlEqualTo(queryLink)).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseJson)));
        String response = SPY_sdncClient.get(queryLink);
        JSONAssert.assertEquals(responseJson, response, false);
    }

    @Test(expected = BadResponseException.class)
    public void post404Test() throws BadResponseException, MapperException, IOException {
        String responseJson =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "SDNCClientPut404Response.json")));

        String queryLink = "/restconf/operations/GENERIC-RESOURCE-API:network-topology-operation";

        wireMockServer.stubFor(post(urlMatching(queryLink)).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseJson)));

        SPY_sdncClient.post("", SDNCTopology.NETWORK);
    }

    @Test
    public void post200Test() throws BadResponseException, MapperException, IOException {
        String responseJson =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "SDNCClientPut200Response.json")));

        String queryLink = "/restconf/operations/GENERIC-RESOURCE-API:network-topology-operation";

        wireMockServer.stubFor(post(urlMatching(queryLink)).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseJson)));

        String response = SPY_sdncClient.post("", SDNCTopology.NETWORK);
        JSONAssert.assertEquals(responseJson, response, true);
    }
}
