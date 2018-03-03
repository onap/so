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
 * Unit tests for DoCreateVfModuleTest.bpmn.
 */
public class DoCreateVfModuleTest extends WorkflowTest {
	
	private final CallbackSet callbacks = new CallbackSet();

	public DoCreateVfModuleTest() throws IOException {
		callbacks.put("assign", FileUtil.readResourceFile(
			"__files/VfModularity/SDNCTopologyAssignCallback.xml"));
		callbacks.put("query", FileUtil.readResourceFile(
			"__files/VfModularity/SDNCTopologyQueryCallback.xml"));
		callbacks.put("queryVnf", FileUtil.readResourceFile(
				"__files/VfModularity/SDNCTopologyQueryCallbackVnf.xml"));
		callbacks.put("queryModuleNoVnf", FileUtil.readResourceFile(
				"__files/VfModularity/SDNCTopologyQueryCallbackVfModuleNoVnf.xml"));
		callbacks.put("queryModule", FileUtil.readResourceFile(
				"__files/VfModularity/SDNCTopologyQueryCallbackVfModule.xml"));
		callbacks.put("activate", FileUtil.readResourceFile(
			"__files/VfModularity/SDNCTopologyActivateCallback.xml"));
		callbacks.put("vnfCreate", FileUtil.readResourceFile(
			"__files/VfModularity/VNFAdapterRestCreateCallback.xml"));
	}

	/**
	 * Test the sunny day scenario.
	 */
	@Test	
	
	@Deployment(resources = {
			"subprocess/DoCreateVfModule.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericGetVnf.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn",
			"subprocess/ConfirmVolumeGroupTenant.bpmn",
			"subprocess/ConfirmVolumeGroupName.bpmn",
			"subprocess/CreateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/CreateAAIVfModuleVolumeGroup.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn"
		})
	public void sunnyDay() throws IOException {
		
		logStart();
		
		MockAAIVfModule();
		MockPatchGenericVnf("skask");
		MockPatchVfModuleId("skask", ".*");
		mockSDNCAdapter("VfModularity/StandardSDNCSynchResponse.xml");
		mockVNFPost("", 202, "skask");	
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		//RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		
		Map<String, Object> variables = setupVariablesSunnyDayBuildingBlocks();
		//runtimeService.startProcessInstanceByKey("DoCreateVfModule", variables);
		invokeSubProcess("DoCreateVfModule", businessKey, variables);
		
		injectSDNCCallbacks(callbacks, "queryVnf");
		injectSDNCCallbacks(callbacks, "assign, queryModuleNoVnf");
		injectVNFRestCallbacks(callbacks, "vnfCreate");
		injectSDNCCallbacks(callbacks, "activate");

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		Assert.assertTrue((boolean) getRawVariable(processEngineRule, "DoCreateVfModule", "DCVFM_SuccessIndicator"));
		
		logEnd();
	}
	
	/**
	 * Test the sunny day scenario with 1702 SDNC interaction.
	 */
	@Test	
	
	@Deployment(resources = {
			"subprocess/DoCreateVfModule.bpmn",
			"subprocess/GenericGetVnf.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn",
			"subprocess/ConfirmVolumeGroupTenant.bpmn",
			"subprocess/ConfirmVolumeGroupName.bpmn",
			"subprocess/CreateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/CreateAAIVfModuleVolumeGroup.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn"
		})
	public void sunnyDay_1702() throws IOException {
		
		logStart();
		
		MockGetGenericVnfByIdWithPriority("skask", ".*", 200, "VfModularity/VfModule-new.xml", 5);
		MockGetGenericVnfByIdWithDepth("skask", 1, "VfModularity/GenericVnf.xml");
		MockPutVfModuleIdNoResponse("skask", "PCRF", ".*");
		MockPutNetwork(".*", "VfModularity/AddNetworkPolicy_AAIResponse_Success.xml", 200);
		MockPutGenericVnf("skask");
		mockSDNCAdapter("/SDNCAdapter", "vnf-type>STMTN", 200, "VfModularity/StandardSDNCSynchResponse.xml");
		mockSDNCAdapter("/SDNCAdapter", "SvcAction>query", 200, "VfModularity/StandardSDNCSynchResponse.xml");
		mockVNFPost("", 202, "skask");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockPatchGenericVnf("skask");
		MockPatchVfModuleId("skask", ".*");
		
		String businessKey = UUID.randomUUID().toString();
		//RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		
		Map<String, Object> variables = setupVariablesSunnyDayBuildingBlocks();
		variables.put("sdncVersion", "1702");
		//runtimeService.startProcessInstanceByKey("DoCreateVfModule", variables);
		invokeSubProcess("DoCreateVfModule", businessKey, variables);
		
		
		injectSDNCCallbacks(callbacks, "assign, queryModule");
		injectVNFRestCallbacks(callbacks, "vnfCreate");
		injectSDNCCallbacks(callbacks, "activate");

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		Assert.assertTrue((boolean) getRawVariable(processEngineRule, "DoCreateVfModule", "DCVFM_SuccessIndicator"));
		
		logEnd();
	}
	
	/**
	 * Test the sunny day scenario.
	 */
	@Test	
	
