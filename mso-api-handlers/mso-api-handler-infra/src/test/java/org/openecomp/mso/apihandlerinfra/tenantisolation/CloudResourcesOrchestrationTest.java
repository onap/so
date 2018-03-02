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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Test;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;

public class CloudResourcesOrchestrationTest {
	
	private String requestJSON = "{\"requestDetails\":{\"requestInfo\":{\"source\":\"VID\",\"requestorId\":\"zz9999\" } } }";

	@Test
	public void testUnlockFailObjectMapping() {
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		Response response = cor.unlockOrchestrationRequest(null, null, null);
		String body = response.getEntity().toString();
		assertTrue(body.contains("Mapping of request to JSON object failed."));
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testUnlockFailObjectMapping2() {
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		Response response = cor.unlockOrchestrationRequest(null, "requestId", null);
		String body = response.getEntity().toString();
		assertTrue(body.contains("Mapping of request to JSON object failed."));
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testParseOrchestrationError1() {
		String requestJSON = "{\"requestDetails\": null }";
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		Response response = cor.unlockOrchestrationRequest(requestJSON, "requestId", null);
		String body = response.getEntity().toString();
		assertTrue(body.contains("Error parsing request."));
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testParseOrchestrationError2() {
		String requestJSON = "{\"requestDetails\":{\"requestInfo\":{\"source\":\"\",\"requestorId\":\"zz9999\" } } }";
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		Response response = cor.unlockOrchestrationRequest(requestJSON, "requestId", null);
		String body = response.getEntity().toString();
		assertTrue(body.contains("Error parsing request."));
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testParseOrchestrationError3() {
		String requestJSON = "{\"requestDetails\":{\"requestInfo\":{\"source\":\"VID\",\"requestorId\":\"\" } } }";
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		Response response = cor.unlockOrchestrationRequest(requestJSON, "requestId", null);
		String body = response.getEntity().toString();
		assertTrue(body.contains("Error parsing request."));
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testGetInfraActiveRequestNull() {
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		cor.setRequestsDB(reqDB);
		when(reqDB.getRequestFromInfraActive("requestId")).thenReturn(null);
		
		Response response = cor.unlockOrchestrationRequest(requestJSON, "requestId", null);
		String body = response.getEntity().toString();
		assertTrue(body.contains("Orchestration RequestId requestId is not found in DB"));
		assertEquals(404, response.getStatus());
	}
	
	@Test
	public void testUnlockError() {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestId");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("ma920e");
		iar.setRequestBody("");
		iar.setRequestStatus("IN_PROGRESS");
		
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		cor.setRequestsDB(reqDB);
		when(reqDB.getRequestFromInfraActive("requestId")).thenReturn(iar);
		when(reqDB.updateInfraStatus("requestId", "UNLOCKED", "APIH")).thenReturn(1);
		
		Response response = cor.unlockOrchestrationRequest(requestJSON, "requestId", null);
		assertEquals(404, response.getStatus());
	}
	
	@Test
	public void testUnlock() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestId");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("ma920e");
		iar.setRequestBody("");
		iar.setRequestStatus("IN_PROGRESS");
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("23/09/2007");
		long time = date.getTime();
		iar.setStartTime(new Timestamp(time));
		
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		cor.setRequestsDB(reqDB);
		when(reqDB.getRequestFromInfraActive("requestId")).thenReturn(iar);
		when(reqDB.updateInfraStatus("requestId", "UNLOCKED", "APIH")).thenReturn(1);
		
		Response response = cor.unlockOrchestrationRequest(requestJSON, "requestId", null);
		assertEquals(204, response.getStatus());
	}
	
	@Test
	public void testUnlockComplete() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestId");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("ma920e");
		iar.setRequestBody("");
		iar.setRequestStatus("COMPLETE");
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("23/09/2007");
		long time = date.getTime();
		iar.setStartTime(new Timestamp(time));
		
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		cor.setRequestsDB(reqDB);
		when(reqDB.getRequestFromInfraActive("requestId")).thenReturn(iar);
		when(reqDB.updateInfraStatus("requestId", "UNLOCKED", "APIH")).thenReturn(1);
		
		Response response = cor.unlockOrchestrationRequest(requestJSON, "requestId", null);
		String body = response.getEntity().toString();
		assertTrue(body.contains("Orchestration RequestId requestId has a status of COMPLETE and can not be unlocked"));
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testGetOperationalEnvFilter() {
		ResteasyUriInfo uriInfo = new ResteasyUriInfo("", "requestId=89c56827-1c78-4827-bc4d-6afcdb37a51f", "");
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		cor.setRequestsDB(reqDB);
		when(reqDB.getRequestFromInfraActive("89c56827-1c78-4827-bc4d-6afcdb37a51f")).thenReturn(null);
		
		Response response = cor.getOperationEnvironmentStatusFilter(uriInfo, null);
		String body = response.getEntity().toString();
		
		assertTrue(body.contains("Orchestration RequestId 89c56827-1c78-4827-bc4d-6afcdb37a51f is not found in DB"));
		assertEquals(204, response.getStatus());
	}
	
	@Test
	public void testGetOperationalEnvFilterException() {
		ResteasyUriInfo uriInfo = new ResteasyUriInfo("", "requestId=89c56827-1c78-4827-bc4d-6afcdb37a51f", "");
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		
		Response response = cor.getOperationEnvironmentStatusFilter(uriInfo, null);
		assertEquals(404, response.getStatus());
	}
	
	@Test
	public void testGetOperationalEnvSuccess() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestId");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("ma920e");
		iar.setRequestBody("");
		iar.setRequestStatus("COMPLETE");
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("23/09/2007");
		long time = date.getTime();
		iar.setStartTime(new Timestamp(time));
		
		ResteasyUriInfo uriInfo = new ResteasyUriInfo("", "requestId=89c56827-1c78-4827-bc4d-6afcdb37a51f", "");
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		cor.setRequestsDB(reqDB);
		when(reqDB.getRequestFromInfraActive("89c56827-1c78-4827-bc4d-6afcdb37a51f")).thenReturn(iar);
		
		Response response = cor.getOperationEnvironmentStatusFilter(uriInfo, null);
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testGetOperationalEnvFilterSuccess() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestId");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("ma920e");
		iar.setRequestBody("");
		iar.setRequestStatus("COMPLETE");
		iar.setStatusMessage("status Message");
		iar.setProgress(20L);
		
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("23/09/2007");
		long time = date.getTime();
		iar.setStartTime(new Timestamp(time));
		iar.setEndTime(new Timestamp(time));
		
		List<InfraActiveRequests> requests = new ArrayList<>();
		requests.add(iar);
		
		Map<String, String> map = new HashMap<>();
		map.put("operationalEnvironmentName", "myVnfOpEnv");
		
		ResteasyUriInfo uriInfo = new ResteasyUriInfo("", "operationalEnvironmentName=myVnfOpEnv&requestorId=test", "");
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		cor.setRequestsDB(reqDB);
		when(reqDB.getCloudOrchestrationFiltersFromInfraActive(map)).thenReturn(requests);
		
		Response response = cor.getOperationEnvironmentStatusFilter(uriInfo, null);
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testGetOperationalEnvFilterException1() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestId");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvironmentId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("ma920e");
		iar.setRequestBody("");
		iar.setRequestStatus("COMPLETE");
		
		List<InfraActiveRequests> requests = new ArrayList<>();
		requests.add(iar);
		
		Map<String, String> map = new HashMap<>();
		map.put("operationalEnvironmentName", "myVnfOpEnv");
		
		ResteasyUriInfo uriInfo = new ResteasyUriInfo("", "filter=operationalEnvironmentName:EQUALS:myVnfOpEnv", "");
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		cor.setRequestsDB(reqDB);
		when(reqDB.getCloudOrchestrationFiltersFromInfraActive(map)).thenReturn(requests);
		
		Response response = cor.getOperationEnvironmentStatusFilter(uriInfo, null);
		assertEquals(500, response.getStatus());
	}
	
	@Test
	public void testGetOperationalEnvFilterException2() throws ParseException {
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("requestId");
		iar.setRequestScope("requestScope");
		iar.setRequestType("requestType");
		iar.setOperationalEnvId("operationalEnvId");
		iar.setOperationalEnvName("operationalEnvName");
		iar.setRequestorId("ma920e");
		iar.setRequestBody("");
		iar.setRequestStatus("COMPLETE");
		
		List<InfraActiveRequests> requests = new ArrayList<>();
		requests.add(iar);
		
		Map<String, String> map = new HashMap<>();
		map.put("operationalEnvironmentName", "myVnfOpEnv");
		
		ResteasyUriInfo uriInfo = new ResteasyUriInfo("", "operationalEnvironmentName=", "");
		CloudResourcesOrchestration cor = new CloudResourcesOrchestration();
		RequestsDatabase reqDB = mock(RequestsDatabase.class);
		cor.setRequestsDB(reqDB);
		when(reqDB.getCloudOrchestrationFiltersFromInfraActive(map)).thenReturn(requests);
		
		Response response = cor.getOperationEnvironmentStatusFilter(uriInfo, null);
		assertEquals(500, response.getStatus());
		assertTrue(response.getEntity().toString().contains("No valid operationalEnvironmentName value is specified"));
	}
}
