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

import java.io.File;
import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.apihandlerinfra.ApiHandlerApplication;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.Status;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Action;
import org.onap.so.apihandlerinfra.tenantisolationbeans.TenantIsolationRequest;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;


public class CloudOrchestrationTest extends BaseTest {
	
	private static final String path = "/onap/so/infra/cloudResources/v1";
	private HttpHeaders headers = new HttpHeaders();

	
	@LocalServerPort
	private int port;
	
	@Autowired 
	private InfraActiveRequestsRepository iarRepo;
	
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
	public void testCreateOpEnvReqRecord() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TenantIsolationRequest request = mapper.readValue(new File("src/test/resources/TenantIsolation/ECOMPOperationEnvironmentCreate.json"), TenantIsolationRequest.class);
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<TenantIsolationRequest> entity = new HttpEntity<TenantIsolationRequest>(request, headers);
		
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("123");
		iar.setOperationalEnvName("myOpEnv");
		iar.setRequestScope("create");
		iar.setRequestStatus("PENDING");
		iar.setRequestAction("UNKNOWN");
		iarRepo.saveAndFlush(iar);
		
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
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		InfraActiveRequests iar = iarRepo.findOneByRequestId("987654321");
		assertEquals(iar.getRequestBody(), mapper.writeValueAsString(request.getRequestDetails()));
		assertEquals(200, response.getStatusCodeValue());
	}
	
	@Test
	public void testCreateVNFDuplicateCheck() throws IOException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestId");
		iar.setOperationalEnvName("myVnfOpEnv");
		iar.setRequestStatus(Status.IN_PROGRESS.toString());
		iar.setAction(Action.create.toString());
		iar.setRequestAction(Action.create.toString());
		iar.setRequestScope("UNKNOWN");
		iarRepo.saveAndFlush(iar);
		
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
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		
		assertEquals(200, response.getStatusCodeValue());
	}
	
	@Test
	public void testDeactivate() throws IOException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("ff3514e3-5a33-55df-13ab-12abad84e7fa");
		iar.setRequestStatus(Status.COMPLETE.toString());
		iar.setRequestAction("UNKNOWN");
		iar.setRequestScope("UNKNOWN");
		iarRepo.saveAndFlush(iar);
		
				
		ObjectMapper mapper = new ObjectMapper();
		TenantIsolationRequest request = mapper.readValue(new File("src/test/resources/TenantIsolation/DeactivateOperationEnvironment.json"), TenantIsolationRequest.class);
		
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<TenantIsolationRequest> entity = new HttpEntity<TenantIsolationRequest>(request, headers);

	
	
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path) + "/operationalEnvironments/ff3514e3-5a33-55df-13ab-12abad84e7fa/deactivate");
		       
		ResponseEntity<String> response = restTemplate.exchange(
				builder.toUriString(),
				HttpMethod.POST, entity, String.class);
		
		assertEquals(200, response.getStatusCodeValue());
	}
	
	@Test
	public void testDeactivateThreadException() throws IOException {
		//need to simulate a 500 error
		/*CloudOrchestration co = new CloudOrchestration();
		TenantIsolationRequest tenantIsolationRequest = mock(TenantIsolationRequest.class);
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		TenantIsolationRunnable thread = mock(TenantIsolationRunnable.class);
		Response res = Response.status(500).entity("Failed creating a Thread").build();
		
		co.setRequestsDatabase(reqDB);
		co.setThread(thread);
		co.setTenantIsolationRequest(tenantIsolationRequest);
		String request = IOUtils.toString(ClassLoader.class.getResourceAsStream ("/DeactivateOperationEnvironment.json"), CharEncoding.UTF_8);
		when(reqDB.checkInstanceNameDuplicate(null, "myVnfOpEnv", "operationalEnvironment")).thenReturn(null);
		doNothing().when(tenantIsolationRequest).createRequestRecord(Status.IN_PROGRESS, Action.deactivate);
		doThrow(Exception.class).when(thread).run();
		when(tenantIsolationRequest.buildServiceErrorResponse(any(Integer.class), any(MsoException.class), any(String.class), any(String.class), any(List.class))).thenReturn(res);

		Response response = co.activateOperationEnvironment(request, null, "ff3514e3-5a33-55df-13ab-12abad84e7ff");
		assertEquals(500, response.getStatus());*/
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
		iarRepo.saveAndFlush(iar);
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