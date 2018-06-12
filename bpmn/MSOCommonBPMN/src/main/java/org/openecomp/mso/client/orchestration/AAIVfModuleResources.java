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

package org.openecomp.mso.client.orchestration;

import java.util.Optional;

import org.openecomp.mso.bpmn.common.InjectionHelper;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.aai.mapper.AAIObjectMapper;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIVfModuleResources {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, AAIVfModuleResources.class);
	
	@Autowired
	private InjectionHelper injectionHelper;
	
	@Autowired
	private AAIObjectMapper aaiObjectMapper;

	public void createVfModule(VfModule vfModule, GenericVnf vnf) {
		AAIResourceUri vfModuleURI = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnf.getVnfId(), vfModule.getVfModuleId());
		vfModule.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);
		injectionHelper.getAaiClient().createIfNotExists(vfModuleURI, Optional.of(aaiObjectMapper.mapVfModule(vfModule)));
	}
	
	public void deleteVfModule(VfModule vfModule, GenericVnf vnf) {
		AAIResourceUri vfModuleURI = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnf.getVnfId(), vfModule.getVfModuleId());
		injectionHelper.getAaiClient().delete(vfModuleURI);
	}

	public void updateOrchestrationStatusVfModule(VfModule vfModule, OrchestrationStatus orchestrationStatus) {
		AAIResourceUri vfModuleURI = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vfModule.getVfModuleId());
		VfModule copiedVfModule = vfModule.shallowCopyId();

		vfModule.setOrchestrationStatus(orchestrationStatus);
		copiedVfModule.setOrchestrationStatus(orchestrationStatus);
		org.onap.aai.domain.yang.VfModule aaiVfModule = aaiObjectMapper.mapVfModule(copiedVfModule);
		injectionHelper.getAaiClient().update(vfModuleURI, aaiVfModule);
	}
	
	public void changeAssignVfModule(VfModule vfModule, GenericVnf vnf) {
		AAIResourceUri vfModuleURI = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnf.getVnfId(), vfModule.getVfModuleId());
		org.onap.aai.domain.yang.VfModule AAIVfModule = aaiObjectMapper.mapVfModule(vfModule);
		injectionHelper.getAaiClient().update(vfModuleURI, AAIVfModule);
	}
	
	public void connectVfModuleToVolumeGroup(GenericVnf vnf, VfModule vfModule, VolumeGroup volumeGroup, CloudRegion cloudRegion) {
		AAIResourceUri vfModuleURI = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnf.getVnfId(), vfModule.getVfModuleId());
		AAIResourceUri volumeGroupURI = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
		injectionHelper.getAaiClient().connect(vfModuleURI, volumeGroupURI);
	}
}
