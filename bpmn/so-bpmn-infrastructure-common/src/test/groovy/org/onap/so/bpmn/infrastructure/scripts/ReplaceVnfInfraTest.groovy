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
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class ReplaceVnfInfraTest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("ReplaceVnfInfra")
        when(mockExecution.getVariable("testProcessKey")).thenReturn("ReplaceVnfInfra")
    }

    @Test
    void testInitProcessVariables() {
        ReplaceVnfInfra replaceVnfInfra = new ReplaceVnfInfra()
        replaceVnfInfra.initProcessVariables(mockExecution)

        verify(mockExecution).setVariable('prefix', 'RPLVnfI_')
        verify(mockExecution).setVariable('Request', null)
        verify(mockExecution).setVariable('ReplaceVnfSuccessIndicator', false)
        verify(mockExecution).setVariable('currentActivity', 'RPLVnfI')
        verify(mockExecution).setVariable('errorCode', "0")
        verify(mockExecution).setVariable('retainResources', true)
        verify(mockExecution).setVariable("rollbackSetClosedLoopDisabledFlag", false)
        verify(mockExecution).setVariable("rollbackVnfStop", false)
        verify(mockExecution).setVariable("rollbackVnfLock", false)
        verify(mockExecution).setVariable("rollbackQuiesceTraffic", false)
        verify(mockExecution).setVariable("platform", null)
        verify(mockExecution).setVariable("lineOfBusiness", null)
    }

    @Test
    void testPreProcessRequest_success() {
        when(mockExecution.getId()).thenReturn("test-exec-id")
        when(mockExecution.getVariable('serviceInstanceId')).thenReturn("si-123")
        when(mockExecution.getVariable('vnfId')).thenReturn("vnf-456")
        when(mockExecution.getVariable('vnfType')).thenReturn("vFW")
        when(mockExecution.getVariable('requestId')).thenReturn("req-789")
        when(mockExecution.getVariable('bpmnRequest')).thenReturn("""{
            "requestDetails": {
                "requestInfo": {
                    "source": "VID",
                    "requestorId": "testUser",
                    "instanceName": "my-vnf",
                    "productFamilyId": "pf-123"
                },
                "modelInfo": {
                    "modelType": "vnf",
                    "modelInvariantUuid": "inv-uuid-123",
                    "modelUuid": "uuid-123",
                    "modelName": "vFW",
                    "modelVersion": "1.0"
                },
                "cloudConfiguration": {
                    "lcpCloudRegionId": "dfwtx",
                    "cloudOwner": "att-aic",
                    "tenantId": "tenant-123"
                },
                "requestParameters": {
                    "controllerType": "APPC",
                    "usePreload": true,
                    "userParams": []
                },
                "relatedInstanceList": [
                    {
                        "relatedInstance": {
                            "modelInfo": {
                                "modelType": "service",
                                "modelInvariantUuid": "svc-inv-uuid",
                                "modelUuid": "svc-uuid",
                                "modelName": "vFW-svc",
                                "modelVersion": "2.0"
                            }
                        }
                    }
                ]
            }
        }""")

        ReplaceVnfInfra replaceVnfInfra = new ReplaceVnfInfra()
        replaceVnfInfra.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable('serviceInstanceId', "si-123")
        verify(mockExecution).setVariable('controllerType', "APPC")
        verify(mockExecution).setVariable('vnfName', "my-vnf")
        verify(mockExecution).setVariable('requestorId', "testUser")
        verify(mockExecution).setVariable('usePreload', true)
        verify(mockExecution).setVariable('productFamilyId', "pf-123")
        verify(mockExecution).setVariable('asdcServiceModelVersion', "2.0")
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequest_invalidJson() {
        when(mockExecution.getId()).thenReturn("test-exec-id")
        when(mockExecution.getVariable('bpmnRequest')).thenReturn("invalid-json")

        ReplaceVnfInfra replaceVnfInfra = new ReplaceVnfInfra()
        replaceVnfInfra.preProcessRequest(mockExecution)
    }
}
