/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.apihandlerinfra;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.onap.so.logging.filter.base.Constants.HttpHeaders.ONAP_PARTNER_NAME;
import static org.onap.so.logging.filter.base.Constants.HttpHeaders.ONAP_REQUEST_ID;
import static org.onap.so.logging.filter.base.Constants.HttpHeaders.TRANSACTION_ID;
import static org.onap.so.logger.HttpHeadersConstants.REQUESTOR_ID;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandlerinfra.exceptions.ContactCamundaException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Fault;

public class ServiceInstancesTest extends BaseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ServiceInstances servInstances;

    @Autowired
    private RequestHandlerUtils requestHandlerUtils;

    @Value("${wiremock.server.port}")
    private String wiremockPort;

    private final String servInstanceuri = "/onap/so/infra/serviceInstantiation/";
    private final String servInstanceUriPrev7 = "/onap/so/infra/serviceInstances/";
    private final String orchestration_path = "/onap/so/infra";

    private String uri;
    private URL selfLink;
    private URL initialUrl;
    private int initialPort;
    private HttpHeaders headers;

    @Before
    public void beforeClass() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests/")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_OK)));
        Mockito.doReturn(null).when(requestsDbClient).getInfraActiveRequestbyRequestId(Mockito.any());
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
    public void createServiceInstanceVIDDefault() throws IOException {
        TestAppender.events.clear();

        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");


        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        // expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v5", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST, headers);

        // then
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createServiceInstanceServiceInstancesUri() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/CreateGenericALaCarteServiceInstance");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/CreateGenericALaCarteServiceInstance"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));


        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));
        // expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v5", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v5";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstancePrev7.json"), uri, HttpMethod.POST, headers);

        // then
        assertEquals(404, response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
    }

    @Test
    public void createServiceInstanceBpelStatusError() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");


        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_BAD_GATEWAY)));


        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceStatusError.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void createServiceInstanceBadGateway() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withStatus(org.apache.http.HttpStatus.SC_BAD_GATEWAY).withBody("{}")));

        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceBadGateway.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void createServiceInstanceEmptyResponse() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceEmpty.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_GATEWAY.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void activateServiceInstanceNoRecipeALaCarte() throws IOException {
        TestAppender.events.clear();
        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/activate";
        HttpHeaders requestIDheaders = new HttpHeaders();
        requestIDheaders.set(ONAPLogConstants.Headers.REQUEST_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceALaCarteTrueNoRecipe.json"), uri,
                HttpMethod.POST, requestIDheaders);

        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));


        wireMockServer.stubFor(get(urlMatching(
                ".*/serviceRecipe/search/findFirstByServiceModelUUIDAndAction?serviceModelUUID=d88da85c-d9e8-4f73-b837-3a72a431622a&action=activateInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withStatus(HttpStatus.SC_NOT_FOUND)));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void activateServiceInstanceNoRecipe() throws IOException {
        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/activate";
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search/.*")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_NOT_FOUND)));

        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceNoRecipe.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void activateServiceInstance() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/activate";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceActivate.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deactivateServiceInstance() throws IOException {

        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service-design-and-creation/services/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("/aai/ServiceFromAAI.json").withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/deactivate";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDeactivate.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deleteServiceInstance() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service-design-and-creation/services/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("/aai/ServiceFromAAI.json").withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a8868/";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDelete.json"), uri, HttpMethod.DELETE, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void assignServiceInstance() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service-design-and-creation/services/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("/aai/ServiceFromAAI.json").withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/assign";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceAssign.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void unassignServiceInstance() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/unassign";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceUnassign.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createPortConfiguration() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v5", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstancePortConfiguration.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
        assertTrue(response.getBody().contains("1882939"));
    }

    @Test
    public void createPortConfigurationEmptyProductFamilyId() throws IOException {
        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceParseFail.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void deletePortConfiguration() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations/f7ce78bb-423b-11e7-93f8-0050569a7970";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstance.json"), uri, HttpMethod.DELETE, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void enablePort() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations/f7ce78bb-423b-11e7-93f8-0050569a7970/enablePort";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceEnablePort.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void disablePort() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations/f7ce78bb-423b-11e7-93f8-0050569a7970/disablePort";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDisablePort.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void activatePort() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations/f7ce78bb-423b-11e7-93f8-0050569a7970/activate";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceActivatePort.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deactivatePort() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations/f7ce78bb-423b-11e7-93f8-0050569a7970/deactivate";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDeactivatePort.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void addRelationships() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/addRelationships";
        ResponseEntity<String> response =
                sendRequest(inputStream("/AddRelationships.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void removeRelationships() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/removeRelationships";
        ResponseEntity<String> response =
                sendRequest(inputStream("/RemoveRelationships.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createVnfInstanceNoALaCarte() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));


        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfResourceCustomization/search/findByModelCustomizationUUID[?]MODEL_CUSTOMIZATION_UUID=68dc9a92-214c-11e7-93ae-92361f002671"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb(
                                        "vnfResourceCustomization_ReplaceVnf_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfResourceCustomization/1/vnfResources"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vnfResources_ReplaceVnf_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfRecipe/search/findFirstVnfRecipeByNfRoleAndAction[?]nfRole=GR-API-DEFAULT&action=createInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfRecipeReplaceInstance_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/49585b36-2b5a-443a-8b10-c75d34bb5e46/vnfs";
        ResponseEntity<String> response =
                sendRequest(inputStream("/VnfCreateDefault.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createVnfInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/5df8b6de-2083-11e7-93ae-92361f002672"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("serviceVnf_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlMatching(".*/service/5df8b6de-2083-11e7-93ae-92361f002672/vnfCustomizations"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vnfResourceCustomizationsList_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));


        wireMockServer.stubFor(
                get(urlMatching(".*/vnfResourceCustomization/68dc9a92-214c-11e7-93ae-92361f002672/vnfResources"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfResourcesCreateVnf_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfRecipe/search/findFirstVnfRecipeByNfRoleAndAction[?]nfRole=GR-API-DEFAULT&action=createInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfRecipeCreateInstance_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vnfs";
        ResponseEntity<String> response =
                sendRequest(inputStream("/VnfWithServiceRelatedInstance.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
        assertTrue(response.getBody().contains("1882939"));
    }

    @Test
    public void createVnfWithServiceRelatedInstanceFail() throws IOException {
        uri = servInstanceUriPrev7 + "v6" + "/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs";
        ResponseEntity<String> response =
                sendRequest(inputStream("/VnfWithServiceRelatedInstanceFail.json"), uri, HttpMethod.POST);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void createVnfInstanceInvalidVnfResource() throws IOException {
        uri = servInstanceuri + "v7" + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vnfs";
        ResponseEntity<String> response = sendRequest(inputStream("/NoVnfResource.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals("No valid vnfResource is specified", realResponse.getServiceException().getText());
    }

    @Test
    public void replaceVnfInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfResourceCustomization/search/findByModelCustomizationUUID[?]MODEL_CUSTOMIZATION_UUID=68dc9a92-214c-11e7-93ae-92361f002671"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb(
                                        "vnfResourceCustomization_ReplaceVnf_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfResourceCustomization/1/vnfResources"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vnfResources_ReplaceVnf_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfRecipe/search/findFirstVnfRecipeByNfRoleAndAction[?]nfRole=GR-API-DEFAULT&action=replaceInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfRecipeReplaceInstance_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/replace";
        ResponseEntity<String> response = sendRequest(inputStream("/ReplaceVnf.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void replaceVnfInstanceNoCloudConfig() throws IOException {
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests/v1/getInfraActiveRequests.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("infra/VnfLookup.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfResourceCustomization/search/findByModelCustomizationUUID[?]MODEL_CUSTOMIZATION_UUID=68dc9a92-214c-11e7-93ae-92361f002671"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb(
                                        "vnfResourceCustomization_ReplaceVnf_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlMatching(".*/vnfResourceCustomization/1/vnfResources"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vnfResources_ReplaceVnf_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfRecipe/search/findFirstVnfRecipeByNfRoleAndAction[?]nfRole=GR-API-DEFAULT&action=replaceInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfRecipeReplaceInstance_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/replace";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ReplaceVnfNoCloudConfig.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void replaceVnfRecreateInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/RecreateInfraVce"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfResourceCustomization/search/findByModelCustomizationUUID[?]MODEL_CUSTOMIZATION_UUID=68dc9a92-214c-11e7-93ae-92361f002674"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfResourceCustomization_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfResourceCustomization/4/vnfResources"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vnfResources_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfRecipe/search/findFirstVnfRecipeByNfRoleAndAction[?]nfRole=TEST&action=replaceInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfRecipe_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/replace";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ReplaceVnfRecreate.json"), uri, HttpMethod.POST, headers);
        logger.debug(response.getBody());

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void recreateVnfInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfResourceCustomization/search/findByModelCustomizationUUID[?]MODEL_CUSTOMIZATION_UUID=68dc9a92-214c-11e7-93ae-92361f002674"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfResourceCustomization_Response"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfResourceCustomization/4/vnfResources"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vnfResources_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfRecipe/search/findFirstVnfRecipeByNfRoleAndAction[?]nfRole=GR-API-DEFAULT&action=recreateInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfRecipe_ResponseWorkflowAction.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/recreate";
        ResponseEntity<String> response = sendRequest(inputStream("/VnfRecreate.json"), uri, HttpMethod.POST, headers);
        logger.debug(response.getBody());

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void updateVnfInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfResourceCustomization/search/findByModelCustomizationUUID[?]MODEL_CUSTOMIZATION_UUID=68dc9a92-214c-11e7-93ae-92361f002674"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfResourceCustomization_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfResourceCustomization/4/vnfResources"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vnfResources_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfRecipe/search/findFirstVnfRecipeByNfRoleAndAction"
                + "[?]nfRole=GR-API-DEFAULT&action=updateInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("UpdateVnfRecipe_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response = sendRequest(inputStream("/UpdateVnf.json"), uri, HttpMethod.PUT, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void applyUpdatedConfig() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/VnfConfigUpdate"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));


        wireMockServer.stubFor(get(urlMatching(".*/vnfRecipe/search/findFirstVnfRecipeByNfRoleAndAction"
                + "[?]nfRole=GR-API-DEFAULT&action=applyUpdatedConfig"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfRecipeApplyUpdatedConfig_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/applyUpdatedConfig";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ApplyUpdatedConfig.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deleteVnfInstanceV5() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfRecipe/search/findFirstVnfRecipeByNfRoleAndAction"
                + "[?]nfRole=GR-API-DEFAULT&action=deleteInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfRecipeDelete_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v5", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v5"
                + "/serviceInstances/e446b97d-9c35-437a-95a2-6b4c542c4507/vnfs/49befbfe-fccb-421d-bb4c-0734a43f5ea0";
        ResponseEntity<String> response =
                sendRequest(inputStream("/DeleteVnfV5.json"), uri, HttpMethod.DELETE, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createCnfInstanceNoALaCarte() throws IOException {

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/5df8b6de-2083-11e7-93ae-92361f002672"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("serviceCnf_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/cnfs";
        ResponseEntity<String> response =
                sendRequest(inputStream("/CnfWithServiceRelatedInstance.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
        assertTrue(response.getBody().contains("1882939"));
    }

    @Test
    public void createVfModuleInstance() throws IOException {
        wireMockServer.stubFor(get(urlMatching(
                "/vfModuleCustomization/search/findFirstByModelCustomizationUUIDOrderByCreatedDesc\\?MODEL_CUSTOMIZATION_UUID=cb82ffd8-252a-11e7-93ae-92361f002671"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vfModuleCustomization_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/vfModule/20c4431c-246d-11e7-93ae-92361f002671"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModule_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching("/vfModuleCustomization/1/vfModule"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModule_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                "/vfModuleCustomization/search/findFirstByModelCustomizationUUIDOrderByCreatedDesc\\?MODEL_CUSTOMIZATION_UUID=20c4431c-246d-11e7-93ae-92361f002671"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vfModule_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/CreateVfModuleInfra"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                "/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=20c4431c-246d-11e7-93ae-92361f002671&vnfComponentType=vfModule&action=createInstance"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb("vnfComponentRecipe_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/7a88cbeb-0ec8-4765-a271-4f9e90c3da7b/vnfs/cbba721b-4803-4df7-9347-307c9a955426/vfModules";
        ResponseEntity<String> response =
                sendRequest(inputStream("/VfModuleWithRelatedInstances.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
        assertTrue(response.getBody().contains("1882939"));
    }

    @Test
    public void createVfModuleInstanceNoModelCustomization() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/CreateVfModuleInfra"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfResource/fe6478e4-ea33-3346-ac12-ab121484a3fe"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vnfResourceForVfModule_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer
                .stubFor(get(urlMatching(".*/vnfResourceCustomization/search/findByModelInstanceNameAndVnfResources"
                        + "[?]MODEL_INSTANCE_NAME=test&VNF_RESOURCE_MODEL_UUID=fe6478e4-ea33-3346-ac12-ab121484a3fe"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfResourceCustomizationForVfModule_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfResourceCustomization/3/vfModuleCustomizations"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModuleCustomizationsPCM_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vfModuleCustomization/search/findFirstByModelCustomizationUUIDAndVfModuleModelUUIDOrderByCreatedDesc[?]"
                        + "MODEL_CUSTOMIZATION_UUID=b4ea86b4-253f-11e7-93ae-92361f002672&MODEL_UUID=066de97e-253e-11e7-93ae-92361f002672"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vfModuleCustomizationPCM_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vfModuleCustomization/2/vfModule"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModulePCM_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vfModule/066de97e-253e-11e7-93ae-92361f002672"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModulePCM_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));


        wireMockServer.stubFor(
                get(urlMatching(".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVnfComponentTypeAndAction"
                        + "[?]vnfComponentType=vfModule&action=createInstance"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipeVNF_API_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v6", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v6"
                + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules";
        ResponseEntity<String> response =
                sendRequest(inputStream("/VfModuleNoModelCustomization.json"), uri, HttpMethod.POST, headers);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deleteVfModuleInstanceNoMatchingModelUUD() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfResource/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vnfResourceForVfModule_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer
                .stubFor(get(urlMatching(".*/vnfResourceCustomization/search/findByModelInstanceNameAndVnfResources.*"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb(
                                        "vnfResourceCustomizationForVfModule_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfResourceCustomization/3/vfModuleCustomizations"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModuleCustomizationsPCM_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vfModuleCustomization/search/findFirstByModelCustomizationUUIDOrderByCreatedDesc[?]MODEL_CUSTOMIZATION_UUID=b4ea86b4-253f-11e7-93ae-92361f002672"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vfModuleCustomizationPCM_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vfModuleCustomization/2/vfModule"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModulePCM_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vfModule/066de97e-253e-11e7-93ae-92361f002672"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModulePCM_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=GR-API-DEFAULT&vnfComponentType=vfModule&action=deleteInstance"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipeDeleteVfModule_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v6", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v6"
                + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response =
                sendRequest(inputStream("/VfModuleNoMatchingModelUUID.json"), uri, HttpMethod.DELETE, headers);


        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createVfModuleInstanceNoRecipe() throws IOException {

        wireMockServer.stubFor(get(urlMatching(".*/vnfResource/fe6478e4-ea33-3346-ac12-ab121484a3fe"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vnfResourceForVfModule_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer
                .stubFor(get(urlMatching(".*/vnfResourceCustomization/search/findByModelInstanceNameAndVnfResources"
                        + "[?]MODEL_INSTANCE_NAME=test&VNF_RESOURCE_MODEL_UUID=fe6478e4-ea33-3346-ac12-ab121484a3fe"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfResourceCustomizationForVfModule_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfResourceCustomization/3/vfModuleCustomizations"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModuleCustomizationsPCM_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(
                get(urlMatching(".*/vfModuleCustomization/search/findByModelCustomizationUUIDAndVfModuleModelUUID[?]"
                        + "modelCustomizationUUID=b4ea86b4-253f-11e7-93ae-92361f002672&vfModuleModelUUID=066de97e-253e-11e7-93ae-92361f002672"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vfModuleCustomizationPCM_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v6"
                + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules";
        ResponseEntity<String> response =
                sendRequest(inputStream("/VfModuleInvalid.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals("No valid vfModuleCustomization is specified", realResponse.getServiceException().getText());
    }

    @Test
    public void replaceVfModuleInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer
                .stubFor(get(urlMatching(".*/vfModule/search/findFirstVfModuleByModelInvariantUUIDAndModelVersion[?]"
                        + "modelInvariantUUID=78ca26d0-246d-11e7-93ae-92361f002671&modelVersion=2"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb("vfModule_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=GR-API-DEFAULT&vnfComponentType=vfModule&action=replaceInstance"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipeDeleteVfModule_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000/replace";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ReplaceVfModule.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void replaceVfModuleInstanceNoCloudConfigurationTest() throws IOException {
        wireMockServer.stubFor(
                get(urlPathMatching("/aai/v\\d+/network/generic-vnfs/generic-vnf/ff305d54-75b4-431b-adb2-eb6b9e5ff000"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile("infra/Vnf.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        wireMockServer
                .stubFor(get(urlMatching(".*/vfModule/search/findFirstVfModuleByModelInvariantUUIDAndModelVersion[?]"
                        + "modelInvariantUUID=78ca26d0-246d-11e7-93ae-92361f002671&modelVersion=2"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb("vfModule_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=GR-API-DEFAULT&vnfComponentType=vfModule&action=replaceInstance"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipeDeleteVfModule_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000/replace";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ReplaceVfModuleNoCloudConfig.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void updateVfModuleInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vfModuleCustomization/search/findFirstByModelCustomizationUUIDOrderByCreatedDesc[?]MODEL_CUSTOMIZATION_UUID=cb82ffd8-252a-11e7-93ae-92361f002671"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vfModuleCustomization_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vfModuleCustomization/1/vfModule"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModule_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vfModule/20c4431c-246d-11e7-93ae-92361f002671"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModule_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=GR-API-DEFAULT&vnfComponentType=vfModule&action=updateInstance"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipe_GRAPI_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response =
                sendRequest(inputStream("/UpdateVfModule.json"), uri, HttpMethod.PUT, headers);
        logger.debug(response.getBody());

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createVfModuleNoModelType() throws IOException {
        InfraActiveRequests expectedRecord = new InfraActiveRequests();
        expectedRecord.setRequestStatus("FAILED");
        expectedRecord.setStatusMessage("Error parsing request: No valid modelType is specified");
        expectedRecord.setProgress(100L);
        expectedRecord.setSource("VID");
        expectedRecord.setRequestBody(inputStream("/VfModuleNoModelType.json"));
        expectedRecord.setLastModifiedBy("APIH");
        expectedRecord.setVfModuleName("testVfModule2");
        expectedRecord.setVfModuleModelName("serviceModel");
        expectedRecord.setRequestScope("vfModule");
        expectedRecord.setRequestAction("createInstance");
        expectedRecord.setRequestorId("zz9999");
        expectedRecord.setRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");
        // VnfType is not sent in this request, should be blank in db
        expectedRecord.setVnfType("");
        uri = servInstanceuri
                + "v5/serviceInstances/32807a28-1a14-4b88-b7b3-2950918aa76d/vnfs/32807a28-1a14-4b88-b7b3-2950918aa76d/vfModules";

        ResponseEntity<String> response =
                sendRequest(inputStream("/VfModuleNoModelType.json"), uri, HttpMethod.POST, headers);
        // ActualRecord
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        assertEquals("32807a28-1a14-4b88-b7b3-2950918aa76d",
                response.getHeaders().get(ONAPLogConstants.Headers.REQUEST_ID).get(0));
    }

    @Test
    public void inPlaceSoftwareUpdate() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/VnfInPlaceUpdate"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfRecipe/search/findFirstVnfRecipeByNfRoleAndAction[?]"
                + "nfRole=GR-API-DEFAULT&action=inPlaceSoftwareUpdate"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfRecipeInPlaceUpdate_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/inPlaceSoftwareUpdate";
        ResponseEntity<String> response =
                sendRequest(inputStream("/InPlaceSoftwareUpdate.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deleteVfModuleInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer
                .stubFor(get(urlMatching(".*/vfModule/search/findFirstVfModuleByModelInvariantUUIDAndModelVersion[?]"
                        + "modelInvariantUUID=78ca26d0-246d-11e7-93ae-92361f002671&modelVersion=2"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb("vfModule_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=GR-API-DEFAULT&vnfComponentType=vfModule&action=deleteInstance"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipeDeleteVfModule_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response =
                sendRequest(inputStream("/DeleteVfModule.json"), uri, HttpMethod.DELETE, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deleteVfModuleNoModelInvariantId() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=VNF-API-DEFAULT&vnfComponentType=vfModule&action=deleteInstance"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipeDeleteVfModule_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/196b4a84-0858-4317-a1f6-497e2e52ae43/vnfs/36e4f902-ec32-451e-8d53-e3edc19e40a4/vfModules/09f3a38d-933f-450a-8784-9e6c4dec3f72";
        ResponseEntity<String> response =
                sendRequest(inputStream("/DeleteVfModuleNoModelInvariantId.json"), uri, HttpMethod.DELETE, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deactivateAndCloudDeleteVfModuleInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer
                .stubFor(get(urlMatching(".*/vfModule/search/findFirstVfModuleByModelInvariantUUIDAndModelVersion[?]"
                        + "modelInvariantUUID=78ca26d0-246d-11e7-93ae-92361f002671&modelVersion=2"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb("vfModule_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=GR-API-DEFAULT&vnfComponentType=vfModule&action=deactivateAndCloudDelete"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipeDeactivate_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000/deactivateAndCloudDelete";
        ResponseEntity<String> response =
                sendRequest(inputStream("/DeactivateAndCloudDeleteVfModule.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createVolumeGroupInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vfModuleCustomization/search/findFirstByModelCustomizationUUIDOrderByCreatedDesc[?]MODEL_CUSTOMIZATION_UUID=b4ea86b4-253f-11e7-93ae-92361f002671"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vfModuleCustomizationVolGrp_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vfModuleCustomization/1/vfModule"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModuleVolGroup_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=GR-API-DEFAULT&vnfComponentType=volumeGroup&action=createInstance"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipeVolGrp_GRAPI_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/volumeGroups";
        ResponseEntity<String> response = sendRequest(inputStream("/VolumeGroup.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
        assertTrue(response.getBody().contains("1882939"));
    }

    @Test
    public void updateVolumeGroupInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vfModuleCustomization/search/findFirstByModelCustomizationUUIDOrderByCreatedDesc[?]MODEL_CUSTOMIZATION_UUID=b4ea86b4-253f-11e7-93ae-92361f002671"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vfModuleCustomizationVolGrp_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vfModuleCustomization/1/vfModule"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModuleVolGroup_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=GR-API-DEFAULT&vnfComponentType=volumeGroup&action=updateInstance"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipeVolGrp_GRAPI_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/volumeGroups/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response =
                sendRequest(inputStream("/UpdateVolumeGroup.json"), uri, HttpMethod.PUT, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deleteVolumeGroupInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vfModuleCustomization/search/findFirstByModelCustomizationUUIDOrderByCreatedDesc[?]MODEL_CUSTOMIZATION_UUID=b4ea86b4-253f-11e7-93ae-92361f002671"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vfModuleCustomizationVolGrp_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vfModuleCustomization/1/vfModule"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModuleVolGroup_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=GR-API-DEFAULT&vnfComponentType=volumeGroup&action=deleteInstance"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipeVolGrp_GRAPI_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/volumeGroups/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response =
                sendRequest(inputStream("/DeleteVolumeGroup.json"), uri, HttpMethod.DELETE, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createNetworkInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("networkResourceCustomization_Response.json"))
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(
                get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac/networkResource"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkResource_Response.json"))
                                .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkRecipe/search/findFirstByModelNameAndAction[?]"
                + "modelName=GR-API-DEFAULT&action=createInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkRecipe_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks";
        ResponseEntity<String> response =
                sendRequest(inputStream("/NetworkCreate.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void updateNetworkInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("networkResourceCustomization_Response.json"))
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(
                get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac/networkResource"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkResource_Response.json"))
                                .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkRecipe/search/findFirstByModelNameAndAction[?]"
                + "modelName=GR-API-DEFAULT&action=updateInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkRecipe_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks/1710966e-097c-4d63-afda-e0d3bb7015fb";
        ResponseEntity<String> response = sendRequest(inputStream("/UpdateNetwork.json"), uri, HttpMethod.PUT, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
        assertTrue(response.getBody().contains("1882939"));
    }

    @Test
    public void deleteNetworkInstance() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("networkResourceCustomization_Response.json"))
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(
                get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac/networkResource"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkResource_Response.json"))
                                .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkRecipe/search/findFirstByModelNameAndAction[?]"
                + "modelName=VNF-API-DEFAULT&action=deleteInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkRecipe_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks/1710966e-097c-4d63-afda-e0d3bb7015fb";
        ResponseEntity<String> response =
                sendRequest(inputStream("/NetworkInstance.json"), uri, HttpMethod.DELETE, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deleteNetworkInstanceNoReqParams() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkRecipe/search/findFirstByModelNameAndAction[?]"
                + "modelName=GR-API-DEFAULT&action=deleteInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkRecipe_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));


        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v6", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v6"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks/1710966e-097c-4d63-afda-e0d3bb7015fb";
        ResponseEntity<String> response =
                sendRequest(inputStream("/NetworkInstanceNoReqParams.json"), uri, HttpMethod.DELETE, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void convertJsonToServiceInstanceRequestFail() throws IOException {
        // ExpectedRecord
        InfraActiveRequests expectedRecord = new InfraActiveRequests();
        expectedRecord.setRequestStatus("FAILED");
        expectedRecord.setStatusMessage("Error mapping request: ");
        expectedRecord.setProgress(100L);
        expectedRecord.setRequestBody(inputStream("/ConvertRequestFail.json"));
        expectedRecord.setLastModifiedBy("APIH");
        expectedRecord.setRequestScope("network");
        expectedRecord.setRequestAction("deleteInstance");
        expectedRecord.setRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");

        uri = servInstanceuri + "v6"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks/1710966e-097c-4d63-afda-e0d3bb7015fb";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ConvertRequestFail.json"), uri, HttpMethod.DELETE, headers);

        // ActualRecord

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void convertJsonToServiceInstanceRequestConfigurationFail() throws IOException {
        uri = servInstanceuri + "v5"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/configurations/test/enablePort";
        ResponseEntity<String> response = sendRequest(inputStream("/ConvertRequestFail.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void creatServiceInstanceGRTestApiNoCustomRecipeFound() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service-design-and-creation/services/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("/aai/ServiceFromAAI.json").withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        uri = servInstanceuri + "v7" + "/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceMacro.json"), uri, HttpMethod.POST, headers);

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createNetworkInstanceTestApiUndefinedUsePropertiesDefault() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("networkResourceCustomization_Response.json"))
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(
                get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac/networkResource"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkResource_Response.json"))
                                .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkRecipe/search/findFirstByModelNameAndAction[?]"
                + "modelName=GR-API-DEFAULT&action=createInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkRecipe_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks";
        ResponseEntity<String> response =
                sendRequest(inputStream("/NetworkCreateAlternateInstanceName.json"), uri, HttpMethod.POST, headers);

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createNetworkInstanceTestApiIncorrectUsePropertiesDefault() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks";
        ResponseEntity<String> response =
                sendRequest(inputStream("/NetworkCreateTestApiIncorrect.json"), uri, HttpMethod.POST);

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void createNetworkInstanceTestApiGrApi() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("networkResourceCustomization_Response.json"))
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(
                get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac/networkResource"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkResource_Response.json"))
                                .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkRecipe/search/findFirstByModelNameAndAction[?]"
                + "modelName=GR-API-DEFAULT&action=createInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkRecipe_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks";
        ResponseEntity<String> response =
                sendRequest(inputStream("/NetworkCreateTestApiGrApi.json"), uri, HttpMethod.POST, headers);

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void createNetworkInstanceTestApiVnfApi() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/CreateNetworkInstance"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("networkResourceCustomization_Response.json"))
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(
                get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac/networkResource"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkResource_Response.json"))
                                .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkRecipe/search/findFirstByModelNameAndAction[?]"
                + "modelName=VNF-API-DEFAULT&action=createInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkRecipeVNF_API_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks";
        ResponseEntity<String> response =
                sendRequest(inputStream("/NetworkCreateTestApiVnfApi.json"), uri, HttpMethod.POST, headers);

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void activateServiceInstanceRequestStatus() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service-design-and-creation/services/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("/aai/ServiceFromAAI.json").withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        // expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v5", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7999/activate";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstancePrev8.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void invalidRequestId() throws IOException {
        String illegalRequestId = "1234";
        HttpHeaders ivalidRequestIdHeaders = new HttpHeaders();
        ivalidRequestIdHeaders.set(ONAPLogConstants.Headers.REQUEST_ID, illegalRequestId);
        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST, ivalidRequestIdHeaders);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        assertTrue(response.getBody().contains("Request Id " + illegalRequestId + " is not a valid UUID"));
    }

    @Test
    public void invalidBPELResponse() throws IOException {

        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile("Camunda/TestResponseInvalid2.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals("Request Failed due to BPEL error with HTTP Status = 202{\"instanceId\": \"1882939\"}",
                realResponse.getServiceException().getText());
    }

    @Test
    public void unauthorizedBPELResponse() throws IOException {

        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(org.apache.http.HttpStatus.SC_UNAUTHORIZED)));

        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals("Request Failed due to BPEL error with HTTP Status = 401 UNAUTHORIZED",
                realResponse.getServiceException().getText());
    }

    @Test
    public void invalidBPELResponse2() throws IOException {

        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile("Camunda/TestResponseInvalid.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));
        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertTrue(realResponse.getServiceException().getText()
                .contains("<aetgt:ErrorMessage>Exception in create execution list 500"));
    }

    @Test
    public void createMacroServiceInstance() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/CreateMacroServiceNetworkVnf");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/CreateMacroServiceNetworkVnf"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBodyFile("Camunda/TestResponseInvalid.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        // expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v5", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v5";
        ResponseEntity<String> response =
                sendRequest(inputStream("/MacroServiceInstance.json"), uri, HttpMethod.POST, headers);

        // then
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void testUserParams() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest request =
                mapper.readValue(inputStream("/MacroServiceInstance.json"), ServiceInstancesRequest.class);
        RequestParameters requestParameters = request.getRequestDetails().getRequestParameters();
        String userParamsTxt = inputStream("/userParams.txt");

        List<Map<String, Object>> userParams = requestHandlerUtils.configureUserParams(requestParameters);
        System.out.println(userParams);
        assertTrue(userParams.size() > 0);
        assertTrue(userParams.get(0).containsKey("name"));
        assertTrue(userParams.get(0).containsKey("value"));
        assertEquals(userParamsTxt.replaceAll("\\s+", ""), userParams.toString().replaceAll("\\s+", ""));
    }

    @Test
    public void testConfigureCloudConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest request =
                mapper.readValue(inputStream("/MacroServiceInstance.json"), ServiceInstancesRequest.class);
        CloudConfiguration cloudConfig =
                requestHandlerUtils.configureCloudConfig(request.getRequestDetails().getRequestParameters());

        assertEquals("mdt25b", cloudConfig.getLcpCloudRegionId());
        assertEquals("aefb697db6524ddebfe4915591b0a347", cloudConfig.getTenantId());
    }

    @Test
    public void testMapToLegacyRequest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest request =
                mapper.readValue(inputStream("/MacroServiceInstance.json"), ServiceInstancesRequest.class);
        ServiceInstancesRequest expected =
                mapper.readValue(inputStream("/LegacyMacroServiceInstance.json"), ServiceInstancesRequest.class);
        requestHandlerUtils.mapToLegacyRequest(request.getRequestDetails());
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
        assertThat(request, sameBeanAs(expected));
    }

    @Test
    public void scaleOutVfModule() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vfModuleCustomization/search/findFirstByModelCustomizationUUIDOrderByCreatedDesc[?]MODEL_CUSTOMIZATION_UUID=cb82ffd8-252a-11e7-93ae-92361f002671"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vfModuleCustomization_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vfModuleCustomization/1/vfModule"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModule_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vfModule/20c4431c-246d-11e7-93ae-92361f002671"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vfModule_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));


        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfComponentsRecipe/search/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction"
                        + "[?]vfModuleModelUUID=GR-API-DEFAULT&vnfComponentType=vfModule&action=scaleOut"))
                                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                        .withBody(getWiremockResponseForCatalogdb(
                                                "vnfComponentRecipeVfModuleScaleOut_Response.json"))
                                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vfModule/search/findByModelInvariantUUIDOrderByModelVersionDesc[?]modelInvariantUUID=78ca26d0-246d-11e7-93ae-92361f002671"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vfModulesListByInvariantId_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/7a88cbeb-0ec8-4765-a271-4f9e90c3da7b/vnfs/cbba721b-4803-4df7-9347-307c9a955426/vfModules/scaleOut";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ScaleOutRequest.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
        assertTrue(response.getBody().contains("1882939"));
    }

    @Test
    public void createServiceInstanceBadResponse() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");

        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestBadResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals("Exception caught mapping Camunda JSON response to object",
                realResponse.getServiceException().getText());
    }

    @Test
    public void createServiceInstanceDuplicateError() throws IOException {
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests/checkInstanceNameDuplicate"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(
                "Unable to check for duplicate instance due to error contacting requestDb: org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 Server Error: [no body]",
                realResponse.getServiceException().getText());
    }

    @Test
    public void createServiceInstanceDuplicateHistoryCheck() throws IOException {
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests/checkInstanceNameDuplicate"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("InfraActiveRequests/createInfraActiveRequests.json")
                        .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(get(
                ("/sobpmnengine/history/process-instance?processInstanceBusinessKey=f0a35706-efc4-4e27-80ea-a995d7a2a40f&active=true&maxResults=1"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile("Camunda/HistoryCheckResponse.json")
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(
                "Error: Locked instance - This service (testService9) already has a request being worked with a status of UNLOCKED (RequestId - f0a35706-efc4-4e27-80ea-a995d7a2a40f). The existing request must finish or be cleaned up before proceeding.",
                realResponse.getServiceException().getText());
    }

    @Test
    public void createServiceInstanceDuplicateHistoryCheckException() throws IOException {
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests/checkInstanceNameDuplicate"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("InfraActiveRequests/createInfraActiveRequests.json")
                        .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(get(
                ("/sobpmnengine/history/process-instance?processInstanceBusinessKey=f0a35706-efc4-4e27-80ea-a995d7a2a40f&active=true&maxResults=1"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withStatus(org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(
                "Unable to get process-instance history from Camunda for requestId: f0a35706-efc4-4e27-80ea-a995d7a2a40f due to error: org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 Server Error: [no body]",
                realResponse.getServiceException().getText());
    }

    @Test
    public void createServiceInstanceDuplicate() throws IOException {
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests/checkInstanceNameDuplicate"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(
                "Unable to check for duplicate instance due to error contacting requestDb: org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 Server Error: [no body]",
                realResponse.getServiceException().getText());
    }

    @Test
    public void createServiceInstanceSaveError() throws IOException {
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setOrchestrationUri("/mso/async/services/WorkflowActionBB");
        serviceRecipe.setServiceModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        serviceRecipe.setAction(Action.createInstance.toString());
        serviceRecipe.setId(1);
        serviceRecipe.setRecipeTimeout(180);
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests/"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        wireMockServer.stubFor(get(urlMatching(".*/service/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/serviceRecipe/search.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(
                "Unable to save instance to db due to error contacting requestDb: org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 Server Error: [no body]",
                realResponse.getServiceException().getText());
    }

    @Test
    public void createPortConfigurationSaveError() throws IOException {
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests/"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstancePortConfiguration.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(
                "Unable to save instance to db due to error contacting requestDb: org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 Server Error: [no body]",
                realResponse.getServiceException().getText());
    }

    @Test
    public void createPortConfigDbUpdateError() throws IOException {
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests/"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ServiceInstanceParseFail.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(
                "Unable to save instance to db due to error contacting requestDb: org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 Server Error: [no body]",
                realResponse.getServiceException().getText());
    }

    @Test
    public void vnfUpdateWithNetworkInstanceGroup() throws IOException {
        TestAppender.events.clear();
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(
                ".*/vnfResourceCustomization/search/findByModelCustomizationUUID[?]MODEL_CUSTOMIZATION_UUID=2ccae1b4-7d9e-46fa-a452-9180ce008d17"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("vnfResourceCustomization_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfResourceCustomization/4/vnfResources"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(getWiremockResponseForCatalogdb("vnfResources_Response.json"))
                        .withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/vnfRecipe/search/findFirstVnfRecipeByNfRoleAndAction"
                + "[?]nfRole=GR-API-DEFAULT&action=updateInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("UpdateVnfRecipe_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));
        headers.set(ONAPLogConstants.Headers.PARTNER_NAME, "VID");
        // expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri
                + "v7/serviceInstances/e05864f0-ab35-47d0-8be4-56fd9619ba3c/vnfs/f501ce76-a9bc-4601-9837-74fd9f4d5eca";
        ResponseEntity<String> response =
                sendRequest(inputStream("/VnfwithNeteworkInstanceGroup.json"), uri, HttpMethod.PUT, headers);
        // then
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
        assertEquals(response.getHeaders().get(TRANSACTION_ID).get(0), "32807a28-1a14-4b88-b7b3-2950918aa76d");
    }

    @Test
    public void createInstanceGroup() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "/v7/instanceGroups";
        ResponseEntity<String> response =
                sendRequest(inputStream("/CreateInstanceGroup.json"), uri, HttpMethod.POST, headers);

        // then
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deleteInstanceGroup() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "/v7/instanceGroups/e05864f0-ab35-47d0-8be4-56fd9619ba3c";
        ResponseEntity<String> response = sendRequest(null, uri, HttpMethod.DELETE, headers);

        // then
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deleteInstanceGroupNoRequestIdHeader() throws IOException {
        uri = servInstanceuri + "/v7/instanceGroups/e05864f0-ab35-47d0-8be4-56fd9619ba3c";
        ResponseEntity<String> response = sendRequest(null, uri, HttpMethod.DELETE);
        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(realResponse.getServiceException().getText(), "No valid X-ONAP-RequestID header is specified");
    }

    @Test
    public void deleteInstanceGroupNoPartnerNameHeader() throws IOException {
        HttpHeaders noPartnerHeaders = new HttpHeaders();
        noPartnerHeaders.set(ONAPLogConstants.Headers.REQUEST_ID, "eca3a1b1-43ab-457e-ab1c-367263d148b4");
        noPartnerHeaders.set(REQUESTOR_ID, "xxxxxx");
        uri = servInstanceuri + "/v7/instanceGroups/e05864f0-ab35-47d0-8be4-56fd9619ba3c";
        ResponseEntity<String> response = sendRequest(null, uri, HttpMethod.DELETE, noPartnerHeaders);
        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(realResponse.getServiceException().getText(), "No valid X-ONAP-PartnerName header is specified");
    }

    @Test
    public void deleteInstanceGroupNoRquestorIdHeader() throws IOException {
        HttpHeaders noRequestorIdHheaders = new HttpHeaders();
        noRequestorIdHheaders.set(ONAPLogConstants.Headers.REQUEST_ID, "eca3a1b1-43ab-457e-ab1c-367263d148b4");
        noRequestorIdHheaders.set(ONAPLogConstants.Headers.PARTNER_NAME, "eca3a1b1-43ab-457e-ab1c-367263d148b4");
        // expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "/v7/instanceGroups/e05864f0-ab35-47d0-8be4-56fd9619ba3c";
        ResponseEntity<String> response = sendRequest(null, uri, HttpMethod.DELETE, noRequestorIdHheaders);

        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(realResponse.getServiceException().getText(), "No valid X-RequestorID header is specified");
    }

    @Test
    public void addMembers() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "/v7/instanceGroups/e05864f0-ab35-47d0-8be4-56fd9619ba3c/addMembers";
        ResponseEntity<String> response = sendRequest(inputStream("/AddMembers.json"), uri, HttpMethod.POST, headers);

        // then
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void removeMembers() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        // expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "/v7/instanceGroups/e05864f0-ab35-47d0-8be4-56fd9619ba3c/removeMembers";
        ResponseEntity<String> response =
                sendRequest(inputStream("/RemoveMembers.json"), uri, HttpMethod.POST, headers);

        // then
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void deleteNetworkInstanceNoCustomizationEntry() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_NOT_FOUND)));

        wireMockServer.stubFor(get(urlMatching(".*/networkRecipe/search/findFirstByModelNameAndAction[?]"
                + "modelName=VNF-API-DEFAULT&action=deleteInstance"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(getWiremockResponseForCatalogdb("networkRecipe_Response.json"))
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        // expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        requestReferences.setRequestSelfLink(createExpectedSelfLink("v7", "32807a28-1a14-4b88-b7b3-2950918aa76d"));
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks/1710966e-097c-4d63-afda-e0d3bb7015fb";
        ResponseEntity<String> response =
                sendRequest(inputStream("/NetworkInstance.json"), uri, HttpMethod.DELETE, headers);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }

    @Test
    public void updateNetworkInstanceNoCustomizationEntry() throws IOException {
        wireMockServer.stubFor(get(urlMatching(".*/networkResourceCustomization/3bdbb104-476c-483e-9f8b-c095b3d308ac"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_NOT_FOUND)));

        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks/1710966e-097c-4d63-afda-e0d3bb7015fb";
        ResponseEntity<String> response = sendRequest(inputStream("/UpdateNetwork.json"), uri, HttpMethod.PUT, headers);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(realResponse.getServiceException().getText(),
                "No valid modelCustomizationId for networkResourceCustomization lookup is specified");
    }

    @Test
    public void setServiceTypeTestALaCarte() throws JsonProcessingException {
        String requestScope = ModelType.service.toString();
        Boolean aLaCarteFlag = true;
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setSource("VID");
        requestDetails.setRequestInfo(requestInfo);
        sir.setRequestDetails(requestDetails);
        Service defaultService = new Service();
        defaultService.setServiceType("testServiceTypeALaCarte");

        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        String serviceType = requestHandlerUtils.getServiceType(requestScope, sir, aLaCarteFlag);
        assertEquals(serviceType, "testServiceTypeALaCarte");
    }

    @Test
    public void setServiceTypeTest() throws JsonProcessingException {
        String requestScope = ModelType.service.toString();
        Boolean aLaCarteFlag = false;
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelVersionId("0dd91181-49da-446b-b839-cd959a96f04a");
        requestInfo.setSource("VID");
        requestDetails.setModelInfo(modelInfo);
        requestDetails.setRequestInfo(requestInfo);
        sir.setRequestDetails(requestDetails);
        Service defaultService = new Service();
        defaultService.setServiceType("testServiceType");

        wireMockServer.stubFor(get(urlMatching(".*/service/0dd91181-49da-446b-b839-cd959a96f04a"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        String serviceType = requestHandlerUtils.getServiceType(requestScope, sir, aLaCarteFlag);
        assertEquals(serviceType, "testServiceType");
    }

    @Test
    public void setServiceTypeTestDefault() throws JsonProcessingException {
        String requestScope = ModelType.service.toString();
        Boolean aLaCarteFlag = false;
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelVersionId("0dd91181-49da-446b-b839-cd959a96f04a");
        requestInfo.setSource("VID");
        requestDetails.setModelInfo(modelInfo);
        requestDetails.setRequestInfo(requestInfo);
        sir.setRequestDetails(requestDetails);
        Service defaultService = new Service();
        defaultService.setServiceType("testServiceType");

        wireMockServer.stubFor(get(urlMatching(".*/service/0dd91181-49da-446b-b839-cd959a96f04a"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_NOT_FOUND)));
        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        String serviceType = requestHandlerUtils.getServiceType(requestScope, sir, aLaCarteFlag);
        assertEquals(serviceType, "testServiceType");
    }

    @Test
    public void setServiceTypeTestNetwork() throws JsonProcessingException {
        String requestScope = ModelType.network.toString();
        Boolean aLaCarteFlag = null;
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelName("networkModelName");
        requestInfo.setSource("VID");
        requestDetails.setModelInfo(modelInfo);
        requestDetails.setRequestInfo(requestInfo);
        sir.setRequestDetails(requestDetails);

        String serviceType = requestHandlerUtils.getServiceType(requestScope, sir, aLaCarteFlag);
        assertEquals(serviceType, "networkModelName");
    }

    @Test
    public void setServiceInstanceIdInstanceGroupTest() throws JsonParseException, JsonMappingException, IOException {
        String requestScope = "instanceGroup";
        ServiceInstancesRequest sir =
                mapper.readValue(inputStream("/CreateInstanceGroup.json"), ServiceInstancesRequest.class);
        assertEquals("ddcbbf3d-f2c1-4ca0-8852-76a807285efc",
                requestHandlerUtils.setServiceInstanceId(requestScope, sir));
    }

    @Test
    public void setServiceInstanceIdTest() {
        String requestScope = "vnf";
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        sir.setServiceInstanceId("f0a35706-efc4-4e27-80ea-a995d7a2a40f");
        assertEquals("f0a35706-efc4-4e27-80ea-a995d7a2a40f",
                requestHandlerUtils.setServiceInstanceId(requestScope, sir));
    }

    @Test
    public void setServiceInstanceIdReturnNullTest() {
        String requestScope = "vnf";
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        assertNull(requestHandlerUtils.setServiceInstanceId(requestScope, sir));
    }

    @Test
    public void camundaHistoryCheckTest() throws ContactCamundaException, RequestDbFailureException {
        wireMockServer.stubFor(get(
                ("/sobpmnengine/history/process-instance?processInstanceBusinessKey=f0a35706-efc4-4e27-80ea-a995d7a2a40f&active=true&maxResults=1"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile("Camunda/HistoryCheckResponse.json")
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        InfraActiveRequests duplicateRecord = new InfraActiveRequests();
        duplicateRecord.setRequestId("f0a35706-efc4-4e27-80ea-a995d7a2a40f");
        boolean inProgress = false;
        inProgress = requestHandlerUtils.camundaHistoryCheck(duplicateRecord, null);
        assertTrue(inProgress);
    }

    @Test
    public void camundaHistoryCheckNoneFoundTest() throws ContactCamundaException, RequestDbFailureException {
        wireMockServer.stubFor(get(
                ("/sobpmnengine/history/process-instance?processInstanceBusinessKey=f0a35706-efc4-4e27-80ea-a995d7a2a40f&active=true&maxResults=1"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody("[]").withStatus(org.apache.http.HttpStatus.SC_OK)));

        InfraActiveRequests duplicateRecord = new InfraActiveRequests();
        duplicateRecord.setRequestId("f0a35706-efc4-4e27-80ea-a995d7a2a40f");
        boolean inProgress = false;
        inProgress = requestHandlerUtils.camundaHistoryCheck(duplicateRecord, null);
        assertFalse(inProgress);
    }

    @Test
    public void handleReplaceInstance_Test() throws JsonParseException, JsonMappingException, IOException {
        String replaceVfModule = inputStream("/ReplaceVfModule.json");
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(replaceVfModule, ServiceInstancesRequest.class);
        Actions action = servInstances.handleReplaceInstance(Action.replaceInstance, sir);
        assertEquals(Action.replaceInstance, action);
    }

    @Test
    public void handleReplaceInstance_retainAssignments_Test()
            throws JsonParseException, JsonMappingException, IOException {
        String replaceVfModule = inputStream("/ReplaceVfModuleRetainAssignments.json");
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest sir = mapper.readValue(replaceVfModule, ServiceInstancesRequest.class);
        Actions action = servInstances.handleReplaceInstance(Action.replaceInstance, sir);
        assertEquals(Action.replaceInstanceRetainAssignments, action);
    }

    @Test
    public void getCloudConfigurationAAIEntityNotFoundTest() throws IOException {
        RequestError expectedResponse =
                mapper.readValue(inputStream("/AAIEntityNotFoundResponse.json"), RequestError.class);
        uri = servInstanceuri + "v7"
                + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000/replace";
        ResponseEntity<String> response =
                sendRequest(inputStream("/ReplaceVfModuleNoCloudConfig.json"), uri, HttpMethod.POST, headers);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertThat(expectedResponse, sameBeanAs(realResponse));
    }

}

