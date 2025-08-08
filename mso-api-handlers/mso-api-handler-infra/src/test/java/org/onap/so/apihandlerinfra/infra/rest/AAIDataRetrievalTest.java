/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.infra.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aai.domain.yang.VolumeGroups;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;

@RunWith(MockitoJUnitRunner.class)
public class AAIDataRetrievalTest {
    @Mock
    private AAIResourcesClient aaiResourcesClient;

    @InjectMocks
    private AAIDataRetrieval aaiDataRetrieval = new AAIDataRetrieval();

    @Test
    public void getVfModulesOfVnfTest() {
        VfModules vfModules = getVfModules();
        AAIPluralResourceUri uri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("vnfId").vfModules());
        doReturn(Optional.of(vfModules)).when(aaiResourcesClient).get(VfModules.class, uri);
        List<VfModule> vfModulesList = aaiDataRetrieval.getVfModulesOfVnf("vnfId");
        assertEquals("vfm1", vfModulesList.get(0).getVfModuleId());

        Optional<String> vfModuleIds = aaiDataRetrieval.getVfModuleIdsByVnfId("vnfId");
        assertEquals(Optional.of("vfm1,vfm2"), vfModuleIds);
    }

    @Test
    public void getVfModulesOfVnfWhenNoneTest() {
        VfModules vfModules = new VfModules();
        AAIPluralResourceUri uri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("vnfId").vfModules());
        doReturn(Optional.of(vfModules)).when(aaiResourcesClient).get(VfModules.class, uri);
        List<VfModule> vfModulesList = aaiDataRetrieval.getVfModulesOfVnf("vnfId");
        assertEquals(true, vfModulesList.isEmpty());

        Optional<String> vfModuleIds = aaiDataRetrieval.getVfModuleIdsByVnfId("vnfId");
        assertEquals(false, vfModuleIds.isPresent());
    }

    @Test
    public void getVolumeGroupsOfVnfTest() {
        VolumeGroups volumeGroups = getVolumeGroups();
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("vnfId"))
                .relatedTo(Types.VOLUME_GROUPS.getFragment());
        doReturn(Optional.of(volumeGroups)).when(aaiResourcesClient).get(VolumeGroups.class, uri);
        List<VolumeGroup> volumeGroupList = aaiDataRetrieval.getVolumeGroupsOfVnf("vnfId");
        assertEquals("vg1", volumeGroupList.get(0).getVolumeGroupId());
        Optional<String> volumeGroupIds = aaiDataRetrieval.getVolumeGroupIdsByVnfId("vnfId");
        assertEquals(Optional.of("vg1,vg2"), volumeGroupIds);
    }

    @Test
    public void getVolumeGroupsOfVnfWhenNoneTest() {
        VolumeGroups volumeGroups = new VolumeGroups();
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("vnfId"))
                .relatedTo(Types.VOLUME_GROUPS.getFragment());
        doReturn(Optional.of(volumeGroups)).when(aaiResourcesClient).get(VolumeGroups.class, uri);
        List<VolumeGroup> volumeGroupList = aaiDataRetrieval.getVolumeGroupsOfVnf("vnfId");
        assertEquals(true, volumeGroupList.isEmpty());
        Optional<String> volumeGroupIds = aaiDataRetrieval.getVolumeGroupIdsByVnfId("vnfId");
        assertEquals(false, volumeGroupIds.isPresent());
    }


    private VfModules getVfModules() {
        VfModule vfModule1 = new VfModule();
        VfModule vfModule2 = new VfModule();
        vfModule1.setVfModuleId("vfm1");
        vfModule2.setVfModuleId("vfm2");
        VfModules vfModules = new VfModules();
        vfModules.getVfModule().add(vfModule1);
        vfModules.getVfModule().add(vfModule2);
        return vfModules;
    }

    private VolumeGroups getVolumeGroups() {
        VolumeGroup volumeGroup1 = new VolumeGroup();
        volumeGroup1.setVolumeGroupId("vg1");
        VolumeGroup volumeGroup2 = new VolumeGroup();
        volumeGroup2.setVolumeGroupId("vg2");
        VolumeGroups volumeGroups = new VolumeGroups();
        volumeGroups.getVolumeGroup().add(volumeGroup1);
        volumeGroups.getVolumeGroup().add(volumeGroup2);
        return volumeGroups;
    }
}
