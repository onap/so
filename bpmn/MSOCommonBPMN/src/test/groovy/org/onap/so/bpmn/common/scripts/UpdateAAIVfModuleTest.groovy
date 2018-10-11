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
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.bpmn.mock.StubResponseAAI

import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
@Ignore
class UpdateAAIVfModuleTest {
    def prefix = "UAAIVfMod_"

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090)

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetVfModule() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")

        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.UpdateAAIVfModule.aai.version")).thenReturn('8')

        StubResponseAAI.MockAAIVfModule()
        UpdateAAIVfModule obj = new UpdateAAIVfModule()
        obj.getVfModule(mockExecution)

        verify(mockExecution).setVariable(prefix + "getVfModuleResponseCode", 200)
    }

    @Test
    void testGetVfModuleEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")

        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.UpdateAAIVfModule.aai.version")).thenReturn('8')

        StubResponseAAI.MockAAIVfModule()
        try {
            UpdateAAIVfModule obj = new UpdateAAIVfModule()
            obj.getVfModule(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        Mockito.verify(mockExecution, times(3)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getAllValues().get(1)
        Assert.assertEquals(9999, workflowException.getErrorCode())
        Assert.assertEquals("org.apache.http.client.ClientProtocolException", workflowException.getErrorMessage())
    }

    @Test
    void testUpdateVfModule() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")

        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.UpdateAAIVfModule.aai.version")).thenReturn('8')

        String getVfModuleResponse = FileUtil.readResourceFile("__files/VfModularity/GenericVnf.xml")
        when(mockExecution.getVariable(prefix + "getVfModuleResponse")).thenReturn(getVfModuleResponse)
        StubResponseAAI.MockAAIVfModule()
        UpdateAAIVfModule obj = new UpdateAAIVfModule()
        obj.updateVfModule(mockExecution)

        verify(mockExecution).setVariable(prefix + "updateVfModuleResponseCode", 200)
    }

    @Test
    void testUpdateVfModuleEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")

        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.UpdateAAIVfModule.aai.version")).thenReturn('8')

        String getVfModuleResponse = FileUtil.readResourceFile("__files/VfModularity/GenericVnf.xml")
        when(mockExecution.getVariable(prefix + "getVfModuleResponse")).thenReturn(getVfModuleResponse)
        StubResponseAAI.MockAAIVfModule()
        try {
            UpdateAAIVfModule obj = new UpdateAAIVfModule()
            obj.updateVfModule(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        Mockito.verify(mockExecution, times(3)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getAllValues().get(1)
        Assert.assertEquals(9999, workflowException.getErrorCode())
        Assert.assertEquals("org.apache.http.client.ClientProtocolException", workflowException.getErrorMessage())
    }

    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("UpdateAAIVfModule")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("UpdateAAIVfModule")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("UpdateAAIVfModule")
        when(mockExecution.getProcessInstanceId()).thenReturn("UpdateAAIVfModule")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }
}
