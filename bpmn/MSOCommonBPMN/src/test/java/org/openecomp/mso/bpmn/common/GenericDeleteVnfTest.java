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

package org.openecomp.mso.bpmn.common;

import static org.junit.Assert.assertEquals;
import static org.openecomp.mso.bpmn.common.BPMNUtil.executeWorkFlow;
import static org.openecomp.mso.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteGenericVnf_500;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteVce;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfById_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetVceById;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;

/**
 * Please describe the GenericDeleteVnfTest.java class
 *
 */
public class GenericDeleteVnfTest extends WorkflowTest {

	@Test
	@Deployment(resources = {"subprocess/GenericDeleteVnf.bpmn"})
	public void testGenericDeleteVnf_success_genericVnf() throws Exception{
		MockDeleteGenericVnf("testVnfId123", "testReVer123");

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "testReVer123");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericDeleteVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		assertVariables("true", "true", "true", null);

	}

	@Test
	@Deployment(resources = {"subprocess/GenericDeleteVnf.bpmn"})
	public void testGenericDeleteVnf_success_vce() throws Exception{
		MockDeleteVce("testVnfId123", "testReVer123", 204);

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "vce", "testReVer123");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericDeleteVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		assertVariables("true", "true", "true", null);

	}

	@Test
	@Deployment(resources = {"subprocess/GenericDeleteVnf.bpmn"})
	public void testGenericDeleteVnf_success_genericVnfNoResourceVersion() throws Exception{

		MockGetGenericVnfById("/testVnfId123", "GenericFlows/getGenericVnfByNameResponse.xml", 200);
		MockDeleteGenericVnf("testVnfId123", "testReVer123");

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericDeleteVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		assertVariables("true", "true", "false", null);

	}

	@Test
	@Deployment(resources = {"subprocess/GenericDeleteVnf.bpmn"})
	public void testGenericDeleteVnf_success_vceNoResourceVersion() throws Exception{
		MockDeleteVce("testVnfId123", "testReVer123", 204);
		MockGetVceById("testVnfId123", "GenericFlows/getVceResponse.xml");

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "vce", null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericDeleteVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		assertVariables("true", "true", "false", null);

	}

	@Test
	@Deployment(resources = {"subprocess/GenericDeleteVnf.bpmn"})
	public void testGenericDeleteVnf_success_genericVnf404() throws Exception{
		MockDeleteGenericVnf("testVnfId123", "testReVer123", 404);

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "testReVer123");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericDeleteVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		assertVariables("true", "false", "true", null);

	}

	@Test
	@Deployment(resources = {"subprocess/GenericDeleteVnf.bpmn"})
	public void testGenericDeleteVnf_success_vce404() throws Exception{
		MockDeleteVce("testVnfId123", "testReVer123", 404);

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "vce", "testReVer123");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericDeleteVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		assertVariables("true", "false", "true", null);

	}

	@Test
	@Deployment(resources = {"subprocess/GenericDeleteVnf.bpmn"})
	public void testGenericDeleteVnf_success_genericVnfNoResourceVersion404() throws Exception{
		MockGetGenericVnfById_404("testVnfId123");

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericDeleteVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		assertVariables("true", "false", "false", null);

	}

	@Test
	@Deployment(resources = {"subprocess/GenericDeleteVnf.bpmn"})
	public void testGenericDeleteVnf_error_missingVariables() throws Exception{

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "", "testReVer123");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericDeleteVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		assertVariables("false", "false", "true", "WorkflowException[processKey=GenericDeleteVnf,errorCode=500,errorMessage=Incoming Required Variable is Missing or Null!]");

	}

	@Test
	@Deployment(resources = {"subprocess/GenericDeleteVnf.bpmn"})
	public void testGenericDeleteVnf_error_genericVnf500() throws Exception{

		MockDeleteGenericVnf_500("testVnfId123", "testReVer123");
		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "testReVer123");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericDeleteVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		assertVariables("false", "false", "true", "WorkflowException[processKey=GenericDeleteVnf,errorCode=500,errorMessage=Received a bad response from AAI]");

	}

	@Test
	@Deployment(resources = {"subprocess/GenericDeleteVnf.bpmn"})
	public void testGenericDeleteVnf_error_genericVnf412() throws Exception{
		MockDeleteGenericVnf("testVnfId123", "testReVer123", 412);

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "testReVer123");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericDeleteVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		assertVariables("false", "false", "true", "WorkflowException[processKey=GenericDeleteVnf,errorCode=412,errorMessage=Delete Vnf Received a resource-version Mismatch Error Response from AAI]");

	}

	private void assertVariables(String exSuccessIndicator, String exFound, String exRVProvided, String exWorkflowException) {

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericDeleteVnf", "GENDV_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericDeleteVnf", "GENDV_FoundIndicator");
		String rvProvided = BPMNUtil.getVariable(processEngineRule, "GenericDeleteVnf", "GENDV_resourceVersionProvided");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericDeleteVnf", "WorkflowException");

		assertEquals(exSuccessIndicator, successIndicator);
		assertEquals(exFound, found);
		assertEquals(exRVProvided, rvProvided);
		assertEquals(exWorkflowException, workflowException);
	}

	private void setVariables(Map<String, String> variables, String vnfId, String type, String resourceVer) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("GENDV_vnfId", vnfId);
		variables.put("GENDV_type", type);
		variables.put("GENDV_resourceVersion", resourceVer);
	}

}
