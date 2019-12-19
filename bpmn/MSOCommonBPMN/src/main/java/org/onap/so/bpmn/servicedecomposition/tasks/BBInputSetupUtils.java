/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

package org.onap.so.bpmn.servicedecomposition.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.Configuration;
import org.onap.aai.domain.yang.Configurations;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.InstanceGroup;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.L3Networks;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aai.domain.yang.ServiceSubscription;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aai.domain.yang.VolumeGroups;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.MultipleObjectsFoundException;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.NoServiceInstanceFoundException;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

@Component("BBInputSetupUtils")
public class BBInputSetupUtils {

    private static final Logger logger = LoggerFactory.getLogger(BBInputSetupUtils.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String REQUEST_ERROR = "Could not find request.";
    private static final String DATA_LOAD_ERROR = "Could not process loading data from database";
    private static final String DATA_PARSE_ERROR = "Could not parse data";
    private static final String PROCESSING_DATA_NAME_EXECUTION_FLOWS = "flowExecutionPath";

    @Autowired
    protected CatalogDbClient catalogDbClient;

    @Autowired
    protected RequestsDbClient requestsDbClient;

    @Autowired
    protected InjectionHelper injectionHelper;

    public RelatedInstance getRelatedInstanceByType(RequestDetails requestDetails, ModelType modelType) {
        if (requestDetails.getRelatedInstanceList() != null) {
            for (RelatedInstanceList relatedInstanceList : requestDetails.getRelatedInstanceList()) {
                RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
                if (isRelatedInstanceValid(modelType, relatedInstance)) {
                    return relatedInstance;
                }
            }
        }
        return null;
    }

    public void updateInfraActiveRequestVnfId(InfraActiveRequests request, String vnfId) {
        updateInfraActiveRequest(request, (requestToChange) -> {
            requestToChange.setVnfId(vnfId);
            return requestToChange;
        });
    }

    public void updateInfraActiveRequestVfModuleId(InfraActiveRequests request, String vfModuleId) {
        updateInfraActiveRequest(request, (requestToChange) -> {
            requestToChange.setVfModuleId(vfModuleId);
            return requestToChange;
        });
    }

    public void updateInfraActiveRequestVolumeGroupId(InfraActiveRequests request, String volumeGroupId) {
        updateInfraActiveRequest(request, (requestToChange) -> {
            requestToChange.setVolumeGroupId(volumeGroupId);
            return requestToChange;
        });
    }

    public void updateInfraActiveRequestNetworkId(InfraActiveRequests request, String networkId) {
        updateInfraActiveRequest(request, (requestToChange) -> {
            requestToChange.setNetworkId(networkId);
            return requestToChange;
        });
    }

    public void persistFlowExecutionPath(String requestId, List<ExecuteBuildingBlock> flowsToExecute) {

        if (requestId != null) {
            List<String> flows = new ArrayList<>();
            ObjectMapper om = new ObjectMapper();
            try {
                for (ExecuteBuildingBlock ebb : flowsToExecute) {
                    flows.add(om.writeValueAsString(ebb));
                }
            } catch (JsonProcessingException e) {
                logger.error(DATA_PARSE_ERROR, e);
            }
            requestsDbClient.persistProcessingData(flows.toString(), requestId);
        } else {
            logger.debug(REQUEST_ERROR);
        }
    }

    public InfraActiveRequests loadInfraActiveRequestById(String requestId) {
        return requestsDbClient.getInfraActiveRequestbyRequestId(requestId);
    }

    public InfraActiveRequests loadOriginalInfraActiveRequestById(String requestId) {
        return loadInfraActiveRequestById(loadInfraActiveRequestById(requestId).getOriginalRequestId());
    }

    public List<ExecuteBuildingBlock> loadOriginalFlowExecutionPath(String requestId) {
        if (requestId != null) {
            InfraActiveRequests request = loadInfraActiveRequestById(requestId);
            if (request.getOriginalRequestId() != null) {
                return extractBuildingBlocksToExecute(request);
            } else {
                throw new RuntimeException("Original Request Id is null for record: " + requestId);
            }
        } else {
            throw new RuntimeException("Null Request Id Passed in");
        }
    }

    public Service getCatalogServiceByModelUUID(String modelUUID) {
        return catalogDbClient.getServiceByID(modelUUID);
    }

    public Service getCatalogServiceByModelVersionAndModelInvariantUUID(String modelVersion,
            String modelInvariantUUID) {
        return catalogDbClient.getServiceByModelVersionAndModelInvariantUUID(modelVersion, modelInvariantUUID);
    }

    public CollectionNetworkResourceCustomization getCatalogCollectionNetworkResourceCustByID(String key) {
        return catalogDbClient.getCollectionNetworkResourceCustomizationByID(key);
    }

    public NetworkCollectionResourceCustomization getCatalogNetworkCollectionResourceCustByID(
            String collectionCustomizationId) {
        return catalogDbClient.getNetworkCollectionResourceCustomizationByID(collectionCustomizationId);
    }

    public VfModuleCustomization getVfModuleCustomizationByModelCuztomizationUUID(String modelCustomizationUUID) {
        return catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(modelCustomizationUUID);
    }

    public CvnfcConfigurationCustomization getCvnfcConfigurationCustomization(String serviceModelUUID,
            String vnfCustomizationUuid, String vfModuleCustomizationUuid, String cvnfcCustomizationUuid) {
        return catalogDbClient.getCvnfcCustomization(serviceModelUUID, vnfCustomizationUuid, vfModuleCustomizationUuid,
                cvnfcCustomizationUuid);
    }

    public List<VnfcInstanceGroupCustomization> getVnfcInstanceGroups(String modelCustomizationUUID) {
        return catalogDbClient.getVnfcInstanceGroupsByVnfResourceCust(modelCustomizationUUID);
    }

    public Map<String, String> getURIKeysFromServiceInstance(String serviceInstanceId) {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId);
        return uri.getURIKeys();
    }

