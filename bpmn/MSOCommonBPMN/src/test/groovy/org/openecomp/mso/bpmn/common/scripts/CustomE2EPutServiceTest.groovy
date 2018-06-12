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

package org.openecomp.mso.bpmn.common.scripts

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
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutServiceInstance

/**
 * @author sushilma
 * @since January 10, 2018
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
class CustomE2EPutServiceTest extends MsoGroovyTest  {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090)

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    public void testPutServiceInstance(){
        ExecutionEntity mockExecution = setupMockWithPrefix('CustomE2EPutService','GENPS_')
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn('8')
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.customer.uri")).thenReturn('/aai/v8/business/customers/customer')
        when(mockExecution.getVariable("GENPS_type")).thenReturn('service-instance')
        when(mockExecution.getVariable("GENPS_payload")).thenReturn('')
        when(mockExecution.getVariable("GENPS_globalSubscriberId")).thenReturn("1604-MVM-26")
        when(mockExecution.getVariable("GENPS_serviceType")).thenReturn("SDN-ETHERNET-INTERNET")
        when(mockExecution.getVariable("GENPS_serviceInstanceId")).thenReturn( "1234")
        MockPutServiceInstance("1604-MVM-26", "SDN-ETHERNET-INTERNET", "1234", "GenericFlows/getServiceInstance.xml");

        CustomE2EPutService customE2EPutService = new CustomE2EPutService()
        customE2EPutService.putServiceInstance(mockExecution);
        Mockito.verify(mockExecution, times(6)).setVariable(captor.capture(), captor.capture())
        Assert.assertEquals(200, captor.getAllValues().get(7))
    }
}
