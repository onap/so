/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.infra.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandlerinfra.infra.rest.exception.AAIEntityNotFound;
import org.onap.so.apihandlerinfra.infra.rest.exception.CloudConfigurationNotFoundException;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.logger.HttpHeadersConstants;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BpmnRequestBuilder {

    private static final String CLOUD_CONFIGURATION_COULD_NOT_BE_FOUND = "Cloud Configuration could not be found";

    private static final String GENERIC_VNF_NOT_FOUND_IN_INVENTORY_VNF_ID =
            "Generic Vnf Not Found In Inventory, VnfId: ";

    private static final String VF_MODULE_NOT_FOUND_IN_INVENTORY_VNF_ID = "VF Module Not Found In Inventory, VnfId: ";

    private static final Logger logger = LoggerFactory.getLogger(BpmnRequestBuilder.class);

    @Autowired
    private RequestsDbClient infraActiveRequestsClient;

    @Autowired
    private AAIDataRetrieval aaiDataRet;

    private static final ObjectMapper mapper = new ObjectMapper();


    public ServiceInstancesRequest buildVFModuleDeleteRequest(String vnfId, String vfModuleId, ModelType modelType)
            throws AAIEntityNotFound {
        GenericVnf vnf = aaiDataRet.getGenericVnf(vnfId);
        if (vnf == null) {
            throw new AAIEntityNotFound(GENERIC_VNF_NOT_FOUND_IN_INVENTORY_VNF_ID + vnfId);
        }
        VfModule vfModule = aaiDataRet.getAAIVfModule(vnfId, vfModuleId);
        if (vfModule == null) {
            throw new AAIEntityNotFound(VF_MODULE_NOT_FOUND_IN_INVENTORY_VNF_ID + vnfId + " vfModuleId: " + vfModuleId);
        }
        return createServiceInstancesRequest(vnf, vfModule, modelType);
    }

    public CloudConfiguration getCloudConfigurationVfModuleReplace(String vnfId, String vfModuleId)
            throws AAIEntityNotFound {
        GenericVnf vnf = aaiDataRet.getGenericVnf(vnfId);
        if (vnf == null) {
            throw new AAIEntityNotFound(GENERIC_VNF_NOT_FOUND_IN_INVENTORY_VNF_ID + vnfId);
        }

        return mapCloudConfiguration(vnf, vfModuleId);
    }

    public ServiceInstancesRequest buildVolumeGroupDeleteRequest(String vnfId, String volumeGroupId)
            throws AAIEntityNotFound {
        GenericVnf vnf = aaiDataRet.getGenericVnf(vnfId);
        if (vnf == null) {
            throw new AAIEntityNotFound(GENERIC_VNF_NOT_FOUND_IN_INVENTORY_VNF_ID + vnfId);
        }
        VolumeGroup volumeGroup = aaiDataRet.getVolumeGroup(vnfId, volumeGroupId);
        if (volumeGroup == null) {
            throw new AAIEntityNotFound(
                    VF_MODULE_NOT_FOUND_IN_INVENTORY_VNF_ID + vnfId + " volumeGroupId: " + volumeGroupId);
        }
        return createServiceInstancesRequest(vnf, volumeGroup);
    }

    public ServiceInstancesRequest buildServiceDeleteRequest(String serviceInstanceId) throws AAIEntityNotFound {
        ServiceInstance serviceInstance = aaiDataRet.getServiceInstance(serviceInstanceId);
        if (serviceInstance == null) {
            throw new AAIEntityNotFound(
                    "ServiceInstance Not Found In Inventory, ServiceInstanceId: " + serviceInstanceId);
        }
        return createServiceInstancesRequest(serviceInstance);
    }

    public ServiceInstancesRequest buildVnfDeleteRequest(String vnfId) throws AAIEntityNotFound {
        GenericVnf vnf = aaiDataRet.getGenericVnf(vnfId);
        if (vnf == null) {
            throw new AAIEntityNotFound(GENERIC_VNF_NOT_FOUND_IN_INVENTORY_VNF_ID + vnfId);
        }
        return createServiceInstancesRequest(vnf);
    }

    public ServiceInstancesRequest buildNetworkDeleteRequest(String networkId) throws AAIEntityNotFound {
        L3Network network = aaiDataRet.getNetwork(networkId);
        if (network == null) {
            throw new AAIEntityNotFound("Network Not Found In Inventory, NetworkId: " + networkId);
        }
        return createServiceInstancesRequest(network);
    }

    protected ServiceInstancesRequest createServiceInstancesRequest(ServiceInstance serviceInstance) {
        ServiceInstancesRequest request = new ServiceInstancesRequest();
        RequestDetails requestDetails = mapServiceRequestDetails(serviceInstance);
        request.setRequestDetails(requestDetails);
        return request;
    }

    protected ServiceInstancesRequest createServiceInstancesRequest(GenericVnf vnf) {
        ServiceInstancesRequest request = new ServiceInstancesRequest();
        RequestDetails requestDetails = mapVnfRequestDetails(vnf);
        request.setRequestDetails(requestDetails);
        return request;
    }

    protected ServiceInstancesRequest createServiceInstancesRequest(GenericVnf vnf, VfModule vfModule,
            ModelType modelType) {
        ServiceInstancesRequest request = new ServiceInstancesRequest();
        RequestDetails requestDetails = mapRequestDetails(vnf, vfModule, modelType);
        request.setRequestDetails(requestDetails);
        return request;
    }

    protected ServiceInstancesRequest createServiceInstancesRequest(GenericVnf vnf, VolumeGroup volumeGroup) {
        ServiceInstancesRequest request = new ServiceInstancesRequest();
        RequestDetails requestDetails = mapRequestDetails(vnf, volumeGroup);
        request.setRequestDetails(requestDetails);
        return request;
    }

    protected ServiceInstancesRequest createServiceInstancesRequest(L3Network network) {
        ServiceInstancesRequest request = new ServiceInstancesRequest();
        RequestDetails requestDetails = mapNetworkRequestDetails(network);
        request.setRequestDetails(requestDetails);
        return request;
    }

    protected RequestDetails mapRequestDetails(GenericVnf vnf, VfModule vfModule, ModelType modelType) {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestInfo(createRequestInfo());
        if (vfModule.getVfModuleName() != null) {
            requestDetails.getRequestInfo().setInstanceName(vfModule.getVfModuleName());
        }
        requestDetails.setModelInfo(mapVfModuleModelInformation(vfModule, modelType));

        requestDetails.setCloudConfiguration(mapCloudConfiguration(vnf, vfModule.getVfModuleId()));
        requestDetails.setRequestParameters(createRequestParameters());
        return requestDetails;
    }

    protected RequestDetails mapRequestDetails(GenericVnf vnf, VolumeGroup volumeGroup) {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestInfo(createRequestInfo());
        if (volumeGroup.getVolumeGroupName() != null) {
            requestDetails.getRequestInfo().setInstanceName(volumeGroup.getVolumeGroupName());
        }
        requestDetails.setModelInfo(mapVolumeGroupModelInformation(volumeGroup));
        requestDetails.setCloudConfiguration(mapCloudConfigurationVolume(vnf, volumeGroup));
        requestDetails.setRequestParameters(createRequestParameters());
        return requestDetails;
    }

    protected RequestDetails mapVnfRequestDetails(GenericVnf vnf) {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestInfo(createRequestInfo());
        if (vnf.getVnfName() != null) {
            requestDetails.getRequestInfo().setInstanceName(vnf.getVnfName());
        }
        requestDetails.setModelInfo(mapVnfModelInformation(vnf));
        requestDetails.setRequestParameters(createRequestParameters());
        return requestDetails;
    }

    protected RequestDetails mapServiceRequestDetails(ServiceInstance serviceInstance) {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestInfo(createRequestInfo());
        if (serviceInstance.getServiceInstanceName() != null) {
            requestDetails.getRequestInfo().setInstanceName(serviceInstance.getServiceInstanceName());
        }
        requestDetails.setModelInfo(mapServiceModelInformation(serviceInstance));
        requestDetails.setRequestParameters(createRequestParameters());
        return requestDetails;
    }

    protected RequestDetails mapNetworkRequestDetails(L3Network network) {
        RequestDetails requestDetails = new RequestDetails();
        requestDetails.setRequestInfo(createRequestInfo());
        if (network.getNetworkName() != null) {
            requestDetails.getRequestInfo().setInstanceName(network.getNetworkName());
        }
        requestDetails.setCloudConfiguration(mapCloudConfigurationNetwork(network));
        requestDetails.setModelInfo(mapNetworkModelInformation(network));
        requestDetails.setRequestParameters(createRequestParameters());
        return requestDetails;
    }

    protected ModelInfo mapVfModuleModelInformation(VfModule vfModule, ModelType modelType) {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(vfModule.getModelCustomizationId());
        modelInfo.setModelCustomizationUuid(vfModule.getModelCustomizationId());
        modelInfo.setModelType(modelType);
        return modelInfo;
    }

    protected ModelInfo mapVolumeGroupModelInformation(VolumeGroup volumeGroup) {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(volumeGroup.getModelCustomizationId());
        modelInfo.setModelCustomizationUuid(volumeGroup.getModelCustomizationId());
        modelInfo.setModelType(ModelType.volumeGroup);
        return modelInfo;
    }

    protected ModelInfo mapServiceModelInformation(ServiceInstance serviceInstance) {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelVersionId(serviceInstance.getModelVersionId());
        modelInfo.setModelType(ModelType.service);
        return modelInfo;
    }

    protected ModelInfo mapVnfModelInformation(GenericVnf vnf) {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(vnf.getModelCustomizationId());
        modelInfo.setModelCustomizationUuid(vnf.getModelCustomizationId());
        modelInfo.setModelType(ModelType.vnf);
        return modelInfo;
    }

    protected ModelInfo mapNetworkModelInformation(L3Network network) {
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelType(ModelType.network);
        modelInfo.setModelCustomizationId(network.getModelCustomizationId());
        modelInfo.setModelCustomizationUuid(network.getModelCustomizationId());
        return modelInfo;
    }

    public CloudConfiguration mapCloudConfiguration(GenericVnf vnf, String vfModuleId) {
        CloudConfiguration cloudConfig = new CloudConfiguration();
        AAIResultWrapper wrapper = new AAIResultWrapper(vnf);
        Optional<org.onap.aaiclient.client.aai.entities.Relationships> relationshipsOpt = wrapper.getRelationships();
        String tenantId = null;
        String cloudOwner = null;
        String lcpRegionId = null;
        if (relationshipsOpt.isPresent()) {
            tenantId = relationshipsOpt.get().getRelatedUris(Types.TENANT).stream().findFirst()
                    .map(item -> item.getURIKeys().get(AAIFluentTypeBuilder.Types.TENANT.getUriParams().tenantId))
                    .orElse(null);
            cloudOwner = relationshipsOpt.get().getRelatedUris(Types.TENANT).stream().findFirst().map(
                    item -> item.getURIKeys().get(AAIFluentTypeBuilder.Types.CLOUD_REGION.getUriParams().cloudOwner))
                    .orElse(null);
            lcpRegionId = relationshipsOpt.get().getRelatedUris(Types.TENANT).stream().findFirst().map(
                    item -> item.getURIKeys().get(AAIFluentTypeBuilder.Types.CLOUD_REGION.getUriParams().cloudRegionId))
                    .orElse(null);
        }

        if (tenantId == null || cloudOwner == null || lcpRegionId == null) {
            Map<String, String[]> filters = createQueryRequest("vfModuleId", vfModuleId);
            Optional<ServiceInstancesRequest> request = findServiceInstanceRequest(filters);
            if (request.isPresent()) {
                if (request.get().getRequestDetails() != null
                        && request.get().getRequestDetails().getCloudConfiguration() != null) {
                    if (request.get().getRequestDetails().getCloudConfiguration().getTenantId() != null) {
                        tenantId = request.get().getRequestDetails().getCloudConfiguration().getTenantId();
                    }
                    if (request.get().getRequestDetails().getCloudConfiguration().getCloudOwner() != null) {
                        cloudOwner = request.get().getRequestDetails().getCloudConfiguration().getCloudOwner();
                    }
                    if (request.get().getRequestDetails().getCloudConfiguration().getLcpCloudRegionId() != null) {
                        lcpRegionId = request.get().getRequestDetails().getCloudConfiguration().getLcpCloudRegionId();
                    }
                }
            } else {
                throw new CloudConfigurationNotFoundException(CLOUD_CONFIGURATION_COULD_NOT_BE_FOUND);
            }
        }
        cloudConfig.setTenantId(tenantId);
        cloudConfig.setCloudOwner(cloudOwner);
        cloudConfig.setLcpCloudRegionId(lcpRegionId);
        return cloudConfig;
    }

    public CloudConfiguration mapCloudConfigurationVolume(GenericVnf vnf, VolumeGroup volumeGroup) {
        CloudConfiguration cloudConfig = new CloudConfiguration();
        AAIResultWrapper wrapper = new AAIResultWrapper(vnf);
        Optional<org.onap.aaiclient.client.aai.entities.Relationships> relationshipsOpt = wrapper.getRelationships();
        String tenantId = null;
        String cloudOwner = null;
        String lcpRegionId = null;
        if (relationshipsOpt.isPresent()) {
            tenantId = relationshipsOpt.get().getRelatedUris(Types.TENANT).stream().findFirst()
                    .map(item -> item.getURIKeys().get(AAIFluentTypeBuilder.Types.TENANT.getUriParams().tenantId))
                    .orElse(null);
            cloudOwner = relationshipsOpt.get().getRelatedUris(Types.TENANT).stream().findFirst().map(
                    item -> item.getURIKeys().get(AAIFluentTypeBuilder.Types.CLOUD_REGION.getUriParams().cloudOwner))
                    .orElse(null);
            lcpRegionId = relationshipsOpt.get().getRelatedUris(Types.TENANT).stream().findFirst().map(
                    item -> item.getURIKeys().get(AAIFluentTypeBuilder.Types.CLOUD_REGION.getUriParams().cloudRegionId))
                    .orElse(null);
        }

        if (tenantId == null || cloudOwner == null || lcpRegionId == null) {
            Map<String, String[]> filters = createQueryRequest("volumeGroupId", volumeGroup.getVolumeGroupId());
            Optional<ServiceInstancesRequest> request = findServiceInstanceRequest(filters);
            if (request.isPresent()) {
                tenantId = request.get().getRequestDetails().getCloudConfiguration().getTenantId();
                cloudOwner = request.get().getRequestDetails().getCloudConfiguration().getCloudOwner();
                lcpRegionId = request.get().getRequestDetails().getCloudConfiguration().getLcpCloudRegionId();
            } else {
                throw new CloudConfigurationNotFoundException(CLOUD_CONFIGURATION_COULD_NOT_BE_FOUND);
            }
        }
        cloudConfig.setTenantId(tenantId);
        cloudConfig.setCloudOwner(cloudOwner);
        cloudConfig.setLcpCloudRegionId(lcpRegionId);
        return cloudConfig;
    }

    public CloudConfiguration mapCloudConfigurationNetwork(L3Network network) {
        CloudConfiguration cloudConfig = new CloudConfiguration();
        String tenantId = null;
        String cloudOwner = null;
        String lcpRegionId = null;

        Map<String, String[]> filters = createQueryRequest("networkId", network.getNetworkId());
        Optional<ServiceInstancesRequest> request = findServiceInstanceRequest(filters);
        if (request.isPresent()) {
            if (request.get().getRequestDetails() != null
                    && request.get().getRequestDetails().getCloudConfiguration() != null) {
                if (request.get().getRequestDetails().getCloudConfiguration().getTenantId() != null) {
                    tenantId = request.get().getRequestDetails().getCloudConfiguration().getTenantId();
                }
                if (request.get().getRequestDetails().getCloudConfiguration().getCloudOwner() != null) {
                    cloudOwner = request.get().getRequestDetails().getCloudConfiguration().getCloudOwner();
                }
                if (request.get().getRequestDetails().getCloudConfiguration().getLcpCloudRegionId() != null) {
                    lcpRegionId = request.get().getRequestDetails().getCloudConfiguration().getLcpCloudRegionId();
                }
            }
        } else {
            throw new CloudConfigurationNotFoundException(CLOUD_CONFIGURATION_COULD_NOT_BE_FOUND);
        }

        cloudConfig.setTenantId(tenantId);
        cloudConfig.setCloudOwner(cloudOwner);
        cloudConfig.setLcpCloudRegionId(lcpRegionId);
        return cloudConfig;
    }

    public CloudConfiguration mapCloudConfigurationVnf(String vnfId) {
        CloudConfiguration cloudConfig = new CloudConfiguration();
        String tenantId = null;
        String cloudOwner = null;
        String lcpRegionId = null;

        Map<String, String[]> filters = createQueryRequest("vnfId", vnfId);
        Optional<ServiceInstancesRequest> request = findServiceInstanceRequest(filters);
        if (request.isPresent()) {
            if (request.get().getRequestDetails() != null
                    && request.get().getRequestDetails().getCloudConfiguration() != null) {
                if (request.get().getRequestDetails().getCloudConfiguration().getTenantId() != null) {
                    tenantId = request.get().getRequestDetails().getCloudConfiguration().getTenantId();
                }
                if (request.get().getRequestDetails().getCloudConfiguration().getCloudOwner() != null) {
                    cloudOwner = request.get().getRequestDetails().getCloudConfiguration().getCloudOwner();
                }
                if (request.get().getRequestDetails().getCloudConfiguration().getLcpCloudRegionId() != null) {
                    lcpRegionId = request.get().getRequestDetails().getCloudConfiguration().getLcpCloudRegionId();
                }
            }
        } else {
            throw new CloudConfigurationNotFoundException(CLOUD_CONFIGURATION_COULD_NOT_BE_FOUND);
        }
        cloudConfig.setTenantId(tenantId);
        cloudConfig.setCloudOwner(cloudOwner);
        cloudConfig.setLcpCloudRegionId(lcpRegionId);
        return cloudConfig;
    }

    public Optional<ServiceInstancesRequest> findServiceInstanceRequest(Map<String, String[]> filters) {
        List<InfraActiveRequests> completeRequests = infraActiveRequestsClient.getRequest(filters);
        InfraActiveRequests foundRequest = completeRequests.get(0);
        ServiceInstancesRequest siRequest;
        try {
            siRequest = mapper.readValue(foundRequest.getRequestBody(), ServiceInstancesRequest.class);
            return Optional.of(siRequest);
        } catch (Exception e) {
            logger.error("Could not read Create Instance Request", e);
        }
        return Optional.empty();
    }

    public Map<String, String[]> createQueryRequest(String name, String instanceId) {
        Map<String, String[]> filters = new HashMap<>();
        filters.put(name, new String[] {"EQ", instanceId});
        filters.put("requestStatus", new String[] {"EQ", Status.COMPLETE.toString()});
        filters.put("action", new String[] {"EQ", "createInstance"});
        return filters;
    }

    public RequestInfo createRequestInfo() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setSuppressRollback(false);
        requestInfo.setSource(MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));
        requestInfo.setRequestorId(MDC.get(HttpHeadersConstants.REQUESTOR_ID));
        return requestInfo;
    }

    public RequestParameters createRequestParameters() {
        RequestParameters requestParams = new RequestParameters();
        requestParams.setaLaCarte(true);
        requestParams.setTestApi("GR_API");
        return requestParams;
    }


}
