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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Please describe the DeleteVnfInfra.java class
 *
 */
public class DoDeleteVnfTest extends WorkflowTest {

	
	@Test
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn", "subprocess/GenericDeleteVnf.bpmn", "subprocess/DoDeleteVnf.bpmn"})
	public void testDoDeleteVnf_success() throws Exception{
		
		MockGetGenericVnfByIdWithDepth("testVnfId123", 1, "GenericFlows/getGenericVnfByNameResponse.xml");
		MockDeleteGenericVnf("testVnfId123", "testReVer123");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<String, Object>();
		setVariables(variables);
		invokeSubProcess("DoDeleteVnf", businessKey, variables);
		// Disabled until SDNC support is there
//		injectSDNCCallbacks(callbacks, "assign");		
//		injectSDNCCallbacks(callbacks, "activate");

		waitForProcessEnd(businessKey, 10000);
				
		Assert.assertTrue(isProcessEnded(businessKey));
		String found = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "GENGV_FoundIndicator") ;
		String inUse = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "DoDVNF_vnfInUse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "WorkflowException");

		assertEquals("true", found);
		assertEquals("false", inUse);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn", "subprocess/GenericDeleteVnf.bpmn", "subprocess/DoDeleteVnf.bpmn"})
	public void testDeleteVnfInfra_success_vnfNotFound() throws Exception{

		MockDeleteGenericVnf("testVnfId123", "testReVer123", 404);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<String, Object>();
		setVariables(variables);

		invokeSubProcess("DoDeleteVnf", businessKey, variables);
		// Disabled until SDNC support is there
//		injectSDNCCallbacks(callbacks, "assign");		
//		injectSDNCCallbacks(callbacks, "activate");

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));
		String found = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "GENGV_FoundIndicator") ;
		String inUse = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "DoDVNF_vnfInUse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "WorkflowException");

		assertEquals("false", found);
		assertEquals("false", inUse);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn", "subprocess/GenericDeleteVnf.bpmn", "subprocess/DoDeleteVnf.bpmn"})
	public void testDeleteVnfInfra_error_vnfInUse() throws Exception{

		MockGetGenericVnfByIdWithDepth("testVnfId123", 1, "GenericFlows/getGenericVnfResponse_hasRelationships.xml");
		MockDeleteGenericVnf("testVnfId123", "testReVer123");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<String, Object>();
		setVariables(variables);

		invokeSubProcess("DoDeleteVnf", businessKey, variables);
//		Disabled until SDNC support is there
//		injectSDNCCallbacks(callbacks, "assign");		
//		injectSDNCCallbacks(callbacks, "activate");

		waitForProcessEnd(businessKey, 10000);
		
		Assert.assertTrue(isProcessEnded(businessKey));

		String found = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "GENGV_FoundIndicator") ;
		String inUse = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "DoDVNF_vnfInUse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "WorkflowException");
		String exWfex = "WorkflowException[processKey=DoDeleteVnf,errorCode=5000,errorMessage=Can't Delete Generic Vnf. Generic Vnf is still in use.]";

		assertEquals("true", found);
		assertEquals("true", inUse);
		assertEquals(exWfex, workflowException);
	}

	private void setVariables(Map<String, Object> variables) {
		variables.put("mso-request-id", "123");
		variables.put("isDebugLogEnabled", "true");		
		variables.put("vnfId","testVnfId123");
	}
}
