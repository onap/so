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

package org.onap.so.asdc.client.test.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Spy;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.client.test.emulators.DistributionClientEmulator;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
import org.onap.so.db.catalog.beans.AllottedResource;
import org.onap.so.db.catalog.beans.AllottedResourceCustomization;
import org.onap.so.db.catalog.data.repository.AllottedResourceRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ASDCRestInterfaceTest extends BaseTest {

	@Autowired
	private AllottedResourceRepository allottedRepo;

	@Autowired
	private ServiceRepository serviceRepo; 
	
	@Autowired
	private NetworkResourceRepository networkRepo;
	
	@Autowired
	private ASDCRestInterface asdcRestInterface;

	private TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

	private HttpHeaders headers = new HttpHeaders();
	
	@Spy
	DistributionClientEmulator spyClient = new DistributionClientEmulator();

	@LocalServerPort
	private int port;
	
	
	@Rule
	public TemporaryFolder folder= new TemporaryFolder();


	@Before
	public void setUp() {
		//ASDC Controller writes to this path
		System.setProperty("mso.config.path", folder.getRoot().toString());
	}
		
	@Test
	@Transactional
	public void testAllottedResourceService() throws Exception {
		
		wireMockServer.stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		ObjectMapper mapper = new ObjectMapper();
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/allottedresource/notif-portm.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/allottedresource/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	
		
		AllottedResource expectedService = new AllottedResource();
		expectedService.setDescription("rege1802pnf");
		expectedService.setModelInvariantUUID("b8f83c3f-077c-4e2c-b489-c66382060436");
		expectedService.setModelName("rege1802pnf");
		expectedService.setModelUUID("5b18c75e-2d08-4bf2-ad58-4ea704ec648d");
		expectedService.setModelVersion("1.0");
		expectedService.setSubcategory("Contrail Route");
		expectedService.setToscaNodeType("org.openecomp.resource.pnf.Rege1802pnf");
		Set<AllottedResourceCustomization> arCustomizationSet = new HashSet<AllottedResourceCustomization>();
		AllottedResourceCustomization arCustomization = new AllottedResourceCustomization();
		arCustomization.setModelCustomizationUUID("f62bb612-c5d4-4406-865c-0abec30631ba");
		arCustomization.setModelInstanceName("rege1802pnf 0");
		arCustomizationSet.add(arCustomization);
		
		arCustomization.setAllottedResource(expectedService);
		
		
		expectedService.setAllotedResourceCustomization(arCustomizationSet);	

		AllottedResource actualResponse = allottedRepo.findResourceByModelUUID("5b18c75e-2d08-4bf2-ad58-4ea704ec648d");
				
		
		if(actualResponse == null)
			throw new Exception("No Allotted Resource Written to database");
		

		assertThat(actualResponse, sameBeanAs(expectedService).ignoring("0x1.created").ignoring("0x1.allotedResourceCustomization.created"));
	}
	
	@Test
	public void invokeASDCStatusDataNullTest() {
		String request = "";
		Response response = asdcRestInterface.invokeASDCStatusData(request);
		assertNull(response);
		
	}
	
	
	
	

	protected String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
}
