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

package org.onap.so.bpmn.infrastructure.sdnc.tasks;

import org.onap.sdnc.northbound.client.model.GenericResourceApiNetworkOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfModuleOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfOperationInformation;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.SDNCNetworkResources;
import org.onap.so.client.orchestration.SDNCServiceInstanceResources;
import org.onap.so.client.orchestration.SDNCVfModuleResources;
import org.onap.so.client.orchestration.SDNCVnfResources;
import org.onap.so.client.sdnc.beans.SDNCRequest;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
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
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
			GenericResourceApiServiceOperationInformation req = sdncServiceInstanceResources.changeModelServiceInstance(serviceInstance, gBBInput.getCustomer(), gBBInput.getRequestContext());
			SDNCRequest sdncRequest = new SDNCRequest();
			sdncRequest.setSDNCPayload(req);
			sdncRequest.setTopology(SDNCTopology.SERVICE);
			execution.setVariable("SDNCRequest", sdncRequest);
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void changeModelVnf(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
			GenericResourceApiVnfOperationInformation req = sdncVnfResources.changeModelVnf(genericVnf, serviceInstance, gBBInput.getCustomer(), gBBInput.getCloudRegion(), gBBInput.getRequestContext());
			SDNCRequest sdncRequest = new SDNCRequest();
			sdncRequest.setSDNCPayload(req);
			sdncRequest.setTopology(SDNCTopology.VNF);
			execution.setVariable("SDNCRequest", sdncRequest);
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void changeAssignNetwork(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			L3Network network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
			GenericResourceApiNetworkOperationInformation req = sdncNetworkResources.changeAssignNetwork(network, serviceInstance, gBBInput.getCustomer(), gBBInput.getRequestContext(), gBBInput.getCloudRegion());
			SDNCRequest sdncRequest = new SDNCRequest();
			sdncRequest.setSDNCPayload(req);
			sdncRequest.setTopology(SDNCTopology.NETWORK);
			execution.setVariable("SDNCRequest", sdncRequest);
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void changeAssignModelVfModule(BuildingBlockExecution execution) throws Exception {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			RequestContext requestContext = gBBInput.getRequestContext();
			CloudRegion cloudRegion = gBBInput.getCloudRegion();
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
			Customer customer = gBBInput.getCustomer();
			GenericResourceApiVfModuleOperationInformation req = sdncVfModuleResources.changeAssignVfModule(vfModule, vnf, serviceInstance, customer, cloudRegion, requestContext);
			SDNCRequest sdncRequest = new SDNCRequest();
			sdncRequest.setSDNCPayload(req);
			sdncRequest.setTopology(SDNCTopology.VFMODULE);
			execution.setVariable("SDNCRequest", sdncRequest);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
