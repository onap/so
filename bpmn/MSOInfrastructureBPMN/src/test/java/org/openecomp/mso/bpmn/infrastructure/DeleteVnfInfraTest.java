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

/**
 * Please describe the DeleteVnfInfra.java class
 *
 */
public class DeleteVnfInfraTest extends WorkflowTest {

	private String deleteVnfInfraRequest;
	private String deleteVnfInfraRequestCascadeDelete;

	public DeleteVnfInfraTest () throws IOException {
		deleteVnfInfraRequest = FileUtil.readResourceFile("__files/InfrastructureFlows/CreateVnfInfraRequest.json");
		deleteVnfInfraRequestCascadeDelete = FileUtil.readResourceFile("__files/InfrastructureFlows/DeleteVnfInfraRequestCascadeDelete.json");
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn",  
							"subprocess/GenericDeleteVnf.bpmn", 
							"subprocess/DoDeleteVnf.bpmn", 
							"process/DeleteVnfInfra.bpmn", 
							"subprocess/FalloutHandler.bpmn", 
							"subprocess/CompleteMsoProcess.bpmn"})
	public void testDeleteVnfInfra_success() throws Exception{
		
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getGenericVnfByNameResponse.xml")));
		
		MockDeleteGenericVnf();
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		Map<String, String> variables = new HashMap<String, String>();
		setVariables(variables, deleteVnfInfraRequest, "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");
		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "DeleteVnfInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());
		Object cascadeDelete = BPMNUtil.getRawVariable(processEngineRule, "DeleteVnfInfra", "DELVI_cascadeDelete");
		String found = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "GENGV_FoundIndicator") ;
		String inUse = BPMNUtil.getVariable(processEngineRule, "DeleteVnfInfra", "DELVI_vnfInUse");
		String response = BPMNUtil.getVariable(processEngineRule, "DeleteVnfInfra", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DeleteVnfInfra", "WorkflowException");

		assertEquals(false, cascadeDelete);
		assertEquals("true", found);
		assertEquals("false", inUse);
		assertEquals("Success", response);
		assertEquals(null, workflowException);
	}
	
	@Test
	@Ignore // DoDeleteVnfAndModules not complete yet
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn", 
							"subprocess/GenericDeleteVnf.bpmn", 
							"subprocess/DoDeleteVnfAndModules.bpmn", 
							"process/DeleteVnfInfra.bpmn", 
							"subprocess/FalloutHandler.bpmn", 
							"subprocess/CompleteMsoProcess.bpmn"})
	public void testDeleteVnfInfra_cascadeDelete() throws Exception{
		MockGetGenericVnfById();
		MockDeleteGenericVnf();
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		Map<String, String> variables = new HashMap<String, String>();
		setVariables(variables, deleteVnfInfraRequestCascadeDelete, "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");
		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "DeleteVnfInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String found = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "GENGV_FoundIndicator") ;
		String inUse = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "DoDVNF_vnfInUse");
		String response = BPMNUtil.getVariable(processEngineRule, "DeleteVnfInfra", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DeleteVnfInfra", "WorkflowException");
		Object cascadeDelete = BPMNUtil.getRawVariable(processEngineRule, "DeleteVnfInfra", "DELVI_cascadeDelete");

		assertEquals(true, cascadeDelete);
		assertEquals("true", found);
		assertEquals("false", inUse);
		assertEquals("Success", response);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn", 
							"subprocess/GenericDeleteVnf.bpmn", 
							"subprocess/DoDeleteVnf.bpmn", 
							"process/DeleteVnfInfra.bpmn", 
							"subprocess/FalloutHandler.bpmn", 
							"subprocess/CompleteMsoProcess.bpmn"})
	public void testDeleteVnfInfra_success_vnfNotFound() throws Exception{

		MockDeleteGenericVnf_404();
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		Map<String, String> variables = new HashMap<String, String>();
		setVariables(variables, deleteVnfInfraRequest, "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "DeleteVnfInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String found = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "GENGV_FoundIndicator") ;
		String inUse = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "DoDVNF_vnfInUse");
		String response = BPMNUtil.getVariable(processEngineRule, "DeleteVnfInfra", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "WorkflowException");

		assertEquals("false", found);
		assertEquals("false", inUse);
		assertEquals("Success", response);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn",  
							"subprocess/GenericDeleteVnf.bpmn", 
							"subprocess/DoDeleteVnf.bpmn", 
							"process/DeleteVnfInfra.bpmn", 
							"subprocess/FalloutHandler.bpmn", 
							"subprocess/CompleteMsoProcess.bpmn"})
	public void testDeleteVnfInfra_error_vnfInUse() throws Exception{

		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/testVnfId123[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("GenericFlows/getGenericVnfResponse_hasRelationships.xml")));
		MockDeleteGenericVnf();
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

		Map<String, String> variables = new HashMap<String, String>();
		setVariables(variables, deleteVnfInfraRequest, "testRequestId123", "MIS%2F1604%2F0026%2FSW_INTERNET");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "DeleteVnfInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String found = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "GENGV_FoundIndicator") ;
		String inUse = BPMNUtil.getVariable(processEngineRule, "DoDeleteVnf", "DoDVNF_vnfInUse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DeleteVnfInfra", "SavedWorkflowException1");

		String exWfex = "WorkflowException[processKey=DoDeleteVnf,errorCode=5000,errorMessage=Can't Delete Generic Vnf. Generic Vnf is still in use.]";

		assertEquals("true", found);
		assertEquals("true", inUse);
		assertEquals(exWfex, workflowException);
	}

	private void setVariables(Map<String, String> variables, String request, String requestId, String siId) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("bpmnRequest", request);
		variables.put("mso-request-id", requestId);
		variables.put("serviceInstanceId",siId);
		variables.put("vnfId","testVnfId123");
	}
}
