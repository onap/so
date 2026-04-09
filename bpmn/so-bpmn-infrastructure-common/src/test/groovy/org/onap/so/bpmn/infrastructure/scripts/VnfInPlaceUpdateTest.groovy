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
class VnfInPlaceUpdateTest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("VnfInPlaceUpdate")
        when(mockExecution.getVariable("testProcessKey")).thenReturn("VnfInPlaceUpdate")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testInitProcessVariables() {
        VnfInPlaceUpdate vnfInPlaceUpdate = new VnfInPlaceUpdate()
        vnfInPlaceUpdate.initProcessVariables(mockExecution)

        verify(mockExecution).setVariable('prefix', 'VnfIPU_')
        verify(mockExecution).setVariable('Request', null)
        verify(mockExecution).setVariable('requestInfo', null)
        verify(mockExecution).setVariable('source', null)
        verify(mockExecution).setVariable('vnfInputs', null)
        verify(mockExecution).setVariable('tenantId', null)
        verify(mockExecution).setVariable('controllerType', null)
        verify(mockExecution).setVariable('UpdateVnfSuccessIndicator', false)
        verify(mockExecution).setVariable('currentActivity', 'VnfIPU')
        verify(mockExecution).setVariable('errorCode', "0")
        verify(mockExecution).setVariable('maxRetryCount', 3)
        verify(mockExecution).setVariable("rollbackSetClosedLoopDisabledFlag", false)
        verify(mockExecution).setVariable("rollbackVnfStop", false)
        verify(mockExecution).setVariable("rollbackVnfLock", false)
        verify(mockExecution).setVariable("rollbackQuiesceTraffic", false)
        verify(mockExecution).setVariable("rollbackSetVnfInMaintenanceFlag", false)
    }

    @Test
    void testPreProcessRequest_success() {
        when(mockExecution.getId()).thenReturn("test-exec-id")
        when(mockExecution.getVariable('serviceInstanceId')).thenReturn("si-123")
        when(mockExecution.getVariable('vnfId')).thenReturn("vnf-456")
        when(mockExecution.getVariable('mso-request-id')).thenReturn("req-789")
        when(mockExecution.getVariable('bpmnRequest')).thenReturn("""{
            "requestDetails": {
                "requestInfo": {
                    "source": "VID",
                    "requestorId": "testUser"
                },
                "cloudConfiguration": {
                    "lcpCloudRegionId": "dfwtx",
                    "cloudOwner": "att-aic",
                    "tenantId": "tenant-123"
                },
                "requestParameters": {
                    "controllerType": "APPC",
                    "payload": "{\\"config-params\\":{\\"key1\\":\\"val1\\"}}"
                }
            }
        }""")

        VnfInPlaceUpdate vnfInPlaceUpdate = new VnfInPlaceUpdate()
        vnfInPlaceUpdate.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable('serviceInstanceId', "si-123")
        verify(mockExecution).setVariable('vnfId', "vnf-456")
        verify(mockExecution).setVariable('controllerType', "APPC")
        verify(mockExecution).setVariable('lcpCloudRegionId', "dfwtx")
        verify(mockExecution).setVariable('cloudOwner', "att-aic")
        verify(mockExecution).setVariable('tenantId', "tenant-123")
        verify(mockExecution).setVariable('requestorId', "testUser")
        verify(mockExecution).setVariable("source", "VID")
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequest_invalidJson() {
        when(mockExecution.getId()).thenReturn("test-exec-id")
        when(mockExecution.getVariable('bpmnRequest')).thenReturn("not-json")

        VnfInPlaceUpdate vnfInPlaceUpdate = new VnfInPlaceUpdate()
        vnfInPlaceUpdate.preProcessRequest(mockExecution)
    }
}
