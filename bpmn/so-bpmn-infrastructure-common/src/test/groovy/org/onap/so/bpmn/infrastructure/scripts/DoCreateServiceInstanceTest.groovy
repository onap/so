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

package org.onap.so.bpmn.infrastructure.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
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
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInstance
import org.onap.so.bpmn.mock.StubResponseAAI

import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DoCreateServiceInstanceTest {
    def prefix = "DCRESI_"

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090)

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
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("MIS/1604/0026/SW_INTERNET")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("MDTWNJ21")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("testRequestId-1503410089303")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:28080/mso/SDNCAdapterCallbackService")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("MSO_dev")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("MSO-dev-service-type")
        when(mockExecution.getVariable("productFamilyId")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable("sdnc.si.svc.types")).thenReturn("PORT-MIRROR,PPROBES")

        ServiceDecomposition decomposition = new ServiceDecomposition()
        ModelInfo modelInfo = new ModelInfo()
        ServiceInstance instance = new ServiceInstance()
        instance.instanceId = "12345"
        decomposition.modelInfo = modelInfo
        decomposition.serviceInstance = instance

        when(mockExecution.getVariable("serviceDecomposition")).thenReturn(decomposition)
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.default.aai.customer.version")).thenReturn("8")


        DoCreateServiceInstance obj = new DoCreateServiceInstance()
        obj.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", prefix)
        verify(mockExecution).setVariable("sdncCallbackUrl", "http://localhost:28080/mso/SDNCAdapterCallbackService")
    }



    @Test
    void testGetAAICustomerById() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("12345")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.DoCreateServiceInstance.aai.version")).thenReturn('8')
        StubResponseAAI.MockGetCustomer("12345", "")
        DoCreateServiceInstance obj = new DoCreateServiceInstance()
        obj.getAAICustomerById(mockExecution)

        verify(mockExecution, times(1)).getVariable("aai.endpoint")
    }

    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoCreateServiceInstance")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoCreateServiceInstance")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DoCreateServiceInstance")
        when(mockExecution.getProcessInstanceId()).thenReturn("DoCreateServiceInstance")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }
}
