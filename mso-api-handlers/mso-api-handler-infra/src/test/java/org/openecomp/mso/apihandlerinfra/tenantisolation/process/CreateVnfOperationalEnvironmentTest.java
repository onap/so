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

package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.apihandlerinfra.ApiHandlerApplication;
import org.openecomp.mso.apihandlerinfra.BaseTest;
import org.openecomp.mso.apihandlerinfra.exceptions.ApiException;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;
import org.openecomp.mso.client.grm.beans.Property;
import org.openecomp.mso.client.grm.beans.ServiceEndPointList;
import org.openecomp.mso.db.request.beans.InfraActiveRequests;
import org.openecomp.mso.db.request.data.repository.InfraActiveRequestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;


public class CreateVnfOperationalEnvironmentTest extends BaseTest{
	
	private CloudOrchestrationRequest request;
	private ServiceEndPointList serviceEndpoints;
	
	@Autowired
	private CreateVnfOperationalEnvironment createVnfOpEnv;
	@Autowired
	private InfraActiveRequestsRepository infraActiveRequestsRepository;

	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(28090));
	
	@Before
	public void testSetUp() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		String jsonRequest = getFileContentsAsString("__files/vnfoperenv/createVnfOperationalEnvironmentRequest.json");
		request = mapper.readValue(jsonRequest, CloudOrchestrationRequest.class);
		String jsonServiceEndpoints = getFileContentsAsString("__files/vnfoperenv/endpoints.json");
		serviceEndpoints = mapper.readValue(jsonServiceEndpoints, ServiceEndPointList.class);
	}
	
	@Test
	public void testGetEcompManagingEnvironmentId() throws Exception { 
		createVnfOpEnv.setRequest(request);
		assertEquals("ff305d54-75b4-431b-adb2-eb6b9e5ff000", createVnfOpEnv.getEcompManagingEnvironmentId());
	}
	
	@Test
	public void testGetTenantContext() throws Exception { 
		createVnfOpEnv.setRequest(request);
		assertEquals("Test", createVnfOpEnv.getTenantContext());
	}
	
	@Test
	public void testGetEnvironmentName() throws Exception {
		createVnfOpEnv.setRequest(request);
		List<Property> props = serviceEndpoints.getServiceEndPointList().get(0).getProperties();
		assertEquals("DEV", createVnfOpEnv.getEnvironmentName(props));
	}
	
	@Test 
	public void testBuildServiceNameForVnf() throws Exception {
		createVnfOpEnv.setRequest(request);
		assertEquals("Test.VNF_E2E-IST.Inventory", createVnfOpEnv.buildServiceNameForVnf("TEST.ECOMP_PSL.Inventory"));
	}
	
	@Test
	public void testGetSearchKey() {
		createVnfOpEnv.setRequest(request);
		AAIOperationalEnvironment ecompEnv = new AAIOperationalEnvironment();
		ecompEnv.setTenantContext("Test");
		ecompEnv.setWorkloadContext("ECOMPL_PSL");
		assertEquals("Test.ECOMPL_PSL.*", createVnfOpEnv.getSearchKey(ecompEnv));
	}
	
	public String getFileContentsAsString(String fileName) {
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
	
	@Test
	public void testExecute() throws ApiException{
		wireMockRule.stubFor(get(urlPathMatching("/aai/v12/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("vnfoperenv/ecompOperationalEnvironment.json").withStatus(HttpStatus.SC_ACCEPTED)));
		wireMockRule.stubFor(post(urlPathMatching("/GRMLWPService/v1/serviceEndPoint/findRunning"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("vnfoperenv/endpoints.json").withStatus(HttpStatus.SC_ACCEPTED)));
		wireMockRule.stubFor(post(urlPathMatching("/GRMLWPService/v1/serviceEndPoint/add"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		wireMockRule.stubFor(put(urlPathMatching("/aai/v12/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("vnfoperenv/ecompOperationalEnvironment.json").withStatus(HttpStatus.SC_ACCEPTED)));
		String requestId = UUID.randomUUID().toString();
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId(requestId);
		iar.setOperationalEnvName("myOpEnv");
		iar.setRequestScope("create");
		iar.setRequestStatus("PENDING");
		iar.setRequestAction("UNKNOWN");
		infraActiveRequestsRepository.saveAndFlush(iar);
		createVnfOpEnv.execute(requestId, request);
		
		InfraActiveRequests infraActiveRequest = infraActiveRequestsRepository.findOne(requestId);
		assertNotNull(infraActiveRequest);
		assertTrue(infraActiveRequest.getStatusMessage().contains("SUCCESS"));
	}
}
