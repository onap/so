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

package org.openecomp.mso.bpmn.infrastructure.sdnc.tasks;

import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.SDNCServiceInstanceResources;
import org.openecomp.mso.client.orchestration.SDNCVfModuleResources;
import org.openecomp.mso.client.orchestration.SDNCVnfResources;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.client.orchestration.SDNCNetworkResources;

@Component
public class SDNCUnassignTasks {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SDNCUnassignTasks.class);
	@Autowired
	private SDNCServiceInstanceResources sdncSIResources;
	@Autowired
	private SDNCVfModuleResources sdncVfModuleResources;
	@Autowired
	private SDNCVnfResources sdncVnfResources;
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private SDNCNetworkResources sdncNetworkResources;

	public void unassignServiceInstance(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID)); 
			
			if (serviceInstance.getOrchestrationStatus() == OrchestrationStatus.INVENTORIED) {
				return;  // If INVENTORIED then SDNC unassign is not necessary
			}
			
			RequestContext requestContext = gBBInput.getRequestContext();
			Customer customer = gBBInput.getCustomer();
			String sdncUnassignResponse = sdncSIResources.unassignServiceInstance(serviceInstance, customer, requestContext);
			execution.setVariable("sdncUnassignResponse", sdncUnassignResponse);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void unassignVfModule(BuildingBlockExecution execution) {		
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID)); 
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID)); 
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
			
			if (OrchestrationStatus.INVENTORIED == vfModule.getOrchestrationStatus() || OrchestrationStatus.PENDING_CREATE == vfModule.getOrchestrationStatus()) {
				return;  // If INVENTORIED or PENDING_CREATE then SDNC unassign is not necessary
			}
			
			execution.setVariable("sdncVfModuleRollback", false);
		
			String response = sdncVfModuleResources.unassignVfModule(vfModule, vnf, serviceInstance);		
			execution.setVariable("SDNCResponse", response);
			execution.setVariable("sdncVfModuleRollback", true);
		} catch (Exception ex) {			
		    exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void unassignVnf(BuildingBlockExecution execution)  {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			
			if (OrchestrationStatus.INVENTORIED == vnf.getOrchestrationStatus() || OrchestrationStatus.CREATED == vnf.getOrchestrationStatus()) {
				return; // If INVENTORIED or CREATED then SDNC unassign is not necessary
			}
			
			RequestContext requestContext = gBBInput.getRequestContext();
			Customer customer = gBBInput.getCustomer();
			CloudRegion cloudRegion = gBBInput.getCloudRegion();
			execution.setVariable("sdncVnfRollback", false);
			String response = sdncVnfResources.unassignVnf(vnf, serviceInstance, customer, cloudRegion, requestContext);
			execution.setVariable("sdncUnassignVnfResponse", response);
			execution.setVariable("sdncVnfRollback", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	public void unassignNetwork(BuildingBlockExecution execution) throws Exception {
		GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
		L3Network network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
		
		if (OrchestrationStatus.INVENTORIED == network.getOrchestrationStatus()) {
			return; // If INVENTORIED then SDNC unassign is not necessary
		}
		
		ServiceInstance serviceInstance =  extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
		Customer customer = gBBInput.getCustomer();
		RequestContext requestContext = gBBInput.getRequestContext();		
		CloudRegion cloudRegion = gBBInput.getCloudRegion();
		String cloudRegionSdnc = execution.getVariable("cloudRegionSdnc");
		cloudRegion.setLcpCloudRegionId(cloudRegionSdnc);
		try {
			String response = sdncNetworkResources.unassignNetwork(network, serviceInstance, customer, requestContext, cloudRegion);
			execution.setVariable("SDNCUnAssignNetworkResponse", response);
			execution.setVariable("isRollbackNeeded", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
