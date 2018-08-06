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

import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.onap.so.client.sdnc.mapper.ServiceTopologyOperationMapper;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceOperationInformation;

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
