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

package org.openecomp.mso.bpmn.infrastructure.adapter.vnf.tasks;

import org.openecomp.mso.adapters.vnfrest.DeleteVfModuleRequest;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.VnfAdapterVfModuleResources;
import org.openecomp.mso.client.orchestration.VnfAdapterVolumeGroupResources;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VnfAdapterDeleteTasks {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, VnfAdapterDeleteTasks.class);
	private static final String VNFREST_REQUEST = "VNFREST_Request";
	
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private VnfAdapterVolumeGroupResources vnfAdapterVolumeGroupResources;
	@Autowired
	private VnfAdapterVfModuleResources vnfAdapterVfModuleResources;
	@Autowired
	private ExceptionBuilder exceptionUtil;
	
	public void deleteVolumeGroup(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID, execution.getLookupMap().get(ResourceKey.VOLUME_GROUP_ID));
			
			vnfAdapterVolumeGroupResources.deleteVolumeGroup(gBBInput.getRequestContext(), gBBInput.getCloudRegion(), serviceInstance, volumeGroup);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	public void deleteVfModule(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
			GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			
			DeleteVfModuleRequest deleteVfModuleRequest = vnfAdapterVfModuleResources.deleteVfModuleRequest( gBBInput.getRequestContext(), gBBInput.getCloudRegion(), serviceInstance, genericVnf, vfModule);
			execution.setVariable(VNFREST_REQUEST, deleteVfModuleRequest.toXmlString());
			execution.setVariable("deleteVfModuleRequest", "true");
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
