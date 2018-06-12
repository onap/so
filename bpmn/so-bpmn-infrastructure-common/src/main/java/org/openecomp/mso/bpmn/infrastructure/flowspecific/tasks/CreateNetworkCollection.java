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

import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Collection;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.AAINetworkResources;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateNetworkCollection {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, CreateNetworkCollection.class);
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private AAINetworkResources aaiNetworkResources;

	private static String UNDERSCORE = "_";
	
	/**
	 * BPMN access method to build Network Collection Name
	 * @param execution
	 * @throws Exception
	 */
	public void buildNetworkCollectionName(BuildingBlockExecution execution) throws Exception {
		try{
			ServiceInstance serviceInstance =  extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			InstanceGroup instanceGroup =  serviceInstance.getCollection().getInstanceGroup();
			//Build collection name assembling SI name and IG function
			String networkCollectionName = serviceInstance.getServiceInstanceName().concat(UNDERSCORE).concat(instanceGroup.getInstanceGroupFunction());
			//set networkCollectionName object on execution to be re-used within current BB
			execution.setVariable("networkCollectionName", networkCollectionName);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	/**
	 * BPMN access method to connect Network Collection
	 * @param execution
	 * @throws Exception
	 */
	public void connectCollectionToInstanceGroup(BuildingBlockExecution execution) throws Exception {
		execution.setVariable("connectCollectionToInstanceGroupRollback", false);
		try{
			ServiceInstance serviceInstance =  extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			Collection networkCollection =  serviceInstance.getCollection();
			aaiNetworkResources.connectNetworkCollectionInstanceGroupToNetworkCollection(networkCollection.getInstanceGroup(), networkCollection);
			execution.setVariable("connectCollectionToInstanceGroupRollback", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	/**
	 * BPMN access method to connect Network Collection
	 * @param execution
	 * @throws Exception
	 */
	public void connectCollectionToServiceInstance(BuildingBlockExecution execution) throws Exception {
		execution.setVariable("connectCollectionToServiceInstanceRollback", false);
		try{
			ServiceInstance serviceInstance =  extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			Collection networkCollection =  serviceInstance.getCollection();
			aaiNetworkResources.connectNetworkCollectionToServiceInstance(networkCollection, serviceInstance);
			execution.setVariable("connectCollectionToServiceInstanceRollback", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
