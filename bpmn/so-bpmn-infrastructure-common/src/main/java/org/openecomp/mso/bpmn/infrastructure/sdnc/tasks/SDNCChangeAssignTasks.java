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
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.SDNCNetworkResources;
import org.openecomp.mso.client.orchestration.SDNCServiceInstanceResources;
import org.openecomp.mso.client.orchestration.SDNCVfModuleResources;
import org.openecomp.mso.client.orchestration.SDNCVnfResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SDNCChangeAssignTasks {
	@Autowired
	private SDNCNetworkResources sdncNetworkResources;
	@Autowired
	private SDNCServiceInstanceResources sdncServiceInstanceResources;
	@Autowired
	private SDNCVnfResources sdncVnfResources;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private SDNCVfModuleResources sdncVfModuleResources;
	
	public void changeModelServiceInstance(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			
			String response = sdncServiceInstanceResources.changeModelServiceInstance(serviceInstance, gBBInput.getCustomer(), gBBInput.getRequestContext());
			execution.setVariable("SDNCChangeAssignTasks.changeModelServiceInstance.response", response);
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void changeModelVnf(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			
			String response = sdncVnfResources.changeModelVnf(genericVnf, serviceInstance, gBBInput.getCustomer(), gBBInput.getCloudRegion(), gBBInput.getRequestContext());
			execution.setVariable("SDNCChangeModelVnfResponse", response);
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void changeAssignNetwork(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			L3Network network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			
			String sdncResponse = sdncNetworkResources.changeAssignNetwork(network, serviceInstance, gBBInput.getCustomer(), gBBInput.getRequestContext(), gBBInput.getCloudRegion());
			execution.setVariable("SDNCChangeAssignNetworkResponse", sdncResponse);
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void changeAssignModelVfModule(BuildingBlockExecution execution) throws Exception {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			RequestContext requestContext = gBBInput.getRequestContext();
			CloudRegion cloudRegion = gBBInput.getCloudRegion();
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
			Customer customer = gBBInput.getCustomer();
			String response = sdncVfModuleResources.changeAssignVfModule(vfModule, vnf, serviceInstance, customer, cloudRegion, requestContext);
			execution.setVariable("SDNCChangeAssignVfModuleResponse", response);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
