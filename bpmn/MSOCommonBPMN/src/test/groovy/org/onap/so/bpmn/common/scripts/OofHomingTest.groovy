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

package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.core.domain.*

import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.Silent.class)
class OofHomingTest extends MsoGroovyTest {

    @Spy
    OofHoming oofHoming

    @Before
    void init() throws IOException {
        super.init("OofHoming")
        MockitoAnnotations.initMocks(this)
    }

    @Test(expected = BpmnError.class)
    void testCallOof_missingRequestId() {
        ExecutionEntity mockExecution = setupMock("OofHoming")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("si-123")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("testService")
        when(mockExecution.getVariable("serviceDecomposition")).thenReturn(new ServiceDecomposition())
        when(mockExecution.getVariable("subscriberInfo")).thenReturn("")
        when(mockExecution.getVariable("customerLocation")).thenReturn(["latitude": "32.89", "longitude": "-97.04"])
        when(mockExecution.getVariable("cloudOwner")).thenReturn("att-aic")
        when(mockExecution.getVariable("cloudRegionId")).thenReturn("dfwtx")

        oofHoming.callOof(mockExecution)
    }

    @Test(expected = BpmnError.class)
    void testCallOof_missingServiceInstanceId() {
        ExecutionEntity mockExecution = setupMock("OofHoming")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("testService")
        when(mockExecution.getVariable("serviceDecomposition")).thenReturn(new ServiceDecomposition())
        when(mockExecution.getVariable("subscriberInfo")).thenReturn("")
        when(mockExecution.getVariable("customerLocation")).thenReturn(["latitude": "32.89", "longitude": "-97.04"])
        when(mockExecution.getVariable("cloudOwner")).thenReturn("att-aic")
        when(mockExecution.getVariable("cloudRegionId")).thenReturn("dfwtx")

        oofHoming.callOof(mockExecution)
    }

    @Test(expected = BpmnError.class)
    void testCallOof_missingServiceInstanceName() {
        ExecutionEntity mockExecution = setupMock("OofHoming")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("si-123")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("")
        when(mockExecution.getVariable("serviceDecomposition")).thenReturn(new ServiceDecomposition())
        when(mockExecution.getVariable("subscriberInfo")).thenReturn("")
        when(mockExecution.getVariable("customerLocation")).thenReturn(["latitude": "32.89", "longitude": "-97.04"])
        when(mockExecution.getVariable("cloudOwner")).thenReturn("att-aic")
        when(mockExecution.getVariable("cloudRegionId")).thenReturn("dfwtx")

        oofHoming.callOof(mockExecution)
    }

    @Test(expected = BpmnError.class)
    void testCallOof_missingCustomerLocation() {
        ExecutionEntity mockExecution = setupMock("OofHoming")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("si-123")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("testService")
        when(mockExecution.getVariable("serviceDecomposition")).thenReturn(new ServiceDecomposition())
        when(mockExecution.getVariable("subscriberInfo")).thenReturn("")
        when(mockExecution.getVariable("customerLocation")).thenReturn([:])
        when(mockExecution.getVariable("cloudOwner")).thenReturn("att-aic")
        when(mockExecution.getVariable("cloudRegionId")).thenReturn("dfwtx")

        oofHoming.callOof(mockExecution)
    }
}
