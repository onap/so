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

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import static com.shazam.shazamcrest.MatcherAssert.assertThat
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

/**
 * @author sushilma
 * @since January 10, 2018
 */
class DoUpdateE2EServiceInstanceTest extends MsoGroovyTest{


    @Before
    public void init() {
        super.init("DoUpdateE2EServiceInstance")
    }
    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)
    @Test
    public void testPreProcessRequest(){
        mockData()
        ServiceInstance expectedServiceInstanceData = getExpectedServiceInstance()
        DoUpdateE2EServiceInstance serviceInstance = new DoUpdateE2EServiceInstance()
        serviceInstance.preProcessAAIPUT(mockExecution)
        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        assertThat(captor.getValue(), sameBeanAs(expectedServiceInstanceData))
    }

    private ServiceInstance getExpectedServiceInstance() {
        ServiceInstance expectedServiceInstanceData = new ServiceInstance()
        expectedServiceInstanceData.setServiceInstanceId("1234")
        expectedServiceInstanceData.setServiceInstanceName("volte-service")
        expectedServiceInstanceData.setServiceType("E2E Service")
        expectedServiceInstanceData.setServiceRole("E2E Service")
        return expectedServiceInstanceData
    }

    private void mockData() {
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceType")).thenReturn("TRANSPORT")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("1234")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("volte-service")
        when(mockExecution.getVariable("uuiRequest")).thenReturn("""{"service":{"serviceDefId":"c1d4305f-cdbd-4bbe-9069-a2f4978fd89e" , "templateId" : "d4df5c27-98a1-4812-a8aa-c17f055b7a3f"}}""")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("/mso/sdncadapter/")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.default.aai.customer.version")).thenReturn("8")
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.customer.uri")).thenReturn('/aai/v8/business/customers/customer')
        when(mockExecution.getVariable("URN_mso_workflow_sdncadapter_callback")).thenReturn('/testUrl')
    }

}
