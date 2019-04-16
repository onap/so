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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
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
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.data.repository.AllottedResourceRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ASDCRestInterfaceTest extends BaseTest {

    @Autowired
    private AllottedResourceRepository allottedRepo;

    @Autowired
    private ServiceRepository serviceRepo;

    @Autowired
    private NetworkResourceRepository networkRepo;

    @Autowired
    private WorkflowRepository workflowRepo;

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
    public void testAllottedResourceService() throws Exception {

        wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));

        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));

        ObjectMapper mapper = new ObjectMapper();
        NotificationDataImpl request =
                mapper.readValue(new File("src/test/resources/resource-examples/allottedresource/notif-portm.json"),
                        NotificationDataImpl.class);
        headers.add("resource-location", "src/test/resources/resource-examples/allottedresource/");
        HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        AllottedResource expectedService = new AllottedResource();
        expectedService.setDescription("rege1802pnf");
        expectedService.setModelInvariantUUID("b8f83c3f-077c-4e2c-b489-c66382060436");
        expectedService.setModelName("rege1802pnf");
        expectedService.setModelUUID("5b18c75e-2d08-4bf2-ad58-4ea704ec648d");
        expectedService.setModelVersion("1.0");
        expectedService.setSubcategory("Contrail Route");
        expectedService.setToscaNodeType("org.openecomp.resource.pnf.Rege1802pnf");
        Set<AllottedResourceCustomization> arCustomizationSet = new HashSet<AllottedResourceCustomization>();
        AllottedResourceCustomization arCustomization = new AllottedResourceCustomization();
        arCustomization.setModelCustomizationUUID("f62bb612-c5d4-4406-865c-0abec30631ba");
        arCustomization.setModelInstanceName("rege1802pnf 0");
        arCustomizationSet.add(arCustomization);

        arCustomization.setAllottedResource(expectedService);


        expectedService.setAllotedResourceCustomization(arCustomizationSet);

        AllottedResource actualResponse = allottedRepo.findResourceByModelUUID("5b18c75e-2d08-4bf2-ad58-4ea704ec648d");


        if (actualResponse == null)
            throw new Exception("No Allotted Resource Written to database");


        assertThat(actualResponse, sameBeanAs(expectedService).ignoring("0x1.created")
                .ignoring("0x1.allotedResourceCustomization.created"));
    }

    @Test
    @Transactional
    public void test_VFW_Distrobution() throws Exception {

        wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));

        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));

        ObjectMapper mapper = new ObjectMapper();
        NotificationDataImpl request = mapper.readValue(
                new File("src/test/resources/resource-examples/vFW/notification.json"), NotificationDataImpl.class);
        headers.add("resource-location", "src/test/resources/resource-examples/vFW/");
        HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        Service expectedService = new Service();
        expectedService.setDescription("catalog service description");
        expectedService.setModelInvariantUUID("3164f9ff-d7e7-4813-ab32-6be7e1cacb18");
        expectedService.setModelName("vFW 2019-04-10 21:53:05");
        expectedService.setModelUUID("e16e4ed9-3429-423a-bc3c-1389ae91491c");
        expectedService.setModelVersion("1.0");



        Service actualService = serviceRepo.findOneByModelUUID("e16e4ed9-3429-423a-bc3c-1389ae91491c");


        if (actualService == null)
            throw new Exception("No Allotted Resource Written to database");

        assertEquals(expectedService.getModelName(), actualService.getModelName());
    }

    @Test
    @Transactional
    public void testWorkflowDistribution() throws Exception {

        wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));

        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));

        wireMockServer.stubFor(
                post(urlPathEqualTo("/sobpmnengine/deployment/create")).willReturn(aResponse().withStatus(200)));

        ObjectMapper mapper = new ObjectMapper();
        NotificationDataImpl request = mapper.readValue(
                new File("src/test/resources/resource-examples/WorkflowBpmn/workflow-distribution.json"),
                NotificationDataImpl.class);
        headers.add("resource-location", "src/test/resources/resource-examples/WorkflowBpmn/");
        HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        Workflow actualResponse = workflowRepo.findByArtifactUUID("a90f8eaa-7c20-422f-8c81-aacbca6fb9e7");

        if (actualResponse == null)
            throw new Exception("No Workflow Written to database");

        String expectedBody = new String(Files.readAllBytes(Paths.get(
                // "src/test/resources/resource-examples/WorkflowBpmn/sdc/v1/catalog/services/Testparentservice/1.0/resourceInstances/testvf0/artifacts/TestWF2-1_0.bpmn")));
                "src/test/resources/resource-examples/WorkflowBpmn/TestWF2-1_0.bpmn")));
        assertEquals(actualResponse.getArtifactChecksum(), "ZjUzNjg1NDMyMTc4MWJmZjFlNDcyOGQ0Zjc1YWQwYzQ\u003d");
        assertEquals(actualResponse.getArtifactName(), "TestWF2-1_0.bpmn");
        assertEquals(actualResponse.getDescription(), "Workflow Artifact Description");
        assertEquals(actualResponse.getBody(), expectedBody);

        Workflow shouldNotBeFound = workflowRepo.findByArtifactUUID("f27066a1-c3a7-4672-b02e-1251b74b7b71");
        assertNull(shouldNotBeFound);
    }

    @Test
    public void invokeASDCStatusDataNullTest() {
        String request = "";
        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));
        Response response = asdcRestInterface.invokeASDCStatusData(request);
        assertNull(response);

    }

    protected String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}
