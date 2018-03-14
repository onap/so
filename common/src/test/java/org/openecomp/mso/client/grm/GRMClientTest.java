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

package org.openecomp.mso.client.grm;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openecomp.mso.client.grm.exceptions.GRMClientCallFailed;
import org.openecomp.mso.client.grm.GRMClient;
import org.openecomp.mso.client.grm.beans.ServiceEndPointRequest;
import org.openecomp.mso.client.grm.beans.OperationalInfo;
import org.openecomp.mso.client.grm.beans.Property;
import org.openecomp.mso.client.grm.beans.ServiceEndPoint;
import org.openecomp.mso.client.grm.beans.ServiceEndPointList;
import org.openecomp.mso.client.grm.beans.ServiceEndPointLookupRequest;
import org.openecomp.mso.client.grm.beans.Version;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public class GRMClientTest {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(28090));
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testFind() throws Exception {
		String endpoints = getFileContentsAsString("__files/grm/endpoints.json");
		wireMockRule.stubFor(post(urlPathEqualTo("/GRMLWPService/v1/serviceEndPoint/findRunning"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody(endpoints)));

		
		GRMClient client = new GRMClient();
		ServiceEndPointList sel = client.findRunningServices("TEST.ECOMP_PSL.*", 1, "TEST");
		List<ServiceEndPoint> list = sel.getServiceEndPointList();
		assertEquals(3, list.size());
	}
	
	@Test(expected = GRMClientCallFailed.class) 
	public void testFindFail() throws Exception {
		
		wireMockRule.stubFor(post(urlPathEqualTo("/GRMLWPService/v1/serviceEndPoint/findRunning"))
			.willReturn(aResponse()
				.withStatus(400)
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody("")));
		
		GRMClient client = new GRMClient();
		client.findRunningServices("TEST.ECOMP_PSL.*", 1, "TEST");
	}
	
	@Ignore
	@Test
	public void testAdd() throws Exception {
		
		wireMockRule.stubFor(post(urlPathEqualTo("/GRMLWPService/v1/serviceEndPoint/add"))
			.willReturn(aResponse()
				.withStatus(202)
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody("test")));
		wireMockRule.addMockServiceRequestListener((request, response) -> {
			System.out.println("URL Requested => " + request.getAbsoluteUrl());
			System.out.println("Request Body => " + request.getBodyAsString());
			System.out.println("Request Headers => " + request.getHeaders().toString());
			System.out.println("Response Status => " + response.getStatus());
			System.out.println("Response Body => " + response.getBodyAsString());
		});	
		
		Version ver = new Version();
		ver.setMajor(1);
		ver.setMinor(0);
		ver.setPatch("0");

		ServiceEndPoint sep = new ServiceEndPoint();
		sep.setName("TEST.ECOMP_PSL.Inventory");
		sep.setVersion(ver);
		sep.setHostAddress("127.0.0.1");
		sep.setListenPort("8080");
		sep.setLatitude("37.7022");
		sep.setLongitude("121.9358");
		sep.setContextPath("/");
		sep.setRouteOffer("TEST");
		
		OperationalInfo operInfo = new OperationalInfo();
		operInfo.setCreatedBy("edge");
		operInfo.setUpdatedBy("edge");
		
		sep.setOperationalInfo(operInfo);
		
		Property prop1 = new Property();
		prop1.setName("Environment");
		prop1.setValue("TEST");
		
		Property prop2 = new Property();
		prop2.setName("cpfrun_cluster_name");
		prop2.setValue("testcase_cluster_no_cluster");
		
		List<Property> props = new ArrayList<Property>();
		props.add(prop1);
		props.add(prop2);
		
		sep.setProperties(props);

		ServiceEndPointRequest request = new ServiceEndPointRequest();
		request.setEnv("DEV");
		request.setServiceEndPoint(sep);
		
		System.out.println("Request in JSON: " + mapper.writeValueAsString(request));
		
		GRMClient client = new GRMClient();
		client.addServiceEndPoint(request);
	}
	
	@Test(expected = GRMClientCallFailed.class)
	public void testAddFail() throws Exception {
		wireMockRule.stubFor(post(urlPathEqualTo("/GRMLWPService/v1/serviceEndPoint/add"))
				.willReturn(aResponse()
					.withStatus(404)
					.withHeader("Content-Type", MediaType.APPLICATION_JSON)
					.withBody("test")));
		ServiceEndPointRequest request = new ServiceEndPointRequest();
		GRMClient client = new GRMClient();
		client.addServiceEndPoint(request);
	}

	@Test
	public void testBuildServiceEndPointLookupRequest() {
		GRMClient client = new GRMClient();
		ServiceEndPointLookupRequest request = client.buildServiceEndPointlookupRequest("TEST.ECOMP_PSL.Inventory", 1, "DEV");
		assertEquals("TEST.ECOMP_PSL.Inventory", request.getServiceEndPoint().getName());
		assertEquals(Integer.valueOf(1), Integer.valueOf(request.getServiceEndPoint().getVersion().getMajor()));
		assertEquals("DEV", request.getEnv());
		
	}
	
	protected String getFileContentsAsString(String fileName) {
		String content = "";
		try {
			ClassLoader classLoader = this.getClass().getClassLoader();
			File file = new File(classLoader.getResource(fileName).getFile());
			content = new String(Files.readAllBytes(file.toPath()));
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Exception encountered reading " + fileName + ". Error: " + e.getMessage());
		}
		return content;
	}
}
