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
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.NetworkPolicies
import org.onap.aai.domain.yang.NetworkPolicy
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.core.WorkflowException
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DoDeleteVfModuleFromVnfTest extends MsoGroovyTest {

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Spy
    DoDeleteVfModuleFromVnf deleteVfModuleFromVnf

    @Before
    public void init() throws IOException {
        super.init("DoDeleteVfModuleFromVnf")
        MockitoAnnotations.initMocks(this)
        when(deleteVfModuleFromVnf.getAAIClient()).thenReturn(client)
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

        List fqdnList = new ArrayList()
        fqdnList.add("test")
        when(mockExecution.getVariable("DDVFMV_contrailNetworkPolicyFqdnList")).thenReturn(fqdnList)
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicies())
        uri.queryParam("network-policy-fqdn", "test")
        NetworkPolicies networkPolicies = new NetworkPolicies();
        NetworkPolicy networkPolicy = new NetworkPolicy();
        networkPolicy.setNetworkPolicyId("NP1")
        networkPolicies.getNetworkPolicy().add(networkPolicy)
        when(client.get(NetworkPolicies.class, uri)).thenReturn(Optional.of(networkPolicies))

        AAIResourceUri delUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicy(networkPolicy.getNetworkPolicyId()))
        doNothing().when(client).delete(delUri)
        deleteVfModuleFromVnf.deleteNetworkPoliciesFromAAI(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", 'DDVFMV_')
        Mockito.verify(mockExecution).setVariable("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode", 200)
    }

    @Test
    void testDeleteNetworkPoliciesFromAAINotFound() {

        List fqdnList = new ArrayList()
        fqdnList.add("test")
        when(mockExecution.getVariable("DDVFMV_contrailNetworkPolicyFqdnList")).thenReturn(fqdnList)
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().networkPolicies())
        uri.queryParam("network-policy-fqdn", "test")
        when(client.get(NetworkPolicies.class, uri)).thenReturn(Optional.empty())
        deleteVfModuleFromVnf.deleteNetworkPoliciesFromAAI(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", 'DDVFMV_')
        Mockito.verify(mockExecution).setVariable("DCVFM_aaiQueryNetworkPolicyByFqdnReturnCode", 404)
    }


    @Test
    void testQueryAAIForVfModule() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("12345")).depth(Depth.ONE)
        GenericVnf genericVnf = new GenericVnf()
        genericVnf.setVnfId("test1")
        when(client.get(GenericVnf.class, uri)).thenReturn(Optional.of(genericVnf))
        deleteVfModuleFromVnf.queryAAIForVfModule(mockExecution)

        Mockito.verify(mockExecution, atLeastOnce()).setVariable("DDVMFV_getVnfResponseCode", 200)
        Mockito.verify(mockExecution, atLeastOnce()).setVariable("DDVMFV_getVnfResponse", genericVnf)
    }

    @Test
    void testQueryAAIForVfModuleNotFound() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("vnfId")).thenReturn("12345")
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("12345")).depth(Depth.ONE)
        when(client.get(GenericVnf.class, uri)).thenReturn(Optional.empty())
        deleteVfModuleFromVnf.queryAAIForVfModule(mockExecution)
        Mockito.verify(mockExecution, atLeastOnce()).setVariable("DDVMFV_getVnfResponseCode", 404)
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
