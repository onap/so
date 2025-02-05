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
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfModuleOperationInformation;
import org.onap.so.bpmn.infrastructure.sdnc.mapper.VfModuleTopologyOperationRequestMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SDNCVfModuleResources {

    @Autowired
    @Qualifier("VfModuleTopologyOperationRequestMapper")
    private VfModuleTopologyOperationRequestMapper sdncRM;

    @Autowired
    private SDNCClient sdncClient;

    public GenericResourceApiVfModuleOperationInformation assignVfModule(VfModule vfModule, VolumeGroup volumeGroup,
            GenericVnf vnf, ServiceInstance serviceInstance, Customer customer, CloudRegion cloudRegion,
            RequestContext requestContext, URI callbackURI) throws MapperException {
        return sdncRM.reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION, SDNCSvcAction.ASSIGN, vfModule,
                volumeGroup, vnf, serviceInstance, customer, cloudRegion, requestContext, null, callbackURI);
    }

    public GenericResourceApiVfModuleOperationInformation unassignVfModule(VfModule vfModule, GenericVnf vnf,
            ServiceInstance serviceInstance, RequestContext requestContext, URI callbackURI) throws MapperException {
        return sdncRM.reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION, SDNCSvcAction.UNASSIGN, vfModule, null,
                vnf, serviceInstance, null, null, requestContext, null, callbackURI);
    }

    public GenericResourceApiVfModuleOperationInformation deactivateVfModule(VfModule vfModule, GenericVnf vnf,
            ServiceInstance serviceInstance, Customer customer, CloudRegion cloudRegion, RequestContext requestContext,
            URI callbackURI) throws MapperException {
        return sdncRM.reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION, SDNCSvcAction.DEACTIVATE, vfModule, null,
                vnf, serviceInstance, customer, cloudRegion, requestContext, null, callbackURI);
    }

    public String queryVfModule(VfModule vfModule) throws MapperException, BadResponseException {
        String objectPath = vfModule.getSelflink();
        return sdncClient.get(objectPath);
    }

    public GenericResourceApiVfModuleOperationInformation activateVfModule(VfModule vfModule, GenericVnf vnf,
            ServiceInstance serviceInstance, Customer customer, CloudRegion cloudRegion, RequestContext requestContext,
            URI callbackURI) throws MapperException {
        return sdncRM.reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION, SDNCSvcAction.ACTIVATE, vfModule, null,
                vnf, serviceInstance, customer, cloudRegion, requestContext, null, callbackURI);
    }

    public GenericResourceApiVfModuleOperationInformation changeAssignVfModule(VfModule vfModule, GenericVnf vnf,
            ServiceInstance serviceInstance, Customer customer, CloudRegion cloudRegion, RequestContext requestContext,
            URI callbackURI) throws MapperException {
        return sdncRM.reqMapper(SDNCSvcOperation.VF_MODULE_TOPOLOGY_OPERATION, SDNCSvcAction.CHANGE_ASSIGN, vfModule,
                null, vnf, serviceInstance, customer, cloudRegion, requestContext, null, callbackURI);
    }
}
