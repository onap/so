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
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.CloudRequestData;
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

    @Before
    public void setup() throws IOException {
        wireMockServer.stubFor(get(urlMatching("/sobpmnengine/history/process-instance.*")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(new String(Files.readAllBytes(
                        Paths.get("src/test/resources/OrchestrationRequest/ProcessInstanceHistoryResponse.json"))))
                .withStatus(org.apache.http.HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(
                ("/sobpmnengine/history/activity-instance?processInstanceId=c2fd4066-a26e-11e9-b144-0242ac14000b&maxResults=1"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(new String(Files.readAllBytes(Paths.get(
                                        "src/test/resources/OrchestrationRequest/ActivityInstanceHistoryResponse.json"))))
                                .withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(
                ("/requestProcessingData/search/findBySoRequestIdAndIsDataInternalOrderByGroupingIdDesc?SO_REQUEST_ID=00032ab7-1a18-42e5-965d-8ea592502018&IS_INTERNAL_DATA=false"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(new String(Files.readAllBytes(Paths.get(
                                        "src/test/resources/OrchestrationRequest/getRequestProcessingDataArray.json"))))
                                .withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(
                ("/requestProcessingData/search/findBySoRequestIdAndIsDataInternalOrderByGroupingIdDesc?SO_REQUEST_ID=00032ab7-3fb3-42e5-965d-8ea592502017&IS_INTERNAL_DATA=false"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(new String(Files.readAllBytes(Paths.get(
                                        "src/test/resources/OrchestrationRequest/getRequestProcessingDataArray.json"))))
                                .withStatus(HttpStatus.SC_OK)));
    }

    @Test
    public void testGetOrchestrationRequest() throws Exception {
        setupTestGetOrchestrationRequest();
        // TEST VALID REQUEST
        GetOrchestrationResponse testResponse = new GetOrchestrationResponse();

        Request request = ORCHESTRATION_LIST.getRequestList().get(1).getRequest();
        testResponse.setRequest(request);
        testResponse.getRequest().setRequestProcessingData(new ArrayList<RequestProcessingData>());
        RequestProcessingData e = new RequestProcessingData();
        e.setGroupingId("7d2e8c07-4d10-456d-bddc-37abf38ca714");
        List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> data1 = new HashMap<String, String>();
        data1.put("requestAction", "assign");
        data.add(data1);
        e.setDataPairs(data);
        testResponse.getRequest().getRequestProcessingData().add(e);
        String testRequestId = request.getRequestId();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ONAPLogConstants.Headers.REQUEST_ID, "1e45215d-b7b3-4c5a-9316-65bdddaf649f");
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + testRequestId));

        ResponseEntity<GetOrchestrationResponse> response =
                restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, entity, GetOrchestrationResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(), sameBeanAs(testResponse).ignoring("request.startTime")
                .ignoring("request.finishTime").ignoring("request.requestStatus.timeStamp"));
        assertNull(response.getBody().getRequest().getInstanceReferences().getRequestorId());
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("1e45215d-b7b3-4c5a-9316-65bdddaf649f", response.getHeaders().get("X-TransactionID").get(0));
        assertNotNull(response.getBody().getRequest().getFinishTime());
    }

    @Test
    public void getOrchestrationRequestSimpleTest() throws Exception {
        setupTestGetOrchestrationRequest();
        // TEST VALID REQUEST
        GetOrchestrationResponse testResponse = new GetOrchestrationResponse();

        Request request = ORCHESTRATION_LIST.getRequestList().get(1).getRequest();
        request.setRequestProcessingData(null);
        testResponse.setRequest(request);

        String testRequestId = request.getRequestId();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ONAPLogConstants.Headers.REQUEST_ID, "e5e3c007-9fe9-4a20-8691-bdd20e14504d");
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + testRequestId))
                .queryParam("format", "simple");

        ResponseEntity<GetOrchestrationResponse> response =
                restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, entity, GetOrchestrationResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(), sameBeanAs(testResponse).ignoring("request.startTime")
                .ignoring("request.finishTime").ignoring("request.requestStatus.timeStamp"));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("e5e3c007-9fe9-4a20-8691-bdd20e14504d", response.getHeaders().get("X-TransactionID").get(0));
        assertNotNull(response.getBody().getRequest().getFinishTime());
    }

    @Test
    public void testGetOrchestrationRequestInstanceGroup() throws Exception {
        setupTestGetOrchestrationRequestInstanceGroup();
        // TEST VALID REQUEST
        GetOrchestrationResponse testResponse = new GetOrchestrationResponse();

        Request request = ORCHESTRATION_LIST.getRequestList().get(8).getRequest();
        testResponse.setRequest(request);
        testResponse.getRequest().setRequestProcessingData(new ArrayList<RequestProcessingData>());
        RequestProcessingData e = new RequestProcessingData();
        e.setGroupingId("7d2e8c07-4d10-456d-bddc-37abf38ca714");
        List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> data1 = new HashMap<String, String>();
        data1.put("requestAction", "assign");
        data.add(data1);
        e.setDataPairs(data);
        testResponse.getRequest().getRequestProcessingData().add(e);
        String testRequestId = request.getRequestId();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + testRequestId));

        ResponseEntity<GetOrchestrationResponse> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, GetOrchestrationResponse.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertThat(response.getBody(), sameBeanAs(testResponse).ignoring("request.startTime")
                .ignoring("request.finishTime").ignoring("request.requestStatus.timeStamp"));
    }

    @Test
    public void testGetOrchestrationRequestWithOpenstackDetails() throws Exception {
        setupTestGetOrchestrationRequestOpenstackDetails("00032ab7-3fb3-42e5-965d-8ea592502017", "COMPLETED");
        // Test request with modelInfo request body
        GetOrchestrationResponse testResponse = new GetOrchestrationResponse();

        Request request = ORCHESTRATION_LIST.getRequestList().get(0).getRequest();
        List<CloudRequestData> cloudRequestData = new ArrayList<>();
        CloudRequestData cloudData = new CloudRequestData();
        cloudData.setCloudIdentifier("heatstackName/123123");
        ObjectMapper mapper = new ObjectMapper();
        Object reqData = mapper.readValue(
                "{\r\n  \"test\": \"00032ab7-3fb3-42e5-965d-8ea592502016\",\r\n  \"test2\": \"deleteInstance\",\r\n  \"test3\": \"COMPLETE\",\r\n  \"test4\": \"Vf Module has been deleted successfully.\",\r\n  \"test5\": 100\r\n}",
                Object.class);
        cloudData.setCloudRequest(reqData);
        cloudRequestData.add(cloudData);
        request.setCloudRequestData(cloudRequestData);
        testResponse.setRequest(request);
        String testRequestId = request.getRequestId();
        testResponse.getRequest().setRequestProcessingData(new ArrayList<RequestProcessingData>());
        RequestProcessingData e = new RequestProcessingData();
        e.setGroupingId("7d2e8c07-4d10-456d-bddc-37abf38ca714");
        List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> data1 = new HashMap<String, String>();
        data1.put("requestAction", "assign");
        data.add(data1);
        e.setDataPairs(data);
        testResponse.getRequest().getRequestProcessingData().add(e);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ONAPLogConstants.Headers.REQUEST_ID, "0321e28d-3dde-4b31-9b28-1e0f07231b93");
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(
                "/onap/so/infra/orchestrationRequests/v7/" + testRequestId + "?includeCloudRequest=true"));

        ResponseEntity<GetOrchestrationResponse> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, GetOrchestrationResponse.class);
        System.out.println("Response :" + response.getBody().toString());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());

        assertThat(response.getBody(), sameBeanAs(testResponse).ignoring("request.startTime")
                .ignoring("request.finishTime").ignoring("request.requestStatus.timeStamp"));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("0321e28d-3dde-4b31-9b28-1e0f07231b93", response.getHeaders().get("X-TransactionID").get(0));
    }

    @Test
    public void testGetOrchestrationRequestNoRequestID() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json; charset=UTF-8");
        headers.set("Content-Type", "application/json; charset=UTF-8");
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v6/"));

        ResponseEntity<GetOrchestrationListResponse> response = restTemplate.exchange(builder.toUriString(),
                HttpMethod.GET, entity, GetOrchestrationListResponse.class);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testGetOrchestrationRequestInvalidRequestID() throws Exception {
        setupTestGetOrchestrationRequest();
        // TEST INVALID REQUESTID
        GetOrchestrationResponse testResponse = new GetOrchestrationResponse();

        Request request = ORCHESTRATION_LIST.getRequestList().get(1).getRequest();
        testResponse.setRequest(request);
        String testRequestId = "00032ab7-pfb3-42e5-965d-8ea592502016";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + testRequestId));

        ResponseEntity<GetOrchestrationResponse> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, GetOrchestrationResponse.class);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void testGetOrchestrationRequestFilter() throws Exception {
        setupTestGetOrchestrationRequestFilter();
        List<String> values = new ArrayList<>();
        values.add("EQUALS");
        values.add("vfModule");

        ObjectMapper mapper = new ObjectMapper();
        GetOrchestrationListResponse testResponse =
                mapper.readValue(new File("src/test/resources/OrchestrationRequest/OrchestrationFilterResponse.json"),
                        GetOrchestrationListResponse.class);

        Map<String, List<String>> orchestrationMap = new HashMap<>();
        orchestrationMap.put("modelType", values);

        List<InfraActiveRequests> requests = requestsDbClient.getOrchestrationFiltersFromInfraActive(orchestrationMap);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                createURLWithPort("/onap/so/infra/orchestrationRequests/v6?filter=modelType:EQUALS:vfModule"));

        ResponseEntity<GetOrchestrationListResponse> response = restTemplate.exchange(builder.toUriString(),
                HttpMethod.GET, entity, GetOrchestrationListResponse.class);
        assertThat(response.getBody(), sameBeanAs(testResponse).ignoring("requestList.request.startTime")
                .ignoring("requestList.request.finishTime").ignoring("requestList.request.requestStatus.timeStamp"));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertEquals(requests.size(), response.getBody().getRequestList().size());

    }

    @Test
    public void testUnlockOrchestrationRequest() throws Exception {
        setupTestUnlockOrchestrationRequest("0017f68c-eb2d-45bb-b7c7-ec31b37dc349", "UNLOCKED");
        ObjectMapper mapper = new ObjectMapper();
        String requestJSON =
                new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/Request.json")));
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
        se.setText(
                "Orchestration RequestId 0017f68c-eb2d-45bb-b7c7-ec31b37dc349 has a status of UNLOCKED and can not be unlocked");
        expectedRequestError.setServiceException(se);

        builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(
                "/onap/so/infra/orchestrationRequests/v6/0017f68c-eb2d-45bb-b7c7-ec31b37dc349/unlock"));

        response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
        actualRequestError = mapper.readValue(response.getBody(), RequestError.class);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        assertThat(actualRequestError, sameBeanAs(expectedRequestError));
    }

    @Test
    public void testUnlockOrchestrationRequest_invalid_Json() throws Exception {
        setupTestUnlockOrchestrationRequest_invalid_Json();
        ObjectMapper mapper = new ObjectMapper();
        String requestJSON =
                new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/Request.json")));
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
        se.setText("Null response from RequestDB when searching by RequestId " + INVALID_REQUEST_ID);
        expectedRequestError.setServiceException(se);

        builder = UriComponentsBuilder.fromHttpUrl(
                createURLWithPort("/onap/so/infra/orchestrationRequests/v6/" + INVALID_REQUEST_ID + "/unlock"));

        response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
        actualRequestError = mapper.readValue(response.getBody(), RequestError.class);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
        assertThat(expectedRequestError, sameBeanAs(actualRequestError));
    }

    @Test
    public void testUnlockOrchestrationRequest_Valid_Status()
            throws JsonParseException, JsonMappingException, IOException, ValidationException {
        setupTestUnlockOrchestrationRequest_Valid_Status("5ffbabd6-b793-4377-a1ab-082670fbc7ac", "PENDING");
        String requestJSON =
                new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/Request.json")));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);

        UriComponentsBuilder builder;
        builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(
                "/onap/so/infra/orchestrationRequests/v7/" + "5ffbabd6-b793-4377-a1ab-082670fbc7ac" + "/unlock"));
        // Cannot assert anything further here, already have a wiremock in place
        // which ensures that the post was
        // properly called to update.
    }

    @Test
    public void mapRequestProcessingDataTest() throws JsonParseException, JsonMappingException, IOException {
        RequestProcessingData entry = new RequestProcessingData();
        RequestProcessingData secondEntry = new RequestProcessingData();
        List<HashMap<String, String>> expectedList = new ArrayList<>();
        HashMap<String, String> expectedMap = new HashMap<>();
        List<HashMap<String, String>> secondExpectedList = new ArrayList<>();
        HashMap<String, String> secondExpectedMap = new HashMap<>();
        List<RequestProcessingData> expectedDataList = new ArrayList<>();
        entry.setGroupingId("7d2e8c07-4d10-456d-bddc-37abf38ca714");
        expectedMap.put("requestAction", "assign");
        expectedMap.put("fabricId", "testId");
        expectedList.add(expectedMap);
        entry.setDataPairs(expectedList);
        secondEntry.setGroupingId("7d2e8c07-4d10-456d-bddc-37abf38ca715");
        secondExpectedMap.put("requestAction", "unassign");
        secondExpectedList.add(secondExpectedMap);
        secondEntry.setDataPairs(secondExpectedList);
        expectedDataList.add(entry);
        expectedDataList.add(secondEntry);

        List<org.onap.so.db.request.beans.RequestProcessingData> processingData = new ArrayList<>();
        List<RequestProcessingData> actualProcessingData = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        processingData =
                mapper.readValue(new File("src/test/resources/OrchestrationRequest/RequestProcessingData.json"),
                        new TypeReference<List<org.onap.so.db.request.beans.RequestProcessingData>>() {});
        actualProcessingData = orchReq.mapRequestProcessingData(processingData);
        assertThat(actualProcessingData, sameBeanAs(expectedDataList));
    }

    @Test
    public void testThatActiveRequestsExceptionsAreMapped() throws Exception {
        String testRequestId = UUID.randomUUID().toString();
        wireMockServer.stubFor(any(urlPathEqualTo("/infraActiveRequests/" + testRequestId))
                .willReturn(aResponse().withStatus(HttpStatus.SC_UNAUTHORIZED)));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ONAPLogConstants.Headers.REQUEST_ID, "1e45215d-b7b3-4c5a-9316-65bdddaf649f");
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + testRequestId));

        ResponseEntity<GetOrchestrationResponse> response =
                restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, entity, GetOrchestrationResponse.class);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusCode().value());
    }

    public void setupTestGetOrchestrationRequest() throws Exception {
        // For testGetOrchestrationRequest
        wireMockServer.stubFor(any(urlPathEqualTo("/infraActiveRequests/00032ab7-1a18-42e5-965d-8ea592502018"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(new String(Files.readAllBytes(
                                Paths.get("src/test/resources/OrchestrationRequest/getOrchestrationRequest.json"))))
                        .withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo(
                "/requestProcessingData/search/findBySoRequestIdAndIsDataInternalOrderByGroupingIdDesc/"))
                        .withQueryParam("SO_REQUEST_ID", equalTo("00032ab7-1a18-42e5-965d-8ea592502018"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(new String(Files.readAllBytes(Paths
                                        .get("src/test/resources/OrchestrationRequest/getRequestProcessingData.json"))))
                                .withStatus(HttpStatus.SC_OK)));
    }

    public void setupTestGetOrchestrationRequestInstanceGroup() throws Exception {
        // For testGetOrchestrationRequest
        wireMockServer.stubFor(any(urlPathEqualTo("/infraActiveRequests/00032ab7-1a18-42e5-965d-8ea592502018"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(new String(Files.readAllBytes(Paths.get(
                                "src/test/resources/OrchestrationRequest/getOrchestrationRequestInstanceGroup.json"))))
                        .withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo(
                "/requestProcessingData/search/findBySoRequestIdAndIsDataInternalOrderByGroupingIdDesc/"))
                        .withQueryParam("SO_REQUEST_ID", equalTo("00032ab7-1a18-42e5-965d-8ea592502018"))
                        .withQueryParam("IS_INTERNAL_DATA", equalTo("false"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(new String(Files.readAllBytes(Paths
                                        .get("src/test/resources/OrchestrationRequest/getRequestProcessingData.json"))))
                                .withStatus(HttpStatus.SC_OK)));
    }

    private void setupTestGetOrchestrationRequestRequestDetails(String requestId, String status) throws Exception {
        wireMockServer.stubFor(get(urlPathEqualTo(getTestUrl(requestId))).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(new String(Files.readAllBytes(
                        Paths.get("src/test/resources/OrchestrationRequest/getOrchestrationRequestDetails.json"))))
                .withStatus(HttpStatus.SC_OK)));
    }

    private void setupTestGetOrchestrationRequestOpenstackDetails(String requestId, String status) throws Exception {
        wireMockServer.stubFor(get(urlPathEqualTo(getTestUrl(requestId))).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .withBody(new String(Files.readAllBytes(Paths
                        .get("src/test/resources/OrchestrationRequest/getOrchestrationOpenstackRequestDetails.json"))))
                .withStatus(HttpStatus.SC_OK)));
    }

    private void setupTestUnlockOrchestrationRequest(String requestId, String status) {
        wireMockServer.stubFor(get(urlPathEqualTo(getTestUrl(requestId)))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(String.format(getResponseTemplate, requestId, status)).withStatus(HttpStatus.SC_OK)));
    }

    private void setupTestUnlockOrchestrationRequest_invalid_Json() {
        wireMockServer.stubFor(get(urlPathEqualTo(getTestUrl(INVALID_REQUEST_ID))).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_NOT_FOUND)));

    }

    private void setupTestUnlockOrchestrationRequest_Valid_Status(String requestID, String status) {
        wireMockServer.stubFor(get(urlPathEqualTo(getTestUrl(requestID)))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(String.format(getResponseTemplate, requestID, status)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo(getTestUrl("")))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(String.format(infraActivePost, requestID)).withStatus(HttpStatus.SC_OK)));
    }

    private void setupTestGetOrchestrationRequestFilter() throws Exception {
        // for testGetOrchestrationRequestFilter();
        wireMockServer.stubFor(any(urlPathEqualTo("/infraActiveRequests/getOrchestrationFiltersFromInfraActive/"))
                .withRequestBody(equalToJson("{\"modelType\":[\"EQUALS\",\"vfModule\"]}"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(new String(Files.readAllBytes(
                                Paths.get("src/test/resources/OrchestrationRequest/getRequestDetailsFilter.json"))))
                        .withStatus(HttpStatus.SC_OK)));
    }
}
