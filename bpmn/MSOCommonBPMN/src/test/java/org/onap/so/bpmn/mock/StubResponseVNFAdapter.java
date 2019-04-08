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
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * Please describe the StubResponseVNF.java class
 */
public class StubResponseVNFAdapter {

    public static void mockVNFAdapter(WireMockServer wireMockServer) {
        wireMockServer.stubFor(post(urlEqualTo("/vnfs/VnfAdapterAsync")).willReturn(aResponse().withStatus(200)));
    }

    public static void mockVNFAdapter(WireMockServer wireMockServer, String responseFile) {
        wireMockServer.stubFor(post(urlEqualTo("/vnfs/VnfAdapterAsync")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "text/xml").withBodyFile(responseFile)));
    }

    public static void mockVNFAdapter_500(WireMockServer wireMockServer) {
        wireMockServer.stubFor(post(urlEqualTo("/vnfs/VnfAdapterAsync")).willReturn(aResponse().withStatus(500)));
    }

    public static void mockVNFPost(WireMockServer wireMockServer, String vfModuleId, int statusCode, String vnfId) {
        wireMockServer.stubFor(post(urlEqualTo("/services/rest/v1/vnfs" + vnfId + "/vf-modules" + vfModuleId))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
        wireMockServer.stubFor(post(urlEqualTo("/services/rest/v1/vnfs/" + vnfId + "/vf-modules" + vfModuleId))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
    }

    public static void mockVNFPut(WireMockServer wireMockServer, String vfModuleId, int statusCode) {
        wireMockServer.stubFor(put(urlEqualTo("/services/rest/v1/vnfsvnfId/vf-modules" + vfModuleId))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
        wireMockServer.stubFor(put(urlEqualTo("/services/rest/v1/vnfs/vnfId/vf-modules" + vfModuleId))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
    }

    public static void mockVNFPut(WireMockServer wireMockServer, String vnfId, String vfModuleId, int statusCode) {
        wireMockServer.stubFor(put(urlEqualTo("/services/rest/v1/vnfs" + vnfId + "/vf-modules" + vfModuleId))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
        wireMockServer.stubFor(put(urlEqualTo("/services/rest/v1/vnfs/" + vnfId + "/vf-modules" + vfModuleId))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
    }

    public static void mockVNFDelete(WireMockServer wireMockServer, String vnfId, String vfModuleId, int statusCode) {
        wireMockServer.stubFor(delete(urlEqualTo("/services/rest/v1/vnfs" + vnfId + "/vf-modules" + vfModuleId))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
        wireMockServer.stubFor(delete(urlEqualTo("/services/rest/v1/vnfs/" + vnfId + "/vf-modules" + vfModuleId))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
    }

    public static void mockVNFRollbackDelete(WireMockServer wireMockServer, String vfModuleId, int statusCode) {
        wireMockServer.stubFor(delete(urlEqualTo("/services/rest/v1/vnfsvnfId/vf-modules" + vfModuleId + "/rollback"))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
        wireMockServer.stubFor(delete(urlEqualTo("/services/rest/v1/vnfs/vnfId/vf-modules" + vfModuleId + "/rollback"))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
    }

    public static void mockPutVNFVolumeGroup(WireMockServer wireMockServer, String volumeGroupId, int statusCode) {
        wireMockServer.stubFor(put(urlEqualTo("/vnfs/v1/volume-groups/" + volumeGroupId))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
        wireMockServer.stubFor(put(urlEqualTo("/vnfs/rest/v1/volume-groups/" + volumeGroupId))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
    }

    public static void mockPutVNFVolumeGroupRollback(WireMockServer wireMockServer, String volumeGroupId,
            int statusCode) {
        wireMockServer.stubFor(delete(urlMatching("/vnfs/v1/volume-groups/" + volumeGroupId + "/rollback"))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
        wireMockServer.stubFor(delete(urlMatching("/vnfs/rest/v1/volume-groups/" + volumeGroupId + "/rollback"))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
    }

    public static void mockPostVNFVolumeGroup(WireMockServer wireMockServer, int statusCode) {
        wireMockServer.stubFor(post(urlEqualTo("/vnfs/v1/volume-groups"))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
        wireMockServer.stubFor(post(urlEqualTo("/vnfs/rest/v1/volume-groups"))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
    }

    public static void mockVNFAdapterRest(WireMockServer wireMockServer, String vnfId) {
        wireMockServer.stubFor(post(urlEqualTo("/services/rest/v1/vnfs" + vnfId + "/vf-modules"))
                .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(post(urlEqualTo("/services/rest/v1/vnfs/" + vnfId + "/vf-modules"))
                .willReturn(aResponse().withStatus(200)));
    }

    public static void mockVNFAdapterRest_500(WireMockServer wireMockServer, String vnfId) {
        wireMockServer.stubFor(post(urlEqualTo("/services/rest/v1/vnfs" + vnfId + "/vf-modules"))
                .willReturn(aResponse().withStatus(500)));
        wireMockServer.stubFor(post(urlEqualTo("/services/rest/v1/vnfs/" + vnfId + "/vf-modules"))
                .willReturn(aResponse().withStatus(500)));
    }

    public static void mockVfModuleDelete(WireMockServer wireMockServer, String volumeGroupId) {
        wireMockServer.stubFor(delete(urlMatching("/vnfs/v1/volume-groups/" + volumeGroupId))
                .willReturn(aResponse().withStatus(202).withHeader("Content-Type", "application/xml")));
        wireMockServer.stubFor(delete(urlMatching("/vnfs/rest/v1/volume-groups/" + volumeGroupId))
                .willReturn(aResponse().withStatus(202).withHeader("Content-Type", "application/xml")));
    }

    public static void mockVfModuleDelete(WireMockServer wireMockServer, String volumeGroupId, int statusCode) {
        wireMockServer.stubFor(delete(urlMatching("/vnfs/v1/volume-groups/78987"))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
        wireMockServer.stubFor(delete(urlMatching("/vnfs/rest/v1/volume-groups/78987"))
                .willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/xml")));
    }
}
