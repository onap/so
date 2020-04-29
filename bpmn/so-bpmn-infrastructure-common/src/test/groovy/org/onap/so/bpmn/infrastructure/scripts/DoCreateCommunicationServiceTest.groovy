/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2019, CMCC Technologies Co., Ltd.
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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.aai.domain.yang.CommunicationServiceProfile
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.aaiclient.client.aai.AAIResourcesClient

import static com.shazam.shazamcrest.MatcherAssert.assertThat
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

class DoCreateCommunicationServiceTest extends MsoGroovyTest {

    private DoCreateCommunicationService communicationService = new DoCreateCommunicationService()

    @Before
    public void setUp() throws Exception {
        super.init("DoCreateCommunicationService")
        communicationService.client = spy(AAIResourcesClient.class)
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    public void testPreProcessRequest(){
        mockData()
        DoCreateCommunicationService communicationService = new DoCreateCommunicationService()
        communicationService.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        assertNotNull(captor.getAllValues())
    }

    @Test
    public void testCreateCommunicationService() {
        mockData()
        ServiceInstance expectedServiceInstanceData = getExpectedServiceInstance()

        try {
            communicationService.createCommunicationService(mockExecution)
        } catch (Exception e) {

            assertEquals(e.class, BpmnError.class)
        }


        Mockito.verify(mockExecution).setVariable(eq("communicationServiceInstance"), captor.capture())
        ServiceInstance resData = captor.getValue()
        resData.setCreatedAt("")
        resData.setEnvironmentContext("")
        assertThat(resData, sameBeanAs(expectedServiceInstanceData))
    }

    @Test
    public void testCreateCommunicationServiceProfile() {
        mockData()
        DoCreateCommunicationService communicationService = new DoCreateCommunicationService()

        CommunicationServiceProfile expectedServiceInstanceData = getExpectedServiceInstanceProfile()

        try {
            communicationService.createCommunicationServiceProfile(mockExecution)
        } catch (Exception e) {
            assertEquals(e.class, BpmnError.class)
        }

        Mockito.verify(mockExecution).setVariable(eq("communicationServiceInstanceProfile"), captor.capture())
        CommunicationServiceProfile resData = captor.getValue()
        resData.setProfileId("")
        assertThat(resData, sameBeanAs(expectedServiceInstanceData))
    }

    private static CommunicationServiceProfile getExpectedServiceInstanceProfile() {
        CommunicationServiceProfile expectedServiceInstanceData = new CommunicationServiceProfile()
        expectedServiceInstanceData.setProfileId("")
        expectedServiceInstanceData.setLatency(20)
        expectedServiceInstanceData.setMaxNumberOfUEs(300)
        expectedServiceInstanceData.setUeMobilityLevel("stationary")
        expectedServiceInstanceData.setResourceSharingLevel("shared")
        expectedServiceInstanceData.setExpDataRateUL(30)
        expectedServiceInstanceData.setExpDataRateDL(10)
        expectedServiceInstanceData.setCoverageAreaList("01001")
        return expectedServiceInstanceData
    }

    private static ServiceInstance getExpectedServiceInstance() {
        ServiceInstance expectedServiceInstanceData = new ServiceInstance()
        expectedServiceInstanceData.setServiceInstanceName("CSMFService")
        expectedServiceInstanceData.setServiceRole("communication-service")
        expectedServiceInstanceData.setOrchestrationStatus("processing")
        expectedServiceInstanceData.setModelInvariantId("e75698d9-925a-4cdd-a6c0-edacbe6a0b51")
        expectedServiceInstanceData.setModelVersionId("8ee5926d-720b-4bb2-86f9-d20e921c143b")
        expectedServiceInstanceData.setInputParameters("""{
            "service":{
                "name":"CSMFService",
                "description":"CSMFService",
                "serviceInvariantUuid":"e75698d9-925a-4cdd-a6c0-edacbe6a0b51",
                "serviceUuid":"8ee5926d-720b-4bb2-86f9-d20e921c143b",
                "globalSubscriberId":"5GCustomer",
                "serviceType":"5G",
                "parameters":{
                    "requestInputs":{
                        "expDataRateDL":10,
                        "expDataRateUL":30,
                        "latency":20,
                        "maxNumberofUEs":300,
                        "uemobilityLevel":"stationary",
                        "resourceSharingLevel":"shared",
                        "coverageAreaList": "01001",
                        "useInterval":"3"
                    }
                }
            }
        }""")
        expectedServiceInstanceData.setWorkloadContext("3")
        expectedServiceInstanceData.setCreatedAt("")
        expectedServiceInstanceData.setEnvironmentContext("")
        return expectedServiceInstanceData
    }

    private void mockData() {
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("CSMFService")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")
        when(mockExecution.getVariable("uuiRequest")).thenReturn("""{
            "service":{
                "name":"CSMFService",
                "description":"CSMFService",
                "serviceInvariantUuid":"e75698d9-925a-4cdd-a6c0-edacbe6a0b51",
                "serviceUuid":"8ee5926d-720b-4bb2-86f9-d20e921c143b",
                "globalSubscriberId":"5GCustomer",
                "serviceType":"5G",
                "parameters":{
                    "requestInputs":{
                        "expDataRateDL":10,
                        "expDataRateUL":30,
                        "latency":20,
                        "maxNumberofUEs":300,
                        "uemobilityLevel":"stationary",
                        "resourceSharingLevel":"shared",
                        "coverageAreaList": "01001",
                        "useInterval":"3"
                    }
                }
            }
        }""")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        Map<String, Object> csInputMap = new HashMap<>()
        csInputMap.put("expDataRateDL", 10)
        csInputMap.put("expDataRateUL", 30)
        csInputMap.put("latency", 20)
        csInputMap.put("maxNumberofUEs", 300)
        csInputMap.put("uEMobilityLevel", "stationary")
        csInputMap.put("resourceSharingLevel", "shared")
        csInputMap.put("coverageAreaTAList", "01001")
        csInputMap.put("useInterval", "3")

        when(mockExecution.getVariable("csInputMap")).thenReturn(csInputMap)

        when(mockExecution.getVariable("modelInvariantUuid")).thenReturn("e75698d9-925a-4cdd-a6c0-edacbe6a0b51")
        when(mockExecution.getVariable("modelUuid")).thenReturn("8ee5926d-720b-4bb2-86f9-d20e921c143b")
        when(mockExecution.getVariable("useInterval")).thenReturn("3")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5G")
    }
}
