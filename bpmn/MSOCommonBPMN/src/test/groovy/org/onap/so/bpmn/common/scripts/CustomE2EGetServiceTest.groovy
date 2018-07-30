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

package org.onap.so.bpmn.common.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner

import static org.mockito.Mockito.times
import static org.mockito.Mockito.when
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetServiceInstance
import static org.onap.so.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById
import static org.onap.so.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceByName

/**
 * @author sushilma
 * @since January 10, 2018
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
class CustomE2EGetServiceTest extends MsoGroovyTest  {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090)

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    public void testObtainServiceInstanceUrlById (){
        ExecutionEntity mockExecution = setupMockWithPrefix('CustomE2EGetService','GENGS_')
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.nodes-query.uri")).thenReturn('/aai/v8/search/nodes-query')
        when(mockExecution.getVariable("GENGS_type")).thenReturn('service-instance')
        when(mockExecution.getVariable("GENGS_serviceInstanceId")).thenReturn("MIS%2F1604%2F0026%2FSW_INTERNET")
        MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlById.xml");
        CustomE2EGetService customE2EGetService = new CustomE2EGetService();
        customE2EGetService.obtainServiceInstanceUrlById(mockExecution)
        Mockito.verify(mockExecution, times(9)).setVariable(captor.capture(), captor.capture())
        Assert.assertEquals(200, captor.getAllValues().get(5))
    }

    @Test
    public void testObtainServiceInstanceUrlByName (){
        ExecutionEntity mockExecution = setupMockWithPrefix('CustomE2EGetService','GENGS_')
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.nodes-query.uri")).thenReturn('/aai/v8/search/nodes-query')
        when(mockExecution.getVariable("GENGS_type")).thenReturn('service-instance')
        when(mockExecution.getVariable("GENGS_serviceInstanceName")).thenReturn("1604-MVM-26")
        MockNodeQueryServiceInstanceByName("1604-MVM-26", "GenericFlows/getSIUrlByName.xml");
        CustomE2EGetService customE2EGetService = new CustomE2EGetService();
        customE2EGetService.obtainServiceInstanceUrlByName(mockExecution)
        Mockito.verify(mockExecution, times(8)).setVariable(captor.capture(), captor.capture())
        Assert.assertEquals(200, captor.getAllValues().get(5))
    }

    @Test
    public void testServiceObject (){
        ExecutionEntity mockExecution = setupMockWithPrefix('CustomE2EGetService','GENGS_')
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.nodes-query.uri")).thenReturn('/aai/v8/search/nodes-query')
        when(mockExecution.getVariable("GENGS_type")).thenReturn('service-instance')
        when(mockExecution.getVariable("GENGS_serviceInstanceId")).thenReturn("MIS%2F1604%2F0026%2FSW_INTERNET")
        when(mockExecution.getVariable("GENGS_resourceLink")).thenReturn("/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/1234453")
        MockGetServiceInstance("MSO_1610_dev", "MSO-dev-service-type", "1234453", "GenericFlows/getServiceInstance.xml");
        CustomE2EGetService customE2EGetService = new CustomE2EGetService();
        customE2EGetService.getServiceObject(mockExecution)
        Mockito.verify(mockExecution, times(7)).setVariable(captor.capture(), captor.capture())
        Assert.assertEquals(200, captor.getAllValues().get(5))
    }
}
