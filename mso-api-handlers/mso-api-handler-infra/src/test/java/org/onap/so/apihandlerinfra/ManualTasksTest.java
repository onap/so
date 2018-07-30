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
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.log4j.MDC;
import org.junit.Test;
import org.onap.so.apihandlerinfra.tasksbeans.RequestDetails;
import org.onap.so.apihandlerinfra.tasksbeans.RequestInfo;
import org.onap.so.apihandlerinfra.tasksbeans.TaskRequestReference;
import org.onap.so.apihandlerinfra.tasksbeans.TasksRequest;
import org.onap.so.apihandlerinfra.tasksbeans.ValidResponses;
import org.onap.so.logger.MsoLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		headers.set(MsoLogger.ECOMP_REQUEST_ID, "987654321");
		headers.set(MsoLogger.CLIENT_ID, "VID");
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
		ILoggingEvent logEvent = TestAppender.events.get(0);
        Map<String,String> mdc = logEvent.getMDCPropertyMap();
        assertEquals("987654321", mdc.get(MsoLogger.REQUEST_ID));
        assertEquals("VID", mdc.get(MsoLogger.CLIENT_ID));        
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("1.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("987654321", response.getHeaders().get("X-TransactionID").get(0));
        MDC.remove(MsoLogger.CLIENT_ID);
		
	}
}
