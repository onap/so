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
import org.onap.so.beans.nsmf.SliceTaskParams
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInfo
import org.onap.so.bpmn.core.domain.ServiceProxy

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

class CreateSliceServiceTest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("CreateSliceService")
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
                         "UUIRequest": {
                              "service":{
                                   "name": "NSMF",
                                   "description": "CSMFService",
                                   "serviceInvariantUuid": "123456",
                                   "serviceUuid": "123456",
                                   "globalSubscriberId": "5GCustomer",
                                   "serviceType": "5G",
                                   "parameters": {},
                                   "requestInputs": {
                                        "sST": "embb",
                                        "sNSSAI": "1-10101",
                                        "uEMobilityLevel": "stationary",
                                        "areaTrafficCapDL": 123,
                                        "maxNumberofUEs": 1000,
                                        "expDataRateUL": 2000,
                                        "plmnIdList": "39-00|39-01",
                                        "areaTrafficCapUL": 456,
                                        "latency": 300,
                                        "expDataRateDL": 400,
                                        "coverageAreaTAList": 101001,
                                        "activityFactor": 99,
                                        "resourceSharingLevel": "shared"
                                   }
                              }
                         }
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
        }""".replaceAll("\\s+", "")

    final String uuiRequest = """
        "service":{
             "name": "NSMF",
             "description": "CSMFService",
             "serviceInvariantUuid": "123456",
             "serviceUuid": "123456",
             "globalSubscriberId": "5GCustomer",
             "serviceType": "5G",
             "parameters": {},
             "requestInputs": {
                  "sST": "embb",
                  "sNSSAI": "1-10101",
                  "uEMobilityLevel": "stationary",
                  "areaTrafficCapDL": 123,
                  "maxNumberofUEs": 1000,
                  "expDataRateUL": 2000,
                  "plmnIdList": "39-00|39-01",
                  "areaTrafficCapUL": 456,
                  "latency": 300,
                  "expDataRateDL": 400,
                  "coverageAreaTAList": 101001,
                  "activityFactor": 99,
                  "resourceSharingLevel": "shared"
             }
        }""".replaceAll("\\s+", "")

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
        Mockito.verify(mockExecution, times(14)).setVariable(captor.capture() as String, captor.capture())
        List<ExecutionEntity> values = captor.getAllValues()
        assertNotNull(values)
    }

    @Test
    void testPrepareDecomposeService() {
        when(mockExecution.getVariable("uuiRequest")).thenReturn(uuiRequest)
        when(mockExecution.getVariable("serviceProfile")).thenReturn(serviceProfile)
        CreateSliceService sliceService = new CreateSliceService()
        sliceService.prepareDecomposeService(mockExecution)

        String serviceModelInfoExcept = """{
            "modelInvariantUuid":"123456",
            "modelUuid":"123456",
            "modelVersion":""
            }"""
        Mockito.verify(mockExecution, times(1)).setVariable(eq("ssServiceModelInfo"), captor.capture())
        String serviceModelInfo = captor.getValue()
        assertEquals(serviceModelInfoExcept.replaceAll("\\s+", ""),
                serviceModelInfo.replaceAll("\\s+", ""))
    }

    @Test
    void testProcessDecomposition() {
        when(mockExecution.getVariable("uuiRequest")).thenReturn(uuiRequest)
        when(mockExecution.getVariable("serviceProfile")).thenReturn(serviceProfile)
        when(mockExecution.getVariable("nstSolution")).thenReturn(nstSolution)

        CreateSliceService sliceService = new CreateSliceService()
        sliceService.processDecomposition(mockExecution)

        Mockito.verify(mockExecution, times(1)).setVariable(eq("subscriptionServiceType"), captor.capture())
        assertEquals(captor.getValue(), "5G")
        Mockito.verify(mockExecution, times(1)).setVariable(eq("serviceType"), captor.capture())
        assertEquals(captor.getValue(), "embb")
        Mockito.verify(mockExecution, times(1)).setVariable(eq("resourceSharingLevel"), captor.capture())
        assertEquals(captor.getValue(), "shared")
        Mockito.verify(mockExecution, times(1)).setVariable(eq("nstModelUuid"), captor.capture())
        assertEquals(captor.getValue(), "aaaaaa")
        Mockito.verify(mockExecution, times(1)).setVariable(eq("nstModelInvariantUuid"), captor.capture())
        assertEquals(captor.getValue(), "bbbbbb")
    }

    @Test
    void testPrepareCreateOrchestrationTask() {
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("123456")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("test")
        when(mockExecution.getVariable("serviceProfile")).thenReturn(serviceProfile)

        CreateSliceService sliceService = new CreateSliceService()
        sliceService.prepareCreateOrchestrationTask(mockExecution)

        SliceTaskParams sliceTaskParamsExpect = new SliceTaskParams()
        sliceTaskParamsExpect.setServiceId("123456")
        sliceTaskParamsExpect.setServiceName("test")
        sliceTaskParamsExpect.setServiceProfile(serviceProfile)
        String paramJsonExpect = sliceTaskParamsExpect.convertToJson()

        Mockito.verify(mockExecution, times(2)).setVariable(eq("subscriptionServiceType"), captor.capture())
        List allValues = captor.getAllValues()
        SliceTaskParams sliceTaskParams = allValues.get(0)
        String paramJson = allValues.get(1)
        assertEquals(sliceTaskParams.getServiceId(), sliceTaskParams.getServiceId())
        assertEquals(sliceTaskParams.getServiceName(), sliceTaskParams.getServiceName())
        assertEquals(sliceTaskParams.getServiceProfile(), sliceTaskParams.getServiceProfile())
        assertEquals(paramJsonExpect, paramJson)
    }

    @Test
    void testSendSyncResponse() {
        when(mockExecution.getVariable("operationId")).thenReturn("123456")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        CreateSliceService sliceService = new CreateSliceService()
        sliceService.sendSyncResponse(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("sentSyncResponse"), captor.capture())
        def catchSyncResponse = captor.getValue()
        assertEquals(catchSyncResponse, true)
    }

}
