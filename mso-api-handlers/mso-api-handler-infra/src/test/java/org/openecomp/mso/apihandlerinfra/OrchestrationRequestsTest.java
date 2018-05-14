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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import mockit.MockUp;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.spi.ResteasyUriInfo;
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
//			Mockito.when(orReq.getOrchestrationRequest(Mockito.anyString(), Mockito.anyString())).thenReturn(RESPONSE);
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
//			assertEquals(resp.getStatus(), HttpStatus.SC_OK);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Test
	public void testGetOrchestrationRequestNotPresent() {
		String requestJSON = " {\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"ab1234\"}}}";
		try {

			InfraActiveRequests infraRequests = new InfraActiveRequests();
			infraRequests.setRequestId("rq1234d1-5a33-55df-13ab-12abad84e333");
			infraRequests.setNetworkType("CONTRAIL30_BASIC");
			infraRequests.setSource("VID");
			infraRequests.setTenantId("19123c2924c648eb8e42a3c1f14b7682");
			infraRequests.setServiceInstanceId("ea4d5374-d28d-4bbf-9691-22985f088b12");
			infraRequests.setRequestStatus(Status.IN_PROGRESS.name());
			infraRequests.setStartTime(Timestamp.valueOf(LocalDateTime.now()));
			final List<InfraActiveRequests> infraActiveRequests = Collections.singletonList(infraRequests);

			// create InfraActiveRequests object
			final MockUp<RequestsDatabase> mockUpRDB = new MockUp<RequestsDatabase>() {
				@mockit.Mock
				public InfraActiveRequests getRequestFromInfraActive(String requestId) {
					return infraRequests;
				}

				@mockit.Mock
				public List<InfraActiveRequests> getOrchestrationFiltersFromInfraActive(Map<String, List<String>> orchestrationMap) {
					return infraActiveRequests;
				}

				@mockit.Mock
				public int updateInfraStatus(String requestId, String requestStatus, String lastModifiedBy) {
					return 1;
				}
			};

			Response response = null;
			try {
				OrchestrationRequests requests = new OrchestrationRequests();
				final ResteasyUriInfo ui = new ResteasyUriInfo(new URI("", "", "", "filter=service-instance-id:EQUALS:abc", ""));
				response = requests.getOrchestrationRequest(ui,"v5");
			} finally {
				mockUpRDB.tearDown();
			}
			assertEquals(HttpStatus.SC_OK, response.getStatus());
			assertNotNull(response.getEntity());


		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Test
	public void testUnlockOrchestrationRequest()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		ObjectMapper mapper = new ObjectMapper();
		String requestJSON = " {\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"ab1234\"}}}";
		//String requestJSON = "{\"requestDetails\":{\"requestInfo\":{\"instanceName\":\"Vfmodule_vLB-0514-1\",\"source\":\"VID\",\"suppressRollback\":false,\"requestorId\":\"demo\"},\"modelInfo\":{\"modelType\":\"vfModule\",\"modelInvariantId\":\"80d62376-2d6d-4618-b666-bf00d0e58296\",\"modelVersionId\":\"578b52e5-4572-444d-8de7-2c140ec2e6e5\",\"modelName\":\"Vloadbalancer..base_vlb..module-0\",\"modelVersion\":\"1\",\"modelCustomizationId\":\"bf87db73-2854-4cd1-adfd-8cd08e12befe\",\"modelCustomizationName\":\"Vloadbalancer..base_vlb..module-0\"},\"requestParameters\":{\"usePreload\":true},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"RegionOne\",\"tenantId\":\"ebb0ea7144004bacac1e39ff23105fa7\"},\"relatedInstanceList\":[{\"relatedInstance\":{\"instanceId\":\"60e28eb9-2808-4a5a-830f-ec982f01dcfe\",\"modelInfo\":{\"modelType\":\"service\",\"modelName\":\"vLoadBalancer\",\"modelInvariantId\":\"3f95e3ed-394d-4301-8c9b-c5f39ff89cfd\",\"modelVersion\":\"1.0\",\"modelVersionId\":\"da1b5347-7bcb-4cc4-8c29-d18dafdb1a47\"}}},{\"relatedInstance\":{\"instanceId\":\"338811a1-f7cd-4093-9903-d0f69b7cb176\",\"modelInfo\":{\"modelType\":\"vnf\",\"modelName\":\"vLoadBalancer\",\"modelInvariantId\":\"040740de-1ce8-4737-ad39-970684b0e3e8\",\"modelVersion\":\"1.0\",\"modelVersionId\":\"5fd1ce25-c414-4baf-903b-5042a60cfb02\",\"modelCustomizationId\":\"5801ace5-7cc7-4011-b677-165a0e8a2a27\",\"modelCustomizationName\":\"vLoadBalancer 0\"}}}]}}";
		
		MsoRequest msoRequest = new MsoRequest("rq1234d1-5a33-55df-13ab-12abad84e333");
		ServiceInstancesRequest sir = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		msoRequest.parseOrchestration(sir);

		//create object instead of a DB call.


		final MockUp<RequestsDatabase> mockUp = new MockUp<RequestsDatabase>() {
			@mockit.Mock
			public InfraActiveRequests getRequestFromInfraActive(String requestId) {
				InfraActiveRequests infraRequests = new InfraActiveRequests();
				infraRequests.setRequestId("rq1234d1-5a33-55df-13ab-12abad84e333");
				infraRequests.setNetworkType("CONTRAIL30_BASIC");
				infraRequests.setSource("VID");
				infraRequests.setTenantId("19123c2924c648eb8e42a3c1f14b7682");
				infraRequests.setServiceInstanceId("ea4d5374-d28d-4bbf-9691-22985f088b12");
				infraRequests.setRequestStatus(Status.IN_PROGRESS.name());
				infraRequests.setStartTime(Timestamp.valueOf(LocalDateTime.now()));
				infraRequests.setRequestBody(requestJSON);
				return infraRequests;
			}

			@mockit.Mock
			public int updateInfraStatus(String requestId, String requestStatus, String lastModifiedBy) {
				return 1;
			}
		};

		final Response response;
		try {
			OrchestrationRequests requests = new OrchestrationRequests();
			response = requests.unlockOrchestrationRequest(requestJSON, "rq1234d1-5a33-55df-13ab-12abad84e333", "v5");
		} finally {
			mockUp.tearDown();
		}

		assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
		assertEquals("", response.getEntity().toString());
	}

}
