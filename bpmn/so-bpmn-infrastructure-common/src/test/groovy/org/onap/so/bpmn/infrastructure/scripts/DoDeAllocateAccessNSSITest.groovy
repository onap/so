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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import static org.junit.Assert.assertEquals
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class DoDeAllocateAccessNSSITest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoDeAllocateAccessNSSI")
        when(mockExecution.getVariable("testProcessKey")).thenReturn("DoDeAllocateAccessNSSI")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest_validInputs() {
        String sliceParams = '{"snssaiList":["001-100001"],"sliceProfileId":"sp-123","nsiId":"nsi-456"}'
        when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("cust-1")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5G")
        when(mockExecution.getVariable("serviceInstanceID")).thenReturn("nssi-789")

        DoDeAllocateAccessNSSI instance = new DoDeAllocateAccessNSSI()
        instance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("anSliceProfileId", "sp-123")
        verify(mockExecution).setVariable("nsiId", "nsi-456")
        verify(mockExecution).setVariable("anNssiId", "nssi-789")
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequest_emptySnssaiList() {
        String sliceParams = '{"snssaiList":[],"sliceProfileId":"sp-123","nsiId":"nsi-456"}'
        when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("cust-1")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5G")
        when(mockExecution.getVariable("serviceInstanceID")).thenReturn("nssi-789")

        DoDeAllocateAccessNSSI instance = new DoDeAllocateAccessNSSI()
        instance.preProcessRequest(mockExecution)
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequest_blankSliceProfileId() {
        String sliceParams = '{"snssaiList":["001-100001"],"sliceProfileId":"","nsiId":"nsi-456"}'
        when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("cust-1")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5G")
        when(mockExecution.getVariable("serviceInstanceID")).thenReturn("nssi-789")

        DoDeAllocateAccessNSSI instance = new DoDeAllocateAccessNSSI()
        instance.preProcessRequest(mockExecution)
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequest_missingGlobalSubscriberId() {
        String sliceParams = '{"snssaiList":["001-100001"],"sliceProfileId":"sp-123","nsiId":"nsi-456"}'
        when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5G")
        when(mockExecution.getVariable("serviceInstanceID")).thenReturn("nssi-789")

        DoDeAllocateAccessNSSI instance = new DoDeAllocateAccessNSSI()
        instance.preProcessRequest(mockExecution)
    }
}
