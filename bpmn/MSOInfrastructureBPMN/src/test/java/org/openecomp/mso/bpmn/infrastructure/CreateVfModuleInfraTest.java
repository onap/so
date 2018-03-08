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

import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchVfModuleId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockAAIVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockSDNCAdapterVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockVNFAdapterRestVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDBUpdateVfModule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Ignore;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;
import org.openecomp.mso.bpmn.mock.FileUtil;

/**
 * Unit test cases for CreateVfModuleInfra.bpmn
 */
public class CreateVfModuleInfraTest extends WorkflowTest {
	
	private final CallbackSet callbacks = new CallbackSet();

	public CreateVfModuleInfraTest() throws IOException {
		callbacks.put("assign", FileUtil.readResourceFile(
			"__files/VfModularity/SDNCTopologyAssignCallback.xml"));
		callbacks.put("query", FileUtil.readResourceFile(
			"__files/VfModularity/SDNCTopologyQueryCallbackVfModule.xml"));
		callbacks.put("activate", FileUtil.readResourceFile(
			"__files/VfModularity/SDNCTopologyActivateCallback.xml"));
		callbacks.put("vnfCreate", FileUtil.readResourceFile(
			"__files/VfModularity/VNFAdapterRestCreateCallback.xml"));
	}
	
	
	/**
	 * Sunny day VID scenario with preloads.
	 * 
	 * @throws Exception
	 */
	@Test	
	@Deployment(resources = {
			"process/CreateVfModuleInfra.bpmn",
			"subprocess/DoCreateVfModule.bpmn",
			"subprocess/GenericGetVnf.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericNotificationService.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn",
			"subprocess/ConfirmVolumeGroupTenant.bpmn",
			"subprocess/ConfirmVolumeGroupName.bpmn",
			"subprocess/CreateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn",
			"subprocess/CompleteMsoProcess.bpmn",
			"subprocess/FalloutHandler.bpmn"
		})
	public void sunnyDayVIDWithPreloads() throws Exception {
				
		logStart();
		
		MockAAIVfModule();
		MockPatchGenericVnf("skask");
		MockPatchVfModuleId("skask", ".*");
		MockSDNCAdapterVfModule();		
		MockVNFAdapterRestVfModule();
		MockDBUpdateVfModule();
		
		String businessKey = UUID.randomUUID().toString();
		String createVfModuleRequest =
			FileUtil.readResourceFile("__files/CreateVfModule_VID_request.json");
		
		Map<String, Object> variables = setupVariablesSunnyDayVID();		
		
		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVfModuleInfra",
			"v1", businessKey, createVfModuleRequest, variables);
				
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
		
		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		injectSDNCCallbacks(callbacks, "assign, query");
		injectVNFRestCallbacks(callbacks, "vnfCreate");
		injectSDNCCallbacks(callbacks, "activate");
		
		// TODO add appropriate assertions

		waitForProcessEnd(businessKey, 10000);
		checkVariable(businessKey, "CreateVfModuleSuccessIndicator", true);
		
