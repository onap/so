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

package org.openecomp.mso.cloudify.connector.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.hamcrest.CoreMatchers.*;
import org.openecomp.mso.cloudify.base.client.CloudifyRequest;
import org.openecomp.mso.cloudify.base.client.CloudifyResponseException;
import org.openecomp.mso.cloudify.base.client.HttpMethod;
import org.openecomp.mso.cloudify.v3.model.Deployment;

public class HttpClientConnectorTest {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void sunnyDay_POST(){			
		wireMockRule.stubFor(post(urlPathEqualTo("/testUrl")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody("TEST").withStatus(HttpStatus.SC_OK)));
		int port = wireMockRule.port();
		HttpClientConnector conector = new HttpClientConnector();
		CloudifyRequest<Deployment> request = new CloudifyRequest<Deployment>();
		Deployment deployment = new Deployment();
		deployment.setId("id");
		request.entity(deployment, "application/json");
		request.endpoint("http://localhost:"+port+"/testUrl");
		request.setBasicAuthentication("USER","PASSWORD");
		request.header("Content-Type","application/json");
		request.method(HttpMethod.POST);
		conector.request(request);
		verify(postRequestedFor(urlEqualTo("/testUrl")));
	}
	
	
	@Test
	public void sunnyDay_GET(){			
		wireMockRule.stubFor(get(urlPathEqualTo("/testUrl")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody("TEST").withStatus(HttpStatus.SC_OK)));
		int port = wireMockRule.port();
		HttpClientConnector conector = new HttpClientConnector();
		CloudifyRequest<Deployment> request = new CloudifyRequest<Deployment>();
		request.endpoint("http://localhost:"+port+"/testUrl");
		request.setBasicAuthentication("USER","PASSWORD");
		request.method(HttpMethod.GET);
		conector.request(request);
		verify(getRequestedFor(urlEqualTo("/testUrl")));
	}
	
	@Test
	public void sunnyDay_PUT(){			
		wireMockRule.stubFor(put(urlPathEqualTo("/testUrl")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody("TEST").withStatus(HttpStatus.SC_OK)));
		int port = wireMockRule.port();
		HttpClientConnector conector = new HttpClientConnector();
		CloudifyRequest<Deployment> request = new CloudifyRequest<Deployment>();
		request.endpoint("http://localhost:"+port+"/testUrl");
		request.setBasicAuthentication("USER","PASSWORD");
		request.method(HttpMethod.PUT);
		conector.request(request);
		verify(putRequestedFor(urlEqualTo("/testUrl")));
	}
	
	
	@Test
	public void sunnyDay_DELETE(){			
		wireMockRule.stubFor(delete(urlPathEqualTo("/testUrl")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody("TEST").withStatus(HttpStatus.SC_OK)));
		int port = wireMockRule.port();
		HttpClientConnector conector = new HttpClientConnector();
		CloudifyRequest<Deployment> request = new CloudifyRequest<Deployment>();
		request.endpoint("http://localhost:"+port+"/testUrl");
		request.setBasicAuthentication("USER","PASSWORD");
		request.method(HttpMethod.DELETE);
		conector.request(request);
		verify(deleteRequestedFor(urlEqualTo("/testUrl")));
	}
	
	
	@Test
	public void rainydDay_PATCH(){			 
		thrown.expect(HttpClientException.class);
		thrown.expectMessage("Unrecognized HTTP Method: PATCH");
		HttpClientConnector conector = new HttpClientConnector();
		CloudifyRequest<Deployment> request = new CloudifyRequest<Deployment>();
		request.endpoint("http://localhost:123123/testUrl");
		request.setBasicAuthentication("USER","PASSWORD");
		request.method(HttpMethod.PATCH);
		conector.request(request);
	
	}
	
	
	@Test
	public void rainydDay_RunTimeException(){	
		wireMockRule.stubFor(post(urlEqualTo("/503")).willReturn(
                aResponse().withStatus(503).withHeader("Content-Type", "text/plain").withBody("failure")));
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Unexpected client exception");
		HttpClientConnector conector = new HttpClientConnector();
		CloudifyRequest<Deployment> request = new CloudifyRequest<Deployment>();
		request.endpoint("http://localhost:123123/503");
		request.setBasicAuthentication("USER","PASSWORD");
		request.method(HttpMethod.POST);
		conector.request(request);
	
	}


}