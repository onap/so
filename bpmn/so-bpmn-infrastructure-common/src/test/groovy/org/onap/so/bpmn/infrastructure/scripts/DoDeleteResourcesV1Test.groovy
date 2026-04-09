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
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class DoDeleteResourcesV1Test extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoDeleteResourcesV1")
        when(mockExecution.getVariable("testProcessKey")).thenReturn("DoDeleteResourcesV1")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest_setsPrefix() {
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("customer-1")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("si-123")
        when(mockExecution.getVariable("serviceInputParams")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://callback")

        DoDeleteResourcesV1 instance = new DoDeleteResourcesV1()
        instance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", "DDR_")
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequest_blankServiceInstanceId() {
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("customer-1")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("")

        DoDeleteResourcesV1 instance = new DoDeleteResourcesV1()
        instance.preProcessRequest(mockExecution)
    }

    @Test
    void testPreProcessRequest_nullGlobalSubscriberIdSetsEmpty() {
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn(null)
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("si-123")
        when(mockExecution.getVariable("serviceInputParams")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://callback")

        DoDeleteResourcesV1 instance = new DoDeleteResourcesV1()
        instance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("globalSubscriberId", "")
    }

    @Test
    void testPreProcessRequest_withServiceInputParams() {
        when(mockExecution.getVariable("msoRequestId")).thenReturn("req-123")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("customer-1")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("si-123")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://callback")

        Map<String, String> params = new HashMap<>()
        params.put("param1", "value1")
        when(mockExecution.getVariable("serviceInputParams")).thenReturn(params)

        DoDeleteResourcesV1 instance = new DoDeleteResourcesV1()
        instance.preProcessRequest(mockExecution)

        ArgumentCaptor<String> paramsCaptor = ArgumentCaptor.forClass(String.class)
        verify(mockExecution).setVariable(eq("siParamsXml"), paramsCaptor.capture())
        String siParams = paramsCaptor.getValue()
        assertNotNull(siParams)
        assertTrue(siParams.contains("<service-input-parameters>"))
        assertTrue(siParams.contains("param1"))
    }
}
