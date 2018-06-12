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

package org.openecomp.mso.client.orchestration;

import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.openecomp.mso.client.exception.BadResponseException;
import org.openecomp.mso.client.exception.MapperException;
import org.openecomp.mso.client.sdnc.SDNCClient;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcOperation;
import org.openecomp.mso.client.sdnc.endpoint.SDNCTopology;
import org.openecomp.mso.client.sdnc.mapper.VfModuleTopologyOperationRequestMapper;
import org.openecomp.mso.logger.MsoLogger;
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
	
	public String queryVfModule(String sdncAssignResponse)
			throws MapperException, BadResponseException {
		
		String objectPath = sdncRM.buildObjectPath(sdncAssignResponse);
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
