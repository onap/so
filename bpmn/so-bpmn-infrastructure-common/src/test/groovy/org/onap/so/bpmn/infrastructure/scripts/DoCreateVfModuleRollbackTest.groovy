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
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DoCreateVfModuleRollbackTest {

    def prefix = "DCVFMR_"

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090)

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testPrepSDNCAdapterRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("testReqId")).thenReturn("testReqId")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:28080/mso/SDNCAdapterCallbackService")
        when(mockExecution.getVariable(prefix + "source")).thenReturn("VID")
        when(mockExecution.getVariable(prefix + "tenantId")).thenReturn("fba1bd1e195a404cacb9ce17a9b2b421")
        when(mockExecution.getVariable(prefix + "vnfType")).thenReturn("vRRaas")
        when(mockExecution.getVariable(prefix + "vnfName")).thenReturn("skask-test")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleModelName")).thenReturn("PCRF::module-0-2")
        when(mockExecution.getVariable(prefix + "requestId")).thenReturn("testRequestId")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("cb510af0-5b21-4bc7-86d9-323cb396ce32")
        when(mockExecution.getVariable(prefix + "vfModuleName")).thenReturn("PCRF::module-0-2")
        when(mockExecution.getVariable(prefix + "serviceId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "cloudSiteId")).thenReturn("RDM2WAGPLCP")

        when(mockExecution.getVariable(prefix + "rollbackSDNCRequestActivate")).thenReturn("true")


        DoCreateVfModuleRollback obj = new DoCreateVfModuleRollback()
        obj.prepSDNCAdapterRequest(mockExecution)

        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        String expectedValue = FileUtil.readResourceFile("__files/DoCreateVfModuleRollback/sdncAdapterWorkflowRequest.xml")
        XmlComparator.assertXMLEquals(expectedValue, captor.getValue())
    }

    

    @Test
    void testBuildSDNCRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("testReqId")).thenReturn("testReqId")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:28080/mso/SDNCAdapterCallbackService")
        when(mockExecution.getVariable(prefix + "source")).thenReturn("VID")
        when(mockExecution.getVariable(prefix + "vnfType")).thenReturn("vRRaas")
        when(mockExecution.getVariable(prefix + "vnfName")).thenReturn("skask-test")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "requestId")).thenReturn("testRequestId")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("cb510af0-5b21-4bc7-86d9-323cb396ce32")
        when(mockExecution.getVariable(prefix + "vfModuleName")).thenReturn("PCRF::module-0-2")
        when(mockExecution.getVariable(prefix + "serviceId")).thenReturn("12345")

        when(mockExecution.getVariable(prefix + "rollbackSDNCRequestActivate")).thenReturn("true")


        DoCreateVfModuleRollback obj = new DoCreateVfModuleRollback()
        String sdncRequest = obj.buildSDNCRequest(mockExecution, "svcInstId_test", "deactivate")
        String expectedValue = FileUtil.readResourceFile("__files/DoCreateVfModuleRollback/deactivateSDNCRequest.xml")
        XmlComparator.assertXMLEquals(expectedValue, sdncRequest)
    }

   

    @Test
    void testPrepVNFAdapterRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("testReqId")).thenReturn("testReqId")
        when(mockExecution.getVariable("mso.use.qualified.host")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "requestId")).thenReturn("testRequestId")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("cb510af0-5b21-4bc7-86d9-323cb396ce32")
        when(mockExecution.getVariable(prefix + "mso-request-id")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.message.endpoint")).thenReturn('http://localhost:18080/mso/WorkflowMessage/')


        DoCreateVfModuleRollback obj = new DoCreateVfModuleRollback()
        String sdncRequest = obj.prepVNFAdapterRequest(mockExecution)

        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        String expectedValue = FileUtil.readResourceFile("__files/DoCreateVfModuleRollback/vnfAdapterRestV1Request.xml")
        XmlComparator.assertXMLEquals(expectedValue, captor.getValue(), "messageId", "notificationUrl")
    }

    @Test
    void testDeleteNetworkPoliciesFromAAI() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("DCVFM_cloudSiteId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.DoCreateVfModuleRollback.aai.network-policy.uri")).thenReturn("/aai/v8/network/network-policies/network-policy")
        when(mockExecution.getVariable("mso.workflow.custom.DoCreateVfModuleRollback.aai.version")).thenReturn("8")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("rollbackData")).thenReturn(new RollbackData())
        List fqdnList = new ArrayList()
        fqdnList.add("test")
        when(mockExecution.getVariable(prefix + "createdNetworkPolicyFqdnList")).thenReturn(fqdnList)
        mockData()
        DoCreateVfModuleRollback obj = new DoCreateVfModuleRollback()
        obj.deleteNetworkPoliciesFromAAI(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", prefix)
        Mockito.verify(mockExecution).setVariable(prefix + "networkPolicyFqdnCount", 1)
        Mockito.verify(mockExecution).setVariable(prefix + "aaiQueryNetworkPolicyByFqdnReturnCode", 200)
    }


    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoCreateVfModuleRollback")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoCreateVfModuleRollback")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DoCreateVfModuleRollback")
        when(mockExecution.getProcessInstanceId()).thenReturn("DoCreateVfModuleRollback")
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
                .withStatus(200)));

    }
}
