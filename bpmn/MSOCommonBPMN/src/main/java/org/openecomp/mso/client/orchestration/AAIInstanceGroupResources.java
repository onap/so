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
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.aai.mapper.AAIObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIInstanceGroupResources {
	@Autowired
	private InjectionHelper injectionHelper;
	
	@Autowired
	private AAIObjectMapper aaiObjectMapper;

	public void createInstanceGroup(InstanceGroup instanceGroup) {
		AAIResourceUri instanceGroupUri = AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId());
		org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = aaiObjectMapper.mapInstanceGroup(instanceGroup);
		injectionHelper.getAaiClient().createIfNotExists(instanceGroupUri, Optional.of(aaiInstanceGroup));
	}
	
	public void deleteInstanceGroup(InstanceGroup instanceGroup) {
		AAIResourceUri instanceGroupUri = AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId());
		injectionHelper.getAaiClient().delete(instanceGroupUri);
	}

	public void connectInstanceGroupToVnf(InstanceGroup instanceGroup, GenericVnf vnf) {
		AAIResourceUri instanceGroupUri = AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId());
		AAIResourceUri vnfURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnf.getVnfId());
		injectionHelper.getAaiClient().connect(instanceGroupUri, vnfURI);
	}
	
	public boolean exists(InstanceGroup instanceGroup) {
		AAIResourceUri instanceGroupUri = AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroup.getId());
		return injectionHelper.getAaiClient().exists(instanceGroupUri);
	}

}
