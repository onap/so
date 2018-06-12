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

package org.openecomp.mso.adapters.network;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.adapters.nwrest.NetworkTechnology;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkResponse;
import org.openecomp.mso.adapters.openstack.MsoOpenstackAdaptersApplication;
import org.openecomp.mso.adapters.vnf.BaseRestTestUtils;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.entity.MsoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class NetworkAdapterRestTest extends BaseRestTestUtils{

	@Autowired
	private CloudConfig cloudConfig;
	private static final String CLOUDSITE_ID = "mtn13";
	private static final String TENANT_ID = "ba38bc24a2ef4fb2ad2810c894f1938f";
	private static final String NETWORK_ID = "da886914-efb2-4917-b335-c8381528d90b";
	private static final String NETWORK_STACK_ID = "stackId";
	private static final String NETWORK_TYPE = "CONTRAIL30_BASIC";
	private static final String MODEL_CUSTOMIZATION_UUID = "3bdbb104-476c-483e-9f8b-c095b3d308ac";
	private static final String MSO_SERVICE_INSTANCE_ID = "05869d5f-47df-4b45-bbfc-4f03ce0a50bf";
	private static final String MSO_REQUEST_ID = "requestId";
	private static final String NETWORK_NAME = "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0";

	@Test
	public void testCreateNetwork() throws JSONException, JsonParseException, JsonMappingException, IOException {
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
		CreateNetworkRequest request = new CreateNetworkRequest();
		request.setBackout(true);
		request.setSkipAAI(true);
		request.setFailIfExists(false);
		MsoRequest msoReq = new MsoRequest();
		NetworkTechnology networkTechnology = NetworkTechnology.CONTRAIL;

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

		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");

		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(post(urlPathEqualTo("/mockPublicUrl/stacks"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Stack.json").withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0/stackId"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBodyFile("OpenstackResponse_Stack_Created.json").withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0"))
						.willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));

		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<CreateNetworkRequest> entity = new HttpEntity<CreateNetworkRequest>(request, headers);

		ResponseEntity<CreateNetworkResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks"), HttpMethod.POST, entity, CreateNetworkResponse.class);

		CreateNetworkResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/CreateNetworkResponse.json"), CreateNetworkResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
	}
	
	

	@Test
	public void testCreateNetwork_JSON() throws JSONException, JsonParseException, JsonMappingException, IOException {
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");


		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");

		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(post(urlPathEqualTo("/mockPublicUrl/stacks"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Stack.json").withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/APP-C-24595-T-IST-04AShared_untrusted_vDBE_net_3/stackId"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBodyFile("OpenstackResponse_Stack_Created.json").withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/APP-C-24595-T-IST-04AShared_untrusted_vDBE_net_3"))
						.willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));
		
		headers.add("Content-Type", MediaType.APPLICATION_JSON);
		headers.add("Accept", MediaType.APPLICATION_JSON);
		
		String request = readJsonFileAsString("src/test/resources/CreateNetwork.json");
		HttpEntity<String> entity = new HttpEntity<String>(request, headers);

		ResponseEntity<CreateNetworkResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks"), HttpMethod.POST, entity, CreateNetworkResponse.class);

		CreateNetworkResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/CreateNetworkResponse2.json"), CreateNetworkResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
	}
	
	@Test
	public void testDeleteNetwork() throws IOException{
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
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

		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");
		
		String quaryResponse = this.readFile("src/test/resources/__files/OpenstackResponse_StackId.json");
		String quaryResponsereplaced = quaryResponse.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/stackId");
		
		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_ID)).willReturn(aResponse()
				.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/"+NETWORK_ID)
				.withBody(quaryResponsereplaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_NAME+"/"+NETWORK_ID)).willReturn(aResponse()
				.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/"+NETWORK_NAME+"/"+NETWORK_ID)
				.withBody(quaryResponsereplaced).withStatus(HttpStatus.SC_NOT_FOUND)));
		
		wireMockRule.stubFor(delete(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_NAME+"/"+NETWORK_ID)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NO_CONTENT)));
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		
		HttpEntity<DeleteNetworkRequest> entity = new HttpEntity<DeleteNetworkRequest>(request, headers);
		
		ResponseEntity<DeleteNetworkResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks/da886914-efb2-4917-b335-c8381528d90b"), HttpMethod.DELETE, entity, DeleteNetworkResponse.class); 
		
		DeleteNetworkResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/DeleteNetworkResponse.json"), DeleteNetworkResponse.class);
		
		//verify(getRequestedFor(urlPathEqualTo("/mockPublicUrl/stacks"+NETWORK_NAME+"/"+NETWORK_ID)));
		
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		
	}
	
	@Test
	public void testUpdateNetwork() throws IOException{
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
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
		
		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");
		
		String quaryResponse = this.readFile("src/test/resources/__files/OpenstackResponse_StackId.json");
		String quaryResponsereplaced = quaryResponse.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/"+NETWORK_NAME);
		
		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_NAME)).willReturn(aResponse()
				.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/"+NETWORK_NAME)
				.withBody(quaryResponsereplaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_ID)).willReturn(aResponse()
				.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/"+NETWORK_NAME)
				.withBody(quaryResponsereplaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_NAME+"/"+NETWORK_ID)).willReturn(aResponse()
				.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/"+NETWORK_NAME)
				.withBody(quaryResponsereplaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(put(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_NAME+"/"+NETWORK_ID)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));

				
	
		headers.add("Accept", MediaType.APPLICATION_JSON);
		
		HttpEntity<UpdateNetworkRequest> entity = new HttpEntity<UpdateNetworkRequest>(request, headers);
		
		ResponseEntity<UpdateNetworkResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/networks/da886914-efb2-4917-b335-c8381528d90b"), HttpMethod.PUT, entity, UpdateNetworkResponse.class); 
		
		UpdateNetworkResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/UpdateNetworkResponse.json"), UpdateNetworkResponse.class);
		
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		
	}	
	
	protected String readJsonFileAsString(String fileLocation) throws JsonParseException, JsonMappingException, IOException{
		return new String(Files.readAllBytes(Paths.get(fileLocation)));
	}
}
