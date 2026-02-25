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
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
@Ignore
class GenerateVfModuleNameTest {

    @Captor
    ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class);


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    @Test
    public void testQueryAAI() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("aai.endpoint")).thenReturn('http://localhost:8090')
        when(mockExecution.getVariable("vnfId")).thenReturn('skask')
        when(mockExecution.getVariable("personaModelId")).thenReturn('personaModelId_test')
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')

        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
        when(mockExecution.getVariable("mso.workflow.default.aai.generic-vnf.version")).thenReturn('8')

        mockData()
        GenerateVfModuleName obj = new GenerateVfModuleName()
        obj.queryAAI(mockExecution)

        Mockito.verify(mockExecution).setVariable("GVFMN_queryAAIVfModuleResponseCode", 200)
    }

    @Test
    public void testQueryAAIEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("vnfId")).thenReturn('skask')
        when(mockExecution.getVariable("personaModelId")).thenReturn('personaModelId_test')
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn('true')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')

        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
        when(mockExecution.getVariable("mso.workflow.default.aai.generic-vnf.version")).thenReturn('8')

        mockData()
        try {
            GenerateVfModuleName obj = new GenerateVfModuleName()
            obj.queryAAI(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(1002, workflowException.getErrorCode())
        Assert.assertEquals("AAI GET Failed:org.apache.http.client.ClientProtocolException", workflowException.getErrorMessage())
    }


    private void mockData() {
        stubFor(get(urlMatching(".*/aai/v[0-9]+/network/generic-vnfs/generic-vnf.*"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("GenericFlows/getGenericVnfResponse.xml")))
    }

    private ExecutionEntity setupMock() {

        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("GenerateVfModuleName")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("GenerateVfModuleName")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("GenerateVfModuleName")
        when(mockExecution.getProcessInstanceId()).thenReturn("GenerateVfModuleName")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)
        return mockExecution
    }
}
