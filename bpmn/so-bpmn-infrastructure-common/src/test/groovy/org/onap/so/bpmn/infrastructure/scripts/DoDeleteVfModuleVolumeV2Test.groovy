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
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.common.scripts.utils.XmlComparator
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.graphinventory.exceptions.GraphInventoryUriComputationException
import org.springframework.core.env.Environment
import org.springframework.mock.env.MockEnvironment

import jakarta.ws.rs.NotFoundException

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.Silent.class)
class DoDeleteVfModuleVolumeV2Test extends MsoGroovyTest{

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090)

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Spy
    DoDeleteVfModuleVolumeV2 deleteVfModuleVolumeV2

    @Mock
    Environment mockEnvironment

    private String Prefix = "DDVMV_"
    private RepositoryService mockRepositoryService


    @After
    void cleanupEnv() {
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader()
        urnPropertiesReader.setEnvironment(null)
    }

    @Before
    public void init() throws IOException {
        super.init("DoDeleteVfModuleVolumeV2")
        MockitoAnnotations.initMocks(this);
        when(deleteVfModuleVolumeV2.getAAIClient()).thenReturn(client)
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.version")).thenReturn("14")
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.namespace")).thenReturn("defaultTestNamespace")
        when(mockEnvironment.getProperty("aai.endpoint")).thenReturn("http://localhost:8090")
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader()
        urnPropertiesReader.setEnvironment(mockEnvironment)
    }

    @Test
    public void testCallRESTQueryAAICloudRegion() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("12345")
        mockSuccessfulCloudData()

        DoDeleteVfModuleVolumeV2 obj = new DoDeleteVfModuleVolumeV2()
        obj.callRESTQueryAAICloudRegion(mockExecution, "true")
        Mockito.verify(mockExecution).setVariable("DDVMV_queryCloudRegionReturnCode", "200")
        Mockito.verify(mockExecution).setVariable("DDVMV_aicCloudRegion", "RDM2WAGPLCP")
    }

    @Test(expected = GroovyRuntimeException.class)
    public void testCallRESTQueryAAICloudRegionAAiEndpointNull() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("12345")
        when(mockEnvironment.getProperty("aai.endpoint")).thenReturn(null)

        mockSuccessfulCloudData()
        try {
            DoDeleteVfModuleVolumeV2 obj = new DoDeleteVfModuleVolumeV2()
            obj.callRESTQueryAAICloudRegion(mockExecution, "true")

        } catch (Exception ex) {
            println " Test End - Handle catch-throw Exception! "
            Mockito.verify(mockExecution).getVariable(eq("lcpCloudRegionId"))
            Assert.assertEquals(GroovyRuntimeException.class, ex.class)
            throw ex
        }
    }

    @Test
    public void testCallRESTQueryAAICloudRegionNotFound() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("12345")
        when(mockExecution.getVariable(Prefix + "queryCloudRegionReturnCode")).thenReturn("404")

        wireMockRule.stubFor(
                get(urlMatching(".*/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/12345"))
                .willReturn(aResponse()
                .withStatus(404)))

        DoDeleteVfModuleVolumeV2 obj = new DoDeleteVfModuleVolumeV2()
        obj.callRESTQueryAAICloudRegion(mockExecution, "true")

        Mockito.verify(mockExecution).getVariable(eq("lcpCloudRegionId"))
        Mockito.verify(mockExecution).setVariable(eq(Prefix + "queryCloudRegionReturnCode"), eq("404"))
        Mockito.verify(mockExecution).setVariable(eq(Prefix + "aicCloudRegion"), eq("AAIAIC25"))
    }

    @Test
    public void testPrepareVnfAdapterDeleteRequest() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("aicCloudRegion")).thenReturn("RegionOne")
        when(mockExecution.getVariable("tenantId")).thenReturn("12345")
        when(mockExecution.getVariable("volumeGroupId")).thenReturn("12345")
        when(mockExecution.getVariable("volumeGroupHeatStackId")).thenReturn("12345")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("12345")
        when(mockExecution.getVariable("mso.use.qualified.host")).thenReturn("true")
        when(mockExecution.getVariable("mso.workflow.message.endpoint")).thenReturn("http://localhost:18080/mso/WorkflowMessage")

        DoDeleteVfModuleVolumeV2 obj = new DoDeleteVfModuleVolumeV2()
        obj.prepareVnfAdapterDeleteRequest(mockExecution, "true")

        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        String str = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV2/vnfAdapterDeleteRequest.xml")
        XmlComparator.assertXMLEquals(str, captor.getValue(),"messageId","notificationUrl")
    }

    @Test
    void testCallRESTQueryAAIForVolumeGroup(){
        when(mockExecution.getVariable("tenantId")).thenReturn("Tenant123")
        when(mockExecution.getVariable("volumeGroupId")).thenReturn("VolumeGroup123")
        when(mockExecution.getVariable("DDVMV_aicCloudRegion")).thenReturn("Region1")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(CLOUD_OWNER, "Region1").volumeGroup("VolumeGroup123"))
        Optional<VolumeGroup> volumeGroup = getAAIObjectFromJson(VolumeGroup.class,"__files/AAI/VolumeGroupWithTenant.json");
        when(client.get(VolumeGroup.class,resourceUri)).thenReturn(volumeGroup)
        when(client.get(resourceUri)).thenReturn(
                new AAIResultWrapper(FileUtil.readResourceFile("__files/AAI/VolumeGroupWithTenant.json")))
        deleteVfModuleVolumeV2.callRESTQueryAAIForVolumeGroup(mockExecution,"true")
        Mockito.verify(mockExecution).setVariable("DDVMV_queryAAIVolGrpResponse", volumeGroup.get())
        Mockito.verify(mockExecution).setVariable("DDVMV_volumeGroupHeatStackId", volumeGroup.get().getHeatStackId())
    }

    @Test
    void testCallRESTQueryAAIForVolumeGroupNoTenant(){
        when(mockExecution.getVariable("tenantId")).thenReturn("Tenant123")
        when(mockExecution.getVariable("volumeGroupId")).thenReturn("VolumeGroup123")
        when(mockExecution.getVariable("DDVMV_aicCloudRegion")).thenReturn("Region1")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(CLOUD_OWNER, "Region1").volumeGroup("VolumeGroup123"))
        Optional<VolumeGroup> volumeGroup = getAAIObjectFromJson(VolumeGroup.class,"__files/aai/VolumeGroup.json");
        when(client.get(VolumeGroup.class,resourceUri)).thenReturn(volumeGroup)
        try {
            deleteVfModuleVolumeV2.callRESTQueryAAIForVolumeGroup(mockExecution, "true")
        }catch(BpmnError error) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
    }

    @Test
    void testCallRESTQueryAAIForVolumeGroupDifferentTenant(){
        when(mockExecution.getVariable("tenantId")).thenReturn("Tenant12345")
        when(mockExecution.getVariable("volumeGroupId")).thenReturn("VolumeGroup123")
        when(mockExecution.getVariable("DDVMV_aicCloudRegion")).thenReturn("Region1")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(CLOUD_OWNER, "Region1").volumeGroup("VolumeGroup123"))
        Optional<VolumeGroup> volumeGroup = getAAIObjectFromJson(VolumeGroup.class,"__files/AAI/VolumeGroupWithTenant.json");
        when(client.get(VolumeGroup.class,resourceUri)).thenReturn(volumeGroup)
        try {
            deleteVfModuleVolumeV2.callRESTQueryAAIForVolumeGroup(mockExecution, "true")
        }catch(BpmnError error) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
    }

    @Test
    void testCallRESTQueryAAIForVolumeGroupNotFound(){
        when(mockExecution.getVariable("tenantId")).thenReturn("Tenant123")
        when(mockExecution.getVariable("volumeGroupId")).thenReturn("VolumeGroup123")
        when(mockExecution.getVariable("DDVMV_aicCloudRegion")).thenReturn("Region1")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(CLOUD_OWNER, "Region1").volumeGroup("VolumeGroup123"))
        when(client.get(VolumeGroup.class,resourceUri)).thenReturn(Optional.empty())
        try {
            deleteVfModuleVolumeV2.callRESTQueryAAIForVolumeGroup(mockExecution, "true")
        }catch(BpmnError error) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
    }

    @Test
    void testCallRESTQueryAAIForVolumeGroupWithVfModule(){
        when(mockExecution.getVariable("tenantId")).thenReturn("Tenant123")
        when(mockExecution.getVariable("volumeGroupId")).thenReturn("VolumeGroup123")
        when(mockExecution.getVariable("DDVMV_aicCloudRegion")).thenReturn("Region1")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(CLOUD_OWNER, "Region1").volumeGroup("VolumeGroup123"))
        Optional<VolumeGroup> volumeGroup = getAAIObjectFromJson(VolumeGroup.class,"__files/aai/VolumeGroupWithVfModule.json");
        when(client.get(VolumeGroup.class,resourceUri)).thenReturn(volumeGroup)
        try {
            deleteVfModuleVolumeV2.callRESTQueryAAIForVolumeGroup(mockExecution, "true")
        }catch(BpmnError error) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
    }

    @Test
    void testCallRESTDeleteAAIVolumeGroup(){
        Optional<VolumeGroup> volumeGroup = getAAIObjectFromJson(VolumeGroup.class,"__files/aai/VolumeGroup.json");
        when(mockExecution.getVariable("DDVMV_queryAAIVolGrpResponse")).thenReturn(volumeGroup.get())
        when(mockExecution.getVariable("DDVMV_aicCloudRegion")).thenReturn("Region1")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(CLOUD_OWNER, "Region1").volumeGroup("VolumeGroup123"))
        doNothing().when(client).delete(resourceUri)
        deleteVfModuleVolumeV2.callRESTDeleteAAIVolumeGroup(mockExecution,"true")
    }

    @Test
    void testCallRESTDeleteAAIVolumeGroupAaiError(){
        Optional<VolumeGroup> volumeGroup = getAAIObjectFromJson(VolumeGroup.class,"__files/aai/VolumeGroupWithVfModule.json");
        when(mockExecution.getVariable("DDVMV_queryAAIVolGrpResponse")).thenReturn(volumeGroup.get())
        when(mockExecution.getVariable("DDVMV_aicCloudRegion")).thenReturn("Region1")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(CLOUD_OWNER, "Region1").volumeGroup(volumeGroup.get()).getVolumeGroupId())
        doThrow(new GraphInventoryUriComputationException("Error")).when(client).delete(resourceUri)
        try {
            deleteVfModuleVolumeV2.callRESTDeleteAAIVolumeGroup(mockExecution, "true")
        } catch (BpmnError error) {
                println " Test End - Handle catch-throw BpmnError()! "
        }
    }

    @Test
    void testCallRESTDeleteAAIVolumeGroupNotFound(){
        Optional<VolumeGroup> volumeGroup = getAAIObjectFromJson(VolumeGroup.class,"__files/aai/VolumeGroup.json");
        when(mockExecution.getVariable("DDVMV_queryAAIVolGrpResponse")).thenReturn(volumeGroup.get())
        when(mockExecution.getVariable("DDVMV_aicCloudRegion")).thenReturn("Region1")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(CLOUD_OWNER, "Region1").volumeGroup("VolumeGroup123"))
        doThrow(new NotFoundException("VolumeGroup Not found")).when(client).delete(resourceUri)
        try {
            deleteVfModuleVolumeV2.callRESTDeleteAAIVolumeGroup(mockExecution, "true")
        } catch (BpmnError error) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
    }




    private ExecutionEntity setupMock() {

        mockRepositoryService = mock(RepositoryService.class)
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(this.mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)

        return mockExecution

    }

    private void mockSuccessfulCloudData() {
        wireMockRule.stubFor(get(urlMatching(".*/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/12345"))
                .willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "text/xml")
                .withBodyFile("DoCreateVfModuleVolumeV2/cloudRegion_AAIResponse_Success.xml")))
    }
}
