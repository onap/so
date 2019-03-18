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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.constants.Defaults

import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.Silent.class)
class UpdateVfModuleVolumeTest extends MsoGroovyTest{

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Rule
    public ExpectedException thrown = ExpectedException.none()


    @Before
    public void init() {
        super.init("UpdateVfModuleVolume")
        MockitoAnnotations.initMocks(this)
    }


    @Test
    void testQueryAAIForVolumeGroup() {
        String aicCloudRegion = "aicCloudRegionId"
        String volumeGroupId = "volumeGroupId"
        when(mockExecution.getVariable("UPDVfModVol_volumeGroupId")).thenReturn(volumeGroupId)
        when(mockExecution.getVariable("UPDVfModVol_aicCloudRegion")).thenReturn(aicCloudRegion)

        UpdateVfModuleVolume obj = spy(UpdateVfModuleVolume.class)
        when(obj.getAAIClient()).thenReturn(client)
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), aicCloudRegion, volumeGroupId)
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId(volumeGroupId)

        AAIResultWrapper wrapper = new AAIResultWrapper(FileUtil.readResourceFile("__files/AAI/VolumeGroupWithTenant.json"))
        when(client.get(uri)).thenReturn(wrapper)
        obj.queryAAIForVolumeGroup(mockExecution)
        verify(mockExecution).setVariable("UPDVfModVol_volumeGroupHeatStackId","heatStackId")
    }

    @Test
    void testQueryAAIForVolumeGroupNoTenant() {
        String aicCloudRegion = "aicCloudRegionId"
        String volumeGroupId = "volumeGroupId"
        when(mockExecution.getVariable("UPDVfModVol_volumeGroupId")).thenReturn(volumeGroupId)
        when(mockExecution.getVariable("UPDVfModVol_aicCloudRegion")).thenReturn(aicCloudRegion)

        UpdateVfModuleVolume obj = spy(UpdateVfModuleVolume.class)
        when(obj.getAAIClient()).thenReturn(client)
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), aicCloudRegion, volumeGroupId)
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId(volumeGroupId)

        when(client.get(uri)).thenThrow(NullPointerException.class)
        thrown.expect(BpmnError.class)
        obj.queryAAIForVolumeGroup(mockExecution)
    }
}
