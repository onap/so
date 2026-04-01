/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInfo
import org.onap.so.bpmn.core.domain.ServiceProxy

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.atLeast
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

class CreateSliceServiceTest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        mockExecution = setupMock("CreateSliceService")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    final String bpmnRequest =  """
        {
            "requestDetails": {
                "modelInfo": {
                    "modelInvariantId": "123456",
                    "modelType": "service",
                    "modelNameVersionId": "123456",
                    "modelName": "Service Profile",
                    "modelVersion": "1.0",
                    "modelVersionId": "123456",
                    "modelUuid": "123456",
                    "modelInvariantUuid": "123456"
                },
                "requestInfo": {
                    "source": "UUI",
                    "instanceName": "NSMF",
                    "suppressRollback": true
                },
                "subscriberInfo": {
                    "globalSubscriberId": "5GCustomer"
                },
                "requestParameters": {
                    "subscriptionServiceType": "MOG",
                    "userParams": [{
                        "ServiceInstanceName": "NSMF",
                        "UUIRequest": "{\\"service\\":{\\"name\\":\\"NSMF\\",\\"description\\":\\"CSMFService\\",\\"serviceInvariantUuid\\":\\"123456\\",\\"serviceUuid\\":\\"123456\\",\\"globalSubscriberId\\":\\"5GCustomer\\",\\"serviceType\\":\\"5G\\",\\"parameters\\":{\\"requestInputs\\":{\\"sST\\":\\"embb\\",\\"sNSSAI\\":\\"1-10101\\",\\"uEMobilityLevel\\":\\"stationary\\",\\"areaTrafficCapDL\\":123,\\"maxNumberofUEs\\":1000,\\"expDataRateUL\\":2000,\\"plmnIdList\\":\\"39-00|39-01\\",\\"areaTrafficCapUL\\":456,\\"latency\\":300,\\"expDataRateDL\\":400,\\"coverageAreaTAList\\":101001,\\"activityFactor\\":99,\\"resourceSharingLevel\\":\\"shared\\"}}}}"
                    }],
                    "aLaCarte": true,
                    "usePreload": true
                }
            },
            "serviceInstanceId": null,
            "vnfInstanceId": null,
            "networkInstanceId": null,
            "volumeGroupInstanceId": null,
            "vfModuleInstanceId": null,
            "configurationId": null,
            "instanceGroupId": null
        }"""

    final def serviceProfile = ["sST": "embb", "sNSSAI": "1-10101", "uEMobilityLevel": "stationary", "areaTrafficCapDL": 123,
                                "maxNumberofUEs": 1000, "expDataRateUL": 2000, "plmnIdList": "39-00|39-01", "areaTrafficCapUL": 456,
                                "latency": 300, "expDataRateDL": 400, "coverageAreaTAList": 101001, "activityFactor": 99,
                                "resourceSharingLevel": "shared"]

    final def nstSolution = ["UUID": "aaaaaa", "NSTName": "test NST", "invariantUUID": "bbbbbb", "matchLevel": "100%"]

    @Test
    void testPreProcessRequest() {
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(bpmnRequest)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("123456")
        CreateSliceService sliceService = new CreateSliceService()
        sliceService.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution, atLeast(1)).setVariable(captor.capture() as String, captor.capture())
        List<ExecutionEntity> values = captor.getAllValues()
        assertNotNull(values)
    }

    @Test
    void testPrepareCreateOrchestrationTask() {
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("123456")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("test")
        when(mockExecution.getVariable("serviceProfile")).thenReturn(serviceProfile)

        CreateSliceService sliceService = new CreateSliceService()
        sliceService.prepareCreateOrchestrationTask(mockExecution)

        ArgumentCaptor<SliceTaskParamsAdapter> sliceParamsCaptor = ArgumentCaptor.forClass(SliceTaskParamsAdapter.class)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("sliceTaskParams"), sliceParamsCaptor.capture())
        SliceTaskParamsAdapter sliceTaskParams = sliceParamsCaptor.getValue()
        assertEquals("123456", sliceTaskParams.getServiceId())
        assertEquals("test", sliceTaskParams.getServiceName())
        assertEquals(serviceProfile, sliceTaskParams.getServiceProfile())
    }

    @Test
    void testSendSyncResponse() {
        when(mockExecution.getVariable("operationId")).thenReturn("123456")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("isAsyncProcess")).thenReturn("true")
        CreateSliceService sliceService = new CreateSliceService()
        sliceService.sendSyncResponse(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("sentSyncResponse"), captor.capture())
        def catchSyncResponse = captor.getValue()
        assertEquals(catchSyncResponse, true)
    }

}
