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

package org.onap.so.client.aai.entities.uri;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.client.aai.AAIClient;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.defaultproperties.DefaultAAIPropertiesImpl;
import org.onap.so.client.graphinventory.exceptions.GraphInventoryPayloadException;
import org.onap.so.client.graphinventory.exceptions.GraphInventoryUriComputationException;
import org.onap.so.client.graphinventory.exceptions.GraphInventoryUriNotFoundException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

@RunWith(MockitoJUnitRunner.class)
public class ServiceInstanceUriTest {

	private final static String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/aai/resources/";
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	 
	@Spy
	public AAIClient client;
	
	@InjectMocks
	public AAIResourcesClient aaiClient = new AAIResourcesClient();
	
	@Before
	public void beforeTest() {
		doReturn(new DefaultAAIPropertiesImpl(wireMockRule.port())).when(client).getRestProperties();
	}
	@Test
	public void found() throws IOException {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "service-instance-pathed-query.json")));
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key3");
		final Optional<String> result = instance.extractRelatedLink(content);
		final String expected = "/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3";
		assertEquals("result is equal", expected, result.get());
		
	}
	
	@Test
	public void oneKey() throws IOException, URISyntaxException, GraphInventoryUriNotFoundException, GraphInventoryPayloadException {
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key1");
		ServiceInstanceUri spy = spy(instance);
		doReturn("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3").when(spy).getObjectById(any(Object.class));
		
		final URI result = spy.build();
		final URI expected = UriBuilder.fromPath("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3").build();
		assertEquals("result is equal", expected, result);
		
	}
	
	@Test
	public void oneKeyQueryParams() throws IOException, URISyntaxException, GraphInventoryUriNotFoundException, GraphInventoryPayloadException {
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key1");
		ServiceInstanceUri spy = spy(instance);
		doReturn("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3").when(spy).getObjectById(any(Object.class));
		
		final URI result = spy.resourceVersion("1234").build();
		final URI expected = UriBuilder.fromUri("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3?resource-version=1234").build();
		assertEquals("result is equal", expected, result);
		
	}
	
	@Test
	public void oneKeyEncoded() throws IOException, URISyntaxException, GraphInventoryUriNotFoundException, GraphInventoryPayloadException {
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key1");
		ServiceInstanceUri spy = spy(instance);
		doReturn("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3%20space").when(spy).getObjectById(any(Object.class));
		
		final URI result = spy.build();
		final URI expected = UriBuilder.fromUri("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3%20space").build();
		assertEquals("result is equal", expected, result);
		
	}
	
	@Test
	public void oneKeyGetKeys() throws IOException, URISyntaxException, GraphInventoryUriNotFoundException, GraphInventoryPayloadException {
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key1");
		ServiceInstanceUri spy = spy(instance);
		doReturn("/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3%28space").when(spy).getObjectById(any(Object.class));
		
		assertThat(spy.getURIKeys().values(), contains("key1", "key2", "key3(space"));
		
	}
	@Test
	public void oneKeyClone() throws GraphInventoryUriNotFoundException, GraphInventoryPayloadException {
		ServiceInstanceUri instance = new ServiceInstanceUri("key1");
		ServiceInstanceUri spy = spy(instance);
		String uri = "/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3";
		doReturn(uri).when(spy).getObjectById(any(Object.class));
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
	public void notfound() throws IOException, GraphInventoryUriNotFoundException, GraphInventoryPayloadException {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "empty-query-result.json")));
		 
		ServiceInstanceUri instance = new ServiceInstanceUri("key3");
		ServiceInstanceUri spy = spy(instance);
		AAIResourcesClient mockResourcesClient = mock(AAIResourcesClient.class);
		AAIResultWrapper wrapper = mock(AAIResultWrapper.class);
		when(mockResourcesClient.get(ArgumentMatchers.<AAIResourceUri>any(AAIResourceUri.class), ArgumentMatchers.<Class<NotFoundException>>any())).thenReturn(wrapper);
		when(wrapper.getJson()).thenReturn(content);
		when(spy.getResourcesClient()).thenReturn(mockResourcesClient);
		exception.expect(GraphInventoryUriComputationException.class);
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
	public void noVertexFound() throws GraphInventoryUriNotFoundException, GraphInventoryPayloadException {
		ServiceInstanceUri instance = new ServiceInstanceUri("key3");
		ServiceInstanceUri spy = spy(instance);
		AAIResourcesClient client = aaiClient;
		doReturn(client).when(spy).getResourcesClient();
		stubFor(get(urlPathMatching("/aai/v[0-9]+/nodes/service-instances/service-instance/key3")) 
				.willReturn(aResponse() 
					.withStatus(404) 
					.withHeader("Content-Type", "application/json") 
					.withBodyFile("")));
		exception.expect(NotFoundException.class);
		spy.build();	
	}
}
