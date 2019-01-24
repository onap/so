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

package org.onap.so.adapters.network;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeletePublicUrlStackByNameAndID_204;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetPublicUrlStackByNameAndID_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetPublicUrlStackByNameAndID_204;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackAppC_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackCreatedAppC_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackCreatedVUSP_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVUSP_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVfModule_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostPublicUrlWithBodyFile_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPublicUrlStackByID_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPublicUrlStackByName_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutPublicUrlStackByNameAndID_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.junit.Test;
import org.onap.so.adapters.nwrest.CreateNetworkRequest;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.DeleteNetworkRequest;
import org.onap.so.adapters.nwrest.DeleteNetworkResponse;
import org.onap.so.adapters.nwrest.NetworkTechnology;
import org.onap.so.adapters.nwrest.QueryNetworkError;
import org.onap.so.adapters.nwrest.QueryNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkRequest;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.adapters.vnf.BaseRestTestUtils;
import org.onap.so.client.policy.JettisonStyleMapperProvider;
import org.onap.so.entity.MsoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class NetworkAdapterRestTest extends BaseRestTestUtils {

	@Autowired
	private JettisonStyleMapperProvider jettisonTypeObjectMapper;
	private static final String CLOUDSITE_ID = "mtn13";
	private static final String TENANT_ID = "ba38bc24a2ef4fb2ad2810c894f1938f";
	private static final String NETWORK_ID = "da886914-efb2-4917-b335-c8381528d90b";
	private static final String NETWORK_TYPE = "CONTRAIL30_BASIC";
	private static final String MODEL_CUSTOMIZATION_UUID = "3bdbb104-476c-483e-9f8b-c095b3d308ac";
	private static final String MSO_SERVICE_INSTANCE_ID = "05869d5f-47df-4b45-bbfc-4f03ce0a50bf";
	private static final String MSO_REQUEST_ID = "requestId";
	private static final String NETWORK_NAME = "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0";

	@Test
	public void testCreateNetwork() throws JSONException, JsonParseException, JsonMappingException, IOException {
		
		CreateNetworkRequest request = new CreateNetworkRequest();
		request.setBackout(true);
		request.setSkipAAI(true);
		request.setFailIfExists(false);
		MsoRequest msoReq = new MsoRequest();
		String networkTechnology = "CONTRAIL";

		msoReq.setRequestId(MSO_REQUEST_ID);
		msoReq.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
		request.setMsoRequest(msoReq);
		request.setCloudSiteId(CLOUDSITE_ID);
		request.setTenantId(TENANT_ID);
		request.setNetworkId(NETWORK_ID);
		request.setNetworkName(NETWORK_NAME);
		request.setNetworkType(NETWORK_TYPE);
		request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
		request.setNetworkTechnology(networkTechnology);

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackPostPublicUrlWithBodyFile_200();

		mockOpenStackGetStackCreatedVUSP_200();

		mockOpenStackGetStackVUSP_404();

		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<CreateNetworkRequest> entity = new HttpEntity<CreateNetworkRequest>(request, headers);
		ResponseEntity<CreateNetworkResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks"), HttpMethod.POST, entity, CreateNetworkResponse.class);

		CreateNetworkResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
				new File("src/test/resources/__files/CreateNetworkResponse.json"), CreateNetworkResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
	}

	@Test
	public void testCreateNetwork_JSON() throws JSONException, JsonParseException, JsonMappingException, IOException {
		


		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackPostPublicUrlWithBodyFile_200();

		mockOpenStackGetStackCreatedAppC_200();
		
		mockOpenStackGetStackAppC_404();
		
		headers.add("Content-Type", MediaType.APPLICATION_JSON);
		headers.add("Accept", MediaType.APPLICATION_JSON);
		
		String request = readJsonFileAsString("src/test/resources/CreateNetwork.json");
		HttpEntity<String> entity = new HttpEntity<String>(request, headers);

		ResponseEntity<CreateNetworkResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks"), HttpMethod.POST, entity, CreateNetworkResponse.class);
		
		CreateNetworkResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
				new File("src/test/resources/__files/CreateNetworkResponse2.json"), CreateNetworkResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
	}
	

	
	@Test
	public void testDeleteNetwork() throws IOException{
		
		DeleteNetworkRequest request = new DeleteNetworkRequest();
		
		MsoRequest msoReq = new MsoRequest();
		
		msoReq.setRequestId(MSO_REQUEST_ID);
		msoReq.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
		request.setMsoRequest(msoReq);
		request.setCloudSiteId(CLOUDSITE_ID);
		request.setTenantId(TENANT_ID);
		request.setNetworkId(NETWORK_ID);
		request.setNetworkType(NETWORK_TYPE);
		request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
		request.setNetworkStackId(NETWORK_ID);
		
		mockOpenStackResponseAccess(wireMockPort);
		
		mockOpenStackPublicUrlStackByID_200(wireMockPort);
		
		mockOpenStackGetPublicUrlStackByNameAndID_204(wireMockPort);
		
		mockOpenStackDeletePublicUrlStackByNameAndID_204();
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		
		HttpEntity<DeleteNetworkRequest> entity = new HttpEntity<DeleteNetworkRequest>(request, headers);
		
		ResponseEntity<DeleteNetworkResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks/da886914-efb2-4917-b335-c8381528d90b"), HttpMethod.DELETE, entity, DeleteNetworkResponse.class); 
		
		DeleteNetworkResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
				new File("src/test/resources/__files/DeleteNetworkResponse.json"), DeleteNetworkResponse.class);
		
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		
	}

	@Test
	public void testQueryNetwork_Exception() throws IOException{
		MsoRequest msoReq = new MsoRequest();
		msoReq.setRequestId(MSO_REQUEST_ID);
		msoReq.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
		headers.add("Accept", MediaType.APPLICATION_JSON);

		HttpEntity<DeleteNetworkRequest> entity = new HttpEntity<DeleteNetworkRequest>(headers);

		ResponseEntity<QueryNetworkError> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks/da886914-efb2-4917-b335-c8381528d90b"), HttpMethod.GET,
				entity, QueryNetworkError.class);

		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());

	}

	@Test
	public void testQueryNetwork() throws IOException{

		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_200();

		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<DeleteNetworkRequest> entity = new HttpEntity<DeleteNetworkRequest>(headers);

		ResponseEntity<QueryNetworkResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks/da886914-efb2-4917-b335-c8381528d90b"+"?cloudSiteId=" + CLOUDSITE_ID + "&tenantId=" + TENANT_ID
						+ "&aaiNetworkId=aaiNetworkId"), HttpMethod.GET, 
				entity, QueryNetworkResponse.class);

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());

	}
	
	@Test
	public void testUpdateNetwork() throws IOException{
		
		UpdateNetworkRequest request = new UpdateNetworkRequest();
		
		MsoRequest msoReq = new MsoRequest();
		
		msoReq.setRequestId(MSO_REQUEST_ID);
		msoReq.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
		request.setMsoRequest(msoReq);
		request.setCloudSiteId(CLOUDSITE_ID);
		request.setTenantId(TENANT_ID);
		request.setNetworkId(NETWORK_ID);
		request.setNetworkName(NETWORK_NAME);
		request.setNetworkType(NETWORK_TYPE);
		request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
		request.setNetworkStackId(NETWORK_ID);
		
		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackPublicUrlStackByName_200(wireMockPort);
		
		mockOpenStackPublicUrlStackByID_200(wireMockPort);
		
		mockOpenStackGetPublicUrlStackByNameAndID_200(wireMockPort);

		mockOpenStackPutPublicUrlStackByNameAndID_200();

		headers.add("Accept", MediaType.APPLICATION_JSON);
		
		HttpEntity<UpdateNetworkRequest> entity = new HttpEntity<UpdateNetworkRequest>(request, headers);
		
		ResponseEntity<UpdateNetworkResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks/da886914-efb2-4917-b335-c8381528d90b"), HttpMethod.PUT, entity, UpdateNetworkResponse.class); 
		
		UpdateNetworkResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
				new File("src/test/resources/__files/UpdateNetworkResponse.json"), UpdateNetworkResponse.class);
		
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}	
	
	@Test
	public void testCreateNetworkCNRC_JSON() throws JSONException, JsonParseException, JsonMappingException, IOException {
		
		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackPostPublicUrlWithBodyFile_200();

		mockOpenStackGetStackCreatedAppC_200();
		
		mockOpenStackGetStackAppC_404();
		
		headers.add("Content-Type", MediaType.APPLICATION_JSON);
		headers.add("Accept", MediaType.APPLICATION_JSON);
		
		String request = readJsonFileAsString("src/test/resources/CreateNetwork3.json");
		HttpEntity<String> entity = new HttpEntity<String>(request, headers);

		ResponseEntity<CreateNetworkResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks"), HttpMethod.POST, entity, CreateNetworkResponse.class);

		CreateNetworkResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
				new File("src/test/resources/__files/CreateNetworkResponse3.json"), CreateNetworkResponse.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
	}
	
	@Test
	public void testCreateNetworkNC_Shared_JSON() throws JSONException, JsonParseException, JsonMappingException, IOException {
		
		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackPostPublicUrlWithBodyFile_200();

		mockOpenStackGetStackCreatedAppC_200();
		
		mockOpenStackGetStackAppC_404();
		
		headers.add("Content-Type", MediaType.APPLICATION_JSON);
		headers.add("Accept", MediaType.APPLICATION_JSON);
		
		String request = readJsonFileAsString("src/test/resources/CreateNetwork4.json");
		HttpEntity<String> entity = new HttpEntity<String>(request, headers);

		ResponseEntity<CreateNetworkResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks"), HttpMethod.POST, entity, CreateNetworkResponse.class);

		CreateNetworkResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
				new File("src/test/resources/__files/CreateNetworkResponse4.json"), CreateNetworkResponse.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
	}
	
	@Override
	protected String readJsonFileAsString(String fileLocation) throws JsonParseException, JsonMappingException, IOException{
		return new String(Files.readAllBytes(Paths.get(fileLocation)));
	}
}
