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
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class ServiceIntentUtilsTest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("ServiceIntentUtils")
        when(mockExecution.getVariable("testProcessKey")).thenReturn("ServiceIntentUtils")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testSetCommonExecutionVars_validInput() {
        String bpmnRequest = '{"globalSubscriberId":"cust-1","serviceType":"5G","name":"myservice",' +
                '"subscriptionServiceType":"5G","additionalProperties":{"key":"val"}}'
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(bpmnRequest)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("req-123")

        ServiceIntentUtils instance = new ServiceIntentUtils()
        instance.setCommonExecutionVars(mockExecution)

        verify(mockExecution).setVariable("msoRequestId", "req-123")
        verify(mockExecution).setVariable("globalSubscriberId", "cust-1")
        verify(mockExecution).setVariable("serviceType", "5G")
        verify(mockExecution).setVariable("servicename", "myservice")
        verify(mockExecution).setVariable("subscriptionServiceType", "5G")
    }

    @Test(expected = BpmnError.class)
    void testSetCommonExecutionVars_blankGlobalSubscriberIdThrows() {
        String bpmnRequest = '{"globalSubscriberId":"","serviceType":"5G",' +
                '"subscriptionServiceType":"5G"}'
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(bpmnRequest)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("req-123")

        ServiceIntentUtils instance = new ServiceIntentUtils()
        instance.setCommonExecutionVars(mockExecution)
    }

    @Test(expected = BpmnError.class)
    void testSetCommonExecutionVars_blankServiceTypeThrows() {
        String bpmnRequest = '{"globalSubscriberId":"cust-1","serviceType":"",' +
                '"subscriptionServiceType":"5G"}'
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(bpmnRequest)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("req-123")

        ServiceIntentUtils instance = new ServiceIntentUtils()
        instance.setCommonExecutionVars(mockExecution)
    }

    @Test
    void testSetCommonExecutionVars_noExceptionOnErr() {
        String bpmnRequest = '{"globalSubscriberId":"","serviceType":"","subscriptionServiceType":"5G"}'
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(bpmnRequest)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("req-123")

        ServiceIntentUtils instance = new ServiceIntentUtils()
        // With exceptionOnErr=false, blank globalSubscriberId and serviceType should not throw
        instance.setCommonExecutionVars(mockExecution, false)

        verify(mockExecution).setVariable("msoRequestId", "req-123")
        verify(mockExecution).setVariable("subscriptionServiceType", "5G")
    }

    @Test
    void testSetCommonExecutionVars_setsJobId() {
        String bpmnRequest = '{"globalSubscriberId":"cust-1","serviceType":"5G",' +
                '"subscriptionServiceType":"5G"}'
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(bpmnRequest)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("req-123")

        ServiceIntentUtils instance = new ServiceIntentUtils()
        instance.setCommonExecutionVars(mockExecution)

        ArgumentCaptor<String> jobIdCaptor = ArgumentCaptor.forClass(String.class)
        verify(mockExecution).setVariable(eq("jobId"), jobIdCaptor.capture())
        assertNotNull(jobIdCaptor.getValue())
    }
}
