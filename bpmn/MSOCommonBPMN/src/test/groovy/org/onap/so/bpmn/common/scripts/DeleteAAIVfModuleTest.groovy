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

import static org.mockito.Mockito.*

import jakarta.ws.rs.NotFoundException

import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.Spy
import org.onap.aai.domain.yang.GenericVnf
import org.onap.so.bpmn.core.WorkflowException
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri

class DeleteAAIVfModuleTest extends  MsoGroovyTest{

    def prefix = "DAAIVfMod_"

    @Spy
    DeleteAAIVfModule deleteAAIVfModule ;

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    void init() throws IOException {
        super.init("DeleteAAIVfModule")
        when(deleteAAIVfModule.getAAIClient()).thenReturn(client)
    }

    @Test
    void testQueryAAIForGenericVnf() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("DAAIVfMod_vnfId")).thenReturn("vnfId1")
        mockAAIGenericVnf("vnfId1")
        deleteAAIVfModule.queryAAIForGenericVnf(mockExecution)
        Mockito.verify(mockExecution).setVariable(prefix + "queryGenericVnfResponseCode", 200)
    }

    @Test
    void testQueryAAIForGenericVnfNotFound() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("DAAIVfMod_vnfId")).thenReturn("vnfId1")
        mockAAIGenericVnfNotFound("vnfId1")
        deleteAAIVfModule.queryAAIForGenericVnf(mockExecution)
        Mockito.verify(mockExecution).setVariable(prefix + "queryGenericVnfResponseCode", 404)
    }
    @Test
    void testQueryAAIForGenericVnfEndpointNull() {
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable("DAAIVfMod_vnfId", "vnfId1")
        try {
            deleteAAIVfModule.queryAAIForGenericVnf(execution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        Assert.assertEquals(404, execution.getVariable("DAAIVfMod_queryGenericVnfResponseCode"))
        Assert.assertEquals("Vnf Not Found!", execution.getVariable("DAAIVfMod_queryGenericVnfResponse"))
    }

    @Test
    void testDeleteGenericVnf() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("DAAIVfMod_vnfId")).thenReturn("vnfId1")
        doNothing().when(client).delete(isA(AAIResourceUri.class) as AAIResourceUri)
        deleteAAIVfModule.deleteGenericVnf(mockExecution)
        Mockito.verify(mockExecution).setVariable(prefix + "deleteGenericVnfResponseCode", 200)
    }

    @Test
    void testParseForVfModule() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("DAAIVfMod_vfModuleId")).thenReturn("testVfModuleIdGWSec")
        Optional<GenericVnf> genericVnf = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json");
        when(mockExecution.getVariable("DAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf.get())
        deleteAAIVfModule.parseForVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_moduleExists", true)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_isBaseModule", false)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_isLastModule", false)
    }

    @Test
    void testParseForVfModuleNotFound() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("DAAIVfMod_vfModuleId")).thenReturn("notFound")
        when(mockExecution.getVariable("DAAIVfMod_moduleExists")).thenReturn(false)
        Optional<GenericVnf> genericVnf = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json");
        when(mockExecution.getVariable("DAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf.get())
        deleteAAIVfModule.parseForVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_moduleExists", false)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_isBaseModule", false)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_isLastModule", false)
    }

    @Test
    void testParseForVfModuleBase() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("DAAIVfMod_vfModuleId")).thenReturn("lukewarm")
        Optional<GenericVnf> genericVnf = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json");
        when(mockExecution.getVariable("DAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf.get())
        deleteAAIVfModule.parseForVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_moduleExists", true)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_isBaseModule", true)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_isLastModule", false)
    }

    @Test
    void testParseForVfModuleLast() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("DAAIVfMod_vfModuleId")).thenReturn("testVfModuleIdGWSec")
        Optional<GenericVnf> genericVnfOps = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json");
        GenericVnf genericVnf =  genericVnfOps.get();
        genericVnf.getVfModules().getVfModule().remove(0)
        when(mockExecution.getVariable("DAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf)
        deleteAAIVfModule.parseForVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_moduleExists", true)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_isBaseModule", false)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_isLastModule", true)
    }

    @Test
    void testParseForVfModuleBaseLast() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("DAAIVfMod_vfModuleId")).thenReturn("lukewarm")
        Optional<GenericVnf> genericVnfOps = getAAIObjectFromJson(GenericVnf.class,"__files/aai/GenericVnfVfModule.json");
        GenericVnf genericVnf =  genericVnfOps.get();
        genericVnf.getVfModules().getVfModule().remove(1)
        when(mockExecution.getVariable("DAAIVfMod_queryGenericVnfResponse")).thenReturn(genericVnf)
        deleteAAIVfModule.parseForVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_moduleExists", true)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_isBaseModule", true)
        Mockito.verify(mockExecution).setVariable("DAAIVfMod_isLastModule", true)
    }



    @Test
    void testDeleteGenericVnfEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("DAAIVfMod_vnfId")).thenReturn("vnfId1")
        try {
            doThrow(new NotFoundException("Vnf Not Found")).when(client).delete(isA(AAIResourceUri.class) as AAIResourceUri)
            deleteAAIVfModule.deleteGenericVnf(mockExecution)
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
        when(mockExecution.getVariable("DAAIVfMod_vnfId")).thenReturn("vnfId1")
        when(mockExecution.getVariable("DAAIVfMod_vfModuleId")).thenReturn("vfModuleId1")
        doNothing().when(client).delete(isA(AAIResourceUri.class) as AAIResourceUri)
        deleteAAIVfModule.deleteVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable(prefix + "deleteVfModuleResponseCode", 200)
    }

    @Test
    void testDeleteVfModuleEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("DAAIVfMod_vnfId")).thenReturn("vnfId1")
        when(mockExecution.getVariable("DAAIVfMod_vfModuleId")).thenReturn("vfModuleId1")
        try {
            doThrow(new NotFoundException("Vnf Not Found")).when(client).delete(isA(AAIResourceUri.class) as AAIResourceUri)
            deleteAAIVfModule.deleteVfModule(mockExecution)
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
