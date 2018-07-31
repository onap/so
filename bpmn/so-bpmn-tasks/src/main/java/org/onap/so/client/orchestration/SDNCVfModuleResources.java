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

package org.onap.so.client.orchestration;

import org.onap.sdnc.apps.client.model.GenericResourceApiVfModuleOperationInformation;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.onap.so.client.sdnc.mapper.VfModuleTopologyOperationRequestMapper;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SDNCVfModuleResources {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SDNCVfModuleResources.class);
	
	@Autowired
	private VfModuleTopologyOperationRequestMapper sdncRM;
	
	@Autowired
	private SDNCClient sdncClient;
	
	public String assignVfModule(VfModule vfModule, VolumeGroup volumeGroup,GenericVnf vnf, ServiceInstance serviceInstance, Customer customer,
			CloudRegion cloudRegion, RequestContext requestContext)
			throws MapperException, BadResponseException {
		
		GenericResourceApiVfModuleOperationInformation sdncReq = sdncRM.reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
				SDNCSvcAction.ASSIGN, vfModule, volumeGroup, vnf, serviceInstance, customer, cloudRegion, requestContext, null);
		return sdncClient.post(sdncReq, SDNCTopology.VFMODULE);
	}

	public String unassignVfModule(VfModule vfModule, GenericVnf vnf, ServiceInstance serviceInstance)
			throws MapperException, BadResponseException {
		
		GenericResourceApiVfModuleOperationInformation sdncReq = sdncRM.reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
				SDNCSvcAction.UNASSIGN, vfModule, null, vnf, serviceInstance, null, null, null, null);
		return sdncClient.post(sdncReq, SDNCTopology.VFMODULE);
	}

	public String deactivateVfModule(VfModule vfModule, GenericVnf vnf, ServiceInstance serviceInstance,
			Customer customer, CloudRegion cloudRegion, RequestContext requestContext)
			throws MapperException, BadResponseException {

		GenericResourceApiVfModuleOperationInformation sdncReq = sdncRM.reqMapper(
				SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION, SDNCSvcAction.DEACTIVATE, vfModule, null, vnf, serviceInstance,
				customer, cloudRegion, requestContext, null);
		return sdncClient.post(sdncReq, SDNCTopology.VFMODULE);
	}
	
	public String queryVfModule(VfModule vfModule)
			throws MapperException, BadResponseException {
		
		String objectPath = vfModule.getSelflink();		
		return sdncClient.get(objectPath);
	}
	
	public String activateVfModule(VfModule vfModule, GenericVnf vnf, ServiceInstance serviceInstance, Customer customer,
			CloudRegion cloudRegion, RequestContext requestContext)
			throws MapperException, BadResponseException {
		
		GenericResourceApiVfModuleOperationInformation sdncReq = sdncRM.reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION,
				SDNCSvcAction.ACTIVATE, vfModule, null, vnf, serviceInstance, customer, cloudRegion, requestContext, null);
		return sdncClient.post(sdncReq, SDNCTopology.VFMODULE);
	}
	
	public String changeAssignVfModule(VfModule vfModule, GenericVnf vnf, ServiceInstance serviceInstance, Customer customer, CloudRegion cloudRegion, RequestContext requestContext) throws MapperException, BadResponseException {
		GenericResourceApiVfModuleOperationInformation sdncReq = sdncRM.reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION, SDNCSvcAction.CHANGE_ASSIGN, vfModule, null, vnf, serviceInstance, customer, cloudRegion, requestContext, null);
		return sdncClient.post(sdncReq, SDNCTopology.VFMODULE);
	}
}
