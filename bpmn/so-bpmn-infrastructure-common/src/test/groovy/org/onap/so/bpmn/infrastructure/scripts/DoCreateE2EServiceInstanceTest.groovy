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
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.utils.XmlComparator

import static org.mockito.Mockito.*
/**
 * @author sushilma
 * @since January 10, 2018
 */
@RunWith(MockitoJUnitRunner.class)
class DoCreateE2EServiceInstanceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090);

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    String expectedServiceInstanceData = """ <service-instance xmlns="http://org.openecomp.aai.inventory/v8">
        <service-instance-id>1234</service-instance-id>
        <service-instance-name>volte-service</service-instance-name>
        <service-type>voLTE type</service-type>
        <service-role>voLTE role</service-role>
        <orchestration-status>Created</orchestration-status>
            <model-invariant-id>c1d4305f-cdbd-4bbe-9069-a2f4978fd89e</model-invariant-id>
            <model-version-id>d4df5c27-98a1-4812-a8aa-c17f055b7a3f</model-version-id>
        </service-instance>"""
    @Test
    public void testPreProcessRequest(){
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceType")).thenReturn("TRANSPORT")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("1234")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("volte-service")
        when(mockExecution.getVariable("uuiRequest")).thenReturn("""{"service":{"serviceDefId":"c1d4305f-cdbd-4bbe-9069-a2f4978fd89e" , "templateId" : "d4df5c27-98a1-4812-a8aa-c17f055b7a3f"}}""")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("/mso/sdncadapter/")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("mso.workflow.default.aai.customer.version")).thenReturn("8")
        DoCreateE2EServiceInstance obj = new DoCreateE2EServiceInstance()
        obj.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution, times(7)).setVariable(captor.capture(), captor.capture())
        XmlComparator.assertXMLEquals(expectedServiceInstanceData, captor.getValue())
    }

    private ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoCreateE2EServiceInstance")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoCreateE2EServiceInstance")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DoCreateE2EServiceInstance")
        when(mockExecution.getProcessInstanceId()).thenReturn("DoCreateE2EServiceInstance")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)
        return mockExecution
    }
}
