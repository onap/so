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

import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.client.exception.BadResponseException;
import org.openecomp.mso.client.exception.MapperException;
import org.openecomp.mso.client.sdnc.SDNCClient;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcAction;
import org.openecomp.mso.client.sdnc.beans.SDNCSvcOperation;
import org.openecomp.mso.client.sdnc.endpoint.SDNCTopology;
import org.openecomp.mso.client.sdnc.mapper.ServiceTopologyOperationMapper;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class SDNCServiceInstanceResources {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL,
			SDNCServiceInstanceResources.class);

	@Autowired
	private ServiceTopologyOperationMapper sdncRM;
	
	@Autowired
	private SDNCClient sdncClient;
	
	/**
	 * SDNC call to perform Service Topology Assign for ServiceInsatnce 
	 * @param serviceInstance
	 * @param customer
	 * @param requestContext
	 * @throws MapperException
	 * @throws BadResponseException
	 * @return the response as a String
	 */
	public String assignServiceInstance(ServiceInstance serviceInstance, Customer customer,
			RequestContext requestContext) throws MapperException, BadResponseException {
		GenericResourceApiServiceOperationInformation sdncReq = sdncRM.reqMapper(
				SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN,GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE, serviceInstance, customer,
				requestContext);
		return sdncClient.post(sdncReq, SDNCTopology.SERVICE);
	}

	/**
	 * SDNC call to perform Service Topology Delete for ServiceInsatnce 
	 * @param serviceInstance
	 * @param customer
	 * @param requestContext
	 * @throws MapperException
	 * @throws BadResponseException
	 * @return the response as a String
	 */
	public String deleteServiceInstance(ServiceInstance serviceInstance, Customer customer,
			RequestContext requestContext) throws MapperException, BadResponseException {
		GenericResourceApiServiceOperationInformation sdncReq = sdncRM.reqMapper(
				SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.DELETE, GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE, serviceInstance, customer,
				requestContext);
		return sdncClient.post(sdncReq, SDNCTopology.SERVICE);
	}
	
	public String unassignServiceInstance(ServiceInstance serviceInstance, Customer customer,
			RequestContext requestContext) throws MapperException, BadResponseException {
		GenericResourceApiServiceOperationInformation sdncReq = sdncRM.reqMapper(
				SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.DELETE, GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE, serviceInstance, customer,
				requestContext);
		return sdncClient.post(sdncReq, SDNCTopology.SERVICE);
	}
	
	/**
	 * SDNC call to perform Service Topology Deactivate for ServiceInstance
	 * @param serviceInstance
	 * @param customer
	 * @param requestContext
	 * @throws MapperException
	 * @throws BadResponseException
	 * @return the response as a String
	 */
	public String deactivateServiceInstance(ServiceInstance serviceInstance, Customer customer,
			RequestContext requestContext) throws MapperException, BadResponseException {
		GenericResourceApiServiceOperationInformation sdncReq = sdncRM.reqMapper(
				SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.DEACTIVATE, GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE, serviceInstance, customer,
				requestContext);
		return sdncClient.post(sdncReq, SDNCTopology.SERVICE);
	}
	
	/**
	 * SDNC call to perform Service Topology Change Assign for the ServiceInstance 
	 * @param serviceInstance
	 * @param customer
	 * @param requestContext
	 * @throws MapperException
	 * @throws BadResponseException
	 * @return the response as a String
	 */
	public String changeModelServiceInstance(ServiceInstance serviceInstance, Customer customer, RequestContext requestContext) throws MapperException, BadResponseException {
		GenericResourceApiServiceOperationInformation sdncReq = sdncRM.reqMapper(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.CHANGE_ASSIGN, GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE, serviceInstance, customer, requestContext);
		return sdncClient.post(sdncReq, SDNCTopology.SERVICE);
	}
}
