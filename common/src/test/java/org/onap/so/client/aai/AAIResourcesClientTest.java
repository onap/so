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

package org.onap.so.client.aai;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.defaultproperties.DefaultAAIPropertiesImpl;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
public class AAIResourcesClientTest {


	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8443));
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void verifyNotExists() {
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test");
		wireMockRule.stubFor(get(
				urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
				.willReturn(
					aResponse()
					.withHeader("Content-Type", "text/plain")
					.withBody("hello")
					.withStatus(404)));
		AAIResourcesClient client= createClient();
		boolean result = client.exists(path);
		assertEquals("path not found", false, result);
	}
	
	@Test
	public void verifyDelete() {
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test2");
		wireMockRule.stubFor(get(
				urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
				.willReturn(
					aResponse()
					.withHeader("Content-Type", "application/json")
					.withBodyFile("aai/resources/mockObject.json")
					.withStatus(200)));
		wireMockRule.stubFor(delete(
				urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
				.withQueryParam("resource-version", equalTo("1234"))
				.willReturn(
					aResponse()
					.withStatus(204)));
		AAIResourcesClient client= createClient();
		client.delete(path);
	}
	
	@Test
	public void verifyBasicAuth() {
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test3");
		wireMockRule.stubFor(get(
				urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build().toString()))
				.withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
				.willReturn(
					aResponse()
					.withHeader("Content-Type", "application/json")
					.withBodyFile("aai/resources/mockObject.json")
					.withStatus(200)));
		AAIResourcesClient client= createClient();
		client.get(path);
	}
	
	@Test
	public void verifyConnect() {
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test2");
		AAIResourceUri path2 = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test3");
		wireMockRule.stubFor(put(
				urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build() + "/relationship-list/relationship"))
				.willReturn(
					aResponse()
					.withHeader("Content-Type", "application/json")
					.withStatus(200)));
		
		AAIResourceUri pathClone = path.clone();
		AAIResourcesClient client= createClient();
		client.connect(path, path2);
		assertEquals("uri not modified", pathClone.build().toString(), path.build().toString());
	}
	
	@Test
	public void verifyDisconnect() {
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test2");
		AAIResourceUri path2 = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test3");
		
		wireMockRule.stubFor(post(
				urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build() + "/relationship-list/relationship"))
				.willReturn(
					aResponse()
					.withStatus(204)));
		
		AAIResourceUri pathClone = path.clone();
		AAIResourcesClient client= createClient();
		client.disconnect(path, path2);
		assertEquals("uri not modified", pathClone.build().toString(), path.build().toString());
	}
	
	@Test
	public void verifyPatch() {
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test2");
		
		wireMockRule.stubFor(post(
				urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
				.willReturn(
					aResponse()
					.withStatus(200)));
		
		AAIResourcesClient client= createClient();

		client.update(path, "{}");
	}
	
	@Test
	public void verifyNotExistsGet() {
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test");
		wireMockRule.stubFor(get(
				urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
				.willReturn(
					aResponse()
					.withHeader("Content-Type", "text/plain")
					.withBody("hello")
					.withStatus(404)));
		AAIResourcesClient client= createClient();
		AAIResultWrapper result = client.get(path);
		assertEquals("is empty", true, result.isEmpty());
	}
	
	@Test
	public void verifyNotExistsGetException() {
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test");
		wireMockRule.stubFor(get(
				urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
				.willReturn(
					aResponse()
					.withHeader("Content-Type", "text/plain")
					.withBody("hello")
					.withStatus(404)));
		AAIResourcesClient client= createClient();
		thrown.expect(NotFoundException.class);
		thrown.expectMessage(containsString(path.build() + " not found in A&AI"));
		AAIResultWrapper result = client.get(path, NotFoundException.class);
	}
	
	private AAIResourcesClient createClient() {
		AAIResourcesClient client = spy(new AAIResourcesClient());
		doReturn(new DefaultAAIPropertiesImpl()).when(client).getRestProperties();
		return client;
	}
}
