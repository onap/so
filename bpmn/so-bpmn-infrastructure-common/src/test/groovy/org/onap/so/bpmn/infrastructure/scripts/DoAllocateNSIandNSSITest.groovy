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

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class DoAllocateNSIandNSSITest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoAllocateNSIandNSSI")
        when(mockExecution.getVariable("testProcessKey")).thenReturn("DoAllocateNSIandNSSI")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest_nsstCount6_setsProcessFHandMHTrue() {
        when(mockExecution.getVariable("nsstCount")).thenReturn(6)

        DoAllocateNSIandNSSI instance = new DoAllocateNSIandNSSI()
        instance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("processFHandMH", true)
        verify(mockExecution).setVariable("isMoreNSSTtoProcess", true)
    }

    @Test
    void testPreProcessRequest_nsstCountNot6_setsProcessFHandMHFalse() {
        when(mockExecution.getVariable("nsstCount")).thenReturn(3)

        DoAllocateNSIandNSSI instance = new DoAllocateNSIandNSSI()
        instance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("processFHandMH", false)
        verify(mockExecution).setVariable("isMoreNSSTtoProcess", true)
    }

    @Test
    void testPreProcessRequest_setsNssiMapAndNsstSequence() {
        when(mockExecution.getVariable("nsstCount")).thenReturn(3)

        DoAllocateNSIandNSSI instance = new DoAllocateNSIandNSSI()
        instance.preProcessRequest(mockExecution)

        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class)
        verify(mockExecution).setVariable(eq("nssiMap"), mapCaptor.capture())
        assertTrue(mapCaptor.getValue().isEmpty())

        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class)
        verify(mockExecution).setVariable(eq("nsstSequence"), listCaptor.capture())
        assertEquals(["cn"], listCaptor.getValue())
    }
}
