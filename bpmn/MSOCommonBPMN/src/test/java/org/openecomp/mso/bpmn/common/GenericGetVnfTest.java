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

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;

/**
 * Please describe the GenericGetVnfTest.java class
 *
 */
public class GenericGetVnfTest extends WorkflowTest {

	@Test
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn"})
	public void testGenericGetVnf_success_genericVnf() throws Exception{
		MockGetGenericVnfByIdWithDepth("testVnfId123", 1, "GenericFlows/getGenericVnfByNameResponse.xml");

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "testVnfName123", "generic-vnf");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_FoundIndicator");
		String vnf = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_vnf");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_getVnfByName");
		String response = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "WorkflowException");

		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("false", byName);
		assertNotNull(response);
		assertNotNull(vnf);
		assertEquals(null, workflowException);

	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn"})
	public void testGenericGetVnf_success_vce() throws Exception{
		MockGetVceById("testVnfId123[?]depth=1", "GenericFlows/getVceResponse.xml");

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "testVnfName123", "vce");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_FoundIndicator");
		String vnf = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_vnf");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_getVnfByName");
		String response = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "WorkflowException");

		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("false", byName);
		assertNotNull(response);
		assertNotNull(vnf);
		assertEquals(null, workflowException);

	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn"})
	public void testGenericGetVnf_success_genericVnfByName() throws Exception{
		MockGetGenericVnfByNameWithDepth("testVnfName123", 1, "GenericFlows/getGenericVnfResponse.xml");

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "", "testVnfName123", "generic-vnf");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_FoundIndicator");
		String vnf = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_vnf");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_getVnfByName");
		String response = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "WorkflowException");

		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("true", byName);
		assertNotNull(response);
		assertNotNull(vnf);
		assertEquals(null, workflowException);

	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn"})
	public void testGenericGetVnf_success_vceByName() throws Exception{
		MockGetGenericVceByNameWithDepth("testVnfName123", 1, "GenericFlows/getVceByNameResponse.xml");

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, null, "testVnfName123", "vce");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_FoundIndicator");
		String vnf = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_vnf");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_getVnfByName");
		String response = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "WorkflowException");

		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("true", byName);
		assertNotNull(response);
		assertNotNull(vnf);
		assertEquals(null, workflowException);

	}

	@Test
	@Ignore // BROKEN TEST (previously ignored)
	@Deployment(resources = {"subprocess/GenericGetVnf.bpmn"})
	public void testGenericGetVnf_error_genericVnf500() throws Exception{

		MockGetGenericVnfById_500("testVnfId123");

		Map<String, String> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", "testVnfName123", "generic-vnf");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetVnf", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_FoundIndicator");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "WorkflowException");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetVnf", "GENGV_getVnfByName");

		String expectedWorkflowException = "WorkflowException[processKey=GenericGetVnf,errorCode=500,errorMessage=Incoming VnfId and VnfName are null. VnfId or VnfName is required!]";

		assertEquals("false", successIndicator);
		assertEquals("false", found);
		assertEquals("false", byName);

		assertEquals(expectedWorkflowException, workflowException);
	}

	private void setVariables(Map<String, String> variables, String vnfId, String vnfName, String type) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("GENGV_vnfId", vnfId);
		variables.put("GENGV_vnfName",vnfName);
		variables.put("GENGV_type", type);
	}
}
