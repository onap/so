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

import java.util.Optional;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
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
public class AAIVfModuleResources {

    @Autowired
    private InjectionHelper injectionHelper;

    @Autowired
    private AAIObjectMapper aaiObjectMapper;

    public void createVfModule(VfModule vfModule, GenericVnf vnf) {
        AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnf.getVnfId(), vfModule.getVfModuleId());
        vfModule.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
        injectionHelper.getAaiClient().createIfNotExists(vfModuleURI,
                Optional.of(aaiObjectMapper.mapVfModule(vfModule)));
    }

    public void deleteVfModule(VfModule vfModule, GenericVnf vnf) {
        AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnf.getVnfId(), vfModule.getVfModuleId());
        injectionHelper.getAaiClient().delete(vfModuleURI);
    }

    public void updateOrchestrationStatusVfModule(VfModule vfModule, GenericVnf vnf,
            OrchestrationStatus orchestrationStatus) {
        AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnf.getVnfId(), vfModule.getVfModuleId());
        VfModule copiedVfModule = vfModule.shallowCopyId();

        vfModule.setOrchestrationStatus(orchestrationStatus);
        copiedVfModule.setOrchestrationStatus(orchestrationStatus);
        org.onap.aai.domain.yang.VfModule aaiVfModule = aaiObjectMapper.mapVfModule(copiedVfModule);
        injectionHelper.getAaiClient().update(vfModuleURI, aaiVfModule);
    }

    public void updateHeatStackIdVfModule(VfModule vfModule, GenericVnf vnf) {
        AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnf.getVnfId(), vfModule.getVfModuleId());
        VfModule copiedVfModule = vfModule.shallowCopyId();

        copiedVfModule.setHeatStackId(vfModule.getHeatStackId());
        org.onap.aai.domain.yang.VfModule aaiVfModule = aaiObjectMapper.mapVfModule(copiedVfModule);
        injectionHelper.getAaiClient().update(vfModuleURI, aaiVfModule);
    }

    public void updateContrailServiceInstanceFqdnVfModule(VfModule vfModule, GenericVnf vnf) {
        AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnf.getVnfId(), vfModule.getVfModuleId());
        VfModule copiedVfModule = vfModule.shallowCopyId();

        copiedVfModule.setContrailServiceInstanceFqdn(vfModule.getContrailServiceInstanceFqdn());
        org.onap.aai.domain.yang.VfModule aaiVfModule = aaiObjectMapper.mapVfModule(copiedVfModule);
        injectionHelper.getAaiClient().update(vfModuleURI, aaiVfModule);
    }

    public void changeAssignVfModule(VfModule vfModule, GenericVnf vnf) {
        AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnf.getVnfId(), vfModule.getVfModuleId());
        org.onap.aai.domain.yang.VfModule aaiVfModule = aaiObjectMapper.mapVfModule(vfModule);
        injectionHelper.getAaiClient().update(vfModuleURI, aaiVfModule);
    }

    public void connectVfModuleToVolumeGroup(GenericVnf vnf, VfModule vfModule, VolumeGroup volumeGroup,
            CloudRegion cloudRegion) {
        AAIResourceUri vfModuleURI =
                AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnf.getVnfId(), vfModule.getVfModuleId());
        AAIResourceUri volumeGroupURI = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP,
                cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
        injectionHelper.getAaiClient().connect(vfModuleURI, volumeGroupURI);
    }

    public boolean checkNameInUse(VfModule vfModule) {
        boolean nameInUse = false;
        AAIPluralResourceUri vfModuleUri = AAIUriFactory.createNodesUri(AAIObjectPlurals.VF_MODULE)
                .queryParam("vf-module-name", vfModule.getVfModuleName());
        AAIPluralResourceUri vfModuleUriWithCustomization = vfModuleUri.clone().queryParam("model-customization-id",
                vfModule.getModelInfoVfModule().getModelCustomizationUUID());
        if (injectionHelper.getAaiClient().exists(vfModuleUriWithCustomization)) {
            // assume it's a resume case and return false
            nameInUse = false;
        } else {
            nameInUse = injectionHelper.getAaiClient().exists(vfModuleUri);
        }
        return nameInUse;
    }
}
