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

package org.openecomp.mso.adapters.tenant;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.adapters.tenantrest.CreateTenantRequest;
import org.openecomp.mso.adapters.tenantrest.CreateTenantResponse;
import org.openecomp.mso.adapters.tenantrest.DeleteTenantRequest;
import org.openecomp.mso.adapters.tenantrest.DeleteTenantResponse;
import org.openecomp.mso.adapters.vnf.BaseRestTestUtils;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.entity.MsoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TenantAdapterRestTest extends BaseRestTestUtils{

	@Autowired
	private CloudConfig cloudConfig;
	
	@Test
	public void testCreateTenantCreated() throws JsonParseException, JsonMappingException, IOException{
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
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
		
		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access_Admin.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");
		
		String requestTenantString = this.readFile("src/test/resources/__files/OpenstackRequest_Tenant.json");

		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlEqualTo("/mockPublicUrl/tenants/?name=testingTenantName")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NOT_FOUND)));
		
		wireMockRule.stubFor(post(urlPathEqualTo("/mockPublicUrl/tenants"))
				.withRequestBody(equalToJson(requestTenantString)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBodyFile("OpenstackResponse_Tenant.json").withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/users/m93945")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBodyFile("OpenstackResponse_User.json").withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/OS-KSADM/roles")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBodyFile("OpenstackResponse_Roles.json").withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(put(urlPathEqualTo("/mockPublicUrl/tenants/tenantId/users/msoId/roles/OS-KSADM/admin")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody("").withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(post(urlPathEqualTo("/mockPublicUrl/tenants/tenantId/metadata")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBodyFile("OpenstackResponse_Metadata.json").withStatus(HttpStatus.SC_OK)));
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<CreateTenantRequest> entity = new HttpEntity<CreateTenantRequest>(request, headers);

		ResponseEntity<CreateTenantResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/tenants"), HttpMethod.POST,
				entity, CreateTenantResponse.class);

		CreateTenantResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/CreateTenantResponse_Created.json"), CreateTenantResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse)); 
	}
	
	@Test
	public void testCreateTenantExists() throws JsonParseException, JsonMappingException, IOException{
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
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
		
		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access_Admin.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");

		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlEqualTo("/mockPublicUrl/tenants/?name=testingTenantName")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBodyFile("OpenstackResponse_Tenant.json").withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(post(urlPathEqualTo("/mockPublicUrl/tenants")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/tenants/tenantId/metadata")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBodyFile("OpenstackResponse_Metadata.json").withStatus(HttpStatus.SC_OK)));
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<CreateTenantRequest> entity = new HttpEntity<CreateTenantRequest>(request, headers);

		ResponseEntity<CreateTenantResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/tenants"), HttpMethod.POST,
				entity, CreateTenantResponse.class);

		CreateTenantResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/CreateTenantResponse_Exists.json"), CreateTenantResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
	}
	
	@Test
	public void testDeleteTenant() throws IOException{
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
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
		
		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access_Admin.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");
		
		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/tenants/tenantId")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBodyFile("OpenstackResponse_Tenant.json").withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(delete(urlPathEqualTo("/mockPublicUrl/tenants/tenantId")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<DeleteTenantRequest> entity = new HttpEntity<DeleteTenantRequest>(request, headers);

		ResponseEntity<DeleteTenantResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/tenants/tenantId"), HttpMethod.DELETE,
				entity, DeleteTenantResponse.class);
		
		DeleteTenantResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/DeleteTenantResponse_Success.json"), DeleteTenantResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
		
		
	}
	
	//@Ignore
	@Test
	public void testDeleteTenantFails() throws IOException{
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
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
		
		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access_Admin.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");
		
		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/tenants/tenantId")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NOT_FOUND)));
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<DeleteTenantRequest> entity = new HttpEntity<DeleteTenantRequest>(request, headers);

		ResponseEntity<DeleteTenantResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/tenants/tenantId"), HttpMethod.DELETE,
				entity, DeleteTenantResponse.class);
		
		DeleteTenantResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/DeleteTenantResponse_Failed.json"), DeleteTenantResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
		
		
	}
	
	@Ignore
	@Test
	public void testQuaryTenant(){
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");

		
	}
}
