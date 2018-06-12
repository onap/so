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

package org.openecomp.mso.adapters.vnf;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.adapters.vnfrest.CreateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.CreateVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.QueryVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.RollbackVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.RollbackVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleRequest;
import org.openecomp.mso.adapters.vnfrest.UpdateVfModuleResponse;
import org.openecomp.mso.adapters.vnfrest.VfModuleRollback;
import org.openecomp.mso.adapters.vnfrest.VfRequestCommon;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.entity.MsoRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VnfAdapterRestTest extends BaseRestTestUtils {
	
	@Autowired
	private CloudConfig cloudConfig;
	private static final String MESSAGE_ID = "62265093-277d-4388-9ba6-449838ade586-1517252396874";
	private static final String AAI_VNF_ID = "c93e0d34-5b63-45de-bbae-b0fe49dd3bd9";
	private static final String MSO_REQUEST_ID = "62265093-277d-4388-9ba6-449838ade586";
	private static final String MSO_SERVICE_INSTANCE_ID = "4147e06f-1b89-49c5-b21f-4faf8dc9805a";
	private static final String CLOUDSITE_ID = "mtn13";
	private static final String TENANT_ID = "0422ffb57ba042c0800a29dc85ca70f8";
	private static final String VNF_TYPE = "MSOTADevInfra_vSAMP10a_Service/vSAMP10a 1";
	private static final String VNF_NAME = "MSO-DEV-VNF-1802-it3-pwt3-vSAMP10a-1XXX-Replace";
	private static final String VNF_VERSION = "1.0";
	private static final String VF_MODULE_ID = "1d48aaec-b7f3-4c24-ba4a-4e798ed3223c";
	private static final String VF_MODULE_NAME = "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001";
	private static final String VF_MODULE_TYPE = "vSAMP10aDEV::PCM::module-2";
	private static final String MODEL_CUSTOMIZATION_UUID = "cb82ffd8-252a-11e7-93ae-92361f002671";
	private static final String BASE_VF_MODULE_ID = "3d7ff7b4-720b-4604-be0a-1974fc58ed96";
	// vfModuleParams specific variables
	private static final String NETWORK_NAME = "Dev-vSAMP10a-ntwk-1802-pwt3-v6-Replace-1001";
	private static final String SERVER_NAME = "Dev-vSAMP10a-addon2-1802-pwt3-v6-Replace-1001";
	private static final String IMAGE = "ubuntu_14.04_IPv6";
	private static final String EXN_DIRECT_NET_FQDN = "direct";
	private static final String EXN_HSL_NET_FQDN = "hsl";
	private static final String AVAILABILITY_ZONE_0 = "nova";
	private static final String VF_MODULE_INDEX = "0";
	private static final String REQUEST_TYPE = "";

	@Test
	public void testCreateVfModule() throws JSONException, JsonParseException, JsonMappingException, IOException {
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
		CreateVfModuleRequest request = new CreateVfModuleRequest();
		request.setBackout(true);
		request.setSkipAAI(true);
		request.setFailIfExists(false);
		MsoRequest msoReq = new MsoRequest();
		boolean failIfExists = true;
		boolean enableBridge = false;
		Map<String, String> vfModuleParams = new HashMap<String, String>();
		
		vfModuleParams.put("vf_module_id", VF_MODULE_ID);
		vfModuleParams.put("vnf_id", AAI_VNF_ID);
		vfModuleParams.put("network_name", NETWORK_NAME);
		vfModuleParams.put("vnf_name", VNF_NAME);
		vfModuleParams.put("environment_context", "");
		vfModuleParams.put("server_name", SERVER_NAME);
		vfModuleParams.put("image", IMAGE);
		vfModuleParams.put("workload_context", "");
		vfModuleParams.put("vf_module_index", VF_MODULE_INDEX);
		vfModuleParams.put("vf_module_name", VF_MODULE_NAME);
		vfModuleParams.put("availability_zone_0", AVAILABILITY_ZONE_0);
		vfModuleParams.put("exn_direct_net_fqdn", EXN_DIRECT_NET_FQDN);
		vfModuleParams.put("exn_hsl_net_fqdn", EXN_HSL_NET_FQDN);

		msoReq.setRequestId(MSO_REQUEST_ID);
		msoReq.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
		request.setMsoRequest(msoReq);
		request.setRequestType(REQUEST_TYPE);
		request.setCloudSiteId(CLOUDSITE_ID);
		request.setTenantId(TENANT_ID);
		request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
		request.setVnfId(AAI_VNF_ID);
		request.setVnfType(VNF_TYPE);
		request.setVnfVersion(VNF_VERSION);
		request.setVfModuleId(VF_MODULE_ID);
		request.setVfModuleName(VF_MODULE_NAME);
		request.setVfModuleType(VF_MODULE_TYPE);
		request.setBaseVfModuleId(BASE_VF_MODULE_ID);
		request.setFailIfExists(failIfExists);
		request.setEnableBridge(enableBridge);
		request.setVfModuleParams(vfModuleParams);
		request.setMessageId(MESSAGE_ID);

		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");

		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withStatus(HttpStatus.SC_NOT_FOUND)));

		wireMockRule.stubFor(post(urlPathEqualTo("/mockPublicUrl/stacks"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Stack.json").withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(get(
				urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBodyFile("OpenstackResponse_Stack_Created_VfModule.json")
								.withStatus(HttpStatus.SC_OK)));

		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<CreateVfModuleRequest> entity = new HttpEntity<CreateVfModuleRequest>(request, headers);

		ResponseEntity<CreateVfModuleResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"), HttpMethod.POST,
				entity, CreateVfModuleResponse.class);
		
		ResponseEntity<CreateVfModuleResponse> responseV2 = restTemplate.exchange(
				createURLWithPort("/services/rest/v2/vnfs/" + AAI_VNF_ID + "/vf-modules"), HttpMethod.POST,
				entity, CreateVfModuleResponse.class);

		CreateVfModuleResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/CreateVfModuleResponse.json"), CreateVfModuleResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
		
		assertEquals(Response.Status.OK.getStatusCode(), responseV2.getStatusCode().value());
		assertThat(responseV2.getBody(), sameBeanAs(expectedResponse));
	}
	
	
	@Test
	public void testCreateVfModuleWithEnableBridgeNull()
			throws JSONException, JsonParseException, JsonMappingException, IOException {
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
		CreateVfModuleRequest request = new CreateVfModuleRequest();
		request.setBackout(true);
		request.setSkipAAI(true);
		request.setFailIfExists(false);
		MsoRequest msoReq = new MsoRequest();
		boolean failIfExists = true;
		Boolean enableBridge = null;
		Map<String, String> vfModuleParams = new HashMap<String, String>();


		vfModuleParams.put("vf_module_id", VF_MODULE_ID);
		vfModuleParams.put("vnf_id", AAI_VNF_ID);
		vfModuleParams.put("network_name", NETWORK_NAME);
		vfModuleParams.put("vnf_name", VNF_NAME);
		vfModuleParams.put("environment_context", "");
		vfModuleParams.put("server_name", SERVER_NAME);
		vfModuleParams.put("image", IMAGE);
		vfModuleParams.put("workload_context", "");
		vfModuleParams.put("vf_module_index", VF_MODULE_INDEX);
		vfModuleParams.put("vf_module_name", VF_MODULE_NAME);
		vfModuleParams.put("availability_zone_0", AVAILABILITY_ZONE_0);
		vfModuleParams.put("exn_direct_net_fqdn", EXN_DIRECT_NET_FQDN);
		vfModuleParams.put("exn_hsl_net_fqdn", EXN_HSL_NET_FQDN);

		msoReq.setRequestId(MSO_REQUEST_ID);
		msoReq.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
		request.setMsoRequest(msoReq);
		request.setRequestType(REQUEST_TYPE);
		request.setCloudSiteId(CLOUDSITE_ID);
		request.setTenantId(TENANT_ID);
		request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
		request.setVnfId(AAI_VNF_ID);
		request.setVnfVersion(VNF_VERSION);
		request.setVfModuleId(VF_MODULE_ID);
		request.setVfModuleName(VF_MODULE_NAME);
		request.setBaseVfModuleId(BASE_VF_MODULE_ID);
		request.setFailIfExists(failIfExists);
		request.setEnableBridge(enableBridge);
		request.setVfModuleParams(vfModuleParams);
		request.setMessageId(MESSAGE_ID);

		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");

		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withStatus(HttpStatus.SC_NOT_FOUND)));

		wireMockRule.stubFor(post(urlPathEqualTo("/mockPublicUrl/stacks"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Stack.json").withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(get(
				urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBodyFile("OpenstackResponse_Stack_Created_VfModule.json")
								.withStatus(HttpStatus.SC_OK)));

		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<CreateVfModuleRequest> entity = new HttpEntity<CreateVfModuleRequest>(request, headers);

		ResponseEntity<CreateVfModuleResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"), HttpMethod.POST,
				entity, CreateVfModuleResponse.class);

		CreateVfModuleResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/CreateVfModuleResponse.json"), CreateVfModuleResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
	}

	@Test
	public void testCreateVfModuleFail() throws IOException{
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
		CreateVfModuleRequest request = new CreateVfModuleRequest();
		request.setBackout(true);
		request.setSkipAAI(true);
		request.setFailIfExists(false);
		MsoRequest msoReq = new MsoRequest();
		boolean failIfExists = true;
		boolean enableBridge = false;
		Map<String, String> vfModuleParams = new HashMap<String, String>();

		vfModuleParams.put("vf_module_id", VF_MODULE_ID);
		vfModuleParams.put("vnf_id", AAI_VNF_ID);
		vfModuleParams.put("network_name", NETWORK_NAME);
		vfModuleParams.put("vnf_name", VNF_NAME);
		vfModuleParams.put("environment_context", "");
		vfModuleParams.put("server_name", SERVER_NAME);
		vfModuleParams.put("image", IMAGE);
		vfModuleParams.put("workload_context", "");
		vfModuleParams.put("vf_module_index", VF_MODULE_INDEX);
		vfModuleParams.put("vf_module_name", VF_MODULE_NAME);
		vfModuleParams.put("availability_zone_0", AVAILABILITY_ZONE_0);
		vfModuleParams.put("exn_direct_net_fqdn", EXN_DIRECT_NET_FQDN);
		vfModuleParams.put("exn_hsl_net_fqdn", EXN_HSL_NET_FQDN);

		msoReq.setRequestId(MSO_REQUEST_ID);
		msoReq.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
		request.setMsoRequest(msoReq);
		request.setRequestType(REQUEST_TYPE);
		request.setCloudSiteId(CLOUDSITE_ID);
		request.setTenantId(TENANT_ID);
		request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
		request.setVnfId(AAI_VNF_ID);
		request.setVnfType(VNF_TYPE);
		request.setVnfVersion(VNF_VERSION);
		request.setVfModuleId(VF_MODULE_ID);
		request.setVfModuleName(VF_MODULE_NAME);
		request.setVfModuleType(VF_MODULE_TYPE);
		request.setBaseVfModuleStackId(BASE_VF_MODULE_ID);
		request.setFailIfExists(failIfExists);
		request.setEnableBridge(enableBridge);
		request.setVfModuleParams(vfModuleParams);
		request.setMessageId(MESSAGE_ID);

		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");

		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBodyFile("OpenstackResponse_Stack_Created_VfModule.json")
								.withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/3d7ff7b4-720b-4604-be0a-1974fc58ed96"))
						.willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));

		wireMockRule.stubFor(post(urlPathEqualTo("/mockPublicUrl/stacks"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Stack.json").withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(get(
				urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBodyFile("OpenstackResponse_Stack_Created_VfModule.json")
								.withStatus(HttpStatus.SC_OK)));

		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<CreateVfModuleRequest> entity = new HttpEntity<CreateVfModuleRequest>(request, headers);

		ResponseEntity<CreateVfModuleResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"), HttpMethod.POST,
				entity, CreateVfModuleResponse.class);

		CreateVfModuleResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/CreateVfModuleResponse.json"), CreateVfModuleResponse.class);

		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
		
		String responseBody =  this.readFile("src/test/resources/__files/OpenstackResponse_Stack_Created_VfModule.json");
		String replaceResponse = responseBody.replaceAll("CREATE_COMPLETE", "DELETE_IN_PROGRESS");
		
		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBody(replaceResponse)
								.withStatus(HttpStatus.SC_OK)));
		
		response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"), HttpMethod.POST,
				entity, CreateVfModuleResponse.class);
		
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
		
		responseBody =  this.readFile("src/test/resources/__files/OpenstackResponse_Stack_Created_VfModule.json");
		replaceResponse = responseBody.replaceAll("CREATE_COMPLETE", "DELETE_FAILED");
		
		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBody(replaceResponse)
								.withStatus(HttpStatus.SC_OK)));
		
		response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"), HttpMethod.POST,
				entity, CreateVfModuleResponse.class);
		
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
		
		replaceResponse = replaceResponse.replaceAll("DELETE_FAILED", "UPDATE_COMPLETE");
		
		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBody(replaceResponse)
								.withStatus(HttpStatus.SC_OK)));
		
		response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"), HttpMethod.POST,
				entity, CreateVfModuleResponse.class);
		
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
		
		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBody(replaceResponse)
								.withStatus(HttpStatus.SC_NOT_FOUND)));
		
		response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules"), HttpMethod.POST,
				entity, CreateVfModuleResponse.class);
		
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
		
	}
	
	@Test
	public void testDeleteVfModule() throws IOException{
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
		DeleteVfModuleRequest request = new DeleteVfModuleRequest();
		MsoRequest msoRequest = new MsoRequest();
		String vfModuleStackId = "stackId";

		 
		msoRequest.setRequestId(MSO_REQUEST_ID);
		msoRequest.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
		request.setCloudSiteId(CLOUDSITE_ID);
		request.setTenantId(TENANT_ID);
		request.setVfModuleId(VF_MODULE_ID);
		request.setVfModuleStackId(vfModuleStackId);
		request.setVnfId(AAI_VNF_ID);
		request.setMsoRequest(msoRequest);
		
		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");
		
		String quaryResponse = this.readFile("src/test/resources/__files/OpenstackResponse_StackId.json");
		String quaryResponsereplaced = quaryResponse.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/stackId");
		
		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(post(urlPathEqualTo("/mockPublicUrl/stacks"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Stack.json").withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/stackId")).willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0/da886914-efb2-4917-b335-c8381528d90b")).willReturn(aResponse()
				.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/stackId")
				.withBody(quaryResponsereplaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(delete(urlPathEqualTo("/mockPublicUrl/stacks/vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0/da886914-efb2-4917-b335-c8381528d90b")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NO_CONTENT)));
		
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<DeleteVfModuleRequest> entity = new HttpEntity<DeleteVfModuleRequest>(request, headers);
		
		ResponseEntity<DeleteVfModuleResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_ID), HttpMethod.DELETE,
				entity, DeleteVfModuleResponse.class);
		
		ResponseEntity<DeleteVfModuleResponse> responseV2 = restTemplate.exchange(
				createURLWithPort("/services/rest/v2/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_ID), HttpMethod.DELETE,
				entity, DeleteVfModuleResponse.class);
		

		DeleteVfModuleResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/DeleteVfModuleResponse.json"), DeleteVfModuleResponse.class);
		
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
		
		assertEquals(Response.Status.OK.getStatusCode(), responseV2.getStatusCode().value());
		assertThat(responseV2.getBody(), sameBeanAs(expectedResponse));
	}
	
	@Test
	public void testUpdateVfModule() throws IOException{
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
		UpdateVfModuleRequest request = new UpdateVfModuleRequest();
		MsoRequest msoRequest = new MsoRequest();
		String vfModuleStackId = "vfModuleStackId";
		String volumeGroupId = "volumeGroupId";
		String volumeGroupStackId = "volumeGroupStackId";
		String baseVfModuleStackId = "baseVfModuleStackId";
		Boolean failIfExists = false;
		Boolean backout = false;
		msoRequest.setRequestId(MSO_REQUEST_ID);
		msoRequest.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
		
		Map<String, String> vfModuleParams = new HashMap<String, String>();

		vfModuleParams.put("exn_direct_net_fqdn", EXN_DIRECT_NET_FQDN);
		vfModuleParams.put("exn_hsl_net_fqdn", EXN_HSL_NET_FQDN);

		Map<String, String> vfModuleOutputs = new HashMap<String, String>();
		
		vfModuleOutputs.put("output name", "output value");
		
		request.setBackout(backout);
		//request.setBaseVfModuleId(BASE_VF_MODULE_ID);
		//request.setBaseVfModuleStackId(baseVfModuleStackId);
		request.setCloudSiteId(CLOUDSITE_ID);
		request.setFailIfExists(failIfExists);
		request.setModelCustomizationUuid(MODEL_CUSTOMIZATION_UUID);
		request.setMsoRequest(msoRequest);
		request.setRequestType(REQUEST_TYPE);
		request.setTenantId(TENANT_ID);
		request.setVfModuleId(VF_MODULE_ID);
		request.setVfModuleName(VF_MODULE_NAME);
		request.setVfModuleStackId(vfModuleStackId);
		request.setBackout(backout);
		request.setVfModuleParams(vfModuleParams);
		
		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");
		
		String quaryStackResponse = this.readFile("src/test/resources/__files/OpenstackResponse_VnfStackId.json");
		String quaryStackResponsereplaced = quaryStackResponse.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/stackId");
		
		String quaryBaseStackResponse = this.readFile("src/test/resources/__files/OpenstackResponse_VnfBaseStackId.json");
		String quaryBaseStackResponsereplaced = quaryBaseStackResponse.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/stackId");
		
		
		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001")).willReturn(aResponse()
				.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001")
				.withBody(quaryStackResponsereplaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001")).willReturn(aResponse()
				.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withBody(quaryStackResponsereplaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/baseVfModuleStackId")).willReturn(aResponse()
				.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/baseVfModuleStackId")
				.withBody(quaryBaseStackResponsereplaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(put(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
		
		UpdateVfModuleResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/UpdateVfModuleResponse.json"), UpdateVfModuleResponse.class);
		expectedResponse.setVfModuleOutputs(vfModuleOutputs);
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<UpdateVfModuleRequest> entity = new HttpEntity<UpdateVfModuleRequest>(request, headers);
		
		ResponseEntity<UpdateVfModuleResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_TYPE), HttpMethod.PUT,
				entity, UpdateVfModuleResponse.class);
		 
		ResponseEntity<UpdateVfModuleResponse> responseV2 = restTemplate.exchange(
				createURLWithPort("/services/rest/v2/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_TYPE), HttpMethod.PUT,
				entity, UpdateVfModuleResponse.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
		
		assertEquals(Response.Status.OK.getStatusCode(), responseV2.getStatusCode().value());
		assertThat(responseV2.getBody(), sameBeanAs(expectedResponse));	
		
	}
	
	@Test
	public void testRollbackVfModule() throws IOException{
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
		
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId(MSO_REQUEST_ID);
		msoRequest.setServiceInstanceId(MSO_SERVICE_INSTANCE_ID);
		
		VfModuleRollback vfModuleRollback = new VfModuleRollback(AAI_VNF_ID, VF_MODULE_ID,
				"StackId", false, TENANT_ID, CLOUDSITE_ID, msoRequest, "messageId");
		
		RollbackVfModuleRequest request = new RollbackVfModuleRequest();
		request.setVfModuleRollback(vfModuleRollback);
		
		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");
		
		String quaryResponse = this.readFile("src/test/resources/__files/OpenstackResponse_StackId.json");
		String quaryResponsereplaced = quaryResponse.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/stackId");
		
		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/StackId")).willReturn(aResponse()
				.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + wiremockPort + "/mockPublicUrl/stacks/stackId")
				.withBody(quaryResponsereplaced).withStatus(HttpStatus.SC_OK)));
		
		wireMockRule.stubFor(delete(urlPathEqualTo("/mockPublicUrl/stacks/vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0/da886914-efb2-4917-b335-c8381528d90b")).
				willReturn(aResponse().withHeader("X-Openstack-Request-Id", "openstackRquest")));
		
		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0/da886914-efb2-4917-b335-c8381528d90b")).
				willReturn(aResponse()
						.withHeader("X-Openstack-Request-Id", "openstackRquest")
						.withStatus(HttpStatus.SC_NOT_FOUND)));
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity<RollbackVfModuleRequest> entity = new HttpEntity<RollbackVfModuleRequest>(request, headers);
		
		ResponseEntity<RollbackVfModuleResponse> response = restTemplate.exchange(
				createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_ID + "/rollback"), HttpMethod.DELETE,
				entity, RollbackVfModuleResponse.class);
		
		RollbackVfModuleResponse expectedResponse = new ObjectMapper().readValue(
				new File("src/test/resources/__files/RollbackVfModuleResponse.json"),RollbackVfModuleResponse.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(), sameBeanAs(expectedResponse));
		
	}
	
	@Ignore
	@Test
	public void testQueryVfModule() throws IOException{
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
		String testUrl = createURLWithPort("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_ID);
		String testUri = UriBuilder.fromPath("/services/rest/v1/vnfs/" + AAI_VNF_ID + "/vf-modules/" + VF_MODULE_ID )
				.host("localhost").port(wiremockPort).scheme("http")
				//.queryParam("cloudSiteId", CLOUDSITE_ID).queryParam("tenantId", TENANT_ID)
				.build().toString();
		System.out.println(testUri);
		
		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");
		
		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));
		
		
		headers.add("Accept", MediaType.APPLICATION_JSON);
		HttpEntity entity = new HttpEntity(null, headers);
		ResponseEntity<QueryVfModuleResponse> response = restTemplate.getForEntity(testUri, QueryVfModuleResponse.class);
		System.out.println(response);
	}
}
