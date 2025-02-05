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
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.common.scripts.VfModule
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*
import org.onap.so.bpmn.common.scripts.utils.XmlComparator

@RunWith(MockitoJUnitRunner.class)
class DoUpdateVfModuleTest extends MsoGroovyTest{

    def prefix = "DOUPVfMod_"
    String doUpdateVfModuleRequest = FileUtil.readResourceFile("__files/VfModularity/DoUpdateVfModuleRequest.xml");
    String sdncChangeAssignRequest = FileUtil.readResourceFile("__files/DoUpdateVfModule/sdncChangeAssignRequest.xml")
    String sdncTopologyRequest = FileUtil.readResourceFile("__files/DoUpdateVfModule/sdncTopologyRequest.xml")

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Spy
    DoUpdateVfModule doUpdateVfModule

    @Before
    public void init() {
        super.init("DoUpdateVfModule")
        MockitoAnnotations.initMocks(this)
        when(doUpdateVfModule.getAAIClient()).thenReturn(client)
    }

    @Test
    void testPreProcessRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("DoUpdateVfModuleRequest")).thenReturn(doUpdateVfModuleRequest)
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:28080/mso/SDNCAdapterCallbackService")

        DoUpdateVfModule obj = new DoUpdateVfModule()
        obj.preProcessRequest(mockExecution)

        Mockito.verify(mockExecution, atLeastOnce()).getVariable("mso.workflow.sdncadapter.callback")
    }



    @Test
    void testPrepConfirmVolumeGroupTenant() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "aicCloudRegion")).thenReturn("CloudOwner")
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region")
        when(mockExecution.getVariable("mso.workflow.default.aai.cloud-region.version")).thenReturn("8")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn("http://org.openecomp.aai.inventory/")

        mockData()
        DoUpdateVfModule obj = new DoUpdateVfModule()
        obj.prepConfirmVolumeGroupTenant(mockExecution)

        Mockito.verify(mockExecution).setVariable(prefix + "queryCloudRegionRequest", "http://localhost:28090/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
        Mockito.verify(mockExecution).setVariable(prefix + "queryCloudRegionReturnCode", "200")
        Mockito.verify(mockExecution).setVariable(prefix + "cloudRegionForVolume", "AAIAIC25")
        Mockito.verify(mockExecution).setVariable(prefix + "isCloudRegionGood", true)
    }


    @Test
    void testPrepSDNCTopologyChg() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable(prefix + "requestId")).thenReturn("12345")
        when(mockExecution.getVariable("testReqId")).thenReturn("testReqId")


        when(mockExecution.getVariable(prefix + "cloudSiteId")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("cb510af0-5b21-4bc7-86d9-323cb396ce32")
        when(mockExecution.getVariable("volumeGroupStackId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "vfModuleName")).thenReturn("PCRF::module-0-2")
        when(mockExecution.getVariable(prefix + "aicCloudRegion")).thenReturn("MDTWNJ21")
        when(mockExecution.getVariable(prefix + "usePreload")).thenReturn("Y")
        when(mockExecution.getVariable(prefix + "vnfNameFromAAI")).thenReturn("skask-test")

        def node = new Node(null, 'vfModule')
        new Node(node, 'vf-module-name', "abc")
        VfModule vfModule = new VfModule(node, true);
        when(mockExecution.getVariable(prefix + "vfModule")).thenReturn(vfModule)

        when(mockExecution.getVariable(prefix + "tenantId")).thenReturn("fba1bd1e195a404cacb9ce17a9b2b421")
        when(mockExecution.getVariable(prefix + "vnfType")).thenReturn("vRRaas")
        when(mockExecution.getVariable(prefix + "vnfName")).thenReturn("skask-test")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleModelName")).thenReturn("PCRF::module-0-2")
        when(mockExecution.getVariable(prefix + "requestId")).thenReturn("testRequestId")
        when(mockExecution.getVariable(prefix + "serviceId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "serviceInstanceId")).thenReturn("MIS/1604/0026/SW_INTERNET")
        Map<String, String> map = new HashMap<String, String>();
        map.put("vrr_image_name", "MDT17");
        map.put("availability_zone_0", "nova");
        map.put("vrr_flavor_name", "ns.c16r32d128.v1");
        when(mockExecution.getVariable("vnfParamsMap")).thenReturn(map)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("testRequestId-1503410089303")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapter")

        mockData()
        DoUpdateVfModule obj = new DoUpdateVfModule()
        obj.prepSDNCTopologyChg(mockExecution)

        Mockito.verify(mockExecution).setVariable(prefix + "vnfName", "skask-test")

        Mockito.verify(mockExecution, times(2)).setVariable(captor.capture(), captor.capture())
        XmlComparator.assertXMLEquals(sdncChangeAssignRequest, captor.getValue())
    }



    @Test
    void testPrepSDNCTopologyQuery() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("testReqId")).thenReturn("testReqId")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "serviceId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "serviceInstanceId")).thenReturn("MIS/1604/0026/SW_INTERNET")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapter")

        mockData()
        DoUpdateVfModule obj = new DoUpdateVfModule()
        obj.prepSDNCTopologyQuery(mockExecution)

        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        XmlComparator.assertXMLEquals(sdncTopologyRequest, captor.getValue())
    }



    @Test
    void testPrepVnfAdapterRest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable(prefix + "aicCloudRegion")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(prefix + "cloudRegion")).thenReturn("CloudOwner")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("cb510af0-5b21-4bc7-86d9-323cb396ce32")
        when(mockExecution.getVariable(prefix + "volumeGroupStackId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "vfModuleName")).thenReturn("PCRF::module-0-2")
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
        when(mockExecution.getVariable(prefix + "baseVfModuleId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "baseVfModuleHeatStackId")).thenReturn("12345")

        def node = new Node(null, 'vfModule')
        new Node(node, 'heat-stack-id', "abc")
        VfModule vfModule = new VfModule(node, true);
        when(mockExecution.getVariable(prefix + "vfModule")).thenReturn(vfModule)

        String sdncGetResponse = FileUtil.readResourceFile("__files/DoUpdateVfModule/sdncGetResponse.xml");
        when(mockExecution.getVariable(prefix + "sdncTopologyResponse")).thenReturn(sdncGetResponse)
        Map<String, String> map = new HashMap<String, String>();
        map.put("vrr_image_name", "MDT17");
        map.put("availability_zone_0", "nova");
        map.put("vrr_flavor_name", "ns.c16r32d128.v1");
        when(mockExecution.getVariable(prefix + "vnfParamsMap")).thenReturn(map)
        when(mockExecution.getVariable("mso-request-id")).thenReturn("testRequestId-1503410089303")
        when(mockExecution.getVariable("mso.use.qualified.host")).thenReturn("true")
        when(mockExecution.getVariable("mso.workflow.message.endpoint")).thenReturn("http://localhost:28080/mso/WorkflowMesssage")

        mockData()
        DoUpdateVfModule obj = new DoUpdateVfModule()
        obj.prepVnfAdapterRest(mockExecution)

        String createVnfARequest = FileUtil.readResourceFile("__files/DoUpdateVfModule/vnfAdapterRestRequest.xml")
        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        XmlComparator.assertXMLEquals(createVnfARequest, captor.getValue(), "messageId", "notificationUrl")
    }



    @Test
    void testPrepSDNCTopologyAct() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "aicCloudRegion")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable("testReqId")).thenReturn("testReqId")
        when(mockExecution.getVariable(prefix + "vfModuleId")).thenReturn("cb510af0-5b21-4bc7-86d9-323cb396ce32")
        when(mockExecution.getVariable(prefix + "tenantId")).thenReturn("fba1bd1e195a404cacb9ce17a9b2b421")
        when(mockExecution.getVariable(prefix + "vnfType")).thenReturn("vRRaas")
        when(mockExecution.getVariable(prefix + "vnfName")).thenReturn("skask-test")
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("skask")
        when(mockExecution.getVariable(prefix + "vfModuleModelName")).thenReturn("PCRF::module-0-2")
        when(mockExecution.getVariable(prefix + "requestId")).thenReturn("testRequestId")
        when(mockExecution.getVariable(prefix + "serviceId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "serviceInstanceId")).thenReturn("MIS/1604/0026/SW_INTERNET")
        when(mockExecution.getVariable(prefix + "usePreload")).thenReturn("Y")
        when(mockExecution.getVariable(prefix + "modelCustomizationUuid")).thenReturn("cb510af0-5b21-4bc7-86d9-323cb396ced3")

        def node = new Node(null, 'vfModule')
        new Node(node, 'vf-module-name', "abc")
        VfModule vfModule = new VfModule(node, true);
        when(mockExecution.getVariable(prefix + "vfModule")).thenReturn(vfModule)

        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapter")

        mockData()
        DoUpdateVfModule obj = new DoUpdateVfModule()
        obj.prepSDNCTopologyAct(mockExecution)

        String createVnfARequest = FileUtil.readResourceFile("__files/DoUpdateVfModule/sdncActivateRequest.xml")
        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        XmlComparator.assertXMLEquals(createVnfARequest, captor.getValue())
    }

    @Test
    void testQueryAAIVfModule() {
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable(prefix + "vnfId")).thenReturn("12345")

        mockAAIGenericVnf("12345","__files/AAI/GenericVnfVfModule.json")
        doUpdateVfModule.queryAAIVfModule(mockExecution)
        Mockito.verify(mockExecution).setVariable(prefix + "queryAAIVfModuleResponseCode", 200)
        Mockito.verify(mockExecution).setVariable("DOUPVfMod_baseVfModuleId", "lukewarm")
        Mockito.verify(mockExecution).setVariable("DOUPVfMod_baseVfModuleHeatStackId", "fastburn")

    }


    private static ExecutionEntity setupMock() {
        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoUpdateVfModule")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoUpdateVfModule")
        when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getId()).thenReturn("100")
        when(mockExecution.getProcessDefinitionId()).thenReturn("DoUpdateVfModule")
        when(mockExecution.getProcessInstanceId()).thenReturn("DoUpdateVfModule")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }

    private static void mockData() {
        stubFor(get(urlMatching(".*/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("DoUpdateVfModule/cloudRegion_AAIResponse_Success.xml")))

        stubFor(get(urlMatching(".*/aai/v[0-9]+/network/generic-vnfs/generic-vnf/12345[?]depth=1"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("DoUpdateVfModule/getGenericVnfResponse.xml")))
    }
}
