/*
* ============LICENSE_START=======================================================
 * ONAP : SO
 * ================================================================================
 * Copyright (C) 2018 TechMahindra
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

package org.onap.so.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class RESTClientTest {

	private RESTClient restClient;
	private JSONObject jsonPayload;
	private JSONObject jsonResponse;
	private String jsonObjectAsString;
	private String jsonResponseAsString;
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());	
	
	
	@Before
	public void before() throws Exception {
		jsonPayload = new JSONObject();
		jsonPayload.put("firstName", "firstName1");
		jsonPayload.put("lastName", "lastName1");
		jsonObjectAsString = jsonPayload.toString();
		jsonResponse = new JSONObject();
		jsonResponse.put("response", "responseValue");
		jsonResponseAsString = jsonResponse.toString(); 
		restClient = new RESTClient("http://localhost:" + wireMockRule.port() + "/example", "localhost", wireMockRule.port());
	}	
	
	@Test
	public void testHeadersParameters() throws Exception {
		restClient.setHeader("name", "value");
		restClient.setParameter("name", "value");
		assertEquals("[value]", restClient.getParameters().get("name").toString());		
		assertEquals("[value]", restClient.getHeaders().get("name").toString());
		restClient.setHeader("name", "value2");
		assertEquals("[value2]", restClient.getHeaders().get("name").toString());
		restClient.setParameter("name", "value2");
		assertEquals("[value2]", restClient.getParameters().get("name").toString());
		restClient.addParameter("name", "value");  
		assertEquals(1, restClient.getParameters().size());
		restClient.addAuthorizationHeader("token");  
		assertEquals("[token]", restClient.getHeaders().get("Authorization").toString());		
		assertEquals("http://localhost:" + wireMockRule.port() + "/example", restClient.getURL());
		restClient = new RESTClient("http://localhost:" + wireMockRule.port() + "/example1");
		assertEquals("http://localhost:" + wireMockRule.port() + "/example1", restClient.getURL());
	}
	
	@Test
	public void testHttpPost() throws Exception {
		RESTClient restClientMock = mock(RESTClient.class);
		restClientMock = spy(restClient);
		wireMockRule.stubFor(post(urlPathMatching("/example/*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.OK.value()).withBody(jsonResponseAsString)));		
		APIResponse apiResponse = restClientMock.httpPost(jsonObjectAsString);
		assertEquals(200, apiResponse.getStatusCode());
		assertEquals(jsonResponseAsString, apiResponse.getResponseBodyAsString());
		assertEquals("application/json", apiResponse.getFirstHeader("Content-Type"));
		verify(restClientMock, times(2)).getURL();
	}	
	
	@Test
	public void testPost() throws Exception {
		wireMockRule.stubFor(post(urlPathMatching("/example/*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.OK.value()).withBody(jsonResponseAsString)));		
		APIResponse apiResponse = restClient.post();
		assertEquals(200, apiResponse.getStatusCode());
		assertEquals(jsonResponseAsString, apiResponse.getResponseBodyAsString());
		assertEquals("application/json", apiResponse.getFirstHeader("Content-Type"));
	}	
	
	@Test
	public void testHttpPut() throws Exception {
		wireMockRule.stubFor(put(urlPathMatching("/example/*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.OK.value()).withBody(jsonResponseAsString)));		
		restClient.setParameter("name", "value");
		APIResponse apiResponse = restClient.httpPut(jsonObjectAsString);
		assertEquals(200, apiResponse.getStatusCode());
		assertEquals(jsonResponseAsString, apiResponse.getResponseBodyAsString());
		assertEquals("application/json", apiResponse.getFirstHeader("Content-Type"));
		
	}		
	
	@Test
	public void testHttpPatch() throws Exception {
		wireMockRule.stubFor(patch(urlPathMatching("/example/*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.OK.value()).withBody(jsonResponseAsString)));				
		APIResponse apiResponse = restClient.httpPatch(jsonObjectAsString);
		assertEquals(200, apiResponse.getStatusCode());
		assertEquals(jsonResponseAsString, apiResponse.getResponseBodyAsString());
		assertEquals("application/json", apiResponse.getFirstHeader("Content-Type"));
	}	

	@Test
	public void testPatch_withParameter() throws Exception {
		wireMockRule.stubFor(patch(urlPathMatching("/example/*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.OK.value()).withBody(jsonResponseAsString)));				
		restClient.setParameter("name", "value");
		APIResponse apiResponse = restClient.patch(jsonObjectAsString);
		assertEquals(200, apiResponse.getStatusCode());
		assertEquals(jsonResponseAsString, apiResponse.getResponseBodyAsString());
		assertEquals("application/json", apiResponse.getFirstHeader("Content-Type"));
	}		
	
	@Test
	public void testHttpDelete_withPayload() throws Exception {
		wireMockRule.stubFor(delete(urlPathMatching("/example/*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.OK.value()).withBody(jsonResponseAsString)));				
		APIResponse apiResponse = restClient.httpDelete(jsonObjectAsString);
		assertEquals(200, apiResponse.getStatusCode());
		assertEquals(jsonResponseAsString, apiResponse.getResponseBodyAsString());
		assertEquals("application/json", apiResponse.getFirstHeader("Content-Type"));
	}	
	
	@Test
	public void testHttpDelete() throws Exception {
		wireMockRule.stubFor(delete(urlPathMatching("/example/*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.OK.value()).withBody(jsonResponseAsString)));				
		APIResponse apiResponse = restClient.httpDelete();
		assertEquals(200, apiResponse.getStatusCode());
		assertEquals(jsonResponseAsString, apiResponse.getResponseBodyAsString());
		assertEquals("application/json", apiResponse.getFirstHeader("Content-Type"));
	}	
	
	@Test
	public void testDelete() throws Exception {
		wireMockRule.stubFor(delete(urlPathMatching("/example/*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.OK.value()).withBody(jsonResponseAsString)));				
		APIResponse apiResponse = restClient.delete();
		assertEquals(200, apiResponse.getStatusCode());
		assertEquals(jsonResponseAsString, apiResponse.getResponseBodyAsString());
		assertEquals("application/json", apiResponse.getFirstHeader("Content-Type"));
	}	
	
	@Test
	public void testHttpGet() throws Exception {
		wireMockRule.stubFor(get(urlPathMatching("/example/*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.OK.value()).withBody(jsonResponseAsString)));				
		APIResponse apiResponse = restClient.httpGet();
		assertEquals(200, apiResponse.getStatusCode());
		assertEquals(jsonResponseAsString, apiResponse.getResponseBodyAsString());
		assertEquals("application/json", apiResponse.getFirstHeader("Content-Type"));
	}	
	
	@Test
	public void testGet_withParameter() throws Exception {
		wireMockRule.stubFor(get(urlPathMatching("/example/*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.OK.value()).withBody(jsonResponseAsString)));				
		restClient.setParameter("name", "value");
		restClient.setParameter("type", "valueType");
		APIResponse apiResponse = restClient.get();
		assertEquals(200, apiResponse.getStatusCode());
		assertEquals(jsonResponseAsString, apiResponse.getResponseBodyAsString());
		assertEquals("application/json", apiResponse.getFirstHeader("Content-Type"));
	}	
	
}
