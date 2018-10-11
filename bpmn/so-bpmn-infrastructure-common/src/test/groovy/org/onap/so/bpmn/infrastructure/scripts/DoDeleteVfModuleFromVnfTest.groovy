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
class DoDeleteVfModuleFromVnfTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090);

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPreProcessRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("source")).thenReturn("VID")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        when(mockExecution.getVariable("tenantId")).thenReturn("19123c2924c648eb8e42a3c1f14b7682")
        when(mockExecution.getVariable("vfModuleId")).thenReturn("12345")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("12345")
        when(mockExecution.getVariable("sdncVersion")).thenReturn("8")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")

        DoDeleteVfModuleFromVnf obj = new DoDeleteVfModuleFromVnf()
        obj.preProcessRequest(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", "DDVFMV_")
        Mockito.verify(mockExecution).setVariable("DDVFMV_contrailNetworkPolicyFqdnList", null)
        Mockito.verify(mockExecution).setVariable("mso-request-id", "12345")
        Mockito.verify(mockExecution).setVariable("requestId", "12345")
        Mockito.verify(mockExecution).setVariable("cloudSiteId", "12345")
        Mockito.verify(mockExecution).setVariable("source", "VID")
        Mockito.verify(mockExecution).setVariable("isVidRequest", "true")
        Mockito.verify(mockExecution).setVariable("srvInstId", "")
        Mockito.verify(mockExecution).setVariable("DDVFMV_serviceInstanceIdToSdnc", "12345")
        Mockito.verify(mockExecution).setVariable("DDVFMV_sdncVersion", "8")
        Mockito.verify(mockExecution).setVariable("sdncCallbackUrl", "http://localhost:8090/SDNCAdapterCallback")
    }



    @Test
    void testDeleteNetworkPoliciesFromAAI() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso.workflow.DoDeleteVfModuleFromVnf.aai.network-policy.uri")).thenReturn("/aai/v8/network/network-policies/network-policy")
        when(mockExecution.getVariable("mso.workflow.custom.DoDeleteVfModuleFromVnf.aai.version")).thenReturn("8")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        List fqdnList = new ArrayList()
        fqdnList.add("test")
        when(mockExecution.getVariable("DDVFMV_contrailNetworkPolicyFqdnList")).thenReturn(fqdnList)
        mockData()
        DoDeleteVfModuleFromVnf obj = new DoDeleteVfModuleFromVnf()
        obj.deleteNetworkPoliciesFromAAI(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", 'DDVFMV_')
        Mockito.verify(mockExecution).setVariable("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode", 200)
    }


    @Test
    void testQueryAAIForVfModule() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("mso.workflow.global.default.aai.namespace")
        when(mockExecution.getVariable("mso.workflow.default.aai.generic-vnf.version")).thenReturn("8")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

        mockData()
        DoDeleteVfModuleFromVnf obj = new DoDeleteVfModuleFromVnf()
        obj.queryAAIForVfModule(mockExecution)

        Mockito.verify(mockExecution, atLeastOnce()).setVariable("DDVMFV_getVnfResponseCode", 200)
    }



    private ExecutionEntity setupMock() {

        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoDeleteVfModuleFromVnf")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoDeleteVfModuleFromVnf")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables

        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DoDeleteVfModuleFromVnf")
        when(mockExecution.getProcessInstanceId()).thenReturn("DoDeleteVfModuleFromVnf")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution

    }

    private static void mockData() {
        stubFor(get(urlMatching(".*/aai/v[0-9]+/network/generic-vnfs/generic-vnf/12345[?]depth=1"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("VfModularity/GenerateVfModuleName_AAIResponse_Success.xml")))

        stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy\\?network-policy-fqdn=.*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBodyFile("VfModularity/QueryNetworkPolicy_AAIResponse_Success.xml")))

        stubFor(delete(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/.*"))
                .willReturn(aResponse()
                .withStatus(200)));

    }
}
