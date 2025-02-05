/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2020 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2021 Orange
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

package org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.onap.aai.domain.yang.ComposedResource;
import org.onap.aai.domain.yang.ComposedResources;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperProvider;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.infrastructure.workflow.tasks.VrfBondingServiceException;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoPnf;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.ACTIVATE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.DEACTIVATE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.DELETE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.UNASSIGN_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.UPGRADE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.WORKFLOW_ACTION_ERROR_MESSAGE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.CREATE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.FABRIC_CONFIGURATION;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.NETWORKCOLLECTION;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.USER_PARAM_SERVICE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.ASSIGN_INSTANCE;


@Component
public class ServiceEBBLoader {

    private static final Logger logger = LoggerFactory.getLogger(ServiceEBBLoader.class);

    private final UserParamsServiceTraversal userParamsServiceTraversal;
    private final CatalogDbClient catalogDbClient;
    private final VrfValidation vrfValidation;
    private final AAIConfigurationResources aaiConfigurationResources;
    private final WorkflowActionExtractResourcesAAI workflowActionUtils;
    private final BBInputSetupUtils bbInputSetupUtils;
    private final BBInputSetup bbInputSetup;
    private final ExceptionBuilder exceptionBuilder;

    public ServiceEBBLoader(UserParamsServiceTraversal userParamsServiceTraversal, CatalogDbClient catalogDbClient,
            VrfValidation vrfValidation, AAIConfigurationResources aaiConfigurationResources,
            @Qualifier("WorkflowActionExtractResourcesAAI") WorkflowActionExtractResourcesAAI workflowActionUtils,
            BBInputSetupUtils bbInputSetupUtils, BBInputSetup bbInputSetup, ExceptionBuilder exceptionBuilder) {
        this.userParamsServiceTraversal = userParamsServiceTraversal;
        this.catalogDbClient = catalogDbClient;
        this.vrfValidation = vrfValidation;
        this.aaiConfigurationResources = aaiConfigurationResources;
        this.workflowActionUtils = workflowActionUtils;
        this.bbInputSetupUtils = bbInputSetupUtils;
        this.bbInputSetup = bbInputSetup;
        this.exceptionBuilder = exceptionBuilder;
    }

    public List<Resource> getResourceListForService(ServiceInstancesRequest sIRequest, String requestAction,
            DelegateExecution execution, String serviceInstanceId, String resourceId,
            List<Pair<WorkflowType, String>> aaiResourceIds) throws IOException, VrfBondingServiceException {
        boolean containsService = false;
        List<Resource> resourceList = new ArrayList<>();
        List<Map<String, Object>> userParams = sIRequest.getRequestDetails().getRequestParameters().getUserParams();
        if (requestAction.equalsIgnoreCase(ASSIGN_INSTANCE)) {
            // SERVICE-MACRO-ASSIGN will always get user params with a
            // service.

            if (userParams != null) {
                containsService = isContainsService(sIRequest);
                if (containsService) {
                    resourceList = userParamsServiceTraversal.getResourceListFromUserParams(execution, userParams,
                            serviceInstanceId, requestAction);
                }
            } else {
                buildAndThrowException(execution,
                        "Service-Macro-Assign request details must contain user params with a service");
            }
        } else if (requestAction.equalsIgnoreCase(CREATE_INSTANCE)) {
            // SERVICE-MACRO-CREATE will get user params with a service,
            // a service with a network, a service with a
            // network collection, OR an empty service.
            // If user params is just a service or null and macro
            // queries the SI and finds a VNF, macro fails.

            if (userParams != null) {
                containsService = isContainsService(sIRequest);
            }
            if (containsService) {
                resourceList = userParamsServiceTraversal.getResourceListFromUserParams(execution, userParams,
                        serviceInstanceId, requestAction);
            }
            if (!isComposedService(resourceList) && !foundRelated(resourceList)) {
                traverseCatalogDbService(execution, sIRequest, resourceList, aaiResourceIds);
            }
        } else if ((ACTIVATE_INSTANCE.equalsIgnoreCase(requestAction)
                || UNASSIGN_INSTANCE.equalsIgnoreCase(requestAction) || DELETE_INSTANCE.equalsIgnoreCase(requestAction)
                || UPGRADE_INSTANCE.equalsIgnoreCase(requestAction)
                || requestAction.equalsIgnoreCase("activate" + FABRIC_CONFIGURATION))) {
            // SERVICE-MACRO-ACTIVATE, SERVICE-MACRO-UNASSIGN, and
            // SERVICE-MACRO-DELETE
            // Will never get user params with service, macro will have
            // to query the SI in AAI to find related instances.
            traverseAAIService(execution, resourceList, resourceId, aaiResourceIds);
        } else if (DEACTIVATE_INSTANCE.equalsIgnoreCase(requestAction)) {
            resourceList.add(new Resource(WorkflowType.SERVICE, "", false, null));
        }
        return resourceList;
    }

