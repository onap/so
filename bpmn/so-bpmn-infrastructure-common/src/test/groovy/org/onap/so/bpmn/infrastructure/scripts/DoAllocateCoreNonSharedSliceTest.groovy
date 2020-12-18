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

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertEquals

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

        String networkServiceModelInfo="""{
                        "modelInfo"                : {
                                "modelName"              : "vfw_cnf_service_2310 Service Proxy",
                                "modelUuid"              : "35386eb0-b673-48c5-9757-45ecfc506bf8",
                                "modelInvariantUuid"     : "b048d7bc-8bfd-4950-aea5-22b1aaf5d76b",
                                "modelVersion"           : "1.0",
                                "modelCustomizationUuid" : "82f4db76-e7ad-47eb-b5e3-661683f14de6",
                                "modelInstanceName"      : "vfw_cnf_service_2310_proxy 0"
                },
                        "toscaNodeType"            : "org.openecomp.nodes.vfw_cnf_service_2310_proxy",
                        "description"            : "A Proxy for Service vfw_cnf_service_2310",
                        "sourceModelUuid"            : "f3666c56-744e-4055-9f4a-0726460898e0"
                }"""

		String sliceParams= """{\r\n\t\"sliceProfile\": {\r\n\t\t\"snssaiList\": [\r\n\t\t\t\"001-100001\"\r\n\t\t],\r\n\t\t\"sliceProfileId\": \"ab9af40f13f721b5f13539d87484098\",\r\n\t\t\"plmnIdList\": [\r\n\t\t\t\"460-00\",\r\n\t\t\t\"460-01\"\r\n\t\t],\r\n\t\t\"perfReq\": {\r\n\t\t\t\"perfReqEmbbList \": [{\r\n\t\t\t\t\"activityFactor\": 50\r\n\t\t\t}]\r\n\t\t},\r\n\t\t\"maxNumberofUEs\": 200,\r\n\t\t\"coverageAreaTAList\": [\r\n\t\t\t\"1\",\r\n\t\t\t\"2\",\r\n\t\t\t\"3\",\r\n\t\t\t\"4\"\r\n\t\t],\r\n\t\t\"latency\": 2,\r\n\t\t\"resourceSharingLevel\": \"non-shared\"\r\n\t},\r\n\t\"endPoints\": [{\r\n\t\t\"IpAdress\": \"\",\r\n\t\t\"LogicalLinkId\": \"\",\r\n\t\t\"nextHopInfo\": \"\"\r\n\t}],\r\n\t\"nsiInfo\": {\r\n\t\t\"nsiId\": \"NSI-M-001-HDBNJ-NSMF-01-A-ZX\",\r\n\t\t\"nsiName\": \"eMBB-001\"\r\n\t},\r\n\t\"scriptName\": \"AN1\"\r\n}"""

        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("123456")
        when(mockExecution.getVariable("networkServiceModelInfo")).thenReturn(networkServiceModelInfo)

		when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)

        DoAllocateCoreNonSharedSlice allocateNssi = new DoAllocateCoreNonSharedSlice()
        allocateNssi.preProcessRequest(mockExecution)

        Mockito.verify(mockExecution, times(1)).setVariable(eq("networkServiceModelUuid"), captor.capture())
        captor.getValue()
        assertEquals("f3666c56-744e-4055-9f4a-0726460898e0", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("networkServiceName"), captor.capture())
        assertEquals("vfw_cnf_service_2310", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("orchestrationStatus"), captor.capture())
        assertEquals("created", captor.getValue())

        Mockito.verify(mockExecution, times(5)).setVariable(captor.capture() as String, captor.capture())
    }

    @Test
    void testPrepareServiceOrderRequest() {

        String sliceProfile = "{\r\n      \"snssaiList\": [ \r\n        \"001-100001\"\r\n      ],\r\n      \"sliceProfileId\": \"ab9af40f13f721b5f13539d87484098\",\r\n      \"plmnIdList\": [\r\n        \"460-00\",\r\n        \"460-01\"\r\n      ],\r\n      \"perfReq\": {\r\n        \"perfReqEmbbList \": [\r\n          {\r\n            \"activityFactor\": 50\r\n          }\r\n        ]\r\n      },\r\n      \"maxNumberofUEs\": 200, \r\n      \"coverageAreaTAList\": [ \r\n        \"1\",\r\n        \"2\",\r\n        \"3\",\r\n        \"4\"\r\n      ],\r\n      \"latency\": 2,\r\n      \"resourceSharingLevel\": \"non-shared\" \r\n    }"
        when(mockExecution.getVariable("sliceProfile")).thenReturn(sliceProfile)
        when(mockExecution.getVariable("serviceType")).thenReturn("5g")
        when(mockExecution.getVariable("networkServiceName")).thenReturn("5g_embb")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("networkServiceModelUuid")).thenReturn("12345")
        when(mockExecution.getVariable("vnfInstanceName")).thenReturn("vf00")

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
        ServiceCharacteristicValue.put("name", "vf00_snssai")
        ServiceCharacteristicValue.put("value", ServiceCharacteristicValueObject)

        List expectedList= new ArrayList()
        expectedList.add(ServiceCharacteristicValue)

        ObjectMapper objectMapper = new ObjectMapper()
        Map<String, Object> serviceCharacteristic = objectMapper.readValue(sliceProfile, Map.class);

        DoAllocateCoreNonSharedSlice allocateNssi = new DoAllocateCoreNonSharedSlice()
        List characteristicList=allocateNssi.retrieveServiceCharacteristicsAsKeyValue(mockExecution, serviceCharacteristic)
        assertEquals(expectedList, characteristicList)
    }
}