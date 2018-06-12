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
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutVce;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.BaseIntegrationTest;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;

/**
 * Please describe the GenericPutVnf.java class
 *
 */

public class GenericPutVnfIT extends BaseIntegrationTest {

	private String genericVnfPayload = "<generic-vnf><vnf-id>testId</vnf-id></generic-vnf>";
	private String vcePayload = "<vce><vnf-id>testId</vnf-id></vce>";

	@Test
	
	public void testGenericPutVnf_success_genericVnf() throws Exception{

		MockPutGenericVnf("testVnfId123");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", genericVnfPayload, "generic-vnf");

		String processId = invokeSubProcess( "GenericPutVnf", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "GENPV_SuccessIndicator",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "WorkflowException",processId);

		assertEquals("true", successIndicator);
		assertEquals(null, workflowException);

	}

	@Test
	
	public void testGenericPutVnf_success_vce() throws Exception{

		MockPutVce("testVnfId123");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", vcePayload, "vce");

		String processId = invokeSubProcess( "GenericPutVnf", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "GENPV_SuccessIndicator",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "WorkflowException",processId);

		assertEquals("true", successIndicator);
		assertEquals(null, workflowException);

	}

	@Test
	
	public void testGenericPutVnf_error_missingType() throws Exception{

		MockPutGenericVnf("testVnfId123");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", genericVnfPayload, "");

		String processId = invokeSubProcess( "GenericPutVnf", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "GENPV_SuccessIndicator",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "WorkflowException",processId);

		String expectedWFEX = "WorkflowException[processKey=GenericPutVnf,errorCode=500,errorMessage=Incoming Vnf Payload and/or Type is null. These Variables are required!,workStep=*]";

		assertEquals("false", successIndicator);
		assertEquals(expectedWFEX, workflowException);

	}

	@Test
	
	public void testGenericPutVnf_error_missingPayload() throws Exception{

		MockPutGenericVnf("testVnfId123");

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", genericVnfPayload, "");

		String processId = invokeSubProcess( "GenericPutVnf", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "GENPV_SuccessIndicator",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "WorkflowException",processId);

		String expectedWFEX = "WorkflowException[processKey=GenericPutVnf,errorCode=500,errorMessage=Incoming Vnf Payload and/or Type is null. These Variables are required!,workStep=*]";

		assertEquals("false", successIndicator);
		assertEquals(expectedWFEX, workflowException);

	}

	@Test
	
	public void testGenericPutVnf_error_404() throws Exception{
		MockPutGenericVnf("/testVnfId123", 404);
		
		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", genericVnfPayload, "generic-vnf");

		String processId = invokeSubProcess( "GenericPutVnf", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "GENPV_SuccessIndicator",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "WorkflowException",processId);

		String expectedWFEX = "WorkflowException[processKey=GenericPutVnf,errorCode=404,errorMessage=Received a bad response from AAI,workStep=*]";

		assertEquals("false", successIndicator);
		assertEquals(expectedWFEX, workflowException);
	}

	@Test
	
	public void testGenericPutVnf_error_400() throws Exception{

		MockPutGenericVnf("/testVnfId123", 400);

		Map<String, Object> variables = new HashMap<>();
		setVariables(variables, "testVnfId123", genericVnfPayload, "generic-vnf");

		String processId = invokeSubProcess( "GenericPutVnf", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "GENPV_SuccessIndicator",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericPutVnf", "WorkflowException",processId);

		String expectedWFEX = "WorkflowException[processKey=GenericPutVnf,errorCode=400,errorMessage=Received a bad response from AAI,workStep=*]";

		assertEquals("false", successIndicator);
		assertEquals(expectedWFEX, workflowException);


	}

	private void setVariables(Map<String, Object> variables, String vnfId, String payload, String type) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("GENPV_vnfId", vnfId);
		variables.put("GENPV_vnfPayload",payload);
		variables.put("GENPV_type", type);
		variables.put("mso-request-id", UUID.randomUUID().toString());
	}


}
