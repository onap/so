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
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
public class DoDeleteVnfAndModulesTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090);

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    String cloudConfiguration = "{ " +
            "\"lcpCloudRegionId\": \"mdt1\"," +
            "\"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\"" + "}";

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPreProcessRequestTest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("cloudConfiguration")).thenReturn(cloudConfiguration)
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("serviceInstanceId")
        when(mockExecution.getVariable("sdncVersion")).thenReturn("1702")
        when(mockExecution.getVariable("productFamilyId")).thenReturn("productFamilyId")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("mdt1")
        when(mockExecution.getVariable("tenantId")).thenReturn("19123c2924c648eb8e42a3c1f14b7682")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("MSO_test")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")

        DoDeleteVnfAndModules obj = new DoDeleteVnfAndModules()
        obj.preProcessRequest(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", "DDVAM_")
        Mockito.verify(mockExecution).setVariable("requestId", "12345")
        Mockito.verify(mockExecution).setVariable("mso-request-id", "12345")
        Mockito.verify(mockExecution).setVariable("DDVAM_source", "VID")
        Mockito.verify(mockExecution).setVariable("DDVAM_isVidRequest", "true")
        Mockito.verify(mockExecution).setVariable("DDVAM_sdncVersion", "1702")
        Mockito.verify(mockExecution).setVariable("DDVAM_isVidRequest", "true")
        Mockito.verify(mockExecution).setVariable("sdncCallbackUrl", "http://localhost:8090/SDNCAdapterCallback")
    }

  

    @Test
    public void testQueryAAIVfModuleNullEndPoint() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.DoDeleteVnfAndModules.aai.generic-vnf.uri")).thenReturn("/aai/v8/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn(null)
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        try {
            DoDeleteVnfAndModules obj = new DoDeleteVnfAndModules()
            obj.queryAAIVfModule(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        Mockito.verify(mockExecution,atLeastOnce()).setVariable(captor.capture(),captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals("AAI GET Failed:null", workflowException.getErrorMessage())
        Assert.assertEquals(1002, workflowException.getErrorCode())
    }

    @Test
    public void testQueryAAIVfModule() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.DoDeleteVnfAndModules.aai.generic-vnf.uri")).thenReturn("/aai/v8/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        DoDeleteVnfAndModules obj = new DoDeleteVnfAndModules()
        obj.queryAAIVfModule(mockExecution)

        Mockito.verify(mockExecution).setVariable("DCVFM_queryAAIVfModuleResponseCode", 200)
    }

    private ExecutionEntity setupMock() {

        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoDeleteVnfAndModules")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoDeleteVnfAndModules")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables

        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DoDeleteVnfAndModules")
        when(mockExecution.getProcessInstanceId()).thenReturn("DoDeleteVnfAndModules")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution

    }

    private void mockData() {

        stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/12345[?]depth=1"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBodyFile("GenericFlows/getGenericVnfResponse_hasRelationships.xml")));
    }
}
