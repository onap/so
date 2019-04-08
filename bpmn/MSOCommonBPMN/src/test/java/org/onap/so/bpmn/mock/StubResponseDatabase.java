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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * Stub response class for Database stubs including database adapter, catalog db, and other databases.
 */
public class StubResponseDatabase {

    public static void setupAllMocks() {

    }

    public static void MockUpdateRequestDB(WireMockServer wireMockServer, String fileName) {
        wireMockServer.stubFor(post(urlEqualTo("/services/RequestsDbAdapter"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml").withBodyFile(fileName)));
    }

    public static void mockUpdateRequestDB(WireMockServer wireMockServer, int statusCode, String reponseFile) {
        wireMockServer.stubFor(post(urlEqualTo("/services/RequestsDbAdapter")).willReturn(
                aResponse().withStatus(statusCode).withHeader("Content-Type", "text/xml").withBodyFile(reponseFile)));
    }

    public static void MockGetAllottedResourcesByModelInvariantId(WireMockServer wireMockServer,
            String modelInvariantId, String responseFile) {
        wireMockServer
                .stubFor(get(urlEqualTo("/v1/serviceAllottedResources?serviceModelInvariantUuid=" + modelInvariantId))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBodyFile(responseFile)));
    }

    public static void MockGetAllottedResourcesByModelInvariantId_500(WireMockServer wireMockServer,
            String modelInvariantId, String responseFile) {
        wireMockServer
                .stubFor(get(urlEqualTo("/v1/serviceAllottedResources?serviceModelInvariantUuid=" + modelInvariantId))
                        .willReturn(aResponse().withStatus(500)));
    }

    public static void MockGetVnfCatalogDataCustomizationUuid(WireMockServer wireMockServer,
            String vnfModelCustomizationUuid, String responseFile) {
        wireMockServer.stubFor(get(urlEqualTo("/v2/serviceVnfs?vnfModelCustomizationUuid=" + vnfModelCustomizationUuid))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile(responseFile)));
    }

    public static void MockGetVfModuleByModelNameCatalogData(WireMockServer wireMockServer, String vfModuleModelName,
            String responseFile) {
        wireMockServer.stubFor(get(urlEqualTo("/v2/vfModules?vfModuleModelName=" + vfModuleModelName)).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBodyFile(responseFile)));
    }

    public static void MockGetServiceResourcesCatalogData(WireMockServer wireMockServer,
            String serviceModelInvariantUuid, String serviceModelVersion, String responseFile) {
        wireMockServer.stubFor(get(urlEqualTo("/ecomp/mso/catalog/v2/serviceResources?serviceModelInvariantUuid="
                + serviceModelInvariantUuid + "&serviceModelVersion=" + serviceModelVersion))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBodyFile(responseFile)));
    }

    public static void MockGetServiceResourcesCatalogData(WireMockServer wireMockServer,
            String serviceModelInvariantUuid, String responseFile) {
        wireMockServer.stubFor(get(urlEqualTo(
                "/ecomp/mso/catalog/v2/serviceResources?serviceModelInvariantUuid=" + serviceModelInvariantUuid))
                        .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                                .withBodyFile(responseFile)));
    }

    public static void MockGetServiceResourcesCatalogDataByModelUuid(WireMockServer wireMockServer,
            String serviceModelUuid, String responseFile) {
        wireMockServer.stubFor(get(urlEqualTo("/v2/serviceResources?serviceModelUuid=" + serviceModelUuid)).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBodyFile(responseFile)));
    }

    public static void MockPostRequestDB(WireMockServer wireMockServer) {
        wireMockServer.stubFor(post(urlEqualTo("/dbadapters/RequestsDbAdapter"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml")));
    }
}
