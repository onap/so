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

package org.onap.so.asdc.client.test.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Spy;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.client.test.emulators.DistributionClientEmulator;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
import org.onap.so.db.catalog.beans.AllottedResource;
import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.data.repository.AllottedResourceRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.ToscaCsarRepository;
import org.onap.so.db.catalog.data.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ASDCRestInterfaceCCVPNTest extends BaseTest {

    @Autowired
    private AllottedResourceRepository allottedRepo;

    @Autowired
    private ServiceRepository serviceRepo;

    @Autowired
    private NetworkResourceRepository networkRepo;

    @Autowired
    private WorkflowRepository workflowRepo;

    @Autowired
    private ToscaCsarRepository toscaCsarRepo;

    @Autowired
    private ASDCRestInterface asdcRestInterface;

    private TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

    private HttpHeaders headers = new HttpHeaders();

    @Spy
    DistributionClientEmulator spyClient = new DistributionClientEmulator();

    @LocalServerPort
    private int port;


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Before
    public void setUp() {
        // ASDC Controller writes to this path
        System.setProperty("mso.config.path", folder.getRoot().toString());
    }

    @Test
    @Transactional
    public void test_CCVPN_Distribution() throws Exception {
        wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));

        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));

        String resourceLocation = "src/test/resources/resource-examples/ccvpn/";
        ObjectMapper mapper = new ObjectMapper();
        NotificationDataImpl request = mapper.readValue(new File(resourceLocation + "demo-ccvpn-notification.json"),
                NotificationDataImpl.class);
        headers.add("resource-location", resourceLocation);
        HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        Optional<Service> service = serviceRepo.findById("317887d3-a4e4-45cb-8971-2a78426fefac");
        assertTrue(service.isPresent());
        assertEquals("CCVPNService", service.get().getModelName());
    }

    protected String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}
