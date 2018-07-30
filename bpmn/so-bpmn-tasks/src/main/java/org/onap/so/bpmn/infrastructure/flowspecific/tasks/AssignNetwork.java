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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignNetwork {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL,
			AssignNetwork.class);
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;

	/**
	 * Check if there are collection(s)
	 * @param execution
	 * @return boolean
	 */
	public boolean hasCollection(BuildingBlockExecution execution) {
		boolean hasCollection = false;
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID,
					execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			if (serviceInstance.getCollection() != null){
				hasCollection = true;
				msoLogger.debug("there are collections to create");
			}
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
		return hasCollection;
	}
	
	/**
	 * Check if network was found by name
	 * @param execution
	 * @return
	 */
	public boolean networkFoundByName(BuildingBlockExecution execution) {
		boolean networkFound = false;
		try {
			L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID,
					execution.getLookupMap().get(ResourceKey.NETWORK_ID));

			if (!OrchestrationStatus.PRECREATED.equals(l3network.getOrchestrationStatus())){
				networkFound = true;
				msoLogger.debug("network found in NOT PRECREATED status");
			}
		} catch (Exception ex) {
			// return false if no network present
		}
		return networkFound;
	}
	
	/**
	 * BPMN access method. Return flag if BPMN flow should skip AAI interaction
	 * @param execution
	 * @return TRUE if network collection was NOT present OR network WAS found by name
	 */
	public boolean skipNetworkCreationInAAI(BuildingBlockExecution execution) {

		return !hasCollection(execution) || networkFoundByName(execution);
	}
}
