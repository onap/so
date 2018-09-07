/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.aai;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.entities.uri.ServiceInstanceUri;
import org.onap.so.client.defaultproperties.DefaultAAIPropertiesImpl;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class AAIResourcesClientWithServiceInstanceUriTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8443));
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private ServiceInstanceUri uri;
	@Before
	public void setUp() {
		wireMockRule.stubFor(get(urlMatching("/aai/v[0-9]+/nodes.*")) 
				.willReturn(aResponse() 
					.withStatus(404) 
					.withHeader("Content-Type", "application/json")
					.withHeader("Mock", "true")));
		
		uri = spy((ServiceInstanceUri)AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, "id"));
		doReturn(createClient()).when(uri).getResourcesClient();
	}
	
	@Test
	public void getWithClass() {
		AAIResourcesClient client = createClient();
		Optional<String> result = client.get(String.class, uri);
		
		assertThat(result.isPresent(), equalTo(false));
	}
	
	@Test
	public void getFullResponse() {
		AAIResourcesClient client = createClient();
		Response result = client.getFullResponse(uri);
		assertThat(result.getStatus(), equalTo(Status.NOT_FOUND.getStatusCode()));
	}
	
	@Test
	public void getWithGenericType() {
		AAIResourcesClient client = createClient();
		Optional<List<String>> result = client.get(new GenericType<List<String>>() {}, uri);
		assertThat(result.isPresent(), equalTo(false));
	}
	
	@Test
	public void getAAIWrapper() {
		AAIResourcesClient client = createClient();
		AAIResultWrapper result = client.get(uri);
		assertThat(result.isEmpty(), equalTo(true));
	}
	
	@Test
	public void getWithException() {
		AAIResourcesClient client = createClient();
		this.thrown.expect(IllegalArgumentException.class);
		AAIResultWrapper result = client.get(uri, IllegalArgumentException.class);
	}
	
	@Test
	public void existsTest() {
		AAIResourcesClient client = createClient();
		doReturn(uri).when(uri).clone();
		boolean result = client.exists(uri);
		assertThat(result, equalTo(false));
	}
	private AAIResourcesClient createClient() {
		AAIResourcesClient client = spy(new AAIResourcesClient());
		doReturn(new DefaultAAIPropertiesImpl()).when(client).getRestProperties();
		return client;
	}
}
