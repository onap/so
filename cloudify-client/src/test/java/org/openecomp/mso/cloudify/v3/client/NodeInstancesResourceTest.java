/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.cloudify.v3.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.mso.cloudify.connector.http.HttpClientException;
import org.openecomp.mso.cloudify.v3.client.NodeInstancesResource.GetNodeInstance;
import org.openecomp.mso.cloudify.v3.client.NodeInstancesResource.ListNodeInstances;
import org.openecomp.mso.cloudify.v3.client.NodeInstancesResource.UpdateNodeInstance;
import org.openecomp.mso.cloudify.v3.model.NodeInstance;
import org.openecomp.mso.cloudify.v3.model.NodeInstances;
import org.openecomp.mso.cloudify.v3.model.UpdateNodeInstanceParams;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class NodeInstancesResourceTest {
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void nodeInstanceGet() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/node-instances/")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{ \"node_instance\": { \"id\": \"123\" } }")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		NodeInstancesResource nir = c.nodeInstances();
		GetNodeInstance gni = nir.byId("123");
		NodeInstance ni = gni.execute();
		assertEquals("123", ni.getId());
	}

	@Test
	public void nodeInstanceList() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/node-instances")).willReturn(aResponse().withHeader("Content-Type", "application/json")
//				.withBody(" { \"items\": [ { \"node_instance\": { \"id\": \"123\" } } ] } ")
				.withBody(" { \"items\": [ { \"id\": \"123\" } ] } ")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		NodeInstancesResource nir = c.nodeInstances();
		ListNodeInstances lni = nir.list();
		NodeInstances ni = lni.execute();
		assertEquals("123", ni.getItems().get(0).getId());
	}
	
	@Test
	public void nodeInstanceUpdate() {
		wireMockRule.stubFor(patch(urlPathEqualTo("/api/v3/node-instances/")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{ \"node_instance\": { \"id\": \"123\" } }")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		NodeInstancesResource nir = c.nodeInstances();
		UpdateNodeInstanceParams params = new UpdateNodeInstanceParams();

		UpdateNodeInstance uni = nir.update("123", params);
		thrown.expect(HttpClientException.class); /// ???????
		NodeInstance ni = uni.execute();
	}
}
