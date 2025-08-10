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
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner

import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
@Ignore
class PrepareUpdateAAIVfModuleTest {

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
        when(mockExecution.getVariable("PUAAIVfMod_vnfId")).thenReturn("skask")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.PrepareUpdateAAIVfModule.aai.version")).thenReturn('8')

        StubResponseAAI.MockAAIVfModule()

        PrepareUpdateAAIVfModule obj = new PrepareUpdateAAIVfModule()
        obj.getGenericVnf(mockExecution)

        verify(mockExecution).setVariable("PUAAIVfMod_getVnfResponseCode", 200)
    }

    @Test
    void testGetGenericVnfEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("PUAAIVfMod_vnfId")).thenReturn("skask")

        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.PrepareUpdateAAIVfModule.aai.version")).thenReturn('8')

        StubResponseAAI.MockAAIVfModule()
        try {
            PrepareUpdateAAIVfModule obj = new PrepareUpdateAAIVfModule()
            obj.getGenericVnf(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        verify(mockExecution).setVariable("PUAAIVfMod_getVnfResponseCode", 500)
        verify(mockExecution).setVariable("PUAAIVfMod_getVnfResponse", "AAI GET Failed:org.apache.http.client.ClientProtocolException")
    }

    @Test
    void testUpdateVfModule() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("PUAAIVfMod_vnfId")).thenReturn("skask")
        when(mockExecution.getVariable("PUAAIVfMod_vfModuleId")).thenReturn("supercool")

        def node = new Node(null, 'vfModule')
        new Node(node, 'vf-module-name', "abc")
        VfModule vfModule = new VfModule(node, true)

        when(mockExecution.getVariable("PUAAIVfMod_vfModule")).thenReturn(vfModule)
        when(mockExecution.getVariable("PUAAIVfMod_orchestrationStatus")).thenReturn("created")

        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.PrepareUpdateAAIVfModule.aai.version")).thenReturn('8')

        StubResponseAAI.MockAAIVfModule()
        PrepareUpdateAAIVfModule obj = new PrepareUpdateAAIVfModule()
        obj.updateVfModule(mockExecution)

        verify(mockExecution).setVariable("PUAAIVfMod_updateVfModuleResponseCode", 200)
        verify(mockExecution).setVariable("PUAAIVfMod_updateVfModuleResponse", "")
    }

    @Test
    void testUpdateVfModuleEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("PUAAIVfMod_vnfId")).thenReturn("skask")
        when(mockExecution.getVariable("PUAAIVfMod_vfModuleId")).thenReturn("supercool")

        def node = new Node(null, 'vfModule')
        new Node(node, 'vf-module-name', "abc")
        VfModule vfModule = new VfModule(node, true)

        when(mockExecution.getVariable("PUAAIVfMod_vfModule")).thenReturn(vfModule)
        when(mockExecution.getVariable("PUAAIVfMod_orchestrationStatus")).thenReturn("created")

        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.PrepareUpdateAAIVfModule.aai.version")).thenReturn('8')

        StubResponseAAI.MockAAIVfModule()
        try {
            PrepareUpdateAAIVfModule obj = new PrepareUpdateAAIVfModule()
            obj.updateVfModule(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        verify(mockExecution).setVariable("PUAAIVfMod_updateVfModuleResponseCode", 500)
        verify(mockExecution).setVariable("PUAAIVfMod_updateVfModuleResponse", "AAI PATCH Failed:org.apache.http.client.ClientProtocolException")
    }

    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("PrepareUpdateAAIVfModule")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("PrepareUpdateAAIVfModule")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("PrepareUpdateAAIVfModule")
        when(mockExecution.getProcessInstanceId()).thenReturn("PrepareUpdateAAIVfModule")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }
}
