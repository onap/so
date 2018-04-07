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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.mso.cloudify.connector.http.HttpClientException;
import org.openecomp.mso.cloudify.v3.client.ExecutionsResource.CancelExecution;
import org.openecomp.mso.cloudify.v3.client.ExecutionsResource.GetExecution;
import org.openecomp.mso.cloudify.v3.client.ExecutionsResource.ListExecutions;
import org.openecomp.mso.cloudify.v3.client.ExecutionsResource.StartExecution;
import org.openecomp.mso.cloudify.v3.client.ExecutionsResource.UpdateExecution;
import org.openecomp.mso.cloudify.v3.model.CancelExecutionParams;
import org.openecomp.mso.cloudify.v3.model.Execution;
import org.openecomp.mso.cloudify.v3.model.Executions;
import org.openecomp.mso.cloudify.v3.model.StartExecutionParams;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class ExecutionsResourceTest {
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void cloudifyClientExecutions() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/executions")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{\"items\": [{ \"id\": \"345\" }, { \"id\": \"123\" }], \"metadata\": {\"pagination\": {\"total\": 100, \"offset\": 0, \"size\": 25}}}")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		ExecutionsResource xr = c.executions();
		ListExecutions lx = xr.list();
		Executions x = lx.execute();
		assertEquals("123", x.getItems().get(1).getId());
	}

	@Test
	public void cloudifyClientExecutionsSorted() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/executions")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{\"items\": [{ \"id\": \"123\" }, { \"id\": \"345\" }], \"metadata\": {\"pagination\": {\"total\": 100, \"offset\": 0, \"size\": 25}}}")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		ExecutionsResource xr = c.executions();
		ListExecutions lx = xr.listSorted("id");
		Executions x = lx.execute();
		assertEquals("345", x.getItems().get(1).getId());
	}

	@Test
	public void cloudifyClientExecutionsFilter() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/executions")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{\"items\": [{ \"id\": \"121\" }, { \"id\": \"123\" }], \"metadata\": {\"pagination\": {\"total\": 100, \"offset\": 0, \"size\": 25}}}")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		ExecutionsResource xr = c.executions();
		ListExecutions lx = xr.listFiltered("a=b", "id");
		Executions x = lx.execute();
		assertEquals("123", x.getItems().get(1).getId());
	}

	@Test
	public void cloudifyClientExecutionById() {
		wireMockRule.stubFor(get(urlPathEqualTo("/api/v3/executions")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{ \"id\": \"123\" }")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		ExecutionsResource xr = c.executions();
		GetExecution gx = xr.byId("123");
		Execution x = gx.execute();
		assertEquals("123", x.getId());
	}

	@Test
	public void cloudifyClientStartExecution() {
		wireMockRule.stubFor(post(urlPathEqualTo("/api/v3/executions")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{ \"id\": \"123\" }")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		ExecutionsResource xr = c.executions();

		StartExecutionParams params = new StartExecutionParams();
		StartExecution sx = xr.start(params);
		Execution x = sx.execute();
		assertEquals("123", x.getId());
	}

	@Test
	public void cloudifyClientUpdateExecution() {
		thrown.expect(HttpClientException.class);
		thrown.expectMessage("Unrecognized HTTP Method: PATCH");
		
		wireMockRule.stubFor(patch(urlPathEqualTo("/api/v3/executions")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{ \"id\": \"123\" }")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		ExecutionsResource xr = c.executions();

		UpdateExecution ux = xr.updateStatus("123", "good");
		Execution x = ux.execute();
		assertEquals("123", x.getId());
	}

	@Test
	public void cloudifyClientCancelExecution() {
		wireMockRule.stubFor(post(urlPathEqualTo("/api/v3/executions")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody("{ \"id\": \"123\" }")
				.withStatus(HttpStatus.SC_OK)));
		
		int port = wireMockRule.port();

		Cloudify c = new Cloudify("http://localhost:"+port, "tenant");
		ExecutionsResource xr = c.executions();

		CancelExecutionParams params = new CancelExecutionParams();
		CancelExecution cx = xr.cancel("123", params);
		Execution x = cx.execute();
		assertEquals("123", x.getId());
	}

	

}
