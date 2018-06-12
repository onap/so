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
import static org.junit.Assert.assertNotNull;
import static org.openecomp.mso.bpmn.common.BPMNUtil.executeWorkFlow;
import static org.openecomp.mso.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByIdWithDepth;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfById_500;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVnfByNameWithDepth;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetGenericVceByNameWithDepth;
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
 * Please describe the GenericGetVnfTest.java class
 *
 */

public class GenericGetVnfIT extends BaseIntegrationTest {

	@Test
	
	public void testGenericGetVnf_success_genericVnf() throws Exception{
		MockGetGenericVnfByIdWithDepth("testVnfId123", 1, "GenericFlows/getGenericVnfByNameResponse.xml");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "testVnfName123", "generic-vnf");

		String processId = invokeSubProcess(  "GenericGetVnf", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_SuccessIndicator",processId);
		String found = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_FoundIndicator",processId);
		String vnf = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_vnf",processId);
		String byName = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_getVnfByName",processId);
		String response = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "WorkflowResponse",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "WorkflowException",processId);

		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("false", byName);
		assertNotNull(response);
		assertNotNull(vnf);
		assertEquals(null, workflowException);

	}

	@Test
	
	public void testGenericGetVnf_success_vce() throws Exception{
		MockGetVceById("testVnfId123[?]depth=1", "GenericFlows/getVceResponse.xml");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "testVnfName123", "vce");

		String processId = invokeSubProcess(  "GenericGetVnf", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_SuccessIndicator",processId);
		String found = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_FoundIndicator",processId);
		String vnf = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_vnf",processId);
		String byName = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_getVnfByName",processId);
		String response = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "WorkflowResponse",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "WorkflowException",processId);

		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("false", byName);
		assertNotNull(response);
		assertNotNull(vnf);
		assertEquals(null, workflowException);

	}

	@Test
	
	public void testGenericGetVnf_success_genericVnfByName() throws Exception{
		MockGetGenericVnfByNameWithDepth("testVnfName123", 1, "GenericFlows/getGenericVnfResponse.xml");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "", "testVnfName123", "generic-vnf");

		String processId = invokeSubProcess(  "GenericGetVnf", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_SuccessIndicator",processId);
		String found = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_FoundIndicator",processId);
		String vnf = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_vnf",processId);
		String byName = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_getVnfByName",processId);
		String response = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "WorkflowResponse",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "WorkflowException",processId);

		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("true", byName);
		assertNotNull(response);
		assertNotNull(vnf);
		assertEquals(null, workflowException);

	}

	@Test
	
	public void testGenericGetVnf_success_vceByName() throws Exception{
		MockGetGenericVceByNameWithDepth("testVnfName123", 1, "GenericFlows/getVceByNameResponse.xml");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, null, "testVnfName123", "vce");

		String processId = invokeSubProcess(  "GenericGetVnf", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_SuccessIndicator",processId);
		String found = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_FoundIndicator",processId);
		String vnf = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_vnf",processId);
		String byName = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_getVnfByName",processId);
		String response = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "WorkflowResponse",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "WorkflowException",processId);

		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("true", byName);
		assertNotNull(response);
		assertNotNull(vnf);
		assertEquals(null, workflowException);

	}

	

	@Test
	public void testGenericGetVnf_error_genericVnf500() throws Exception{

		MockGetGenericVnfById_500("testVnfId123_500");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123_500", "testVnfId123_500", "generic-vnf");

		String processId = invokeSubProcess(  "GenericGetVnf", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_SuccessIndicator",processId);
		String found = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_FoundIndicator",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "WorkflowException",processId);
		String byName = BPMNUtil.getVariable(processEngine, "GenericGetVnf", "GENGV_getVnfByName",processId);

		String expectedWorkflowException = "WorkflowException[processKey=GenericGetVnf,errorCode=500,errorMessage=Received a bad response from AAI,workStep=*]";

		assertEquals("false", successIndicator);
		assertEquals("false", found);
		assertEquals("false", byName);

		assertEquals(expectedWorkflowException, workflowException);
	}

	private void setVariables(Map<String, Object> variables, String vnfId, String vnfName, String type) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("GENGV_vnfId", vnfId);
		variables.put("GENGV_vnfName",vnfName);
		variables.put("GENGV_type", type);
		variables.put("mso-request-id", UUID.randomUUID().toString());
	}
}
