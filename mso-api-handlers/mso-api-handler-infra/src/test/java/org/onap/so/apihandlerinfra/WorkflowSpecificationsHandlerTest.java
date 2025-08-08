/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2020 Nordix
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

package org.onap.so.apihandlerinfra;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Test;
import org.onap.so.apihandlerinfra.workflowspecificationbeans.WorkflowInputParameter;
import org.onap.so.apihandlerinfra.workflowspecificationbeans.WorkflowSpecifications;
import org.onap.so.db.catalog.beans.*;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.util.UriComponentsBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WorkflowSpecificationsHandlerTest extends BaseTest {
    @Autowired
    WorkflowSpecificationsHandler workflowSpecificationsHandler;

    @Autowired
    ObjectMapper mapper;

    @Value("${wiremock.server.port}")
    private String wiremockPort;

    private final String basePath = "/onap/so/infra/workflowSpecifications";

    @Test
    public void queryWorkflowSpecificationsByVnfModelUUID_Test_Success()
            throws JSONException, JsonMappingException, IOException {

        final String urlPath = basePath + "/v1/workflows";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        wireMockServer.stubFor(get(urlMatching(
                "/workflow/search/findWorkflowByVnfModelUUID[?]vnfResourceModelUUID=b5fa707a-f55a-11e7-a796-005056856d52"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("WorkflowSpecificationsQuery_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/workflow/1/workflowActivitySpecSequence"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("WorkflowActivitySpecSequence_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/workflowActivitySpecSequence/1/activitySpec"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("ActivitySpecSequence1_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/workflowActivitySpecSequence/2/activitySpec"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("ActivitySpecSequence2_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/workflowActivitySpecSequence/3/activitySpec"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("ActivitySpecSequence3_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/activitySpec/1/activitySpecUserParameters"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("ActivitySpecUserParameters1_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/activitySpec/2/activitySpecUserParameters"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("ActivitySpecUserParameters2_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/activitySpec/3/activitySpecUserParameters"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("ActivitySpecUserParameters3_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/activitySpecUserParameters/1/userParameters"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("UserParameters1_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/activitySpecUserParameters/2/userParameters"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("UserParameters2_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/activitySpecUserParameters/3/userParameters"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("UserParameters3_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/activitySpecUserParameters/4/userParameters"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("UserParameters4_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/activitySpecUserParameters/5/userParameters"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("UserParameters5_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/activitySpecUserParameters/6/userParameters"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("UserParameters6_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(urlPath))
                .queryParam("vnfModelVersionId", "b5fa707a-f55a-11e7-a796-005056856d52");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        WorkflowSpecifications expectedResponse = mapper.readValue(
                new String(Files
                        .readAllBytes(Paths.get("src/test/resources/__files/catalogdb/WorkflowSpecifications.json"))),
                WorkflowSpecifications.class);
        WorkflowSpecifications realResponse = mapper.readValue(response.getBody(), WorkflowSpecifications.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(expectedResponse, sameBeanAs(realResponse));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("1.0.0", response.getHeaders().get("X-LatestVersion").get(0));
    }

    @Test
    public void mapWorkflowsToWorkflowSpecifications_Test_Success() throws Exception {
        List<Workflow> workflows = new ArrayList<>();
        Workflow workflow = new Workflow();
        workflow.setArtifactUUID("ab6478e4-ea33-3346-ac12-ab121484a333");
        workflow.setArtifactName("inPlaceSoftwareUpdate-1_0.bpmn");
        workflow.setVersion(1.0);
        workflow.setDescription("xyz xyz");
        workflow.setName("inPlaceSoftwareUpdate");
        workflow.setOperationName("inPlaceSoftwareUpdate");
        workflow.setSource("sdc");
        workflow.setResourceTarget("vnf");

        UserParameters userParameter1 = new UserParameters();
        userParameter1.setLabel("Operations Timeout");
        userParameter1.setType("text");
        userParameter1.setIsRequried(true);
        userParameter1.setMaxLength(50);
        userParameter1.setAllowableChars("someRegEx");
        userParameter1.setName("operations_timeout");
        userParameter1.setPayloadLocation("userParams");

        UserParameters userParameter2 = new UserParameters();
        userParameter2.setLabel("Existing Software Version");
        userParameter2.setType("text");
        userParameter2.setIsRequried(true);
        userParameter2.setMaxLength(50);
        userParameter2.setAllowableChars("someRegEx");
        userParameter2.setName("existing_software_version");
        userParameter2.setPayloadLocation("userParams");

        UserParameters userParameter3 = new UserParameters();
        userParameter3.setLabel("Cloud Owner");
        userParameter3.setType("text");
        userParameter3.setIsRequried(true);
        userParameter3.setMaxLength(7);
        userParameter3.setAllowableChars("someRegEx");
        userParameter3.setName("cloudOwner");
        userParameter3.setPayloadLocation("cloudConfiguration");

        UserParameters userParameter4 = new UserParameters();
        userParameter4.setLabel("Tenant/Project ID");
        userParameter4.setType("text");
        userParameter4.setIsRequried(true);
        userParameter4.setMaxLength(36);
        userParameter4.setAllowableChars("someRegEx");
        userParameter4.setName("tenantId");
        userParameter4.setPayloadLocation("cloudConfiguration");

        UserParameters userParameter5 = new UserParameters();
        userParameter5.setLabel("New Software Version");
        userParameter5.setType("text");
        userParameter5.setIsRequried(true);
        userParameter5.setMaxLength(50);
        userParameter5.setAllowableChars("someRegEx");
        userParameter5.setName("new_software_version");
        userParameter5.setPayloadLocation("userParams");

        UserParameters userParameter6 = new UserParameters();
        userParameter6.setLabel("Cloud Region ID");
        userParameter6.setType("text");
        userParameter6.setIsRequried(true);
        userParameter6.setMaxLength(7);
        userParameter6.setAllowableChars("someRegEx");
        userParameter6.setName("lcpCloudRegionId");
        userParameter6.setPayloadLocation("cloudConfiguration");


        List<ActivitySpecUserParameters> activitySpecUserParameters = new ArrayList<ActivitySpecUserParameters>();

        ActivitySpecUserParameters activitySpecUserParameter1 = new ActivitySpecUserParameters();
        activitySpecUserParameter1.setUserParameters(userParameter1);
        activitySpecUserParameters.add(activitySpecUserParameter1);

        ActivitySpecUserParameters activitySpecUserParameter2 = new ActivitySpecUserParameters();
        activitySpecUserParameter2.setUserParameters(userParameter2);
        activitySpecUserParameters.add(activitySpecUserParameter2);

        ActivitySpecUserParameters activitySpecUserParameter3 = new ActivitySpecUserParameters();
        activitySpecUserParameter3.setUserParameters(userParameter3);
        activitySpecUserParameters.add(activitySpecUserParameter3);


        ActivitySpecUserParameters activitySpecUserParameter4 = new ActivitySpecUserParameters();
        activitySpecUserParameter4.setUserParameters(userParameter4);
        activitySpecUserParameters.add(activitySpecUserParameter4);

        ActivitySpecUserParameters activitySpecUserParameter5 = new ActivitySpecUserParameters();
        activitySpecUserParameter5.setUserParameters(userParameter5);
        activitySpecUserParameters.add(activitySpecUserParameter5);

        ActivitySpecUserParameters activitySpecUserParameter6 = new ActivitySpecUserParameters();
        activitySpecUserParameter6.setUserParameters(userParameter6);
        activitySpecUserParameters.add(activitySpecUserParameter6);

        List<WorkflowActivitySpecSequence> workflowActivitySpecSequences = new ArrayList<>();

        ActivitySpec activitySpec1 = new ActivitySpec();
        activitySpec1.setName("VNFQuiesceTrafficActivity");
        activitySpec1.setDescription("Activity to QuiesceTraffic on VNF");
        activitySpec1.setActivitySpecUserParameters(activitySpecUserParameters);
        WorkflowActivitySpecSequence workflowActivitySpecSequence1 = new WorkflowActivitySpecSequence();
        workflowActivitySpecSequence1.setActivitySpec(activitySpec1);
        workflowActivitySpecSequences.add(workflowActivitySpecSequence1);

        ActivitySpec activitySpec2 = new ActivitySpec();
        activitySpec2.setName("VNFHealthCheckActivity");
        activitySpec2.setDescription("Activity to HealthCheck VNF");
        activitySpec2.setActivitySpecUserParameters(activitySpecUserParameters);
        WorkflowActivitySpecSequence workflowActivitySpecSequence2 = new WorkflowActivitySpecSequence();
        workflowActivitySpecSequence2.setActivitySpec(activitySpec2);
        workflowActivitySpecSequences.add(workflowActivitySpecSequence2);

        ActivitySpec activitySpec3 = new ActivitySpec();
        activitySpec3.setName("FlowCompleteActivity");
        activitySpec3.setDescription("Activity to Complete the BPMN Flow");
        activitySpec3.setActivitySpecUserParameters(activitySpecUserParameters);
        WorkflowActivitySpecSequence workflowActivitySpecSequence3 = new WorkflowActivitySpecSequence();
        workflowActivitySpecSequence3.setActivitySpec(activitySpec3);
        workflowActivitySpecSequences.add(workflowActivitySpecSequence3);

        workflow.setWorkflowActivitySpecSequence(workflowActivitySpecSequences);
        workflows.add(workflow);

        Workflow workflowNative = new Workflow();
        workflowNative.setArtifactUUID("da6478e4-ea33-3346-ac12-ab121284a333");
        workflowNative.setArtifactName("VnfInPlaceUpdate.bpmn");
        workflowNative.setVersion(1.0);
        workflowNative.setDescription("native workflow");
        workflowNative.setName("VnfInPlaceUpdate");
        workflowNative.setOperationName("inPlaceSoftwareUpdate");
        workflowNative.setSource("native");
        workflowNative.setResourceTarget("vnf");
        workflows.add(workflowNative);

        WorkflowSpecifications workflowSpecifications =
                workflowSpecificationsHandler.mapWorkflowsToWorkflowSpecifications(workflows);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String workflowSpecificationsJson = mapper.writeValueAsString(workflowSpecifications);
        WorkflowSpecifications expectedResult = mapper.readValue(
                new String(Files
                        .readAllBytes(Paths.get("src/test/resources/__files/catalogdb/WorkflowSpecifications.json"))),
                WorkflowSpecifications.class);
        String expectedResultJson = mapper.writeValueAsString(expectedResult);

        JSONAssert.assertEquals(expectedResultJson, workflowSpecificationsJson, false);
        assertThat(expectedResult, sameBeanAs(workflowSpecifications).ignoring(WorkflowInputParameter.class));
    }

    @Test
    public void queryWorkflowSpecificationsByPnfModelUUID_Test_Success() throws JSONException, IOException {

        final String urlPath = basePath + "/v1/workflows";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        wireMockServer.stubFor(get(urlMatching(
                "/workflow/search/findWorkflowByPnfModelUUID[?]pnfResourceModelUUID=f2d1f2b2-88bb-49da-b716-36ae420ccbff"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb(
                                        "WorkflowSpecificationsForPnfQuery_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/workflow/4/workflowActivitySpecSequence"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("Empty_workflowActivitySpecSequence_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(urlPath))
                .queryParam("pnfModelVersionId", "f2d1f2b2-88bb-49da-b716-36ae420ccbff");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        WorkflowSpecifications expectedResponse = mapper.readValue(
                new String(Files.readAllBytes(
                        Paths.get("src/test/resources/__files/catalogdb/WorkflowSpecificationsForPnf.json"))),
                WorkflowSpecifications.class);
        WorkflowSpecifications realResponse = mapper.readValue(response.getBody(), WorkflowSpecifications.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(expectedResponse, sameBeanAs(realResponse));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("1.0.0", response.getHeaders().get("X-LatestVersion").get(0));
    }

    @Test
    public void queryWorkflowSpecificationsByResourceTarget_Test_Success() throws JSONException, IOException {

        String URL_PATH = basePath + "/v1/workflows";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        String WORKFLOW_QUERY = "/workflow/search/findByResourceTarget[?]resourceTarget=service";
        String WORKFLOW_SPEC_QUERY = "/workflow/5/workflowActivitySpecSequence";
        String JSON_FILE_PATH = "src/test/resources/__files/catalogdb/WorkflowSpecificationsForService.json";
        String MOCK_RESP_FILE = "WorkflowSpecificationsForServiceWorkflows_Response.json";
        String MOCK_RESP_SPEC_FILE = "Empty_workflowActivitySpecSequence_Response.json";

        wireMockServer.stubFor(get(urlMatching(WORKFLOW_QUERY))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb(MOCK_RESP_FILE))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(WORKFLOW_SPEC_QUERY))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb(MOCK_RESP_SPEC_FILE))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(URL_PATH)).queryParam("resourceTarget", "service");

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        WorkflowSpecifications expectedResponse = mapper
                .readValue(new String(Files.readAllBytes(Paths.get(JSON_FILE_PATH))), WorkflowSpecifications.class);
        WorkflowSpecifications realResponse = mapper.readValue(response.getBody(), WorkflowSpecifications.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(expectedResponse, sameBeanAs(realResponse));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("1.0.0", response.getHeaders().get("X-LatestVersion").get(0));
    }

    @Test
    public void testWorkflowSpecificationsForPnf_Success() throws JSONException, IOException {

        final String urlPath = basePath + "/v1/pnfWorkflows";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity(null, headers);

        wireMockServer.stubFor(get(urlMatching("/workflow/search/findByResourceTarget[?]resourceTarget=pnf"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(
                                getWiremockResponseForCatalogdb("WorkflowSpecificationsForPnfWorkflows_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/infraActiveRequests.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/workflow/1/workflowActivitySpecSequence"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("Empty_workflowActivitySpecSequence_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(urlPath));

        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        WorkflowSpecifications expectedResponse = mapper.readValue(
                new String(Files.readAllBytes(
                        Paths.get("src/test/resources/__files/catalogdb/WorkflowSpecificationsForPnf.json"))),
                WorkflowSpecifications.class);
        WorkflowSpecifications realResponse = mapper.readValue(response.getBody(), WorkflowSpecifications.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(expectedResponse, sameBeanAs(realResponse));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("1.0.0", response.getHeaders().get("X-LatestVersion").get(0));
    }

    private String getWiremockResponseForCatalogdb(String file) {
        try {
            File resource = ResourceUtils.getFile("classpath:__files/catalogdb/" + file);
            return new String(Files.readAllBytes(resource.toPath())).replaceAll("localhost:8090",
                    "localhost:" + wiremockPort);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
