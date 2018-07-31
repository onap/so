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

package org.onap.so.bpmn.common.scripts

import static org.mockito.Mockito.*
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPutServiceInstance;

import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.mockito.runners.MockitoJUnitRunner

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.apache.commons.lang3.*

@RunWith(MockitoJUnitRunner.class)
@Ignore
class GenericPutServiceTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8090);

	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)

	}

	@Test
	public void preProcessRequest() {


		println "************ preProcessRequest ************* "

		ExecutionEntity mockExecution = setupMock()

		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		when(mockExecution.getVariable("GENPS_globalSubscriberId")).thenReturn("1604-MVM-26")
		when(mockExecution.getVariable("GENPS_serviceInstanceId")).thenReturn("MIS%2F1604%2F0026%2FSW_INTERNET")
		when(mockExecution.getVariable("GENPS_serviceType")).thenReturn("SDN-ETHERNET-INTERNET")
		when(mockExecution.getVariable("GENPS_serviceInstanceData")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
		when(mockExecution.getVariable("GENPS_type")).thenReturn("service-instance")

		GenericPutService  putServiceInstance= new GenericPutService()
		putServiceInstance.preProcessRequest(mockExecution)

		// check the sequence of variable invocation
		//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
		//preDebugger.printInvocations(mockExecution)

		verify(mockExecution, atLeast(1)).getVariable("isDebugLogEnabled")
		verify(mockExecution).setVariable("prefix", "GENPS_")

	//	execution.getVariable("isDebugLogEnabled")

		verify(mockExecution).setVariable("GENPS_SuccessIndicator", false)
	//	verify(mockExecution).setVariable("globalSubscriberId", "1604-MVM-26")
	//	verify(mockExecution).setVariable("serviceInstanceId", "MIS%2F1604%2F0026%2FSW_INTERNET")
	//	verify(mockExecution).setVariable("serviceType", "SDN-ETHERNET-INTERNET")
	//	verify(mockExecution).setVariable("ServiceInstanceData", "f70e927b-6087-4974-9ef8-c5e4d5847ca4")


	}


	@Test
	@Ignore
	public void putServiceInstance() {
		println "************ putServiceInstance ************* "

		WireMock.reset();

		MockPutServiceInstance("1604-MVM-26", "SDN-ETHERNET-INTERNET", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericPutServiceInstance/GenericPutServiceInstance_PutServiceInstance_AAIResponse_Success.xml");
		ExecutionEntity mockExecution = setupMock()

		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		when(mockExecution.getVariable("GENPS_globalSubscriberId")).thenReturn("1604-MVM-26")
		when(mockExecution.getVariable("GENPS_serviceInstanceId")).thenReturn("MIS%2F1604%2F0026%2FSW_INTERNET")
		when(mockExecution.getVariable("GENPS_serviceType")).thenReturn("SDN-ETHERNET-INTERNET")
		when(mockExecution.getVariable("GENPS_serviceInstanceData")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
		when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
		when(mockExecution.getVariable("mso.workflow.PutServiceInstance.aai.business.customer.uri")).thenReturn("/aai/v7/business/customers/customer")
		when(mockExecution.getVariable("GENPS_serviceInstanceData")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
		when(mockExecution.getVariable("GENPS_type")).thenReturn("service-instance")
		when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
		when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("7")
		when(mockExecution.getVariable("mso.workflow.default.aai.v7.customer.uri")).thenReturn("/aai/v7/business/customers/customer")
		when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
		when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

		GenericPutService  serviceInstance= new GenericPutService()
		serviceInstance.putServiceInstance(mockExecution)

		// check the sequence of variable invocation
		MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
		preDebugger.printInvocations(mockExecution)

		verify(mockExecution, atLeast(1)).getVariable("isDebugLogEnabled")
		verify(mockExecution).setVariable("prefix", "GENPS_")

	//	execution.getVariable("isDebugLogEnabled")
	//	verify(mockExecution).setVariable("GENPSI_serviceInstanceData","f70e927b-6087-4974-9ef8-c5e4d5847ca4")

		String servicePayload = """<service-instance xmlns="http://org.openecomp.aai.inventory/v7">f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance>""" as String
		verify(mockExecution).setVariable("GENPS_serviceInstancePayload",servicePayload)

		String serviceAaiPath = "http://localhost:28090/aai/v7/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET/service-instances/service-instance/MIS%252F1604%252F0026%252FSW_INTERNET"
		verify(mockExecution).setVariable("GENPS_putServiceInstanceAaiPath", serviceAaiPath)

		int responseCode = 200
		verify(mockExecution).setVariable("GENPS_putServiceInstanceResponseCode", responseCode)

		String aaiResponse = """<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
                   statusCode="200">
   <rest:headers>
      <rest:header name="Date" value="Thu,10 Mar 2016 00:01:18 GMT"/>
      <rest:header name="Content-Length" value="0"/>
      <rest:header name="Expires" value="Thu,01 Jan 1970 00:00:00 UTC"/>
      <rest:header name="X-AAI-TXID" value="mtcnjv9aaas03-20160310-00:01:18:551-132672"/>
      <rest:header name="Server" value="Apache-Coyote/1.1"/>
      <rest:header name="Cache-Control" value="private"/>
   </rest:headers>
</rest:RESTResponse>"""

		verify(mockExecution).setVariable("GENPS_putServiceInstanceResponse", aaiResponse)

		verify(mockExecution).setVariable("GENPS_SuccessIndicator", true)
	}

	@Test
	@Ignore
	public void putServiceInstance_404() {


		println "************ putServiceInstance ************* "

		WireMock.reset();

		ExecutionEntity mockExecution = setupMock()

		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		when(mockExecution.getVariable("GENPS_globalSubscriberId")).thenReturn("1604-MVM-26")
		when(mockExecution.getVariable("GENPS_serviceInstanceId")).thenReturn("MIS%2F1604%2F0026%2FSW_INTERNET")
		when(mockExecution.getVariable("GENPS_serviceType")).thenReturn("SDN-ETHERNET-INTERNET")
		when(mockExecution.getVariable("GENPS_ServiceInstanceData")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
		when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
		when(mockExecution.getVariable("mso.workflow.PutServiceInstance.aai.business.customer_uri")).thenReturn("/aai/v7/business/customers/customer")
		when(mockExecution.getVariable("GENPS_ServiceInstanceData")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")

		GenericPutService  serviceInstance= new GenericPutService()
		serviceInstance.putServiceInstance(mockExecution)

		// check the sequence of variable invocation
		MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
		preDebugger.printInvocations(mockExecution)

		verify(mockExecution, atLeast(1)).getVariable("isDebugLogEnabled")
		verify(mockExecution).setVariable("prefix", "GENPS_")

	//	execution.getVariable("isDebugLogEnabled")


		verify(mockExecution).setVariable("GENPS_serviceInstanceData","f70e927b-6087-4974-9ef8-c5e4d5847ca4")

		String serviceInstancepayload = """<service-instance xmlns="http://org.onap.so.aai.inventory/v7">f70e927b-6087-4974-9ef8-c5e4d5847ca4
			</service-instance>""" as String
		verify(mockExecution).setVariable("GENPS_serviceInstancePayload",serviceInstancepayload)

		String serviceInstanceAaiPath = "http://localhost:8090/aai/v7/business/customers/customer/1604-MVM-26/service-subscriptions/service-subscription/SDN-ETHERNET-INTERNET/service-instances/service-instance/MIS%252F1604%252F0026%252FSW_INTERNET"
		verify(mockExecution).setVariable("GENPS_putServiceInstanceAaiPath", serviceInstanceAaiPath)

		int responseCode = 404
		verify(mockExecution).setVariable("GENPS_putServiceInstanceResponseCode", responseCode)

		String aaiResponse = ""
		verify(mockExecution).setVariable("GENPS_putServiceInstanceResponse", aaiResponse)

		verify(mockExecution).setVariable("GENPS_SuccessIndicator", false)


	}


	private ExecutionEntity setupMock() {

		ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
		when(mockProcessDefinition.getKey()).thenReturn("PutServiceInstance")
		RepositoryService mockRepositoryService = mock(RepositoryService.class)
		when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
		when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("PutServiceInstance")
		when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
		ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
		when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		// Initialize prerequisite variables

		when(mockExecution.getId()).thenReturn("100")
		when(mockExecution.getProcessDefinitionId()).thenReturn("PutServiceInstance")
		when(mockExecution.getProcessInstanceId()).thenReturn("PutServiceInstance")
		when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
		when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

		return mockExecution
	}

}
