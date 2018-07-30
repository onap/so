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
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.StubResponseAAI

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
@Ignore
class DeleteAAIVfModuleTest {

    def prefix = "DAAIVfMod_"

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090)

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testQueryAAIForGenericVnf() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "genericVnfEndpoint")).thenReturn("/aai/v8/network/generic-vnfs/generic-vnf/skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("aai.auth")).thenReturn("9B2278E8B8E95F256A560719055F4DF3")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("aa3871669d893c7fb8abbcda31b88b4f")

        StubResponseAAI.MockAAIVfModule()
        DeleteAAIVfModule obj = new DeleteAAIVfModule()
        obj.queryAAIForGenericVnf(mockExecution)

        Mockito.verify(mockExecution).setVariable(prefix + "queryGenericVnfResponseCode", 200)
    }

    @Test
    void testQueryAAIForGenericVnfEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "genericVnfEndpoint")).thenReturn("/aai/v8/network/generic-vnfs/generic-vnf/skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("aai.auth")).thenReturn("9B2278E8B8E95F256A560719055F4DF3")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("aa3871669d893c7fb8abbcda31b88b4f")

        StubResponseAAI.MockAAIVfModule()
        try {
            DeleteAAIVfModule obj = new DeleteAAIVfModule()
            obj.queryAAIForGenericVnf(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        Mockito.verify(mockExecution, times(2)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(5000, workflowException.getErrorCode())
        Assert.assertEquals("Internal Error - Occured during queryAAIForGenericVnf", workflowException.getErrorMessage())
    }

    @Test
    void testDeleteGenericVnf() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "genericVnfEndpoint")).thenReturn("/aai/v9/cloud-infrastructure/volume-groups/volume-group/78987")
        when(mockExecution.getVariable(prefix + "genVnfRsrcVer")).thenReturn("0000020")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("aai.auth")).thenReturn("9B2278E8B8E95F256A560719055F4DF3")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("aa3871669d893c7fb8abbcda31b88b4f")

        stubFor(delete(urlMatching("/aai/v[0-9]+/cloud-infrastructure/volume-groups/volume-group/78987/[?]resource-version=0000020"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBodyFile("")))

        StubResponseAAI.MockAAIVfModule()
        DeleteAAIVfModule obj = new DeleteAAIVfModule()
        obj.deleteGenericVnf(mockExecution)

        Mockito.verify(mockExecution).setVariable(prefix + "deleteGenericVnfResponseCode", 200)
    }

    @Test
    void testDeleteGenericVnfEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "genericVnfEndpoint")).thenReturn("/aai/v9/cloud-infrastructure/volume-groups/volume-group/78987")
        when(mockExecution.getVariable(prefix + "genVnfRsrcVer")).thenReturn("0000020")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("aai.auth")).thenReturn("9B2278E8B8E95F256A560719055F4DF3")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("aa3871669d893c7fb8abbcda31b88b4f")

        StubResponseAAI.MockAAIVfModule()
        try {
            DeleteAAIVfModule obj = new DeleteAAIVfModule()
            obj.deleteGenericVnf(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(5000, workflowException.getErrorCode())
        Assert.assertEquals("Internal Error - Occured during deleteGenericVnf", workflowException.getErrorMessage())
    }

    @Test
    void testDeleteVfModule() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "vfModuleEndpoint")).thenReturn("/aai/v9/cloud-infrastructure/volume-groups/volume-group/78987")
        when(mockExecution.getVariable(prefix + "vfModRsrcVer")).thenReturn("0000020")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("aai.auth")).thenReturn("9B2278E8B8E95F256A560719055F4DF3")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("aa3871669d893c7fb8abbcda31b88b4f")

        stubFor(delete(urlMatching("/aai/v[0-9]+/cloud-infrastructure/volume-groups/volume-group/78987/[?]resource-version=0000020"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBodyFile("")))

        DeleteAAIVfModule obj = new DeleteAAIVfModule()
        obj.deleteVfModule(mockExecution)

        Mockito.verify(mockExecution).setVariable(prefix + "deleteVfModuleResponseCode", 200)
    }

    @Test
    void testDeleteVfModuleEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "vfModuleEndpoint")).thenReturn("/aai/v9/cloud-infrastructure/volume-groups/volume-group/78987")
        when(mockExecution.getVariable(prefix + "vfModRsrcVer")).thenReturn("0000020")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("aai.auth")).thenReturn("9B2278E8B8E95F256A560719055F4DF3")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("aa3871669d893c7fb8abbcda31b88b4f")

        StubResponseAAI.MockAAIVfModule()
        try {
            DeleteAAIVfModule obj = new DeleteAAIVfModule()
            obj.deleteVfModule(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(5000, workflowException.getErrorCode())
        Assert.assertEquals("Internal Error - Occured during deleteVfModule", workflowException.getErrorMessage())
    }

    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DeleteAAIVfModule")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DeleteAAIVfModule")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DeleteAAIVfModule")
        when(mockExecution.getProcessInstanceId()).thenReturn("DeleteAAIVfModule")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }
}
