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

package org.onap.so.adapters.tenant;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeleteTenantById_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetMetadata_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetRoles_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetTenantById_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetTenantById_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetTenantByName_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetTenantByName_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetUser_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostMetadata_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostTenantWithBodyFile_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostTenant_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutRolesAdmin_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccessAdmin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.adapters.tenantrest.CreateTenantRequest;
import org.onap.so.adapters.tenantrest.CreateTenantResponse;
import org.onap.so.adapters.tenantrest.DeleteTenantRequest;
import org.onap.so.adapters.tenantrest.DeleteTenantResponse;
import org.onap.so.adapters.vnf.BaseRestTestUtils;
import org.onap.so.client.policy.JettisonStyleMapperProvider;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.entity.MsoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class TenantAdapterRestTest extends BaseRestTestUtils {

	@Autowired
	private CloudConfig cloudConfig;
	@Autowired
	private JettisonStyleMapperProvider jettisonTypeObjectMapper;
	
	@Test
	public void testCreateTenantCreated() throws JsonParseException, JsonMappingException, IOException {
		
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");
		CreateTenantRequest request = new CreateTenantRequest();
		String cloudSiteId = "mtn13";
		String requestId = "62265093-277d-4388-9ba6-449838ade586";
		String serviceInstanceId = "4147e06f-1b89-49c5-b21f-4faf8dc9805a";
		String tenantName = "testingTenantName";
		boolean failIfExists = true;
		boolean backout = true;
		Map<String, String> metaData = new HashMap<>();
		metaData.put("key1", "value2");
		MsoRequest msoReq = new MsoRequest();
		msoReq.setRequestId(requestId);
		msoReq.setServiceInstanceId(serviceInstanceId);
		
		request.setCloudSiteId(cloudSiteId);
		request.setMsoRequest(msoReq);
		request.setTenantName(tenantName);
		request.setMetadata(metaData);
		request.setBackout(backout);
		request.setFailIfExists(failIfExists);
		
		mockOpenStackResponseAccessAdmin(wireMockPort);
		
		mockOpenStackGetTenantByName_404(tenantName);
		
		mockOpenStackPostTenantWithBodyFile_200();
		
		mockOpenStackGetUser_200("m93945");
		
		mockOpenStackGetRoles_200("OS-KSADM");
		
		mockOpenStackPutRolesAdmin_200("OS-KSADM");
		
		mockOpenStackPostMetadata_200();
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<CreateTenantRequest> entity = new HttpEntity<CreateTenantRequest>(request, headers);

		ResponseEntity<CreateTenantResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/tenants"), HttpMethod.POST,
				entity, CreateTenantResponse.class);

		CreateTenantResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
				new File("src/test/resources/__files/CreateTenantResponse_Created.json"), CreateTenantResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse)); 
	}
	
	@Test
	public void testCreateTenantExists() throws JsonParseException, JsonMappingException, IOException {
		
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");
		CreateTenantRequest request = new CreateTenantRequest();
		String cloudSiteId = "mtn13";
		String requestId = "62265093-277d-4388-9ba6-449838ade586";
		String serviceInstanceId = "4147e06f-1b89-49c5-b21f-4faf8dc9805a";
		String tenantName = "testingTenantName";
		boolean failIfExists = false;
		boolean backout = true;
		Map<String, String> metadata = new HashMap<>();
		
		MsoRequest msoReq = new MsoRequest();
		msoReq.setRequestId(requestId);
		msoReq.setServiceInstanceId(serviceInstanceId);
		
		request.setCloudSiteId(cloudSiteId);
		request.setMsoRequest(msoReq);
		request.setTenantName(tenantName);
		request.setMetadata(metadata);
		request.setBackout(backout);
		request.setFailIfExists(failIfExists);

		mockOpenStackResponseAccessAdmin(wireMockPort);
		
		mockOpenStackGetTenantByName_200(tenantName);
		
		mockOpenStackPostTenant_200();
		
		mockOpenStackGetMetadata_200();
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<CreateTenantRequest> entity = new HttpEntity<CreateTenantRequest>(request, headers);

		ResponseEntity<CreateTenantResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/tenants"), HttpMethod.POST,
				entity, CreateTenantResponse.class);

		CreateTenantResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
				new File("src/test/resources/__files/CreateTenantResponse_Exists.json"), CreateTenantResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
	}
	
	@Test
	public void testDeleteTenant() throws IOException {
		
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");
		DeleteTenantRequest request = new DeleteTenantRequest();
		String cloudSiteId = "mtn13";
		String tenantId = "tenantId";
		String requestId = "ra1";
		String serviceInstanceId = "sa1";
		
		MsoRequest msoReq = new MsoRequest();
		msoReq.setRequestId(requestId);
		msoReq.setServiceInstanceId(serviceInstanceId);
		
		request.setCloudSiteId(cloudSiteId);
		request.setTenantId(tenantId);
		request.setMsoRequest(msoReq);
		
		mockOpenStackResponseAccessAdmin(wireMockPort);
		
		mockOpenStackGetTenantById_200(tenantId);
		
		mockOpenStackDeleteTenantById_200(tenantId);
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<DeleteTenantRequest> entity = new HttpEntity<DeleteTenantRequest>(request, headers);

		ResponseEntity<DeleteTenantResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/tenants/tenantId"), HttpMethod.DELETE,
				entity, DeleteTenantResponse.class);
		
		DeleteTenantResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
				new File("src/test/resources/__files/DeleteTenantResponse_Success.json"), DeleteTenantResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
	}
	
	//@Ignore
	@Test
	public void testDeleteTenantFails() throws IOException {
		
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");
		DeleteTenantRequest request = new DeleteTenantRequest();
		String cloudSiteId = "mtn13";
		String tenantId = "tenantId";
		String requestId = "ra1";
		String serviceInstanceId = "sa1";
		
		MsoRequest msoReq = new MsoRequest();
		msoReq.setRequestId(requestId);
		msoReq.setServiceInstanceId(serviceInstanceId);
		
		request.setCloudSiteId(cloudSiteId);
		request.setTenantId(tenantId);
		request.setMsoRequest(msoReq);
		
		mockOpenStackResponseAccessAdmin(wireMockPort);

		mockOpenStackGetTenantById_404(tenantId);
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<DeleteTenantRequest> entity = new HttpEntity<DeleteTenantRequest>(request, headers);

		ResponseEntity<DeleteTenantResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/tenants/tenantId"), HttpMethod.DELETE,
				entity, DeleteTenantResponse.class);
		
		DeleteTenantResponse expectedResponse = jettisonTypeObjectMapper.getMapper().readValue(
				new File("src/test/resources/__files/DeleteTenantResponse_Failed.json"), DeleteTenantResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
	}
	
	@Ignore
	@Test
	public void testQuaryTenant() {
		
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		
	}
}
