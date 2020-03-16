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

package org.onap.so.client.orchestration;

import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIVolumeGroupResources {

    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    private AAIObjectMapper aaiObjectMapper;

    public void createVolumeGroup(VolumeGroup volumeGroup, CloudRegion cloudRegion) {
        AAIResourceUri uriVolumeGroup = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP,
                cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
        volumeGroup.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
        injectionHelper.getAaiClient().create(uriVolumeGroup, aaiObjectMapper.mapVolumeGroup(volumeGroup));
    }

    public void updateOrchestrationStatusVolumeGroup(VolumeGroup volumeGroup, CloudRegion cloudRegion,
            OrchestrationStatus orchestrationStatus) {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, cloudRegion.getCloudOwner(),
                cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
        VolumeGroup copiedVolumeGroup = volumeGroup.shallowCopyId();

        volumeGroup.setOrchestrationStatus(orchestrationStatus);
        copiedVolumeGroup.setOrchestrationStatus(orchestrationStatus);
        injectionHelper.getAaiClient().update(uri, aaiObjectMapper.mapVolumeGroup(copiedVolumeGroup));
    }

    public void connectVolumeGroupToVnf(GenericVnf genericVnf, VolumeGroup volumeGroup, CloudRegion cloudRegion) {
        AAIResourceUri uriGenericVnf =
                AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, genericVnf.getVnfId());
        AAIResourceUri uriVolumeGroup = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP,
                cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
        injectionHelper.getAaiClient().connect(uriGenericVnf, uriVolumeGroup);
    }

    public void connectVolumeGroupToTenant(VolumeGroup volumeGroup, CloudRegion cloudRegion) {
        AAIResourceUri uriTenant = AAIUriFactory.createResourceUri(AAIObjectType.TENANT, cloudRegion.getCloudOwner(),
                cloudRegion.getLcpCloudRegionId(), cloudRegion.getTenantId());
        AAIResourceUri uriVolumeGroup = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP,
                cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
        injectionHelper.getAaiClient().connect(uriTenant, uriVolumeGroup);
    }

    public void deleteVolumeGroup(VolumeGroup volumeGroup, CloudRegion cloudRegion) {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, cloudRegion.getCloudOwner(),
                cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
        injectionHelper.getAaiClient().delete(uri);
    }

    public void updateHeatStackIdVolumeGroup(VolumeGroup volumeGroup, CloudRegion cloudRegion) {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, cloudRegion.getCloudOwner(),
                cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
        VolumeGroup copiedVolumeGroup = volumeGroup.shallowCopyId();

        copiedVolumeGroup.setHeatStackId(volumeGroup.getHeatStackId());
        injectionHelper.getAaiClient().update(uri, aaiObjectMapper.mapVolumeGroup(copiedVolumeGroup));
    }

    public boolean checkNameInUse(VolumeGroup volumeGroup) {
        AAIPluralResourceUri volumeGroupUri = AAIUriFactory.createNodesUri(AAIObjectPlurals.VOLUME_GROUP)
                .queryParam("volume-group-name", volumeGroup.getVolumeGroupName());
        return injectionHelper.getAaiClient().exists(volumeGroupUri);
    }
}
