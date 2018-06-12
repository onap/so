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

package org.openecomp.mso.bpmn.infrastructure.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.openecomp.mso.bpmn.common.scripts.GenericDeleteVnf

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.class)
class GenericDeleteVnfTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090)

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetVnfResourceVersion() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("GENDV_type")).thenReturn("generic-vnf")
        when(mockExecution.getVariable("GENDV_vnfId")).thenReturn("vnf_test")
        when(mockExecution.getVariable('aai.endpoint')).thenReturn('http://localhost:28090')
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.generic-vnf.uri")).thenReturn('/aai/v8/network/generic-vnfs/generic-vnf')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')


        stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/vnf_test"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBody("<resource-version>v8</resource-version>")))
        GenericDeleteVnf obj = new GenericDeleteVnf()
        obj.getVnfResourceVersion(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", "GENDV_")
        Mockito.verify(mockExecution).setVariable("GENDV_getVnfResponseCode", 200)
        Mockito.verify(mockExecution).setVariable("GENDV_getVnfResponse", "<resource-version>v8</resource-version>")
        Mockito.verify(mockExecution).setVariable("GENDV_FoundIndicator", true)
        Mockito.verify(mockExecution).setVariable("GENDV_resourceVersion", "v8")
    }

    @Test
    public void testDeleteVnf() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("GENDV_type")).thenReturn("generic-vnf")
        when(mockExecution.getVariable("GENDV_vnfId")).thenReturn("vnf_test")
        when(mockExecution.getVariable("GENDV_resourceVersion")).thenReturn("8")

        when(mockExecution.getVariable('aai.endpoint')).thenReturn('http://localhost:28090')
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.generic-vnf.uri")).thenReturn('/aai/v8/network/generic-vnfs/generic-vnf')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')


        stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/vnf_test.*"))
                .willReturn(aResponse()
                .withStatus(204)))

        GenericDeleteVnf obj = new GenericDeleteVnf()
        obj.deleteVnf(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", "GENDV_")        
        Mockito.verify(mockExecution).setVariable("GENDV_deleteVnfResponseCode", 204)
        Mockito.verify(mockExecution).setVariable("GENDV_FoundIndicator", true)
        Mockito.verify(mockExecution).setVariable("GENDV_deleteVnfResponse", "")
    }

    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("GenericDeleteVnf")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("GenericDeleteVnf")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("GenericDeleteVnf")
        when(mockExecution.getProcessInstanceId()).thenReturn("GenericDeleteVnf")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }

}
