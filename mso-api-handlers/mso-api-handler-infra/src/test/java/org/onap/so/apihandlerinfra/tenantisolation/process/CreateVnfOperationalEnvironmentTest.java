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

package org.onap.so.apihandlerinfra.tenantisolation.process;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.onap.so.client.aai.AAIVersion;
import org.onap.so.client.aai.objects.AAIOperationalEnvironment;
import org.onap.so.client.grm.beans.Property;
import org.onap.so.client.grm.beans.ServiceEndPointList;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;


public class CreateVnfOperationalEnvironmentTest extends BaseTest{
	
	private CloudOrchestrationRequest request;
	private ServiceEndPointList serviceEndpoints;
	
	@Autowired
	private CreateVnfOperationalEnvironment createVnfOpEnv;

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
	public void testGetEnvironmentName() {
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
	public void testExecute() throws ApiException, JsonProcessingException {
		stubFor(get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("vnfoperenv/ecompOperationalEnvironment.json").withStatus(HttpStatus.SC_ACCEPTED)));
		stubFor(post(urlPathMatching("/GRMLWPService/v1/serviceEndPoint/findRunning"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("vnfoperenv/endpoints.json").withStatus(HttpStatus.SC_ACCEPTED)));
		stubFor(post(urlPathMatching("/GRMLWPService/v1/serviceEndPoint/add"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		stubFor(put(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("vnfoperenv/ecompOperationalEnvironment.json").withStatus(HttpStatus.SC_ACCEPTED)));
		String requestId = UUID.randomUUID().toString();
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId(requestId);
		iar.setOperationalEnvName("myOpEnv");
		iar.setRequestScope("create");
		iar.setRequestStatus("PENDING");
		iar.setRequestAction("UNKNOWN");
		ObjectMapper mapper = new ObjectMapper();
		stubFor(post(urlPathEqualTo("/infraActiveRequests/"))
				.withRequestBody(containing("{\"requestId\":\""+ requestId+"\",\"clientRequestId\":null,\"action\":null,\"requestStatus\":\"COMPLETE\",\"statusMessage\":\"SUCCESSFUL"))
				.willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
						.withStatus(HttpStatus.SC_OK)));

		stubFor(get(urlPathEqualTo("/infraActiveRequests/"+requestId))
				.willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
						.withBody(mapper.writeValueAsString(iar))
						.withStatus(HttpStatus.SC_OK)));
		createVnfOpEnv.execute(requestId, request);
	}
}
