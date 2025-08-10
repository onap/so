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

package org.onap.so.apihandlerinfra;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.onap.logging.filter.base.Constants.HttpHeaders.ONAP_PARTNER_NAME;
import static org.onap.logging.filter.base.Constants.HttpHeaders.ONAP_REQUEST_ID;
import static org.onap.logging.filter.base.Constants.HttpHeaders.TRANSACTION_ID;
import static org.onap.so.logger.HttpHeadersConstants.REQUESTOR_ID;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class InstanceManagementTest extends BaseTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private ObjectMapper errorMapper = new ObjectMapper();

    @Autowired
    InstanceManagement instanceManagement;

    @Value("${wiremock.server.port}")
    private String wiremockPort;

    private final String instanceManagementUri = "/onap/so/infra/instanceManagement/";
    private final String orchestration_path = "/onap/so/infra";

    private String uri;
    private URL selfLink;
    private URL initialUrl;
    private int initialPort;
    private HttpHeaders headers;

    @Before
    public void beforeClass() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        errorMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        errorMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        // set headers
        headers = new HttpHeaders();
        headers.set(ONAPLogConstants.Headers.PARTNER_NAME, "test_name");
        headers.set(TRANSACTION_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        headers.set(ONAP_REQUEST_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        headers.set(ONAPLogConstants.MDCs.REQUEST_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        headers.set(ONAP_PARTNER_NAME, "VID");
        headers.set(REQUESTOR_ID, "xxxxxx");
        try { // generate one-time port number to avoid RANDOM port number later.
            initialUrl = new URL(createURLWithPort(Constants.ORCHESTRATION_REQUESTS_PATH, orchestration_path));
            initialPort = initialUrl.getPort();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests.*")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_OK)));
    }

    public String inputStream(String JsonInput) throws IOException {
        JsonInput = "src/test/resources/ServiceInstanceTest" + JsonInput;
        return new String(Files.readAllBytes(Paths.get(JsonInput)));
    }

    private URL createExpectedSelfLink(String version, String requestId) {
        System.out.println("createdUrl: " + initialUrl.toString());
        try {
            selfLink = new URL(initialUrl.toString().concat("/").concat(version).concat("/").concat(requestId));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return selfLink;
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

    public ResponseEntity<String> sendRequest(String requestJson, String uriPath, HttpMethod reqMethod,
            HttpHeaders headers) {

        if (!headers.containsKey(HttpHeaders.ACCEPT)) {
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(uriPath, initialPort));

        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        return restTemplate.exchange(builder.toUriString(), reqMethod, request, String.class);
    }

    public ResponseEntity<String> sendRequest(String requestJson, String uriPath, HttpMethod reqMethod) {
        return sendRequest(requestJson, uriPath, reqMethod, new HttpHeaders());
    }

    @Test
    public void executeCustomWorkflow() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/testingWorkflow"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/workflow/search/findByArtifactUUID[?]artifactUUID=71526781-e55c-4cb7-adb3-97e09d9c76be"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("workflow_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v1", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = instanceManagementUri + "v1"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/workflows/71526781-e55c-4cb7-adb3-97e09d9c76be";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ExecuteCustomWorkflow.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void executePNFCustomWorkflow() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/testingPNFWorkflow"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/workflow/search/findByArtifactUUID[?]artifactUUID=81526781-e55c-4cb7-adb3-97e09d9c76bf"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("workflow_pnf_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v1", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = instanceManagementUri + "v1"
                + "/serviceInstances/5df8b6de-2083-11e7-93ae-92361f002676/pnfs/testPnfName/workflows/81526781-e55c-4cb7-adb3-97e09d9c76bf";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ExecutePNFCustomWorkflow.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());

        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void executeServiceLevelCustomWorkflow() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/testingServiceLevelWorkflow"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/workflow/search/findByArtifactUUID[?]artifactUUID=81526781-e55c-4cb7-adb3-97e09d9c76bf"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("workflow_ServiceLevel_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v1", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = instanceManagementUri + "v1"
                + "/serviceInstances/5df8b6de-2083-11e7-93ae-92361f002676/workflows/81526781-e55c-4cb7-adb3-97e09d9c76bf";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ExecuteServiceLevelCustomWorkflow.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());

        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void workflowAndOperationNameTest() {
        wireMockServer.stubFor(get(urlMatching(
                ".*/workflow/search/findByArtifactUUID[?]artifactUUID=71526781-e55c-4cb7-adb3-97e09d9c76be"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("workflow_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));
        InfraActiveRequests activeReq = new InfraActiveRequests();
        activeReq =
                instanceManagement.setWorkflowNameAndOperationName(activeReq, "71526781-e55c-4cb7-adb3-97e09d9c76be");
        assertEquals(activeReq.getWorkflowName(), "testingWorkflow");
        assertEquals(activeReq.getOperationName(), "testOperation");
    }
}
