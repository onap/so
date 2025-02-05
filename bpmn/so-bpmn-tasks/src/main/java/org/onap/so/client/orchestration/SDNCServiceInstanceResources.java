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

import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceOperationInformation;
import org.onap.so.bpmn.infrastructure.sdnc.mapper.ServiceTopologyOperationMapper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SDNCServiceInstanceResources {

    @Autowired
    @Qualifier("serviceTopologyOperationMapper")
    private ServiceTopologyOperationMapper sdncRM;

    /**
     * SDNC call to perform Service Topology Assign for ServiceInsatnce
     * 
     * @param serviceInstance
     * @param customer
     * @param requestContext
     * @throws MapperException
     * @throws BadResponseException
     * @return the response as a String
     */
    public GenericResourceApiServiceOperationInformation assignServiceInstance(ServiceInstance serviceInstance,
            Customer customer, RequestContext requestContext) {
        return sdncRM.reqMapper(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN,
                GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE, serviceInstance, customer,
                requestContext);
    }

    /**
     * SDNC call to perform Service Topology Delete for ServiceInsatnce
     * 
     * @param serviceInstance
     * @param customer
     * @param requestContext
     * @throws MapperException
     * @throws BadResponseException
     * @return the response as a String
     */
    public GenericResourceApiServiceOperationInformation deleteServiceInstance(ServiceInstance serviceInstance,
            Customer customer, RequestContext requestContext) {
        return sdncRM.reqMapper(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.DELETE,
                GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE, serviceInstance, customer,
                requestContext);
    }

    public GenericResourceApiServiceOperationInformation unassignServiceInstance(ServiceInstance serviceInstance,
            Customer customer, RequestContext requestContext) {
        return sdncRM.reqMapper(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.DELETE,
                GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE, serviceInstance, customer,
                requestContext);
    }

    /**
     * SDNC call to perform Service Topology Deactivate for ServiceInstance
     * 
     * @param serviceInstance
     * @param customer
     * @param requestContext
     * @throws MapperException
     * @throws BadResponseException
     * @return the response as a String
     */
    public GenericResourceApiServiceOperationInformation deactivateServiceInstance(ServiceInstance serviceInstance,
            Customer customer, RequestContext requestContext) {
        return sdncRM.reqMapper(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.DEACTIVATE,
                GenericResourceApiRequestActionEnumeration.DELETESERVICEINSTANCE, serviceInstance, customer,
                requestContext);
    }

    /**
     * SDNC call to perform Service Topology Change Assign for the ServiceInstance
     * 
     * @param serviceInstance
     * @param customer
     * @param requestContext
     * @throws MapperException
     * @throws BadResponseException
     * @return the response as a String
     */
    public GenericResourceApiServiceOperationInformation changeModelServiceInstance(ServiceInstance serviceInstance,
            Customer customer, RequestContext requestContext) {
        return sdncRM.reqMapper(SDNCSvcOperation.SERVICE_TOPOLOGY_OPERATION, SDNCSvcAction.CHANGE_ASSIGN,
                GenericResourceApiRequestActionEnumeration.CREATESERVICEINSTANCE, serviceInstance, customer,
                requestContext);
    }
}
