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


import static org.mockito.Mockito.*
import jakarta.ws.rs.core.UriBuilder
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.VfModule
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.constants.Defaults

@RunWith(MockitoJUnitRunner.Silent.class)
class UpdateVfModuleVolumeInfraV1Test extends MsoGroovyTest{

    def prefix = "UPDVfModVol_"
    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Rule
    public ExpectedException thrown = ExpectedException.none()


    @Before
    public void init() {
        super.init("UpdateVfModuleVolumeInfraV1")
        MockitoAnnotations.initMocks(this)
    }

    @Test
    void testQueryAAIForVfModule() {
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("UPDVfModVol_relatedVfModuleLink")).thenReturn("/aai/v8/network/generic-vnfs/generic-vnf/12345/vf-modules/vf-module/12345")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:28090")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

        UpdateVfModuleVolumeInfraV1 obj = spy(UpdateVfModuleVolumeInfraV1.class)
        when(obj.getAAIClient()).thenReturn(client)
        AAIResourceUri uri = AAIUriFactory.createResourceFromExistingURI(Types.VF_MODULE, UriBuilder.fromPath("/aai/v8/network/generic-vnfs/generic-vnf/12345/vf-modules/vf-module/12345").build())
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId("12345")
        vfModule.setModelInvariantId("ff5256d2-5a33-55df-13ab-12abad84e7ff")
        when(client.get(VfModule.class,uri)).thenReturn(Optional.of(vfModule))
        obj.queryAAIForVfModule(mockExecution, "true")

        verify(mockExecution, atLeastOnce()).setVariable("UPDVfModVol_personaModelId", "ff5256d2-5a33-55df-13ab-12abad84e7ff")
    }

    @Test
    void testQueryAAIForVolumeGroup() {
        String aicCloudRegion = "aicCloudRegionId"
        String volumeGroupId = "volumeGroupId"
        when(mockExecution.getVariable("UPDVfModVol_volumeGroupId")).thenReturn(volumeGroupId)
        when(mockExecution.getVariable("UPDVfModVol_aicCloudRegion")).thenReturn(aicCloudRegion)

        UpdateVfModuleVolumeInfraV1 obj = spy(UpdateVfModuleVolumeInfraV1.class)
        when(obj.getAAIClient()).thenReturn(client)
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(Defaults.CLOUD_OWNER.toString(), aicCloudRegion).volumeGroup(volumeGroupId))
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId(volumeGroupId)

        AAIResultWrapper wrapper = new AAIResultWrapper(FileUtil.readResourceFile("__files/aai/VolumeGroupWithTenant.json"))
        when(client.get(uri)).thenReturn(wrapper)
        thrown.expect(BpmnError.class)
        obj.queryAAIForVolumeGroup(mockExecution, "true")
    }

    @Test
    void testQueryAAIForGenericVnf() {
        String vnfId = "vnfId"
        when(mockExecution.getVariable("vnfId")).thenReturn(vnfId)

        UpdateVfModuleVolumeInfraV1 obj = spy(UpdateVfModuleVolumeInfraV1.class)
        when(obj.getAAIClient()).thenReturn(client)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
        GenericVnf genericVnf = new GenericVnf()
        genericVnf.setVnfId(vnfId)
        genericVnf.setVnfName("testvnfName")
        when(client.get(GenericVnf.class,uri)).thenReturn(Optional.of(genericVnf))
        obj.queryAAIForGenericVnf(mockExecution, "true")
        verify(mockExecution).setVariable("UPDVfModVol_AAIQueryGenericVfnResponse", genericVnf)
    }

    @Test
    void testQueryAAIForGenericVnfNodata() {
        String vnfId = "vnfId"
        when(mockExecution.getVariable("vnfId")).thenReturn(vnfId)

        UpdateVfModuleVolumeInfraV1 obj = spy(UpdateVfModuleVolumeInfraV1.class)
        when(obj.getAAIClient()).thenReturn(client)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
        when(client.get(GenericVnf.class,uri)).thenReturn(Optional.empty())
        thrown.expect(BpmnError.class)
        obj.queryAAIForGenericVnf(mockExecution, "true")
        verify(mockExecution).setVariable("UPDVfModVol_AAIQueryGenericVfnResponse", genericVnf)
    }

    @Test
    void testPrepVnfAdapterRest() {
        when(mockExecution.getVariable("prefix")).thenReturn(prefix)
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(prefix + "aicCloudRegion")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(prefix + "cloudOwner")).thenReturn("CloudOwner")
        when(mockExecution.getVariable(prefix + "tenantId")).thenReturn("")
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setHeatStackId("heatStackId")
        when(mockExecution.getVariable(prefix + "aaiVolumeGroupResponse")).thenReturn(volumeGroup)
        when(mockExecution.getVariable(prefix + "vnfType")).thenReturn("vnf1")
        when(mockExecution.getVariable(prefix + "vnfVersion")).thenReturn("1")
        GenericVnf genericVnf = new GenericVnf()
        genericVnf.setVnfId("vnfId")
        genericVnf.setVnfName("testvnfName")
        when(mockExecution.getVariable(prefix + "AAIQueryGenericVfnResponse")).thenReturn(genericVnf)
        when(mockExecution.getVariable(prefix + "requestId")).thenReturn("12345")
        when(mockExecution.getVariable(prefix + "serviceId")).thenReturn("12345")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("12345")
        when(mockExecution.getVariable("mso.workflow.message.endpoint")).thenReturn('http://localhost:28080/mso/WorkflowMessage')
        when(mockExecution.getVariable("mso.use.qualified.host")).thenReturn("true")

        UpdateVfModuleVolumeInfraV1 obj = new UpdateVfModuleVolumeInfraV1()
        obj.prepVnfAdapterRest(mockExecution, "true")

        verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        String updateVolumeGroupRequest = captor.getValue()
        Assert.assertTrue(updateVolumeGroupRequest.contains("testvnfName"))
    }
}
