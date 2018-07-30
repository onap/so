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

package org.onap.so.apihandlerinfra.tenantisolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;


public class CloudResourcesOrchestrationTest extends BaseTest{
	
	@Autowired
	private InfraActiveRequestsRepository iarRepo;
	private String requestJSON = "{\"requestDetails\":{\"requestInfo\":{\"source\":\"VID\",\"requestorId\":\"xxxxxx\" } } }";
	private static final String path = "/onap/so/infra/cloudResourcesRequests";
	
	@LocalServerPort
	private int port;



	HttpHeaders headers = new HttpHeaders();

	@Test
	public void testUnlockFailObjectMapping() {
		
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1/test/unlock");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		
		String body = response.getBody();
		assertTrue(body.contains("Mapping of request to JSON object failed."));
		assertEquals(400, response.getStatusCodeValue());
	}
	
	@Test
	public void testParseOrchestrationError1() {
		String requestJSON = "{\"requestDetails\": null }";
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1/test/unlock");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		String body = response.getBody();
		assertTrue(body.contains("No valid requestDetails is specified"));
		assertEquals(400, response.getStatusCodeValue());
	}
	
	@Test
	public void testParseOrchestrationError2() {
		String requestJSON = "{\"requestDetails\":{\"requestInfo\":{\"source\":\"\",\"requestorId\":\"xxxxxx\" } } }";
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1/test/unlock");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		String body = response.getBody();
		assertTrue(body.contains("No valid source is specified"));
		assertEquals(400, response.getStatusCodeValue());
	}
	
	@Test
	public void testParseOrchestrationError3() {
		String requestJSON = "{\"requestDetails\":{\"requestInfo\":{\"source\":\"VID\",\"requestorId\":\"\" } } }";
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1/test/unlock");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		String body = response.getBody();
		assertTrue(body.contains("No valid requestorId is specified"));
		assertEquals(400, response.getStatusCodeValue());
	}
	
	@Test
	public void testGetInfraActiveRequestNull() {
		
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1/request-id-null-check/unlock");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		String body = response.getBody();
		assertTrue(body.contains("Orchestration RequestId request-id-null-check is not found in DB"));
		assertEquals(400, response.getStatusCodeValue());

	}
	
	@Test
	public void testUnlockError() {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestIdtestUnlockError");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("xxxxxx");
		iar.setRequestBody("");
		iar.setRequestStatus("IN_PROGRESS");
		iar.setRequestAction("TEST");
		
		iarRepo.saveAndFlush(iar);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestJSON, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1/requestId/unlock");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		
		assertEquals(400, response.getStatusCodeValue());
	}
	
	@Test
	public void testUnlock() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestIdtestUnlock");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("xxxxxx");
		iar.setRequestBody("{}");
		iar.setRequestStatus("IN_PROGRESS");
		iar.setRequestAction("TEST");
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("23/09/2007");
		long time = date.getTime();
		iar.setStartTime(new Timestamp(time));
		
		iarRepo.saveAndFlush(iar);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestJSON, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1/requestIdtestUnlock/unlock");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		
		assertEquals(204, response.getStatusCodeValue());
	}
	
	@Test
	public void testUnlockComplete() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestIdtestUnlockComplete");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("xxxxxx");
		iar.setRequestBody("{}");
		iar.setRequestStatus("COMPLETE");
		iar.setRequestAction("TEST");
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("23/09/2007");
		long time = date.getTime();
		iar.setStartTime(new Timestamp(time));
		
		iarRepo.saveAndFlush(iar);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(requestJSON, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1/requestIdtestUnlockComplete/unlock");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		String body = response.getBody().toString();
		assertTrue(body.contains("Orchestration RequestId requestIdtestUnlockComplete has a status of COMPLETE and can not be unlocked"));
		assertEquals(400, response.getStatusCodeValue());
	}
	
	@Test
	public void testGetOperationalEnvFilter() {
		
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(null, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1");
		
		builder.queryParam("requestId", "not-there");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		//204s cannot have a body
		//assertTrue(response.getBody().contains("Orchestration RequestId not-there is not found in DB"));
		assertEquals(204, response.getStatusCodeValue());
	}
	
	@Test
	public void testGetOperationalEnvSuccess() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("90c56827-1c78-4827-bc4d-6afcdb37a51f");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("xxxxxx");
		iar.setRequestBody("{}");
		iar.setRequestStatus("COMPLETE");
		iar.setRequestAction("TEST");
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("23/09/2007");
		long time = date.getTime();
		iar.setStartTime(new Timestamp(time));
		
		iarRepo.saveAndFlush(iar);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>("", headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1");
		
		builder.queryParam("requestId", "90c56827-1c78-4827-bc4d-6afcdb37a51f");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(200, response.getStatusCodeValue());
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("1.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("90c56827-1c78-4827-bc4d-6afcdb37a51f", response.getHeaders().get("X-TransactionID").get(0));
	}
	
	@Test
	public void testGetOperationalEnvFilterSuccess() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestIdtestGetOperationalEnvFilterSuccess");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("myVnfOpEnv");
		iar.setRequestorId("xxxxxx");
		iar.setRequestBody("");
		iar.setRequestStatus("COMPLETE");
		iar.setStatusMessage("status Message");
		iar.setProgress(20L);
		iar.setRequestAction("TEST");
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("23/09/2007");
		long time = date.getTime();
		iar.setStartTime(new Timestamp(time));
		iar.setEndTime(new Timestamp(time));
		
		iarRepo.saveAndFlush(iar);

		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(null, headers);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1");
		
		builder.queryParam("requestId", "requestIdtestGetOperationalEnvFilterSuccess");
		builder.queryParam("operationalEnvironmentName", "myVnfOpEnv");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(200, response.getStatusCodeValue());
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("1.0.0", response.getHeaders().get("X-LatestVersion").get(0));

	}
	
	@Test
	public void testGetOperationalEnvFilterException1() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestId-getOpEnvFilterEx1");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("xxxxxx");
		iar.setRequestBody("");
		iar.setRequestStatus("COMPLETE");
		iar.setRequestAction("TEST");
		
		iarRepo.saveAndFlush(iar);

		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>("", headers);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1");
		
		builder.queryParam("filter", "operationalEnvironmentName:EQUALS:myVnfOpEnv");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		assertEquals(500, response.getStatusCodeValue());
	}
	
	@Test
	public void testGetOperationalEnvFilterException2() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestIdFilterException2");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("xxxxxx");
		iar.setRequestBody("");
		iar.setRequestStatus("COMPLETE");
		iar.setRequestAction("TEST");
		
		iarRepo.saveAndFlush(iar);

		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(null, headers);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/v1");
		
		builder.queryParam("operationalEnvironmentName", "");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		
		
		assertEquals(500, response.getStatusCodeValue());
		assertTrue(response.getBody().toString().contains("No valid operationalEnvironmentName value is specified"));
	}

}
