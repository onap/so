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

package org.onap.so.bpmn.infrastructure.sdnc.mapper;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.onap.so.logger.LoggingAnchor;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParamParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestinformationRequestInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiSdncrequestheaderSdncRequestHeader;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceinformationServiceInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiSvcActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfModuleOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfModuleResponseInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfmoduleinformationVfModuleInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfmodulerequestinputVfModuleRequestInput;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfinformationVnfInformation;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.onap.so.client.sdnc.beans.SDNCSvcOperation;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class VfModuleTopologyOperationRequestMapper {
    private static final Logger logger = LoggerFactory.getLogger(VfModuleTopologyOperationRequestMapper.class);

    @Autowired
    private GeneralTopologyObjectMapper generalTopologyObjectMapper;

    public GenericResourceApiVfModuleOperationInformation reqMapper(SDNCSvcOperation svcOperation,
            SDNCSvcAction svcAction, VfModule vfModule, VolumeGroup volumeGroup, GenericVnf vnf,
            ServiceInstance serviceInstance, Customer customer, CloudRegion cloudRegion, RequestContext requestContext,
            String sdncAssignResponse, URI callbackURL) throws MapperException {
        GenericResourceApiVfModuleOperationInformation req = new GenericResourceApiVfModuleOperationInformation();

        boolean includeModelInformation = false;

        GenericResourceApiRequestActionEnumeration requestAction =
                GenericResourceApiRequestActionEnumeration.CREATEVFMODULEINSTANCE;
        GenericResourceApiSvcActionEnumeration genericResourceApiSvcAction =
                GenericResourceApiSvcActionEnumeration.ASSIGN;

        if (svcAction.equals(SDNCSvcAction.ACTIVATE)) {
            genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.ACTIVATE;
            requestAction = GenericResourceApiRequestActionEnumeration.CREATEVFMODULEINSTANCE;
            includeModelInformation = true;
        } else if (svcAction.equals(SDNCSvcAction.ASSIGN)) {
            genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.ASSIGN;
            requestAction = GenericResourceApiRequestActionEnumeration.CREATEVFMODULEINSTANCE;
            includeModelInformation = true;
        } else if (svcAction.equals(SDNCSvcAction.DEACTIVATE)) {
            genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.DEACTIVATE;
            requestAction = GenericResourceApiRequestActionEnumeration.DELETEVFMODULEINSTANCE;
            includeModelInformation = true;
        } else if (svcAction.equals(SDNCSvcAction.DELETE)) {
            genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.DELETE;
            requestAction = GenericResourceApiRequestActionEnumeration.DELETEVFMODULEINSTANCE;
            includeModelInformation = false;
        } else if (svcAction.equals(SDNCSvcAction.UNASSIGN)) {
            genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.UNASSIGN;
            requestAction = GenericResourceApiRequestActionEnumeration.DELETEVFMODULEINSTANCE;
            includeModelInformation = false;
        } else if (svcAction.equals(SDNCSvcAction.CHANGE_ASSIGN)) {
            genericResourceApiSvcAction = GenericResourceApiSvcActionEnumeration.CHANGEASSIGN;
            requestAction = GenericResourceApiRequestActionEnumeration.CREATEVFMODULEINSTANCE;
            includeModelInformation = true;
        }

        String sdncReqId = UUID.randomUUID().toString();
        String msoRequestId = UUID.randomUUID().toString();
        if (requestContext != null && requestContext.getMsoRequestId() != null) {
            msoRequestId = requestContext.getMsoRequestId();
        }

        GenericResourceApiRequestinformationRequestInformation requestInformation = generalTopologyObjectMapper
                .buildGenericResourceApiRequestinformationRequestInformation(msoRequestId, requestAction);
        GenericResourceApiServiceinformationServiceInformation serviceInformation = generalTopologyObjectMapper
                .buildServiceInformation(serviceInstance, requestContext, customer, includeModelInformation);
        GenericResourceApiVnfinformationVnfInformation vnfInformation =
                generalTopologyObjectMapper.buildVnfInformation(vnf, serviceInstance, includeModelInformation);
        GenericResourceApiVfmoduleinformationVfModuleInformation vfModuleInformation = generalTopologyObjectMapper
                .buildVfModuleInformation(vfModule, vnf, serviceInstance, requestContext, includeModelInformation);
        GenericResourceApiVfmodulerequestinputVfModuleRequestInput vfModuleRequestInput =
                buildVfModuleRequestInput(vfModule, volumeGroup, cloudRegion, requestContext);
        GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader =
                buildVfModuleSdncRequestHeader(sdncReqId, genericResourceApiSvcAction, callbackURL);

        req.setRequestInformation(requestInformation);
        req.setSdncRequestHeader(sdncRequestHeader);
        req.setServiceInformation(serviceInformation);
        req.setVnfInformation(vnfInformation);
        req.setVfModuleInformation(vfModuleInformation);
        req.setVfModuleRequestInput(vfModuleRequestInput);

        return req;
    }

    private GenericResourceApiVfmodulerequestinputVfModuleRequestInput buildVfModuleRequestInput(VfModule vfModule,
            VolumeGroup volumeGroup, CloudRegion cloudRegion, RequestContext requestContext) {
        GenericResourceApiVfmodulerequestinputVfModuleRequestInput vfModuleRequestInput =
                new GenericResourceApiVfmodulerequestinputVfModuleRequestInput();
        if (cloudRegion != null) {
            vfModuleRequestInput.setTenant(cloudRegion.getTenantId());
            vfModuleRequestInput.setAicCloudRegion(cloudRegion.getLcpCloudRegionId());
            vfModuleRequestInput.setCloudOwner(cloudRegion.getCloudOwner());
        }
        if (vfModule.getVfModuleName() != null && !vfModule.getVfModuleName().equals("")) {
            vfModuleRequestInput.setVfModuleName(vfModule.getVfModuleName());
        }
        GenericResourceApiParam vfModuleInputParameters = new GenericResourceApiParam();

        if (requestContext != null && requestContext.getUserParams() != null) {
            for (Map.Entry<String, Object> entry : requestContext.getUserParams().entrySet()) {
                GenericResourceApiParamParam paramItem = new GenericResourceApiParamParam();
                paramItem.setName(entry.getKey());
                paramItem.setValue(generalTopologyObjectMapper.mapUserParamValue(entry.getValue()));
                vfModuleInputParameters.addParamItem(paramItem);
            }
        }

        if (vfModule.getCloudParams() != null) {
            for (Map.Entry<String, String> entry : vfModule.getCloudParams().entrySet()) {
                GenericResourceApiParamParam paramItem = new GenericResourceApiParamParam();
                paramItem.setName(entry.getKey());
                paramItem.setValue(entry.getValue());
                vfModuleInputParameters.addParamItem(paramItem);
            }
        }

        if (volumeGroup != null) {
            GenericResourceApiParamParam paramItem = new GenericResourceApiParamParam();
            paramItem.setName("volume-group-id");
            paramItem.setValue(volumeGroup.getVolumeGroupId());
            vfModuleInputParameters.addParamItem(paramItem);
        }
        vfModuleRequestInput.setVfModuleInputParameters(vfModuleInputParameters);

        return vfModuleRequestInput;
    }

    private GenericResourceApiSdncrequestheaderSdncRequestHeader buildVfModuleSdncRequestHeader(String sdncReqId,
            GenericResourceApiSvcActionEnumeration svcAction, URI callbackUrl) {
        GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader =
                new GenericResourceApiSdncrequestheaderSdncRequestHeader();
        sdncRequestHeader.setSvcRequestId(sdncReqId);
        sdncRequestHeader.setSvcAction(svcAction);
        sdncRequestHeader.setSvcNotificationUrl(callbackUrl.toString());
        return sdncRequestHeader;
    }

    public String buildObjectPath(String sdncAssignResponse) {
        String objectPath = null;
        if (sdncAssignResponse != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                GenericResourceApiVfModuleResponseInformation assignResponseInfo =
                        mapper.readValue(sdncAssignResponse, GenericResourceApiVfModuleResponseInformation.class);
                objectPath = assignResponseInfo.getVfModuleResponseInformation().getObjectPath();
            } catch (Exception e) {
                logger.error(LoggingAnchor.FIVE, MessageEnum.RA_RESPONSE_FROM_SDNC.toString(), e.getMessage(), "BPMN",
                        ErrorCode.UnknownError.getValue(), e.getMessage());
            }
        }
        return objectPath;
    }
}