	@Deployment(resources = {
			"subprocess/DoCreateVfModule.bpmn",
			"subprocess/GenerateVfModuleName.bpmn",
			"subprocess/GenericGetVnf.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn",
			"subprocess/ConfirmVolumeGroupTenant.bpmn",
			"subprocess/ConfirmVolumeGroupName.bpmn",
			"subprocess/CreateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/CreateAAIVfModuleVolumeGroup.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn"
		})
	public void sunnyDay_withVfModuleNameGeneration() throws IOException {
		
		logStart();
		
		MockGetGenericVnfByIdWithPriority("skask", ".*", 200, "VfModularity/VfModule-new.xml", 5);
		MockGetGenericVnfByIdWithDepth("skask", 1, "VfModularity/GenericVnf.xml");
		MockPutVfModuleIdNoResponse("skask", "PCRF", ".*");
		MockPutNetwork(".*", "VfModularity/AddNetworkPolicy_AAIResponse_Success.xml", 200);
		MockPutGenericVnf("skask");
		MockAAIVfModule();
		mockSDNCAdapter("/SDNCAdapter", "vnf-type>STMTN", 200, "VfModularity/StandardSDNCSynchResponse.xml");
		mockSDNCAdapter("/SDNCAdapter", "SvcAction>query", 200, "VfModularity/StandardSDNCSynchResponse.xml");
		mockVNFPost("", 202, "skask");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockPatchGenericVnf("skask");
		MockPatchVfModuleId("skask", ".*");
		
		String businessKey = UUID.randomUUID().toString();
		//RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		
		Map<String, Object> variables = setupVariablesSunnyDayBuildingBlocks();
		variables.put("vfModuleName", null);
		variables.put("vfModuleLabel", "MODULELABEL");
		variables.put("sdncVersion", "1702");
		//runtimeService.startProcessInstanceByKey("DoCreateVfModule", variables);
		invokeSubProcess("DoCreateVfModule", businessKey, variables);
		
		injectSDNCCallbacks(callbacks, "assign, query");
		injectVNFRestCallbacks(callbacks, "vnfCreate");
		injectSDNCCallbacks(callbacks, "activate");

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		Assert.assertTrue((boolean) getRawVariable(processEngineRule, "DoCreateVfModule", "DCVFM_SuccessIndicator"));
		
		logEnd();
	}
	
	
	private Map<String, Object> setupVariablesSunnyDayBuildingBlocks() {
		Map<String, Object> variables = new HashMap<String, Object>();
		//try {
		//	variables.put("bpmnRequest", FileUtil.readResourceFile("__files/CreateVfModule_VID_request.json"));
		//}
		//catch (Exception e) {
			
		//}
		
		variables.put("mso-request-id", "testRequestId");
		
		variables.put("msoRequestId", "testRequestId");		
		variables.put("isBaseVfModule", false);
		variables.put("isDebugLogEnabled", "true");
		variables.put("disableRollback", "true");
		//variables.put("recipeTimeout", "0");		
		//variables.put("requestAction", "CREATE_VF_MODULE");
		variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
		variables.put("vnfId", "skask");
		variables.put("vnfName", "vnfname");
		variables.put("vfModuleName", "PCRF::module-0-2");
		variables.put("vnfType", "vSAMP12");
		variables.put("vfModuleId", "");
		variables.put("volumeGroupId", "");			
		variables.put("serviceType", "MOG");	
		variables.put("vfModuleType", "");
		variables.put("isVidRequest", "true");
		variables.put("asdcServiceModelVersion", "1.0");
		variables.put("usePreload", true);
					
		String vfModuleModelInfo = "{ "+ "\"modelType\": \"vfModule\"," +
			"\"modelInvariantUuid\": \"ff5256d2-5a33-55df-13ab-12abad84e7ff\"," + 
			"\"modelUuid\": \"fe6478e5-ea33-3346-ac12-ab121484a3fe\"," +
			"\"modelName\": \"STMTN5MMSC21-MMSC::model-1-0\"," +
			"\"modelVersion\": \"1\"," + 
			"\"modelCustomizationUuid\": \"MODEL-123\"" + "}";
		variables.put("vfModuleModelInfo", vfModuleModelInfo);
		
		variables.put("sdncVersion", "1707");
		
		variables.put("lcpCloudRegionId", "MDTWNJ21");
		variables.put("tenantId", "fba1bd1e195a404cacb9ce17a9b2b421");		
		
		String serviceModelInfo = "{ "+ "\"modelType\": \"service\"," +
				"\"modelInvariantUuid\": \"aa5256d2-5a33-55df-13ab-12abad84e7ff\"," + 
				"\"modelUuid\": \"bb6478e5-ea33-3346-ac12-ab121484a3fe\"," +
				"\"modelName\": \"SVC-STMTN5MMSC21-MMSC::model-1-0\"," +
				"\"modelVersion\": \"1\"," + 
				 "}";
		variables.put("serviceModelInfo", serviceModelInfo);
			
		String vnfModelInfo = "{ "+ "\"modelType\": \"vnf\"," +
					"\"modelInvariantUuid\": \"445256d2-5a33-55df-13ab-12abad84e7ff\"," + 
					"\"modelUuid\": \"f26478e5-ea33-3346-ac12-ab121484a3fe\"," +
					"\"modelName\": \"VNF-STMTN5MMSC21-MMSC::model-1-0\"," +
					"\"modelVersion\": \"1\"," + 
					"\"modelCustomizationUuid\": \"VNF-MODEL-123\"" + "}";
		variables.put("vnfModelInfo", vnfModelInfo);
		
		variables.put("vnfQueryPath", "/restconf/vnfQueryPath");
		
		return variables;
		
	}
}