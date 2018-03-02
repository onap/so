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

package org.openecomp.mso.client.aai.entities.uri;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.UriBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.AAIQueryClient;
import org.openecomp.mso.client.aai.Format;
import org.openecomp.mso.client.aai.entities.CustomQuery;
import org.openecomp.mso.client.aai.exceptions.AAIPayloadException;
import org.openecomp.mso.client.aai.exceptions.AAIUriComputationException;
import org.openecomp.mso.client.aai.exceptions.AAIUriNotFoundException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class ServiceInstanceUriTest {

	private final static String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/aai/resources/";
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8443));
	
	 @Rule
	 public final ExpectedException exception = ExpectedException.none();
	 
	@Test
	public void found() throws IOException {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "service-instance-pathed-query.json")));
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key3");
		final Optional<String> result = instance.extractRelatedLink(content);
		final String expected = "/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3";
		assertEquals("result is equal", expected, result.get());
		
	}
	
	@Test
	public void oneKey() throws IOException, URISyntaxException, AAIUriNotFoundException, AAIPayloadException {
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key1");
		ServiceInstanceUri spy = spy(instance);
		doReturn("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3").when(spy).getSerivceInstance(any(Object.class));
		
		final URI result = spy.build();
		final URI expected = UriBuilder.fromPath("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3").build();
		assertEquals("result is equal", expected, result);
		
	}
	
	@Test
	public void oneKeyQueryParams() throws IOException, URISyntaxException, AAIUriNotFoundException, AAIPayloadException {
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key1");
		ServiceInstanceUri spy = spy(instance);
		doReturn("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3").when(spy).getSerivceInstance(any(Object.class));
		
		final URI result = spy.resourceVersion("1234").build();
		final URI expected = UriBuilder.fromUri("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3?resource-version=1234").build();
		assertEquals("result is equal", expected, result);
		
	}
	
	@Test
	public void oneKeyEncoded() throws IOException, URISyntaxException, AAIUriNotFoundException, AAIPayloadException {
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key1");
		ServiceInstanceUri spy = spy(instance);
		doReturn("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3%20space").when(spy).getSerivceInstance(any(Object.class));
		
		final URI result = spy.build();
		final URI expected = UriBuilder.fromUri("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3%20space").build();
		assertEquals("result is equal", expected, result);
		
	}
	
	@Test
	public void oneKeyGetKeys() throws IOException, URISyntaxException, AAIUriNotFoundException, AAIPayloadException {
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key1");
		ServiceInstanceUri spy = spy(instance);
		doReturn("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3%28space").when(spy).getSerivceInstance(any(Object.class));
		
		assertThat(spy.getURIKeys().values(), contains("key1", "key2", "key3(space"));
		
	}
	@Test
	public void oneKeyClone() throws AAIUriNotFoundException, AAIPayloadException {
		ServiceInstanceUri instance = new ServiceInstanceUri("key1");
		ServiceInstanceUri spy = spy(instance);
		String uri = "/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3";
		doReturn(uri).when(spy).getSerivceInstance(any(Object.class));
		doReturn(Optional.of(uri)).when(spy).getCachedValue();
		final URI result = spy.resourceVersion("1234").clone().build();
		final URI expected = UriBuilder.fromUri("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3?resource-version=1234").build();
		assertEquals("result is equal", expected, result);
	}
	
	@Test
	public void threeKey() throws IOException {
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key1", "key2", "key3");
		final URI result = instance.build();
		final URI expected = UriBuilder.fromPath("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3").build();
		assertEquals("result is equal", expected, result);
		
	}
	
	@Test
	public void notfound() throws IOException, AAIUriNotFoundException, AAIPayloadException {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "empty-query-result.json")));
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key3");
		ServiceInstanceUri spy = spy(instance);
		AAIQueryClient mockQueryClient = mock(AAIQueryClient.class);
		when(mockQueryClient.query(any(Format.class), any(CustomQuery.class))).thenReturn(content);
		when(spy.getQueryClient()).thenReturn(mockQueryClient);
		exception.expect(AAIUriComputationException.class);
		spy.build();
		
	}
	
	@Test
	public void cloneTest() {
		ServiceInstanceUri instance = new ServiceInstanceUri("key1", "key2", "key3");
		final URI result = instance.build();
		final URI result2 = instance.clone().queryParam("something", "new").build();
		assertEquals("uris are not equal", false, result.toString().equals(result2.toString()));
		
	}
	
	@Test
	public void noVertexFound() throws AAIUriNotFoundException, AAIPayloadException {
		ServiceInstanceUri instance = new ServiceInstanceUri("key3");
		ServiceInstanceUri spy = spy(instance);
		stubFor(put(urlMatching("/aai/v[0-9]+/query.*")) 
				.withRequestBody(containing("key3")) 
				.willReturn(aResponse() 
					.withStatus(400) 
					.withHeader("Content-Type", "application/json") 
					.withBodyFile("")));
		exception.expect(AAIUriComputationException.class);
		exception.expectMessage(containsString("NotFoundException"));
		spy.build();	
	}
}
