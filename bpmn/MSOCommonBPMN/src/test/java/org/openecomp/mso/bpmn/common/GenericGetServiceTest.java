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
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance_500;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceSubscription;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById_500;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceByName;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceByName_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceByName_500;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;


/**
 * Unit Test for the GenericGetService Sub Flow
 */
public class GenericGetServiceTest extends WorkflowTest {


	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceInstance() throws Exception{
		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");
		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", null, "SDN-ETHERNET-INTERNET", "123456789");
		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());
		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String response = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("false", obtainUrl);
		assertEquals("false", byName);
		assertNotNull(response);
		assertEquals(null, workflowException);
	}


	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceSubscription() throws Exception{

		MockGetServiceSubscription("1604-MVM-26", "SDN-ETHERNET-INTERNET", "GenericFlows/getServiceSubscription.xml");

		Map<String, String> variables = new HashMap<>();
		setVariablesSubscription(variables, "", null , "1604-MVM-26", "SDN-ETHERNET-INTERNET");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String response = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");

		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("false", obtainUrl);
		assertEquals("false", byName);
		assertNotNull(response);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceInstance_byName() throws Exception{

		MockNodeQueryServiceInstanceByName("1604-MVM-26", "GenericFlows/getSIUrlByName.xml");
		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, null, "1604-MVM-26", null, null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String response = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		String siUrlResponseCode = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainSIUrlResponseCode");

		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("true", obtainUrl);
		assertEquals("true", byName);
		assertNotNull(response);
		assertEquals("200", siUrlResponseCode);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceInstance_byId() throws Exception{

		MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlById.xml");
		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", null, null, null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String response = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		String siUrlResponseCode = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_genericQueryResponseCode");

		assertEquals("true", successIndicator);
		assertEquals("true", found);
		assertEquals("true", obtainUrl);
		assertEquals("false", byName);
		assertNotNull(response);
		assertEquals("200", siUrlResponseCode);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceInstance_404Response() throws Exception{

		MockGetServiceInstance_404("SDN-ETHERNET-INTERNET", "123456789", "MIS%2F1604%2F0026%2FSW_INTERNET");

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", null, "SDN-ETHERNET-INTERNET", "123456789");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");

		assertEquals("true", successIndicator);
		assertEquals("false", found);
		assertEquals("false", obtainUrl);
		assertEquals("false", byName);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceSubscription404() throws Exception{
		MockGetServiceSubscription("1604-MVM-26", "SDN-ETHERNET-INTERNET", 404);
		
		Map<String, String> variables = new HashMap<>();
		setVariablesSubscription(variables, "", "", "SDN-ETHERNET-INTERNET", "1604-MVM-26");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String response = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");

		assertEquals("true", successIndicator);
		assertEquals("false", found);
		assertEquals("false", obtainUrl);
		assertEquals("false", byName);
		assertNotNull(response);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceInstanceByName404() throws Exception{

		MockNodeQueryServiceInstanceByName_404("1604-MVM-26");

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, "", "1604-MVM-26", null, null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		String siUrlResponseCode = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainSIUrlResponseCode");

		assertEquals("true", successIndicator);
		assertEquals("false", found);
		assertEquals("true", obtainUrl);
		assertEquals("true", byName);
		assertEquals("404", siUrlResponseCode);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceInstanceById404() throws Exception{

		MockNodeQueryServiceInstanceById_404("MIS%2F1604%2F0026%2FSW_INTERNET");

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", null, null, null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		String siUrlResponseCode = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_genericQueryResponseCode");

		assertEquals("true", successIndicator);
		assertEquals("false", found);
		assertEquals("true", obtainUrl);
		assertEquals("false", byName);
		assertEquals("404", siUrlResponseCode);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceInstanceEmptyResponse() throws Exception{

		MockGetServiceInstance("1604-MVM-26", "SDN-ETHERNET-INTERNET", "MIS%252F1604%252F0026%252FSW_INTERNET", " ");

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", null, "SDN-ETHERNET-INTERNET", "123456789");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");

		assertEquals("true", successIndicator);
		assertEquals("false", found);
		assertEquals("false", obtainUrl);
		assertEquals("false", byName);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceInstanceByNameEmpty() throws Exception{
		MockNodeQueryServiceInstanceByName("1604-MVM-26", "");
		
		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, "", "1604-MVM-26", null, null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		String siUrlResponseCode = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainSIUrlResponseCode");

		assertEquals("true", successIndicator);
		assertEquals("false", found);
		assertEquals("true", obtainUrl);
		assertEquals("true", byName);
		assertEquals("200", siUrlResponseCode);
		assertEquals(null, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceInstanceByIdEmpty() throws Exception{

	        MockNodeQueryServiceInstanceById("MIS[%]2F1604[%]2F0026[%]2FSW_INTERNET", "");

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", null, null, null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		String siUrlResponseCode = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_genericQueryResponseCode");

		assertEquals("true", successIndicator);
		assertEquals("false", found);
		assertEquals("true", obtainUrl);
		assertEquals("false", byName);
		assertEquals("200", siUrlResponseCode);
		assertEquals(null, workflowException);
	}


	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_error_serviceInstanceInvalidVariables() throws Exception{

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, null, null, "SDN-ETHERNET-INTERNET", null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");

		String expectedWorkflowException = "WorkflowException[processKey=GenericGetService,errorCode=500,errorMessage=Incoming serviceInstanceId and serviceInstanceName are null. ServiceInstanceId or ServiceInstanceName is required to Get a service-instance.]";

		assertEquals("false", successIndicator);
		assertEquals("false", found);
		assertEquals("false", obtainUrl);
		assertEquals("false", byName);
		assertEquals(expectedWorkflowException, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceSubscriptionInvalidVariables() throws Exception{

		Map<String, String> variables = new HashMap<>();
		setVariablesSubscription(variables, "", "", "SDN-ETHERNET-INTERNET", null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");

		String expectedWorkflowException = "WorkflowException[processKey=GenericGetService,errorCode=500,errorMessage=Incoming ServiceType or GlobalCustomerId is null. These variables are required to Get a service-subscription.]";


		assertEquals("false", successIndicator);
		assertEquals("false", found);
		assertEquals("false", obtainUrl);
		assertEquals("false", byName);
		assertEquals(expectedWorkflowException, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_error_serviceInstance_getSIBadResponse() throws Exception{

		MockGetServiceInstance_500("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET");

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", "1604-MVM-26", "SDN-ETHERNET-INTERNET", "123456789");

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");

		String expectedWorkflowException = "WorkflowException[processKey=GenericGetService,errorCode=500,errorMessage=Received a bad response from AAI]";

		assertEquals("false", successIndicator);
		assertEquals("false", found);
		assertEquals("false", obtainUrl);
		assertEquals("false", byName);
		assertEquals(expectedWorkflowException, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_error_serviceInstance_getUrlByIdBadResponse() throws Exception{

		MockNodeQueryServiceInstanceById_500("MIS%2F1604%2F0026%2FSW_INTERNET");

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, "MIS%2F1604%2F0026%2FSW_INTERNET", null, null, null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String siUrlResponseCode = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_genericQueryResponseCode");

		String expectedWorkflowException = "WorkflowException[processKey=GenericGetService,errorCode=500,errorMessage=Received a bad response from AAI]";

		assertEquals("false", successIndicator);
		assertEquals("false", found);
		assertEquals("true", obtainUrl);
		assertEquals("false", byName);
		assertEquals("500", siUrlResponseCode);
		assertEquals(expectedWorkflowException, workflowException);
	}

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_error_serviceInstance_getUrlByNameBadResponse() throws Exception{

		MockNodeQueryServiceInstanceByName_500("1604-MVM-26");

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, null, "1604-MVM-26", null, null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		String obtainUrl = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainObjectsUrl");
		String byName = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainServiceInstanceUrlByName");
		String siUrlResponseCode = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainSIUrlResponseCode");

		String expectedWorkflowException = "WorkflowException[processKey=GenericGetService,errorCode=500,errorMessage=Received a bad response from AAI]";

		assertEquals("false", successIndicator);
		assertEquals("false", found);
		assertEquals("true", obtainUrl);
		assertEquals("true", byName);
		assertEquals("500", siUrlResponseCode);
		assertEquals(expectedWorkflowException, workflowException);
	}

    @Test
    @Deployment(resources = {"subprocess/GenericGetService.bpmn"})
    public void testGenericGetService_success_serviceInstance_byNameServicePresent() throws Exception{

        MockNodeQueryServiceInstanceByName("1604-MVM-26", "GenericFlows/getSIUrlByNameMultiCustomer.xml");
        MockGetServiceInstance("XyCorporation", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");

        Map<String, String> variables = new HashMap<>();
        setVariablesInstance(variables, null, "1604-MVM-26", "XyCorporation", null);

        WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
        waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

        String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
        String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
        String resourceLink = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_resourceLink");
        String response = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowResponse");
        String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
        String siUrlResponseCode = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainSIUrlResponseCode");

        assertEquals("true", successIndicator);
        assertEquals("true", found);
		assertNotNull(resourceLink);
        assertNotNull(response);
        assertEquals("200", siUrlResponseCode);
        assertEquals(null, workflowException);
    }

	@Test
	@Deployment(resources = {"subprocess/GenericGetService.bpmn"})
	public void testGenericGetService_success_serviceInstance_byNameServiceNotPresent() throws Exception{

		MockNodeQueryServiceInstanceByName("1604-MVM-26", "GenericFlows/getSIUrlByNameMultiCustomer.xml");
		MockGetServiceInstance("CorporationNotPresent", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");

		Map<String, String> variables = new HashMap<>();
		setVariablesInstance(variables, null, "1604-MVM-26", "CorporationNotPresent", null);

		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "GenericGetService", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String successIndicator = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_SuccessIndicator");
		String found = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_FoundIndicator");
		String resourceLink = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_resourceLink");
		String response = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowResponse");
		String workflowException = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "WorkflowException");
		String siUrlResponseCode = BPMNUtil.getVariable(processEngineRule, "GenericGetService", "GENGS_obtainSIUrlResponseCode");

		assertEquals("true", successIndicator);
		assertEquals("false", found);
		assertEquals(null, resourceLink);
		assertEquals("  ", response);
		assertEquals("200", siUrlResponseCode);
		assertEquals(null, workflowException);
	}

	private void setVariablesInstance(Map<String, String> variables, String siId, String siName, String globalCustId, String serviceType) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("GENGS_serviceInstanceId", siId);
		variables.put("GENGS_serviceInstanceName", siName);
		variables.put("GENGS_globalCustomerId",globalCustId);
		variables.put("GENGS_serviceType", serviceType);
		variables.put("GENGS_type", "service-instance");
	}

	private void setVariablesSubscription(Map<String, String> variables, String siId, String siName, String globalCustId, String serviceType) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("GENGS_serviceInstanceId", siId);
		variables.put("GENGS_serviceInstanceName", siName);
		variables.put("GENGS_globalCustomerId",globalCustId);
		variables.put("GENGS_serviceType", serviceType);
		variables.put("GENGS_type", "service-subscription");
	}


}
