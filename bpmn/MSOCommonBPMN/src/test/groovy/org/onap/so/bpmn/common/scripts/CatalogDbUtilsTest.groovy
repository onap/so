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
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.mock.FileUtil

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.class)
class CatalogDbUtilsTest {


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090)

    @Test
    public void testGetResponseFromCatalogDb() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("mso.catalog.db.endpoint")).thenReturn('http://localhost:8090')
		when(mockExecution.getVariable("mso.adapters.db.auth")).thenReturn('757A94191D685FD2092AC1490730A4FC')
		when(mockExecution.getVariable("mso.msoKey")).thenReturn('07a7159d3bf51a0e53be7a8f89699be7')

        stubFor(get(urlMatching(".*/serviceNetworks[?]serviceModelUuid=12345"))
                .willReturn(aResponse()
                .withStatus(200)
                .withBodyFile("catalogDbFiles/DoCreateServiceInstance_request.json")))

        CatalogDbUtils obj = new CatalogDbUtils()
        String str = obj.getResponseFromCatalogDb(mockExecution, "/serviceNetworks?serviceModelUuid=12345")
        String expectedValue =
                FileUtil.readResourceFile("__files/catalogDbFiles/DoCreateServiceInstance_request.json");
        Assert.assertEquals(expectedValue, str)

    }

    private ExecutionEntity setupMock() {

        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("GenericGetService")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("GenericGetService")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("GenericGetService")
        when(mockExecution.getProcessInstanceId()).thenReturn("GenericGetService")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)
        return mockExecution
    }
}
