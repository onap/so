/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

class DoDeallocateTnNssiTest extends MsoGroovyTest {
    @Before
    void init() throws IOException {
        super.init("DeallocateTnNssiTest")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest() {
        when(mockExecution.getVariable("msoRequestId")).thenReturn("4c614769-f58a-4556-8ad9-dcd903077c82")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
        when(mockExecution.getVariable("modelInvariantUuid")).thenReturn("f85cbcc0-ad74-45d7-a5a1-17c8744fdb71")
        when(mockExecution.getVariable("modelUuid")).thenReturn("36a3a8ea-49a6-4ac8-b06c-89a54544b9b6")
        when(mockExecution.getVariable("serviceInstanceID")).thenReturn("eb0863e9-a69b-4b17-8a56-f05ad110bef7")
        when(mockExecution.getVariable("operationId")).thenReturn("998c2081-5a71-4a39-9ae6-d6b7c5bb50c0")
        when(mockExecution.getVariable("operationType")).thenReturn("opTypeTest")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("servicename")).thenReturn("5G-test")
        when(mockExecution.getVariable("networkType")).thenReturn("5G-network")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5G-service")
        when(mockExecution.getVariable("nsiId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable("jobId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
        when(mockExecution.getVariable("sliceParams")).thenReturn("""
        {
    "sliceProfile": {
      "snssaiList": [
        "001-100001"
      ],
      "sliceProfileId": "ab9af40f13f721b5f13539d87484098",
      "plmnIdList": [
        "460-00",
        "460-01"
      ],
      "perfReq": {
      },
      "coverageAreaTAList": [
      ],
      "latency": 2,
      "maxBandwidth": 100,
      "resourceSharingLevel": "non-shared"
    },
    "transportSliceNetworks": [
            {
                "connectionLinks": [
                    {
                        "transportEndpointA": "tranportEp_ID_XXX",
                        "transportEndpointB": "tranportEp_ID_YYY"
                    },
                    {
                        "transportEndpointA": "tranportEp_ID_AAA",
                        "transportEndpointB": "tranportEp_ID_BBB"
                    }
                ]
            },
            {
                "connectionLinks": [
                    {
                        "transportEndpointA": "tranportEp_ID_CCC",
                        "transportEndpointB": "tranportEp_ID_DDD"
                    },
                    {
                        "transportEndpointA": "tranportEp_ID_EEE",
                        "transportEndpointB": "tranportEp_ID_FFF"
                    }
                ]
            }
    ],
    "nsiInfo": {
      "nsiId": "NSI-M-001-HDBNJ-NSMF-01-A-ZX",
      "nsiName": "eMBB-001"
    },
    "scriptName": "AN1"
        }""".replaceAll("\\\\s+", ""))

        DoDeallocateTnNssi runScript = new DoDeallocateTnNssi()
        runScript.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("sliceServiceInstanceId"), captor.capture())
        String sliceServiceInstanceId = captor.getValue()
        assertNotNull(sliceServiceInstanceId)
    }

    @Test
    void testDeleteServiceInstance() {
        when(mockExecution.getVariable("serviceInstanceID")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5G")

        AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, "5GCustomer", "5G", "5ad89cf9-0569-4a93-9306-d8324321e2be")
        DoDeallocateTnNssi obj = spy(DoDeallocateTnNssi.class)
        when(obj.getAAIClient()).thenReturn(client)
        doNothing().when(client).delete(serviceInstanceUri)

        obj.deleteServiceInstance(mockExecution)
        Mockito.verify(client, times(1)).delete(serviceInstanceUri)
    }
}
