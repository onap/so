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
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.BaseIntegrationTest;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;

/**
 * Please describe the GenericDeleteVnfTest.java class
 *
 */

public class GenericDeleteVnfIT extends BaseIntegrationTest {

	@Test
	
	public void testGenericDeleteVnf_success_genericVnf() throws Exception{
		MockDeleteGenericVnf("testVnfId123", "testReVer123");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "testReVer123");

		String processId = invokeSubProcess( "GenericDeleteVnf", variables);
		

		assertVariables("true", "true", "true", null,processId);

	}

	@Test
	
	public void testGenericDeleteVnf_success_vce() throws Exception{
		MockDeleteVce("testVnfId123", "testReVer123", 204);

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "vce", "testReVer123");

		String processId = invokeSubProcess( "GenericDeleteVnf", variables);
		

		assertVariables("true", "true", "true", null,processId);

	}

	@Test
	
	public void testGenericDeleteVnf_success_genericVnfNoResourceVersion() throws Exception{

		MockGetGenericVnfById("/testVnfId123", "GenericFlows/getGenericVnfByNameResponse.xml", 200);
		MockDeleteGenericVnf("testVnfId123", "testReVer123");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "");

		String processId = invokeSubProcess( "GenericDeleteVnf", variables);
		

		assertVariables("true", "true", "false", null,processId);

	}

	@Test
	
	public void testGenericDeleteVnf_success_vceNoResourceVersion() throws Exception{
		MockDeleteVce("testVnfId123", "testReVer123", 204);
		MockGetVceById("testVnfId123", "GenericFlows/getVceResponse.xml");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "vce", null);

		String processId = invokeSubProcess( "GenericDeleteVnf", variables);
		

		assertVariables("false", "true", "false", "WorkflowException[processKey=GenericDeleteVnf,errorCode=400,errorMessage=Unable to obtain Vnf resource-version. GET Vnf Response Does NOT Contain a resource-version,workStep=*]", processId);

	}

	@Test
	
	public void testGenericDeleteVnf_success_genericVnf404() throws Exception{
		MockDeleteGenericVnf("testVnfId123", "testReVer123", 404);

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "testReVer123");

		String processId = invokeSubProcess( "GenericDeleteVnf", variables);
		

		assertVariables("true", "false", "true", null,processId);

	}

	@Test
	
	public void testGenericDeleteVnf_success_vce404() throws Exception{
		MockDeleteVce("testVnfId123", "testReVer123", 404);

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "vce", "testReVer123");

		String processId = invokeSubProcess( "GenericDeleteVnf", variables);
		

		assertVariables("true", "false", "true", null,processId);

	}

	@Test
	
	public void testGenericDeleteVnf_success_genericVnfNoResourceVersion404() throws Exception{
		MockGetGenericVnfById_404("testVnfId123");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "");

		String processId = invokeSubProcess( "GenericDeleteVnf", variables);
		

		assertVariables("true", "false", "false", null,processId);

	}

	@Test
	
	public void testGenericDeleteVnf_error_missingVariables() throws Exception{

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "", "testReVer123");

		String processId = invokeSubProcess( "GenericDeleteVnf", variables);
		

		assertVariables("false", "false", "true", "WorkflowException[processKey=GenericDeleteVnf,errorCode=500,errorMessage=Incoming Required Variable is Missing or Null!,workStep=*]",processId);

	}

	@Test
	
	public void testGenericDeleteVnf_error_genericVnf500() throws Exception{

		MockDeleteGenericVnf_500("testVnfId123", "testReVer123");
		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "testReVer123");

		String processId = invokeSubProcess( "GenericDeleteVnf", variables);
		

		assertVariables("false", "false", "true", "WorkflowException[processKey=GenericDeleteVnf,errorCode=500,errorMessage=Received a bad response from AAI,workStep=*]",processId);

	}

	@Test
	
	public void testGenericDeleteVnf_error_genericVnf412() throws Exception{
		MockDeleteGenericVnf("testVnfId123", "testReVer123", 412);

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "generic-vnf", "testReVer123");

		String processId = invokeSubProcess( "GenericDeleteVnf", variables);
		

		assertVariables("false", "false", "true", "WorkflowException[processKey=GenericDeleteVnf,errorCode=412,errorMessage=Delete Vnf Received a resource-version Mismatch Error Response from AAI,workStep=*]",processId);

	}

	private void assertVariables(String exSuccessIndicator, String exFound, String exRVProvided, String exWorkflowException, String processId) {

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteVnf", "GENDV_SuccessIndicator",processId);
		String found = BPMNUtil.getVariable(processEngine, "GenericDeleteVnf", "GENDV_FoundIndicator",processId);
		String rvProvided = BPMNUtil.getVariable(processEngine, "GenericDeleteVnf", "GENDV_resourceVersionProvided",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericDeleteVnf", "WorkflowException",processId);

		assertEquals(exSuccessIndicator, successIndicator);
		assertEquals(exFound, found);
		assertEquals(exRVProvided, rvProvided);
		assertEquals(exWorkflowException, workflowException);
	}

	private void setVariables(Map<String, Object> variables, String vnfId, String type, String resourceVer) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("GENDV_vnfId", vnfId);
		variables.put("GENDV_type", type);
		variables.put("GENDV_resourceVersion", resourceVer);
		variables.put("mso-request-id", UUID.randomUUID().toString());
	}

}
