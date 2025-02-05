/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import org.onap.sdnc.northbound.client.model.GenericResourceApiNetworkOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.so.bpmn.infrastructure.sdnc.mapper.NetworkTopologyOperationRequestMapper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SDNCNetworkResources {

    @Autowired
    @Qualifier("NetworkTopologyOperationRequestMapper")
    private NetworkTopologyOperationRequestMapper sdncRM;

    public GenericResourceApiNetworkOperationInformation assignNetwork(L3Network network,
            ServiceInstance serviceInstance, Customer customer, RequestContext requestContext,
            CloudRegion cloudRegion) {
        return sdncRM.reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN,
                GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    public GenericResourceApiNetworkOperationInformation rollbackAssignNetwork(L3Network network,
            ServiceInstance serviceInstance, Customer customer, RequestContext requestContext,
            CloudRegion cloudRegion) {
        return sdncRM.reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.UNASSIGN,
                GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    public GenericResourceApiNetworkOperationInformation activateNetwork(L3Network network,
            ServiceInstance serviceInstance, Customer customer, RequestContext requestContext,
            CloudRegion cloudRegion) {
        return sdncRM.reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.ACTIVATE,
                GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    public GenericResourceApiNetworkOperationInformation deactivateNetwork(L3Network network,
            ServiceInstance serviceInstance, Customer customer, RequestContext requestContext,
            CloudRegion cloudRegion) {
        return sdncRM.reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.DEACTIVATE,
                GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    public GenericResourceApiNetworkOperationInformation deleteNetwork(L3Network network,
            ServiceInstance serviceInstance, Customer customer, RequestContext requestContext,
            CloudRegion cloudRegion) {
        return sdncRM.reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.DELETE,
                GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    public GenericResourceApiNetworkOperationInformation changeAssignNetwork(L3Network network,
            ServiceInstance serviceInstance, Customer customer, RequestContext requestContext,
            CloudRegion cloudRegion) {
        return sdncRM.reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.CHANGE_ASSIGN,
                GenericResourceApiRequestActionEnumeration.CREATENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }

    public GenericResourceApiNetworkOperationInformation unassignNetwork(L3Network network,
            ServiceInstance serviceInstance, Customer customer, RequestContext requestContext,
            CloudRegion cloudRegion) {
        return sdncRM.reqMapper(SDNCSvcOperation.NETWORK_TOPOLOGY_OPERATION, SDNCSvcAction.UNASSIGN,
                GenericResourceApiRequestActionEnumeration.DELETENETWORKINSTANCE, network, serviceInstance, customer,
                requestContext, cloudRegion);
    }
}
