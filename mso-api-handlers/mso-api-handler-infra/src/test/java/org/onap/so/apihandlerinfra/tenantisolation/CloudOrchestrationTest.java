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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.Status;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Action;
import org.onap.so.apihandlerinfra.tenantisolationbeans.TenantIsolationRequest;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class CloudOrchestrationTest extends BaseTest {
	
	private static final String path = "/onap/so/infra/cloudResources/v1";

	private HttpHeaders headers = new HttpHeaders();

	@Before
	public void setupTestClass() throws Exception{
		stubFor(post(urlPathEqualTo(getTestUrl(""))).willReturn(aResponse().withHeader(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_CREATED)));
	}

	@Test
	public void testCreateOpEnvObjectMapperError() throws IOException {
		
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/operationalEnvironments");
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);

		String body = response.getBody().toString();
		assertTrue(body.contains("Mapping of request to JSON object failed."));
		assertEquals(400, response.getStatusCodeValue());
	}
	
	@Test
	public void testCreateOpEnvError() throws IOException {
		
		String request = "{\"requestDetails\":{\"requestInfo\":{\"resourceType\":\"operationalEnvironment\",\"instanceName\": \"myOpEnv\",\"source\": \"VID\",\"requestorId\": \"xxxxxx\"},"
											+ "	\"requestParameters\": {\"tenantContext\": \"Test\",\"workloadContext\": \"ECOMP_E2E-IST\"}}}";
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(request, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/operationalEnvironments");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);

		String body = response.getBody().toString();
		assertTrue(body.contains("Mapping of request to JSON object failed"));
		assertEquals(400, response.getStatusCodeValue());
	}
	
	@Test
	public void testCreateOpEnvReqRecordDuplicateCheck() throws IOException {
		stubFor(post(urlPathEqualTo(getTestUrl("checkInstanceNameDuplicate"))).withRequestBody(equalTo("{\"instanceIdMap\":null,\"instanceName\":\"myOpEnv\",\"requestScope\":\"operationalEnvironment\"}")).willReturn(aResponse().withHeader(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.withBody(String.format(getResponseTemplate, "123", "PENDING"))
				.withStatus(HttpStatus.SC_OK)));
		ObjectMapper mapper = new ObjectMapper();
		TenantIsolationRequest request = mapper.readValue(new File("src/test/resources/TenantIsolation/ECOMPOperationEnvironmentCreate.json"), TenantIsolationRequest.class);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<TenantIsolationRequest> entity = new HttpEntity<TenantIsolationRequest>(request, headers);


		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/operationalEnvironments");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		
		assertEquals(409, response.getStatusCodeValue());
	}
	
	@Test
	public void testCreateOperationalEnvironment() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TenantIsolationRequest request = mapper.readValue(new File("src/test/resources/TenantIsolation/ECOMPOperationEnvironmentCreate.json"), TenantIsolationRequest.class);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		headers.set("X-TransactionID", "987654321");
		HttpEntity<TenantIsolationRequest> entity = new HttpEntity<TenantIsolationRequest>(request, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/operationalEnvironments");
		stubFor(post(urlPathEqualTo(getTestUrl("checkInstanceNameDuplicate"))).withRequestBody(equalTo("{\"instanceIdMap\":null,\"instanceName\":\"myOpEnv\",\"requestScope\":\"operationalEnvironment\"}")).willReturn(aResponse().withHeader(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.withStatus(HttpStatus.SC_NOT_FOUND)));
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		assertEquals(200, response.getStatusCodeValue());
	}
	
	@Test
	public void testCreateVNFDuplicateCheck() throws IOException {
		stubFor(post(urlPathEqualTo(getTestUrl("checkInstanceNameDuplicate"))).withRequestBody(equalTo("{\"instanceIdMap\":null,\"instanceName\":\"myVnfOpEnv\",\"requestScope\":\"operationalEnvironment\"}")).willReturn(aResponse().withHeader(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.withBody(String.format(getResponseTemplate, "requestId", Status.IN_PROGRESS.toString()))
				.withStatus(HttpStatus.SC_OK)));
		ObjectMapper mapper = new ObjectMapper();
		TenantIsolationRequest request = mapper.readValue(new File("src/test/resources/TenantIsolation/VNFOperationEnvironmentCreate.json"), TenantIsolationRequest.class);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<TenantIsolationRequest> entity = new HttpEntity<TenantIsolationRequest>(request, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/operationalEnvironments");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		assertEquals(409, response.getStatusCodeValue());
	}
	
	@Test
	public void testCreateVNF() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TenantIsolationRequest request = mapper.readValue(new File("src/test/resources/TenantIsolation/VNFOperationEnvironmentCreate.json"), TenantIsolationRequest.class);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<TenantIsolationRequest> entity = new HttpEntity<TenantIsolationRequest>(request, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/operationalEnvironments");
		stubFor(post(urlPathEqualTo(getTestUrl("checkInstanceNameDuplicate"))).withRequestBody(equalTo("{\"instanceIdMap\":null,\"instanceName\":\"myVnfOpEnv\",\"requestScope\":\"operationalEnvironment\"}")).willReturn(aResponse().withHeader(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.withStatus(HttpStatus.SC_NOT_FOUND)));

		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		assertEquals(200, response.getStatusCodeValue());
	}
	
	@Test
	public void testActivate() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TenantIsolationRequest request = mapper.readValue(new File("src/test/resources/TenantIsolation/ActivateOperationEnvironment.json"), TenantIsolationRequest.class);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<TenantIsolationRequest> entity = new HttpEntity<TenantIsolationRequest>(request, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/operationalEnvironments/ff3514e3-5a33-55df-13ab-12abad84e7ff/activate");
		stubFor(post(urlPathEqualTo(getTestUrl("checkInstanceNameDuplicate"))).withRequestBody(equalTo("{\"instanceIdMap\":{\"operationalEnvironmentId\":\"ff3514e3-5a33-55df-13ab-12abad84e7ff\"},\"instanceName\":\"myVnfOpEnv\",\"requestScope\":\"operationalEnvironment\"}")).willReturn(aResponse().withHeader(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.withStatus(HttpStatus.SC_NOT_FOUND)));

		stubFor(get(urlPathEqualTo(getTestUrl("checkVnfIdStatus/ff3514e3-5a33-55df-13ab-12abad84e7ff"))).willReturn(aResponse().withHeader(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.withStatus(HttpStatus.SC_OK)));

		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		
		assertEquals(200, response.getStatusCodeValue());
	}
	
	@Test
	public void testDeactivate() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TenantIsolationRequest request = mapper.readValue(new File("src/test/resources/TenantIsolation/DeactivateOperationEnvironment.json"), TenantIsolationRequest.class);
		
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<TenantIsolationRequest> entity = new HttpEntity<TenantIsolationRequest>(request, headers);


//		stubFor(post(urlPathEqualTo(getTestUrl("checkInstanceNameDuplicate"))).withRequestBody(equalTo("{\"instanceIdMap\":{\"operationalEnvironmentId\":\"ff3514e3-5a33-55df-13ab-12abad84e7fa\"},\"instanceName\":null,\"requestScope\":\"operationalEnvironment\"}")).willReturn(aResponse().withHeader(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
//				.withBodyFile((String.format(getResponseTemplate, "ff3514e3-5a33-55df-13ab-12abad84e7fa", Status.COMPLETE.toString()))).withStatus(HttpStatus.SC_OK)));

		stubFor(get(urlPathEqualTo(getTestUrl("checkVnfIdStatus/ff3514e3-5a33-55df-13ab-12abad84e7fa"))).willReturn(aResponse().withHeader(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.withStatus(HttpStatus.SC_OK)));

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/operationalEnvironments/ff3514e3-5a33-55df-13ab-12abad84e7fa/deactivate");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		
		assertEquals(200, response.getStatusCodeValue());
	}
	

	@Test
	@Ignore
	public void testDeactivateDupCheck() throws IOException {
		
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestId");
		iar.setOperationalEnvName("myVnfOpEnv");
		iar.setRequestStatus(Status.IN_PROGRESS.toString());
		iar.setAction(Action.create.toString());
		iar.setRequestAction(Action.create.toString());
		iar.setRequestScope("UNKNOWN");
		//iarRepo.saveAndFlush(iar);
		ObjectMapper mapper = new ObjectMapper();
		String request = mapper.readValue(new File("src/test/resources/TenantIsolation/DeactivateOperationEnvironment.json"), String.class);
		
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(request, headers);
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/operationalEnvironments/ff3514e3-5a33-55df-13ab-12abad84e7ff/deactivate");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		assertEquals(409, response.getStatusCodeValue());
	}
	
}