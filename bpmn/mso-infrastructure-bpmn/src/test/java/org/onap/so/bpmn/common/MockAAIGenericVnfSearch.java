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

package org.onap.so.bpmn.common;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;

public class MockAAIGenericVnfSearch {
    public MockAAIGenericVnfSearch(WireMockServer wireMockServer) {
        wireMockServer.stubFor(get(urlPathMatching("/aai/v[0-9]+/network/generic-vnfs"))
                .withQueryParam("vnf-name", equalTo("STMTN5MMSC23")).withQueryParam("depth", equalTo("1"))
                .willReturn(aResponse().withStatus(500).withHeader("Content-Type", "text/xml")
                        .withBodyFile("aaiFault.xml")));
        wireMockServer.stubFor(get(urlPathMatching("/aai/v[0-9]+/network/generic-vnfs"))
                .withQueryParam("vnf-name", equalTo("STMTN5MMSC22")).withQueryParam("depth", equalTo("1"))
                .willReturn(aResponse().withStatus(404)));
        wireMockServer.stubFor(get(
                urlPathMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/768073c7-f41f-4822-9323-b75962763d74"))
                        .willReturn(aResponse().withStatus(404)));
        wireMockServer.stubFor(get(urlPathMatching("/aai/v[0-9]+/network/generic-vnfs"))
                .withQueryParam("vnf-name", equalTo("STMTN5MMSC21")).withQueryParam("depth", equalTo("1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("GenericFlows/vf-module-vnf-1-collection.json")));
        wireMockServer.stubFor(get(
                urlPathMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721"))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBodyFile("GenericFlows/vf-module-vnf-1.json")));
        wireMockServer.stubFor(get(urlPathMatching("/aai/v[0-9]+/network/generic-vnfs"))
                .withQueryParam("vnf-name", equalTo("STMTN5MMSC20")).withQueryParam("depth", equalTo("1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("GenericFlows/vf-module-vnf-2-collection.json")));
        wireMockServer.stubFor(get(
                urlPathMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/2f6aee38-1e2a-11e6-82d1-ffc7d9ee8aa4"))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBodyFile("GenericFlows/vf-module-vnf-2.json")));
        wireMockServer.stubFor(get(
                urlPathMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c723"))
                        .willReturn(aResponse().withStatus(500).withHeader("Content-Type", "text/xml")
                                .withBodyFile("aaiFault.xml")));
        wireMockServer.stubFor(get(
                urlPathMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c722"))
                        .willReturn(aResponse().withStatus(404)));
        wireMockServer.stubFor(get(
                urlPathMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721"))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBodyFile("GenericFlows/vf-module-vnf-3.json")));
        wireMockServer.stubFor(get(
                urlPathMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c720"))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBodyFile("GenericFlows/vf-module-vnf-4.json")));
        wireMockServer.stubFor(get(
                urlPathMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c719"))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBodyFile("GenericFlows/vf-module-vnf-5.json")));
        wireMockServer.stubFor(get(
                urlPathMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718"))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBodyFile("GenericFlows/vf-module-vnf-6.json")));
        stubModuleVersionLookups(wireMockServer);
    }

    private void stubModuleVersionLookups(WireMockServer server) {
        ObjectMapper mapper = new ObjectMapper();
        for (int fixture = 1; fixture <= 6; fixture++) {
            String resource = "/__files/GenericFlows/vf-module-vnf-" + fixture + ".json";
            try (InputStream input = getClass().getResourceAsStream(resource)) {
                if (input == null) {
                    throw new IllegalStateException("Missing AAI fixture: " + resource);
                }
                JsonNode vnf = mapper.readTree(input);
                for (JsonNode module : vnf.path("vf-modules").path("vf-module")) {
                    String path = "/aai/v[0-9]+/network/generic-vnfs/generic-vnf/" + vnf.path("vnf-id").asText()
                            + "/vf-modules/vf-module/" + module.path("vf-module-id").asText();
                    server.stubFor(get(urlPathMatching(path))
                            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                    .withBody(mapper.writeValueAsString(module))));
                }
            } catch (IOException failure) {
                throw new UncheckedIOException("Cannot load AAI fixture: " + resource, failure);
            }
        }
    }
}
