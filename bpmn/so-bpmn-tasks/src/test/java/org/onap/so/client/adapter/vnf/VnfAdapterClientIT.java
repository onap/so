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

package org.onap.so.client.adapter.vnf;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.adapters.vnfrest.CreateVfModuleRequest;
import org.onap.so.adapters.vnfrest.CreateVfModuleResponse;
import org.onap.so.adapters.vnfrest.DeleteVfModuleRequest;
import org.onap.so.adapters.vnfrest.DeleteVfModuleResponse;
import org.onap.so.adapters.vnfrest.QueryVfModuleResponse;
import org.onap.so.adapters.vnfrest.RollbackVfModuleRequest;
import org.onap.so.adapters.vnfrest.RollbackVfModuleResponse;
import org.onap.so.adapters.vnfrest.UpdateVfModuleRequest;
import org.onap.so.adapters.vnfrest.UpdateVfModuleResponse;
import org.onap.so.adapters.vnfrest.VfModuleExceptionResponse;
import org.onap.so.adapters.vnfrest.VfModuleRollback;
import org.onap.so.client.policy.JettisonStyleMapperProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VnfAdapterClientIT extends BaseIntegrationTest{

	private static final String TESTING_ID = "___TESTING___";
	private static final String AAI_VNF_ID = "test";
	private static final String AAI_VF_MODULE_ID = "test";
	private static final String REST_ENDPOINT = "/services/rest/v1/vnfs";

	private VnfAdapterClientImpl client = new VnfAdapterClientImpl();
	private ObjectMapper mapper = new JettisonStyleMapperProvider().getMapper();

	@BeforeClass
	public static void setUp() {
		System.setProperty("mso.config.path", "src/test/resources");
	}

	@Test
	public void createVfModuleTest() throws JsonProcessingException, VnfAdapterClientException {
		CreateVfModuleRequest request = new CreateVfModuleRequest();
		request.setCloudSiteId(TESTING_ID);

		CreateVfModuleResponse mockResponse = new CreateVfModuleResponse();
		mockResponse.setVfModuleCreated(true);
		wireMockServer.stubFor(post(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

		CreateVfModuleResponse response = client.createVfModule(AAI_VNF_ID, request);
		assertEquals("Testing CreateVfModule response", true, response.getVfModuleCreated());
	}
	
	@Test(expected = VnfAdapterClientException.class)
	public void createVfModuleTestThrowException() throws JsonProcessingException, VnfAdapterClientException {
		CreateVfModuleRequest request = new CreateVfModuleRequest();
		request.setCloudSiteId(TESTING_ID);

		VfModuleExceptionResponse mockResponse = new VfModuleExceptionResponse();
		mockResponse.setMessage("Error in create Vf module");
		wireMockServer.stubFor(post(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

		client.createVfModule(AAI_VNF_ID, request);
	}

	@Test
	public void rollbackVfModuleTest() throws JsonProcessingException, VnfAdapterClientException {
		RollbackVfModuleRequest request = new RollbackVfModuleRequest();
		VfModuleRollback rollback = new VfModuleRollback();
		rollback.setCloudSiteId(TESTING_ID);
		request.setVfModuleRollback(rollback);

		RollbackVfModuleResponse mockResponse = new RollbackVfModuleResponse();
		mockResponse.setVfModuleRolledback(true);
		wireMockServer.stubFor(
			delete(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules/" + AAI_VF_MODULE_ID + "/rollback"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

		RollbackVfModuleResponse response = client.rollbackVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, request);
		assertEquals("Testing RollbackVfModule response", true, response.getVfModuleRolledback());
	}
	
	@Test(expected = VnfAdapterClientException.class)
	public void rollbackVfModuleTestThrowException() throws JsonProcessingException, VnfAdapterClientException {
		RollbackVfModuleRequest request = new RollbackVfModuleRequest();
		VfModuleRollback rollback = new VfModuleRollback();
		rollback.setCloudSiteId(TESTING_ID);
		request.setVfModuleRollback(rollback);

		VfModuleExceptionResponse mockResponse = new VfModuleExceptionResponse();
		mockResponse.setMessage("Error in rollback Vf module");
		wireMockServer.stubFor(
			delete(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules/" + AAI_VF_MODULE_ID + "/rollback"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

		client.rollbackVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, request);
	}

	@Test
	public void deleteVfModuleTest() throws JsonProcessingException, VnfAdapterClientException {
		DeleteVfModuleRequest request = new DeleteVfModuleRequest();
		request.setCloudSiteId(TESTING_ID);

		DeleteVfModuleResponse mockResponse = new DeleteVfModuleResponse();
		mockResponse.setVfModuleDeleted(true);
		wireMockServer.stubFor(delete(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules/" + AAI_VF_MODULE_ID))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

		DeleteVfModuleResponse response = client.deleteVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, request);
		assertEquals("Testing DeleteVfModule response", true, response.getVfModuleDeleted());
	}
	
	@Test(expected = VnfAdapterClientException.class)
	public void deleteVfModuleTestThrowException() throws JsonProcessingException, VnfAdapterClientException {
		DeleteVfModuleRequest request = new DeleteVfModuleRequest();
		request.setCloudSiteId(TESTING_ID);

		VfModuleExceptionResponse mockResponse = new VfModuleExceptionResponse();
		mockResponse.setMessage("Error in delete Vf module");
		wireMockServer.stubFor(delete(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules/" + AAI_VF_MODULE_ID))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

		client.deleteVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, request);
	}

	@Test
	public void updateVfModuleTest() throws JsonProcessingException, VnfAdapterClientException {
		UpdateVfModuleRequest request = new UpdateVfModuleRequest();
		request.setCloudSiteId(TESTING_ID);
		request.setVfModuleId("test1");

		UpdateVfModuleResponse mockResponse = new UpdateVfModuleResponse();
		mockResponse.setVfModuleId("test1");
		wireMockServer.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules/" + AAI_VF_MODULE_ID))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

		UpdateVfModuleResponse response = client.updateVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, request);
		assertEquals("Testing UpdateVfModule response", "test1", response.getVfModuleId());
	}
	
	@Test(expected = VnfAdapterClientException.class)
	public void updateVfModuleTestThrowException() throws JsonProcessingException, VnfAdapterClientException {
		UpdateVfModuleRequest request = new UpdateVfModuleRequest();
		request.setCloudSiteId(TESTING_ID);
		request.setVfModuleId("test1");

		VfModuleExceptionResponse mockResponse = new VfModuleExceptionResponse();
		mockResponse.setMessage("Error in update Vf module");
		wireMockServer.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules/" + AAI_VF_MODULE_ID))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

		client.updateVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, request);
	}

	@Test
	public void queryVfModuleTest() throws JsonProcessingException, VnfAdapterClientException {
		QueryVfModuleResponse mockResponse = new QueryVfModuleResponse();
		mockResponse.setVnfId(AAI_VNF_ID);
		mockResponse.setVfModuleId(AAI_VF_MODULE_ID);
		wireMockServer.stubFor(get(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules/" + AAI_VF_MODULE_ID))
				.withQueryParam("cloudSiteId", equalTo(TESTING_ID))
				.withQueryParam("tenantId", equalTo(TESTING_ID))
				.withQueryParam("vfModuleName", equalTo("someName"))
				.withQueryParam("skipAAI", equalTo("true"))
				.withQueryParam("msoRequest.requestId", equalTo("testRequestId"))
				.withQueryParam("msoRequest.serviceInstanceId", equalTo("serviceInstanceId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));
		QueryVfModuleResponse response = client.queryVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, TESTING_ID, TESTING_ID,
				"someName", true, "testRequestId", "serviceInstanceId");
		assertEquals("Testing QueryVfModule response", AAI_VF_MODULE_ID, response.getVfModuleId());
	}
	
	@Test(expected = VnfAdapterClientException.class)
	public void queryVfModuleTestThrowException() throws JsonProcessingException, VnfAdapterClientException {
		VfModuleExceptionResponse mockResponse = new VfModuleExceptionResponse();
		mockResponse.setMessage("Error in update Vf module");
		wireMockServer.stubFor(get(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_VNF_ID + "/vf-modules/" + AAI_VF_MODULE_ID))
				.withQueryParam("cloudSiteId", equalTo(TESTING_ID))
				.withQueryParam("tenantId", equalTo(TESTING_ID))
				.withQueryParam("vfModuleName", equalTo("someName"))
				.withQueryParam("skipAAI", equalTo("true"))
				.withQueryParam("msoRequest.requestId", equalTo("testRequestId"))
				.withQueryParam("msoRequest.serviceInstanceId", equalTo("serviceInstanceId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));
		client.queryVfModule(AAI_VNF_ID, AAI_VF_MODULE_ID, TESTING_ID, TESTING_ID,
				"someName", true, "testRequestId", "serviceInstanceId");
	}
}
