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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.utils.XmlComparator
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DoDeleteVfModuleTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090);

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPrepSDNCAdapterRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("testReqId")).thenReturn("testReqId")
        when(mockExecution.getVariable("requestId")).thenReturn("12345")
        when(mockExecution.getVariable("source")).thenReturn("VID")
        when(mockExecution.getVariable("serviceId")).thenReturn("12345")
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        when(mockExecution.getVariable("tenantId")).thenReturn("19123c2924c648eb8e42a3c1f14b7682")
        when(mockExecution.getVariable("vfModuleId")).thenReturn("12345")
        when(mockExecution.getVariable("DoDVfMod_serviceInstanceIdToSdnc")).thenReturn("123456789")
        when(mockExecution.getVariable("vfModuleName")).thenReturn("vfModuleName_test")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")

        DoDeleteVfModule obj = new DoDeleteVfModule()
        obj.prepSDNCAdapterRequest(mockExecution, 'release')

        String expectedValue = FileUtil.readResourceFile("__files/DoDeleteVfModule/sdncAdapterWorkflowRequest.xml")
        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        XmlComparator.assertXMLEquals(expectedValue, captor.getValue())
    }
   

    @Test
    void testDeleteNetworkPoliciesFromAAI() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso.workflow.DoDeleteVfModule.aai.network-policy.uri")).thenReturn("/aai/v8/network/network-policies/network-policy")
        when(mockExecution.getVariable("mso.workflow.custom.DoDeleteVfModule.aai.version")).thenReturn("8")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        List fqdnList = new ArrayList()
        fqdnList.add("test")
        when(mockExecution.getVariable("DoDVfMod_contrailNetworkPolicyFqdnList")).thenReturn(fqdnList)
        mockData()
        DoDeleteVfModule obj = new DoDeleteVfModule()
        obj.deleteNetworkPoliciesFromAAI(mockExecution)

        Mockito.verify(mockExecution).setVariable("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode", 200)
    }


    @Test
    void testQueryAAIVfModuleForStatus() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("DCVFM_vnfId")).thenReturn("12345")
        when(mockExecution.getVariable("DCVFM_vfModuleName")).thenReturn("module-0")
        when(mockExecution.getVariable("mso.workflow.DoDeleteVfModule.aai.generic-vnf.uri")).thenReturn("/aai/v9/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        DoCreateVfModule obj = new DoCreateVfModule()
        obj.queryAAIVfModuleForStatus(mockExecution)

        Mockito.verify(mockExecution).setVariable("DCVFM_queryAAIVfModuleForStatusResponseCode", 200)
    }

  

    private ExecutionEntity setupMock() {

        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoDeleteVfModule")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoDeleteVfModule")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables

        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DoDeleteVfModule")
        when(mockExecution.getProcessInstanceId()).thenReturn("DoDeleteVfModule")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution

    }

    private static void mockData() {
        stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy\\?network-policy-fqdn=.*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/xml")
                .withBodyFile("VfModularity/QueryNetworkPolicy_AAIResponse_Success.xml")))

        stubFor(delete(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/.*"))
                .willReturn(aResponse()
                .withStatus(200)))

        stubFor(get(urlMatching(".*/aai/v[0-9]+/network/generic-vnfs/generic-vnf/12345/vf-modules/vf-module[?]vf-module-name=module-0"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("DoDeleteVfModule/getGenericVnfResponse.xml")))

    }
}

