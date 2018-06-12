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
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.openecomp.mso.client.exception.BBObjectNotFoundException;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.SDNCNetworkResources;
import org.openecomp.mso.client.orchestration.SDNCVfModuleResources;
import org.openecomp.mso.client.orchestration.SDNCVnfResources;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SDNCActivateTasks {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SDNCActivateTasks.class);
	@Autowired
	private SDNCVnfResources sdncVnfResources;
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private SDNCNetworkResources sdncNetworkResources;
	@Autowired
	private SDNCVfModuleResources sdncVfModuleResources;


	public void activateVnf(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			RequestContext requestContext = gBBInput.getRequestContext();
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			CloudRegion cloudRegion = gBBInput.getCloudRegion();
			Customer customer = gBBInput.getCustomer();
			String response = sdncVnfResources.activateVnf(vnf, serviceInstance, customer, cloudRegion, requestContext);
			execution.setVariable("SDNCResponse", response);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	/**
	 * BPMN access method to perform Assign action on SDNC for L3Network
	 * @param execution
	 * @throws BBObjectNotFoundException 
	 */
	public void activateNetwork(BuildingBlockExecution execution) throws BBObjectNotFoundException {
		execution.setVariable("sdncNetworkActivateRollback", false);
		GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
		
		L3Network l3network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
		ServiceInstance serviceInstance =  extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));

		Customer customer = gBBInput.getCustomer();
		RequestContext requestContext = gBBInput.getRequestContext();
		CloudRegion cloudRegion = gBBInput.getCloudRegion();
		try {
			sdncNetworkResources.activateNetwork(l3network, serviceInstance, customer, requestContext, cloudRegion);
			execution.setVariable("sdncNetworkActivateRollback", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void activateVfModule(BuildingBlockExecution execution) {
		GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
		RequestContext requestContext = gBBInput.getRequestContext();
		ServiceInstance serviceInstance = null;
		GenericVnf vnf = null;
		VfModule vfModule = null;
		try {
			serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID,
					execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID,
					execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID,
					execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
			Customer customer = gBBInput.getCustomer();
			CloudRegion cloudRegion = gBBInput.getCloudRegion();
			execution.setVariable("sdncActivateVfModuleRollback", false);

			String response = sdncVfModuleResources.activateVfModule(vfModule, vnf, serviceInstance, customer,
					cloudRegion, requestContext);
			execution.setVariable("SDNCActivateVfModuleResponse", response);
			execution.setVariable("sdncActivateVfModuleRollback", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
