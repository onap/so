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
package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.so.db.request.beans.OperationStatus
import org.onap.so.db.request.beans.ResourceOperationStatus

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

class RequestDBUtilTest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoDeleteSliceServiceTest")
    }

    @Captor
    static ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class)

        @Test
        void httpPreparationPreservesLegacyIdentifiersAndBusinessData() {
        def helper = RequestDBUtil.forHttp()
        def status = new OperationStatus(serviceId: 'service/%20', operationId: 'operation/%25',
            operation: 'CREATE', userId: 'user', result: 'processing', progress: '0',
            operationContent: '<business>value & more</business>')
        helper.prepareUpdateOperationStatus(mockExecution, status)
        Mockito.verify(mockExecution).setVariable(eq('updateOperationStatusHttp'), captor.capture())
        Map command = captor.value as Map
        assertEquals('service%2F%2520', command.serviceId)
        assertEquals('operation/%25', command.operationId)
        assertEquals('<business>value & more</business>', command.operationContent)
        assertEquals('', command.reason)
        assertFalse(command.toString().contains('Envelope'))

        helper.prepareInitResourceOperationStatus(mockExecution, new ResourceOperationStatus(
            serviceId: 'service/%20', operationId: 'operation', operType: 'CREATE',
            resourceTemplateUUID: 'first::second:'))
        Mockito.verify(mockExecution).setVariable(eq('initResourceOperationStatusHttp'), captor.capture())
        assertEquals('first::second:', (captor.value as Map).resourceTemplateUUIDs)

        helper.prepareUpdateResourceOperationStatus(mockExecution, new ResourceOperationStatus(
            serviceId: 'service/%20', operationId: 'job-operation', resourceTemplateUUID: 'template',
            operType: 'DELETE', resourceInstanceID: 'instance', jobId: 'job', status: 'error', progress: '0'))
        Mockito.verify(mockExecution).setVariable(eq('updateResourceOperationStatusHttp'), captor.capture())
        Map resource = captor.value as Map
        assertEquals('job-operation', resource.operationId)
        assertEquals('job', resource.jobId)
        assertEquals('', resource.errorCode)
        assertEquals('', resource.statusDescription)

        helper.getOperationStatus(mockExecution, 'service/%20', 'operation/%25')
        Mockito.verify(mockExecution).setVariable(eq('getOperationStatusHttp'), captor.capture())
        assertEquals([serviceId: 'service%2F%2520', operationId: 'operation%2F%2525'], captor.value)
        Mockito.verify(mockExecution, Mockito.never()).setVariable(eq('dbAdapterEndpoint'), Mockito.any())
        }

    @Test
    void httpCommandsMatchLegacyPayloadsWithoutMutatingInputs() {
        ['service%id/part', 'service%25id%2Fpart'].each { serviceId ->
            def scenarios = [
                ['prepareUpdateOperationStatus', 'updateOperationStatus', OperationStatus,
                    [serviceId: serviceId, operationId: 'operation%id/part', operation: 'UPDATE']],
                ['prepareInitResourceOperationStatus', 'initResourceOperationStatus', ResourceOperationStatus,
                    [serviceId: serviceId, operationId: 'operation%id/part', operType: 'CREATE',
                        resourceTemplateUUID: 'resource%id/part::']],
                ['prepareUpdateResourceOperationStatus', 'updateResourceOperationStatus', ResourceOperationStatus,
                    [serviceId: serviceId, operationId: 'operation%id/part', operType: 'UPDATE',
                        resourceTemplateUUID: 'resource%id/part']]]
            scenarios.each { scenario ->
                def legacy = Mockito.mock(org.camunda.bpm.engine.delegate.DelegateExecution)
                def current = Mockito.mock(org.camunda.bpm.engine.delegate.DelegateExecution)
                def legacyStatus = scenario[2].newInstance(scenario[3])
                def currentStatus = scenario[2].newInstance(scenario[3])
                new RequestDBUtil().invokeMethod(scenario[0], [legacy, legacyStatus] as Object[])
                RequestDBUtil.forHttp().invokeMethod(scenario[0], [current, currentStatus] as Object[])
                def legacyCapture = ArgumentCaptor.forClass(Object)
                def currentCapture = ArgumentCaptor.forClass(Object)
                Mockito.verify(legacy).setVariable(eq(scenario[1]), legacyCapture.capture())
                Mockito.verify(current).setVariable(eq(scenario[1] + 'Http'), currentCapture.capture())
                Map expected = soapFields(legacyCapture.value as String)
                if (scenario[1] == 'initResourceOperationStatus') {
                    expected.operationType = expected.remove('operType')
                }
                assertEquals(scenario[0], expected, currentCapture.value)
                assertEquals(serviceId, legacyStatus.serviceId)
                assertEquals(legacyStatus.serviceId, currentStatus.serviceId)
                assertSerialized(currentCapture.value as Map)
            }
            def legacy = Mockito.mock(org.camunda.bpm.engine.delegate.DelegateExecution)
            def current = Mockito.mock(org.camunda.bpm.engine.delegate.DelegateExecution)
            new RequestDBUtil().getOperationStatus(legacy, serviceId, 'operation%id/part')
            RequestDBUtil.forHttp().getOperationStatus(current, serviceId, 'operation%id/part')
            def legacyCapture = ArgumentCaptor.forClass(Object)
            def currentCapture = ArgumentCaptor.forClass(Object)
            Mockito.verify(legacy).setVariable(eq('getOperationStatus'), legacyCapture.capture())
            Mockito.verify(current).setVariable(eq('getOperationStatusHttp'), currentCapture.capture())
            assertEquals(soapFields(legacyCapture.value as String), currentCapture.value)
            assertSerialized(currentCapture.value as Map)
        }
    }

    private Map soapFields(String payload) {
        new XmlSlurper().parseText(payload).Body.children()[0].children()
            .collectEntries { [(it.name()): it.text()] }
    }

    private void assertSerialized(Map command) {
        def bytes = new ByteArrayOutputStream()
        new ObjectOutputStream(bytes).withCloseable { it.writeObject(command) }
        new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).withCloseable {
            assertEquals(command, it.readObject())
        }
    }

    @Test
        void httpPreparationRetainsLegacyNullTextAndEmptyDefaults() {
        def helper = RequestDBUtil.forHttp()
        helper.prepareUpdateOperationStatus(mockExecution,
            new OperationStatus(serviceId: 'service', operationId: 'operation'))
        Mockito.verify(mockExecution).setVariable(eq('updateOperationStatusHttp'), captor.capture())
        Map command = captor.value as Map
        assertEquals('null', command.operationType)
        assertEquals('null', command.userId)
        assertEquals('null', command.result)
        assertEquals('0', command.progress)
        assertEquals('', command.operationContent)
        assertEquals('', command.reason)
        helper.prepareInitResourceOperationStatus(mockExecution,
            new ResourceOperationStatus(serviceId: 'service', operationId: 'operation'))
        Mockito.verify(mockExecution).setVariable(eq('initResourceOperationStatusHttp'), captor.capture())
        assertEquals('null', (captor.value as Map).resourceTemplateUUIDs)
        def legacy = new RequestDBUtil()
        legacy.prepareInitResourceOperationStatus(mockExecution,
            new ResourceOperationStatus(serviceId: 'service', operationId: 'operation'))
        Mockito.verify(mockExecution).setVariable(eq('initResourceOperationStatus'), captor.capture())
        assertEquals(true, (captor.value as String).contains('<resourceTemplateUUIDs>null</resourceTemplateUUIDs>'))
        }

        @Test
    void prepareUpdateOperationStatus(){
        when(mockExecution.getVariable("mso.adapters.openecomp.db.endpoint")).thenReturn("http://sdc-wfd-be:8080")
        OperationStatus operationStatus = new OperationStatus()
        operationStatus.setServiceId("testServiceId")
        operationStatus.setOperationId("testOperationId")
        operationStatus.setUserId("testUserId")
        operationStatus.setOperation("testOpertation")
        operationStatus.setResult("testResult")
        operationStatus.setProgress("testProgress")
        operationStatus.setOperationContent("testOperationContent")
        operationStatus.setReason("testReason")

        RequestDBUtil requestDBUtil = new RequestDBUtil()
        requestDBUtil.prepareUpdateOperationStatus(mockExecution, operationStatus)

        Mockito.verify(mockExecution,times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String updateOperationStatus = captor.getAllValues()
        assertNotNull(updateOperationStatus)
    }

    @Test
    void testGetOperationStatus(){
        when(mockExecution.getVariable("mso.adapters.openecomp.db.endpoint")).thenReturn("http://sdc-wfd-be:8080")

        RequestDBUtil requestDBUtil = new RequestDBUtil()
        requestDBUtil.getOperationStatus(mockExecution, "testServiceId","testOperationId")

        Mockito.verify(mockExecution,times(1)).setVariable(eq("getOperationStatus"), captor.capture())
        String getOperationStatus = captor.getAllValues()
        assertNotNull(getOperationStatus)

    }
}