    public org.onap.aai.domain.yang.Customer getAAICustomer(String globalSubscriberId) {
        return injectionHelper.getAaiClient().get(org.onap.aai.domain.yang.Customer.class,
                AAIUriFactory.createResourceUri(AAIObjectType.CUSTOMER, globalSubscriberId)).orElse(null);
    }

    public ServiceSubscription getAAIServiceSubscription(String globalSubscriberId, String subscriptionServiceType) {
        if (isServiceSubscriptionValid(globalSubscriberId, subscriptionServiceType)) {
            return injectionHelper.getAaiClient().get(ServiceSubscription.class, AAIUriFactory
                    .createResourceUri(AAIObjectType.SERVICE_SUBSCRIPTION, globalSubscriberId, subscriptionServiceType))
                    .orElse(null);
        }
        return null;
    }

    public ServiceInstance getAAIServiceInstanceById(String serviceInstanceId) {
        return injectionHelper.getAaiClient()
                .get(ServiceInstance.class, AAIUriFactory
                        .createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId).depth(Depth.TWO))
                .orElse(null);
    }

    public org.onap.so.db.catalog.beans.InstanceGroup getCatalogInstanceGroup(String modelUUID) {
        return catalogDbClient.getInstanceGroupByModelUUID(modelUUID);
    }

    public List<CollectionResourceInstanceGroupCustomization> getCollectionResourceInstanceGroupCustomization(
            String modelCustomizationUUID) {
        return catalogDbClient.getCollectionResourceInstanceGroupCustomizationByModelCustUUID(modelCustomizationUUID);
    }

    public AAIResultWrapper getAAIResourceDepthOne(AAIResourceUri aaiResourceUri) {
        AAIResourceUri clonedUri = aaiResourceUri.clone();
        return injectionHelper.getAaiClient().get(clonedUri.depth(Depth.ONE));
    }

    public AAIResultWrapper getAAIResourceDepthTwo(AAIResourceUri aaiResourceUri) {
        AAIResourceUri clonedUri = aaiResourceUri.clone();
        return injectionHelper.getAaiClient().get(clonedUri.depth(Depth.TWO));
    }

    public ServiceInstances getAAIServiceInstancesGloballyByName(String serviceInstanceName) {
        return getFromAAI(
                () -> AAIUriFactory.createNodesUri(AAIObjectPlurals.SERVICE_INSTANCE)
                        .queryParam("service-instance-name", serviceInstanceName),
                ServiceInstances.class, "Service Instance");
    }

    public GenericVnfs getAAIVnfsGloballyByName(String vnfName) {
        return getFromAAI(
                () -> AAIUriFactory.createNodesUri(AAIObjectPlurals.GENERIC_VNF).queryParam("vnf-name", vnfName),
                GenericVnfs.class, "GenericVnfs");
    }

    public Configuration getAAIConfiguration(String configurationId) {
        return getFromAAI(
                () -> AAIUriFactory.createResourceUri(AAIObjectType.CONFIGURATION, configurationId).depth(Depth.ONE),
                Configuration.class, "Configuration");
    }

    public GenericVnf getAAIGenericVnf(String vnfId) {
        return getFromAAI(() -> AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId).depth(Depth.ONE),
                GenericVnf.class, "Generic Vnf");
    }

    public VpnBinding getAAIVpnBinding(String vpnBindingId) {
        return getFromAAI(
                () -> AAIUriFactory.createResourceUri(AAIObjectType.VPN_BINDING, vpnBindingId).depth(Depth.ONE),
                VpnBinding.class, "VpnBinding");
    }

    public VolumeGroup getAAIVolumeGroup(String cloudOwnerId, String cloudRegionId, String volumeGroupId) {
        return getFromAAI(() -> AAIUriFactory
                .createResourceUri(AAIObjectType.VOLUME_GROUP, cloudOwnerId, cloudRegionId, volumeGroupId)
                .depth(Depth.ONE), VolumeGroup.class, "Volume Group");
    }

    public VfModule getAAIVfModule(String vnfId, String vfModuleId) {
        return getFromAAI(
                () -> AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnfId, vfModuleId).depth(Depth.ONE),
                VfModule.class, "VfModule");
    }

    public L3Network getAAIL3Network(String networkId) {
        return getFromAAI(() -> AAIUriFactory.createResourceUri(AAIObjectType.L3_NETWORK, networkId).depth(Depth.ONE),
                L3Network.class, "L3Network");
    }

    public ServiceInstance getAAIServiceInstanceByName(String serviceInstanceName, Customer customer)
            throws MultipleObjectsFoundException {
        ServiceInstances aaiServiceInstances = getAAIServiceInstancesByName(serviceInstanceName, customer);

        if (aaiServiceInstances == null) {
            return null;
        } else if (aaiServiceInstances.getServiceInstance().size() > 1) {
            throw new MultipleObjectsFoundException("Multiple Service Instances Returned");
        }
        return aaiServiceInstances.getServiceInstance().get(0);
    }

    public Optional<ServiceInstance> getAAIServiceInstanceByName(String globalCustomerId, String serviceType,
            String serviceInstanceName) throws MultipleObjectsFoundException {
        ServiceInstances aaiServiceInstances =
                getAAIServiceInstancesByName(globalCustomerId, serviceType, serviceInstanceName);

        if (aaiServiceInstances != null && aaiServiceInstances.getServiceInstance().size() == 1) {
            return Optional.of(aaiServiceInstances.getServiceInstance().get(0));
        } else if (aaiServiceInstances != null && aaiServiceInstances.getServiceInstance().size() > 1) {
            String message = String.format(
                    "Multiple service instances found for customer-id: %s, service-type: %s and service-instance-name: %s.",
                    globalCustomerId, serviceType, serviceInstanceName);
            throw new MultipleObjectsFoundException(message);
        }
        logger.debug("No ServiceInstance was found");
        return Optional.empty();
    }

    public Optional<ServiceInstance> getRelatedServiceInstanceFromInstanceGroup(String instanceGroupId)
            throws MultipleObjectsFoundException, NoServiceInstanceFoundException {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroupId);
        uri.relatedTo(AAIObjectPlurals.SERVICE_INSTANCE);
        Optional<ServiceInstances> serviceInstances = injectionHelper.getAaiClient().get(ServiceInstances.class, uri);

        if (isOneServiceInstancePresent(serviceInstances)) {
            return Optional.of(serviceInstances.get().getServiceInstance().get(0));
        } else if (isMoreThanOneServiceInstancePresent(serviceInstances)) {
            String message =
                    String.format("Mulitple service instances were found for instance-group-id: %s.", instanceGroupId);
            throw new MultipleObjectsFoundException(message);
        } else if (serviceInstances.isPresent() && serviceInstances.get().getServiceInstance().isEmpty()) {
            throw new NoServiceInstanceFoundException("No ServiceInstances Returned");
        }
        logger.debug("No ServiceInstances were found");
        return Optional.empty();
    }

    public Optional<L3Network> getRelatedNetworkByNameFromServiceInstance(String serviceInstanceId, String networkName)
            throws MultipleObjectsFoundException {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId);
        uri.relatedTo(AAIObjectPlurals.L3_NETWORK).queryParam("network-name", networkName);
        Optional<L3Networks> networks = injectionHelper.getAaiClient().get(L3Networks.class, uri);

        if (isOneL3NetworkPresent(networks)) {
            return Optional.of(networks.get().getL3Network().get(0));
        } else if (isMoreThanOneL3NetworkPresent(networks)) {
            String message = String.format("Multiple networks found for service-instance-id: %s and network-name: %s.",
                    serviceInstanceId, networkName);
            throw new MultipleObjectsFoundException(message);
        }
        logger.debug("No Networks matched by name");
        return Optional.empty();
    }

    public Optional<GenericVnf> getRelatedVnfByNameFromServiceInstance(String serviceInstanceId, String vnfName)
            throws MultipleObjectsFoundException {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId);
        uri.relatedTo(AAIObjectPlurals.GENERIC_VNF).queryParam("vnf-name", vnfName);
        Optional<GenericVnfs> vnfs = injectionHelper.getAaiClient().get(GenericVnfs.class, uri);

        if (isOneGenericVnfPresent(vnfs)) {
            return Optional.of(vnfs.get().getGenericVnf().get(0));
        } else if (isMoreThanOneGenericVnfPresent(vnfs)) {
            String message = String.format("Multiple vnfs found for service-instance-id: %s and vnf-name: %s.",
                    serviceInstanceId, vnfName);
            throw new MultipleObjectsFoundException(message);
        }
        logger.debug("No Vnfs matched by name");
        return Optional.empty();
    }

    public Optional<VolumeGroup> getRelatedVolumeGroupByNameFromVnf(String vnfId, String volumeGroupName)
            throws MultipleObjectsFoundException {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, vnfId);
        uri.relatedTo(AAIObjectPlurals.VOLUME_GROUP).queryParam("volume-group-name", volumeGroupName);
        Optional<VolumeGroups> volumeGroups = injectionHelper.getAaiClient().get(VolumeGroups.class, uri);

        if (isOneVolumeGroupPresent(volumeGroups)) {
            return Optional.of(volumeGroups.get().getVolumeGroup().get(0));
        } else if (isMoreThanOneVolumeGroupPresent(volumeGroups)) {
            String message = String.format("Multiple volume-groups found for vnf-id: %s and volume-group-name: %s.",
                    vnfId, volumeGroupName);
            throw new MultipleObjectsFoundException(message);
        }
        logger.debug("No VolumeGroups matched by name");
        return Optional.empty();
    }

    public Optional<VolumeGroup> getRelatedVolumeGroupByNameFromVfModule(String vnfId, String vfModuleId,
            String volumeGroupName) throws MultipleObjectsFoundException {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnfId, vfModuleId);
        uri.relatedTo(AAIObjectPlurals.VOLUME_GROUP).queryParam("volume-group-name", volumeGroupName);
        Optional<VolumeGroups> volumeGroups = injectionHelper.getAaiClient().get(VolumeGroups.class, uri);

        if (isOneVolumeGroupPresent(volumeGroups)) {
            return Optional.of(volumeGroups.get().getVolumeGroup().get(0));
        } else if (isMoreThanOneVolumeGroupPresent(volumeGroups)) {
            String message = String.format(
                    "Multiple volume-groups found for vnf-id: %s, vf-module-id: %s and volume-group-name: %s.", vnfId,
                    vfModuleId, volumeGroupName);
            throw new MultipleObjectsFoundException(message);
        }
        logger.debug("No VolumeGroups matched by name");
        return Optional.empty();
    }

    public Optional<VolumeGroup> getRelatedVolumeGroupFromVfModule(String vnfId, String vfModuleId) throws Exception {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.VF_MODULE, vnfId, vfModuleId);
        uri.relatedTo(AAIObjectPlurals.VOLUME_GROUP);
        Optional<VolumeGroups> volumeGroups = injectionHelper.getAaiClient().get(VolumeGroups.class, uri);

        if (isOneVolumeGroupPresent(volumeGroups)) {
            return Optional.of(volumeGroups.get().getVolumeGroup().get(0));
        } else if (isMoreThanOneVolumeGroupPresent(volumeGroups)) {
            String message = String.format("Multiple volume-groups found for vnf-id: %s and vf-module-id: %s.", vnfId,
                    vfModuleId);
            throw new Exception(message);
        }
        logger.debug("VfModule does not have a volume group attached");
        return Optional.empty();
    }

    public Optional<Configuration> getRelatedConfigurationByNameFromServiceInstance(String serviceInstanceId,
            String configurationName) throws MultipleObjectsFoundException {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, serviceInstanceId);
        uri.relatedTo(AAIObjectPlurals.CONFIGURATION).queryParam("configuration-name", configurationName);
        Optional<Configurations> configurations = injectionHelper.getAaiClient().get(Configurations.class, uri);

        if (isOneConfigurationPresent(configurations)) {
            return Optional.of(configurations.get().getConfiguration().get(0));
        } else if (isMoreThanOneConfigurationPresent(configurations)) {
            String message = String.format(
                    "Multiple configurations found for service-instance-d: %s and configuration-name: %s.",
                    serviceInstanceId, configurationName);
            throw new MultipleObjectsFoundException(message);
        }
        logger.debug("No Configurations matched by name");
        return Optional.empty();
    }

    public Optional<VpnBinding> getAICVpnBindingFromNetwork(L3Network aaiLocalNetwork) {
        AAIResultWrapper networkWrapper = new AAIResultWrapper(aaiLocalNetwork);
        if (isNetworkWrapperValid(networkWrapper)) {
            return getAAIResourceDepthOne(
                    networkWrapper.getRelationships().get().getRelatedUris(AAIObjectType.VPN_BINDING).get(0))
                            .asBean(VpnBinding.class);
        }
        return Optional.empty();
    }

    public boolean existsAAINetworksGloballyByName(String networkName) {
        return existsAAIRecordGloballyByName(networkName, "network-name",
                () -> AAIUriFactory.createResourceUri(AAIObjectPlurals.L3_NETWORK));
    }

    public boolean existsAAIVfModuleGloballyByName(String vfModuleName) {
        return existsAAIRecordGloballyByName(vfModuleName, "vf-module-name",
                () -> AAIUriFactory.createNodesUri(AAIObjectPlurals.VF_MODULE));
    }

    public boolean existsAAIConfigurationGloballyByName(String configurationName) {
        return existsAAIRecordGloballyByName(configurationName, "configuration-name",
                () -> AAIUriFactory.createResourceUri(AAIObjectPlurals.CONFIGURATION));
    }

    public boolean existsAAIVolumeGroupGloballyByName(String volumeGroupName) {
        return existsAAIRecordGloballyByName(volumeGroupName, "volume-group-name",
                () -> AAIUriFactory.createNodesUri(AAIObjectPlurals.VOLUME_GROUP));
    }

    protected ServiceInstances getAAIServiceInstancesByName(String globalCustomerId, String serviceType,
            String serviceInstanceName) {
        return getFromAAI(
                () -> AAIUriFactory.createResourceUri(AAIObjectPlurals.SERVICE_INSTANCE, globalCustomerId, serviceType)
                        .queryParam("service-instance-name", serviceInstanceName).depth(Depth.TWO),
                ServiceInstances.class, "Service Instances");
    }

    protected ServiceInstances getAAIServiceInstancesByName(String serviceInstanceName, Customer customer) {

        return getFromAAI(
                () -> AAIUriFactory
                        .createResourceUri(AAIObjectPlurals.SERVICE_INSTANCE, customer.getGlobalCustomerId(),
                                customer.getServiceSubscription().getServiceType())
                        .queryParam("service-instance-name", serviceInstanceName).depth(Depth.TWO),
                ServiceInstances.class, "Service Instance");
    }

    protected RequestDetails getRequestDetails(String requestId) throws IOException {
        if (requestId != null && !requestId.isEmpty()) {
            InfraActiveRequests activeRequest = this.getInfraActiveRequest(requestId);
            String requestBody = activeRequest.getRequestBody().replaceAll("\\\\", "");
            objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
            return objectMapper.readValue(requestBody, RequestDetails.class);
        }
        return null;
    }

    protected InfraActiveRequests getInfraActiveRequest(String requestId) {
        if (requestId != null && !requestId.isEmpty()) {
            return requestsDbClient.getInfraActiveRequestbyRequestId(requestId);
        }
        return null;
    }

    protected CloudRegion getCloudRegion(CloudConfiguration cloudConfiguration) {
        if (cloudConfiguration != null) {
            String cloudRegionId = cloudConfiguration.getLcpCloudRegionId();
            String cloudOwner = cloudConfiguration.getCloudOwner();
            if (isCloudConfigurationValid(cloudRegionId, cloudOwner)) {
                return injectionHelper.getAaiClient().get(CloudRegion.class, AAIUriFactory
                        .createResourceUri(AAIObjectType.CLOUD_REGION, cloudOwner, cloudRegionId).depth(Depth.TWO))
                        .orElse(null);
            }
        }
        return null;
    }

    protected InstanceGroup getAAIInstanceGroup(String instanceGroupId) {
        return injectionHelper.getAaiClient().get(InstanceGroup.class,
                AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, instanceGroupId)).orElse(null);
    }

    protected ServiceInstance getAAIServiceInstanceByIdAndCustomer(String globalCustomerId, String serviceType,
            String serviceInstanceId) {
        return injectionHelper.getAaiClient().get(ServiceInstance.class, AAIUriFactory
                .createResourceUri(AAIObjectType.SERVICE_INSTANCE, globalCustomerId, serviceType, serviceInstanceId)
                .depth(Depth.TWO)).orElse(null);
    }

    private void updateInfraActiveRequest(InfraActiveRequests request,
            Function<InfraActiveRequests, InfraActiveRequests> requestChangeFunction) {
        if (request != null) {
            requestsDbClient.updateInfraActiveRequests(requestChangeFunction.apply(request));
        } else {
            logger.debug(REQUEST_ERROR);
        }
    }

    private List<ExecuteBuildingBlock> extractBuildingBlocksToExecute(InfraActiveRequests request) {
        RequestProcessingData requestProcessingData = requestsDbClient.getRequestProcessingDataBySoRequestIdAndName(
                request.getOriginalRequestId(), PROCESSING_DATA_NAME_EXECUTION_FLOWS);
        try {
            ObjectMapper om = new ObjectMapper();
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            return om.readValue(requestProcessingData.getValue(),
                    typeFactory.constructCollectionType(List.class, ExecuteBuildingBlock.class));
        } catch (Exception e) {
            logger.error(DATA_LOAD_ERROR, e);
            throw new RuntimeException("Error Loading Original Request Data", e);
        }
    }

    private <T> T getFromAAI(Supplier<AAIResourceUri> resourceUriCreator, Class<T> clazz, String typeName) {
        return injectionHelper.getAaiClient().get(clazz, resourceUriCreator.get()).orElseGet(() -> {
            logger.debug(String.format("No %s matched.", typeName));
            return null;
        });
    }

    private boolean isNetworkWrapperValid(AAIResultWrapper networkWrapper) {
        return networkWrapper.getRelationships().isPresent()
                && !networkWrapper.getRelationships().get().getRelatedUris(AAIObjectType.VPN_BINDING).isEmpty();
    }

    private boolean isCloudConfigurationValid(String cloudRegionId, String cloudOwner) {
        return cloudRegionId != null && cloudOwner != null && !cloudRegionId.isEmpty() && !cloudOwner.isEmpty();
    }

    private boolean isRelatedInstanceValid(ModelType modelType, RelatedInstance relatedInstance) {
        return relatedInstance != null && relatedInstance.getModelInfo() != null
                && relatedInstance.getModelInfo().getModelType() != null
                && relatedInstance.getModelInfo().getModelType().equals(modelType);
    }

    private boolean isServiceSubscriptionValid(String globalSubscriberId, String subscriptionServiceType) {
        return globalSubscriberId != null && !globalSubscriberId.isEmpty() && subscriptionServiceType != null
                && !subscriptionServiceType.isEmpty();
    }

    private boolean existsAAIRecordGloballyByName(String recordName, String queryParamName,
            Supplier<AAIResourceUri> uriProvider) {
        AAIResourceUri uri = uriProvider.get().queryParam(queryParamName, recordName);
        return injectionHelper.getAaiClient().exists(uri);
    }

    private boolean isOneConfigurationPresent(Optional<Configurations> configurations) {
        return configurations.isPresent() && configurations.get().getConfiguration().size() == 1;
    }

    private boolean isMoreThanOneConfigurationPresent(Optional<Configurations> configurations) {
        return configurations.isPresent() && configurations.get().getConfiguration().size() > 1;
    }

    private boolean isMoreThanOneServiceInstancePresent(Optional<ServiceInstances> serviceInstances) {
        return serviceInstances.isPresent() && serviceInstances.get().getServiceInstance().size() > 1;
    }

    private boolean isOneServiceInstancePresent(Optional<ServiceInstances> serviceInstances) {
        return serviceInstances.isPresent() && serviceInstances.get().getServiceInstance().size() == 1;
    }

    private boolean isMoreThanOneL3NetworkPresent(Optional<L3Networks> networks) {
        return networks.isPresent() && networks.get().getL3Network().size() > 1;
    }

    private boolean isOneL3NetworkPresent(Optional<L3Networks> networks) {
        return networks.isPresent() && networks.get().getL3Network().size() == 1;
    }

    private boolean isMoreThanOneGenericVnfPresent(Optional<GenericVnfs> vnfs) {
        return vnfs.isPresent() && vnfs.get().getGenericVnf().size() > 1;
    }

    private boolean isOneGenericVnfPresent(Optional<GenericVnfs> vnfs) {
        return vnfs.isPresent() && vnfs.get().getGenericVnf().size() == 1;
    }

    private boolean isOneVolumeGroupPresent(Optional<VolumeGroups> volumeGroups) {
        return volumeGroups.isPresent() && volumeGroups.get().getVolumeGroup().size() == 1;
    }

    private boolean isMoreThanOneVolumeGroupPresent(Optional<VolumeGroups> volumeGroups) {
        return volumeGroups.isPresent() && volumeGroups.get().getVolumeGroup().size() > 1;
    }
}
