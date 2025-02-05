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

package org.onap.so.client.grm;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import jakarta.ws.rs.core.MediaType;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.TestAppender;
import org.onap.so.client.grm.beans.ServiceEndPoint;
import org.onap.so.client.grm.beans.ServiceEndPointList;
import org.onap.so.client.grm.beans.ServiceEndPointLookupRequest;
import org.onap.so.client.grm.beans.ServiceEndPointRequest;
import org.onap.so.client.grm.exceptions.GRMClientCallFailed;
import org.slf4j.MDC;


public class GRMClientTest extends BaseTest {


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String uuidRegex =
            "(?i)^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-5][0-9a-f]{3}-?[089ab][0-9a-f]{3}-?[0-9a-f]{12}$";

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("mso.config.path", "src/test/resources");
    }

    @Test
    public void testFind() throws Exception {
        TestAppender.events.clear();
        String endpoints = getFileContentsAsString("__files/grm/endpoints.json");
        wireMockServer
                .stubFor(post(urlPathEqualTo("/GRMLWPService/v1/serviceEndPoint/findRunning")).willReturn(aResponse()
                        .withStatus(200).withHeader("Content-Type", MediaType.APPLICATION_JSON).withBody(endpoints)));

        MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, "/test");
        GRMClient client = new GRMClient();
        ServiceEndPointList sel = client.findRunningServices("TEST.ECOMP_PSL.*", 1, "TEST");
        List<ServiceEndPoint> list = sel.getServiceEndPointList();
        assertEquals(3, list.size());

        wireMockServer.verify(postRequestedFor(urlEqualTo("/GRMLWPService/v1/serviceEndPoint/findRunning"))
                .withHeader(ONAPLogConstants.Headers.INVOCATION_ID.toString(), matching(uuidRegex))
                .withHeader(ONAPLogConstants.Headers.REQUEST_ID.toString(), matching(uuidRegex))
                .withHeader(ONAPLogConstants.Headers.PARTNER_NAME.toString(), equalTo("SO.APIH")));
    }

    @Test
    public void testFindFail() throws Exception {
        wireMockServer.stubFor(post(urlPathEqualTo("/GRMLWPService/v1/serviceEndPoint/findRunning")).willReturn(
                aResponse().withStatus(400).withHeader("Content-Type", MediaType.APPLICATION_JSON).withBody("")));

        GRMClient client = new GRMClient();
        thrown.expect(GRMClientCallFailed.class);
        client.findRunningServices("TEST.ECOMP_PSL.*", 1, "TEST");
    }

    @Test
    public void testAddFail() throws Exception {
        wireMockServer.stubFor(post(urlPathEqualTo("/GRMLWPService/v1/serviceEndPoint/add")).willReturn(
                aResponse().withStatus(404).withHeader("Content-Type", MediaType.APPLICATION_JSON).withBody("test")));
        ServiceEndPointRequest request = new ServiceEndPointRequest();
        GRMClient client = new GRMClient();
        thrown.expect(GRMClientCallFailed.class);
        client.addServiceEndPoint(request);
    }

    @Test
    public void testBuildServiceEndPointLookupRequest() {
        GRMClient client = new GRMClient();
        ServiceEndPointLookupRequest request =
                client.buildServiceEndPointlookupRequest("TEST.ECOMP_PSL.Inventory", 1, "DEV");
        assertEquals("TEST.ECOMP_PSL.Inventory", request.getServiceEndPoint().getName());
        assertEquals(Integer.valueOf(1), Integer.valueOf(request.getServiceEndPoint().getVersion().getMajor()));
        assertEquals("DEV", request.getEnv());

    }

    protected String getFileContentsAsString(String fileName) {
        String content = "";
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            content = new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception encountered reading " + fileName + ". Error: " + e.getMessage());
        }
        return content;
    }
}
