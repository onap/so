/*******************************************************************************
 * ============LICENSE_START======================================================= ONAP - SO
 * ================================================================================ Copyright (C) 2020 Wipro Limited.
 * ============================================================================== Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 *
 *******************************************************************************/

package org.onap.so.apihandlerinfra;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Onap3gppServiceInstancesTest extends BaseTest {

    private String onap3gppServicesUri = "/onap/so/infra/onap3gppServiceInstances/";

    private final ObjectMapper mapper = new ObjectMapper();

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
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlPathEqualTo("/serviceRecipe/search/findFirstByServiceModelUUIDAndAction"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

    }

    public String inputStream(String JsonInput) throws IOException {
        JsonInput = "src/test/resources/Onap3gppServiceInstancesTest" + JsonInput;
        return new String(Files.readAllBytes(Paths.get(JsonInput)));
    }

    public ResponseEntity<String> sendRequest(String requestJson, String uriPath, HttpMethod reqMethod) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(uriPath));
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        return restTemplate.exchange(builder.toUriString(), reqMethod, request, String.class);
    }

    @Test
    public void createServiceInstanceTest() throws IOException {
        String uri = onap3gppServicesUri + "v1/allocate";
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
        String uri = onap3gppServicesUri + "v1/allocate";
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/commonNssmfTest")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("Camunda/BPMN_response.json")
                        .withStatus(org.apache.http.HttpStatus.SC_ACCEPTED)));

        String expectedResponse =
                "{\"jobId\":\"db245365e79c47ed88fcd60caa8f6549\",\"status\":\"\",\"statusDescription\":{}}";
        ResponseEntity<String> response = sendRequest(inputStream("/modifyRequest.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        String actualResponse = response.getBody();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void deleteServiceInstanceTest() throws IOException {
        String uri = onap3gppServicesUri + "v1/allocate";
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/commonNssmfTest")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("Camunda/BPMN_response.json")
                        .withStatus(org.apache.http.HttpStatus.SC_ACCEPTED)));
        String expectedResponse =
                "{\"jobId\":\"db245365e79c47ed88fcd60caa8f6549\",\"status\":\"\",\"statusDescription\":{}}";
        ResponseEntity<String> response = sendRequest(inputStream("/deAllocate.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        String actualResponse = response.getBody();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void activateServiceInstanceTest() throws IOException {
        String uri = onap3gppServicesUri + "v1/allocate";
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
        String uri = onap3gppServicesUri + "v1/allocate";
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

}
