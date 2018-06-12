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

package org.openecomp.mso.client.adapter.network;

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
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkError;
import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkError;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.adapters.nwrest.QueryNetworkError;
import org.openecomp.mso.adapters.nwrest.QueryNetworkResponse;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkError;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkRequest;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkResponse;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkError;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkResponse;
import org.openecomp.mso.openstack.beans.NetworkRollback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class NetworkAdapterClientTest extends BaseTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(28090));

	private static final String TESTING_ID = "___TESTING___";
	private static final String AAI_NETWORK_ID = "test";
	private static final String REST_ENDPOINT = "/networks/rest/v1/networks";

	private NetworkAdapterClientImpl client = new NetworkAdapterClientImpl();
	private ObjectMapper mapper = new ObjectMapper();

	@BeforeClass
	public static void setUp() {
		System.setProperty("mso.config.path", "src/test/resources");
	}

	@Test
	public void createNetworkTest() throws NetworkAdapterClientException, JsonProcessingException {
		CreateNetworkRequest request = new CreateNetworkRequest();
		request.setCloudSiteId(TESTING_ID);

		CreateNetworkResponse mockResponse = new CreateNetworkResponse();
		mockResponse.setNetworkCreated(true);
		wireMockRule.stubFor(post(urlPathEqualTo(REST_ENDPOINT))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

		CreateNetworkResponse response = client.createNetwork(request);
		assertEquals("Testing CreateVfModule response", true, response.getNetworkCreated());
	}

	@Test(expected = NetworkAdapterClientException.class)
	public void createNetworkTestThrowException() throws NetworkAdapterClientException, JsonProcessingException {
		CreateNetworkRequest request = new CreateNetworkRequest();
		request.setCloudSiteId(TESTING_ID);

		CreateNetworkError mockResponse = new CreateNetworkError();
		mockResponse.setMessage("Error in create network");
		wireMockRule.stubFor(post(urlPathEqualTo(REST_ENDPOINT))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

		client.createNetwork(request);
	}

	@Test
	public void deleteNetworkTest() throws NetworkAdapterClientException, JsonProcessingException {
		DeleteNetworkRequest request = new DeleteNetworkRequest();
		request.setCloudSiteId(TESTING_ID);

		DeleteNetworkResponse mockResponse = new DeleteNetworkResponse();
		mockResponse.setNetworkDeleted(true);

		wireMockRule.stubFor(post(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

		DeleteNetworkResponse response = client.deleteNetwork(AAI_NETWORK_ID, request);
		assertEquals("Testing DeleteVfModule response", true, response.getNetworkDeleted());
	}

	@Test(expected = NetworkAdapterClientException.class)
	public void deleteNetworkTestThrowException() throws NetworkAdapterClientException, JsonProcessingException {
		DeleteNetworkRequest request = new DeleteNetworkRequest();
		request.setCloudSiteId(TESTING_ID);

		DeleteNetworkError mockResponse = new DeleteNetworkError();
		mockResponse.setMessage("Error in delete network");
		wireMockRule.stubFor(post(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

		client.deleteNetwork(AAI_NETWORK_ID, request);
	}

	@Test
	public void rollbackNetworkTest() throws NetworkAdapterClientException, JsonProcessingException {
		RollbackNetworkRequest request = new RollbackNetworkRequest();
		NetworkRollback rollback = new NetworkRollback();
		rollback.setCloudId(TESTING_ID);
		request.setNetworkRollback(rollback);

		RollbackNetworkResponse mockResponse = new RollbackNetworkResponse();
		mockResponse.setNetworkRolledBack(true);

		wireMockRule.stubFor(post(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

		RollbackNetworkResponse response = client.rollbackNetwork(AAI_NETWORK_ID, request);
		assertEquals("Testing DeleteVfModule response", true, response.getNetworkRolledBack());
	}
	
	@Test(expected = NetworkAdapterClientException.class)
	public void rollbackNetworkTestThrowException() throws NetworkAdapterClientException, JsonProcessingException {
		RollbackNetworkRequest request = new RollbackNetworkRequest();
		NetworkRollback rollback = new NetworkRollback();
		rollback.setCloudId(TESTING_ID);
		request.setNetworkRollback(rollback);

		RollbackNetworkError mockResponse = new RollbackNetworkError();
		mockResponse.setMessage("Error in rollback network");

		wireMockRule.stubFor(post(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

		client.rollbackNetwork(AAI_NETWORK_ID, request);
	}

	@Test
	public void queryNetworkTest() throws NetworkAdapterClientException, JsonProcessingException {
		QueryNetworkResponse mockResponse = new QueryNetworkResponse();
		mockResponse.setNetworkExists(true);

		wireMockRule.stubFor(get(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID)).withQueryParam("cloudSiteId", equalTo(TESTING_ID))
				.withQueryParam("tenantId", equalTo(TESTING_ID))
				.withQueryParam("networkStackId", equalTo("networkStackId")).withQueryParam("skipAAI", equalTo("true"))
				.withQueryParam("msoRequest.requestId", equalTo("testRequestId"))
				.withQueryParam("msoRequest.serviceInstanceId", equalTo("serviceInstanceId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

		QueryNetworkResponse response = client.queryNetwork(AAI_NETWORK_ID, TESTING_ID, TESTING_ID, "networkStackId",
				true, "testRequestId", "serviceInstanceId");
		assertEquals("Testing QueryVfModule response", true, response.getNetworkExists());
	}
	
	@Test(expected = NetworkAdapterClientException.class)
	public void queryNetworkTestThrowException() throws NetworkAdapterClientException, JsonProcessingException {
		QueryNetworkError mockResponse = new QueryNetworkError();
		mockResponse.setMessage("Error in query network");

		wireMockRule.stubFor(get(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID)).withQueryParam("cloudSiteId", equalTo(TESTING_ID))
				.withQueryParam("tenantId", equalTo(TESTING_ID))
				.withQueryParam("networkStackId", equalTo("networkStackId")).withQueryParam("skipAAI", equalTo("true"))
				.withQueryParam("msoRequest.requestId", equalTo("testRequestId"))
				.withQueryParam("msoRequest.serviceInstanceId", equalTo("serviceInstanceId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

		client.queryNetwork(AAI_NETWORK_ID, TESTING_ID, TESTING_ID, "networkStackId",
				true, "testRequestId", "serviceInstanceId");
	}

	@Test
	public void updateNetworkTest() throws NetworkAdapterClientException, JsonProcessingException {
		UpdateNetworkRequest request = new UpdateNetworkRequest();
		request.setCloudSiteId(TESTING_ID);
		request.setNetworkId("test1");

		UpdateNetworkResponse mockResponse = new UpdateNetworkResponse();
		mockResponse.setNetworkId("test1");
		wireMockRule.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(200)));

		UpdateNetworkResponse response = client.updateNetwork(AAI_NETWORK_ID, request);
		assertEquals("Testing UpdateVfModule response", "test1", response.getNetworkId());
	}
	
	@Test(expected = NetworkAdapterClientException.class)
	public void updateNetworkTestThrowException() throws NetworkAdapterClientException, JsonProcessingException {
		UpdateNetworkRequest request = new UpdateNetworkRequest();
		request.setCloudSiteId(TESTING_ID);
		request.setNetworkId("test1");

		UpdateNetworkError mockResponse = new UpdateNetworkError();
		mockResponse.setMessage("Error in update network");
		wireMockRule.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(mapper.writeValueAsString(mockResponse)).withStatus(500)));

		client.updateNetwork(AAI_NETWORK_ID, request);
	}
}
