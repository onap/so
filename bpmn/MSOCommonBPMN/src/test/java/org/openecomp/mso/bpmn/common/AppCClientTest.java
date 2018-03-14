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

package org.openecomp.mso.bpmn.common;


import static org.junit.Assert.assertEquals;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockAAIVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDBUpdateVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithDepth;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfsByVnfId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetPserverByVnfId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchVfModuleId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockSetInMaintFlagByVnfId;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockVNFAdapterRestVfModule;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.MockGetServiceResourcesCatalogData;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponsePolicy.MockPolicySkip;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;
import static org.openecomp.mso.bpmn.mock.StubResponseVNFAdapter.mockVNFPut;

//import static org.junit.Assert.assertEquals;
//import static org.openecomp.mso.bpmn.common.BPMNUtil.executeWorkFlow;
//import static org.openecomp.mso.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
//import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.onap.appc.client.lcm.model.Action;

//import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;

public class AppCClientTest extends WorkflowTest{

	
	@Test
	@Ignore // 1802 merge
	@Deployment(resources = {"subprocess/BuildingBlock/AppCClient.bpmn"})
	public void test() throws Exception{
				
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
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables = setVariablesInstance();
		String businessKey = UUID.randomUUID().toString();
		invokeSubProcess("AppCClient", businessKey, variables);
		waitForProcessEnd(businessKey, 10000);
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, "AppCClient", "WorkflowException");
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);
		logEnd();
	}        
	
	
	private Map<String, Object> setVariablesInstance(){
		Map<String,Object> variables = new HashMap<String, Object>();
		variables.put("isDebugLogEnabled", "true");
		variables.put("mso-request-id", "RaaACCTest1");
		variables.put("msoRequestId", "RaaACCTestRequestId-1");
		variables.put("requestId", "testRequestId");
		variables.put("vnfId", "skask");
		variables.put("action", Action.Stop);
		variables.put("healthCheckIndex", 0);
		variables.put("payload", "{\"existing-software-version\":\"3.1\",\"new-software-version\":\"3.2\"}"); 
		//variables.put("payload", "{\"vm-id\": \"<VM-ID>\", \"identy-url\":\"<IDENTITY-URL>\", \"tenant-id\": \"<TENANT-ID>\"}, \"Hello\":\"Whats up\"" );
		return variables;
	}
}