    private boolean isContainsService(ServiceInstancesRequest sIRequest) {
        boolean containsService;
        List<Map<String, Object>> userParams = sIRequest.getRequestDetails().getRequestParameters().getUserParams();
        containsService = userParams.stream().anyMatch(param -> param.containsKey(USER_PARAM_SERVICE));
        return containsService;
    }

    public void traverseCatalogDbService(DelegateExecution execution, ServiceInstancesRequest sIRequest,
            List<Resource> resourceList, List<Pair<WorkflowType, String>> aaiResourceIds)
            throws JsonProcessingException, VrfBondingServiceException {
        String modelUUID = sIRequest.getRequestDetails().getModelInfo().getModelVersionId();
        org.onap.so.db.catalog.beans.Service service = catalogDbClient.getServiceByID(modelUUID);

        if (service == null) {
            buildAndThrowException(execution, "Could not find the service model in catalog db.");
        } else {
            Resource serviceResource = new Resource(WorkflowType.SERVICE, service.getModelUUID(), false, null);
            resourceList.add(serviceResource);
            RelatedInstance relatedVpnBinding =
                    bbInputSetupUtils.getRelatedInstanceByType(sIRequest.getRequestDetails(), ModelType.vpnBinding);
            RelatedInstance relatedLocalNetwork =
                    bbInputSetupUtils.getRelatedInstanceByType(sIRequest.getRequestDetails(), ModelType.network);

            if (relatedVpnBinding != null && relatedLocalNetwork != null) {
                traverseVrfConfiguration(aaiResourceIds, resourceList, serviceResource, service, relatedVpnBinding,
                        relatedLocalNetwork);
            } else {
                traverseNetworkCollection(execution, resourceList, serviceResource, service);
            }
        }
    }

    public boolean foundRelated(List<Resource> resourceList) {
        return (containsWorkflowType(resourceList, WorkflowType.VNF)
                || containsWorkflowType(resourceList, WorkflowType.PNF)
                || containsWorkflowType(resourceList, WorkflowType.NETWORK)
                || containsWorkflowType(resourceList, WorkflowType.NETWORKCOLLECTION));
    }

    public boolean isComposedService(List<Resource> resourceList) {
        return resourceList.stream().anyMatch(s -> s.getResourceType() == WorkflowType.SERVICE && s.hasParent());
    }

