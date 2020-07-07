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

package org.onap.so.bpmn.infrastructure.sdnc.mapper;

import java.util.Map;
import java.util.UUID;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParamParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestinformationRequestInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiSdncrequestheaderSdncRequestHeader;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceinformationServiceInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServicerequestinputServiceRequestInput;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceTopologyOperationMapper {

    @Autowired
    public GeneralTopologyObjectMapper generalTopologyObjectMapper;

    public GenericResourceApiServiceOperationInformation reqMapper(SDNCSvcOperation svcOperation,
            SDNCSvcAction svcAction, GenericResourceApiRequestActionEnumeration resourceAction,
            ServiceInstance serviceInstance, Customer customer, RequestContext requestContext) {

        String sdncReqId = UUID.randomUUID().toString();
        String msoRequestId = UUID.randomUUID().toString();
        if (requestContext != null && requestContext.getMsoRequestId() != null) {
            msoRequestId = requestContext.getMsoRequestId();
        }
        GenericResourceApiServiceOperationInformation servOpInput = new GenericResourceApiServiceOperationInformation();
        GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader =
                generalTopologyObjectMapper.buildSdncRequestHeader(svcAction, sdncReqId);
        GenericResourceApiRequestinformationRequestInformation reqInfo = generalTopologyObjectMapper
                .buildGenericResourceApiRequestinformationRequestInformation(msoRequestId, resourceAction);
        GenericResourceApiServiceinformationServiceInformation servInfo =
                generalTopologyObjectMapper.buildServiceInformation(serviceInstance, requestContext, customer, true);
        GenericResourceApiServicerequestinputServiceRequestInput servReqInfo =
                new GenericResourceApiServicerequestinputServiceRequestInput();

        servReqInfo.setServiceInstanceName(serviceInstance.getServiceInstanceName());

        servOpInput.setSdncRequestHeader(sdncRequestHeader);
        servOpInput.setRequestInformation(reqInfo);
        servOpInput.setServiceInformation(servInfo);
        servOpInput.setServiceRequestInput(servReqInfo);

        if (requestContext != null && requestContext.getUserParams() != null) {
            for (Map.Entry<String, Object> entry : requestContext.getUserParams().entrySet()) {
                GenericResourceApiServicerequestinputServiceRequestInput serviceRequestInput =
                        new GenericResourceApiServicerequestinputServiceRequestInput();
                serviceRequestInput.setServiceInstanceName(serviceInstance.getServiceInstanceName());
                GenericResourceApiParam serviceInputParameters = new GenericResourceApiParam();
                GenericResourceApiParamParam paramItem = new GenericResourceApiParamParam();
                paramItem.setName(entry.getKey());
                paramItem.setValue(generalTopologyObjectMapper.mapUserParamValue(entry.getValue()));
                serviceInputParameters.addParamItem(paramItem);
                serviceRequestInput.serviceInputParameters(serviceInputParameters);
                servOpInput.setServiceRequestInput(serviceRequestInput);
            }
        }
        return servOpInput;
    }
}
