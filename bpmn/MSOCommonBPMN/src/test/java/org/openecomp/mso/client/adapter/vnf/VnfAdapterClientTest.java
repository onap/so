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

package org.openecomp.mso.client.adapter.vnf;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Rule;
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

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class VnfAdapterClientTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(28090));

	private static final String TESTING_ID = "___TESTING___";
	private static final String AAI_VNF_ID = "test";
	private static final String AAI_VF_MODULE_ID = "test";
	private static final String REST_ENDPOINT = "/vnfs/rest/v1/vnfs";

	private VnfAdapterClientImpl client = new VnfAdapterClientImpl();

	@BeforeClass
	public static void setUp() {
		System.setProperty("mso.config.path", "src/test/resources");
	}

	@Test
	public void createVfModuleTest() {
		CreateVfModuleRequest request = new CreateVfModuleRequest();
		request.setCloudSiteId(TESTING_ID);

		CreateVfModuleResponse mockResponse = new CreateVfModuleResponse();
		mockResponse.setVfModuleCreated(true);
		wireMockRule.stubFor(post(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mockResponse.toJsonString()).withStatus(200)));

		CreateVfModuleResponse response = client.createVfModule(AAI_VNF_ID, request);
		assertEquals("Testing CreateVfModule response", true, response.getVfModuleCreated());
	}

	@Test
	public void rollbackVfModuleTest() {
		RollbackVfModuleRequest request = new RollbackVfModuleRequest();
		VfModuleRollback rollback = new VfModuleRollback();
		rollback.setCloudSiteId(TESTING_ID);
		request.setVfModuleRollback(rollback);

		RollbackVfModuleResponse mockResponse = new RollbackVfModuleResponse();
		mockResponse.setVfModuleRolledback(true);
		wireMockRule.stubFor(
				put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules/" + AAI_VF_MODULE_ID + "/rollback"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBody(mockResponse.toJsonString()).withStatus(200)));

		RollbackVfModuleResponse response = client.rollbackVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, request);
		assertEquals("Testing RollbackVfModule response", true, response.getVfModuleRolledback());
	}

	@Test
	public void deleteVfModuleTest() {
		DeleteVfModuleRequest request = new DeleteVfModuleRequest();
		request.setCloudSiteId(TESTING_ID);

		DeleteVfModuleResponse mockResponse = new DeleteVfModuleResponse();
		mockResponse.setVfModuleDeleted(true);
		wireMockRule.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules/" + AAI_VF_MODULE_ID))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mockResponse.toJsonString()).withStatus(200)));

		DeleteVfModuleResponse response = client.deleteVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, request);
		assertEquals("Testing DeleteVfModule response", true, response.getVfModuleDeleted());
	}

	@Test
	public void updateVfModuleTest() {
		UpdateVfModuleRequest request = new UpdateVfModuleRequest();
		request.setCloudSiteId(TESTING_ID);
		request.setVfModuleId("test1");

		UpdateVfModuleResponse mockResponse = new UpdateVfModuleResponse();
		mockResponse.setVfModuleId("test1");
		wireMockRule.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules/"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mockResponse.toJsonString()).withStatus(200)));

		UpdateVfModuleResponse response = client.updateVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, request);
		assertEquals("Testing UpdateVfModule response", "test1", response.getVfModuleId());
	}

	@Test
	public void queryVfModuleTest() {
		QueryVfModuleResponse mockResponse = new QueryVfModuleResponse();
		mockResponse.setVnfId(AAI_VNF_ID);
		mockResponse.setVfModuleId(AAI_VF_MODULE_ID);
		wireMockRule.stubFor(get(urlPathEqualTo(REST_ENDPOINT))
				.withQueryParam("cloudSiteId", equalTo(TESTING_ID))
				.withQueryParam("tenantId", equalTo(TESTING_ID))
				.withQueryParam("vfModuleName", equalTo("someName"))
				.withQueryParam("skipAAI", equalTo("true"))
				.withQueryParam("msoRequest.requestId", equalTo("testRequestId"))
				.withQueryParam("msoRequest.serviceInstanceId", equalTo("serviceInstanceId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mockResponse.toJsonString()).withStatus(200)));
		QueryVfModuleResponse response = client.queryVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, TESTING_ID, TESTING_ID,
				"someName", true, "testRequestId", "serviceInstanceId");
		assertEquals("Testing QueryVfModule response", AAI_VF_MODULE_ID, response.getVfModuleId());
	}

	@Test
	public void healthCheckTest() {
		wireMockRule.stubFor(get(urlPathEqualTo("/vnfs/rest/v1/vnfs")).willReturn(
				aResponse().withHeader("Content-Type", "text/plain").withBody("healthCheck").withStatus(200)));

		String healthCheck = client.healthCheck();
		assertEquals("HealthCheck is correct", "healthCheck", healthCheck);
	}
}
