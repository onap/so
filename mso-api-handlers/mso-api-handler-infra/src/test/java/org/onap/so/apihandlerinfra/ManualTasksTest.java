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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.onap.so.logger.MdcConstants.ECOMP_REQUEST_ID;
import static org.onap.so.logger.MdcConstants.ENDTIME;
import static org.onap.so.logger.MdcConstants.INVOCATION_ID;
import static org.onap.so.logger.MdcConstants.PARTNERNAME;
import static org.onap.so.logger.MdcConstants.RESPONSECODE;
import static org.onap.so.logger.MdcConstants.RESPONSEDESC;
import static org.onap.so.logger.MdcConstants.SERVICE_NAME;
import static org.onap.so.logger.MdcConstants.STATUSCODE;
import static org.onap.so.logger.MdcConstants.CLIENT_ID;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
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

import ch.qos.logback.classic.spi.ILoggingEvent;


public class ManualTasksTest extends BaseTest{

    private final String basePath = "/tasks/v1/";

    @Test
    public void testCreateOpEnvObjectMapperError() throws IOException {
        TestAppender.events.clear();
        stubFor(post(urlPathEqualTo("/sobpmnengine/task/55/complete"))
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

        //expected response
        TaskRequestReference expectedResponse = new TaskRequestReference();
        expectedResponse.setTaskId(taskId);	
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ECOMP_REQUEST_ID, "987654321");
        headers.set(CLIENT_ID, "VID");
        HttpEntity<TasksRequest> entity = new HttpEntity<TasksRequest>(taskReq, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(basePath) + taskId + "/complete");	       
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST, entity, String.class);


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        TaskRequestReference realResponse = mapper.readValue(response.getBody(), TaskRequestReference.class);


        //then		
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());	
        assertThat(realResponse, sameBeanAs(expectedResponse));	
        
        for(ILoggingEvent logEvent : TestAppender.events)
            if(logEvent.getLoggerName().equals("org.onap.so.logging.jaxrs.filter.jersey.JaxRsFilterLogging") &&
            		logEvent.getMarker() != null && logEvent.getMarker().getName().equals("ENTRY")
                    ){
                Map<String,String> mdc = logEvent.getMDCPropertyMap();
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.REQUEST_ID));
                assertNotNull(mdc.get(INVOCATION_ID));
                assertEquals("UNKNOWN",mdc.get(PARTNERNAME));
                assertEquals("tasks/v1/55/complete",mdc.get(SERVICE_NAME));
                assertEquals("INPROGRESS",mdc.get(STATUSCODE));
            }else if(logEvent.getLoggerName().equals("org.onap.so.logging.jaxrs.filter.jersey.JaxRsFilterLogging") &&
            		logEvent.getMarker() != null && logEvent.getMarker().getName().equals("EXIT")){
                Map<String,String> mdc = logEvent.getMDCPropertyMap();
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP));
                assertNotNull(mdc.get(ENDTIME));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.REQUEST_ID));
                assertNotNull(mdc.get(INVOCATION_ID));
                assertEquals("202",mdc.get(RESPONSECODE));
                assertEquals("UNKNOWN",mdc.get(PARTNERNAME));
                assertEquals("tasks/v1/55/complete",mdc.get(SERVICE_NAME));
                assertEquals("COMPLETE",mdc.get(STATUSCODE));
                assertNotNull(mdc.get(RESPONSEDESC));
                assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
                assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
                assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
                assertEquals("1.0.0", response.getHeaders().get("X-LatestVersion").get(0));
            }
    }
    @Test
    public void completeTaskMappingError() throws IOException {
        String invalidRequest = "test";
        RequestError expectedResponse = new RequestError();
        ServiceException se = new ServiceException();
        se.setMessageId("SVC0002");
        se.setText("Mapping of request to JSON object failed: Unrecognized token \'test\': "
        		+ "was expecting \'null\', \'true\', \'false\' or NaN\n at [Source: (String)\"test\"; line: 1, column: 9]");
        expectedResponse.setServiceException(se);
	
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ECOMP_REQUEST_ID, "987654321");
        headers.set(CLIENT_ID, "VID");
        HttpEntity<String> entity = new HttpEntity<String>(invalidRequest, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(basePath) + "55" + "/complete");	       
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
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
        headers.set(CLIENT_ID, "VID");
        HttpEntity<TasksRequest> entity = new HttpEntity<TasksRequest>(taskReq, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(basePath) + taskId + "/complete");	       
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());	
        assertThat(realResponse, sameBeanAs(expectedResponse));
    }
    @Test
    public void completeTaskBpelResponseError() throws IOException {
        stubFor(post(urlPathEqualTo("/sobpmnengine/task/55/complete"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withFault(Fault.EMPTY_RESPONSE)));

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
        se.setText("Request Failed due to BPEL error with HTTP Status = 502");
        expectedResponse.setServiceException(se);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set(ECOMP_REQUEST_ID, "987654321");
        headers.set(CLIENT_ID, "VID");
        HttpEntity<TasksRequest> entity = new HttpEntity<TasksRequest>(taskReq, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(basePath) + taskId + "/complete");	       
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST, entity, String.class);


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertEquals(Response.Status.BAD_GATEWAY.getStatusCode(), response.getStatusCode().value());	
        assertThat(realResponse, sameBeanAs(expectedResponse));
    }
}
