/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited.
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
import static org.junit.Assert.assertEquals;
import static org.onap.so.logging.filter.base.Constants.HttpHeaders.ONAP_PARTNER_NAME;
import static org.onap.so.logging.filter.base.Constants.HttpHeaders.ONAP_REQUEST_ID;
import static org.onap.so.logging.filter.base.Constants.HttpHeaders.TRANSACTION_ID;
import static org.onap.so.logger.HttpHeadersConstants.REQUESTOR_ID;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans.QuerySubnetCapability;
import org.springframework.beans.factory.annotation.Autowired;

public class Onap3gppServiceInstancesTest extends BaseTest {

    private static final String ONAP3GPPSERVICES_URI = "/onap/so/infra/3gppservices/";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private Onap3gppServiceInstances objUnderTest;

    @Before
    public void init() throws JsonProcessingException {

        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setServiceModelUUID(defaultService.getModelUUID());
        serviceRecipe.setRecipeTimeout(180);
        serviceRecipe.setOrchestrationUri("/mso/async/services/commonNssmfTest");

        wireMockServer.stubFor(get(urlPathEqualTo("/service/search/findFirstByModelNameOrderByModelVersionDesc"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(MAPPER.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlPathEqualTo("/serviceRecipe/search/findFirstByServiceModelUUIDAndAction"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(MAPPER.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests/")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_OK)));
        Mockito.doReturn(null).when(requestsDbClient).getInfraActiveRequestbyRequestId(Mockito.any());
    }

    public String inputStream(String JsonInput) throws IOException {
        JsonInput = "src/test/resources/Onap3gppServiceInstancesTest" + JsonInput;
        return new String(Files.readAllBytes(Paths.get(JsonInput)));
    }

    public ResponseEntity<String> sendRequest(String requestJson, String uriPath, HttpMethod reqMethod) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ONAPLogConstants.Headers.PARTNER_NAME, "test_name");
        headers.set(TRANSACTION_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        headers.set(ONAP_REQUEST_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        headers.set(ONAPLogConstants.MDCs.REQUEST_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        headers.set(ONAP_PARTNER_NAME, "VID");
        headers.set(REQUESTOR_ID, "xxxxxx");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(uriPath));
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        return restTemplate.exchange(builder.toUriString(), reqMethod, request, String.class);
    }

    @Test
    public void createServiceInstanceTest() throws IOException {
        String uri = ONAP3GPPSERVICES_URI + "v1/allocate";
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/commonNssmfTest")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("Camunda/BPMN_response.json")
                        .withStatus(org.apache.http.HttpStatus.SC_ACCEPTED)));

        String expectedResponse =
                "{\"jobId\":\"db245365e79c47ed88fcd60caa8f6549\",\"status\":\"\",\"statusDescription\":{}}";
        ResponseEntity<String> response = sendRequest(inputStream("/allocateRequest.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        String actualResponse = response.getBody();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void updateServiceInstanceTest() throws IOException {
        String uri = ONAP3GPPSERVICES_URI + "v1/modify";
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/commonNssmfTest")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("Camunda/BPMN_response.json")
                        .withStatus(org.apache.http.HttpStatus.SC_ACCEPTED)));

        String expectedResponse =
                "{\"jobId\":\"db245365e79c47ed88fcd60caa8f6549\",\"status\":\"\",\"statusDescription\":{}}";
        ResponseEntity<String> response = sendRequest(inputStream("/modifyRequest.json"), uri, HttpMethod.PUT);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        String actualResponse = response.getBody();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void deleteServiceInstanceTest() throws IOException {
        String uri = ONAP3GPPSERVICES_URI + "v1/deAllocate";
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/commonNssmfTest")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("Camunda/BPMN_response.json")
                        .withStatus(org.apache.http.HttpStatus.SC_ACCEPTED)));
        String expectedResponse =
                "{\"jobId\":\"db245365e79c47ed88fcd60caa8f6549\",\"status\":\"\",\"statusDescription\":{}}";
        ResponseEntity<String> response = sendRequest(inputStream("/deAllocate.json"), uri, HttpMethod.DELETE);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        String actualResponse = response.getBody();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void activateServiceInstanceTest() throws IOException {
        String uri = ONAP3GPPSERVICES_URI + "v1/activate";
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/commonNssmfTest")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("Camunda/BPMN_response.json")
                        .withStatus(org.apache.http.HttpStatus.SC_ACCEPTED)));
        String expectedResponse =
                "{\"jobId\":\"db245365e79c47ed88fcd60caa8f6549\",\"status\":\"\",\"statusDescription\":{}}";
        ResponseEntity<String> response = sendRequest(inputStream("/activateRequest.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        String actualResponse = response.getBody();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void deActivateServiceInstance() throws IOException {
        String uri = ONAP3GPPSERVICES_URI + "v1/deActivate";
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/commonNssmfTest")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("Camunda/BPMN_response.json")
                        .withStatus(org.apache.http.HttpStatus.SC_ACCEPTED)));
        String expectedResponse =
                "{\"jobId\":\"db245365e79c47ed88fcd60caa8f6549\",\"status\":\"\",\"statusDescription\":{}}";
        ResponseEntity<String> response = sendRequest(inputStream("/deActivateRequest.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        String actualResponse = response.getBody();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void getSliceSubnetCapabilitiesTest() throws IOException, ApiException {
        String request = "{\"subnetTypes\":[\"AN\"]}";
        QuerySubnetCapability subnetCapabilityRequest = MAPPER.readValue(request, QuerySubnetCapability.class);
        String expectedResponse =
                "{\"AN\":{\"latency\":5,\"maxNumberofUEs\":\"100\",\"maxThroughput\":\"150\",\"terminalDensity\":\"50\"}}";
        Response response = objUnderTest.getSliceSubnetCapabilities(subnetCapabilityRequest, "v1");
        String actualResponse = (String) response.getEntity();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(expectedResponse, actualResponse);
    }
}


