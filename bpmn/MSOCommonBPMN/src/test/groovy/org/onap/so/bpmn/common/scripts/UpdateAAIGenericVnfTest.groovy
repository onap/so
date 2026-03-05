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
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.refEq
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
@Ignore
class UpdateAAIGenericVnfTest {

    String getVfModuleResponse = FileUtil.readResourceFile("__files/VfModularity/GenericVnf.xml")

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    void init() throws IOException {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetGenericVnf() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("UAAIGenVnf_vnfId")).thenReturn("skask")

        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.UpdateAAIGenericVnf.aai.version")).thenReturn('8')

        StubResponseAAI.MockAAIVfModule()
        UpdateAAIGenericVnf obj = new UpdateAAIGenericVnf()
        obj.getGenericVnf(mockExecution)

        Mockito.verify(mockExecution).setVariable("UAAIGenVnf_getGenericVnfResponseCode", 200)
    }

    @Test
    void testGetGenericVnfEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("UAAIGenVnf_vnfId")).thenReturn("skask")

        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.UpdateAAIGenericVnf.aai.version")).thenReturn('8')

        StubResponseAAI.MockAAIVfModule()
        try {
            UpdateAAIGenericVnf obj = new UpdateAAIGenericVnf()
            obj.getGenericVnf(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        WorkflowException exception = new WorkflowException("UpdateAAIGenericVnf", 9999, "org.apache.http.client.ClientProtocolException")
        Mockito.verify(mockExecution).setVariable("WorkflowException", refEq(exception, any(WorkflowException.class)))
        Mockito.verify(mockExecution).setVariable("UAAIGenVnf_getGenericVnfResponseCode", 500)
        Mockito.verify(mockExecution).setVariable("UAAIGenVnf_getGenericVnfResponse", "AAI GET Failed:null")
    }

    @Test
    void testUpdateGenericVnf() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("UAAIGenVnf_vnfId")).thenReturn("skask")
        when(mockExecution.getVariable("UAAIGenVnf_getGenericVnfResponse")).thenReturn(getVfModuleResponse)

        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.UpdateAAIGenericVnf.aai.version")).thenReturn('8')

        stubFor(patch(urlMatching(".*/aai/v[0-9]+/network/generic-vnfs/generic-vnf/.*"))
                .willReturn(aResponse()
                .withStatus(200)))
        UpdateAAIGenericVnf obj = new UpdateAAIGenericVnf()
        obj.updateGenericVnf(mockExecution)

        Mockito.verify(mockExecution).setVariable("UAAIGenVnf_updateGenericVnfResponseCode", 200)
        Mockito.verify(mockExecution).setVariable("UAAIGenVnf_updateGenericVnfResponse", "")
    }

    @Test
    void testUpdateGenericVnfEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("UAAIGenVnf_vnfId")).thenReturn("skask")
        when(mockExecution.getVariable("UAAIGenVnf_getGenericVnfResponse")).thenReturn(getVfModuleResponse)

        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.UpdateAAIGenericVnf.aai.version")).thenReturn('8')

        stubFor(patch(urlMatching(".*/aai/v[0-9]+/network/generic-vnfs/generic-vnf/.*"))
                .willReturn(aResponse()
                .withStatus(200)))

        try {
            UpdateAAIGenericVnf obj = new UpdateAAIGenericVnf()
            obj.updateGenericVnf(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
        WorkflowException exception = new WorkflowException("UpdateAAIGenericVnf", 9999, "org.apache.http.client.ClientProtocolException")
        Mockito.verify(mockExecution).setVariable("WorkflowException", refEq(exception, any(WorkflowException.class)))
        Mockito.verify(mockExecution).setVariable("UAAIGenVnf_updateGenericVnfResponseCode", 500)
        Mockito.verify(mockExecution).setVariable("UAAIGenVnf_updateGenericVnfResponse", "AAI PATCH Failed:null")
    }

    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("UpdateAAIGenericVnf")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("UpdateAAIGenericVnf")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("UpdateAAIGenericVnf")
        when(mockExecution.getProcessInstanceId()).thenReturn("UpdateAAIGenericVnf")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }
}
