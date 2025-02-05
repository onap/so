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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.onap.so.db.catalog.beans.AuthenticationType;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.ServerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.github.tomakehurst.wiremock.WireMockServer;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
public abstract class BaseTest extends TestDataSetup {

    @Value("${wiremock.server.port}")
    protected int wireMockPort;
    @Autowired
    protected WireMockServer wireMockServer;

    @After
    public void after() {
        wireMockServer.resetAll();
    }

    protected static String getBody(String body, int port, String urlPath) throws IOException {
        return body.replaceAll("port", "http://localhost:" + port + urlPath);
    }

    @Before
    public void init() throws IOException {
        CloudIdentity identity = getCloudIdentity();
        CloudSite cloudSite = getCloudSite(identity);
        mockCloud(identity, cloudSite);
    }

    private void mockCloud(CloudIdentity identity, CloudSite cloudSite) throws IOException {
        wireMockServer.stubFor(get(urlPathEqualTo("/cloudSite/MTN13")).willReturn(aResponse()
                .withBody(getBody(mapper.writeValueAsString(cloudSite), wireMockPort, ""))
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/cloudSite/DEFAULT")).willReturn(aResponse()
                .withBody(getBody(mapper.writeValueAsString(cloudSite), wireMockPort, ""))
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/cloudIdentity/mtn13")).willReturn(aResponse()
                .withBody(getBody(mapper.writeValueAsString(identity), wireMockPort, ""))
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_OK)));
    }

    protected CloudIdentity getCloudIdentity() {
        CloudIdentity identity = new CloudIdentity();
        identity.setId("mtn13");
        identity.setMsoId("m93945");
        identity.setMsoPass("93937EA01B94A10A49279D4572B48369");
        identity.setAdminTenant("admin");
        identity.setMemberRole("admin");
        identity.setTenantMetadata(false);
        identity.setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");
        identity.setIdentityAuthenticationType(AuthenticationType.USERNAME_PASSWORD);
        identity.setIdentityServerType(ServerType.KEYSTONE);
        return identity;
    }

    protected CloudSite getCloudSite(CloudIdentity identity) {
        CloudSite cloudSite = new CloudSite();
        cloudSite.setId("MTN13");
        cloudSite.setCloudVersion("3.0");
        cloudSite.setClli("MDT13");
        cloudSite.setRegionId("mtn13");
        cloudSite.setIdentityService(identity);
        return cloudSite;
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
}
