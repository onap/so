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

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
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

class CreateCommunicationServiceTest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("CreateCommunicationService")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest() {
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("bpmnRequest")).thenReturn("""
        {
            "requestDetails": {
                "modelInfo": {
                    "modelInvariantId": "e75698d9-925a-4cdd-a6c0-edacbe6a0b51",
                    "modelType": "service",
                    "modelNameVersionId": "8ee5926d-720b-4bb2-86f9-d20e921c143b",
                    "modelName": "voLTE",
                    "modelVersion": "1.0",
                    "modelVersionId": "8ee5926d-720b-4bb2-86f9-d20e921c143b",
                    "modelUuid": "8ee5926d-720b-4bb2-86f9-d20e921c143b",
                    "modelInvariantUuid": "e75698d9-925a-4cdd-a6c0-edacbe6a0b51" 
                },
                "requestInfo": {
                    "source": "UUI",
                    "instanceName": "CSMFService",
                    "suppressRollback": true
                },
                "subscriberInfo": {
                    "globalSubscriberId": "5GCustomer"
                },
                "requestParameters": {
                    "subscriptionServiceType": "MOG",
                    "userParams": [{
                        "ServiceInstanceName": "CSMFService",
                         "UUIRequest": "{\\r\\n    \\
                               "service\\":{\\r\\n        \\"name\\":\\"CSMFService\\",\\r\\n
                                \\"description\\":\\"CSMFService\\",\\r\\n        
                                \\"serviceInvariantUuid\\":\\"e75698d9-925a-4cdd-a6c0-edacbe6a0b51\\",\\r\\n    
                                \\"serviceUuid\\":\\"8ee5926d-720b-4bb2-86f9-d20e921c143b\\",\\r\\n        
                                \\"globalSubscriberId\\":\\"5GCustomer\\",\\r\\n        
                                \\"serviceType\\":\\"5G\\",\\r\\n        
                                \\"parameters\\":{\\r\\n            
                                \\"requestInputs\\":{\\r\\n                
                                \\"expDataRateDL\\":10,\\r\\n                
                                \\"expDataRateUL\\":30,\\r\\n                
                                \\"latency\\":20,\\r\\n                
                                \\"maxNumberofUEs\\":300,\\r\\n                
                                \\"uemobilityLevel\\":\\"stationary\\",\\r\\n                
                                \\"resourceSharingLevel\\":\\"shared\\",\\r\\n                
                                \\"coverageAreaList\\": \\"01001\\",\\r\\n                
                                \\"useInterval\\":\\"3\\"\\r\\n            
                                }\\r\\n        
                                }\\r\\n    }\\r\\n}\\r\\n"
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
        }""".replaceAll("\\s+", ""))
        when(mockExecution.getVariable("mso-request-id")).thenReturn("edb08d97-e0f9-4c71-840a-72080d7be42e")
        CreateCommunicationService communicationService = new CreateCommunicationService()
        communicationService.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution, times(14)).setVariable(captor.capture() as String, captor.capture())
        List<ExecutionEntity> values = captor.getAllValues()
        assertNotNull(values)
    }

    @Test
    void testPrepareInitOperationStatus() {

        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("operationId")).thenReturn("54321")

        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("11111")

        CreateCommunicationService communicationService = new CreateCommunicationService()

        communicationService.prepareInitOperationStatus(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String res = captor.getValue()
        assertNotNull(res)
    }


    @Test
    void testSendSyncResponse() {
        when(mockExecution.getVariable("operationId")).thenReturn("123456")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        CreateCommunicationService communicationService = new CreateCommunicationService()
        communicationService.sendSyncResponse(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("sentSyncResponse"), captor.capture())
        def updateVolumeGroupRequest = captor.getValue()
        assertEquals(updateVolumeGroupRequest, true)
    }


    @Test
    void testPrepareDoComposeE2E() {
        ServiceDecomposition serviceDecomposition = new ServiceDecomposition()
        ServiceProxy serviceProxy = new ServiceProxy()
        serviceProxy.setSourceModelUuid("f2f5967e-72d3-4c5c-b880-e214e71dba4e")
        serviceDecomposition.setServiceProxy(Arrays.asList(serviceProxy))
        when(mockExecution.getVariable("csServiceDecomposition")).thenReturn(serviceDecomposition)
        CreateCommunicationService communicationService = new CreateCommunicationService()
        communicationService.prepareDoComposeE2E(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("e2eServiceModelInfo"), captor.capture())
        String e2eServiceModelInfo = captor.getValue()
        assertEquals(e2eServiceModelInfo.trim().replaceAll(" ", ""),
                """{"modelUuid":"f2f5967e-72d3-4c5c-b880-e214e71dba4e"}""")
    }

    @Test
    void testParseCSParamsFromReq() {
        mockData()
        when(mockExecution.getVariable("csServiceModelInfo")).thenReturn("""
            {
                "modelInvariantUuid":"e75698d9-925a-4cdd-a6c0-edacbe6a0b51",
                "modelName":"voLTE",
                "modelNameVersionId":"8ee5926d-720b-4bb2-86f9-d20e921c143b",
                "modelVersion":"1.0",
                "modelVersionId":"8ee5926d-720b-4bb2-86f9-d20e921c143b",
                "modelInvariantId":"e75698d9-925a-4cdd-a6c0-edacbe6a0b51",
                "modelType":"service",
                "modelUuid":"8ee5926d-720b-4bb2-86f9-d20e921c143b"
            }""")

        ServiceDecomposition decomposition = new ServiceDecomposition()
        decomposition.setServiceType("embb")
        ServiceInfo serviceInfo = new ServiceInfo()
        serviceInfo.setServiceProperties("""
            [{"name":"useInterval","type":"string","required":false},
            {"default":"39-00","name":"plmnIdList","type":"string","required":false},
            {"name":"maxNumberofUEs","type":"integer","required":false},
            {"name":"latency","type":"integer","required":false},
            {"name":"uEMobilityLevel","type":"string","required":false},
            {"name":"expDataRateUL","type":"integer","required":false},
            {"name":"expDataRateDL","type":"integer","required":false},
            {"name":"coverageAreaList","type":"string","required":false},
            {"name":"sNSSAI","type":"string","required":false},
            {"name":"resourceSharingLevel","type":"string","required":false}]
        """)
        decomposition.setServiceInfo(serviceInfo)

        when(mockExecution.getVariable("csServiceDecomposition")).thenReturn(decomposition)

        CreateCommunicationService communicationService = new CreateCommunicationService()
        communicationService.parseCSParamsFromReq(mockExecution)

        Mockito.verify(mockExecution, times(1)).setVariable(eq("csInputMap"), captor.capture())
        def csInputMap = captor.getValue()
        assertEquals(getExpectCsInputMap(), csInputMap)

    }

    private static Map<String, Object> getExpectCsInputMap() {
        Map<String, Object> csInputMap = new HashMap<>()
        csInputMap.put("expDataRateDL", 10)
        csInputMap.put("expDataRateUL", 30)
        csInputMap.put("latency", 20)
        csInputMap.put("maxNumberofUEs", 300)
        csInputMap.put("uEMobilityLevel", "stationary")
        csInputMap.put("resourceSharingLevel", "shared")
        csInputMap.put("coverageAreaTAList", "01001")
        csInputMap.put("useInterval", "3")
        csInputMap.put("coverageAreaList", null)
        csInputMap.put("plmnIdList", "39-00")
        csInputMap.put("sNSSAI", null)
        return csInputMap
    }

   @Test
    void testGenerateE2EServiceProfile() {
       ServiceDecomposition decomposition = new ServiceDecomposition()
       decomposition.setServiceType("embb")
       ServiceInfo serviceInfo = new ServiceInfo()
       serviceInfo.setServiceProperties("""
            [{"name":"nstar0_allottedresource0_providing_service_uuid","type":"string","required":true},
            {"name":"areaTrafficCapDL","type":"integer","required":false},
            {"name":"maxNumberofUEs","type":"integer","required":false},
            {"name":"latency","type":"integer","required":false},
            {"name":"expDataRateUL","type":"integer","required":false},
            {"name":"sNSSAI","type":"string","required":false},
            {"name":"plmnIdList","type":"string","required":false},
            {"name":"sST","type":"integer","required":false},
            {"name":"areaTrafficCapUL","type":"integer","required":false},
            {"name":"uEMobilityLevel","type":"string","required":false},
            {"name":"expDataRateDL","type":"integer","required":false},
            {"name":"nstar0_allottedresource0_providing_service_invariant_uuid","type":"string","required":true},
            {"name":"coverageAreaTAList","type":"string","required":false},
            {"name":"activityFactor","type":"integer","required":false},
            {"name":"resourceSharingLevel","type":"string","required":false}]
        """)
       decomposition.setServiceInfo(serviceInfo)

       ModelInfo modelInfo = new ModelInfo()
       modelInfo.setModelInvariantUuid("e75698d9-925a-4cdd-a6c0-edacbe6a0b51")
       modelInfo.setModelUuid("8ee5926d-720b-4bb2-86f9-d20e921c143b")
       decomposition.setModelInfo(modelInfo)

       when(mockExecution.getVariable("e2eServiceDecomposition")).thenReturn(decomposition)
       when(mockExecution.getVariable("csInputMap")).thenReturn(getExpectCsInputMap())

       CreateCommunicationService communicationService = new CreateCommunicationService()
       communicationService.generateE2EServiceProfile(mockExecution)

       Mockito.verify(mockExecution, times(1)).setVariable(eq("e2eInputMap"), captor.capture())
       def csInputMap = captor.getValue()
       assertEquals(csInputMap, getExpectE2eInputMap())
   }

    private static Map<String, Object> getExpectE2eInputMap() {
        Map<String, Object> e2eInputMap = new HashMap<>()
        e2eInputMap.put("nstar0_allottedresource0_providing_service_uuid", null)
        e2eInputMap.put("nstar0_allottedresource0_providing_service_invariant_uuid", null)
        e2eInputMap.put("areaTrafficCapDL", 100)
        e2eInputMap.put("areaTrafficCapUL", 100)
        e2eInputMap.put("expDataRateDL", 10)
        e2eInputMap.put("expDataRateUL", 30)
        e2eInputMap.put("latency", 20)
        e2eInputMap.put("maxNumberofUEs", 300)
        e2eInputMap.put("uEMobilityLevel", "stationary")
        e2eInputMap.put("resourceSharingLevel", "shared")
        e2eInputMap.put("coverageAreaTAList", "01001")
        e2eInputMap.put("sST", null)
        e2eInputMap.put("activityFactor", 0)
        e2eInputMap.put("plmnIdList", "39-00")
        e2eInputMap.put("sNSSAI", null)

        return e2eInputMap
    }

    @Test
    void testPreRequestSend2NSMF() {
        when(mockExecution.getVariable("e2eInputMap")).thenReturn(getExpectE2eInputMap())
        when(mockExecution.getVariable("csServiceName")).thenReturn("testName")
        when(mockExecution.getVariable("modelUuid")).thenReturn("12345")
        when(mockExecution.getVariable("e2eModelInvariantUuid")).thenReturn("54321")
        when(mockExecution.getVariable("e2eModelUuid")).thenReturn("11111")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("test111")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5GConsumer")
        CreateCommunicationService communicationService = new CreateCommunicationService()
        communicationService.preRequestSend2NSMF(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("CSMF_NSMFRequest"), captor.capture())
        String resultSuccess = captor.getValue()

        def expectMap = getExpectE2eInputMap()
        expectMap.put("nstar0_allottedresource0_providing_service_uuid", null)
        expectMap.put("nstar0_allottedresource0_providing_service_invariant_uuid", null)

        String expect = """
                {
                    "service":{
                        "name": "testName",
                        "description": "e2eService of 12345",
                        "serviceInvariantUuid": "54321",
                        "serviceUuid": "11111",
                        "globalSubscriberId": "test111",
                        "serviceType": "5GConsumer",
                        "parameters":{
                            "requestInputs": ${expectMap as JSONObject}
                        }
                    }
                }
            """
        assertEquals(expect.replaceAll("\\s+", ""), resultSuccess.replaceAll("\\s+", ""))
    }

    @Test
    void testProcessNSMFResponseSuccess() {
        when(mockExecution.getVariable("CSMF_NSMFResponseCode")).thenReturn(202)
        when(mockExecution.getVariable("CSMF_NSMFResponse")).thenReturn("""
                {
                    "service": {
                        "serviceId": "945063ff-1a01-4944-9232-8e7999e0d5e4",
                        "operationId": "e3819a23-3777-4172-a834-35ee78acf3f4"
                    }
                }
        """)

        CreateCommunicationService communicationService = new CreateCommunicationService()
        communicationService.processNSMFResponse(mockExecution)
        Mockito.verify(mockExecution, times(3)).setVariable(captor.capture(), captor.capture())
        def resultSuccess = captor.getAllValues()

        def expect = new ArrayList<>()
        expect.add("e2eServiceInstanceId")
        expect.add("945063ff-1a01-4944-9232-8e7999e0d5e4")
        expect.add("e2eOperationId")
        expect.add("e3819a23-3777-4172-a834-35ee78acf3f4")
        expect.add("ProcessNsmfSuccess")
        expect.add("OK")
        assertEquals(expect, resultSuccess)
    }

    @Test
    void testProcessNSMFResponseError() {
        when(mockExecution.getVariable("CSMF_NSMFResponseCode")).thenReturn(500)
        CreateCommunicationService communicationService = new CreateCommunicationService()
        communicationService.processNSMFResponse(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("ProcessNsmfSuccess"), captor.capture())
        String resultSuccess = captor.getValue()
        assertEquals("ERROR", resultSuccess)
    }

    @Test
    void testPrepareUpdateOperationStatus() {
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("operationId")).thenReturn("54321")

        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("11111")
        when(mockExecution.getVariable("mso.adapters.openecomp.db.endpoint"))
                .thenReturn("http://localhost:28090/dbadapters/RequestsDbAdapter")
        CreateCommunicationService communicationService = new CreateCommunicationService()

        communicationService.prepareUpdateOperationStatus(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String res = captor.getValue()

        String expect = getExpectPayload("updateServiceOperationStatus", "processing", "20",
                "communication service create operation processing: waiting nsmf service create finished")

        assertEquals(expect.replaceAll("\\s+", ""), res.replaceAll("\\s+", ""))
    }


    @Test
    void testPrepareCallCheckProcessStatus() {
        CreateCommunicationService communicationService = new CreateCommunicationService()
        communicationService.prepareCallCheckProcessStatus(mockExecution)
        Mockito.verify(mockExecution, times(10)).setVariable(captor.capture(), captor.capture())
        def resultSuccess = captor.getAllValues()
        assertNotNull(resultSuccess)
    }


    @Test
    void testPrepareCompleteStatus() {
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("operationId")).thenReturn("54321")
        when(mockExecution.getVariable("operationContent"))
                .thenReturn("communication service create operation finished")

        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("11111")
        when(mockExecution.getVariable("operationStatus"))
                .thenReturn("processing")
        CreateCommunicationService communicationService = new CreateCommunicationService()

        communicationService.prepareCompleteStatus(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String res = captor.getValue()

        String expect = getExpectPayload("updateServiceOperationStatus", "processing", "100",
                "communication service create operation finished")

        assertEquals(expect.replaceAll("\\s+", ""), res.replaceAll("\\s+", ""))
    }

    private static String getExpectPayload(String type, String result, String progress, String operationContent) {
        String expect =
                """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                            <soapenv:Header/>
                            <soapenv:Body>
                                <ns:${type} xmlns:ns="http://org.onap.so/requestsdb">
                                    <serviceId>12345</serviceId>
                                    <operationId>54321</operationId>
                                    <operationType>CREATE</operationType>
                                    <userId>11111</userId>
                                    <result>${result}</result>
                                    <operationContent>${operationContent}</operationContent>
                                    <progress>${progress}</progress>
                                    <reason></reason>
                                </ns:${type}>
                            </soapenv:Body>
                   </soapenv:Envelope>
                """
        return expect
    }

    private void mockData() {
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
    }
}
