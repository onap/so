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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.MDC;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.logger.MsoLogger;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Fault;

import ch.qos.logback.classic.spi.ILoggingEvent;


public class ServiceInstancesTest extends BaseTest{

    @Autowired
    private InfraActiveRequestsRepository iar;

    @Autowired
    private ServiceInstances servInstances;

    private final String servInstanceuri = "/onap/so/infra/serviceInstantiation/";
    private final String servInstanceUriPrev7 = "/onap/so/infra/serviceInstances/";
    private String uri;

    public String inputStream(String JsonInput)throws IOException{
        JsonInput = "src/test/resources/ServiceInstanceTest" + JsonInput;
        String input = new String(Files.readAllBytes(Paths.get(JsonInput)));
        return input;
    }

    public ResponseEntity<String> sendRequest(String requestJson, String uriPath, HttpMethod reqMethod){		 
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type",MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(uriPath));

        HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);  
        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(),
                reqMethod, request, String.class);

        return response;
    }

    @Test
    public void test_mapJSONtoMSOStyle() throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        String testRequest= inputStream("/ServiceInstanceDefault.json");
        String resultString = servInstances.mapJSONtoMSOStyle(testRequest, null, false, null);
        ServiceInstancesRequest sir = mapper.readValue(resultString, ServiceInstancesRequest.class);
        ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
        assertEquals("f7ce78bb-423b-11e7-93f8-0050569a796",modelInfo.getModelCustomizationUuid());
        assertEquals("modelInstanceName",modelInfo.getModelInstanceName());
        assertEquals("f7ce78bb-423b-11e7-93f8-0050569a7965",modelInfo.getModelInvariantUuid());
        assertEquals("10",modelInfo.getModelUuid());

    }
    @Test
    public void createServiceInstanceVIDDefault() throws JsonParseException, JsonMappingException, IOException{
        TestAppender.events.clear();
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        headers.set(MsoLogger.TRANSACTION_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        headers.set(MsoLogger.CLIENT_ID, "VID");
        //expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //then		
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
        ILoggingEvent logEvent = TestAppender.events.get(0);
        Map<String,String> mdc = logEvent.getMDCPropertyMap();
        assertEquals("32807a28-1a14-4b88-b7b3-2950918aa76d", mdc.get(MsoLogger.REQUEST_ID));
        assertEquals("VID", mdc.get(MsoLogger.CLIENT_ID));
        MDC.remove(MsoLogger.CLIENT_ID);
        assertTrue(response.getBody().contains("1882939"));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("5.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("32807a28-1a14-4b88-b7b3-2950918aa76d", response.getHeaders().get("X-TransactionID").get(0));

        //ExpectedRecord
        InfraActiveRequests expectedRecord = new InfraActiveRequests();
        expectedRecord.setRequestStatus("IN_PROGRESS");
        expectedRecord.setRequestBody(inputStream("/ServiceInstanceDefault.json"));
        expectedRecord.setAction("createInstance");
        expectedRecord.setSource("VID");
        expectedRecord.setVnfId("1882938");
        expectedRecord.setLastModifiedBy("APIH");
        expectedRecord.setServiceInstanceId("1882939");
        expectedRecord.setServiceInstanceName("testService9");
        expectedRecord.setRequestScope("service");
        expectedRecord.setRequestorId("xxxxxx");
        expectedRecord.setRequestAction("createInstance");
        expectedRecord.setRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");

        //ActualRecord
        InfraActiveRequests requestRecord = iar.findOneByRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");
        assertEquals(sameBeanAs(expectedRecord).toString(), sameBeanAs(requestRecord).ignoring("startTime").ignoring("modifyTime").toString());

    }
    @Test
    public void createServiceInstanceServiceInstancesUri() throws JsonParseException, JsonMappingException, IOException{
        stubFor(post(urlPathEqualTo("/mso/async/services/CreateGenericALaCarteServiceInstance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceUriPrev7 + "v5";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstancePrev7.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //then		
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void createServiceInstanceBpelStatusError() throws JsonParseException, JsonMappingException, IOException{
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_BAD_GATEWAY)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceStatusError.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }
    @Test
    public void createServiceInstanceBadGateway() throws JsonParseException, JsonMappingException, IOException{
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withStatus(org.apache.http.HttpStatus.SC_BAD_GATEWAY).withBody("{}")));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceBadGateway.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }
    @Test
    public void createServiceInstanceBadData() throws JsonParseException, JsonMappingException, IOException{
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withStatus(org.apache.http.HttpStatus.SC_BAD_GATEWAY).withBody("{I AM REALLY BAD}")));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceBadGateway.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }
    @Test
    public void createServiceInstanceEmptyResponse() throws JsonParseException, JsonMappingException, IOException{
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceEmpty.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_GATEWAY.getStatusCode(), response.getStatusCode().value());
    }
    @Test
    public void activateServiceInstanceNoRecipeALaCarte() throws JsonParseException, JsonMappingException, IOException{
        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/activate";
        headers.set("X-ECOMP-RequestID", "32807a28-1a14-4b88-b7b3-2950918aa76d");
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceALaCarteTrueNoRecipe.json"), uri, HttpMethod.POST);

        //ExpectedRecord
        InfraActiveRequests expectedRecord = new InfraActiveRequests();
        expectedRecord.setRequestStatus("FAILED");
        expectedRecord.setAction("activateInstance");
        expectedRecord.setStatusMessage("Recipe could not be retrieved from catalog DB.");
        expectedRecord.setProgress(new Long(100));
        expectedRecord.setSource("VID");
        expectedRecord.setVnfId("1882938");
        expectedRecord.setRequestBody(inputStream("/ServiceInstanceALaCarteTrueNoRecipe.json"));
        expectedRecord.setLastModifiedBy("APIH");
        expectedRecord.setServiceInstanceId("f7ce78bb-423b-11e7-93f8-0050569a7968");
        expectedRecord.setServiceInstanceName("testService7");
        expectedRecord.setRequestScope("service");
        expectedRecord.setRequestAction("activateInstance");
        expectedRecord.setRequestorId("xxxxxx");
        expectedRecord.setRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");

        //ActualRecord
        InfraActiveRequests requestRecord = iar.findOneByRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");
        assertEquals(sameBeanAs(expectedRecord).toString(), sameBeanAs(requestRecord).ignoring("startTime").ignoring("endTime").ignoring("modifyTime").toString());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
    }
    @Test
    public void activateServiceInstanceNoRecipe() throws JsonParseException, JsonMappingException, IOException{
        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/activate";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceNoRecipe.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
    }
    @Test
    public void activateServiceInstance() throws JsonParseException, JsonMappingException, IOException{
        stubFor(post(urlPathEqualTo("/mso/async/services/ActivateInstance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        headers.set("X-TransactionID", "32807a28-1a14-4b88-b7b3-2950918aa76d");
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/activate";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceActivate.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void deactivateServiceInstance() throws JsonParseException, JsonMappingException, IOException{
        stubFor(post(urlPathEqualTo("/mso/async/services/DeactivateInstance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/deactivate";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceDeactivate.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void deleteServiceInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/DeleteInstance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a8868/";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceDelete.json"), uri, HttpMethod.DELETE);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void assignServiceInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/AssignServiceInstance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/assign";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceAssign.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }

    @Test
    public void unassignServiceInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/UnassignServiceInstance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/unassign";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceUnassign.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void createPortConfiguration() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        headers.set("X-TransactionID", "32807a28-1a14-4b88-b7b3-2950918aa76d");
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstancePortConfiguration.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));		
        assertTrue(response.getBody().contains("1882939"));
    }
    @Test
    public void createPortConfigurationEmptyProductFamilyId() throws JsonParseException, JsonMappingException, IOException {
        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceParseFail.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());	
    }
    @Test
    public void deletePortConfiguration() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        headers.set("X-ECOMP-RequestID", "32807a28-1a14-4b88-b7b3-2950918aa76d");
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations/f7ce78bb-423b-11e7-93f8-0050569a7970";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstance.json"), uri, HttpMethod.DELETE);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));		
    }
    @Test
    public void enablePort() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations/f7ce78bb-423b-11e7-93f8-0050569a7970/enablePort";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceEnablePort.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }
    @Test
    public void disablePort() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations/f7ce78bb-423b-11e7-93f8-0050569a7970/disablePort";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceDisablePort.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }
    @Test
    public void activatePort() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations/f7ce78bb-423b-11e7-93f8-0050569a7970/activate";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceActivatePort.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }
    @Test
    public void deactivatePort() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/configurations/f7ce78bb-423b-11e7-93f8-0050569a7970/deactivate";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceDeactivatePort.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }
    @Test
    public void addRelationships() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/addRelationships";
        ResponseEntity<String> response = sendRequest(inputStream("/AddRelationships.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void removeRelationships() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/ALaCarteOrchestrator"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/removeRelationships";
        ResponseEntity<String> response = sendRequest(inputStream("/RemoveRelationships.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void createVnfInstanceNoALaCarte() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/49585b36-2b5a-443a-8b10-c75d34bb5e46/vnfs";
        ResponseEntity<String> response = sendRequest(inputStream("/VnfCreateDefault.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void createVnfInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        String requestId = "b7a6b76f-2ee2-416c-971b-548472a8c5c3";
        headers.set(MsoLogger.TRANSACTION_ID, requestId);
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vnfs";
        ResponseEntity<String> response = sendRequest(inputStream("/VnfWithServiceRelatedInstance.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        InfraActiveRequests record = iar.findOneByRequestId(requestId);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
        assertTrue(response.getBody().contains("1882939"));
        assertEquals(record.getVnfType(), "vSAMP12/test");
    }
    @Test
    public void createVnfWithServiceRelatedInstanceFail() throws JsonParseException, JsonMappingException, IOException {
        uri = servInstanceUriPrev7 + "v6" + "/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs";
        ResponseEntity<String> response = sendRequest(inputStream("/VnfWithServiceRelatedInstanceFail.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
    }
    @Test
    public void createVnfInstanceInvalidVnfResource() throws JsonParseException, JsonMappingException, IOException {		
        uri = servInstanceuri + "v7" + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vnfs";
        ResponseEntity<String> response = sendRequest(inputStream("/NoVnfResource.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertTrue(realResponse.getServiceException().getText().equals("No valid vnfResource is specified"));
    }
    @Test
    public void replaceVnfInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/replace";
        ResponseEntity<String> response = sendRequest(inputStream("/ReplaceVnf.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void replaceVnfRecreateInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/RecreateInfraVce"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/replace";
        ResponseEntity<String> response = sendRequest(inputStream("/ReplaceVnfRecreate.json"), uri, HttpMethod.POST);
        logger.debug(response.getBody());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void updateVnfInstance() throws JsonParseException, JsonMappingException, IOException {	
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response = sendRequest(inputStream("/UpdateVnf.json"), uri, HttpMethod.PUT);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void applyUpdatedConfig() throws JsonParseException, JsonMappingException, IOException {			
        stubFor(post(urlPathEqualTo("/mso/async/services/VnfConfigUpdate"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        String requestId = "b7a6b76f-2ee2-416c-971b-548472a8c5c5";
        headers.set(MsoLogger.TRANSACTION_ID, requestId);
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/applyUpdatedConfig";
        ResponseEntity<String> response = sendRequest(inputStream("/ApplyUpdatedConfig.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        InfraActiveRequests record = iar.findOneByRequestId(requestId);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
        assertNull(record.getVnfType());
    }
    @Test
    public void deleteVnfInstanceV5() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v5" + "/serviceInstances/e446b97d-9c35-437a-95a2-6b4c542c4507/vnfs/49befbfe-fccb-421d-bb4c-0734a43f5ea0";
        ResponseEntity<String> response = sendRequest(inputStream("/DeleteVnfV5.json"), uri, HttpMethod.DELETE);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));
    }
    @Test
    public void createVfModuleInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/CreateVfModuleInfra"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/7a88cbeb-0ec8-4765-a271-4f9e90c3da7b/vnfs/cbba721b-4803-4df7-9347-307c9a955426/vfModules";
        ResponseEntity<String> response = sendRequest(inputStream("/VfModuleWithRelatedInstances.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
        assertTrue(response.getBody().contains("1882939"));
    }
    @Test
    public void createVfModuleInstanceNoModelCustomization() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v6" + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules";
        ResponseEntity<String> response = sendRequest(inputStream("/VfModuleNoModelCustomization.json"), uri, HttpMethod.POST);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void deleteVfModuleInstanceNoMatchingModelUUD() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v6" + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response = sendRequest(inputStream("/VfModuleNoMatchingModelUUID.json"), uri, HttpMethod.DELETE);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void createVfModuleInstanceNoRecipe() throws JsonParseException, JsonMappingException, IOException {
        uri = servInstanceuri + "v6" + "/serviceInstances/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules";
        ResponseEntity<String> response = sendRequest(inputStream("/VfModuleInvalid.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE,  true);
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertTrue(realResponse.getServiceException().getText().equals("No valid vfModuleCustomization is specified"));
    }
    @Test
    public void replaceVfModuleInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000/replace";
        ResponseEntity<String> response = sendRequest(inputStream("/ReplaceVfModule.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void updateVfModuleInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response = sendRequest(inputStream("/UpdateVfModule.json"), uri, HttpMethod.PUT);
        logger.debug(response.getBody());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void createVfModuleNoModelType() throws JsonParseException, JsonMappingException, IOException{
        headers.set(MsoLogger.TRANSACTION_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        InfraActiveRequests expectedRecord = new InfraActiveRequests();
        expectedRecord.setRequestStatus("FAILED");
        expectedRecord.setAction("createInstance");
        expectedRecord.setStatusMessage("Error parsing request: No valid modelType is specified");
        expectedRecord.setProgress(new Long(100));
        expectedRecord.setSource("VID");
        expectedRecord.setRequestBody(inputStream("/VfModuleNoModelType.json"));
        expectedRecord.setLastModifiedBy("APIH");
        expectedRecord.setVfModuleName("testVfModule2");
        expectedRecord.setVfModuleModelName("serviceModel");
        expectedRecord.setRequestScope("vfModule");
        expectedRecord.setRequestAction("createInstance");
        expectedRecord.setRequestorId("zz9999");
        expectedRecord.setRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");
        //VnfType is not sent in this request, should be blank in db
        expectedRecord.setVnfType("");
        uri = servInstanceuri + "v5/serviceInstances/32807a28-1a14-4b88-b7b3-2950918aa76d/vnfs/32807a28-1a14-4b88-b7b3-2950918aa76d/vfModules";

        ResponseEntity<String> response = sendRequest(inputStream("/VfModuleNoModelType.json"), uri, HttpMethod.POST);
        //ActualRecord
        InfraActiveRequests requestRecord = iar.findOneByRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        assertEquals(sameBeanAs(expectedRecord).toString(), sameBeanAs(requestRecord).ignoring("startTime").ignoring("endTime").ignoring("modifyTime").toString());
        assertNotNull(requestRecord.getStartTime());
        assertNotNull(requestRecord.getEndTime());
    }
    @Test
    public void inPlaceSoftwareUpdate() throws JsonParseException, JsonMappingException, IOException {			
        stubFor(post(urlPathEqualTo("/mso/async/services/VnfInPlaceUpdate"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/inPlaceSoftwareUpdate";
        ResponseEntity<String> response = sendRequest(inputStream("/InPlaceSoftwareUpdate.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }

    @Test
    public void inPlaceSoftwareUpdateDuplicate() throws JsonParseException, JsonMappingException, IOException {			
        stubFor(post(urlPathEqualTo("/mso/async/services/VnfInPlaceUpdate"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        InfraActiveRequests req = new InfraActiveRequests();
        req.setRequestStatus("IN_PROGRESS");
        req.setAction("inPlaceSoftwareUpdate");
        req.setProgress(new Long(10));
        req.setRequestBody(inputStream("/ServiceInstanceALaCarteTrueNoRecipe.json"));
        req.setServiceInstanceId("f7ce78bb-423b-11e7-93f8-0050569a7908");
        req.setVnfId("ff305d54-75b4-431b-adb2-eb6b9e5ff033");
        req.setRequestScope("vnf");
        req.setVnfName("duplicateCheck123");
        req.setRequestAction("inPlaceSoftwareUpdate");
        req.setRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");
        iar.save(req);

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7908/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff033/inPlaceSoftwareUpdate";
        ResponseEntity<String> response = sendRequest(inputStream("/InPlaceSoftwareUpdate2.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatusCode().value());

        InfraActiveRequests newRecord = iar.findOneByRequestBody(inputStream("/InPlaceSoftwareUpdate2.json"));

        assertNotNull(newRecord.getServiceInstanceId());
        assertNotNull(newRecord.getVnfId());

    }

    @Test
    public void deleteVfModuleInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response = sendRequest(inputStream("/DeleteVfModule.json"), uri, HttpMethod.DELETE);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void deactivateAndCloudDeleteVfModuleInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/vfModules/ff305d54-75b4-431b-adb2-eb6b9e5ff000/deactivateAndCloudDelete";
        ResponseEntity<String> response = sendRequest(inputStream("/DeactivateAndCloudDeleteVfModule.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void createVolumeGroupInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/volumeGroups";
        ResponseEntity<String> response = sendRequest(inputStream("/VolumeGroup.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
        assertTrue(response.getBody().contains("1882939"));
    }
    @Test
    public void updateVolumeGroupInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/volumeGroups/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response = sendRequest(inputStream("/UpdateVolumeGroup.json"), uri, HttpMethod.PUT);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void deleteVolumeGroupInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7968/vnfs/ff305d54-75b4-431b-adb2-eb6b9e5ff000/volumeGroups/ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        ResponseEntity<String> response = sendRequest(inputStream("/DeleteVolumeGroup.json"), uri, HttpMethod.DELETE);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void createNetworkInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        String requestId = "b7a6b76f-2ee2-416c-971b-548472a8c5c4";
        headers.set(MsoLogger.TRANSACTION_ID, requestId);
        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks";
        ResponseEntity<String> response = sendRequest(inputStream("/NetworkCreate.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        InfraActiveRequests record = iar.findOneByRequestId(requestId);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
        assertEquals(record.getNetworkType(), "TestNetworkType");
    }
    @Test
    public void updateNetworkInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks/1710966e-097c-4d63-afda-e0d3bb7015fb";
        ResponseEntity<String> response = sendRequest(inputStream("/UpdateNetwork.json"), uri, HttpMethod.PUT);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
        assertTrue(response.getBody().contains("1882939"));
    }
    @Test
    public void deleteNetworkInstance() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks/1710966e-097c-4d63-afda-e0d3bb7015fb";
        ResponseEntity<String> response = sendRequest(inputStream("/NetworkInstance.json"), uri, HttpMethod.DELETE);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void deleteNetworkInstanceNoReqParams() throws JsonParseException, JsonMappingException, IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v6" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks/1710966e-097c-4d63-afda-e0d3bb7015fb";
        ResponseEntity<String> response = sendRequest(inputStream("/NetworkInstanceNoReqParams.json"), uri, HttpMethod.DELETE);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }
    @Test
    public void convertJsonToServiceInstanceRequestFail() throws JsonParseException, JsonMappingException, IOException {
        headers.set(MsoLogger.TRANSACTION_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        //ExpectedRecord
        InfraActiveRequests expectedRecord = new InfraActiveRequests();
        expectedRecord.setRequestStatus("FAILED");
        expectedRecord.setStatusMessage("Error mapping request: ");
        expectedRecord.setProgress(new Long(100));
        expectedRecord.setRequestBody(inputStream("/ConvertRequestFail.json"));
        expectedRecord.setLastModifiedBy("APIH");
        expectedRecord.setRequestScope("network");
        expectedRecord.setRequestAction("deleteInstance");
        expectedRecord.setRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");

        uri = servInstanceuri + "v6" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks/1710966e-097c-4d63-afda-e0d3bb7015fb";
        ResponseEntity<String> response = sendRequest(inputStream("/ConvertRequestFail.json"), uri, HttpMethod.DELETE);

        //ActualRecord
        InfraActiveRequests requestRecord = iar.findOneByRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        assertThat(expectedRecord, sameBeanAs(requestRecord).ignoring("startTime").ignoring("endTime").ignoring("modifyTime").ignoring("statusMessage"));
        assertThat(requestRecord.getStatusMessage(), containsString("Error mapping request: "));
        assertNotNull(requestRecord.getStartTime());
        assertNotNull(requestRecord.getEndTime());
    }
    @Test
    public void convertJsonToServiceInstanceRequestConfigurationFail() throws JsonParseException, JsonMappingException, IOException {
        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/configurations/test/enablePort";
        ResponseEntity<String> response = sendRequest(inputStream("/ConvertRequestFail.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void creatServiceInstanceGRTestApiNoCustomRecipeFound() throws IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v7" + "/serviceInstances";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceMacro.json"), uri, HttpMethod.POST);

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }

    @Test
    public void createNetworkInstanceTestApiUndefinedUsePropertiesDefault() throws IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks";
        ResponseEntity<String> response = sendRequest(inputStream("/NetworkCreateAlternateInstanceName.json"), uri, HttpMethod.POST);

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }

    @Test
    public void createNetworkInstanceTestApiIncorrectUsePropertiesDefault() throws IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks";
        ResponseEntity<String> response = sendRequest(inputStream("/NetworkCreateTestApiIncorrect.json"), uri, HttpMethod.POST);

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void createNetworkInstanceTestApiGrApi() throws IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks";
        ResponseEntity<String> response = sendRequest(inputStream("/NetworkCreateTestApiGrApi.json"), uri, HttpMethod.POST);

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }

    @Test
    public void createNetworkInstanceTestApiVnfApi() throws IOException {
        stubFor(post(urlPathEqualTo("/mso/async/services/CreateNetworkInstance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v7" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7969/networks";
        ResponseEntity<String> response = sendRequest(inputStream("/NetworkCreateTestApiVnfApi.json"), uri, HttpMethod.POST);

        //expected response
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }

    @Test
    public void activateServiceInstanceRequestStatus() throws JsonParseException, JsonMappingException, IOException{
        stubFor(post(urlPathEqualTo("/mso/async/services/ActivateInstance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));
        headers.set("X-ECOMP-RequestID", "32807a28-1a14-4b88-b7b3-2950918aa76d");

        InfraActiveRequests expectedRecord = new InfraActiveRequests();
        expectedRecord.setRequestStatus("FAILED");
        expectedRecord.setAction("activateInstance");
        expectedRecord.setStatusMessage("Recipe could not be retrieved from catalog DB.");
        expectedRecord.setProgress(new Long(100));
        expectedRecord.setSource("VID");
        expectedRecord.setRequestBody(inputStream("/ServiceInstanceALaCarteTrueNoRecipe.json"));
        expectedRecord.setLastModifiedBy("APIH");
        expectedRecord.setServiceInstanceId("f7ce78bb-423b-11e7-93f8-0050569a7999");
        expectedRecord.setServiceInstanceName("testService1234");
        expectedRecord.setRequestScope("service");
        expectedRecord.setRequestAction("activateInstance");
        expectedRecord.setRequestorId("xxxxxx");
        expectedRecord.setRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");

        //expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceuri + "v5" + "/serviceInstances/f7ce78bb-423b-11e7-93f8-0050569a7999/activate";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstancePrev8.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        InfraActiveRequests requestRecord = iar.findOneByRequestId("32807a28-1a14-4b88-b7b3-2950918aa76d");

        //then		
        assertEquals(Status.IN_PROGRESS.name(), requestRecord.getRequestStatus());
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }

    @Test
    public void invalidRequestId() throws IOException {
        String illegalRequestId = "1234";
        headers.set("X-ECOMP-RequestID", illegalRequestId);

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        assertTrue(response.getBody().contains("Request Id " + illegalRequestId + " is not a valid UUID"));
    }
    @Test
    public void invalidBPELResponse() throws IOException{
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponseInvalid2.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals("Request Failed due to BPEL error with HTTP Status = 202{\"instanceId\": \"1882939\"}", realResponse.getServiceException().getText());
    }

    @Test
    public void invalidBPELResponse2() throws IOException{
        stubFor(post(urlPathEqualTo("/mso/async/services/WorkflowActionBB"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponseInvalid.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        uri = servInstanceuri + "v5/serviceInstances";
        ResponseEntity<String> response = sendRequest(inputStream("/ServiceInstanceDefault.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertTrue(realResponse.getServiceException().getText().contains("<aetgt:ErrorMessage>Exception in create execution list 500"));
    }

    @Test
    public void createMacroServiceInstance() throws JsonParseException, JsonMappingException, IOException{
        stubFor(post(urlPathEqualTo("/mso/async/services/CreateMacroServiceNetworkVnf"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_OK)));

        //expect
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        requestReferences.setInstanceId("1882939");
        expectedResponse.setRequestReferences(requestReferences);
        uri = servInstanceUriPrev7 + "v5";
        ResponseEntity<String> response = sendRequest(inputStream("/MacroServiceInstance.json"), uri, HttpMethod.POST);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //then		
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        ServiceInstancesResponse realResponse = mapper.readValue(response.getBody(), ServiceInstancesResponse.class);
        assertThat(realResponse, sameBeanAs(expectedResponse).ignoring("requestReferences.requestId"));	
    }

    @Test
    public void testUserParams() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest request = mapper.readValue(inputStream("/MacroServiceInstance.json"), ServiceInstancesRequest.class);
        RequestParameters requestParameters = request.getRequestDetails().getRequestParameters();
        String userParamsTxt = inputStream("/userParams.txt");

        List<Map<String, Object>> userParams = servInstances.configureUserParams(requestParameters);
        System.out.println(userParams);
        assertTrue(userParams.size() > 0);
        assertTrue(userParams.get(0).containsKey("name"));
        assertTrue(userParams.get(0).containsKey("value"));
        assertTrue(userParamsTxt.replaceAll("\\s+","").equals(userParams.toString().replaceAll("\\s+","")));
    }

    @Test
    public void testConfigureCloudConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest request = mapper.readValue(inputStream("/MacroServiceInstance.json"), ServiceInstancesRequest.class);
        CloudConfiguration cloudConfig = servInstances.configureCloudConfig(request.getRequestDetails().getRequestParameters());

        assertEquals("mdt25b", cloudConfig.getLcpCloudRegionId());
        assertEquals("aefb697db6524ddebfe4915591b0a347", cloudConfig.getTenantId());
    }

    @Test
    public void testMapToLegacyRequest() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ServiceInstancesRequest request = mapper.readValue(inputStream("/MacroServiceInstance.json"), ServiceInstancesRequest.class);
        ServiceInstancesRequest expected = mapper.readValue(inputStream("/LegacyMacroServiceInstance.json"), ServiceInstancesRequest.class);
        servInstances.mapToLegacyRequest(request.getRequestDetails());
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
        assertThat(request, sameBeanAs(expected));
    }
}