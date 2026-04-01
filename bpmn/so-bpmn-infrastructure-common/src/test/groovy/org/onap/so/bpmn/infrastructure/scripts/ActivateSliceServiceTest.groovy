package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.so.beans.nsmf.CustomerInfo
import org.onap.so.beans.nsmf.OperationType
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

class ActivateSliceServiceTest extends MsoGroovyTest {
    @Before
    void init() throws IOException {
        mockExecution = setupMock("ActivateSliceService")
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


        ActivateSliceService service = new ActivateSliceService()
        service.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution, times(7)).setVariable(captor.capture(), captor.capture())
    }

    @Test
    void testPrepareInitOperationStatus() {

        CustomerInfo customerInfo = CustomerInfo.builder()
                .serviceInstanceId("12345")
                .operationId("54321")
                .globalSubscriberId("11111")
                .operationType(OperationType.ACTIVATE)
                .build()
        when(mockExecution.getVariable("customerInfo")).thenReturn(customerInfo)

        ActivateSliceService service = new ActivateSliceService()

        service.prepareInitServiceOperationStatus(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String res = captor.getValue()
        assertNotNull(res)
    }

    @Test
    void testSendSyncResponse() {
        CustomerInfo customerInfo = CustomerInfo.builder()
                .operationId("123456")
                .serviceInstanceId("12345")
                .build()
        when(mockExecution.getVariable("customerInfo")).thenReturn(customerInfo)
        when(mockExecution.getVariable("isAsyncProcess")).thenReturn("true")
        ActivateSliceService service = new ActivateSliceService()
        service.sendSyncResponse(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("sentSyncResponse"), captor.capture())
        def updateVolumeGroupRequest = captor.getValue()
        assertEquals(updateVolumeGroupRequest, true)
    }

    private static getExpectPayload = { String type, String result, String progress, String operationType, String operationContent ->
        String expect =
                """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.onap.so/requestsdb">
                            <soapenv:Header/>
                            <soapenv:Body>
                                <ns:${type} xmlns:ns="http://org.onap.so/requestsdb">
                                    <serviceId>12345</serviceId>
                                    <operationId>54321</operationId>
                                    <operationType>${operationType}</operationType>
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
    void testPrepareCompleteStatus() {
        CustomerInfo customerInfo = CustomerInfo.builder()
                .serviceInstanceId("12345")
                .operationId("54321")
                .globalSubscriberId("11111")
                .operationType(OperationType.ACTIVATE)
                .build()
        when(mockExecution.getVariable("customerInfo")).thenReturn(customerInfo)
        ActivateSliceService service = new ActivateSliceService()

        service.prepareCompletionRequest(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String res = captor.getValue()

        String expect = getExpectPayload("updateServiceOperationStatus", "finished", "100", "activation",
                "action finished success")

        assertEquals(expect.replaceAll("\\s+", ""), res.replaceAll("\\s+", ""))
    }
}
