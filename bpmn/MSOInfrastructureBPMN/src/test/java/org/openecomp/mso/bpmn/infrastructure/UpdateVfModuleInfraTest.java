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
 * Unit test cases for UpdateVfModule.bpmn
 */
public class UpdateVfModuleInfraTest extends WorkflowTest {
	
	private final CallbackSet callbacks = new CallbackSet();

	public UpdateVfModuleInfraTest() throws IOException {
		callbacks.put("changeassign", FileUtil.readResourceFile(
			"__files/VfModularity/SDNCTopologyChangeAssignCallback.xml"));
		callbacks.put("query", FileUtil.readResourceFile(
			"__files/VfModularity/SDNCTopologyQueryCallback.xml"));
		callbacks.put("activate", FileUtil.readResourceFile(
			"__files/VfModularity/SDNCTopologyActivateCallback.xml"));
		callbacks.put("vnfUpdate", FileUtil.readResourceFile(
			"__files/VfModularity/VNFAdapterRestUpdateCallback.xml"));
	}
	
	/**
	 * Sunny day scenario.
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore
	@Deployment(resources = {
		"process/UpdateVfModuleInfra.bpmn",
		"subprocess/DoUpdateVfModule.bpmn",
		"subprocess/PrepareUpdateAAIVfModule.bpmn",
		"subprocess/ConfirmVolumeGroupTenant.bpmn",
		"subprocess/SDNCAdapterV1.bpmn",
		"subprocess/VnfAdapterRestV1.bpmn",
		"subprocess/UpdateAAIGenericVnf.bpmn",
		"subprocess/UpdateAAIVfModule.bpmn",
		"subprocess/CompleteMsoProcess.bpmn",
		"subprocess/FalloutHandler.bpmn"
		})
	public void sunnyDay() throws Exception {
				
		logStart();
		
		MockGetGenericVnfByIdWithDepth("skask", 1, "VfModularity/GenericVnf.xml");
		MockPutVfModuleIdNoResponse("skask", "PCRF", "supercool");
		MockGetGenericVnfByIdWithPriority("skask", "supercool", 200, "VfModularity/VfModule-supercool.xml", 1);
		mockSDNCAdapter("/SDNCAdapter", "SvcInstanceId><", 200, "VfModularity/StandardSDNCSynchResponse.xml");
		mockSDNCAdapter("/SDNCAdapter", "vnf-type>STMTN", 200, "VfModularity/StandardSDNCSynchResponse.xml");
		mockSDNCAdapter("/SDNCAdapter", "SvcAction>query", 200, "VfModularity/StandardSDNCSynchResponse.xml");
		mockVNFPut("skask", "/supercool", 202);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		String updaetVfModuleRequest =
			FileUtil.readResourceFile("__files/InfrastructureFlows/UpdateVfModule_VID_request.json");
		
		Map<String, Object> variables = setupVariablesSunnyDayVID();
		
		
		TestAsyncResponse asyncResponse = invokeAsyncProcess("UpdateVfModuleInfra",
			"v1", businessKey, updaetVfModuleRequest, variables);
		
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
		
		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
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
				Map<String, Object> variables = new HashMap<>();
				//try {
				//	variables.put("bpmnRequest", FileUtil.readResourceFile("__files/CreateVfModule_VID_request.json"));
				//}
				//catch (Exception e) {
					
				//}
				//variables.put("mso-request-id", "testRequestId");
				variables.put("requestId", "testRequestId");		
				variables.put("isBaseVfModule", false);
				variables.put("isDebugLogEnabled", "true");
				variables.put("recipeTimeout", "0");		
				variables.put("requestAction", "UPDATE_VF_MODULE");
				variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
				variables.put("vnfId", "skask");
				variables.put("vnfType", "vSAMP12");
				variables.put("vfModuleId", "supercool");
				variables.put("volumeGroupId", "");			
				variables.put("serviceType", "MOG");	
				variables.put("vfModuleType", "");			
				return variables;
				
			}
	
}
