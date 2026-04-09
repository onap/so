/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.beans.nsmf.oof.TemplateInfo
import org.onap.so.bpmn.core.domain.AllottedResource
import org.onap.so.bpmn.core.domain.ServiceDecomposition

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class DoCreateSliceServiceOptionTest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoCreateSliceServiceOption")
        when(mockExecution.getVariable("testProcessKey")).thenReturn("DoCreateSliceServiceOption")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest_noOp() {
        DoCreateSliceServiceOption instance = new DoCreateSliceServiceOption()
        // preProcessRequest is a no-op, should not throw
        instance.preProcessRequest(mockExecution)
    }

    @Test
    void testProcessDecompositionNST_setsNsstInfos() {
        ServiceDecomposition nstDecomp = mock(ServiceDecomposition.class)
        AllottedResource ar1 = mock(AllottedResource.class)
        when(ar1.getProvidingServiceModelUuid()).thenReturn("uuid-1")
        when(ar1.getProvidingServiceModelInvariantUuid()).thenReturn("inv-1")
        when(ar1.getProvidingServiceModelName()).thenReturn("name-1")

        AllottedResource ar2 = mock(AllottedResource.class)
        when(ar2.getProvidingServiceModelUuid()).thenReturn("uuid-2")
        when(ar2.getProvidingServiceModelInvariantUuid()).thenReturn("inv-2")
        when(ar2.getProvidingServiceModelName()).thenReturn("name-2")

        when(nstDecomp.getAllottedResources()).thenReturn([ar1, ar2])
        when(mockExecution.getVariable("nstServiceDecomposition")).thenReturn(nstDecomp)

        DoCreateSliceServiceOption instance = new DoCreateSliceServiceOption()
        instance.processDecompositionNST(mockExecution)

        ArgumentCaptor<List> nsstCaptor = ArgumentCaptor.forClass(List.class)
        verify(mockExecution).setVariable(eq("nsstInfos"), nsstCaptor.capture())
        List<TemplateInfo> nsstInfos = nsstCaptor.getValue()
        assertEquals(2, nsstInfos.size())
        assertEquals("uuid-1", nsstInfos[0].getUUID())
        assertEquals("name-2", nsstInfos[1].getName())

        verify(mockExecution).setVariable("maxNsstIndex", 1)
        verify(mockExecution).setVariable("currentNsstIndex", 0)
    }

    @Test
    void testProcessDecompositionNST_emptyAllottedResources() {
        ServiceDecomposition nstDecomp = mock(ServiceDecomposition.class)
        when(nstDecomp.getAllottedResources()).thenReturn([])
        when(mockExecution.getVariable("nstServiceDecomposition")).thenReturn(nstDecomp)

        DoCreateSliceServiceOption instance = new DoCreateSliceServiceOption()
        instance.processDecompositionNST(mockExecution)

        ArgumentCaptor<List> nsstCaptor = ArgumentCaptor.forClass(List.class)
        verify(mockExecution).setVariable(eq("nsstInfos"), nsstCaptor.capture())
        assertEquals(0, nsstCaptor.getValue().size())
        verify(mockExecution).setVariable("maxNsstIndex", -1)
    }
}
