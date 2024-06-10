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

import org.junit.rules.ExpectedException

import static org.mockito.Mockito.*

import jakarta.ws.rs.NotFoundException
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aai.domain.yang.VfModule


class UpdateAAIVfModuleTest  extends MsoGroovyTest {
    def prefix = "UAAIVfMod_"

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Spy
    UpdateAAIVfModule updateAAIVfModule;

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    void init() throws IOException {
        super.init("UpdateAAIVfModule")
        when(updateAAIVfModule.getAAIClient()).thenReturn(client)
        mockExecution = setupMock("UpdateAAIVfModule")
    }

    @Test
    void testGetVfModule() {
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")
        VfModule vfModule = new VfModule()
        vfModule.setVfModuleId("supercool")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("skask").vfModule("supercool"));
        when(client.get(VfModule.class,resourceUri)).thenReturn(Optional.of(vfModule))
        updateAAIVfModule.getVfModule(mockExecution)
        verify(mockExecution).setVariable(prefix + "getVfModuleResponseCode", 200)
        verify(mockExecution).setVariable(prefix + "getVfModuleResponse", vfModule)
    }

    @Test
    void testGetVfModuleNotFound() {
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("skask").vfModule("supercool"));
        when(client.get(VfModule.class,resourceUri)).thenReturn(Optional.empty())
        updateAAIVfModule.getVfModule(mockExecution)
        verify(mockExecution).setVariable(prefix + "getVfModuleResponseCode", 404)
        verify(mockExecution).setVariable(prefix + "getVfModuleResponse", "VF Module not found in AAI")
    }

    @Test
    void testGetVfModuleException() {
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("skask").vfModule("supercool"));
        when(client.get(VfModule.class,resourceUri)).thenThrow(new NullPointerException("Error from AAI client"))
        updateAAIVfModule.getVfModule(mockExecution)
        verify(mockExecution).setVariable(prefix + "getVfModuleResponseCode", 500)
        verify(mockExecution).setVariable(prefix + "getVfModuleResponse", "AAI GET Failed:"+"Error from AAI client")
    }


    @Test
    void testUpdateVfModule() {
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")
        VfModule vfModule = new VfModule()
        vfModule.setVfModuleId("supercool")
        vfModule.setResourceVersion("12345")
        when(mockExecution.getVariable(prefix + "getVfModuleResponse")).thenReturn(vfModule)
        doNothing().when(client).update(isA(AAIResourceUri.class) as AAIResourceUri, anyObject())
        updateAAIVfModule.updateVfModule(mockExecution)
        verify(mockExecution).setVariable("UAAIVfMod_updateVfModuleResponseCode", 200)
    }

    @Test
    void testUpdateVfModuleNotFound() throws BpmnError {
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")
        VfModule vfModule = new VfModule()
        vfModule.setVfModuleId("supercool")
        vfModule.setResourceVersion("12345")
        when(mockExecution.getVariable(prefix + "getVfModuleResponse")).thenReturn(vfModule)
        doThrow(new NotFoundException("Vf Module not found")).when(client).update(isA(AAIResourceUri.class) as AAIResourceUri, anyObject())
        thrown.expect(BpmnError.class)
        updateAAIVfModule.updateVfModule(mockExecution)
        verify(mockExecution).setVariable("UAAIVfMod_updateVfModuleResponseCode", 404)
    }


    @Test
    void testUpdateVfModuleException() {
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("supercool")
        VfModule vfModule = new VfModule()
        vfModule.setVfModuleId("supercool")
        vfModule.setResourceVersion("12345")
        when(mockExecution.getVariable(prefix + "getVfModuleResponse")).thenReturn(vfModule)
        doThrow(new IllegalStateException("Error in AAI client")).when(client).update(isA(AAIResourceUri.class) as AAIResourceUri, anyObject())
        thrown.expect(BpmnError.class)
        updateAAIVfModule.updateVfModule(mockExecution)
        verify(mockExecution).setVariable("UAAIVfMod_updateVfModuleResponseCode", 500)
    }
}
