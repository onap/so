/*
 * © 2014 AT&T Intellectual Property. All rights reserved. Used under license from AT&T Intellectual Property.
 */
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
 * Unit Test for the DoCreateVnf Flow
 *
 */
public class DoCreateVnfTest extends WorkflowTest {

	private String createVnfInfraRequest;
	private final CallbackSet callbacks = new CallbackSet();


	public DoCreateVnfTest() throws IOException {
		createVnfInfraRequest = FileUtil.readResourceFile("__files/InfrastructureFlows/CreateVnfInfraRequest.json");
		callbacks.put("assign", FileUtil.readResourceFile(
				"__files/VfModularity/SDNCTopologyAssignCallback.xml"));
		callbacks.put("activate", FileUtil.readResourceFile(
				"__files/VfModularity/SDNCTopologyActivateCallback.xml"));
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn", "subprocess/GenericGetVnf.bpmn", "subprocess/GenericPutVnf.bpmn", "subprocess/SDNCAdapterV1.bpmn", "subprocess/DoCreateVnf.bpmn"})
	public void testDoCreateVnfInfra_success() throws Exception{

		MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlById.xml");
		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");
		MockGetGenericVnfByName_404("testVnfName123");
		MockPutGenericVnf("testVnfId123");
		mockSDNCAdapter("/SDNCAdapter", "vnf-type>STMTN", 200, "VfModularity/StandardSDNCSynchResponse.xml");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<>();
		setVariablesSuccess(variables, createVnfInfraRequest, "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");
		invokeSubProcess("DoCreateVnf", businessKey, variables);

		waitForProcessEnd(businessKey, 10000);

		Assert.assertTrue(isProcessEnded(businessKey));
		assertVariables("true", "true", "false", "true", "Success", null);
	}

	private void assertVariables(String exSIFound, String exSISucc, String exVnfFound, String exVnfSucc, String exResponse, String exWorkflowException) {
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DoCreateVnf", "SavedWorkflowException1");

		assertEquals(exWorkflowException, workflowException);
	}

	private void setVariablesSuccess(Map<String, Object> variables, String request, String requestId, String siId) {
		variables.put("isDebugLogEnabled", "true");
		//variables.put("bpmnRequest", request);
		variables.put("mso-request-id", requestId);
		variables.put("serviceInstanceId",siId);
		variables.put("vnfName", "testVnfName123");
		variables.put("disableRollback", "true");
		variables.put("requestId", requestId);
		variables.put("testVnfId","testVnfId123");
		variables.put("vnfType", "STMTN");
		variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		String vnfModelInfo = "{ "+ "\"modelType\": \"vnf\"," +
				"\"modelInvariantUuid\": \"ff5256d2-5a33-55df-13ab-12abad84e7ff\"," +
				"\"modelUuid\": \"fe6478e5-ea33-3346-ac12-ab121484a3fe\"," +
				"\"modelName\": \"vSAMP12\"," +
				"\"modelVersion\": \"1.0\"," +
				"\"modelCustomizationUuid\": \"MODEL-ID-1234\"," +
				"}";
		variables.put("vnfModelInfo", vnfModelInfo);

		String cloudConfiguration = "{ " +
				"\"lcpCloudRegionId\": \"mdt1\"," +
				"\"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\"" + "}";
		variables.put("cloudConfiguration", cloudConfiguration);
		
		String serviceModelInfo = "{ "+ "\"modelType\": \"service\"," +
				"\"modelInvariantUuid\": \"995256d2-5a33-55df-13ab-12abad84e7ff\"," +
				"\"modelUuid\": \"ab6478e5-ea33-3346-ac12-ab121484a3fe\"," +
				"\"modelName\": \"ServicevSAMP12\"," +
				"\"modelVersion\": \"1.0\"," +
				"}";
		variables.put("serviceModelInfo", serviceModelInfo);
		variables.put("globalSubscriberId", "MSO-1610");
	}

}
