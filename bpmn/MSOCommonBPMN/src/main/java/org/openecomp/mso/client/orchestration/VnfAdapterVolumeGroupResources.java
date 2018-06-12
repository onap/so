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

import org.openecomp.mso.adapters.vnfrest.CreateVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupRequest;
import org.openecomp.mso.adapters.vnfrest.DeleteVolumeGroupResponse;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.OrchestrationContext;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.client.adapter.vnf.VnfVolumeAdapterClientImpl;
import org.openecomp.mso.client.adapter.vnf.mapper.VnfAdapterObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VnfAdapterVolumeGroupResources {

	@Autowired
	private VnfAdapterObjectMapper vnfAdapterObjectMapper;
	
	@Autowired
	private VnfVolumeAdapterClientImpl vnfVolumeAdapterClient;
	
	public CreateVolumeGroupRequest createVolumeGroupRequest(RequestContext requestContext, CloudRegion cloudRegion, OrchestrationContext orchestrationContext, ServiceInstance serviceInstance, GenericVnf genericVnf, VolumeGroup volumeGroup, String sdncVfModuleQueryResponse) throws Exception {
		return vnfAdapterObjectMapper.createVolumeGroupRequestMapper(requestContext, cloudRegion, orchestrationContext, serviceInstance, genericVnf, volumeGroup, sdncVfModuleQueryResponse);
	}
	
	public DeleteVolumeGroupResponse deleteVolumeGroup(RequestContext requestContext, CloudRegion cloudRegion, ServiceInstance serviceInstance, VolumeGroup volumeGroup) throws Exception {
		DeleteVolumeGroupRequest deleteVolumeGroupRequest = vnfAdapterObjectMapper.deleteVolumeGroupRequestMapper(requestContext, cloudRegion, serviceInstance, volumeGroup);

		return vnfVolumeAdapterClient.deleteVNFVolumes(volumeGroup.getVolumeGroupId(), deleteVolumeGroupRequest);
	}
}
