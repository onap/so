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
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

class ActivateCommunicationServiceTest extends MsoGroovyTest  {

    @Before
    void init() throws IOException {
        super.init("ActivateCommunicationService")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest() {

        String req = """
                {
                    "globalSubscriberId": "5GCustomer",
                    "serviceType": "5G",
                    "operationId": "test123"
                }
            """
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(req)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("54321")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("operationType")).thenReturn("activation")

        ActivateCommunicationService service = new ActivateCommunicationService()
        service.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution, times(6)).setVariable(captor.capture(), captor.capture())
    }

    @Test
    void testPrepareInitOperationStatus() {

        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("operationId")).thenReturn("54321")

        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("11111")

        ActivateCommunicationService service = new ActivateCommunicationService()

        service.prepareInitOperationStatus(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String res = captor.getValue()
        assertNotNull(res)
    }


    @Test
    void testSendSyncResponse() {
        when(mockExecution.getVariable("operationId")).thenReturn("123456")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        ActivateCommunicationService service = new ActivateCommunicationService()
        service.sendSyncResponse(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("sentSyncResponse"), captor.capture())
        def updateVolumeGroupRequest = captor.getValue()
        assertEquals(updateVolumeGroupRequest, true)
    }

    @Test
    void testPreRequestSend2NSMF() {
        when(mockExecution.getVariable("e2e_service-instance.service-instance-id")).thenReturn("12333")
        when(mockExecution.getVariable("requestParam")).thenReturn("activate")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("test111")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5GConsumer")
        ActivateCommunicationService service = new ActivateCommunicationService()
        service.preRequestSend2NSMF(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("CSMF_NSMFRequest"), captor.capture())
        String resultSuccess = captor.getValue()

        String expect = """
                {
                    "globalSubscriberId":"test111",
                    "serviceType":"5GConsumer"
                }
            """
        assertEquals(expect.replaceAll("\\s+", ""), resultSuccess.replaceAll("\\s+", ""))
    }

    @Test
    void testProcessNSMFResponseSuccess() {
        when(mockExecution.getVariable("CSMF_NSMFResponseCode")).thenReturn(202)
        when(mockExecution.getVariable("CSMF_NSMFResponse")).thenReturn("""
                {                  
                    "operationId": "e3819a23-3777-4172-a834-35ee78acf3f4"
                }
        """)

        ActivateCommunicationService service = new ActivateCommunicationService()
        service.processNSMFResponse(mockExecution)
        Mockito.verify(mockExecution, times(2)).setVariable(captor.capture(), captor.capture())
        def resultSuccess = captor.getAllValues()

        def expect = new ArrayList<>()
        expect.add("e2eOperationId")
        expect.add("e3819a23-3777-4172-a834-35ee78acf3f4")
        expect.add("ProcessNsmfSuccess")
        expect.add("OK")
        assertEquals(expect, resultSuccess)
    }

    @Test
    void testProcessNSMFResponseError() {
        when(mockExecution.getVariable("CSMF_NSMFResponseCode")).thenReturn(500)
        ActivateCommunicationService service = new ActivateCommunicationService()
        service.processNSMFResponse(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("ProcessNsmfSuccess"), captor.capture())
        String resultSuccess = captor.getValue()
        assertEquals("ERROR", resultSuccess)
    }

    @Test
    void testPrepareUpdateOperationStatus() {
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("operationId")).thenReturn("54321")
        when(mockExecution.getVariable("operationType")).thenReturn("activate")

        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("11111")
        when(mockExecution.getVariable("mso.adapters.openecomp.db.endpoint"))
                .thenReturn("http://localhost:28090/dbadapters/RequestsDbAdapter")
        ActivateCommunicationService service = new ActivateCommunicationService()

        service.prepareUpdateOperationStatus(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String res = captor.getValue()

        String expect = getExpectPayload("updateServiceOperationStatus", "processing", "20",
                "communication service activate operation processing: waiting nsmf service create finished")

        assertEquals(expect.replaceAll("\\s+", ""), res.replaceAll("\\s+", ""))
    }

    private static getExpectPayload = { String type, String result, String progress, String operationContent ->
        String expect =
                """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                            <soapenv:Header/>
                            <soapenv:Body>
                                <ns:${type} xmlns:ns="http://org.onap.so/requestsdb">
                                    <serviceId>12345</serviceId>
                                    <operationId>54321</operationId>
                                    <operationType>activate</operationType>
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

    @Test
    void testPrepareCallCheckProcessStatus() {
        ActivateCommunicationService service = new ActivateCommunicationService()
        service.prepareCallCheckProcessStatus(mockExecution)
        Mockito.verify(mockExecution, times(9)).setVariable(captor.capture(), captor.capture())
        def resultSuccess = captor.getAllValues()
        assertNotNull(resultSuccess)
    }

    @Test
    void testPrepareCompleteStatus() {
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("operationId")).thenReturn("54321")
        when(mockExecution.getVariable("operationType")).thenReturn("activate")
        when(mockExecution.getVariable("operationContent"))
                .thenReturn("communication service activate operation finished")

        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("11111")
        when(mockExecution.getVariable("operationStatus"))
                .thenReturn("deactivated")
        ActivateCommunicationService service = new ActivateCommunicationService()

        service.prepareCompleteStatus(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String res = captor.getValue()

        String expect = getExpectPayload("updateServiceOperationStatus", "deactivated", "100",
                "communication service activate operation finished")

        assertEquals(expect.replaceAll("\\s+", ""), res.replaceAll("\\s+", ""))
    }

}


