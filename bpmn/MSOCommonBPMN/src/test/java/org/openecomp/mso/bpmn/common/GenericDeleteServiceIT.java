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
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteServiceInstance_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance_500;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceSubscription;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.BaseIntegrationTest;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;



/**
 * Please describe the GenericDeleteServiceTest.java class
 *
 */

public class GenericDeleteServiceIT extends BaseIntegrationTest {
	
	@Test	
	public void testGenericDeleteService_success_serviceInstance() throws Exception{
		MockDeleteServiceInstance("1604-MVM-26", "SDN-ETHERNET-INTERNET", "MIS%252F1604%252F0026%252FSW_INTERNET", "1234");
		Map<String,Object> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", "1604-MVM-26", "SDN-ETHERNET-INTERNET", "1234");
		String processId = invokeSubProcess( "GenericDeleteService", variables);
		
		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_SuccessIndicator",processId);
		String foundIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_FoundIndicator",processId);
		String resourceVersionProvidedFlag = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_resourceVersionProvidedFlag",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "WorkflowException",processId);
		assertEquals("true", foundIndicator);
		assertEquals("true", successIndicator);
		assertEquals("true", resourceVersionProvidedFlag);
		assertEquals(null, workflowException);
	}
	
	@Test
	public void testGenericDeleteService_success_serviceSubscription() throws Exception{

		MockDeleteServiceInstance("1604-MVM-26", "SDN-ETHERNET-INTERNET", "1234", 204);

		Map<String, Object> variables = new HashMap<>();
		setVariablesSubscription(variables, "", "1604-MVM-26", "SDN-ETHERNET-INTERNET", "1234");

		String processId = invokeSubProcess( "GenericDeleteService", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_SuccessIndicator",processId);
		String foundIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_FoundIndicator",processId);
		String resourceVersionProvidedFlag = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_resourceVersionProvidedFlag",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "WorkflowException",processId);

		assertEquals("true", foundIndicator);
		assertEquals("true", successIndicator);
		assertEquals("true", resourceVersionProvidedFlag);
		assertEquals(null, workflowException);

	}

	@Test
	public void testGenericDeleteService_success_instanceNoResourceVersion() throws Exception {
		MockGetServiceInstance("1604-MVM-26", "SDN-ETHERNET-INTERNET", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceSubscription.xml");
		MockDeleteServiceInstance("1604-MVM-26", "SDN-ETHERNET-INTERNET", "MIS%252F1604%252F0026%252FSW_INTERNET", "1234");

		Map<String, Object> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", "1604-MVM-26", "SDN-ETHERNET-INTERNET", null);

		String processId = invokeSubProcess( "GenericDeleteService", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_SuccessIndicator",processId);
		String foundIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_FoundIndicator",processId);
		String resourceVersionProvidedFlag = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_resourceVersionProvidedFlag",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "WorkflowException",processId);

		assertEquals("true", foundIndicator);
		assertEquals("true", successIndicator);
		assertEquals("false", resourceVersionProvidedFlag);
		assertEquals(null, workflowException);

	}

	@Test
	public void testGenericDeleteService_success_subscriptionNoResourceVersion() throws Exception{
		MockGetServiceSubscription("1604-MVM-26", "SDN-ETHERNET-INTERNET", "GenericFlows/getServiceSubscription.xml");
		MockDeleteServiceInstance("1604-MVM-26", "SDN-ETHERNET-INTERNET", "1234", 204);

		Map<String, Object> variables = new HashMap<>();
		setVariablesSubscription(variables, null, "1604-MVM-26", "SDN-ETHERNET-INTERNET", null);

		String processId = invokeSubProcess( "GenericDeleteService", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_SuccessIndicator",processId);
		String foundIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_FoundIndicator",processId);
		String resourceVersionProvidedFlag = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_resourceVersionProvidedFlag",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "WorkflowException",processId);

		assertEquals("true", foundIndicator);
		assertEquals("true", successIndicator);
		assertEquals("false", resourceVersionProvidedFlag);
		assertEquals(null, workflowException);

	}

	@Test
	public void testGenericDeleteService_success_get404Response() throws Exception{

		MockGetServiceInstance_404("1604-MVM-26", "SDN-ETHERNET-INTERNET", "MIS%252F1604%252F0026%252FSW_INTERNET");

		Map<String, Object> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", "1604-MVM-26", "SDN-ETHERNET-INTERNET", null);

		String processId = invokeSubProcess( "GenericDeleteService", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_SuccessIndicator",processId);
		String foundIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_FoundIndicator",processId);
		String resourceVersionProvidedFlag = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_resourceVersionProvidedFlag",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "WorkflowException",processId);

		assertEquals("false", foundIndicator);
		assertEquals("true", successIndicator);
		assertEquals("false", resourceVersionProvidedFlag);
		assertEquals(null, workflowException);
	}

	@Test
	public void testGenericDeleteService_success_delete404Response() throws Exception{
		MockGetServiceInstance("1604-MVM-26", "SDN-ETHERNET-INTERNET", "MIS%252F1604%252F0026%252FSW_INTERNET", "GENDSI_getServiceInstanceResponse.xml");
		MockDeleteServiceInstance_404("1604-MVM-26", "SDN-ETHERNET-INTERNET", "MIS%252F1604%252F0026%252FSW_INTERNET", "1234");

		Map<String, Object> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", "1604-MVM-26", "SDN-ETHERNET-INTERNET", "1234");

		String processId = invokeSubProcess( "GenericDeleteService", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_SuccessIndicator",processId);
		String foundIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_FoundIndicator",processId);
		String resourceVersionProvidedFlag = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_resourceVersionProvidedFlag",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "WorkflowException",processId);

		assertEquals("false", foundIndicator);
		assertEquals("true", successIndicator);
		assertEquals("true", resourceVersionProvidedFlag);
		assertEquals(null, workflowException);
	}

	@Test
//	@Ignore //This doesn't appear to be a valid test. There is no way to get this to work
	public void testGenericDeleteService_success_subscriptionGetEmpty200() throws Exception{
		MockGetServiceInstance("1604-MVM-26", "SDN-ETHERNET-INTERNET", 200);

		Map<String, Object> variables = new HashMap<>();
		setVariablesSubscription(variables, "", "1604-MVM-26", "SDN-ETHERNET-INTERNET", "");

		String processId = invokeSubProcess( "GenericDeleteService", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_SuccessIndicator",processId);
		String foundIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_FoundIndicator",processId);
		String resourceVersionProvidedFlag = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_resourceVersionProvidedFlag",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "WorkflowException",processId);
		
		String expectedResponse = "WorkflowException[processKey=GenericDeleteService,errorCode=2500,errorMessage=Internal Error - Occured During GetServiceResourceVersion,workStep=*]";
		
		assertEquals("true", foundIndicator);
		assertEquals("true", successIndicator);
		assertEquals("false", resourceVersionProvidedFlag);
		assertEquals(expectedResponse, workflowException);

	}

	@Test
	public void testGenericDeleteService_error_invalidVariables() throws Exception{

		Map<String, Object> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", null, "SDN-ETHERNET-INTERNET", "1234");

		String processId = invokeSubProcess( "GenericDeleteService", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_SuccessIndicator",processId);
		String foundIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_FoundIndicator",processId);
		String resourceVersionProvidedFlag = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_resourceVersionProvidedFlag",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "WorkflowException",processId);

		String expectedResponse = "WorkflowException[processKey=GenericDeleteService,errorCode=500,errorMessage=Incoming Required Variable is Missing or Null!,workStep=*]";

		assertEquals("false", foundIndicator);
		assertEquals("false", successIndicator);
		assertEquals("true", resourceVersionProvidedFlag);
		assertEquals(expectedResponse, workflowException);

	}

	@Test
	public void testGenericDeleteService_error_getBadAAIResponse() throws Exception{

		MockGetServiceInstance_500("1604-MVM-26", "SDN-ETHERNET-INTERNET", "MIS%252F1604%252F0026%252FSW_INTERNET", "aaiFault.xml");

		Map<String, Object> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", "1604-MVM-26", "SDN-ETHERNET-INTERNET", null);

		String processId = invokeSubProcess( "GenericDeleteService", variables);
		

		String successIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_SuccessIndicator",processId);
		String foundIndicator = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_FoundIndicator",processId);
		String resourceVersionProvidedFlag = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "GENDS_resourceVersionProvidedFlag",processId);
		String workflowException = BPMNUtil.getVariable(processEngine, "GenericDeleteService", "WorkflowException",processId);

		String expectedResponse = "WorkflowException[processKey=GenericDeleteService,errorCode=500,errorMessage=<requestError><serviceException><messageId>SVC3002</messageId><text>Error writing output performing %1 on %2 (msg=%3) (ec=%4)</text><variables><variable>PUTcustomer</variable><variable>SubName01</variable><variable>Unexpected error reading/updating database:Adding this property for key [service-instance-id] and value [USSTU2CFCNC0101UJZZ01] violates a uniqueness constraint [service-instance-id]</variable><variable>ERR.5.4.5105</variable></variables></serviceException></requestError>" + "\n" +
		",workStep=*]";

		assertEquals("false", foundIndicator);
		assertEquals("false", successIndicator);
		assertEquals("false", resourceVersionProvidedFlag);
		assertEquals(expectedResponse, workflowException);
	}


	private void setVariablesInstance(Map<String, Object> variables, String siId, String globalCustId, String serviceType, String reVersion) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("GENDS_serviceInstanceId", siId);
		variables.put("GENDS_globalCustomerId",globalCustId);
		variables.put("GENDS_serviceType", serviceType);
		variables.put("GENDS_resourceVersion", reVersion);
		variables.put("GENDS_type", "service-instance");
		variables.put("mso-request-id", UUID.randomUUID().toString());
	}

	private void setVariablesSubscription(Map<String, Object> variables, String siId, String globalCustId, String serviceType, String reVersion) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("GENDS_serviceInstanceId", siId);
		variables.put("GENDS_globalCustomerId",globalCustId);
		variables.put("GENDS_serviceType", serviceType);
		variables.put("GENDS_resourceVersion", reVersion);
		variables.put("GENDS_type", "service-subscription");
		variables.put("mso-request-id", UUID.randomUUID().toString());
	}


}
