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


import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.SDNCClient;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.onap.so.client.sdnc.mapper.GCTopologyOperationRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.onap.sdnc.apps.client.model.GenericResourceApiGcTopologyOperationInformation;
import org.onap.sdnc.apps.client.model.GenericResourceApiRequestActionEnumeration;

@Component
public class SDNCConfigurationResources {
    @Autowired
    private GCTopologyOperationRequestMapper sdncRM;
    
    @Autowired
    private SDNCClient sdncClient;

    /**
     * SDN-C call to assign configuration after it was created in A&AI
     *
     * @param serviceInstance
     * @param requestContext
     * @param vnrConfiguration
     * @param voiceVnf
     * @return
     * @throws MapperException
     * @throws BadResponseException
     */
    public String assignVnrConfiguration(ServiceInstance serviceInstance,
                                         RequestContext requestContext,
                                         Customer customer,
                                         Configuration vnrConfiguration,
                                         GenericVnf voiceVnf)
            throws MapperException, BadResponseException {
        GenericResourceApiGcTopologyOperationInformation sdncReq = sdncRM.assignOrActivateVnrReqMapper(
                SDNCSvcAction.ASSIGN,
                GenericResourceApiRequestActionEnumeration.CREATEGENERICCONFIGURATIONINSTANCE ,
      serviceInstance , requestContext, customer, vnrConfiguration,voiceVnf);
        return sdncClient.post(sdncReq, SDNCTopology.CONFIGURATION);
    }

    /**
     * SDNC Call to Activate VNR Configuration
     *
     * @param serviceInstance
     * @param requestContext
     * @param vnrConfiguration
     * @param voiceVnf
     * @return
     * @throws MapperException
     * @throws BadResponseException
     */
    public String activateVnrConfiguration(ServiceInstance serviceInstance,
                                           RequestContext requestContext,
                                           Customer customer,
                                           Configuration vnrConfiguration,
                                           GenericVnf voiceVnf)
            throws MapperException, BadResponseException {

        GenericResourceApiGcTopologyOperationInformation sdncReq = sdncRM.assignOrActivateVnrReqMapper(
                SDNCSvcAction.ACTIVATE,
                GenericResourceApiRequestActionEnumeration.CREATEGENERICCONFIGURATIONINSTANCE ,
                serviceInstance , requestContext, customer, vnrConfiguration, voiceVnf);
        return sdncClient.post(sdncReq, SDNCTopology.CONFIGURATION);
    }

    /**
     * method to unAssign Vnr Configuration in SDNC
     *
     * @param serviceInstance
     * @param requestContext
     * @param vnrConfiguration
     * @return
     * @throws BadResponseException
     * @throws MapperException
     */
    public String unAssignVnrConfiguration(ServiceInstance serviceInstance, RequestContext requestContext,
                                           Configuration vnrConfiguration) throws BadResponseException, MapperException {

        GenericResourceApiGcTopologyOperationInformation sdncReq = sdncRM.deactivateOrUnassignVnrReqMapper
                (SDNCSvcAction.UNASSIGN,serviceInstance, requestContext, vnrConfiguration);
        return sdncClient.post(sdncReq,  SDNCTopology.CONFIGURATION);
    }

    /***
     *  Deactivate VNR SDNC Call
     * @param serviceInstance
     * @param requestContext
     * @param vnrConfiguration
     * @throws BadResponseException
     * @throws MapperException
     */
    public String deactivateVnrConfiguration(ServiceInstance serviceInstance, RequestContext requestContext, Configuration vnrConfiguration) throws BadResponseException, MapperException {
        GenericResourceApiGcTopologyOperationInformation sdncReq = sdncRM.deactivateOrUnassignVnrReqMapper(
                SDNCSvcAction.DEACTIVATE,
                serviceInstance , requestContext, vnrConfiguration);
        return sdncClient.post(sdncReq, SDNCTopology.CONFIGURATION);
    }
}
