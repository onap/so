package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.core.domain.Resource
import org.onap.so.bpmn.core.domain.ServiceDecomposition

import static com.shazam.shazamcrest.MatcherAssert.assertThat
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when
/**
 * Copyright 2018 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class DoCompareServiceInstanceDataTest extends MsoGroovyTest {


    public static final String uuiSoString = "{\n" +
            "    \"service\":{\n" +
            "        \"name\":\"SiteService\",\n" +
            "        \"description\":\"SiteService\",\n" +
            "        \"serviceInvariantUuid\":\"5c13f3fb-2744-4635-9f1f-c59c92dc8f70\",\n" +
            "        \"serviceUuid\":\"3a76b1f5-fb0d-4b6b-82d5-0e8a4ebc3838\",\n" +
            "        \"globalSubscriberId\":\"test_custormer\",\n" +
            "        \"serviceType\":\"example-service-type\",\n" +
            "        \"parameters\":{\n" +
            "            \"locationConstraints\":[\n" +
            "\n" +
            "            ],\n" +
            "            \"resources\":[\n" +
            "                {\n" +
            "                    \"resourceIndex\":\"1\",\n" +
            "                    \"resourceName\":\"sdwanvpnresource\",\n" +
            "                    \"resourceInvariantUuid\":\"0c0e1cbe-6e01-4f9e-8c45-a9700ebc14df\",\n" +
            "                    \"resourceUuid\":\"4ad2d390-5c51-45f5-9710-b467a4ec7a73\",\n" +
            "                    \"resourceCustomizationUuid\":\"66590e07-0777-415c-af44-36347cf3ddd3\",\n" +
            "                    \"parameters\":{\n" +
            "                        \"locationConstraints\":[\n" +
            "\n" +
            "                        ],\n" +
            "                        \"resources\":[\n" +
            "\n" +
            "                        ],\n" +
            "                        \"requestInputs\":{\n" +
            "\n" +
            "                        }\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"resourceIndex\":\"1\",\n" +
            "                    \"resourceName\":\"sdwansiteresource\",\n" +
            "                    \"resourceInvariantUuid\":\"97a3e552-08c4-4697-aeeb-d8d3e09ce58e\",\n" +
            "                    \"resourceUuid\":\"63d8e1af-32dc-4c71-891d-e3f7b6a976d2\",\n" +
            "                    \"resourceCustomizationUuid\":\"205456e7-3dc0-40c4-8cb0-28e6c1877042\",\n" +
            "                    \"parameters\":{\n" +
            "                        \"locationConstraints\":[\n" +
            "\n" +
            "                        ],\n" +
            "                        \"resources\":[\n" +
            "\n" +
            "                        ],\n" +
            "                        \"requestInputs\":{\n" +
            "\n" +
            "                        }\n" +
            "                    }\n" +
            "                },\n" +
            "                {\n" +
            "                    \"resourceIndex\":\"2\",\n" +
            "                    \"resourceName\":\"sdwansiteresource\",\n" +
            "                    \"resourceInvariantUuid\":\"97a3e552-08c4-4697-aeeb-d8d3e09ce58e\",\n" +
            "                    \"resourceUuid\":\"63d8e1af-32dc-4c71-891d-e3f7b6a976d2\",\n" +
            "                    \"resourceCustomizationUuid\":\"205456e7-3dc0-40c4-8cb0-28e6c1877042\",\n" +
            "                    \"parameters\":{\n" +
            "                        \"locationConstraints\":[\n" +
            "\n" +
            "                        ],\n" +
            "                        \"resources\":[\n" +
            "\n" +
            "                        ],\n" +
            "                        \"requestInputs\":{\n" +
            "\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            ],\n" +
            "            \"requestInputs\":{\n" +
            "                \"sdwanvpnresource_list\":[\n" +
            "                    {\n" +
            "                        \"sdwanvpn_topology\":\"hub_spoke\",\n" +
            "                        \"sdwanvpn_name\":\"defaultvpn\",\n" +
            "                        \"sdwansitelan_list\":[\n" +
            "                            {\n" +
            "                                \"role\":\"Hub\",\n" +
            "                                \"portType\":\"GE\",\n" +
            "                                \"portSwitch\":\"layer3-port\",\n" +
            "                                \"vlanId\":\"\",\n" +
            "                                \"ipAddress\":\"192.168.10.1\",\n" +
            "                                \"deviceName\":\"vCPE\",\n" +
            "                                \"portNumer\":\"0/0/1\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"role\":\"Hub\",\n" +
            "                                \"portType\":\"GE\",\n" +
            "                                \"portSwitch\":\"layer2-port\",\n" +
            "                                \"vlanId\":\"55\",\n" +
            "                                \"ipAddress\":\"192.168.11.1\",\n" +
            "                                \"deviceName\":\"CPE_Beijing\",\n" +
            "                                \"portNumer\":\"0/0/1\"\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"sdwansiteresource_list\":[\n" +
            "                    {\n" +
            "                        \"sdwansite_emails\":\"chenchuanyu@huawei.com\",\n" +
            "                        \"sdwansite_address\":\"Huawei Public Cloud\",\n" +
            "                        \"sdwansite_description\":\"DC Site\",\n" +
            "                        \"sdwansite_role\":\"dsvpn_hub\",\n" +
            "                        \"sdwansite_postcode\":\"20000\",\n" +
            "                        \"sdwansite_type\":\"single_gateway\",\n" +
            "                        \"sdwansite_latitude\":\"\",\n" +
            "                        \"sdwansite_controlPoint\":\"\",\n" +
            "                        \"sdwansite_longitude\":\"\",\n" +
            "                        \"sdwansitewan_list\":[\n" +
            "                            {\n" +
            "                                \"providerIpAddress\":\"\",\n" +
            "                                \"portType\":\"GE\",\n" +
            "                                \"inputBandwidth\":\"1000\",\n" +
            "                                \"ipAddress\":\"\",\n" +
            "                                \"name\":\"10000\",\n" +
            "                                \"transportNetworkName\":\"internet\",\n" +
            "                                \"outputBandwidth\":\"10000\",\n" +
            "                                \"deviceName\":\"vCPE\",\n" +
            "                                \"portNumber\":\"0/0/0\",\n" +
            "                                \"ipMode\":\"DHCP\",\n" +
            "                                \"publicIP\":\"119.3.7.113\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"sdwandevice_list\":[\n" +
            "                            {\n" +
            "                                \"esn\":\"XXXXXXX\",\n" +
            "                                \"vendor\":\"Huawei\",\n" +
            "                                \"name\":\"vCPE\",\n" +
            "                                \"type\":\"AR1000V\",\n" +
            "                                \"version\":\"1.0\",\n" +
            "                                \"class\":\"VNF\",\n" +
            "                                \"systemIp\":\"20.20.20.1\"\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"sdwansite_emails\":\"chenchuanyu@huawei.com\",\n" +
            "                        \"sdwansite_address\":\"Huawei Public Cloud\",\n" +
            "                        \"sdwansite_description\":\"DC Site\",\n" +
            "                        \"sdwansite_role\":\"dsvpn_hub\",\n" +
            "                        \"sdwansite_postcode\":\"20000\",\n" +
            "                        \"sdwansite_type\":\"single_gateway\",\n" +
            "                        \"sdwansite_latitude\":\"\",\n" +
            "                        \"sdwansite_controlPoint\":\"\",\n" +
            "                        \"sdwansite_longitude\":\"\",\n" +
            "                        \"sdwansitewan_list\":[\n" +
            "                            {\n" +
            "                                \"providerIpAddress\":\"\",\n" +
            "                                \"portType\":\"GE\",\n" +
            "                                \"inputBandwidth\":\"1000\",\n" +
            "                                \"ipAddress\":\"172.18.1.2/24\",\n" +
            "                                \"name\":\"10000\",\n" +
            "                                \"transportNetworkName\":\"internet\",\n" +
            "                                \"outputBandwidth\":\"10000\",\n" +
            "                                \"deviceName\":\"CPE_Beijing\",\n" +
            "                                \"portNumber\":\"0/0/0\",\n" +
            "                                \"ipMode\":\"Static\",\n" +
            "                                \"publicIP\":\"\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"sdwandevice_list\":[\n" +
            "                            {\n" +
            "                                \"esn\":\"XXXXXXX\",\n" +
            "                                \"vendor\":\"Huawei\",\n" +
            "                                \"name\":\"CPE_Beijing\",\n" +
            "                                \"type\":\"AR161\",\n" +
            "                                \"version\":\"1.0\",\n" +
            "                                \"class\":\"PNF\",\n" +
            "                                \"systemIp\":\"20.20.20.2\"\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"

    @Before
    void setUp() {
        super.init("DoCompareServiceInstanceData")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest() {
        mockData()
        DoCompareServiceInstanceData csi = new DoCompareServiceInstanceData()
        csi.preProcessRequest(mockExecution)
    }

    @Test(expected = BpmnError.class)
    void testPreProcessRequestException() {
        DoCompareServiceInstanceData csi = new DoCompareServiceInstanceData()
        csi.preProcessRequest(mockExecution)
    }

    @Test
    void testPrepareDecomposeService_Original() {
        mockData()
        DoCompareServiceInstanceData csi = new DoCompareServiceInstanceData()
        csi.prepareDecomposeService_Original(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        String serviceModelInfo = getServiceModelInfo()
        assertThat(captor.getValue(), sameBeanAs(serviceModelInfo))
    }

    @Test
    void testProcessDecomposition_Original() {
        mockData()
        DoCompareServiceInstanceData csi = new DoCompareServiceInstanceData()
        csi.processDecomposition_Original(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        ServiceDecomposition serviceDecomposition = getServiceDecomposition()
        assertThat(captor.getValue(), sameBeanAs(serviceDecomposition))
    }

    @Test
    void testDoCompareUuiRquestInput() {
        mockData()
        DoCompareServiceInstanceData csi = new DoCompareServiceInstanceData()
        csi.doCompareUuiRquestInput(mockExecution)
        Mockito.verify(mockExecution, times(4)).setVariable(captor.capture(), captor.capture())
    }

    private String getServiceModelInfo() {
        String modelInvariantUuid = mockExecution.getVariable("model-invariant-id-original")
        String modelUuid = mockExecution.getVariable("model-version-id-original")
        String serviceModelInfo = """{
            "modelInvariantUuid":"${modelInvariantUuid}",
            "modelUuid":"${modelUuid}",
            "modelVersion":""
             }"""
        serviceModelInfo
    }

    private void mockData() {
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("model-version-id-original")).thenReturn("12345")
        when(mockExecution.getVariable("model-invariant-id-original")).thenReturn("12345")
        when(mockExecution.getVariable("uuiRequest")).thenReturn(uuiSoString)
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("1234")
        when(mockExecution.getVariable("serviceInstanceData-original")).thenReturn(getExpectedServiceInstance())
        when(mockExecution.getVariable("serviceDecomposition")).thenReturn(getServiceDecomposition())
        when(mockExecution.getVariable("serviceDecomposition_Original")).thenReturn(getServiceDecomposition())
    }

    private ServiceDecomposition getServiceDecomposition() {
        ServiceDecomposition decomposition = new ServiceDecomposition()
        List<Resource> allSR_original = new ArrayList<>()
        decomposition.setAllottedResources(allSR_original)
        return decomposition
    }

    private ServiceInstance getExpectedServiceInstance() {
        ServiceInstance expectedServiceInstanceData = new ServiceInstance()
        expectedServiceInstanceData.setServiceInstanceId("1234")
        expectedServiceInstanceData.setServiceInstanceName("volte-service")
        expectedServiceInstanceData.setServiceType("E2E Service")
        expectedServiceInstanceData.setServiceRole("E2E Service")
        expectedServiceInstanceData.setInputParameters(uuiSoString)
        return expectedServiceInstanceData
    }
}
