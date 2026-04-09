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
class DoAllocateAccessNSSITest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoAllocateAccessNSSI")
        when(mockExecution.getVariable("testProcessKey")).thenReturn("DoAllocateAccessNSSI")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest_setsPrefix() {
        String sliceParams = '{"sliceProfile":{"sliceProfileId":"sp-123","snssaiList":["001-100001"],' +
                '"pLMNIdList":["310-410"],"coverageAreaTAList":[1,2,3]},"endPoint":{"ipAddress":"10.0.0.1","logicInterfaceId":"li-1","nextHopInfo":"nh-1"}}'
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("modelInvariantUuid")).thenReturn("inv-1")
        when(mockExecution.getVariable("modelUuid")).thenReturn("uuid-1")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("cust-1")
        when(mockExecution.getVariable("dummyServiceId")).thenReturn("dummy-1")
        when(mockExecution.getVariable("nsiId")).thenReturn("nsi-1")
        when(mockExecution.getVariable("networkType")).thenReturn("AN")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5G")
        when(mockExecution.getVariable("jobId")).thenReturn("job-1")
        when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)
        when(mockExecution.getVariable("servicename")).thenReturn("test-service")
        when(mockExecution.getVariable("sst")).thenReturn("embb")

        DoAllocateAccessNSSI instance = new DoAllocateAccessNSSI()
        instance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", "AASS_")
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequest_blankSliceProfile() {
        String sliceParams = '{"sliceProfile":""}'
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("modelInvariantUuid")).thenReturn("inv-1")
        when(mockExecution.getVariable("modelUuid")).thenReturn("uuid-1")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("cust-1")
        when(mockExecution.getVariable("dummyServiceId")).thenReturn("dummy-1")
        when(mockExecution.getVariable("nsiId")).thenReturn("nsi-1")
        when(mockExecution.getVariable("networkType")).thenReturn("AN")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5G")
        when(mockExecution.getVariable("jobId")).thenReturn("job-1")
        when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)
        when(mockExecution.getVariable("servicename")).thenReturn("test-service")
        when(mockExecution.getVariable("sst")).thenReturn("embb")

        DoAllocateAccessNSSI instance = new DoAllocateAccessNSSI()
        instance.preProcessRequest(mockExecution)
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequest_missingSliceProfileId() {
        String sliceParams = '{"sliceProfile":{"sliceProfileId":"","snssaiList":["001-100001"],' +
                '"pLMNIdList":["310-410"],"coverageAreaTAList":[1,2,3]}}'
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("modelInvariantUuid")).thenReturn("inv-1")
        when(mockExecution.getVariable("modelUuid")).thenReturn("uuid-1")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("cust-1")
        when(mockExecution.getVariable("dummyServiceId")).thenReturn("dummy-1")
        when(mockExecution.getVariable("nsiId")).thenReturn("nsi-1")
        when(mockExecution.getVariable("networkType")).thenReturn("AN")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5G")
        when(mockExecution.getVariable("jobId")).thenReturn("job-1")
        when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)
        when(mockExecution.getVariable("servicename")).thenReturn("test-service")
        when(mockExecution.getVariable("sst")).thenReturn("embb")

        DoAllocateAccessNSSI instance = new DoAllocateAccessNSSI()
        instance.preProcessRequest(mockExecution)
    }
}
