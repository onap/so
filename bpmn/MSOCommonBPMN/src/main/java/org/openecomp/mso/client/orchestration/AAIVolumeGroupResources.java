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

import org.openecomp.mso.bpmn.common.InjectionHelper;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
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
public class AAIVolumeGroupResources {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, AAIVolumeGroupResources.class);
	
	@Autowired
	private InjectionHelper injectionHelper;
	
	@Autowired
	private AAIObjectMapper aaiObjectMapper;

	public void createVolumeGroup(VolumeGroup volumeGroup, CloudRegion cloudRegion) {
		AAIResourceUri uriVolumeGroup = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
		volumeGroup.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);
		injectionHelper.getAaiClient().create(uriVolumeGroup, aaiObjectMapper.mapVolumeGroup(volumeGroup));
	}
	
	public void updateOrchestrationStatusVolumeGroup(VolumeGroup volumeGroup, CloudRegion cloudRegion, OrchestrationStatus orchestrationStatus) {
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
		VolumeGroup copiedVolumeGroup = volumeGroup.shallowCopyId();

		volumeGroup.setOrchestrationStatus(orchestrationStatus);
		copiedVolumeGroup.setOrchestrationStatus(orchestrationStatus);
		injectionHelper.getAaiClient().update(uri, aaiObjectMapper.mapVolumeGroup(copiedVolumeGroup));
	}
	
	public void connectVolumeGroupToVnf(GenericVnf genericVnf, VolumeGroup volumeGroup, CloudRegion cloudRegion) {
		AAIResourceUri uriGenericVnf = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, genericVnf.getVnfId());
		AAIResourceUri uriVolumeGroup = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
		injectionHelper.getAaiClient().connect(uriGenericVnf, uriVolumeGroup);
	}
	
	public void deleteVolumeGroup(VolumeGroup volumeGroup, CloudRegion cloudRegion) {
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VOLUME_GROUP, cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId());
		injectionHelper.getAaiClient().delete(uri);
	}
}
