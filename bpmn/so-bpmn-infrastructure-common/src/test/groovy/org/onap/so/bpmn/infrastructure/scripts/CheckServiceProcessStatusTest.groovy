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
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

class CheckServiceProcessStatusTest extends MsoGroovyTest {
    @Before
    void init() throws IOException {
        super.init("CheckServiceProcessStatus")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)


    @Test
    void testPreProcessRequest () {
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("operationId")).thenReturn("54321")

        def successConditions = new ArrayList<>()
        successConditions.add("finished")
        successConditions.add("completed")

        def errorConditions = new ArrayList<>()
        errorConditions.add("error")
        errorConditions.add("failed")

        when(mockExecution.getVariable("successConditions")).thenReturn(successConditions)
        when(mockExecution.getVariable("errorConditions")).thenReturn(errorConditions)


        CheckServiceProcessStatus serviceProcessStatus = new CheckServiceProcessStatus()
        serviceProcessStatus.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution, times(9)).setVariable(captor.capture(), captor.capture())
    }


    @Test
    void testPreCheckServiceStatusReq() {
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("operationId")).thenReturn("54321")

        CheckServiceProcessStatus serviceProcessStatus = new CheckServiceProcessStatus()

        serviceProcessStatus.preCheckServiceStatusReq(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("getOperationStatus"), captor.capture())
        String res = captor.getValue()
        String expect =
                """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                            <soapenv:Header/>
                            <soapenv:Body>
                                <ns:getServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                                    <serviceId>12345</serviceId>
                                    <operationId>54321</operationId>                       
                                </ns:getServiceOperationStatus>
                            </soapenv:Body>
                        </soapenv:Envelope>
                    """
        assertEquals(expect.replaceAll("\\s+", ""), res.replaceAll("\\s+", ""))
    }


    @Test
    void testHandlerServiceStatusRespSuccess() {
        mockData()
        when(mockExecution.getVariable("dbResponseCode")).thenReturn(200)
        when(mockExecution.getVariable("dbResponse")).thenReturn(getDBResponse("finished"))
        def successConditions = new ArrayList<>()
        successConditions.add("finished")
        successConditions.add("completed")

        when(mockExecution.getVariable("successConditions")).thenReturn(successConditions)

        CheckServiceProcessStatus serviceProcessStatus = new CheckServiceProcessStatus()
        serviceProcessStatus.handlerServiceStatusResp(mockExecution)

        Mockito.verify(mockExecution, times(4)).setVariable(captor.capture(), captor.capture())
        def resultSuccess = captor.getAllValues()

        def expect = new ArrayList<>()
        expect.add("operationStatus")
        expect.add("finished")
        expect.add("operationContent")
        expect.add("communication service create operation finished success")
        expect.add("orchestrationStatus")
        expect.add("deactivated")
        expect.add("isAllFinished")
        expect.add("true")

        assertEquals(expect, resultSuccess)
    }


    @Test
    void testHandlerServiceStatusRespError() {
        mockData()
        when(mockExecution.getVariable("dbResponseCode")).thenReturn(200)
        when(mockExecution.getVariable("dbResponse")).thenReturn(getDBResponse("error"))

        def successConditions = new ArrayList<>()
        successConditions.add("finished")
        successConditions.add("completed")

        def errorConditions = new ArrayList<>()
        errorConditions.add("error")
        errorConditions.add("failed")

        when(mockExecution.getVariable("successConditions")).thenReturn(successConditions)
        when(mockExecution.getVariable("errorConditions")).thenReturn(errorConditions)

        CheckServiceProcessStatus serviceProcessStatus = new CheckServiceProcessStatus()
        serviceProcessStatus.handlerServiceStatusResp(mockExecution)

        Mockito.verify(mockExecution, times(4)).setVariable(captor.capture(), captor.capture())
        def resultSuccess = captor.getAllValues()

        def expect = new ArrayList<>()
        expect.add("operationStatus")
        expect.add("error")
        expect.add("operationContent")
        expect.add("communication service create operation finished with error")
        expect.add("orchestrationStatus")
        expect.add("error")
        expect.add("isAllFinished")
        expect.add("true")

        assertEquals(expect, resultSuccess)
    }


    @Test
    void testHandlerServiceStatusRespProcessingNo() {
        mockData()
        when(mockExecution.getVariable("dbResponseCode")).thenReturn(200)
        when(mockExecution.getVariable("dbResponse")).thenReturn(getDBResponse("processing"))
        when(mockExecution.getVariable("progress")).thenReturn(50)

        def successConditions = new ArrayList<>()
        successConditions.add("finished")
        successConditions.add("completed")

        def errorConditions = new ArrayList<>()
        errorConditions.add("error")
        errorConditions.add("failed")

        when(mockExecution.getVariable("successConditions")).thenReturn(successConditions)
        when(mockExecution.getVariable("errorConditions")).thenReturn(errorConditions)

        CheckServiceProcessStatus serviceProcessStatus = new CheckServiceProcessStatus()
        serviceProcessStatus.handlerServiceStatusResp(mockExecution)

        Mockito.verify(mockExecution, times(2)).setVariable(captor.capture(), captor.capture())
        def resultSuccess = captor.getAllValues()

        def expect = new ArrayList<>()
        expect.add("isNeedUpdateDB")
        expect.add("false")
        expect.add("isAllFinished")
        expect.add("false")

        assertEquals(expect as String, resultSuccess as String)
    }


    @Test
    void testHandlerServiceStatusRespProcessingYes() {
        mockData()
        when(mockExecution.getVariable("dbResponseCode")).thenReturn(200)
        when(mockExecution.getVariable("dbResponse")).thenReturn(getDBResponse("processing"))
        when(mockExecution.getVariable("progress")).thenReturn(60)

        def successConditions = new ArrayList<>()
        successConditions.add("finished")
        successConditions.add("completed")

        def errorConditions = new ArrayList<>()
        errorConditions.add("error")
        errorConditions.add("failed")

        when(mockExecution.getVariable("successConditions")).thenReturn(successConditions)
        when(mockExecution.getVariable("errorConditions")).thenReturn(errorConditions)

        CheckServiceProcessStatus serviceProcessStatus = new CheckServiceProcessStatus()
        serviceProcessStatus.handlerServiceStatusResp(mockExecution)

        Mockito.verify(mockExecution, times(3)).setVariable(captor.capture(), captor.capture())
        def resultSuccess = captor.getAllValues()

        def expect = new ArrayList<>()
        expect.add("progress")
        expect.add("50")
        expect.add("isNeedUpdateDB")
        expect.add("true")
        expect.add("isAllFinished")
        expect.add("false")

        assertEquals(expect as String, resultSuccess as String)
    }


    @Test
    void testTimeWaitDelayNo() {
        mockData()
        when(mockExecution.getVariable("startTime")).thenReturn(System.currentTimeMillis())
        CheckServiceProcessStatus serviceProcessStatus = new CheckServiceProcessStatus()

        serviceProcessStatus.timeWaitDelay(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("isTimeOut"), captor.capture())
        def res = captor.getValue()

        assertEquals("NO", res)
    }


    @Test
    void testTimeWaitDelayYes() {
        mockData()
        when(mockExecution.getVariable("startTime")).thenReturn(1000000)
        CheckServiceProcessStatus serviceProcessStatus = new CheckServiceProcessStatus()

        serviceProcessStatus.timeWaitDelay(mockExecution)
        Mockito.verify(mockExecution, times(4)).setVariable(captor.capture(), captor.capture())
        def res = captor.getAllValues()
        def expect = new ArrayList<>()
        expect.add("operationStatus")
        expect.add("error")
        expect.add("operationContent")
        expect.add("communication service create operation finished with timeout")
        expect.add("orchestrationStatus")
        expect.add("error")
        expect.add("isTimeOut")
        expect.add("YES")

        assertEquals(expect, res)
    }


    @Test
    void testPreUpdateOperationProgress() {
        mockData()
        when(mockExecution.getVariable("progress")).thenReturn(50)
        when(mockExecution.getVariable("initProgress")).thenReturn(20)
        when(mockExecution.getVariable("endProgress")).thenReturn(90)
        when(mockExecution.getVariable("operationType")).thenReturn("CREATE")
        when(mockExecution.getVariable("processServiceType")).thenReturn("communication service")
        when(mockExecution.getVariable("parentServiceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("parentOperationId")).thenReturn("54321")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("11111")

        CheckServiceProcessStatus serviceProcessStatus = new CheckServiceProcessStatus()
        serviceProcessStatus.preUpdateOperationProgress(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String res = captor.getValue()

        String expect = getExpectPayload("55",
                "communication service CREATE operation processing 55")

        assertEquals(expect.replaceAll("\\s+", ""), res.replaceAll("\\s+", ""))


    }


    private static String getExpectPayload(String progress, String operationContent) {
        String expect =
                """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                            <soapenv:Header/>
                            <soapenv:Body>
                                <ns:updateServiceOperationStatus xmlns:ns="http://org.onap.so/requestsdb">
                                    <serviceId>12345</serviceId>
                                    <operationId>54321</operationId>
                                    <operationType>CREATE</operationType>
                                    <userId>11111</userId>
                                    <result>processing</result>
                                    <operationContent>${operationContent}</operationContent>
                                    <progress>${progress}</progress>
                                    <reason></reason>
                                </ns:updateServiceOperationStatus>
                            </soapenv:Body>
                   </soapenv:Envelope>
                """
        return expect
    }


    private static String getDBResponse(String result) {
        String response =
                """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                        <soap:Body>
                            <ns2:getServiceOperationStatusResponse xmlns:ns2="http://org.onap.so/requestsdb">
                            <return><operation>CREATE</operation>
                            <operationContent>Prepare service creation</operationContent>
                            <operationId>077995e3-eb32-44ae-b35d-491fc6983a44</operationId>
                            <progress>50</progress>
                            <reason></reason>
                            <result>${result}</result>
                            <serviceId>3324f117-696d-4518-b8b5-b01fcc127a03</serviceId>
                            <userId>5GCustomer</userId>
                            </return></ns2:getServiceOperationStatusResponse>
                        </soap:Body>
                    </soap:Envelope>
                """
        return response
    }


    private mockData() {
        when(mockExecution.getVariable("processServiceType")).thenReturn("communication service")
        when(mockExecution.getVariable("operationType")).thenReturn("create")
    }
}
