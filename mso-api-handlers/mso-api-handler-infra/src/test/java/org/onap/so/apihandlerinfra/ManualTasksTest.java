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
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.onap.so.logging.filter.base.Constants.HttpHeaders.ECOMP_REQUEST_ID;
import static org.onap.so.logging.filter.base.Constants.HttpHeaders.ONAP_PARTNER_NAME;
import java.io.IOException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.apihandlerinfra.tasksbeans.RequestDetails;
import org.onap.so.apihandlerinfra.tasksbeans.RequestInfo;
import org.onap.so.apihandlerinfra.tasksbeans.TaskRequestReference;
import org.onap.so.apihandlerinfra.tasksbeans.TasksRequest;
import org.onap.so.apihandlerinfra.tasksbeans.ValidResponses;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Fault;


public class ManualTasksTest extends BaseTest {

    private final String basePath = "/onap/so/infra/tasks/v1/";

    @Test
    public void testCreateOpEnvObjectMapperError() throws IOException {
        TestAppender.events.clear();
        wireMockServer.stubFor(post(urlPathEqualTo("/sobpmnengine/task/55/complete"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));

        String taskId = "55";
        TasksRequest taskReq = new TasksRequest();
        RequestDetails reqDetail = new RequestDetails();
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setRequestorId("testId");
        reqInfo.setSource("testSource");
        reqInfo.setResponseValue(ValidResponses.skip);
        reqDetail.setRequestInfo(reqInfo);
        taskReq.setRequestDetails(reqDetail);

        // expected response
        TaskRequestReference expectedResponse = new TaskRequestReference();
        expectedResponse.setTaskId(taskId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ECOMP_REQUEST_ID, "987654321");
        headers.set(ONAP_PARTNER_NAME, "VID");
        HttpEntity<TasksRequest> entity = new HttpEntity<TasksRequest>(taskReq, headers);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(basePath) + taskId + "/complete");
        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        TaskRequestReference realResponse = mapper.readValue(response.getBody(), TaskRequestReference.class);


        // then
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        assertThat(realResponse, sameBeanAs(expectedResponse));
    }

    @Test
    public void completeTaskMappingError() throws IOException {
        String invalidRequest = "test";
        RequestError expectedResponse = new RequestError();
        ServiceException se = new ServiceException();
        se.setMessageId("SVC0002");
        se.setText(
                "Mapping of request to JSON object failed: Unrecognized token \u0027test\u0027: was expecting (JSON String, Number, Array, Object or token \u0027null\u0027, \u0027true\u0027 or \u0027false\u0027)\n at [Source: (String)\"test\"; line: 1, column: 5]");
        expectedResponse.setServiceException(se);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ECOMP_REQUEST_ID, "987654321");
        headers.set(ONAP_PARTNER_NAME, "VID");
        HttpEntity<String> entity = new HttpEntity<String>(invalidRequest, headers);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(basePath) + "55" + "/complete");
        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        assertThat(realResponse, sameBeanAs(expectedResponse));
    }

    @Test
    public void completeTaskValidationError() throws IOException {
        String taskId = "55";
        TasksRequest taskReq = new TasksRequest();
        RequestDetails reqDetail = new RequestDetails();
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setSource("testSource");
        reqInfo.setResponseValue(ValidResponses.skip);
        reqDetail.setRequestInfo(reqInfo);
        taskReq.setRequestDetails(reqDetail);

        RequestError expectedResponse = new RequestError();
        ServiceException se = new ServiceException();
        se.setMessageId("SVC0002");
        se.setText("Mapping of request to JSON Object failed. No valid requestorId is specified");
        expectedResponse.setServiceException(se);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ECOMP_REQUEST_ID, "987654321");
        headers.set(ONAP_PARTNER_NAME, "VID");
        HttpEntity<TasksRequest> entity = new HttpEntity<TasksRequest>(taskReq, headers);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(basePath) + taskId + "/complete");
        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        assertThat(realResponse, sameBeanAs(expectedResponse));
    }

    @Test
    public void completeTaskBpelResponseError() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/sobpmnengine/task/55/complete")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withFault(Fault.EMPTY_RESPONSE)));

        String taskId = "55";
        TasksRequest taskReq = new TasksRequest();
        RequestDetails reqDetail = new RequestDetails();
        RequestInfo reqInfo = new RequestInfo();
        reqInfo.setRequestorId("testId");
        reqInfo.setSource("testSource");
        reqInfo.setResponseValue(ValidResponses.skip);
        reqDetail.setRequestInfo(reqInfo);
        taskReq.setRequestDetails(reqDetail);

        RequestError expectedResponse = new RequestError();
        ServiceException se = new ServiceException();
        se.setMessageId("SVC1000");
        se.setText("Client from http://localhost:" + env.getProperty("wiremock.server.port")
                + "/sobpmnengine/task/55/complete failed to connect or respond");
        expectedResponse.setServiceException(se);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ECOMP_REQUEST_ID, "987654321");
        headers.set(ONAP_PARTNER_NAME, "VID");
        HttpEntity<TasksRequest> entity = new HttpEntity<TasksRequest>(taskReq, headers);

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(createURLWithPort(basePath) + taskId + "/complete");
        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(Response.Status.BAD_GATEWAY.getStatusCode(), response.getStatusCode().value());
        assertThat(realResponse, sameBeanAs(expectedResponse));
    }
}
