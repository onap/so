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
package org.openecomp.mso.apihandlerinfra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.serviceinstancebeans.GetOrchestrationResponse;
import org.openecomp.mso.serviceinstancebeans.InstanceReferences;
import org.openecomp.mso.serviceinstancebeans.Request;
import org.openecomp.mso.serviceinstancebeans.RequestStatus;
import org.openecomp.mso.serviceinstancebeans.ServiceInstancesRequest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OrchestrationRequestsTest {

	private static final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title></title></head><body></body></html>";
	public static final Response RESPONSE = Response.status(HttpStatus.SC_OK).entity(CHECK_HTML).build();
	@Mock
	private static RequestsDatabase db;
	private static OrchestrationRequests orReq;
	private static GetOrchestrationResponse orRes;

	@Test
	public void testGetOrchestrationRequest() {
		orReq = Mockito.mock(OrchestrationRequests.class);
		orRes = new GetOrchestrationResponse();
		try {
			// create InfraActiveRequests object
			InfraActiveRequests infraRequests = new InfraActiveRequests();
			infraRequests.setRequestId("rq1234d1-5a33-55df-13ab-12abad84e333");
			infraRequests.setNetworkType("CONTRAIL30_BASIC");
			infraRequests.setRequestType("createInstance");
			infraRequests.setSource("VID");
			infraRequests.setTenantId("19123c2924c648eb8e42a3c1f14b7682");
			infraRequests.setServiceInstanceId("bc305d54-75b4-431b-adb2-eb6b9e546014");
			infraRequests.setRequestStatus("IN_PROGRESS");
			infraRequests.setRequestorId("ab1234");
			String body = "{\"modelInfo\":{\"modelInvariantId\":\"9771b085-4705-4bf7-815d-8c0627bb7e36\",\"modelType\":\"service\",\"modelName\":\"Service with VNFs with modules\",\"modelVersion\":\"1.0\"}}";		
			infraRequests.setRequestBody(body);
	
			db = Mockito.mock(RequestsDatabase.class);
			Mockito.when(db.getRequestFromInfraActive(Mockito.anyString())).thenReturn(infraRequests);

			///// mock mapInfraActiveRequestToRequest()
			Request request = new Request();
			request.setRequestId(infraRequests.getRequestId());
			request.setRequestScope(infraRequests.getRequestScope());
			request.setRequestType(infraRequests.getRequestAction());

			InstanceReferences ir = new InstanceReferences();
			if (infraRequests.getNetworkId() != null)
				ir.setNetworkInstanceId(infraRequests.getNetworkId());
			if (infraRequests.getNetworkName() != null)
				ir.setNetworkInstanceName(infraRequests.getNetworkName());
			if (infraRequests.getServiceInstanceId() != null)
				ir.setServiceInstanceId(infraRequests.getServiceInstanceId());
			if (infraRequests.getServiceInstanceName() != null)
				ir.setServiceInstanceName(infraRequests.getServiceInstanceName());
			if (infraRequests.getVfModuleId() != null)
				ir.setVfModuleInstanceId(infraRequests.getVfModuleId());
			if (infraRequests.getVfModuleName() != null)
				ir.setVfModuleInstanceName(infraRequests.getVfModuleName());
			if (infraRequests.getVnfId() != null)
				ir.setVnfInstanceId(infraRequests.getVnfId());
			if (infraRequests.getVnfName() != null)
				ir.setVnfInstanceName(infraRequests.getVnfName());
			if (infraRequests.getVolumeGroupId() != null)
				ir.setVolumeGroupInstanceId(infraRequests.getVolumeGroupId());
			if (infraRequests.getVolumeGroupName() != null)
				ir.setVolumeGroupInstanceName(infraRequests.getVolumeGroupName());
			if (infraRequests.getRequestorId() != null)
				ir.setRequestorId(infraRequests.getRequestorId());

			request.setInstanceReferences(ir);
			RequestStatus status = new RequestStatus();

			if (infraRequests.getRequestStatus() != null) {
				status.setRequestState(infraRequests.getRequestStatus());
			}

			request.setRequestStatus(status);
		//	RequestStatus reqStatus = request.getRequestStatus();	
			orRes.setRequest(request);	
			Mockito.when(orReq.getOrchestrationRequest(Mockito.anyString(), Mockito.anyString())).thenReturn(RESPONSE);
			Response resp = orReq.getOrchestrationRequest("rq1234d1-5a33-55df-13ab-12abad84e333", "v3");
			
			assertEquals(db.getRequestFromInfraActive("rq1234d1-5a33-55df-13ab-12abad84e333").getRequestId(),
					"rq1234d1-5a33-55df-13ab-12abad84e333");
			assertEquals(db.getRequestFromInfraActive("rq1234d1-5a33-55df-13ab-12abad84e333").getSource(), "VID");
			assertEquals(db.getRequestFromInfraActive("rq1234d1-5a33-55df-13ab-12abad84e333").getTenantId(),
					"19123c2924c648eb8e42a3c1f14b7682");
			assertEquals(db.getRequestFromInfraActive("rq1234d1-5a33-55df-13ab-12abad84e333").getServiceInstanceId(),
					"bc305d54-75b4-431b-adb2-eb6b9e546014");
			assertEquals(db.getRequestFromInfraActive("rq1234d1-5a33-55df-13ab-12abad84e333").getRequestStatus(),
					"IN_PROGRESS");
			assertEquals(db.getRequestFromInfraActive("rq1234d1-5a33-55df-13ab-12abad84e333").getRequestorId(),
					"ab1234");
			assertEquals(request.getInstanceReferences().getServiceInstanceId(),"bc305d54-75b4-431b-adb2-eb6b9e546014");
			assertEquals(request.getInstanceReferences().getRequestorId(),"ab1234");
			assertEquals(orRes.getRequest().getRequestId(), "rq1234d1-5a33-55df-13ab-12abad84e333");
			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Test
	public void testGetOrchestrationRequestNotPresent() {
		orReq = Mockito.mock(OrchestrationRequests.class);
		orRes = new GetOrchestrationResponse();
		try {
			// create InfraActiveRequests object
			InfraActiveRequests infraRequests = Mockito.mock(InfraActiveRequests.class);			
			db = Mockito.mock(RequestsDatabase.class);
			Mockito.when(db.getRequestFromInfraActive(Mockito.anyString())).thenReturn(infraRequests);

			Request request = new Request();
			RequestStatus status = new RequestStatus();
			request.setRequestStatus(status);
			orRes.setRequest(request);		
			assertFalse("rq1234d1-5a33-55df-13ab-12abad84e333".equalsIgnoreCase(orRes.getRequest().getRequestId()));
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Test
	public void testUnlockOrchestrationRequest()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		ObjectMapper mapper = new ObjectMapper();
		String requestJSON = " {\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"ab1234\"}}}";
		
		MsoRequest msoRequest = new MsoRequest("rq1234d1-5a33-55df-13ab-12abad84e333");
		ServiceInstancesRequest sir = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		msoRequest.parseOrchestration(sir);

		//create object instead of a DB call.
		InfraActiveRequests infraRequests = new InfraActiveRequests();
		infraRequests.setRequestId("rq1234d1-5a33-55df-13ab-12abad84e333");
		infraRequests.setNetworkType("CONTRAIL30_BASIC");
		infraRequests.setSource("VID");
		infraRequests.setTenantId("19123c2924c648eb8e42a3c1f14b7682");
		infraRequests.setServiceInstanceId("ea4d5374-d28d-4bbf-9691-22985f088b12");
		infraRequests.setRequestStatus("IN-PROGRESS");

		db = Mockito.mock(RequestsDatabase.class);
		Mockito.when(db.getRequestFromInfraActive(Mockito.anyString())).thenReturn(infraRequests);

		Request request = new Request();
		InstanceReferences ir = new InstanceReferences();
		request.setInstanceReferences(ir);
		RequestStatus status = new RequestStatus();

		if (infraRequests.getRequestStatus() != null) {
			status.setRequestState(infraRequests.getRequestStatus());
		}
		request.setRequestStatus(status);
		RequestStatus reqStatus = request.getRequestStatus();
		
		assertEquals(reqStatus.getRequestState(),"IN-PROGRESS");
		
		if (reqStatus.getRequestState().equalsIgnoreCase("IN-PROGRESS")){
			reqStatus.setRequestState(Status.UNLOCKED.toString ());
			}
		assertEquals(reqStatus.getRequestState(),"UNLOCKED");

	}

}
