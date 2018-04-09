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

package org.openecomp.mso.cloudify.base.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.mso.cloudify.v3.model.Execution;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class CloudifyClientTest {
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void clientCreate(){
		wireMockRule.stubFor(get(urlPathEqualTo("/testUrl")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody("{\"id\": \"123\"}").withStatus(HttpStatus.SC_OK)));
		int port = wireMockRule.port();
		CloudifyClient cc = new CloudifyClient("http://localhost:"+port);
		cc.setToken("token");
		CloudifyRequest<Execution> crx = cc.get("/testUrl", Execution.class);
		Execution x = crx.execute();
		assertEquals("123", x.getId());
	}

	@Test
	public void clientCreateWithBadConnector(){
		thrown.expect(CloudifyResponseException.class);
		wireMockRule.stubFor(get(urlPathEqualTo("/testUrl")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody("{\"id\": \"123\"}").withStatus(HttpStatus.SC_OK)));
		int port = wireMockRule.port();
		CloudifyClientConnector ccc = new CloudifyClientConnector(){
			@Override
			public <T> CloudifyResponse request(CloudifyRequest<T> request) {
				throw new CloudifyResponseException("test case", 401);
			}}; 
		CloudifyClient cc = new CloudifyClient("http://localhost:"+port, ccc);
//		cc.setToken("token");
		CloudifyRequest<Execution> crx = cc.get("/testUrl", Execution.class);
		Execution x = crx.execute();
	}

	@Test
	public void clientCreateWithBadConnectorAndToken(){
		thrown.expect(CloudifyResponseException.class);
		wireMockRule.stubFor(get(urlPathEqualTo("/testUrl")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody("{\"id\": \"123\"}").withStatus(HttpStatus.SC_OK)));
		int port = wireMockRule.port();
		CloudifyClientConnector ccc = new CloudifyClientConnector(){
			@Override
			public <T> CloudifyResponse request(CloudifyRequest<T> request) {
				throw new CloudifyResponseException("test case", 401);
			}}; 
		CloudifyClient cc = new CloudifyClient("http://localhost:"+port, ccc);
		cc.setToken("token");
		CloudifyRequest<Execution> crx = cc.get("/testUrl", Execution.class);
		Execution x = crx.execute();
	}

	@Test
	public void clientCreateWithTenant(){
		wireMockRule.stubFor(get(urlPathEqualTo("/testUrl")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody("{\"id\": \"123\"}").withStatus(HttpStatus.SC_OK)));
		int port = wireMockRule.port();
		CloudifyClient cc = new CloudifyClient("http://localhost:"+port, "other_tenant");
		cc.setToken("token");
		cc.property("property", "value");
		CloudifyRequest<Execution> crx = cc.get("/testUrl", Execution.class);
		Execution x = crx.execute();
		assertEquals("123", x.getId());
	}

}
