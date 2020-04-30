/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.MockitoAnnotations
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInstance
import org.onap.so.bpmn.mock.StubResponseAAI
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri

import static org.mockito.Mockito.*

class DoCreateServiceInstanceTest extends MsoGroovyTest{
    def prefix = "DCRESI_"

    @Rule
    public ExpectedException thrown = ExpectedException.none()

    @Before
    void init() throws IOException {
        super.init("DoCreateServiceInstance")
        MockitoAnnotations.initMocks(this)
    }

    @Test
    void testPreProcessRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("MIS/1604/0026/SW_INTERNET")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("MDTWNJ21")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("testRequestId-1503410089303")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:28080/mso/SDNCAdapterCallbackService")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("MSO_dev")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("MSO-dev-service-type")
        when(mockExecution.getVariable("productFamilyId")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable("sdnc.si.svc.types")).thenReturn("PORT-MIRROR,PPROBES")

        ServiceDecomposition decomposition = new ServiceDecomposition()
        ModelInfo modelInfo = new ModelInfo()
        ServiceInstance instance = new ServiceInstance()
        instance.instanceId = "12345"
        decomposition.modelInfo = modelInfo
        decomposition.serviceInstance = instance

        when(mockExecution.getVariable("serviceDecomposition")).thenReturn(decomposition)
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.default.aai.customer.version")).thenReturn("8")


        DoCreateServiceInstance obj = new DoCreateServiceInstance()
        obj.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", prefix)
        verify(mockExecution).setVariable("sdncCallbackUrl", "http://localhost:28080/mso/SDNCAdapterCallbackService")
    }



    @Test
    void testGetAAICustomerById() {
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("12345")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.DoCreateServiceInstance.aai.version")).thenReturn('8')
        StubResponseAAI.MockGetCustomer("12345", "")
        DoCreateServiceInstance obj = spy(DoCreateServiceInstance.class)
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(isA(AAIResourceUri.class))).thenReturn(true)
        obj.getAAICustomerById(mockExecution)
    }

    @Test
    void testGetAAICustomerById_NoCustFound() {
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("12345")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.DoCreateServiceInstance.aai.version")).thenReturn('8')
        StubResponseAAI.MockGetCustomer("12345", "")
        DoCreateServiceInstance obj = spy(DoCreateServiceInstance.class)
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(isA(AAIResourceUri.class))).thenReturn(false)
        thrown.expect(BpmnError.class)
        obj.getAAICustomerById(mockExecution)
    }

    @Test
    void testGetAAICustomerById_Exception() {
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("12345")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.custom.DoCreateServiceInstance.aai.version")).thenReturn('8')
        StubResponseAAI.MockGetCustomer("12345", "")
        DoCreateServiceInstance obj = spy(DoCreateServiceInstance.class)
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(isA(AAIResourceUri.class))).thenThrow(Exception.class)
        thrown.expect(Exception.class)
        obj.getAAICustomerById(mockExecution)
    }
}
