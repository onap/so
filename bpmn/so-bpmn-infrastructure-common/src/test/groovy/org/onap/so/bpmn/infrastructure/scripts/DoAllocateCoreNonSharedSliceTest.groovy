/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020  Tech Mahindra
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

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import com.fasterxml.jackson.databind.ObjectMapper

import static org.mockito.Mockito.when
import static org.mockito.Mockito.times
import static org.mockito.Mockito.eq

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

class DoAllocateCoreNonSharedSliceTest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoAllocateCoreNonSharedSlice")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    public void testPreProcessRequest() {

        String networkServiceModelInfo=""" {
                                "modelName"              : "5GC-eMBB Service Proxy",
                                "modelUuid"              : "b666119e-4400-47c6-a0c1-bbe050a33b47",
                                "modelInvariantUuid"     : "a26327e1-4a9b-4883-b7a5-5f37dcb7405a",
                                "modelVersion"           : "1.0",
                                "modelCustomizationUuid" : "cbc12c2a-67e6-4336-9236-eaf51eacdc75",
                                "modelInstanceName"      : "5gcembb_proxy 0"
        }"""

        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("123456")
        when(mockExecution.getVariable("networkServiceModelInfo")).thenReturn(networkServiceModelInfo)

        DoAllocateCoreNonSharedSlice allocateNssi = new DoAllocateCoreNonSharedSlice()
        allocateNssi.preProcessRequest(mockExecution)

        Mockito.verify(mockExecution, times(1)).setVariable(eq("networkServiceModelUuid"), captor.capture())
        captor.getValue()
        assertEquals("b666119e-4400-47c6-a0c1-bbe050a33b47", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("networkServiceName"), captor.capture())
        assertEquals("5GC-eMBB", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("orchestrationStatus"), captor.capture())
        assertEquals("created", captor.getValue())
        Mockito.verify(mockExecution, times(4)).setVariable(captor.capture() as String, captor.capture())
    }

    @Test
    void testPrepareServiceOrderRequest() {

        String sliceProfile = "{\r\n      \"snssaiList\": [ \r\n        \"001-100001\"\r\n      ],\r\n      \"sliceProfileId\": \"ab9af40f13f721b5f13539d87484098\",\r\n      \"plmnIdList\": [\r\n        \"460-00\",\r\n        \"460-01\"\r\n      ],\r\n      \"perfReq\": {\r\n        \"perfReqEmbbList \": [\r\n          {\r\n            \"activityFactor\": 50\r\n          }\r\n        ]\r\n      },\r\n      \"maxNumberofUEs\": 200, \r\n      \"coverageAreaTAList\": [ \r\n        \"1\",\r\n        \"2\",\r\n        \"3\",\r\n        \"4\"\r\n      ],\r\n      \"latency\": 2,\r\n      \"resourceSharingLevel\": \"non-shared\" \r\n    }"
        when(mockExecution.getVariable("sliceProfile")).thenReturn(sliceProfile)
        when(mockExecution.getVariable("serviceType")).thenReturn("5g")
        when(mockExecution.getVariable("networkServiceName")).thenReturn("5g_embb")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("networkServiceModelUuid")).thenReturn("12345")

        DoAllocateCoreNonSharedSlice allocateNssi = new DoAllocateCoreNonSharedSlice()
        allocateNssi.prepareServiceOrderRequest(mockExecution)

        Mockito.verify(mockExecution, times(1)).setVariable(eq("serviceOrderRequest"), captor.capture())
        String value = captor.getValue()
        assertNotNull(value)
    }

    @Test
    void testRetrieveServiceCharacteristicsAsKeyValue() {

        String sliceProfile = "{\r\n      \"snssaiList\": [ \r\n        \"001-100001\"\r\n      ],\r\n      \"sliceProfileId\": \"ab9af40f13f721b5f13539d87484098\",\r\n      \"plmnIdList\": [\r\n        \"460-00\",\r\n        \"460-01\"\r\n      ],\r\n      \"perfReq\": {\r\n        \"perfReqEmbbList \": [\r\n          {\r\n            \"activityFactor\": 50\r\n          }\r\n        ]\r\n      },\r\n      \"maxNumberofUEs\": 200, \r\n      \"coverageAreaTAList\": [ \r\n        \"1\",\r\n        \"2\",\r\n        \"3\",\r\n        \"4\"\r\n      ],\r\n      \"latency\": 2,\r\n      \"resourceSharingLevel\": \"non-shared\" \r\n    }"
        Map<String, Object> ServiceCharacteristicValue = new LinkedHashMap<>()
        Map<String, Object> ServiceCharacteristicValueObject = new LinkedHashMap<>()
        ServiceCharacteristicValueObject.put("serviceCharacteristicValue","001-100001")
        ServiceCharacteristicValue.put("name", "snssai")
        ServiceCharacteristicValue.put("value", ServiceCharacteristicValueObject)

        List expectedList= new ArrayList()
        expectedList.add(ServiceCharacteristicValue)

        ObjectMapper objectMapper = new ObjectMapper()
        Map<String, Object> serviceCharacteristic = objectMapper.readValue(sliceProfile, Map.class);

        DoAllocateCoreNonSharedSlice allocateNssi = new DoAllocateCoreNonSharedSlice()
        List characteristicList=allocateNssi.retrieveServiceCharacteristicsAsKeyValue(serviceCharacteristic)

        assertEquals(expectedList, characteristicList)
    }
}