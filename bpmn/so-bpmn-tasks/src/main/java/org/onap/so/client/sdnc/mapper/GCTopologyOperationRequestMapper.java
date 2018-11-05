/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.sdnc.mapper;

import java.net.URI;

import org.onap.sdnc.northbound.client.model.*;
import org.onap.so.bpmn.servicedecomposition.bbobjects.*;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.springframework.stereotype.Component;

@Component(value = "sdncGCTopologyOperationRequestMapper")
public class GCTopologyOperationRequestMapper {

    private static final GeneralTopologyObjectMapper generalTopologyObjectMapper = new GeneralTopologyObjectMapper();

    public GenericResourceApiGcTopologyOperationInformation assignOrActivateVnrReqMapper(SDNCSvcAction svcAction,
                                                                                         GenericResourceApiRequestActionEnumeration reqAction,
                                                                                         ServiceInstance serviceInstance,
                                                                                         RequestContext requestContext,
                                                                                         Customer customer,
                                                                                         Configuration vnrConfiguration,
                                                                                         GenericVnf voiceVnf, String sdncReqId,URI callbackUri) {

        GenericResourceApiGcTopologyOperationInformation req = new GenericResourceApiGcTopologyOperationInformation();
        GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader = generalTopologyObjectMapper.buildSdncRequestHeader(svcAction, sdncReqId,callbackUri.toString());
        GenericResourceApiRequestinformationRequestInformation requestInformation = generalTopologyObjectMapper.buildGenericResourceApiRequestinformationRequestInformation(sdncReqId, reqAction);
        GenericResourceApiServiceinformationServiceInformation serviceInformation = generalTopologyObjectMapper.buildServiceInformation(serviceInstance, requestContext, customer, false);
        GenericResourceApiConfigurationinformationConfigurationInformation configurationInformation = generalTopologyObjectMapper.buildConfigurationInformation(vnrConfiguration,true);
        GenericResourceApiGcrequestinputGcRequestInput gcRequestInput = generalTopologyObjectMapper.buildGcRequestInformation(voiceVnf,null);
        req.setRequestInformation(requestInformation);
        req.setSdncRequestHeader(sdncRequestHeader);
        req.setServiceInformation(serviceInformation);
        req.setConfigurationInformation(configurationInformation);
        req.setGcRequestInput(gcRequestInput);

        return req;

    }


    public GenericResourceApiGcTopologyOperationInformation deactivateOrUnassignVnrReqMapper(SDNCSvcAction svcAction,
                                                                                             ServiceInstance serviceInstance,
                                                                                             RequestContext requestContext,
                                                                                             Configuration vnrConfiguration, String sdncReqId, URI callbackUri) {

        GenericResourceApiGcTopologyOperationInformation req = new GenericResourceApiGcTopologyOperationInformation();     
        GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader =
                generalTopologyObjectMapper.buildSdncRequestHeader(svcAction, sdncReqId,callbackUri.toString());
        GenericResourceApiRequestinformationRequestInformation requestInformation = generalTopologyObjectMapper
                .buildGenericResourceApiRequestinformationRequestInformation(sdncReqId,
                        GenericResourceApiRequestActionEnumeration.DELETEGENERICCONFIGURATIONINSTANCE);
        GenericResourceApiServiceinformationServiceInformation serviceInformation = new GenericResourceApiServiceinformationServiceInformation();
        serviceInformation.setServiceInstanceId(serviceInstance.getServiceInstanceId());
        GenericResourceApiConfigurationinformationConfigurationInformation configurationInformation =
                new GenericResourceApiConfigurationinformationConfigurationInformation();
        configurationInformation.setConfigurationId(vnrConfiguration.getConfigurationId());
        req.setRequestInformation(requestInformation);
        req.setSdncRequestHeader(sdncRequestHeader);
        req.setServiceInformation(serviceInformation);
        req.setConfigurationInformation(configurationInformation);
        return req;

    }


}
