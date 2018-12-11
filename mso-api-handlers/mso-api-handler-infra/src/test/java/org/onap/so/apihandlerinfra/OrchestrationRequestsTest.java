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
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.GetOrchestrationListResponse;
import org.onap.so.serviceinstancebeans.GetOrchestrationResponse;
import org.onap.so.serviceinstancebeans.Request;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.RequestProcessingData;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrchestrationRequestsTest extends BaseTest {
    @Autowired
    private RequestsDbClient requestsDbClient;
    
    @Autowired 
    private OrchestrationRequests orchReq;

    private static final GetOrchestrationListResponse ORCHESTRATION_LIST = generateOrchestrationList();
    private static final String INVALID_REQUEST_ID = "invalid-request-id";

    private static GetOrchestrationListResponse generateOrchestrationList() {
        GetOrchestrationListResponse list = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            list = mapper.readValue(new File("src/test/resources/OrchestrationRequest/OrchestrationList.json"),
                    GetOrchestrationListResponse.class);
        } catch (JsonParseException jpe) {
            jpe.printStackTrace();
        } catch (JsonMappingException jme) {
            jme.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return list;
    }

    @Test
    public void testGetOrchestrationRequest() throws Exception {
        setupTestGetOrchestrationRequest();
        // TEST VALID REQUEST
        GetOrchestrationResponse testResponse = new GetOrchestrationResponse();

        Request request = ORCHESTRATION_LIST.getRequestList().get(1).getRequest();
        testResponse.setRequest(request);
        String testRequestId = request.getRequestId();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + testRequestId));

        ResponseEntity<GetOrchestrationResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
                entity, GetOrchestrationResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(),
                sameBeanAs(testResponse).ignoring("request.startTime").ignoring("request.requestStatus.finishTime")
                .ignoring("request.requestStatus.timeStamp"));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("00032ab7-na18-42e5-965d-8ea592502018", response.getHeaders().get("X-TransactionID").get(0));
    }
    
    @Test
    public void testGetOrchestrationRequestInstanceGroup() throws Exception {
        setupTestGetOrchestrationRequestInstanceGroup();
        // TEST VALID REQUEST
        GetOrchestrationResponse testResponse = new GetOrchestrationResponse();

        Request request = ORCHESTRATION_LIST.getRequestList().get(8).getRequest();
        testResponse.setRequest(request);
        String testRequestId = request.getRequestId();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + testRequestId));

        ResponseEntity<GetOrchestrationResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
                entity, GetOrchestrationResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(),
                sameBeanAs(testResponse).ignoring("request.startTime")
                .ignoring("request.requestStatus.finishTime")
                .ignoring("request.requestStatus.timeStamp"));
    }

    @Test
    public void testGetOrchestrationRequestRequestDetails() throws Exception {
        setupTestGetOrchestrationRequestRequestDetails("00032ab7-3fb3-42e5-965d-8ea592502017", "COMPLETED");
        //Test request with modelInfo request body
        GetOrchestrationResponse testResponse = new GetOrchestrationResponse();

        Request request = ORCHESTRATION_LIST.getRequestList().get(0).getRequest();
        testResponse.setRequest(request);
        String testRequestId = request.getRequestId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + testRequestId));

        ResponseEntity<GetOrchestrationResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
                entity, GetOrchestrationResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(),
                sameBeanAs(testResponse).ignoring("request.startTime")
                .ignoring("request.requestStatus.finishTime")
                .ignoring("request.requestStatus.timeStamp"));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("00032ab7-3fb3-42e5-965d-8ea592502017", response.getHeaders().get("X-TransactionID").get(0));
    }

    @Test
    public void testGetOrchestrationRequestNoRequestID() {
    	HttpHeaders headers = new HttpHeaders();
    	headers.set("Accept", "application/json; charset=UTF-8");
        headers.set("Content-Type", "application/json; charset=UTF-8"); 
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v6/"));

        ResponseEntity<GetOrchestrationListResponse> response = restTemplate.exchange(builder.toUriString(),
                HttpMethod.GET, entity, GetOrchestrationListResponse.class);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testGetOrchestrationRequestFilter() throws Exception {
        setupTestGetOrchestrationRequestFilter();
        List<String> values = new ArrayList<>();
        values.add("EQUALS");
        values.add("vfModule");
        
        ObjectMapper mapper = new ObjectMapper();
        GetOrchestrationListResponse testResponse = mapper.readValue(new File("src/test/resources/OrchestrationRequest/OrchestrationFilterResponse.json"),
                GetOrchestrationListResponse.class);

        Map<String, List<String>> orchestrationMap = new HashMap<>();
        orchestrationMap.put("modelType", values);
        List<GetOrchestrationResponse> testResponses = new ArrayList<>();

        List<InfraActiveRequests> requests = requestsDbClient.getOrchestrationFiltersFromInfraActive(orchestrationMap);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);
        
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v6?filter=modelType:EQUALS:vfModule"));

        ResponseEntity<GetOrchestrationListResponse> response = restTemplate.exchange(builder.toUriString(),
                HttpMethod.GET, entity, GetOrchestrationListResponse.class);
        assertThat(response.getBody(),
                sameBeanAs(testResponse).ignoring("requestList.request.startTime")
                .ignoring("requestList.request.requestStatus.finishTime")
                .ignoring("requestList.request.requestStatus.timeStamp"));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertEquals(requests.size(), response.getBody().getRequestList().size());
        
    }

    @Test
    public void testUnlockOrchestrationRequest() throws Exception {
        setupTestUnlockOrchestrationRequest("0017f68c-eb2d-45bb-b7c7-ec31b37dc349", "UNLOCKED");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        String requestJSON = new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/Request.json")));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);

        UriComponentsBuilder builder;
        ResponseEntity<String> response;
        RequestError expectedRequestError;
        RequestError actualRequestError;
        ServiceException se;

        // Test invalid JSON
        expectedRequestError = new RequestError();
        se = new ServiceException();
        se.setMessageId(ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
        se.setText("Orchestration RequestId 0017f68c-eb2d-45bb-b7c7-ec31b37dc349 has a status of UNLOCKED and can not be unlocked");
        expectedRequestError.setServiceException(se);

        builder = UriComponentsBuilder.fromHttpUrl(
                createURLWithPort("/onap/so/infra/orchestrationRequests/v6/0017f68c-eb2d-45bb-b7c7-ec31b37dc349/unlock"));

        response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
        actualRequestError = mapper.readValue(response.getBody(), RequestError.class);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        assertThat(actualRequestError, sameBeanAs(expectedRequestError));
    }

    @Test
    public void testUnlockOrchestrationRequest_invalid_Json() throws Exception {
        setupTestUnlockOrchestrationRequest_invalid_Json();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        String requestJSON = new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/Request.json")));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);

        UriComponentsBuilder builder;
        ResponseEntity<String> response;
        RequestError expectedRequestError;
        RequestError actualRequestError;
        ServiceException se;

        // Test invalid requestId
        expectedRequestError = new RequestError();
        se = new ServiceException();
        se.setMessageId(ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
        se.setText("Null response from RequestDB when searching by RequestId");
        expectedRequestError.setServiceException(se);

        builder = UriComponentsBuilder.fromHttpUrl(
                createURLWithPort("/onap/so/infra/orchestrationRequests/v6/" + INVALID_REQUEST_ID + "/unlock"));

        response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
        actualRequestError = mapper.readValue(response.getBody(), RequestError.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
        assertThat(actualRequestError, sameBeanAs(expectedRequestError));
    }

    @Test
    public void testUnlockOrchestrationRequest_Valid_Status()
            throws JsonParseException, JsonMappingException, IOException, ValidationException {
        setupTestUnlockOrchestrationRequest_Valid_Status("5ffbabd6-b793-4377-a1ab-082670fbc7ac", "PENDING");
        ObjectMapper mapper = new ObjectMapper();
        String requestJSON = new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/Request.json")));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);

        UriComponentsBuilder builder;
        ResponseEntity<String> response;
        Request request;

        // Test valid status
        request = ORCHESTRATION_LIST.getRequestList().get(1).getRequest();
        builder = UriComponentsBuilder.fromHttpUrl(
                createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + "5ffbabd6-b793-4377-a1ab-082670fbc7ac" + "/unlock"));

        response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
        //Cannot assert anything further here, already have a wiremock in place which ensures that the post was properly called to update.
    }

    @Ignore //What is this testing?
    @Test
    public void testGetOrchestrationRequestRequestDetailsWhiteSpace() throws Exception {
        InfraActiveRequests requests = new InfraActiveRequests();
        requests.setAction("create");
        requests.setRequestBody("  ");
        requests.setRequestId("requestId");
        requests.setRequestScope("service");
        requests.setRequestType("createInstance");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(requests);

        requestsDbClient.save(requests);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/requestId"));

        ResponseEntity<GetOrchestrationResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
                entity, GetOrchestrationResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("requestId", response.getHeaders().get("X-TransactionID").get(0));
    }

    @Ignore //What is this testing?
    @Test
    public void testGetOrchestrationRequestRequestDetailsAlaCarte() throws IOException {
        InfraActiveRequests requests = new InfraActiveRequests();

        String requestJSON = new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/AlaCarteRequest.json")));

        requests.setAction("create");
        requests.setRequestBody(requestJSON);
        requests.setRequestId("requestId");
        requests.setRequestScope("service");
        requests.setRequestType("createInstance");
//        iar.save(requests);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/requestId"));

        ResponseEntity<GetOrchestrationResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
                entity, GetOrchestrationResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("requestId", response.getHeaders().get("X-TransactionID").get(0));
    }
    @Test
    public void mapRequestProcessingDataTest() throws JsonParseException, JsonMappingException, IOException{
    	RequestProcessingData entry = new RequestProcessingData();
    	RequestProcessingData secondEntry = new RequestProcessingData();
    	List<HashMap<String, String>> expectedList = new ArrayList<>();
    	HashMap<String, String> expectedMap = new HashMap<>();
    	List<HashMap<String, String>> secondExpectedList = new ArrayList<>();
    	HashMap<String, String> secondExpectedMap = new HashMap<>();
    	List<RequestProcessingData> expectedDataList = new ArrayList<>();
    	entry.setGroupingId("7d2e8c07-4d10-456d-bddc-37abf38ca714");
    	entry.setTag("pincFabricConfigRequest");
    	expectedMap.put("requestAction", "assign");
    	expectedMap.put("pincFabricId", "testId");
    	expectedList.add(expectedMap);
    	entry.setDataPairs(expectedList);
    	secondEntry.setGroupingId("7d2e8c07-4d10-456d-bddc-37abf38ca715");
    	secondEntry.setTag("pincFabricConfig");
    	secondExpectedMap.put("requestAction", "unassign");
    	secondExpectedList.add(secondExpectedMap);
    	secondEntry.setDataPairs(secondExpectedList);
    	expectedDataList.add(entry);
    	expectedDataList.add(secondEntry);
    	
    	List<org.onap.so.db.request.beans.RequestProcessingData> processingData = new ArrayList<>(); 
    	List<RequestProcessingData> actualProcessingData = new ArrayList<>();
    	ObjectMapper mapper = new ObjectMapper();
        processingData = mapper.readValue(new File("src/test/resources/OrchestrationRequest/RequestProcessingData.json"),
        		new TypeReference<List<org.onap.so.db.request.beans.RequestProcessingData>>(){});
        actualProcessingData = orchReq.mapRequestProcessingData(processingData);
    	assertThat(actualProcessingData,sameBeanAs(expectedDataList));
    }

    public void setupTestGetOrchestrationRequest() throws Exception{
        //For testGetOrchestrationRequest
        stubFor(any(urlPathEqualTo("/infraActiveRequests/00032ab7-na18-42e5-965d-8ea592502018")).willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/getOrchestrationRequest.json"))))
                .withStatus(HttpStatus.SC_OK)));
        stubFor(get(urlPathEqualTo("/requestProcessingData/search/findBySoRequestIdOrderByGroupingIdDesc/"))
        		.withQueryParam("SO_REQUEST_ID", equalTo("00032ab7-na18-42e5-965d-8ea592502018"))
        		.willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/getRequestProcessingData.json"))))
                .withStatus(HttpStatus.SC_OK)));
    }
    public void setupTestGetOrchestrationRequestInstanceGroup() throws Exception{
        //For testGetOrchestrationRequest
        stubFor(any(urlPathEqualTo("/infraActiveRequests/00032ab7-na18-42e5-965d-8ea592502018")).willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/getOrchestrationRequestInstanceGroup.json"))))
                .withStatus(HttpStatus.SC_OK)));
        stubFor(get(urlPathEqualTo("/requestProcessingData/search/findBySoRequestIdOrderByGroupingIdDesc/"))
        		.withQueryParam("SO_REQUEST_ID", equalTo("00032ab7-na18-42e5-965d-8ea592502018"))
        		.willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/getRequestProcessingData.json"))))
                .withStatus(HttpStatus.SC_OK)));
    }

    private void setupTestGetOrchestrationRequestRequestDetails(String requestId, String status) throws Exception{
        stubFor(get(urlPathEqualTo(getTestUrl(requestId))).willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/getOrchestrationRequestDetails.json"))))
                .withStatus(HttpStatus.SC_OK)));
    }

    private void setupTestUnlockOrchestrationRequest(String requestId, String status) {
        stubFor(get(urlPathEqualTo(getTestUrl(requestId))).willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(String.format(getResponseTemplate, requestId, status))
                .withStatus(HttpStatus.SC_OK)));

    }



    private void setupTestUnlockOrchestrationRequest_invalid_Json() {
        stubFor(get(urlPathEqualTo(getTestUrl(INVALID_REQUEST_ID))).willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withStatus(HttpStatus.SC_NOT_FOUND)));

    }

    private void setupTestUnlockOrchestrationRequest_Valid_Status(String requestID, String status) {
        stubFor(get(urlPathEqualTo(getTestUrl(requestID))).willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(String.format(getResponseTemplate, requestID, status))
                .withStatus(HttpStatus.SC_OK)));

        stubFor(post(urlPathEqualTo(getTestUrl(""))).willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(String.format(infraActivePost, requestID))
                .withStatus(HttpStatus.SC_OK)));
    }

    private void setupTestGetOrchestrationRequestFilter() throws Exception{
        //for testGetOrchestrationRequestFilter();
        stubFor(any(urlPathEqualTo("/infraActiveRequests/getOrchestrationFiltersFromInfraActive/")).withRequestBody(equalToJson("{\"modelType\":[\"EQUALS\",\"vfModule\"]}")).willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/getRequestDetailsFilter.json"))))
                .withStatus(HttpStatus.SC_OK)));
    }
}