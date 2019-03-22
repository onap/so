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
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.utils.XmlComparator
import org.onap.so.bpmn.core.RollbackData
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.bpmn.mock.StubResponseAAI
import org.springframework.mock.env.MockEnvironment

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.ArgumentMatchers.refEq
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DoCreateVfModuleTest {
    def prefix = "DCVFM_"

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(28090)

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Before
    void init() throws IOException {
        MockitoAnnotations.initMocks(this);
    }
  
    @Test
    void testQueryAAIVfModule() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("DCVFM_vnfId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.DoCreateVfModule.aai.generic-vnf.uri")).thenReturn("/aai/v9/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        prepareUrnPropertiesReader()
        mockData()
        DoCreateVfModule obj = new DoCreateVfModule()
        obj.queryAAIVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable("DCVFM_queryAAIVfModuleResponseCode", 200)
    }


    @Test
    void testQueryAAIVfModuleForStatus() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("DCVFM_vnfId")).thenReturn("12345")
        when(mockExecution.getVariable("DCVFM_vfModuleName")).thenReturn("module-0")
        when(mockExecution.getVariable("mso.workflow.DoCreateVfModule.aai.generic-vnf.uri")).thenReturn("/aai/v9/network/generic-vnfs/generic-vnf")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        prepareUrnPropertiesReader()
        mockData()
        DoCreateVfModule obj = new DoCreateVfModule()
        obj.queryAAIVfModuleForStatus(mockExecution)
        Mockito.verify(mockExecution).setVariable("DCVFM_orchestrationStatus", '')
        Mockito.verify(mockExecution).setVariable("DCVFM_queryAAIVfModuleForStatusResponseCode", 200)
    }

  

    @Test
    void testPreProcessVNFAdapterRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable(prefix + "cloudSiteId")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("cb510af0-5b21-4bc7-86d9-323cb396ce32")
        when(mockExecution.getVariable("volumeGroupStackId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "vfModuleName")).thenReturn("PCRF::module-0-2")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("MDTWNJ21")
        when(mockExecution.getVariable(prefix + "tenantId")).thenReturn("fba1bd1e195a404cacb9ce17a9b2b421")
        when(mockExecution.getVariable(prefix + "vnfType")).thenReturn("vRRaas")
        when(mockExecution.getVariable(prefix + "vnfName")).thenReturn("skask-test")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleModelName")).thenReturn("PCRF::module-0-2")
        when(mockExecution.getVariable(prefix + "vfModuleIndex")).thenReturn("index")
        when(mockExecution.getVariable(prefix + "requestId")).thenReturn("testRequestId")
        when(mockExecution.getVariable(prefix + "serviceId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "serviceInstanceId")).thenReturn("MIS/1604/0026/SW_INTERNET")
        when(mockExecution.getVariable(prefix + "backoutOnFailure")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "volumeGroupId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "asdcServiceModelVersion")).thenReturn("1.0")
        when(mockExecution.getVariable(prefix + "modelCustomizationUuid")).thenReturn("cb510af0-5b21-4bc7-86d9-323cb396ced3")
        when(mockExecution.getVariable("baseVfModuleId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "baseVfModuleHeatStackId")).thenReturn("12345")
        String sdncGetResponse = FileUtil.readResourceFile("__files/DoCreateVfModule/sdncGetResponse.xml");
        when(mockExecution.getVariable(prefix + "getSDNCAdapterResponse")).thenReturn(sdncGetResponse)
        Map<String, String> map = new HashMap<String, String>();
        map.put("vrr_image_name", "MDT17");
        map.put("availability_zone_0", "nova");
        map.put("vrr_flavor_name", "ns.c16r32d128.v1");
        when(mockExecution.getVariable("vnfParamsMap")).thenReturn(map)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("testRequestId-1503410089303")
        when(mockExecution.getVariable("mso.use.qualified.host")).thenReturn("true")
        when(mockExecution.getVariable("mso.workflow.message.endpoint")).thenReturn("http://localhost:28080/mso/WorkflowMesssage")

        mockData()
        DoCreateVfModule obj = new DoCreateVfModule()
        obj.preProcessVNFAdapterRequest(mockExecution)

        String createVnfARequest = FileUtil.readResourceFile("__files/DoCreateVfModule/createVnfARequest.xml")
        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        XmlComparator.assertXMLEquals(createVnfARequest, captor.getValue(), "messageId", "notificationUrl")
    }

    @Test
    void testQueryCloudRegion() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("DCVFM_cloudSiteId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.DoCreateVfModule.aai.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        DoCreateVfModule obj = new DoCreateVfModule()
        obj.queryCloudRegion(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", prefix)
        Mockito.verify(mockExecution).setVariable(prefix + "queryCloudRegionRequest",
                "http://localhost:28090/aai/v14/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/12345")
        Mockito.verify(mockExecution).setVariable(prefix + "queryCloudRegionReturnCode", "200")
    }

  

    @Test
    void testCreateNetworkPoliciesInAAI() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("DCVFM_cloudSiteId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.DoCreateVfModule.aai.network-policy.uri")).thenReturn("/aai/v8/network/network-policies/network-policy")
        when(mockExecution.getVariable("mso.workflow.custom.DoCreateVfModule.aai.version")).thenReturn("8")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")
        when(mockExecution.getVariable("rollbackData")).thenReturn(new RollbackData())
        List fqdnList = new ArrayList()
        fqdnList.add("test")
        when(mockExecution.getVariable("DCVFM_contrailNetworkPolicyFqdnList")).thenReturn(fqdnList)

        prepareUrnPropertiesReader()
        mockData()
        DoCreateVfModule obj = new DoCreateVfModule()
        obj.createNetworkPoliciesInAAI(mockExecution)

        Mockito.verify(mockExecution).setVariable("prefix", prefix)
        RollbackData rollbackData = new RollbackData()
        rollbackData.put("VFMODULE", "rollbackCreateNetworkPoliciesAAI", "true")
        rollbackData.put("VFMODULE", "contrailNetworkPolicyFqdn0", "test")

        Mockito.verify(mockExecution).setVariable(eq("rollbackData"), refEq(rollbackData))
    }

   

    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoCreateVfModule")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoCreateVfModule")

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")

        return mockExecution
    }

    private static void prepareUrnPropertiesReader() {
        MockEnvironment mockEnvironment = mock(MockEnvironment.class)
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.version")).thenReturn("14")
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.namespace")).thenReturn("defaultTestNamespace")
        when(mockEnvironment.getProperty("aai.endpoint")).thenReturn("http://localhost:28090")
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader()
        urnPropertiesReader.setEnvironment(mockEnvironment)
    }

    private static void mockData() {
        StubResponseAAI.MockGetGenericVnfById("12345", "DoCreateVfModule/getGenericVnfResponse.xml");
        StubResponseAAI.MockGetVfModuleByName("12345",
                "module-0", "DoCreateVfModule/getGenericVnfResponse.xml", 200);
        StubResponseAAI.MockGetNetworkCloudRegion(
                "DoCreateVfModule/cloudRegion_AAIResponse_Success.xml", "12345")
        stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies\\?depth=0&nodes-only=&network-policy-fqdn=.*"))
                .willReturn(aResponse()
                .withStatus(404)))
        StubResponseAAI.MockPutNetwork(".*", "", 202)
    }
}
