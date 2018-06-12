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

package org.openecomp.mso.bpmn.infrastructure.adapter.network.tasks;

import java.util.Optional;

import org.openecomp.mso.adapters.nwrest.DeleteNetworkResponse;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.NetworkAdapterResources;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NetworkAdapterDeleteTasks {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, NetworkAdapterDeleteTasks.class);
	
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private NetworkAdapterResources networkAdapterResources;
	@Autowired
	private ExceptionBuilder exceptionUtil;
	
	public void deleteNetwork(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			
			L3Network l3Network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			
			String cloudRegionPo = execution.getVariable("cloudRegionPo");
			Optional<DeleteNetworkResponse> oDeleteNetworkResponse = networkAdapterResources.deleteNetwork(gBBInput.getRequestContext(), gBBInput.getCloudRegion(), serviceInstance, l3Network, cloudRegionPo);
			
			if (oDeleteNetworkResponse.isPresent()){
				DeleteNetworkResponse deleteNetworkResponse = oDeleteNetworkResponse.get();
				if (deleteNetworkResponse.getNetworkDeleted()) {
					execution.setVariable("deleteNetworkResponse", deleteNetworkResponse);
				}
			}			
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
