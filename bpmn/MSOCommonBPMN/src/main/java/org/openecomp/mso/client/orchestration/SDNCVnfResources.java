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
import org.openecomp.mso.client.exception.BadResponseException;
import org.openecomp.mso.client.exception.MapperException;
import org.openecomp.mso.client.sdnc.SDNCClient;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcOperation;
import org.openecomp.mso.client.sdnc.endpoint.SDNCTopology;
import org.openecomp.mso.client.sdnc.mapper.VnfTopologyOperationRequestMapper;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class SDNCVnfResources {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SDNCVnfResources.class);

	@Autowired
	private VnfTopologyOperationRequestMapper sdncRM;
	
	@Autowired
	private SDNCClient sdncClient;

	public String assignVnf(GenericVnf vnf, ServiceInstance serviceInstance, Customer customer,
			CloudRegion cloudRegion, RequestContext requestContext, boolean homing)
			throws MapperException, BadResponseException {
		GenericResourceApiVnfOperationInformation sdncReq = sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION,
				SDNCSvcAction.ASSIGN,GenericResourceApiRequestActionEnumeration.CREATEVNFINSTANCE, vnf, serviceInstance, customer, cloudRegion, requestContext, homing);
		return sdncClient.post(sdncReq, SDNCTopology.VNF);
	}

	public String activateVnf(GenericVnf vnf, ServiceInstance serviceInstance, Customer customer,
			CloudRegion cloudRegion, RequestContext requestContext)
			throws MapperException, BadResponseException {
		GenericResourceApiVnfOperationInformation sdncReq = sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION,
				SDNCSvcAction.ACTIVATE,GenericResourceApiRequestActionEnumeration.CREATEVNFINSTANCE, vnf, serviceInstance, customer,cloudRegion, requestContext, false);
		return sdncClient.post(sdncReq, SDNCTopology.VNF);
	}

	
	public String deactivateVnf(GenericVnf vnf, ServiceInstance serviceInstance, Customer customer,
			CloudRegion cloudRegion, RequestContext requestContext)
			throws MapperException, BadResponseException {
		GenericResourceApiVnfOperationInformation sdncReq = sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION,
				SDNCSvcAction.DEACTIVATE,GenericResourceApiRequestActionEnumeration.DELETEVNFINSTANCE, vnf, serviceInstance, customer,cloudRegion, requestContext, false);
		return sdncClient.post(sdncReq, SDNCTopology.VNF);
	}

  
	public String unassignVnf(GenericVnf vnf, ServiceInstance serviceInstance, Customer customer,
			CloudRegion cloudRegion, RequestContext requestContext)
			throws MapperException, BadResponseException {
		GenericResourceApiVnfOperationInformation sdncReq = sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION,
				SDNCSvcAction.UNASSIGN,GenericResourceApiRequestActionEnumeration.DELETEVNFINSTANCE, vnf, serviceInstance, customer, cloudRegion, requestContext, false);
		return sdncClient.post(sdncReq, SDNCTopology.VNF);
	}
	
	public String deleteVnf(GenericVnf vnf, ServiceInstance serviceInstance, Customer customer,
			CloudRegion cloudRegion, RequestContext requestContext)
			throws MapperException, BadResponseException {
		GenericResourceApiVnfOperationInformation sdncReq = sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION,
				SDNCSvcAction.DEACTIVATE, GenericResourceApiRequestActionEnumeration.DELETEVNFINSTANCE,vnf, serviceInstance, customer, cloudRegion, requestContext, false);
		return sdncClient.post(sdncReq, SDNCTopology.VNF);
	}
	
	public String changeModelVnf(GenericVnf vnf, ServiceInstance serviceInstance, Customer customer, 
			CloudRegion cloudRegion, RequestContext requestContext) 
			throws MapperException, BadResponseException {
		GenericResourceApiVnfOperationInformation sdncReq = sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION, 
				SDNCSvcAction.CHANGE_ASSIGN,GenericResourceApiRequestActionEnumeration.CREATEVNFINSTANCE, vnf, serviceInstance, customer, cloudRegion, requestContext, false);
		return sdncClient.post(sdncReq, SDNCTopology.VNF);
	}
	
	public String queryVnf(GenericVnf vnf) throws MapperException, BadResponseException {
		String queryPath = vnf.getSelflink();
		return sdncClient.get(queryPath);
	}
}
