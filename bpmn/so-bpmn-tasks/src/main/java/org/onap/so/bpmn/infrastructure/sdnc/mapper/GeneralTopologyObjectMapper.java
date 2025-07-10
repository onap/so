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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onap.sdnc.northbound.client.model.GenericResourceApiConfigurationinformationConfigurationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiGcrequestinputGcRequestInput;
import org.onap.sdnc.northbound.client.model.GenericResourceApiNetworkinformationNetworkInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiOnapmodelinformationOnapModelInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiParam;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestActionEnumeration;
import org.onap.sdnc.northbound.client.model.GenericResourceApiRequestinformationRequestInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiSdncrequestheaderSdncRequestHeader;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceinformationServiceInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfmoduleinformationVfModuleInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfinformationVnfInformation;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.exception.MapperException;
import org.onap.so.client.sdnc.beans.SDNCSvcAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class GeneralTopologyObjectMapper {

    private static final Logger logger = LoggerFactory.getLogger(GeneralTopologyObjectMapper.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /*
     * Build GenericResourceApiRequestinformationRequestInformation
     */
    public GenericResourceApiRequestinformationRequestInformation buildGenericResourceApiRequestinformationRequestInformation(
            String msoReqId, GenericResourceApiRequestActionEnumeration requestAction) {

        GenericResourceApiRequestinformationRequestInformation requestInformation =
                new GenericResourceApiRequestinformationRequestInformation();
        requestInformation.setRequestId(msoReqId);
        requestInformation.setRequestAction(requestAction);
        requestInformation.setSource("MSO");
        return requestInformation;
    }

    /*
     * Build GenericResourceApiServiceinformationServiceInformation
     */
    public GenericResourceApiServiceinformationServiceInformation buildServiceInformation(
            ServiceInstance serviceInstance, RequestContext requestContext, Customer customer,
            boolean includeModelInformation) {
        GenericResourceApiServiceinformationServiceInformation serviceInformation =
                new GenericResourceApiServiceinformationServiceInformation();
        serviceInformation.serviceId(serviceInstance.getServiceInstanceId());
        if (requestContext != null) {
            serviceInformation.setSubscriptionServiceType(requestContext.getProductFamilyId());
        }
        if (includeModelInformation) {
            GenericResourceApiOnapmodelinformationOnapModelInformation onapModelInformation =
                    new GenericResourceApiOnapmodelinformationOnapModelInformation();
            onapModelInformation
                    .setModelInvariantUuid(serviceInstance.getModelInfoServiceInstance().getModelInvariantUuid());
            onapModelInformation.setModelVersion(serviceInstance.getModelInfoServiceInstance().getModelVersion());
            onapModelInformation.setModelName(serviceInstance.getModelInfoServiceInstance().getModelName());
            onapModelInformation.setModelUuid(serviceInstance.getModelInfoServiceInstance().getModelUuid());
            serviceInformation.setOnapModelInformation(onapModelInformation);
        }
        serviceInformation.setServiceInstanceId(serviceInstance.getServiceInstanceId());

        if (customer != null) {
            serviceInformation.setGlobalCustomerId(customer.getGlobalCustomerId());
            if (customer.getServiceSubscription() != null) {
                serviceInformation.setSubscriptionServiceType(customer.getServiceSubscription().getServiceType());
            }

        }
        return serviceInformation;
    }

    /*
     * Build GenericResourceApiNetworkinformationNetworkInformation
     */
    public GenericResourceApiNetworkinformationNetworkInformation buildNetworkInformation(L3Network network) {
        GenericResourceApiNetworkinformationNetworkInformation networkInformation =
                new GenericResourceApiNetworkinformationNetworkInformation();
        GenericResourceApiOnapmodelinformationOnapModelInformation onapModelInformation =
                new GenericResourceApiOnapmodelinformationOnapModelInformation();
        if (network.getModelInfoNetwork() != null) {
            onapModelInformation.setModelInvariantUuid(network.getModelInfoNetwork().getModelInvariantUUID());
            onapModelInformation.setModelName(network.getModelInfoNetwork().getModelName());
            onapModelInformation.setModelVersion(network.getModelInfoNetwork().getModelVersion());
            onapModelInformation.setModelUuid(network.getModelInfoNetwork().getModelUUID());
            onapModelInformation.setModelCustomizationUuid(network.getModelInfoNetwork().getModelCustomizationUUID());
            networkInformation.setOnapModelInformation(onapModelInformation);
        }

        networkInformation.setFromPreload(null);
        networkInformation.setNetworkId(network.getNetworkId());
        networkInformation.setNetworkType(network.getNetworkType());
        networkInformation.setNetworkTechnology(network.getNetworkTechnology());
        return networkInformation;
    }

    /*
     * Build GenericResourceApiVnfinformationVnfInformation
     */
    public GenericResourceApiVnfinformationVnfInformation buildVnfInformation(GenericVnf vnf,
            ServiceInstance serviceInstance, boolean includeModelInformation) {
        GenericResourceApiVnfinformationVnfInformation vnfInformation =
                new GenericResourceApiVnfinformationVnfInformation();
        if (includeModelInformation && vnf.getModelInfoGenericVnf() != null) {
            GenericResourceApiOnapmodelinformationOnapModelInformation onapModelInformation =
                    new GenericResourceApiOnapmodelinformationOnapModelInformation();
            onapModelInformation.setModelInvariantUuid(vnf.getModelInfoGenericVnf().getModelInvariantUuid());
            onapModelInformation.setModelName(vnf.getModelInfoGenericVnf().getModelName());
            onapModelInformation.setModelVersion(vnf.getModelInfoGenericVnf().getModelVersion());
            onapModelInformation.setModelUuid(vnf.getModelInfoGenericVnf().getModelUuid());
            onapModelInformation.setModelCustomizationUuid(vnf.getModelInfoGenericVnf().getModelCustomizationUuid());
            vnfInformation.setOnapModelInformation(onapModelInformation);
        }
        vnfInformation.setVnfId(vnf.getVnfId());
        vnfInformation.setVnfType(vnf.getVnfType());
        vnfInformation.setVnfName(vnf.getVnfName());
        return vnfInformation;
    }

    /*
     * Build GenericResourceApiVfModuleinformationVfModuleInformation
     */
    public GenericResourceApiVfmoduleinformationVfModuleInformation buildVfModuleInformation(VfModule vfModule,
            GenericVnf vnf, ServiceInstance serviceInstance, RequestContext requestContext,
            boolean includeModelInformation) throws MapperException {
        GenericResourceApiVfmoduleinformationVfModuleInformation vfModuleInformation =
                new GenericResourceApiVfmoduleinformationVfModuleInformation();
        if (includeModelInformation) {
            if (vfModule.getModelInfoVfModule() == null) {
                throw new MapperException("VF Module model info is null for " + vfModule.getVfModuleId());
            } else {
                GenericResourceApiOnapmodelinformationOnapModelInformation onapModelInformation =
                        new GenericResourceApiOnapmodelinformationOnapModelInformation();
                onapModelInformation.setModelInvariantUuid(vfModule.getModelInfoVfModule().getModelInvariantUUID());
                onapModelInformation.setModelName(vfModule.getModelInfoVfModule().getModelName());
                onapModelInformation.setModelVersion(vfModule.getModelInfoVfModule().getModelVersion());
                onapModelInformation.setModelUuid(vfModule.getModelInfoVfModule().getModelUUID());
                onapModelInformation
                        .setModelCustomizationUuid(vfModule.getModelInfoVfModule().getModelCustomizationUUID());
                vfModuleInformation.setOnapModelInformation(onapModelInformation);
            }
        }
        if (vfModule.getModelInfoVfModule() != null) {
            vfModuleInformation.setVfModuleType(vfModule.getModelInfoVfModule().getModelName());
        }
        vfModuleInformation.setVfModuleId(vfModule.getVfModuleId());
        if (requestContext != null && requestContext.getRequestParameters() != null) {
            vfModuleInformation.setFromPreload(requestContext.getRequestParameters().getUsePreload());
        } else {
            vfModuleInformation.setFromPreload(true);
        }

        return vfModuleInformation;
    }


    public GenericResourceApiSdncrequestheaderSdncRequestHeader buildSdncRequestHeader(SDNCSvcAction svcAction,
            String sdncReqId) {
        return buildSdncRequestHeader(svcAction, sdncReqId, null);
    }

    public GenericResourceApiSdncrequestheaderSdncRequestHeader buildSdncRequestHeader(SDNCSvcAction svcAction,
            String sdncReqId, String callbackUrl) {
        GenericResourceApiSdncrequestheaderSdncRequestHeader sdncRequestHeader =
                new GenericResourceApiSdncrequestheaderSdncRequestHeader();
        sdncRequestHeader.setSvcAction(svcAction.getSdncApiAction());
        sdncRequestHeader.setSvcRequestId(sdncReqId);
        sdncRequestHeader.setSvcNotificationUrl(callbackUrl);
        return sdncRequestHeader;
    }

    /**
     * Build ConfigurationInformation
     *
     * @param configuration
     * @param includeModelInformation
     * @return
     */
    public GenericResourceApiConfigurationinformationConfigurationInformation buildConfigurationInformation(
            Configuration configuration, boolean includeModelInformation) {
        GenericResourceApiConfigurationinformationConfigurationInformation configurationInformation =
                new GenericResourceApiConfigurationinformationConfigurationInformation();
        configurationInformation.setConfigurationId(configuration.getConfigurationId());
        configurationInformation.setConfigurationName(configuration.getConfigurationName());
        configurationInformation.setConfigurationType(configuration.getConfigurationType());
        if (includeModelInformation) {
            GenericResourceApiOnapmodelinformationOnapModelInformation onapModelInformation =
                    new GenericResourceApiOnapmodelinformationOnapModelInformation();
            onapModelInformation.setModelInvariantUuid(configuration.getModelInfoConfiguration().getModelInvariantId());
            onapModelInformation.setModelUuid(configuration.getModelInfoConfiguration().getModelVersionId());
            onapModelInformation
                    .setModelCustomizationUuid(configuration.getModelInfoConfiguration().getModelCustomizationId());
            configurationInformation.setOnapModelInformation(onapModelInformation);
        }
        return configurationInformation;
    }


    /**
     * Build GcRequestInformation
     *
     * @param vnf
     * @param genericResourceApiParam
     * @return
     */
    public GenericResourceApiGcrequestinputGcRequestInput buildGcRequestInformation(GenericVnf vnf,
            GenericResourceApiParam genericResourceApiParam) {
        GenericResourceApiGcrequestinputGcRequestInput gcRequestInput =
                new GenericResourceApiGcrequestinputGcRequestInput();
        gcRequestInput.setVnfId(vnf.getVnfId());
        if (genericResourceApiParam != null) {
            gcRequestInput.setInputParameters(genericResourceApiParam);
        }
        return gcRequestInput;
    }


    public String mapUserParamValue(Object value) {
        if (value == null) {
            return null;
        } else {
            if (value instanceof Map || value instanceof Set || value instanceof List) {
                try {
                    return mapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    logger.error("could not map value to string", e);
                    throw new IllegalArgumentException(e);
                }
            } else {
                return value.toString();
            }
        }
    }
}
