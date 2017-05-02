/*- 
 * ============LICENSE_START======================================================= 
 * OPENECOMP - MSO 
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


import static org.openecomp.mso.bpmn.common.BPMNUtil.getRawVariable;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithDepth;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithPriority;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutNetwork;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutVfModuleIdNoResponse;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;
import static org.openecomp.mso.bpmn.mock.StubResponseVNFAdapter.mockVNFPost;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.WorkflowTest.CallbackSet;
import org.openecomp.mso.bpmn.mock.FileUtil;

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
		callbacks.put("activate", FileUtil.readResourceFile(
			"__files/VfModularity/SDNCTopologyActivateCallback.xml"));
		callbacks.put("vnfCreate", FileUtil.readResourceFile(
			"__files/VfModularity/VNFAdapterRestCreateCallback.xml"));
	}

	/**
	 * Test the sunny day scenario.
	 */
	@Test	
	@Ignore
	@Deployment(resources = {
			"subprocess/DoCreateVfModule.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
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
		
		MockGetGenericVnfByIdWithPriority("skask", ".*", 200, "VfModularity/VfModule-new.xml", 5);
		MockGetGenericVnfByIdWithDepth("skask", 1, "VfModularity/GenericVnf.xml");
		MockPutVfModuleIdNoResponse("skask", "PCRF", ".*");
		MockPutNetwork(".*", "VfModularity/AddNetworkPolicy_AAIResponse_Success.xml", 200);
		MockPutGenericVnf("skask");
		mockSDNCAdapter("/SDNCAdapter", "vnf-type>STMTN", 200, "VfModularity/StandardSDNCSynchResponse.xml");
		mockSDNCAdapter("/SDNCAdapter", "SvcAction>query", 200, "VfModularity/StandardSDNCSynchResponse.xml");
		mockVNFPost("", 202, "skask");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		//RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		
		Map<String, Object> variables = setupVariablesSunnyDayBuildingBlocks();
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
		
		variables.put("mso-request-id", "testRequestId");
		variables.put("requestId", "testRequestId");		
		variables.put("isBaseVfModule", false);
		variables.put("isDebugLogEnabled", "true");
		variables.put("disableRollback", "true");
		//variables.put("recipeTimeout", "0");		
		//variables.put("requestAction", "CREATE_VF_MODULE");
		variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
		variables.put("vnfId", "skask");
		variables.put("vfModuleName", "PCRF::module-0-2");
		variables.put("vnfType", "vSAMP12");
		variables.put("vfModuleId", "");
		variables.put("volumeGroupId", "");			
		variables.put("serviceType", "MOG");	
		variables.put("vfModuleType", "");
		variables.put("isVidRequest", "true");
		variables.put("asdcServiceModelVersion", "1.0");
					
		String vfModuleModelInfo = "{" + "\"modelInfo\": { "+ "\"modelType\": \"vfModule\"," +
			"\"modelInvariantId\": \"ff5256d2-5a33-55df-13ab-12abad84e7ff\"," + 
			"\"modelNameVersionId\": \"fe6478e5-ea33-3346-ac12-ab121484a3fe\"," +
			"\"modelName\": \"STMTN5MMSC21-MMSC::model-1-0\"," +
			"\"modelVersion\": \"1\"," + 
			"\"modelCustomizationId\": \"MODEL-123\"" + "}}";
		variables.put("vfModuleModelInfo", vfModuleModelInfo);
		
		String cloudConfiguration = "{" + "\"cloudConfiguration\": { " + 
				"\"lcpCloudRegionId\": \"MDTWNJ21\"," +		
				"\"tenantId\": \"fba1bd1e195a404cacb9ce17a9b2b421\"" + "}}";
		variables.put("cloudConfiguration", cloudConfiguration);
		return variables;
		
	}
}
