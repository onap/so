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

package org.openecomp.mso.apihandlerinfra.tenantisolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.MsoException;
import org.openecomp.mso.apihandlerinfra.Status;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Action;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;

public class CloudOrchestrationTest {
	
	@Test
	public void testCreateOpEnvObjectMapperError() throws IOException {
		CloudOrchestration co = new CloudOrchestration();
		Response response = co.createOperationEnvironment(null, null);
		String body = response.getEntity().toString();
		
		assertTrue(body.contains("Mapping of request to JSON object failed."));
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testCreateOpEnvError() throws IOException {
		String request = "{\"requestDetails\":{\"requestInfo\":{\"resourceType\":\"operationalEnvironment\",\"instanceName\": \"myOpEnv\",\"source\": \"VID\",\"requestorId\": \"az2017\"},"
											+ "	\"requestParameters\": {\"tenantContext\": \"Test\",\"workloadContext\": \"ECOMP_E2E-IST\"}}}";
		CloudOrchestration co = new CloudOrchestration();
		Response response = co.createOperationEnvironment(request, null);
		String body = response.getEntity().toString();
		
		assertTrue(body.contains("Error parsing request."));
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testCreateOpEnvReqRecord() throws IOException {
		CloudOrchestration co = new CloudOrchestration();
		String request = IOUtils.toString(ClassLoader.class.getResourceAsStream ("/ECOMPOperationEnvironmentCreate.json"), CharEncoding.UTF_8);
		Response response = co.createOperationEnvironment(request, null);
		assertEquals(500, response.getStatus());
	}
	
	@Test
	public void testCreateOperationalEnvironment() throws IOException {
		CloudOrchestration co = new CloudOrchestration();
		TenantIsolationRequest tenantIsolationRequest = mock(TenantIsolationRequest.class);
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		co.setRequestsDatabase(reqDB);
		co.setTenantIsolationRequest(tenantIsolationRequest);
		String request = IOUtils.toString(ClassLoader.class.getResourceAsStream ("/ECOMPOperationEnvironmentCreate.json"), CharEncoding.UTF_8);
		when(reqDB.checkInstanceNameDuplicate(new HashMap<String, String>(), "myOpEnv", "create")).thenReturn(null);
		doNothing().when(tenantIsolationRequest).createRequestRecord(Status.IN_PROGRESS, Action.create);
		
		Response response = co.createOperationEnvironment(request, null);
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testCreateVNFDuplicateCheck() throws IOException {
		CloudOrchestration co = new CloudOrchestration();
		TenantIsolationRequest tenantIsolationRequest = mock(TenantIsolationRequest.class);
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		Response res = Response.status(409).entity("already has a request being worked with a status of").build();
		
		co.setRequestsDatabase(reqDB);
		co.setTenantIsolationRequest(tenantIsolationRequest);
		String request = IOUtils.toString(ClassLoader.class.getResourceAsStream ("/VNFOperationEnvironmentCreate.json"), CharEncoding.UTF_8);
		when(reqDB.checkInstanceNameDuplicate(null, "myVnfOpEnv", "operationalEnvironment")).thenReturn(new InfraActiveRequests());
		doNothing().when(tenantIsolationRequest).createRequestRecord(Status.FAILED, Action.create);
		when(tenantIsolationRequest.buildServiceErrorResponse(any(Integer.class), any(MsoException.class), any(String.class), any(String.class), any(List.class))).thenReturn(res);
		
		Response response = co.createOperationEnvironment(request, null);
		assertEquals(409, response.getStatus());
	}
	
	@Test
	public void testCreateVNF() throws IOException {
		CloudOrchestration co = new CloudOrchestration();
		TenantIsolationRequest tenantIsolationRequest = mock(TenantIsolationRequest.class);
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		TenantIsolationRunnable thread = mock(TenantIsolationRunnable.class);
		
		co.setRequestsDatabase(reqDB);
		co.setThread(thread);
		co.setTenantIsolationRequest(tenantIsolationRequest);
		String request = IOUtils.toString(ClassLoader.class.getResourceAsStream ("/VNFOperationEnvironmentCreate.json"), CharEncoding.UTF_8);
		when(reqDB.checkInstanceNameDuplicate(null, "myVnfOpEnv", "operationalEnvironment")).thenReturn(null);
		doNothing().when(tenantIsolationRequest).createRequestRecord(Status.IN_PROGRESS, Action.create);
		doNothing().when(thread).run();
		
		Response response = co.createOperationEnvironment(request, null);
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testActivate() throws IOException {
		CloudOrchestration co = new CloudOrchestration();
		TenantIsolationRequest tenantIsolationRequest = mock(TenantIsolationRequest.class);
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		TenantIsolationRunnable thread = mock(TenantIsolationRunnable.class);
		
		co.setRequestsDatabase(reqDB);
		co.setThread(thread);
		co.setTenantIsolationRequest(tenantIsolationRequest);
		String request = IOUtils.toString(ClassLoader.class.getResourceAsStream ("/ActivateOperationEnvironment.json"), CharEncoding.UTF_8);
		when(reqDB.checkInstanceNameDuplicate(null, "myVnfOpEnv", "operationalEnvironment")).thenReturn(null);
		doNothing().when(tenantIsolationRequest).createRequestRecord(Status.IN_PROGRESS, Action.activate);
		doNothing().when(thread).run();
		
		Response response = co.activateOperationEnvironment(request, null, "ff3514e3-5a33-55df-13ab-12abad84e7ff");
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testDeactivate() throws IOException {
		CloudOrchestration co = new CloudOrchestration();
		TenantIsolationRequest tenantIsolationRequest = mock(TenantIsolationRequest.class);
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		TenantIsolationRunnable thread = mock(TenantIsolationRunnable.class);
		
		co.setRequestsDatabase(reqDB);
		co.setThread(thread);
		co.setTenantIsolationRequest(tenantIsolationRequest);
		String request = IOUtils.toString(ClassLoader.class.getResourceAsStream ("/DeactivateOperationEnvironment.json"), CharEncoding.UTF_8);
		when(reqDB.checkInstanceNameDuplicate(null, "myVnfOpEnv", "operationalEnvironment")).thenReturn(null);
		doNothing().when(tenantIsolationRequest).createRequestRecord(Status.IN_PROGRESS, Action.deactivate);
		doNothing().when(thread).run();
		
		Response response = co.activateOperationEnvironment(request, null, "ff3514e3-5a33-55df-13ab-12abad84e7ff");
		assertEquals(200, response.getStatus());
	}
	
	@Ignore // 1802 merge
	@Test
	public void testDeactivateThreadException() throws IOException {
		CloudOrchestration co = new CloudOrchestration();
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
		assertEquals(500, response.getStatus());
	}
	
	@Test
	public void testDeactivateDupCheck() throws IOException {
		CloudOrchestration co = new CloudOrchestration();
		TenantIsolationRequest tenantIsolationRequest = mock(TenantIsolationRequest.class);
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		Response res = Response.status(409).entity("Failed creating a Thread").build();
		
		co.setRequestsDatabase(reqDB);
		co.setTenantIsolationRequest(tenantIsolationRequest);
		String request = IOUtils.toString(ClassLoader.class.getResourceAsStream ("/DeactivateOperationEnvironment.json"), CharEncoding.UTF_8);
		when(reqDB.checkInstanceNameDuplicate(null, "myVnfOpEnv", "operationalEnvironment")).thenReturn(null);
		when(reqDB.checkVnfIdStatus(null)).thenReturn(new InfraActiveRequests());
		when(tenantIsolationRequest.buildServiceErrorResponse(any(Integer.class), any(MsoException.class), any(String.class), any(String.class), any(List.class))).thenReturn(res);

		Response response = co.deactivateOperationEnvironment(request, null, "ff3514e3-5a33-55df-13ab-12abad84e7ff");
		assertEquals(409, response.getStatus());
	}
}
