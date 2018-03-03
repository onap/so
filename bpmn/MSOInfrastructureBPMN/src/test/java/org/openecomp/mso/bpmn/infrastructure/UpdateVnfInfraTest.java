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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Unit test cases for UpdateVnfInfra.bpmn
 */
public class UpdateVnfInfraTest extends WorkflowTest {
	
	private final CallbackSet callbacks = new CallbackSet();

	public UpdateVnfInfraTest() throws IOException {
		callbacks.put("changeassign", FileUtil.readResourceFile(
				"__files/VfModularity/SDNCTopologyChangeAssignCallback.xml"));
		callbacks.put("activate", FileUtil.readResourceFile(
				"__files/VfModularity/SDNCTopologyActivateCallback.xml"));
		callbacks.put("query", FileUtil.readResourceFile(
				"__files/VfModularity/SDNCTopologyQueryCallback.xml"));		
		callbacks.put("vnfUpdate", FileUtil.readResourceFile(
				"__files/VfModularity/VNFAdapterRestUpdateCallback.xml"));
	}
	
	/**
	 * Sunny day scenario.
	 * 
	 * @throws Exception
	 */
	@Test	
	@Ignore // IGNORED FOR 1710 MERGE TO ONAP
	@Deployment(resources = {
		"process/UpdateVnfInfra.bpmn",		
		"subprocess/DoUpdateVfModule.bpmn",
		"subprocess/DoUpdateVnfAndModules.bpmn",
		"subprocess/PrepareUpdateAAIVfModule.bpmn",
		"subprocess/ConfirmVolumeGroupTenant.bpmn",
		"subprocess/SDNCAdapterV1.bpmn",
		"subprocess/VnfAdapterRestV1.bpmn",
		"subprocess/UpdateAAIGenericVnf.bpmn",
		"subprocess/UpdateAAIVfModule.bpmn",
		"subprocess/CompleteMsoProcess.bpmn",
		"subprocess/FalloutHandler.bpmn",
		"subprocess/BuildingBlock/DecomposeService.bpmn",
		"subprocess/BuildingBlock/RainyDayHandler.bpmn",
		"subprocess/BuildingBlock/ManualHandling.bpmn"
		
		})
	public void sunnyDay() throws Exception {
				
		logStart();
		
		MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlByIdVipr.xml");
		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");
		//MockGetGenericVnfById_404("testVnfId");
		MockGetServiceResourcesCatalogData("995256d2-5a33-55df-13ab-12abad84e7ff", "1.0", "VIPR/getCatalogServiceResourcesDataForUpdateVnfInfra.json");
		MockGetGenericVnfByIdWithDepth("skask", 1, "VfModularity/GenericVnf.xml");
		MockPutGenericVnf(".*");
		MockAAIVfModule();
		MockPatchGenericVnf("skask");
		MockPatchVfModuleId("skask", ".*");
		mockSDNCAdapter("VfModularity/StandardSDNCSynchResponse.xml");	
		mockVNFPut("skask", "/supercool", 202);
		mockVNFPut("skask", "/lukewarm", 202);
		MockVNFAdapterRestVfModule();
		MockDBUpdateVfModule();	
		MockGetPserverByVnfId("skask", "AAI/AAI_pserverByVnfId.json", 200);
		MockGetGenericVnfsByVnfId("skask", "AAI/AAI_genericVnfsByVnfId.json", 200);
		MockSetInMaintFlagByVnfId("skask", 200);
		MockPolicySkip();
		
		mockSDNCAdapter("VfModularity/StandardSDNCSynchResponse.xml");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		String updaetVnfRequest =
			FileUtil.readResourceFile("__files/InfrastructureFlows/UpdateVnf_VID_request.json");
		
		Map<String, Object> variables = setupVariablesSunnyDayVID();
		
		
		TestAsyncResponse asyncResponse = invokeAsyncProcess("UpdateVnfInfra",
			"v1", businessKey, updaetVnfRequest, variables);
		
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
		
		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		injectSDNCCallbacks(callbacks, "changeassign, query");
		injectVNFRestCallbacks(callbacks, "vnfUpdate");
		injectSDNCCallbacks(callbacks, "activate");
		injectSDNCCallbacks(callbacks, "changeassign, query");
		injectVNFRestCallbacks(callbacks, "vnfUpdate");
		injectSDNCCallbacks(callbacks, "activate");
		
		// TODO add appropriate assertions

		waitForProcessEnd(businessKey, 10000);
		checkVariable(businessKey, "UpdateVfModuleInfraSuccessIndicator", true);
		
		logEnd();
	}
	
	// Active Scenario
	private Map<String, Object> setupVariablesSunnyDayVID() {
				Map<String, Object> variables = new HashMap<String, Object>();
				//try {
				//	variables.put("bpmnRequest", FileUtil.readResourceFile("__files/CreateVfModule_VID_request.json"));
				//}
				//catch (Exception e) {
					
				//}
				//variables.put("mso-request-id", "testRequestId");
				variables.put("requestId", "testRequestId");				
				variables.put("isDebugLogEnabled", "true");				
				variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
				variables.put("vnfId", "skask");
				variables.put("vnfType", "vSAMP12");					
				variables.put("serviceType", "MOG");	
						
				return variables;
				
			}
	
}
