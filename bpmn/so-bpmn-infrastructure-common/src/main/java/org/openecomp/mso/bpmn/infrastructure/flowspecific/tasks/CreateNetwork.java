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
package org.openecomp.mso.bpmn.infrastructure.flowspecific.tasks;

import java.util.Map;

import org.openecomp.mso.adapters.nwrest.CreateNetworkRequest;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.adapter.network.mapper.NetworkAdapterObjectMapper;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateNetwork {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CreateNetwork.class);
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private NetworkAdapterObjectMapper networkAdapterObjectMapper;
	
	/**
	 * BPMN access method to build CreateNetworkRequest object
	 * 
	 */
	public void buildCreateNetworkRequest(BuildingBlockExecution execution) throws Exception {
		try{
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			ServiceInstance serviceInstance =  extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			L3Network l3Network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
			Map<String, String> userInput = gBBInput.getUserInput();
			String cloudRegionPo = execution.getVariable("cloudRegionPo");
			
			CreateNetworkRequest createNetworkRequest = networkAdapterObjectMapper.createNetworkRequestMapper(gBBInput.getRequestContext(), gBBInput.getCloudRegion(),  gBBInput.getOrchContext(), serviceInstance, l3Network, userInput, cloudRegionPo, gBBInput.getCustomer());
			
			//set CreateNetowrkRequest object on execution to be re-used within current BB
			execution.setVariable("createNetworkRequest", createNetworkRequest);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