    public void traverseAAIService(DelegateExecution execution, List<Resource> resourceList, String resourceId,
            List<Pair<WorkflowType, String>> aaiResourceIds) {
        try {
            ServiceInstance serviceInstanceAAI = bbInputSetupUtils.getAAIServiceInstanceById(resourceId);
            org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                    bbInputSetup.getExistingServiceInstance(serviceInstanceAAI);
            var serviceResource =
                    new Resource(WorkflowType.SERVICE, serviceInstanceMSO.getServiceInstanceId(), false, null);
            serviceResource.setModelInvariantId(serviceInstanceAAI.getModelInvariantId());
            serviceResource.setModelVersionId(serviceInstanceAAI.getModelVersionId());
            resourceList.add(serviceResource);
            traverseServiceInstanceChildService(resourceList, serviceResource, serviceInstanceAAI);
            traverseServiceInstanceMSOVnfs(resourceList, serviceResource, aaiResourceIds, serviceInstanceMSO);
            traverseServiceInstanceMSOPnfs(resourceList, serviceResource, aaiResourceIds, serviceInstanceMSO);
            if (serviceInstanceMSO.getNetworks() != null) {
                for (org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network network : serviceInstanceMSO
                        .getNetworks()) {
                    aaiResourceIds.add(new Pair<>(WorkflowType.NETWORK, network.getNetworkId()));
                    Resource networkResource =
                            new Resource(WorkflowType.NETWORK, network.getNetworkId(), false, serviceResource);
                    ModelInfoNetwork modelInfoNetwork = network.getModelInfoNetwork();
                    if (modelInfoNetwork != null) {
                        networkResource.setModelCustomizationId(modelInfoNetwork.getModelCustomizationUUID());
                        networkResource.setModelVersionId(modelInfoNetwork.getModelUUID());
                        networkResource.setModelCustomizationId(modelInfoNetwork.getModelCustomizationUUID());
                    }
                    resourceList.add(networkResource);
                }
            }
            if (serviceInstanceMSO.getCollection() != null) {
                logger.debug("found networkcollection");
                aaiResourceIds
                        .add(new Pair<>(WorkflowType.NETWORKCOLLECTION, serviceInstanceMSO.getCollection().getId()));
                resourceList.add(new Resource(WorkflowType.NETWORKCOLLECTION,
                        serviceInstanceMSO.getCollection().getId(), false, serviceResource));
            }
            if (serviceInstanceMSO.getConfigurations() != null) {
                for (Configuration config : serviceInstanceMSO.getConfigurations()) {
                    Optional<org.onap.aai.domain.yang.Configuration> aaiConfig =
                            aaiConfigurationResources.getConfiguration(config.getConfigurationId());
                    if (aaiConfig.isPresent() && aaiConfig.get().getRelationshipList() != null) {
                        for (Relationship relationship : aaiConfig.get().getRelationshipList().getRelationship()) {
                            if (relationship.getRelatedTo().contains("vnfc")
                                    || relationship.getRelatedTo().contains("vpn-binding")) {
                                aaiResourceIds.add(new Pair<>(WorkflowType.CONFIGURATION, config.getConfigurationId()));
                                resourceList.add(new Resource(WorkflowType.CONFIGURATION, config.getConfigurationId(),
                                        false, serviceResource));
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Exception in traverseAAIService", ex);
            buildAndThrowException(execution,
                    "Could not find existing Service Instance or related Instances to execute the request on.");
        }
    }

    private void traverseServiceInstanceMSOVnfs(List<Resource> resourceList, Resource serviceResource,
            List<Pair<WorkflowType, String>> aaiResourceIds,
            org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO) {
        if (serviceInstanceMSO.getVnfs() == null) {
            return;
        }
        for (org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf : serviceInstanceMSO.getVnfs()) {
            aaiResourceIds.add(new Pair<>(WorkflowType.VNF, vnf.getVnfId()));
            GenericVnf genericVnf = bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId());
            Resource vnfResource = new Resource(WorkflowType.VNF, vnf.getVnfId(), false, serviceResource);
            vnfResource.setVnfCustomizationId(genericVnf.getModelCustomizationId());
            vnfResource.setModelCustomizationId(genericVnf.getModelCustomizationId());
            vnfResource.setModelVersionId(genericVnf.getModelVersionId());
            resourceList.add(vnfResource);
            traverseVnfModules(resourceList, vnfResource, aaiResourceIds, vnf);
            if (vnf.getVolumeGroups() != null) {
                for (org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup : vnf.getVolumeGroups()) {
                    aaiResourceIds.add(new Pair<>(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId()));
                    resourceList.add(
                            new Resource(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId(), false, vnfResource));
                }
            }
        }
    }

    private void traverseServiceInstanceMSOPnfs(List<Resource> resourceList, Resource serviceResource,
            List<Pair<WorkflowType, String>> aaiResourceIds,
            org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO) {
        if (serviceInstanceMSO.getPnfs() == null) {
            return;
        }
        for (org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf pnf : serviceInstanceMSO.getPnfs()) {
            aaiResourceIds.add(new Pair<>(WorkflowType.PNF, pnf.getPnfId()));
            Resource resource = new Resource(WorkflowType.PNF, pnf.getPnfId(), false, serviceResource);
            ModelInfoPnf modelInfo = pnf.getModelInfoPnf();
            if (modelInfo != null) {
                resource.setModelVersionId(modelInfo.getModelUuid());
                resource.setModelCustomizationId(modelInfo.getModelCustomizationUuid());
            }
            resourceList.add(resource);
        }
    }

    public void traverseServiceInstanceChildService(List<Resource> resourceList, Resource serviceResource,
            ServiceInstance serviceInstanceAAI) {

        ComposedResources composedResources = serviceInstanceAAI.getComposedResources();
        if (composedResources == null) {
            return;
        }

        List<ComposedResource> listOfComposedResource = composedResources.getComposedResource();

        listOfComposedResource.forEach(composedResource -> {
            // Get ServiceInstance from composedResource relationship List
            RelationshipList relationshipList = composedResource.getRelationshipList();
            if (relationshipList == null) {
                return;
            }
            List<Relationship> composedResourceRelationshipList = relationshipList.getRelationship();
            ServiceInstance childService = new ServiceInstance();
            composedResourceRelationshipList.forEach(composedRelation -> {
                if ("service-instance".equalsIgnoreCase(composedRelation.getRelatedTo())) {
                    List<RelationshipData> rData = composedRelation.getRelationshipData();
                    rData.forEach(data -> {
                        if ("service-instance.service-instance-id".equalsIgnoreCase(data.getRelationshipKey())) {
                            childService.setServiceInstanceId(data.getRelationshipValue());
                        }
                    });
                    composedRelation.getRelatedToProperty().forEach(relatedToProperty -> {
                        if ("service-instance.service-instance-name"
                                .equalsIgnoreCase(relatedToProperty.getPropertyKey())) {
                            childService.setServiceInstanceName(relatedToProperty.getPropertyValue());
                        }
                    });
                }
            });

            if (childService.getServiceInstanceId() == null) {
                return;
            }

            Resource childServiceResource =
                    new Resource(WorkflowType.SERVICE, childService.getServiceInstanceId(), false, serviceResource);

            childServiceResource.setInstanceName(childService.getServiceInstanceName());
            resourceList.add(childServiceResource);
        });

    }

    protected void traverseVrfConfiguration(List<Pair<WorkflowType, String>> aaiResourceIds,
            List<Resource> resourceList, Resource serviceResource, org.onap.so.db.catalog.beans.Service service,
            RelatedInstance relatedVpnBinding, RelatedInstance relatedLocalNetwork)
            throws VrfBondingServiceException, JsonProcessingException {
        org.onap.aai.domain.yang.L3Network aaiLocalNetwork =
                bbInputSetupUtils.getAAIL3Network(relatedLocalNetwork.getInstanceId());
        vrfValidation.vrfServiceValidation(service);
        vrfValidation.vrfCatalogDbChecks(service);
        vrfValidation.aaiVpnBindingValidation(relatedVpnBinding.getInstanceId(),
                bbInputSetupUtils.getAAIVpnBinding(relatedVpnBinding.getInstanceId()));
        vrfValidation.aaiNetworkValidation(relatedLocalNetwork.getInstanceId(), aaiLocalNetwork);
        vrfValidation.aaiSubnetValidation(aaiLocalNetwork);
        vrfValidation.aaiAggregateRouteValidation(aaiLocalNetwork);
        vrfValidation.aaiRouteTargetValidation(aaiLocalNetwork);
        String existingAAIVrfConfiguration = getExistingAAIVrfConfiguration(relatedVpnBinding, aaiLocalNetwork);
        if (existingAAIVrfConfiguration != null) {
            aaiResourceIds.add(new Pair<>(WorkflowType.CONFIGURATION, existingAAIVrfConfiguration));
        }
        resourceList.add(new Resource(WorkflowType.CONFIGURATION,
                service.getConfigurationCustomizations().get(0).getModelCustomizationUUID(), false, serviceResource));

    }

    protected void traverseNetworkCollection(DelegateExecution execution, List<Resource> resourceList,
            Resource serviceResource, org.onap.so.db.catalog.beans.Service service) {
        if (isVnfCustomizationsInTheService(service)) {
            buildAndThrowException(execution,
                    "Cannot orchestrate Service-Macro-Create without user params with a vnf. Please update ASDC model for new macro orchestration support or add service_recipe records to route to old macro flows");
        }
        if (isPnfCustomizationsInTheService(service)) {
            buildAndThrowException(execution,
                    "Cannot orchestrate Service-Macro-Create without user params with a pnf. Please update ASDC model for new macro orchestration support or add service_recipe records to route to old macro flows");
        }
        List<CollectionResourceCustomization> customizations = service.getCollectionResourceCustomizations();
        if (customizations.isEmpty()) {
            logger.debug("No Collections found. CollectionResourceCustomization list is empty.");
        } else {
            CollectionResourceCustomization collectionResourceCustomization =
                    findCatalogNetworkCollection(execution, service);
            traverseNetworkCollectionResourceCustomization(resourceList, serviceResource,
                    collectionResourceCustomization);
        }
        traverseNetworkCollectionCustomization(resourceList, serviceResource, service);
    }

    private void traverseNetworkCollectionResourceCustomization(List<Resource> resourceList, Resource serviceResource,
            CollectionResourceCustomization collectionResourceCustomization) {
        if (collectionResourceCustomizationShouldNotBeProcessed(resourceList, serviceResource,
                collectionResourceCustomization))
            return;
        int minNetworks = 0;
        org.onap.so.db.catalog.beans.InstanceGroup instanceGroup =
                collectionResourceCustomization.getCollectionResource().getInstanceGroup();
        CollectionResourceInstanceGroupCustomization collectionInstCust = null;
        if (!instanceGroup.getCollectionInstanceGroupCustomizations().isEmpty()) {
            for (CollectionResourceInstanceGroupCustomization collectionInstanceGroupTemp : instanceGroup
                    .getCollectionInstanceGroupCustomizations()) {
                if (collectionInstanceGroupTemp.getModelCustomizationUUID()
                        .equalsIgnoreCase(collectionResourceCustomization.getModelCustomizationUUID())) {
                    collectionInstCust = collectionInstanceGroupTemp;
                    break;
                }
            }
            if (interfaceNetworkQuantityIsAvailableInCollection(collectionInstCust)) {
                minNetworks = collectionInstCust.getSubInterfaceNetworkQuantity();
            }
        }
        logger.debug("minNetworks: {}", minNetworks);
        CollectionNetworkResourceCustomization collectionNetworkResourceCust =
                getCollectionNetworkResourceCustomization(collectionResourceCustomization, instanceGroup);
        for (int i = 0; i < minNetworks; i++) {
            if (collectionNetworkResourceCust != null) {
                Resource resource = new Resource(WorkflowType.VIRTUAL_LINK,
                        collectionNetworkResourceCust.getModelCustomizationUUID(), false, serviceResource);
                resource.setVirtualLinkKey(Integer.toString(i));
                resourceList.add(resource);
            }
        }
    }

    private CollectionNetworkResourceCustomization getCollectionNetworkResourceCustomization(
            CollectionResourceCustomization collectionResourceCustomization, InstanceGroup instanceGroup) {
        CollectionNetworkResourceCustomization collectionNetworkResourceCust = null;
        for (CollectionNetworkResourceCustomization collectionNetworkTemp : instanceGroup
                .getCollectionNetworkResourceCustomizations()) {
            if (collectionNetworkTemp.getNetworkResourceCustomization().getModelCustomizationUUID()
                    .equalsIgnoreCase(collectionResourceCustomization.getModelCustomizationUUID())) {
                collectionNetworkResourceCust = collectionNetworkTemp;
                break;
            }
        }
        return collectionNetworkResourceCust;
    }

    private boolean collectionResourceCustomizationShouldNotBeProcessed(List<Resource> resourceList,
            Resource serviceResource, CollectionResourceCustomization collectionResourceCustomization) {
        if (collectionResourceCustomization == null) {
            logger.debug("No Network Collection Customization found");
            return true;
        }
        resourceList.add(new Resource(WorkflowType.NETWORKCOLLECTION,
                collectionResourceCustomization.getModelCustomizationUUID(), false, serviceResource));
        logger.debug("Found a network collection");
        if (collectionResourceCustomization.getCollectionResource() == null) {
            logger.debug("No Network Collection found. collectionResource is null");
            return true;
        }
        if (collectionResourceCustomization.getCollectionResource().getInstanceGroup() == null) {
            logger.debug("No Instance Group found for network collection.");
            return true;
        }
        String toscaNodeType =
                collectionResourceCustomization.getCollectionResource().getInstanceGroup().getToscaNodeType();
        if (!toscaNodeTypeHasNetworkCollection(toscaNodeType)) {
            logger.debug("Instance Group tosca node type does not contain NetworkCollection:  {}", toscaNodeType);
            return true;
        }
        return false;
    }

    private boolean interfaceNetworkQuantityIsAvailableInCollection(
            CollectionResourceInstanceGroupCustomization collectionInstCust) {
        return collectionInstCust != null && collectionInstCust.getSubInterfaceNetworkQuantity() != null;
    }

    private boolean toscaNodeTypeHasNetworkCollection(String toscaNodeType) {
        return toscaNodeType != null && toscaNodeType.contains(NETWORKCOLLECTION);
    }

    private void traverseNetworkCollectionCustomization(List<Resource> resourceList, Resource serviceResource,
            org.onap.so.db.catalog.beans.Service service) {
        if (isNetworkCollectionInTheResourceList(resourceList)) {
            return;
        }
        if (service.getNetworkCustomizations() == null) {
            logger.debug("No networks were found on this service model");
            return;
        }
        for (int i = 0; i < service.getNetworkCustomizations().size(); i++) {
            resourceList.add(new Resource(WorkflowType.NETWORK,
                    service.getNetworkCustomizations().get(i).getModelCustomizationUUID(), false, serviceResource));
        }
    }

    private boolean isVnfCustomizationsInTheService(org.onap.so.db.catalog.beans.Service service) {
        return !(service.getVnfCustomizations() == null || service.getVnfCustomizations().isEmpty());
    }

    private boolean isPnfCustomizationsInTheService(org.onap.so.db.catalog.beans.Service service) {
        return !(service.getPnfCustomizations() == null || service.getPnfCustomizations().isEmpty());
    }

    private void traverseVnfModules(List<Resource> resourceList, Resource vnfResource,
            List<Pair<WorkflowType, String>> aaiResourceIds,
            org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf) {
        if (vnf.getVfModules() == null) {
            return;
        }
        for (VfModule vfModule : vnf.getVfModules()) {
            aaiResourceIds.add(new Pair<>(WorkflowType.VFMODULE, vfModule.getVfModuleId()));
            Resource resource = new Resource(WorkflowType.VFMODULE, vfModule.getVfModuleId(), false, vnfResource);
            org.onap.aai.domain.yang.VfModule aaiVfModule =
                    bbInputSetupUtils.getAAIVfModule(vnf.getVnfId(), vfModule.getVfModuleId());
            resource.setModelCustomizationId(aaiVfModule.getModelCustomizationId());
            resource.setModelInvariantId(aaiVfModule.getModelInvariantId());
            resource.setBaseVfModule(vfModule.getModelInfoVfModule().getIsBaseBoolean());
            resourceList.add(resource);
        }
    }


    protected String getExistingAAIVrfConfiguration(RelatedInstance relatedVpnBinding,
            org.onap.aai.domain.yang.L3Network aaiLocalNetwork)
            throws JsonProcessingException, VrfBondingServiceException {
        Optional<Relationships> relationshipsOp = new AAIResultWrapper(
                new AAICommonObjectMapperProvider().getMapper().writeValueAsString(aaiLocalNetwork)).getRelationships();
        if (relationshipsOp.isPresent()) {
            List<AAIResultWrapper> configurationsRelatedToLocalNetwork =
                    relationshipsOp.get().getByType(AAIFluentTypeBuilder.Types.CONFIGURATION);
            if (configurationsRelatedToLocalNetwork.size() > 1) {
                throw new VrfBondingServiceException(
                        "Network: " + aaiLocalNetwork.getNetworkId() + " has more than 1 configuration related to it");
            }
            if (configurationsRelatedToLocalNetwork.size() == 1) {
                AAIResultWrapper configWrapper = configurationsRelatedToLocalNetwork.get(0);
                Optional<Configuration> relatedConfiguration = configWrapper.asBean(Configuration.class);
                if (relatedConfiguration.isPresent() && vrfConfigurationAlreadyExists(relatedVpnBinding,
                        relatedConfiguration.get(), configWrapper)) {
                    return relatedConfiguration.get().getConfigurationId();
                }
            }
        }
        return null;
    }

    protected boolean vrfConfigurationAlreadyExists(RelatedInstance relatedVpnBinding, Configuration vrfConfiguration,
            AAIResultWrapper configWrapper) throws VrfBondingServiceException {
        if ("VRF-ENTRY".equalsIgnoreCase(vrfConfiguration.getConfigurationType())) {
            Optional<Relationships> relationshipsConfigOp = configWrapper.getRelationships();
            if (relationshipsConfigOp.isPresent()) {
                Optional<VpnBinding> relatedInfraVpnBindingOp =
                        workflowActionUtils.extractRelationshipsVpnBinding(relationshipsConfigOp.get());
                if (relatedInfraVpnBindingOp.isPresent()) {
                    VpnBinding relatedInfraVpnBinding = relatedInfraVpnBindingOp.get();
                    if (!relatedInfraVpnBinding.getVpnId().equalsIgnoreCase(relatedVpnBinding.getInstanceId())) {
                        throw new VrfBondingServiceException("Configuration: " + vrfConfiguration.getConfigurationId()
                                + " is not connected to the same vpn binding id provided in request: "
                                + relatedVpnBinding.getInstanceId());
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean containsWorkflowType(List<Resource> resourceList, WorkflowType workflowType) {
        return resourceList.stream().anyMatch(resource -> resource.getResourceType().equals(workflowType));
    }

    public boolean isNetworkCollectionInTheResourceList(List<Resource> resourceList) {
        return resourceList.stream().anyMatch(x -> WorkflowType.NETWORKCOLLECTION == x.getResourceType());
    }

    public CollectionResourceCustomization findCatalogNetworkCollection(DelegateExecution execution,
            org.onap.so.db.catalog.beans.Service service) {
        CollectionResourceCustomization networkCollection = null;
        int count = 0;
        for (CollectionResourceCustomization collectionCust : service.getCollectionResourceCustomizations()) {
            if (catalogDbClient.getNetworkCollectionResourceCustomizationByID(
                    collectionCust.getModelCustomizationUUID()) != null) {
                networkCollection = collectionCust;
                count++;
            }
        }
        if (count == 0) {
            return null;
        } else if (count > 1) {
            buildAndThrowException(execution,
                    "Found multiple Network Collections in the Service model, only one per Service is supported.");
        }
        return networkCollection;
    }

    protected void buildAndThrowException(DelegateExecution execution, String msg) {
        logger.error(msg);
        execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg);
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
    }
}
