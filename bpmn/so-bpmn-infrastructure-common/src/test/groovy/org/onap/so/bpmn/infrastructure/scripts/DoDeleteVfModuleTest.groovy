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

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*
import jakarta.ws.rs.NotFoundException
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.NetworkPolicies
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.aai.domain.yang.VfModule
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.common.scripts.utils.XmlComparator
import org.onap.so.bpmn.mock.FileUtil

@RunWith(MockitoJUnitRunner.class)
class DoDeleteVfModuleTest extends MsoGroovyTest{

    @Spy
    DoDeleteVfModule doDeleteVfModule

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    public void init() throws IOException {
        super.init("DoDeleteVfModule")
        MockitoAnnotations.initMocks(this);
        when(doDeleteVfModule.getAAIClient()).thenReturn(client)
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
        List fqdnList = new ArrayList()
        fqdnList.add("test")
        when(mockExecution.getVariable("DoDVfMod_contrailNetworkPolicyFqdnList")).thenReturn(fqdnList)
        NetworkPolicies networkPolicies = new NetworkPolicies()
        NetworkPolicy networkPolicy = new NetworkPolicy()
        networkPolicy.setNetworkPolicyId("NP1")
        networkPolicies.getNetworkPolicy().add(networkPolicy)
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicies())
        uri.queryParam("network-policy-fqdn", "test")
        when(client.get(NetworkPolicies.class, uri)).thenReturn(Optional.of(networkPolicies))
        doDeleteVfModule.deleteNetworkPoliciesFromAAI(mockExecution)
        Mockito.verify(mockExecution).setVariable("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode", 200)
    }

    @Test
    void testDeleteNetworkPoliciesFromAAIError() {
        List fqdnList = new ArrayList()
        fqdnList.add("test")
        when(mockExecution.getVariable("DoDVfMod_contrailNetworkPolicyFqdnList")).thenReturn(fqdnList)
        NetworkPolicies networkPolicies = new NetworkPolicies()
        NetworkPolicy networkPolicy = new NetworkPolicy()
        networkPolicy.setNetworkPolicyId("NP1")
        networkPolicies.getNetworkPolicy().add(networkPolicy)
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicies())
        uri.queryParam("network-policy-fqdn", "test")
        when(client.get(NetworkPolicies.class, uri)).thenReturn(Optional.of(networkPolicies))
        AAIResourceUri delUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicy("NP1"))
        doThrow(new NotFoundException(("Not Found !"))).when(client).delete(delUri)
        doDeleteVfModule.deleteNetworkPoliciesFromAAI(mockExecution)
        Mockito.verify(client).delete(delUri)
    }

    @Test
    void testQueryAAIVfModuleForStatus() {
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        when(mockExecution.getVariable("vfModuleId")).thenReturn("module-0")
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("12345").vfModule("module-0"))
        VfModule vfModule = new VfModule()
        vfModule.setOrchestrationStatus("Created")
        when(client.get(VfModule.class, uri)).thenReturn(Optional.of(vfModule))
        doDeleteVfModule.queryAAIVfModuleForStatus(mockExecution)
        Mockito.verify(mockExecution).setVariable("DoDVfMod_queryAAIVfModuleForStatusResponseCode", 200)
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

