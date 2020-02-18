/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.onap.so.bpmn.common.scripts.DecomposeOpticalService
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.PInterface

import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DecomposeOpticalServiceTest {
	def prefix = "DMS_"
	def uuiRequest = "{\n" +
    "\t\"service\":{\n" +
       "\t\"name\":\"svc-Customer1-orange-001\",\n" +
       "\t\"description\":\"svc-Customer1-orange-001\",\n" +
       "\t\"serviceInvariantUuid\":\"71512640-7aaa-4732-9ce2-dbd91fa3468b\",\n" +   
       "\t\"serviceUuid\":\"401daff5-f13f-4502-8a28-ea638e3344ce\",\n" +
       "\t\"globalSubscriberId\":\"Orange\",\n" +
       "\t\"serviceType\":\"MDONS_OTN\",\n" +            
       "\t\"parameters\":{\n" +
          "\t\t\"requestInputs\":{\n" +     
            "\t\t\"name\": \"svc-Customer1-orange-001\",\n" +
            "\t\t\"customer\": \"customer\",\n" +
            "\t\t\"service_provider\": \"Orange\",\n" +
            "\t\t\"uni_id\": \"5eb2e487-1176-4cbe-b4c0-0d9bd88aedcf\",\n" +
            "\t\t\"uni_client-proto\": \"Ethernet\",\n" +
            "\t\t\"uni_coding-func\": \"10GBASE-R\",\n" +
            "\t\t\"uni_optical-interface\": \"LR\",\n" +
            "\t\t\"enni_id\": \"64e6c1ef-981c-4a99-860f-4c0fd4ffeb97\"\n" +
          "\t\t}\n" +
       "\t}\n" +
    "\t}\"\n" +
    "\t}"

	@Captor
	static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

	@Before
	void init() throws IOException {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void testPreProcessRequest() {
		ExecutionEntity mockExecution = setupMock()
		when(mockExecution.getVariable("prefix")).thenReturn(prefix)
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		when(mockExecution.getVariable("rate")).thenReturn("10")
		when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5bb51ace-bea1-4948-ae16-51e970d74e62")
		when(mockExecution.getVariable("uniIdPort")).thenReturn("5eb2e487-1176-4cbe-b4c0-0d9bd88aedcf")
		when(mockExecution.getVariable("enniIdPort")).thenReturn("64e6c1ef-981c-4a99-860f-4c0fd4ffeb97")
		when(mockExecution.getVariable("uuiRequest")).thenReturn(uuiRequest)
		when(mockExecution.getVariable("uniObj")).thenReturn(new PInterface())
		when(mockExecution.getVariable("enniObj")).thenReturn(new PInterface())

		when(mockExecution.getVariable("isMultiDomain")).thenReturn("false")
		when(mockExecution.getVariable("uniDomainType")).thenReturn("MSA")

		when(mockExecution.getVariable("serviceInstanceName")).thenReturn("serviceInstanceName")
		when(mockExecution.getVariable("serviceType")).thenReturn("MDONS_OTN")
		when(mockExecution.getVariable("allDomainsFinished")).thenReturn("false")

		DecomposeOpticalService obj = new DecomposeOpticalService()
		obj.preProcessRequest(mockExecution)

		Mockito.verify(mockExecution, times(31)).setVariable(captor.capture(), captor.capture())
		List list = captor.getAllValues()
		String str = list.get(51)
		Assert.assertEquals("http://localhost:28080/mso/SDNCAdapterCallbackService", str)
	}
 
	private static ExecutionEntity setupMock() {
		ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
		when(mockProcessDefinition.getKey()).thenReturn("DecomposeOpticalService")
		RepositoryService mockRepositoryService = mock(RepositoryService.class)
		when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
		when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DecomposeOpticalService")
		when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
		ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
		when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		// Initialize prerequisite variables
		when(mockExecution.getId()).thenReturn("100")
		when(mockExecution.getProcessDefinitionId()).thenReturn("DecomposeOpticalService")
		when(mockExecution.getProcessInstanceId()).thenReturn("DecomposeOpticalService")
		when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
		when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

		return mockExecution
	}
}
