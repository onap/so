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

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
@Ignore
class GenericGetServiceTest {

    def prefix = "GENGS_"
    @Captor
    ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class);

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    @Test
    public void testGetServiceObject() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
        when(mockExecution.getVariable("aai.endpoint")).thenReturn('http://localhost:8090')
        when(mockExecution.getVariable(prefix + "type")).thenReturn('service-instance')
        when(mockExecution.getVariable(prefix + "serviceInstanceId")).thenReturn('12345')
        when(mockExecution.getVariable(prefix + "resourceLink")).thenReturn("/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')

        mockData()
        GenericGetService obj = new GenericGetService()
        obj.getServiceObject(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", prefix)
        Mockito.verify(mockExecution).setVariable(prefix + "getServiceUrl", "http://localhost:8090/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/")
        Mockito.verify(mockExecution).setVariable(prefix + "getServiceResponseCode", 200)
    }

    @Test
    public void testGetServiceObjectEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable(prefix + "type")).thenReturn('service-instance')
        when(mockExecution.getVariable(prefix + "serviceInstanceId")).thenReturn('12345')
        when(mockExecution.getVariable(prefix + "resourceLink")).thenReturn("/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')

        mockData()
        try {
            GenericGetService obj = new GenericGetService()
            obj.getServiceObject(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        Mockito.verify(mockExecution, times(3)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(9999, workflowException.getErrorCode())
        Assert.assertEquals("org.apache.http.client.ClientProtocolException", workflowException.getErrorMessage())
    }

    private void mockData() {
        stubFor(get(urlMatching(".*/aai/v[0-9]+/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/.*"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("")))
    }

    private ExecutionEntity setupMock() {

        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("GenericGetService")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("GenericGetService")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("GenericGetService")
        when(mockExecution.getProcessInstanceId()).thenReturn("GenericGetService")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)
        return mockExecution
    }
}
