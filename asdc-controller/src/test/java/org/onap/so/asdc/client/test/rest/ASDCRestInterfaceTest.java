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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.data.repository.AllottedResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.AllottedResourceRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.ToscaCsarRepository;
import org.onap.so.db.catalog.data.repository.VnfCustomizationRepository;
import org.onap.so.db.catalog.data.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.util.ReflectionTestUtils;

public class ASDCRestInterfaceTest extends BaseTest {

    @Autowired
    private AllottedResourceRepository allottedRepo;

    @Autowired
    private AllottedResourceCustomizationRepository allottedCustomRepo;

    @Autowired
    private ServiceRepository serviceRepo;

    @Autowired
    private NetworkResourceRepository networkRepo;

    @Autowired
    private VnfCustomizationRepository vnfCustRepo;

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
        ReflectionTestUtils.setField(toscaInstaller, "toscaCsarRepo", toscaCsarRepo);
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

        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"),
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
    public void test_VFW_Distribution() throws Exception {

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

        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"),
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

        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        Workflow actualResponse = workflowRepo.findByArtifactUUID("a90f8eaa-7c20-422f-8c81-aacbca6fb9e7");

        if (actualResponse == null)
            throw new Exception("No Workflow Written to database");

        String expectedBody = new String(
                Files.readAllBytes(Paths.get("src/test/resources/resource-examples/WorkflowBpmn/TestWF2-1_0.bpmn")));
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


    @Test
    public void test_Vcpe_Infra_Distribution() throws Exception {
        wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));

        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));

        String resourceLocation = "src/test/resources/resource-examples/vcpe-infra/";

        ObjectMapper mapper = new ObjectMapper();
        NotificationDataImpl request = mapper.readValue(new File(resourceLocation + "demovcpeinfra-notification.json"),
                NotificationDataImpl.class);
        headers.add("resource-location", resourceLocation);
        HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        Optional<ToscaCsar> toscaCsar = toscaCsarRepo.findById("144606d8-a505-4ba0-90a9-6d1c6219fc6b");
        assertTrue(toscaCsar.isPresent());
        assertEquals("service-Demovcpeinfra-csar.csar", toscaCsar.get().getName());

        Optional<Service> service = serviceRepo.findById("8a77cbbb-9850-40bb-a42f-7aec8e3e6ab7");
        assertTrue(service.isPresent());
        assertEquals("demoVCPEInfra", service.get().getModelName());

        Optional<NetworkResource> networkResource = networkRepo.findById("89789b26-a46b-4cee-aed0-d46e21f93a5e");
        assertTrue(networkResource.isPresent());
        assertEquals("Generic NeutronNet", networkResource.get().getModelName());

        List<VnfResourceCustomization> vnfCustomizationResources =
                vnfCustRepo.findByModelCustomizationUUID("01564fe7-0541-4d92-badc-464cc35f83ba");

        for (VnfResourceCustomization vnfResourceCustomization : vnfCustomizationResources) {

            assertTrue(vnfResourceCustomization.getVfModuleCustomizations().stream()
                    .anyMatch(vfModuleCust -> "354b1e83-47db-4af1-8af4-9c14b03b482d"
                            .equals(vfModuleCust.getModelCustomizationUUID())));

        }

    }

    @Test
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
        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        Optional<Service> service = serviceRepo.findById("317887d3-a4e4-45cb-8971-2a78426fefac");
        assertTrue(service.isPresent());
        assertEquals("CCVPNService", service.get().getModelName());
    }

    @Test
    public void test_E2ESlicing_Distribution() throws Exception {
        wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));

        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));
        String resourceLocation = "src/test/resources/resource-examples/e2eSlicing/";
        ObjectMapper mapper = new ObjectMapper();

        NotificationDataImpl request;
        HttpEntity<NotificationDataImpl> entity;
        ResponseEntity<String> response;
        headers.add("resource-location", resourceLocation);

        request = mapper.readValue(new File(resourceLocation + "nsst-notification.json"), NotificationDataImpl.class);
        entity = new HttpEntity<NotificationDataImpl>(request, headers);
        response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"), HttpMethod.POST, entity,
                String.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        request = mapper.readValue(new File(resourceLocation + "nst-notification.json"), NotificationDataImpl.class);
        entity = new HttpEntity<NotificationDataImpl>(request, headers);
        response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"), HttpMethod.POST, entity,
                String.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        Optional<Service> service = serviceRepo.findById("7981375e-5e0a-4bf5-93fa-f3e3c02f2b11");
        assertTrue(service.isPresent());
        assertEquals("EmbbNst", service.get().getModelName());

        service = serviceRepo.findById("637e9b93-208b-4b06-80f2-a2021c228174");
        assertTrue(service.isPresent());
        assertEquals("EmbbCn", service.get().getModelName());
    }

    @Test
    public void test_PublicNS_Distribution() throws Exception {
        wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));

        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));

        String resourceLocation = "src/test/resources/resource-examples/public-ns/";
        ObjectMapper mapper = new ObjectMapper();
        NotificationDataImpl request = mapper.readValue(new File(resourceLocation + "demo-public-ns-notification.json"),
                NotificationDataImpl.class);
        headers.add("resource-location", resourceLocation);
        HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        Optional<Service> service = serviceRepo.findById("da28696e-d4c9-4df4-9f91-465c6c09a81e");
        // assertTrue(service.isPresent());
        // assertEquals("PublicNS", service.get().getModelName());
    }

    @Test
    public void test_Vcperescust_Distribution() throws Exception {
        wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));

        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));

        String resourceLocation = "src/test/resources/resource-examples/vcpe-rescust/";
        ObjectMapper mapper = new ObjectMapper();
        NotificationDataImpl request = mapper.readValue(
                new File(resourceLocation + "demo-vcpe-rescust-notification.json"), NotificationDataImpl.class);
        headers.add("resource-location", resourceLocation);
        HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        Optional<Service> service = serviceRepo.findById("d3aac917-543d-4421-b6d7-ba2b65884eb7");
        assertTrue(service.isPresent());
        assertEquals("vCPEResCust 2019-10-01 _2364", service.get().getModelName());
    }

    @Test
    public void testServiceUbuntu16Test() throws Exception {
        wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));
        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));
        String resourceLocation = "src/test/resources/resource-examples/service-ubuntu16test/";
        ObjectMapper mapper = new ObjectMapper();
        NotificationDataImpl request = mapper.readValue(
                new File(resourceLocation + "service-ubuntu16test-notification.json"), NotificationDataImpl.class);
        headers.add("resource-location", resourceLocation);
        HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        Optional<Service> service = serviceRepo.findById("ed0391da-b963-4c45-bf3a-b49cc7a94fab");
        assertTrue(service.isPresent());
        assertEquals("ubuntu16test", service.get().getModelName());
    }


    @Test
    public void testServiceBasicCnf() throws Exception {
        wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));
        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));
        String resourceLocation = "src/test/resources/resource-examples/service-BasicCnf/";
        ObjectMapper mapper = new ObjectMapper();
        NotificationDataImpl request = mapper.readValue(
                new File(resourceLocation + "service-BasicCnf-notification.json"), NotificationDataImpl.class);
        headers.add("resource-location", resourceLocation);
        HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        Optional<Service> service = serviceRepo.findById("31e0cd50-0a84-42b4-a7a8-dd5d82e6907d");
        assertTrue(service.isPresent());
        assertEquals("basic_cnf", service.get().getModelName());
    }

    @Test
    public void testServiceBasicNetwork() throws Exception {
        wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")));
        wireMockServer.stubFor(post(urlPathMatching("/v1.0/activity-spec"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(org.springframework.http.HttpStatus.ACCEPTED.value())));
        String resourceLocation = "src/test/resources/resource-examples/service-BasicNetwork/";
        ObjectMapper mapper = new ObjectMapper();
        NotificationDataImpl request = mapper.readValue(
                new File(resourceLocation + "service-BasicNetwork-notification.json"), NotificationDataImpl.class);
        headers.add("resource-location", resourceLocation);
        HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/test/treatNotification/v1"),
                HttpMethod.POST, entity, String.class);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        Optional<Service> service = serviceRepo.findById("9ff42123-ff24-41dc-9f41-a956c9328699");
        assertTrue(service.isPresent());
        assertEquals("basic_network", service.get().getModelName());
    }


    protected String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}
