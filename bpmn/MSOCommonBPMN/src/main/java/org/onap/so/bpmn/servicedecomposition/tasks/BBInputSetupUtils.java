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

package org.onap.so.bpmn.servicedecomposition.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.Configuration;
import org.onap.aai.domain.yang.Configurations;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.InstanceGroup;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.L3Networks;
import org.onap.aai.domain.yang.Pnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aai.domain.yang.ServiceSubscription;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aai.domain.yang.VolumeGroups;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIFluentSingleType;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.MultipleObjectsFoundException;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.NoServiceInstanceFoundException;
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
    private static final String REQUEST_ERROR = "Could not find request.";
    private static final String DATA_LOAD_ERROR = "Could not process loading data from database";
    private static final String DATA_PARSE_ERROR = "Could not parse data";
    private static final String PROCESSING_DATA_NAME_EXECUTION_FLOWS = "flowExecutionPath";
    private static final ObjectMapper mapper;
    private static final ObjectMapper mapperRootValue;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapperRootValue = new ObjectMapper();
        mapperRootValue.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapperRootValue.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapperRootValue.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        mapperRootValue.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
    }

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
                if (relatedInstance != null && relatedInstance.getModelInfo() != null
                        && relatedInstance.getModelInfo().getModelType() != null
                        && relatedInstance.getModelInfo().getModelType().equals(modelType)) {
                    return relatedInstance;
                }
            }
        }
        return null;
    }

    public void updateInfraActiveRequestVnfId(InfraActiveRequests request, String vnfId) {
        if (request != null) {
            request.setVnfId(vnfId);
            this.requestsDbClient.updateInfraActiveRequests(request);
        } else {
            logger.debug(REQUEST_ERROR);
        }
    }

    public void updateInfraActiveRequestVfModuleId(InfraActiveRequests request, String vfModuleId) {
        if (request != null) {
            request.setVfModuleId(vfModuleId);
            this.requestsDbClient.updateInfraActiveRequests(request);
        } else {
            logger.debug(REQUEST_ERROR);
        }
    }

    public void updateInfraActiveRequestVolumeGroupId(InfraActiveRequests request, String volumeGroupId) {
        if (request != null) {
            request.setVolumeGroupId(volumeGroupId);
            this.requestsDbClient.updateInfraActiveRequests(request);
        } else {
            logger.debug(REQUEST_ERROR);
        }
    }

    public void updateInfraActiveRequestNetworkId(InfraActiveRequests request, String networkId) {
        if (request != null) {
            request.setNetworkId(networkId);
            this.requestsDbClient.updateInfraActiveRequests(request);
        } else {
            logger.debug(REQUEST_ERROR);
        }
    }

    public void persistFlowExecutionPath(String requestId, List<ExecuteBuildingBlock> flowsToExecute) {

        if (requestId != null) {
            List<String> flows = new ArrayList<>();
            try {
                for (ExecuteBuildingBlock ebb : flowsToExecute) {
                    flows.add(mapper.writeValueAsString(ebb));
                }
            } catch (JsonProcessingException e) {
                logger.error(DATA_PARSE_ERROR, e);
            }

            this.requestsDbClient.persistProcessingData(flows.toString(), requestId);
        } else {
            logger.debug(REQUEST_ERROR);
        }
    }

    public InfraActiveRequests loadInfraActiveRequestById(String requestId) {

        return this.requestsDbClient.getInfraActiveRequestbyRequestId(requestId);
    }

    public InfraActiveRequests loadOriginalInfraActiveRequestById(String requestId) {
        return this.requestsDbClient.getInfraActiveRequestbyRequestId(
                this.requestsDbClient.getInfraActiveRequestbyRequestId(requestId).getOriginalRequestId());
    }

    public List<ExecuteBuildingBlock> loadOriginalFlowExecutionPath(String requestId) {
        if (requestId != null) {
            InfraActiveRequests request = loadInfraActiveRequestById(requestId);
            if (request.getOriginalRequestId() != null) {
                RequestProcessingData requestProcessingData =
                        this.requestsDbClient.getRequestProcessingDataBySoRequestIdAndName(
                                request.getOriginalRequestId(), PROCESSING_DATA_NAME_EXECUTION_FLOWS);
                try {
                    TypeFactory typeFactory = mapper.getTypeFactory();
                    return mapper.readValue(requestProcessingData.getValue(),
                            typeFactory.constructCollectionType(List.class, ExecuteBuildingBlock.class));
                } catch (Exception e) {
                    logger.error(DATA_LOAD_ERROR, e);
                    throw new RuntimeException("Error Loading Original Request Data", e);
                }
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
        AAIResourceUri uri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId));
        return uri.getURIKeys();
    }

    protected RequestDetails getRequestDetails(String requestId) throws IOException {
        if (requestId != null && !requestId.isEmpty()) {
            InfraActiveRequests activeRequest = this.getInfraActiveRequest(requestId);
            String requestBody = activeRequest.getRequestBody().replaceAll("\\\\", "");
            return mapperRootValue.readValue(requestBody, RequestDetails.class);
        } else {
            return null;
        }
    }

    protected InfraActiveRequests getInfraActiveRequest(String requestId) {
        if (requestId != null && !requestId.isEmpty()) {
            return requestsDbClient.getInfraActiveRequestbyRequestId(requestId);
        } else {
            return null;
        }
    }

    protected CloudRegion getCloudRegion(CloudConfiguration cloudConfiguration) {
        if (cloudConfiguration != null) {
            String cloudRegionId = cloudConfiguration.getLcpCloudRegionId();
            String cloudOwner = cloudConfiguration.getCloudOwner();
            if (cloudRegionId != null && cloudOwner != null && !cloudRegionId.isEmpty() && !cloudOwner.isEmpty()) {
                return injectionHelper.getAaiClient().get(CloudRegion.class,
                        AAIUriFactory.createResourceUri(
                                AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(cloudOwner, cloudRegionId))
                                .depth(Depth.ONE).nodesOnly(true))
                        .orElse(null);

            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public InstanceGroup getAAIInstanceGroup(String instanceGroupId) {
        return injectionHelper.getAaiClient()
                .get(InstanceGroup.class,
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroupId)))
                .orElse(null);
    }

    public org.onap.aai.domain.yang.Customer getAAICustomer(String globalSubscriberId) {
        return injectionHelper.getAaiClient()
                .get(org.onap.aai.domain.yang.Customer.class,
                        AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId)))
                .orElse(null);
    }

    public ServiceSubscription getAAIServiceSubscription(String globalSubscriberId, String subscriptionServiceType) {

        if (globalSubscriberId == null || globalSubscriberId.equals("") || subscriptionServiceType == null
                || subscriptionServiceType.equals("")) {
            return null;
        } else {
            return injectionHelper
                    .getAaiClient().get(ServiceSubscription.class, AAIUriFactory.createResourceUri(AAIFluentTypeBuilder
                            .business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType)))
                    .orElse(null);
        }

    }

    public ServiceInstance getAAIServiceInstanceById(String serviceInstanceId) {
        return injectionHelper.getAaiClient()
                .get(ServiceInstance.class, AAIUriFactory
                        .createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId)).depth(Depth.TWO))
                .orElse(null);
    }

    protected ServiceInstance getAAIServiceInstanceByIdAndCustomer(String globalCustomerId, String serviceType,
            String serviceInstanceId) {
        return injectionHelper.getAaiClient().get(ServiceInstance.class,
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalCustomerId)
                        .serviceSubscription(serviceType).serviceInstance(serviceInstanceId)).depth(Depth.TWO))
                .orElse(null);
    }

    public org.onap.aai.domain.yang.ServiceInstance getAAIServiceInstanceByName(String serviceInstanceName,
            Customer customer) throws Exception {
        Optional<org.onap.aai.domain.yang.ServiceInstance> aaiServiceInstance = injectionHelper.getAaiClient().getOne(
                ServiceInstances.class, org.onap.aai.domain.yang.ServiceInstance.class,
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId())
                        .serviceSubscription(customer.getServiceSubscription().getServiceType()).serviceInstances())
                        .queryParam("service-instance-name", serviceInstanceName).depth(Depth.TWO));

        return aaiServiceInstance.orElse(null);
    }

    public Optional<ServiceInstance> getAAIServiceInstanceByName(String globalCustomerId, String serviceType,
            String serviceInstanceName) {

        return injectionHelper.getAaiClient().getOne(ServiceInstances.class, ServiceInstance.class,
                AAIUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.business().customer(globalCustomerId)
                                .serviceSubscription(serviceType).serviceInstances())
                        .queryParam("service-instance-name", serviceInstanceName).depth(Depth.TWO));
    }

    public org.onap.so.db.catalog.beans.InstanceGroup getCatalogInstanceGroup(String modelUUID) {
        return this.catalogDbClient.getInstanceGroupByModelUUID(modelUUID);
    }

    public List<CollectionResourceInstanceGroupCustomization> getCollectionResourceInstanceGroupCustomization(
            String modelCustomizationUUID) {
        return this.catalogDbClient
                .getCollectionResourceInstanceGroupCustomizationByModelCustUUID(modelCustomizationUUID);
    }

    public AAIResultWrapper getAAIResourceDepthOne(AAIResourceUri aaiResourceUri) {
        AAIResourceUri clonedUri = aaiResourceUri.clone();
        return this.injectionHelper.getAaiClient().get(clonedUri.depth(Depth.ONE));
    }

    public AAIResultWrapper getAAIResourceDepthTwo(AAIResourceUri aaiResourceUri) {
        AAIResourceUri clonedUri = aaiResourceUri.clone();
        return this.injectionHelper.getAaiClient().get(clonedUri.depth(Depth.TWO));
    }

    public Configuration getAAIConfiguration(String configurationId) {
        return this.getConcreteAAIResource(Configuration.class,
                AAIFluentTypeBuilder.network().configuration(configurationId));
    }

    public GenericVnf getAAIGenericVnf(String vnfId) {
        return getConcreteAAIResource(GenericVnf.class, AAIFluentTypeBuilder.network().genericVnf(vnfId));
    }


    public Pnf getAAIPnf(String pnfId) {
        return getConcreteAAIResource(Pnf.class, AAIFluentTypeBuilder.network().pnf(pnfId));
    }

    public VpnBinding getAAIVpnBinding(String vpnBindingId) {
        return getConcreteAAIResource(VpnBinding.class, AAIFluentTypeBuilder.network().vpnBinding(vpnBindingId));
    }

    public VolumeGroup getAAIVolumeGroup(String cloudOwnerId, String cloudRegionId, String volumeGroupId) {
        return getConcreteAAIResource(VolumeGroup.class, AAIFluentTypeBuilder.cloudInfrastructure()
                .cloudRegion(cloudOwnerId, cloudRegionId).volumeGroup(volumeGroupId));
    }

    public VfModule getAAIVfModule(String vnfId, String vfModuleId) {
        return getConcreteAAIResource(VfModule.class,
                AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
    }

    public L3Network getAAIL3Network(String networkId) {
        return getConcreteAAIResource(L3Network.class, AAIFluentTypeBuilder.network().l3Network(networkId));
    }

    private <T> T getConcreteAAIResource(Class<T> clazz, AAIFluentSingleType type) {
        return injectionHelper.getAaiClient().get(clazz, AAIUriFactory.createResourceUri(type).depth(Depth.ONE))
                .orElseGet(() -> {
                    logger.debug("No resource of type: {} matched by ids: {}", type.build().typeName(),
                            Arrays.toString(type.values()));
                    return null;
                });
    }

    public Optional<ServiceInstance> getRelatedServiceInstanceFromInstanceGroup(String instanceGroupId)
            throws Exception {
        AAIPluralResourceUri uri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().instanceGroup(instanceGroupId))
                        .relatedTo(Types.SERVICE_INSTANCES.getFragment());
        Optional<ServiceInstances> serviceInstances = injectionHelper.getAaiClient().get(ServiceInstances.class, uri);
        ServiceInstance serviceInstance = null;
        if (!serviceInstances.isPresent()) {
            logger.debug("No ServiceInstances were found");
            return Optional.empty();
        } else {
            if (serviceInstances.get().getServiceInstance().isEmpty()) {
                throw new NoServiceInstanceFoundException("No ServiceInstances Returned");
            } else if (serviceInstances.get().getServiceInstance().size() > 1) {
                String message = String.format("Mulitple service instances were found for instance-group-id: %s.",
                        instanceGroupId);
                throw new MultipleObjectsFoundException(message);
            } else {
                serviceInstance = serviceInstances.get().getServiceInstance().get(0);
            }
            return Optional.of(serviceInstance);
        }
    }

    public Optional<L3Network> getRelatedNetworkByNameFromServiceInstance(String serviceInstanceId, String networkName)
            throws MultipleObjectsFoundException {
        AAIPluralResourceUri uri =
                AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))
                        .relatedTo(Types.L3_NETWORKS.getFragment()).queryParam("network-name", networkName);
        Optional<L3Networks> networks = injectionHelper.getAaiClient().get(L3Networks.class, uri);
        L3Network network = null;
        if (!networks.isPresent()) {
            logger.debug("No Networks matched by name");
            return Optional.empty();
        } else {
            if (networks.get().getL3Network().size() > 1) {
                String message =
                        String.format("Multiple networks found for service-instance-id: %s and network-name: %s.",
                                serviceInstanceId, networkName);
                throw new MultipleObjectsFoundException(message);
            } else {
                network = networks.get().getL3Network().get(0);
            }
            return Optional.of(network);
        }
    }

    public Optional<GenericVnf> getRelatedVnfByNameFromServiceInstance(String serviceInstanceId, String vnfName) {
        AAIPluralResourceUri uri =
                AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))
                        .relatedTo(Types.GENERIC_VNFS.getFragment()).queryParam("vnf-name", vnfName);
        return injectionHelper.getAaiClient().getOne(GenericVnfs.class, GenericVnf.class, uri);

    }

    public Optional<VolumeGroup> getRelatedVolumeGroupByNameFromVnf(String vnfId, String volumeGroupName) {
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
                .relatedTo(Types.VOLUME_GROUPS.getFragment()).queryParam("volume-group-name", volumeGroupName);
        return injectionHelper.getAaiClient().getOne(VolumeGroups.class, VolumeGroup.class, uri);
    }

    public Optional<VolumeGroup> getRelatedVolumeGroupByIdFromVnf(String vnfId, String volumeGroupId) {
        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId))
                .relatedTo(Types.VOLUME_GROUPS.getFragment()).queryParam("volume-group-id", volumeGroupId);
        return injectionHelper.getAaiClient().getOne(VolumeGroups.class, VolumeGroup.class, uri);
    }

    public Optional<VolumeGroup> getRelatedVolumeGroupByNameFromVfModule(String vnfId, String vfModuleId,
            String volumeGroupName) throws Exception {
        AAIPluralResourceUri uri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId))
                        .relatedTo(Types.VOLUME_GROUPS.getFragment()).queryParam("volume-group-name", volumeGroupName);
        return injectionHelper.getAaiClient().getOne(VolumeGroups.class, VolumeGroup.class, uri);
    }

    public Optional<VolumeGroup> getRelatedVolumeGroupFromVfModule(String vnfId, String vfModuleId) throws Exception {
        AAIPluralResourceUri uri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId))
                        .relatedTo(Types.VOLUME_GROUPS.getFragment());
        return injectionHelper.getAaiClient().getOne(VolumeGroups.class, VolumeGroup.class, uri);
    }

    public Optional<org.onap.aai.domain.yang.VpnBinding> getAICVpnBindingFromNetwork(
            org.onap.aai.domain.yang.L3Network aaiLocalNetwork) {
        AAIResultWrapper networkWrapper = new AAIResultWrapper(aaiLocalNetwork);
        if (networkWrapper.getRelationships().isPresent()
                && !networkWrapper.getRelationships().get().getRelatedUris(Types.VPN_BINDING).isEmpty()) {
            return getAAIResourceDepthOne(
                    networkWrapper.getRelationships().get().getRelatedUris(Types.VPN_BINDING).get(0))
                            .asBean(org.onap.aai.domain.yang.VpnBinding.class);
        }
        return Optional.empty();
    }

    public ServiceInstances getAAIServiceInstancesGloballyByName(String serviceInstanceName) {

        return injectionHelper.getAaiClient()
                .get(ServiceInstances.class, AAIUriFactory.createNodesUri(Types.SERVICE_INSTANCES.getFragment())
                        .queryParam("service-instance-name", serviceInstanceName))
                .orElseGet(() -> {
                    logger.debug("No Service Instance matched by name");
                    return null;
                });
    }

    public boolean existsAAINetworksGloballyByName(String networkName) {

        AAIPluralResourceUri l3networkUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Networks())
                .queryParam("network-name", networkName);
        AAIResourcesClient aaiRC = injectionHelper.getAaiClient();
        return aaiRC.exists(l3networkUri);
    }

    public boolean existsAAIVfModuleGloballyByName(String vfModuleName) {
        AAIPluralResourceUri vfModuleUri =
                AAIUriFactory.createNodesUri(Types.VF_MODULES.getFragment()).queryParam("vf-module-name", vfModuleName);
        return injectionHelper.getAaiClient().exists(vfModuleUri);
    }

    public boolean existsAAIConfigurationGloballyByName(String configurationName) {
        AAIPluralResourceUri configUri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().configurations())
                        .queryParam("configuration-name", configurationName);
        return injectionHelper.getAaiClient().exists(configUri);
    }

    public boolean existsAAIVolumeGroupGloballyByName(String volumeGroupName) {
        AAIPluralResourceUri volumeGroupUri = AAIUriFactory.createNodesUri(Types.VOLUME_GROUPS.getFragment())
                .queryParam("volume-group-name", volumeGroupName);
        return injectionHelper.getAaiClient().exists(volumeGroupUri);
    }

    public GenericVnfs getAAIVnfsGloballyByName(String vnfName) {

        return injectionHelper.getAaiClient()
                .get(GenericVnfs.class,
                        AAIUriFactory.createNodesUri(Types.GENERIC_VNFS.getFragment()).queryParam("vnf-name", vnfName))
                .orElseGet(() -> {
                    logger.debug("No GenericVnfs matched by name");
                    return null;
                });
    }

    public Optional<Configuration> getRelatedConfigurationByNameFromServiceInstance(String serviceInstanceId,
            String configurationName) {
        AAIPluralResourceUri uri = AAIUriFactory
                .createResourceUri(Types.SERVICE_INSTANCE.getFragment(serviceInstanceId))
                .relatedTo(Types.CONFIGURATIONS.getFragment()).queryParam("configuration-name", configurationName);
        return injectionHelper.getAaiClient().getOne(Configurations.class, Configuration.class, uri);
    }
}