		logEnd();
	}
	
	/**
	 * Sunny day VID scenario with no preloads.
	 * 
	 * @throws Exception
	 */
	@Test	
	@Deployment(resources = {
			"process/CreateVfModuleInfra.bpmn",
			"subprocess/DoCreateVfModule.bpmn",
			"subprocess/GenericGetVnf.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn",
			"subprocess/ConfirmVolumeGroupTenant.bpmn",
			"subprocess/GenericNotificationService.bpmn",
			"subprocess/ConfirmVolumeGroupName.bpmn",
			"subprocess/CreateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn",
			"subprocess/CompleteMsoProcess.bpmn",
			"subprocess/FalloutHandler.bpmn"
		})
	public void sunnyDayVIDNoPreloads() throws Exception {
				
		logStart();
		
		MockAAIVfModule();
		MockPatchGenericVnf("skask");
		MockPatchVfModuleId("skask", ".*");
		MockSDNCAdapterVfModule();		
		MockVNFAdapterRestVfModule();
		MockDBUpdateVfModule();
		
		String businessKey = UUID.randomUUID().toString();
		String createVfModuleRequest =
			FileUtil.readResourceFile("__files/CreateVfModule_VID_request_noPreloads.json");
		
		Map<String, Object> variables = setupVariablesSunnyDayVID();		
		
		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVfModuleInfra",
			"v1", businessKey, createVfModuleRequest, variables);
				
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
		
		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);
		
		injectSDNCCallbacks(callbacks, "assign, query");
		injectVNFRestCallbacks(callbacks, "vnfCreate");
		injectSDNCCallbacks(callbacks, "activate");
		
		// TODO add appropriate assertions

		waitForProcessEnd(businessKey, 10000);
		checkVariable(businessKey, "CreateVfModuleSuccessIndicator", true);
		
		logEnd();
	}
	
	// Active Scenario
		private Map<String, Object> setupVariablesSunnyDayVID() {
			Map<String, Object> variables = new HashMap<>();
			//try {
				//variables.put("bpmnRequest", FileUtil.readResourceFile("__files/CreateVfModule_VID_request.json"));
			//}
			//catch (Exception e) {
				
			//}
			//variables.put("mso-request-id", "testRequestId");
			variables.put("requestId", "testRequestId");		
			variables.put("isBaseVfModule", false);
			variables.put("isDebugLogEnabled", "true");
			variables.put("recipeTimeout", "0");		
			variables.put("requestAction", "CREATE_VF_MODULE");
			variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
			variables.put("vnfId", "skask");
			variables.put("vnfType", "vSAMP12");
			variables.put("vfModuleId", "");
			variables.put("volumeGroupId", "");			
			variables.put("serviceType", "MOG");	
			variables.put("vfModuleType", "");			
			return variables;
			
		}
		
		/**
		 * Sunny day VID with volume attach scenario.
		 * 
		 * @throws Exception
		 */
		@Test
		@Ignore
		@Deployment(resources = {
				"process/CreateVfModuleInfra.bpmn",
				"subprocess/DoCreateVfModule.bpmn",
				"subprocess/GenericGetVnf.bpmn",
				"subprocess/SDNCAdapterV1.bpmn",
				"subprocess/VnfAdapterRestV1.bpmn",
				"subprocess/ConfirmVolumeGroupTenant.bpmn",
				"subprocess/ConfirmVolumeGroupName.bpmn",
				"subprocess/CreateAAIVfModule.bpmn",
				"subprocess/CreateAAIVfModuleVolumeGroup.bpmn",
				"subprocess/GenericNotificationService.bpmn",
				"subprocess/UpdateAAIVfModule.bpmn",
				"subprocess/UpdateAAIGenericVnf.bpmn",
				"subprocess/CompleteMsoProcess.bpmn",
				"subprocess/FalloutHandler.bpmn"
			})
		
		public void sunnyDayVIDWithVolumeGroupAttach() throws Exception {
					
			logStart();
			

			MockAAIVfModule();
			MockPatchGenericVnf("skask");
			MockPatchVfModuleId("skask", ".*");
			MockSDNCAdapterVfModule();		
			MockVNFAdapterRestVfModule();
			MockDBUpdateVfModule();
			
			String businessKey = UUID.randomUUID().toString();
			String createVfModuleRequest =
				FileUtil.readResourceFile("__files/CreateVfModuleVolumeGroup_VID_request.json");
			
			Map<String, Object> variables = setupVariablesSunnyDayVIDWVolumeAttach();
			
			TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVfModuleInfra",
				"v1", businessKey, createVfModuleRequest, variables);
					
			WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
			
			String responseBody = response.getResponse();
			System.out.println("Workflow (Synch) Response:\n" + responseBody);
			
			injectSDNCCallbacks(callbacks, "assign, query");
			injectVNFRestCallbacks(callbacks, "vnfCreate");
			injectSDNCCallbacks(callbacks, "activate");
			
			// TODO add appropriate assertions

			waitForProcessEnd(businessKey, 10000);
			checkVariable(businessKey, "CreateVfModuleSuccessIndicator", true);
			
			logEnd();
		}
		
		// Active Scenario
			private Map<String, Object> setupVariablesSunnyDayVIDWVolumeAttach() {
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
				variables.put("requestAction", "CREATE_VF_MODULE");
				variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
				variables.put("vnfId", "skask");
				variables.put("vnfType", "vSAMP12");
				variables.put("vfModuleId", "");
				variables.put("volumeGroupId", "78987");			
				variables.put("serviceType", "MOG");	
				variables.put("vfModuleType", "");			
				return variables;
				
			}
			/**
			 * Sunny day VID scenario with no preloads.
			 * 
			 * @throws Exception
			 */
			@Test	
			@Deployment(resources = {
					"process/CreateVfModuleInfra.bpmn",
					"subprocess/DoCreateVfModule.bpmn",
					"subprocess/GenericGetVnf.bpmn",
					"subprocess/SDNCAdapterV1.bpmn",
					"subprocess/VnfAdapterRestV1.bpmn",
					"subprocess/ConfirmVolumeGroupTenant.bpmn",
					"subprocess/GenericNotificationService.bpmn",
					"subprocess/ConfirmVolumeGroupName.bpmn",
					"subprocess/CreateAAIVfModule.bpmn",
					"subprocess/UpdateAAIVfModule.bpmn",
					"subprocess/UpdateAAIGenericVnf.bpmn",
					"subprocess/CompleteMsoProcess.bpmn",
					"subprocess/FalloutHandler.bpmn"
				})
			public void sunnyDayVIDMultipleUserParamValues() throws Exception {
						
				logStart();
				
				MockAAIVfModule();
				MockPatchGenericVnf("skask");
				MockPatchVfModuleId("skask", ".*");
				MockSDNCAdapterVfModule();		
				MockVNFAdapterRestVfModule();
				MockDBUpdateVfModule();
				
				String businessKey = UUID.randomUUID().toString();
				String createVfModuleRequest =
					FileUtil.readResourceFile("__files/CreateVfModule_VID_request_userParam.json");
				
				Map<String, Object> variables = setupVariablesSunnyDayVID();		
				
				TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVfModuleInfra",
					"v1", businessKey, createVfModuleRequest, variables);
						
				WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
				
				String responseBody = response.getResponse();
				System.out.println("Workflow (Synch) Response:\n" + responseBody);
				
				injectSDNCCallbacks(callbacks, "assign, query");
				injectVNFRestCallbacks(callbacks, "vnfCreate");
				injectSDNCCallbacks(callbacks, "activate");
				
				// TODO add appropriate assertions
				
				waitForProcessEnd(businessKey, 10000);
				checkVariable(businessKey, "CreateVfModuleSuccessIndicator", true);
				
				logEnd();
			}
		
	
}
