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
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.put
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

/**
 * @author sushilma
 * @since January 10, 2018
 */
@RunWith(MockitoJUnitRunner.class)
class DoCreateVFCNetworkServiceInstanceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090)
    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    public void testAddNSRelationship(){
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("nsInstanceId")).thenReturn("NS12345")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("MSO_dev")
        when(mockExecution.getVariable("serviceType")).thenReturn("MSO-dev-service-type")
        when(mockExecution.getVariable("serviceId")).thenReturn("SER12345")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
        MockPutServiceInstance("MSO_dev", "MSO-dev-service-type", "SER12345");
        DoCreateVFCNetworkServiceInstance DoCreateVFCNetworkServiceInstance = new DoCreateVFCNetworkServiceInstance()
        DoCreateVFCNetworkServiceInstance.addNSRelationship(mockExecution);
        verify(mockExecution, times(1)).getVariable("aai.endpoint")
        verify(mockExecution, times(1)).getVariable("mso.msoKey")
        verify(mockExecution, times(1)).getVariable("aai.auth")
    }

    private ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoCreateVFCNetworkServiceInstance")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoCreateVFCNetworkServiceInstance")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DoCreateVFCNetworkServiceInstance")
        when(mockExecution.getProcessInstanceId()).thenReturn("DoCreateVFCNetworkServiceInstance")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)
        return mockExecution
    }

    public static void MockPutServiceInstance(String globalCustId, String subscriptionType, String serviceInstanceId) {
        stubFor(put(urlMatching("/aai/v[0-9]+/business/customers/customer/" + globalCustId + "/service-subscriptions/service-subscription/" + subscriptionType + "/service-instances/service-instance/" + serviceInstanceId+"/relationship-list/relationship"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/xml").withBody("")
               ));
    }
}
