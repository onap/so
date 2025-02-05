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

import java.net.URI;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfOperationInformation;
import org.onap.so.bpmn.infrastructure.sdnc.mapper.VnfTopologyOperationRequestMapper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
public class SDNCVnfResources {

    @Autowired
    @Qualifier("VnfTopologyOperationRequestMapper")
    private VnfTopologyOperationRequestMapper sdncRM;

    @Autowired
    private SDNCClient sdncClient;

    /**
     * This method is used for setting the SDNCSvcAction for assignVnf .
     *
     * @param vnf
     * @param serviceInstance
     * @param customer
     * @param cloudRegion
     * @param requestContext
     * @param homing
     * @return
     */
    public GenericResourceApiVnfOperationInformation assignVnf(GenericVnf vnf, ServiceInstance serviceInstance,
            Customer customer, CloudRegion cloudRegion, RequestContext requestContext, boolean homing,
            URI callbackURI) {
        return sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN,
                GenericResourceApiRequestActionEnumeration.CREATEVNFINSTANCE, vnf, serviceInstance, customer,
                cloudRegion, requestContext, homing, callbackURI);
    }

    /**
     * This method is used for setting the SDNCSvcAction for activate vnf.
     *
     * @param vnf
     * @param serviceInstance
     * @param customer
     * @param cloudRegion
     * @param requestContext
     * @param homing
     * @return
     */
    public GenericResourceApiVnfOperationInformation activateVnf(GenericVnf vnf, ServiceInstance serviceInstance,
            Customer customer, CloudRegion cloudRegion, RequestContext requestContext, URI callbackURI) {
        return sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION, SDNCSvcAction.ACTIVATE,
                GenericResourceApiRequestActionEnumeration.CREATEVNFINSTANCE, vnf, serviceInstance, customer,
                cloudRegion, requestContext, false, callbackURI);
    }

    /**
     * This method is used for setting the SDNCSvcAction for deactivate vnf.
     *
     * @param vnf
     * @param serviceInstance
     * @param customer
     * @param cloudRegion
     * @param requestContext
     * @param homing
     * @return
     */
    public GenericResourceApiVnfOperationInformation deactivateVnf(GenericVnf vnf, ServiceInstance serviceInstance,
            Customer customer, CloudRegion cloudRegion, RequestContext requestContext, URI callbackURI) {
        return sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION, SDNCSvcAction.DEACTIVATE,
                GenericResourceApiRequestActionEnumeration.DELETEVNFINSTANCE, vnf, serviceInstance, customer,
                cloudRegion, requestContext, false, callbackURI);
    }

    /**
     * This method is used for setting the SDNCSvcAction for unassign vnf.
     *
     * @param vnf
     * @param serviceInstance
     * @param customer
     * @param cloudRegion
     * @param requestContext
     * @param homing
     * @return
     */
    public GenericResourceApiVnfOperationInformation unassignVnf(GenericVnf vnf, ServiceInstance serviceInstance,
            Customer customer, CloudRegion cloudRegion, RequestContext requestContext, URI callbackURI) {
        return sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION, SDNCSvcAction.UNASSIGN,
                GenericResourceApiRequestActionEnumeration.DELETEVNFINSTANCE, vnf, serviceInstance, customer,
                cloudRegion, requestContext, false, callbackURI);
    }

    /**
     * This method is used for setting the SDNCSvcAction for delete vnf.
     *
     * @param vnf
     * @param serviceInstance
     * @param customer
     * @param cloudRegion
     * @param requestContext
     * @param homing
     * @return
     */
    public GenericResourceApiVnfOperationInformation deleteVnf(GenericVnf vnf, ServiceInstance serviceInstance,
            Customer customer, CloudRegion cloudRegion, RequestContext requestContext, URI callbackURI) {
        return sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION, SDNCSvcAction.DEACTIVATE,
                GenericResourceApiRequestActionEnumeration.DELETEVNFINSTANCE, vnf, serviceInstance, customer,
                cloudRegion, requestContext, false, callbackURI);
    }

    /**
     * This method is used for setting the SDNCSvcAction for changeModelVnf.
     *
     * @param vnf
     * @param serviceInstance
     * @param customer
     * @param cloudRegion
     * @param requestContext
     * @param homing
     * @return
     */
    public GenericResourceApiVnfOperationInformation changeModelVnf(GenericVnf vnf, ServiceInstance serviceInstance,
            Customer customer, CloudRegion cloudRegion, RequestContext requestContext, URI callbackURI) {
        return sdncRM.reqMapper(SDNCSvcOperation.VNF_TOPOLOGY_OPERATION, SDNCSvcAction.CHANGE_ASSIGN,
                GenericResourceApiRequestActionEnumeration.CREATEVNFINSTANCE, vnf, serviceInstance, customer,
                cloudRegion, requestContext, false, callbackURI);
    }

    /**
     * This method is used for querying SDNC client for getting the vnf details.
     *
     * @param vnf
     * @exception MapperException & BadResponseException
     * @return
     */
    public String queryVnf(GenericVnf vnf) throws MapperException, BadResponseException {
        String queryPath = vnf.getSelflink();
        return sdncClient.get(queryPath);
    }
}
