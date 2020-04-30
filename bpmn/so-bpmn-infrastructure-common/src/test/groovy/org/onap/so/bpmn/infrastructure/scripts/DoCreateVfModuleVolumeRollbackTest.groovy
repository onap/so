/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.scripts;

import org.junit.Before;
import org.junit.Test
import org.onap.aai.domain.yang.VfModule
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.aai.domain.yang.VolumeGroups;
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.aaiclient.client.aai.AAIObjectPlurals
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.so.constants.Defaults

import static org.mockito.Mockito.spy
import static org.mockito.Mockito.when;

public class DoCreateVfModuleVolumeRollbackTest extends MsoGroovyTest {

    private  DoCreateVfModuleVolumeRollback doCreateVfModuleVolumeRollback;
    @Before
    public void init(){
        super.init("DoCreateVfModuleVolumeRollback");
        doCreateVfModuleVolumeRollback = spy(DoCreateVfModuleVolumeRollback.class);
        when(doCreateVfModuleVolumeRollback.getAAIClient()).thenReturn(client)
    }

    @Test
    void callRESTDeleteAAIVolumeGroupTest(){
        String volumeGroupName = "volumeGroupName"
        String cloudRegionId = "cloudRegionId"
        when(mockExecution.getVariable("DCVFMODVOLRBK_volumeGroupName")).thenReturn(volumeGroupName)
        when(mockExecution.getVariable("DCVFMODVOLRBK_lcpCloudRegionId")).thenReturn(cloudRegionId)
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.VOLUME_GROUP, Defaults.CLOUD_OWNER.toString(), cloudRegionId).queryParam("volume-group-name", volumeGroupName)
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId("volumeGroupId")
        VolumeGroups groups = new VolumeGroups();
        groups.getVolumeGroup().add(volumeGroup)
        when(client.get(VolumeGroups.class,uri)).thenReturn(Optional.of(groups))

        doCreateVfModuleVolumeRollback.callRESTDeleteAAIVolumeGroup(mockExecution,null)
    }

}
