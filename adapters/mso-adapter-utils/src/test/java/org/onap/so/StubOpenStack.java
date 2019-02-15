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

package org.onap.so;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.apache.http.HttpStatus;

public class StubOpenStack {

    public static void mockOpenStackResponseAccess(int port) throws IOException {
        stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse().withHeader("Content-Type", "application/json")
                .withBody(getBodyFromFile("OpenstackResponse_Access.json", port, "/mockPublicUrl"))
                .withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenStackResponseUnauthorized(int port) throws IOException {
        stubFor(
            post(urlPathEqualTo("/v2.0/tokens"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBody(getBodyFromFile("OpenstackResponse_Access.json", port, "/mockPublicUrl"))
                    .withStatus(HttpStatus.SC_UNAUTHORIZED)));
    }

    public static void mockOpenStackDelete(String id) {
        stubFor(delete(urlMatching("/mockPublicUrl/stacks/" + id)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenStackGet(String id) {
        stubFor(
            get(urlPathEqualTo("/mockPublicUrl/stacks/" + id))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                    .withBodyFile("OpenstackResponse_Stack_Created.json")
                    .withStatus(HttpStatus.SC_OK)));
    }


    public static void mockOpenStackPostStack_200(String filename) {
        stubFor(post(urlPathEqualTo("/mockPublicUrl/stacks")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile(filename).withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenStackPostTenantWithBodyFile_200() throws IOException {
        stubFor(post(urlPathEqualTo("/mockPublicUrl/tenants"))
                .willReturn(aResponse().withBodyFile("OpenstackResponse_Tenant.json").withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenStackGetTenantByName(String tenantName) throws IOException {
        stubFor(get(urlMatching("/mockPublicUrl/tenants/[?]name=" + tenantName))
                .willReturn(aResponse().withBodyFile("OpenstackResponse_Tenant.json").withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenStackGetTenantById(String tenantId) throws IOException {
        stubFor(get(urlPathEqualTo("/mockPublicUrl/tenants/tenantId"))
                .willReturn(aResponse().withBodyFile("OpenstackResponse_Tenant.json").withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenStackDeleteTenantById_200(String tenantId) {
        stubFor(delete(urlPathEqualTo("/mockPublicUrl/tenants/" + tenantId)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenStackGetUserById(String user) {
        stubFor(get(urlPathEqualTo("/mockPublicUrl/users/" + user)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("OpenstackResponse_User.json").withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenStackGetUserByName(String userName) {
        stubFor(get(urlMatching("/mockPublicUrl/users/[?]name=" + userName)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("OpenstackResponse_User.json").withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenStackGetUserByName_500(String userName) {
        stubFor(get(urlMatching("/mockPublicUrl/users/[?]name=" + userName)).willReturn(aResponse()
                .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
    }

    public static void mockOpenStackGetRoles_200(String roleFor) {
        stubFor(get(urlPathEqualTo("/mockPublicUrl/" + roleFor + "/roles")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile("OpenstackResponse_Roles.json").withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenstackPostNetwork(String responseFile) {
        stubFor(post(urlPathEqualTo("/mockPublicUrl/v2.0/networks")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile(responseFile)
                .withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenstackPutNetwork(String responseFile, String networkId) {
        stubFor(put(urlPathEqualTo("/mockPublicUrl/v2.0/networks/"+networkId)).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBodyFile(responseFile)
                .withStatus(HttpStatus.SC_OK)));
    }
    
    public static void mockOpenStackGetNeutronNetwork(String filename,String networkId) {
        stubFor(get(urlPathEqualTo("/mockPublicUrl/v2.0/networks/"+ networkId))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile(filename).withStatus(HttpStatus.SC_OK)));
    }

    public static void mockOpenStackGetNeutronNetwork_500(String networkId) {
        stubFor(get(urlPathEqualTo("/mockPublicUrl/v2.0/networks/"+ networkId))
                .willReturn(aResponse().withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
    }

    public static void mockOpenStackDeleteNeutronNetwork(String networkId) {
        stubFor(delete(urlPathEqualTo("/mockPublicUrl/v2.0/networks/" + networkId))
                .willReturn(aResponse().withStatus(HttpStatus.SC_OK)));
    }

    private static String readFile(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        }
    }

    public static String getBodyFromFile(String fileName, int port, String urlPath) throws IOException {
        return readFile("src/test/resources/__files/" + fileName).replaceAll("port", "http://localhost:" + port + urlPath);
    }
}
