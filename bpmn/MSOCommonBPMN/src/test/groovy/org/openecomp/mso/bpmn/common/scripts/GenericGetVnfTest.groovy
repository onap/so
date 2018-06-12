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

package org.openecomp.mso.bpmn.common.scripts

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
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.openecomp.mso.bpmn.core.WorkflowException

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.class)
@Ignore
class GenericGetVnfTest {

    def prefix = "GENGV_"

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090)

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)


    @Before
    public void init()
    {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    public void testGetVnfByName() {

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable(prefix+"vnfName")).thenReturn("genricVnf_test")
        when(mockExecution.getVariable(prefix+"type")).thenReturn("generic-vnf")
        when(mockExecution.getVariable("mso.workflow.DoCreateVfModule.aai.generic-vnf.uri")).thenReturn("/aai/v9/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        GenericGetVnf obj = new GenericGetVnf()
        obj.getVnfByName(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix","GENGV_")
        Mockito.verify(mockExecution).setVariable(prefix+"getVnfPath","http://localhost:28090/aai/v9/network/generic-vnfs/generic-vnf?vnf-name=genricVnf_test&depth=1")
    }

    @Test
    public void testGetVnfByNameEndpointNull() {

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable(prefix+"vnfName")).thenReturn("genricVnf_test")
        when(mockExecution.getVariable(prefix+"type")).thenReturn("generic-vnf")
        when(mockExecution.getVariable("mso.workflow.DoCreateVfModule.aai.generic-vnf.uri")).thenReturn("/aai/v9/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        try{
            GenericGetVnf obj = new GenericGetVnf()
            obj.getVnfByName(mockExecution)
        }catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        Mockito.verify(mockExecution, times(3)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(9999, workflowException.getErrorCode())
        Assert.assertEquals("org.apache.http.client.ClientProtocolException", workflowException.getErrorMessage())
    }

    @Test
    public void testGetVnfById() {

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable(prefix+"vnfId")).thenReturn("genricVnf_test")
        when(mockExecution.getVariable(prefix+"type")).thenReturn("generic-vnf")
        when(mockExecution.getVariable("mso.workflow.DoCreateVfModule.aai.generic-vnf.uri")).thenReturn("/aai/v9/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        GenericGetVnf obj = new GenericGetVnf()
        obj.getVnfById(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix","GENGV_")
        Mockito.verify(mockExecution).setVariable(prefix+"getVnfPath","http://localhost:28090/aai/v9/network/generic-vnfs/generic-vnf/genricVnf_test?depth=1")
    }

    @Test
    public void testGetVnfByIdEndpointNull() {

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable(prefix+"vnfId")).thenReturn("genricVnf_test")
        when(mockExecution.getVariable(prefix+"type")).thenReturn("generic-vnf")
        when(mockExecution.getVariable("mso.workflow.DoCreateVfModule.aai.generic-vnf.uri")).thenReturn("/aai/v9/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        try{
            GenericGetVnf obj = new GenericGetVnf()
            obj.getVnfById(mockExecution)
        }catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        Mockito.verify(mockExecution, times(3)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(9999, workflowException.getErrorCode())
        Assert.assertEquals("org.apache.http.client.ClientProtocolException", workflowException.getErrorMessage())
    }

    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoCreateVfModule")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoCreateVfModule")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DoCreateVfModule")
        when(mockExecution.getProcessInstanceId()).thenReturn("DoCreateVfModule")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }

    private static void mockData() {
        stubFor(get(urlMatching(".*/aai/v[0-9]+/network/generic-vnfs/generic-vnf.*"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("GenericFlows/getGenericVnfResponse.xml")))
    }
}
