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
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.springframework.core.env.Environment

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DoUpdateVnfAndModulesTest {

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090);

    @Mock
    Environment mockEnvironment

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    void testQueryAAIVfModule() {
        ExecutionEntity mockExecution = setupMock()


        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.default.aai.generic-vnf.version")).thenReturn("8")

        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        DoUpdateVnfAndModules obj = new DoUpdateVnfAndModules()
        obj.queryAAIVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("DUVAM_queryAAIVfModuleResponseCode", 200)
    }

    @Test
    @Ignore
    void testQueryAAIVfModuleEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.default.aai.generic-vnf.version")).thenReturn("8")

        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        try {
            DoUpdateVnfAndModules obj = new DoUpdateVnfAndModules()
            obj.queryAAIVfModule(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
    }

    private ExecutionEntity setupMock() {
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader()
        urnPropertiesReader.setEnvironment(mockEnvironment)
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.version")).thenReturn("14")
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.namespace")).thenReturn("defaultTestNamespace")
        when(mockEnvironment.getProperty("aai.endpoint")).thenReturn("http://localhost:28090")

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        return mockExecution
    }

    private static void mockData() {
        stubFor(get(urlMatching(".*/aai/v[0-9]+/network/generic-vnfs/generic-vnf/12345[?]depth=1"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("DoUpdateVfModule/getGenericVnfResponse.xml")))
    }

}
