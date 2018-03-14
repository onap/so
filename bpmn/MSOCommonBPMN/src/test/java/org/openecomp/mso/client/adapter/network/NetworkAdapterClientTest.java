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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.CreateNetworkResponse;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkRequest;
import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.adapters.nwrest.QueryNetworkResponse;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkRequest;
import org.openecomp.mso.adapters.nwrest.RollbackNetworkResponse;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkRequest;
import org.openecomp.mso.adapters.nwrest.UpdateNetworkResponse;
import org.openecomp.mso.openstack.beans.NetworkRollback;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class NetworkAdapterClientTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(28090));

	private static final String TESTING_ID = "___TESTING___";
	private static final String AAI_NETWORK_ID = "test";
	private static final String REST_ENDPOINT = "/networks/rest/v1/networks";

	private NetworkAdapterClientImpl client = new NetworkAdapterClientImpl();

	@BeforeClass
	public static void setUp() {
		System.setProperty("mso.config.path", "src/test/resources");
	}

	@Test
	public void createNetworkTest() {
		CreateNetworkRequest request = new CreateNetworkRequest();
		request.setCloudSiteId(TESTING_ID);

		CreateNetworkResponse mockResponse = new CreateNetworkResponse();
		mockResponse.setNetworkCreated(true);
		wireMockRule.stubFor(post(urlPathEqualTo(REST_ENDPOINT)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(mockResponse.toJsonString()).withStatus(200)));

		CreateNetworkResponse response = client.createNetwork(request);
		assertEquals("Testing CreateVfModule response", true, response.getNetworkCreated());
	}

	@Test
	public void deleteNetworkTest() {
		DeleteNetworkRequest request = new DeleteNetworkRequest();
		request.setCloudSiteId(TESTING_ID);

		DeleteNetworkResponse mockResponse = new DeleteNetworkResponse();
		mockResponse.setNetworkDeleted(true);

		wireMockRule.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(mockResponse.toJsonString()).withStatus(200)));

		DeleteNetworkResponse response = client.deleteNetwork(AAI_NETWORK_ID, request);
		assertEquals("Testing DeleteVfModule response", true, response.getNetworkDeleted());
	}

	@Test
	public void rollbackNetworkTest() {
		RollbackNetworkRequest request = new RollbackNetworkRequest();
		NetworkRollback rollback = new NetworkRollback();
		rollback.setCloudId(TESTING_ID);
		request.setNetworkRollback(rollback);

		RollbackNetworkResponse mockResponse = new RollbackNetworkResponse();
		mockResponse.setNetworkRolledBack(true);

		wireMockRule.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(mockResponse.toJsonString()).withStatus(200)));

		RollbackNetworkResponse response = client.rollbackNetwork(AAI_NETWORK_ID, request);
		assertEquals("Testing DeleteVfModule response", true, response.getNetworkRolledBack());
	}

	@Test
	public void queryNetworkTest() {
		QueryNetworkResponse mockResponse = new QueryNetworkResponse();
		mockResponse.setNetworkExists(true);

		wireMockRule.stubFor(get(urlPathEqualTo(REST_ENDPOINT))
				.withQueryParam("cloudSiteId", equalTo(TESTING_ID))
				.withQueryParam("tenantId", equalTo(TESTING_ID))
				.withQueryParam("networkStackId", equalTo("networkStackId"))
				.withQueryParam("skipAAI", equalTo("true"))
				.withQueryParam("msoRequest.requestId", equalTo("testRequestId"))
				.withQueryParam("msoRequest.serviceInstanceId", equalTo("serviceInstanceId"))
				.willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(mockResponse.toJsonString()).withStatus(200)));

		QueryNetworkResponse response = client.queryNetwork(AAI_NETWORK_ID, TESTING_ID, TESTING_ID,
				"networkStackId", true, "testRequestId", "serviceInstanceId");
		assertEquals("Testing QueryVfModule response", true, response.getNetworkExists());
	}

	@Ignore // 1802 merge
	@Test
	public void updateNetworkTest() {
		UpdateNetworkRequest request = new UpdateNetworkRequest();
		request.setCloudSiteId(TESTING_ID);
		request.setNetworkId("test1");

		UpdateNetworkResponse mockResponse = new UpdateNetworkResponse();
		mockResponse.setNetworkId("test1");
		wireMockRule.stubFor(put(urlPathEqualTo(REST_ENDPOINT + "/" + AAI_NETWORK_ID)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(mockResponse.toJsonString()).withStatus(200)));

		UpdateNetworkResponse response = client.updateNetwork(AAI_NETWORK_ID, request);
		assertEquals("Testing UpdateVfModule response", "test1", response.getNetworkId());
	}
}
