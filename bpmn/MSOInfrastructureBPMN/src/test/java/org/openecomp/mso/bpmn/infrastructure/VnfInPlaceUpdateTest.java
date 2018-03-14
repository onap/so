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

package org.openecomp.mso.bpmn.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockAAIVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDBUpdateVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithDepth;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithPriority;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetPserverByVnfId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfsByVnfId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockSetInMaintFlagByVnfId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetDefaultCloudRegionByCloudRegionId;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.MockGetServiceResourcesCatalogData;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponsePolicy.MockPolicySkip;
import static org.openecomp.mso.bpmn.mock.StubResponseAPPC.MockAppcError;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;
import org.openecomp.mso.bpmn.mock.FileUtil;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;

/**
 * Unit test cases for VnfInPlaceUpdate.bpmn
 */
public class VnfInPlaceUpdateTest extends WorkflowTest {	

	public VnfInPlaceUpdateTest() throws IOException {
		
	}
	
	/**
	 * Sunny day scenario.
	 * 
	 * @throws Exception
	 */
	@Test	

	
	@Deployment(resources = {
		"process/VnfInPlaceUpdate.bpmn",		
		"subprocess/CompleteMsoProcess.bpmn",
		"subprocess/FalloutHandler.bpmn",		
		"subprocess/BuildingBlock/RainyDayHandler.bpmn",
		"subprocess/BuildingBlock/ManualHandling.bpmn",
		"subprocess/BuildingBlock/AppCClient.bpmn"
		
		})
	public void sunnyDay() throws Exception {
				
		logStart();
		System.setProperty("mso.config.path", "src/test/resources");
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "skask");
		wireMockRule.stubFor(get(
				urlPathEqualTo("/aai/v11" + path.build()))
				.willReturn(
					aResponse()
					.withHeader("Content-Type", "application/json")
					.withBodyFile("AAI/mockObject.json")
					.withStatus(200)));
		
		//
		
		//AAIResourceUri path1 = AAIUriFactory.createResourceUri(AAIObjectType.CUSTOM_QUERY).queryParam("format", "RESOURCE");
		//wireMockRule.stubFor(put(
		//		urlMatching("/aai/v10/query/pservers-fromVnf"))
		//		.willReturn(
		//			aResponse()
		//			.withHeader("Content-Type", "application/json")
		//			.withBodyFile("AAI/AAI_pserversByVnfId.json")
		//			.withStatus(200)));
		
		MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlByIdVipr.xml");
		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");
		//MockGetGenericVnfById_404("testVnfId");		
		MockGetGenericVnfByIdWithDepth("skask", 1, "AAI/AAI_genericVnfsByVnfId.json");		
		MockAAIVfModule();		
		MockDBUpdateVfModule();	
		MockGetPserverByVnfId("skask", "AAI/AAI_pserverByVnfId.json", 200);		
		MockSetInMaintFlagByVnfId("skask", "AAI/AAI_genericVnfsByVnfId.json", 200);
		MockGetGenericVnfsByVnfId("skask", "AAI/AAI_genericVnfsByVnfId.json", 200);
		MockGetDefaultCloudRegionByCloudRegionId("mdt1", "AAI/AAI_defaultCloudRegionByCloudRegionId.json", 200);
		MockPolicySkip();
		MockAppcError();		
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		String updaetVnfRequest =
			FileUtil.readResourceFile("__files/InfrastructureFlows/VnfInPlaceUpdate_VID_request.json");
		
		Map<String, Object> variables = setupVariablesSunnyDayVID();
		
		
		TestAsyncResponse asyncResponse = invokeAsyncProcess("VnfInPlaceUpdate",
			"v1", businessKey, updaetVnfRequest, variables);
		
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 100000);
		
		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);		
	
		// TODO add appropriate assertions

		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "VnfInPlaceUpdateSuccessIndicator", true);
		
		logEnd();
	}
	
	// Active Scenario
	private Map<String, Object> setupVariablesSunnyDayVID() {
				Map<String, Object> variables = new HashMap<String, Object>();
				
				variables.put("requestId", "testRequestId");				
				variables.put("isDebugLogEnabled", "true");				
				variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
				variables.put("vnfId", "skask");			
						
				return variables;
				
			}
	
}
