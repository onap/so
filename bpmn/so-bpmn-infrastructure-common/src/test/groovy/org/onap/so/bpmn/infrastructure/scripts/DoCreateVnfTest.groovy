/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.core.domain.VnfResource

import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DoCreateVnfTest {
    def prefix = "DoCVNF_"

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090)

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testPreProcessRequest() {
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("MIS/1604/0026/SW_INTERNET")
        when(mockExecution.getVariable("vnfType")).thenReturn("vRRaas")
        when(mockExecution.getVariable("vnfName")).thenReturn("skask-test")

        when(mockExecution.getVariable("productFamilyId")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("MDTWNJ21")

        when(mockExecution.getVariable("vnfResourceDecomposition")).thenReturn(new VnfResource())
        when(mockExecution.getVariable("mso-request-id")).thenReturn("testRequestId-1503410089303")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).
                thenReturn("http://localhost:28080/mso/SDNCAdapterCallbackService")



        DoCreateVnf obj = new DoCreateVnf()
        obj.preProcessRequest(mockExecution)

        Mockito.verify(mockExecution, times(31)).setVariable(captor.capture(), captor.capture())
        List list = captor.getAllValues()
        String str = list.get(51)
        Assert.assertEquals("http://localhost:28080/mso/SDNCAdapterCallbackService", str)
    }
}
