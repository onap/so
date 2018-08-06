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

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Test
import org.onap.appc.client.lcm.model.Action

import static org.mockito.Mockito.*

class AppCClientTest extends MsoGroovyTest {

    @Test
    void runAppcCommand_healtCheck_indexIncremented() {
        DelegateExecution delegateExecutionMock = mock(DelegateExecution.class)
        mockGettingExecutionVariables(delegateExecutionMock)

        new AppCClient().runAppcCommand(delegateExecutionMock)

        verifySettingExecutionVariables(delegateExecutionMock)
    }

    private mockGettingExecutionVariables(DelegateExecution delegateExecutionMock) {
        when(delegateExecutionMock.getVariable("isDebugLogEnabled")).thenReturn('true')
        when(delegateExecutionMock.getVariable("action")).thenReturn(Action.HealthCheck)
        when(delegateExecutionMock.getVariable("vnfId")).thenReturn("vnfIdTest")
        when(delegateExecutionMock.getVariable("msoRequestId")).thenReturn("msoRequestIdTest")
        when(delegateExecutionMock.getVariable("vnfName")).thenReturn("vnfNameTest")
        when(delegateExecutionMock.getVariable("aicIdentity")).thenReturn("aicIdentityTest")
        when(delegateExecutionMock.getVariable("vnfHostIpAddress")).thenReturn("vnfHostIpAddressTest")
        when(delegateExecutionMock.getVariable("vmIdList")).thenReturn("vmIdListTest")
        when(delegateExecutionMock.getVariable("identityUrl")).thenReturn("identityUrlTest")
        when(delegateExecutionMock.getVariable("vfModuleId")).thenReturn("vfModuleIdTest")
        when(delegateExecutionMock.getVariable("healthCheckIndex")).thenReturn(1)
    }

    private verifySettingExecutionVariables(DelegateExecution delegateExecutionMock) {
        verify(delegateExecutionMock).setVariable("rollbackVnfStop", false)
        verify(delegateExecutionMock).setVariable("rollbackVnfLock", false)
        verify(delegateExecutionMock).setVariable("rollbackQuiesceTraffic", false)
        verify(delegateExecutionMock).setVariable("workStep", Action.HealthCheck.toString())
        verify(delegateExecutionMock).setVariable("workStep", Action.HealthCheck.toString() + 1)
        verify(delegateExecutionMock).setVariable("healthCheckIndex", 2)
    }
}
