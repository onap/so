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

package org.onap.so.db.catalog.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ws.rs.core.UriBuilder;
import org.onap.so.db.catalog.beans.BuildingBlockDetail;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.ExternalServiceToInternalService;
import org.onap.so.db.catalog.beans.HomingInstance;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkRecipe;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.OrchestrationAction;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.onap.so.db.catalog.beans.PnfResource;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.ResourceType;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfComponentsRecipe;
import org.onap.so.db.catalog.beans.VnfRecipe;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfVfmoduleCvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.onap.so.logger.LogConstants;
import org.onap.so.logging.jaxrs.filter.SpringClientFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.ClientFactory;
import uk.co.blackpepper.bowman.Configuration;

@Component("CatalogDbClient")
public class CatalogDbClient {

    private static final String CLOUD_SITE = "/cloudSite";
    private static final String CLOUDIFY_MANAGER = "/cloudifyManager";
    private static final String RAINY_DAY_HANDLER_MACRO = "/rainy_day_handler_macro";
    private static final String NORTHBOUND_REQUEST_REF_LOOKUP = "/northbound_request_ref_lookup";
    private static final String NETWORK_RESOURCE_CUSTOMIZATION = "/networkResourceCustomization";
    private static final String COLLECTION_RESOURCE_INSTANCE_GROUP_CUSTOMIZATION = "/collectionResourceInstanceGroupCustomization";
    private static final String VNFC_INSTANCE_GROUP_CUSTOMIZATION = "/vnfcInstanceGroupCustomization";
    private static final String ORCHESTRATION_FLOW = "/orchestrationFlow";
    private static final String ORCHESTRATION_STATUS_STATE_TRANSITION_DIRECTIVE = "/orchestrationStatusStateTransitionDirective";
    private static final String INSTANCE_GROUP = "/instanceGroup";
    private static final String COLLECTION_NETWORK_RESOURCE_CUSTOMIZATION = "/collectionNetworkResourceCustomization";
    private static final String BUILDING_BLOCK_DETAIL = "/buildingBlockDetail";
    private static final String NETWORK_COLLECTION_RESOURCE_CUSTOMIZATION = "/networkCollectionResourceCustomization";
    private static final String VNF_RESOURCE_CUSTOMIZATION = "/vnfResourceCustomization";
    private static final String SERVICE = "/service";
    private static final String EXTERNAL_SERVICE_TO_INTERNAL_MODEL_MAPPING = "/externalServiceToInternalService";
    private static final String VNF_RESOURCE = "/vnfResource";
    private static final String VNF_RECIPE = "/vnfRecipe";
    private static final String VFMODULE = "/vfModule";
    private static final String VFMODULE_CUSTOMIZATION = "/vfModuleCustomization";
    private static final String VNF_COMPONENTS_RECIPE = "/vnfComponentsRecipe";
    private static final String SERVICE_RECIPE = "/serviceRecipe";
    private static final String NETWORK_RECIPE = "/networkRecipe";
    private static final String PNF_RESOURCE = "/pnfResource";
    private static final String PNF_RESOURCE_CUSTOMIZATION = "/pnfResourceCustomization";


    private static final String SEARCH = "/search";
    private static final String URI_SEPARATOR = "/";

    private static final String SERVICE_MODEL_UUID = "serviceModelUUID";
    private static final String SERVICE_NAME = "serviceName";
    private static final String MODEL_UUID = "modelUUID";
    private static final String MODEL_CUSTOMIZATION_UUID = "modelCustomizationUUID";
    private static final String ACTION = "action";
    private static final String MODEL_NAME = "modelName";
    private static final String MODEL_VERSION = "modelVersion";
    private static final String MODEL_INVARIANT_UUID = "modelInvariantUUID";
    private static final String MODEL_INSTANCE_NAME = "modelInstanceName";
    private static final String VNF_RESOURCE_MODEL_UUID = "vnfResourceModelUUID";
    private static final String NF_ROLE = "nfRole";
    private static final String VF_MODULE_MODEL_UUID = "vfModuleModelUUID";
    private static final String VNF_COMPONENT_TYPE = "vnfComponentType";
    private static final String BUILDING_BLOCK_NAME = "buildingBlockName";
    private static final String RESOURCE_TYPE = "resourceType";
    private static final String ORCHESTRATION_STATUS = "orchestrationStatus";
    private static final String TARGET_ACTION = "targetAction";
    private static final String REQUEST_SCOPE = "requestScope";
    private static final String IS_ALACARTE = "isALaCarte";
    private static final String CLOUD_OWNER = "cloudOwner";
    private static final String FLOW_NAME = "flowName";
    private static final String SERVICE_TYPE = "serviceType";
    private static final String VNF_TYPE = "vnfType";
    private static final String ERROR_CODE = "errorCode";
    private static final String WORK_STEP = "workStep";
    private static final String CLLI = "clli";
    private static final String CLOUD_VERSION = "cloudVersion";
    private static final String HOMING_INSTANCE = "/homingInstance";

    private static final String TARGET_ENTITY = "SO:CatalogDB";
    private static final String ASTERISK = "*";

    private String findExternalToInternalServiceByServiceName = "/findByServiceName";
    private String findServiceByModelName = "/findOneByModelName";
    private String findServiceRecipeByActionAndServiceModelUUID = "/findByActionAndServiceModelUUID";
    private String findServiceByModelUUID = "/findOneByModelUUID";
    private String findFirstByModelNameURI = "/findFirstByModelNameOrderByModelVersionDesc";
    private String findFirstByServiceModelUUIDAndActionURI = "/findFirstByServiceModelUUIDAndAction";
    private String findFirstByModelVersionAndModelInvariantUUIDURI = "/findFirstByModelVersionAndModelInvariantUUID";
    private String findByModelInvariantUUIDURI = "/findByModelInvariantUUIDOrderByModelVersionDesc";
    private String findFirstByModelNameAndAction = "/findFirstByModelNameAndAction";
    private String findFirstResourceByModelInvariantUUIDAndModelVersion = "/findFirstResourceByModelInvariantUUIDAndModelVersion";
    private String findByModelInstanceNameAndVnfResources = "/findByModelInstanceNameAndVnfResources";
    private String findFirstVnfRecipeByNfRoleAndAction = "/findFirstVnfRecipeByNfRoleAndAction";
    private String findByModelCustomizationUUIDAndVfModuleModelUUID = "/findByModelCustomizationUUIDAndVfModuleModelUUID";
    private String findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction = "/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction";
    private String findFirstVnfComponentsRecipeByVnfComponentTypeAndAction = "/findFirstVnfComponentsRecipeByVnfComponentTypeAndAction";
    private String findVfModuleByModelInvariantUUIDOrderByModelVersionDesc = "/findByModelInvariantUUIDOrderByModelVersionDesc";
    private String findFirstVfModuleByModelInvariantUUIDAndModelVersion = "/findFirstVfModuleByModelInvariantUUIDAndModelVersion";
    private String findOneByBuildingBlockName = "/findOneByBuildingBlockName";
    private String findOneByResourceTypeAndOrchestrationStatusAndTargetAction = "/findOneByResourceTypeAndOrchestrationStatusAndTargetAction";
    private String findByAction = "/findByAction";
    private String findVnfcInstanceGroupCustomizationByModelCustomizationUUID = "/findByModelCustomizationUUID";
    private String findCollectionResourceInstanceGroupCustomizationByModelCustomizationUUID = "/findByModelCustomizationUUID";
    private String findOneByActionAndRequestScopeAndIsAlacarte = "/findOneByActionAndRequestScopeAndIsAlacarte";
    private String findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwner = "/findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwner";
    private String findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwnerAndServiceType = "/findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwnerAndServiceType";
    private String findOneByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep = "/findOneByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep";
    private String findByClliAndCloudVersion = "/findByClliAndCloudVersion";
    private String findServiceByServiceInstanceId = "/findServiceByServiceInstanceId";


    private String serviceURI;
    private String vfModuleURI;
    private String vnfResourceURI;
    private String vfModuleCustomizationURI;
    private String networkCollectionResourceCustomizationURI;
    private String networkResourceCustomizationURI;
    private String vnfResourceCustomizationURI;
    private String collectionNetworkResourceCustomizationURI;
    private String instanceGroupURI;
    private String cloudifyManagerURI;
    private String cloudSiteURI;
    private String homingInstanceURI;
    private String pnfResourceURI;
    private String pnfResourceCustomizationURI;

    private final Client<Service> serviceClient;

    private final Client<NetworkRecipe> networkRecipeClient;

    private final Client<NetworkResourceCustomization> networkResourceCustomizationClient;

    private final Client<VnfResource> vnfResourceClient;

    private final Client<VnfResourceCustomization> vnfResourceCustomizationClient;

    private final Client<VnfRecipe> vnfRecipeClient;

    private final Client<VfModuleCustomization> vfModuleCustomizationClient;

    private final Client<VfModule> vfModuleClient;

    private final Client<VnfComponentsRecipe> vnfComponentsRecipeClient;

    private final Client<OrchestrationFlow> orchestrationClient;

    private final Client<NorthBoundRequest> northBoundRequestClient;

    private final Client<RainyDayHandlerStatus> rainyDayHandlerStatusClient;

    private final Client<BuildingBlockDetail> buildingBlockDetailClient;

    private final Client<OrchestrationStatusStateTransitionDirective> orchestrationStatusStateTransitionDirectiveClient;

    private final Client<VnfcInstanceGroupCustomization> vnfcInstanceGroupCustomizationClient;

    private final Client<CollectionResourceInstanceGroupCustomization> collectionResourceInstanceGroupCustomizationClient;

    private final Client<InstanceGroup> instanceGroupClient;

    private final Client<NetworkCollectionResourceCustomization> networkCollectionResourceCustomizationClient;

    private final Client<CollectionNetworkResourceCustomization> collectionNetworkResourceCustomizationClient;

    private final Client<ServiceRecipe> serviceRecipeClient;

    private final Client<ExternalServiceToInternalService> externalServiceToInternalServiceClient;

    private final Client<CloudSite> cloudSiteClient;

    private final Client<HomingInstance> homingInstanceClient;

    private final Client<CloudifyManager> cloudifyManagerClient;

    private final Client<CvnfcCustomization> cvnfcCustomizationClient;

    private final Client<ControllerSelectionReference> controllerSelectionReferenceClient;

    private final Client<VnfVfmoduleCvnfcConfigurationCustomization> vnfVfmoduleCvnfcConfigurationCustomizationClient;

    private final Client<PnfResource> pnfResourceClient;

    private final Client<PnfResourceCustomization> pnfResourceCustomizationClient;

    @Value("${mso.catalog.db.spring.endpoint}")
    private String endpoint;

    @Value("${mso.db.auth}")
    private String msoAdaptersAuth;


    @PostConstruct
    public void init() {
        findExternalToInternalServiceByServiceName =
            endpoint + EXTERNAL_SERVICE_TO_INTERNAL_MODEL_MAPPING + SEARCH + findExternalToInternalServiceByServiceName;
        findServiceByModelName = endpoint + SERVICE + SEARCH + findServiceByModelName;
        findServiceRecipeByActionAndServiceModelUUID =
            endpoint + SERVICE_RECIPE + SEARCH + findServiceRecipeByActionAndServiceModelUUID;
        findServiceByModelUUID = endpoint + SERVICE + SEARCH + findServiceByModelUUID;
        findFirstByModelNameURI = endpoint + SERVICE + SEARCH + findFirstByModelNameURI;
        findFirstByModelVersionAndModelInvariantUUIDURI =
            endpoint + SERVICE + SEARCH + findFirstByModelVersionAndModelInvariantUUIDURI;
        findByModelInvariantUUIDURI = endpoint + SERVICE + SEARCH + findByModelInvariantUUIDURI;
        findFirstByServiceModelUUIDAndActionURI =
            endpoint + SERVICE_RECIPE + SEARCH + findFirstByServiceModelUUIDAndActionURI;
        findFirstByModelNameAndAction = endpoint + NETWORK_RECIPE + SEARCH + findFirstByModelNameAndAction;
        findFirstResourceByModelInvariantUUIDAndModelVersion =
            endpoint + VNF_RESOURCE + SEARCH + findFirstResourceByModelInvariantUUIDAndModelVersion;
        findByModelInstanceNameAndVnfResources =
            endpoint + VNF_RESOURCE_CUSTOMIZATION + SEARCH + findByModelInstanceNameAndVnfResources;
        findFirstVnfRecipeByNfRoleAndAction = endpoint + VNF_RECIPE + SEARCH + findFirstVnfRecipeByNfRoleAndAction;
        findByModelCustomizationUUIDAndVfModuleModelUUID =
            endpoint + VFMODULE_CUSTOMIZATION + SEARCH + findByModelCustomizationUUIDAndVfModuleModelUUID;
        findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction =
            endpoint + VNF_COMPONENTS_RECIPE + SEARCH
                + findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction;
        findFirstVnfComponentsRecipeByVnfComponentTypeAndAction =
            endpoint + VNF_COMPONENTS_RECIPE + SEARCH + findFirstVnfComponentsRecipeByVnfComponentTypeAndAction;
        findVfModuleByModelInvariantUUIDOrderByModelVersionDesc =
            endpoint + VFMODULE + SEARCH + findVfModuleByModelInvariantUUIDOrderByModelVersionDesc;
        findFirstVfModuleByModelInvariantUUIDAndModelVersion =
            endpoint + VFMODULE + SEARCH + findFirstVfModuleByModelInvariantUUIDAndModelVersion;
        findOneByBuildingBlockName = endpoint + BUILDING_BLOCK_DETAIL + SEARCH + findOneByBuildingBlockName;
        findOneByResourceTypeAndOrchestrationStatusAndTargetAction =
            endpoint + ORCHESTRATION_STATUS_STATE_TRANSITION_DIRECTIVE + SEARCH
                + findOneByResourceTypeAndOrchestrationStatusAndTargetAction;
        findByAction = endpoint + ORCHESTRATION_FLOW + SEARCH + findByAction;
        findVnfcInstanceGroupCustomizationByModelCustomizationUUID =
            endpoint + VNFC_INSTANCE_GROUP_CUSTOMIZATION + SEARCH
                + findVnfcInstanceGroupCustomizationByModelCustomizationUUID;
        findCollectionResourceInstanceGroupCustomizationByModelCustomizationUUID =
            endpoint + COLLECTION_RESOURCE_INSTANCE_GROUP_CUSTOMIZATION + SEARCH
                + findCollectionResourceInstanceGroupCustomizationByModelCustomizationUUID;
        findOneByActionAndRequestScopeAndIsAlacarte =
            endpoint + NORTHBOUND_REQUEST_REF_LOOKUP + SEARCH + findOneByActionAndRequestScopeAndIsAlacarte;
        findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwner = endpoint + NORTHBOUND_REQUEST_REF_LOOKUP + SEARCH
            + findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwner;
        findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwnerAndServiceType =
            endpoint + NORTHBOUND_REQUEST_REF_LOOKUP + SEARCH
                + findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwnerAndServiceType;
        findOneByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep = endpoint + RAINY_DAY_HANDLER_MACRO + SEARCH
            + findOneByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep;
        findByClliAndCloudVersion = endpoint + CLOUD_SITE + SEARCH + findByClliAndCloudVersion;

        serviceURI = endpoint + SERVICE + URI_SEPARATOR;
        vfModuleURI = endpoint + VFMODULE + URI_SEPARATOR;
        vnfResourceURI = endpoint + VNF_RESOURCE + URI_SEPARATOR;
        vfModuleCustomizationURI = endpoint + VFMODULE_CUSTOMIZATION + URI_SEPARATOR;
        networkCollectionResourceCustomizationURI =
            endpoint + NETWORK_COLLECTION_RESOURCE_CUSTOMIZATION + URI_SEPARATOR;
        networkResourceCustomizationURI = endpoint + NETWORK_RESOURCE_CUSTOMIZATION + URI_SEPARATOR;
        vnfResourceCustomizationURI = endpoint + VNF_RESOURCE_CUSTOMIZATION + URI_SEPARATOR;
        collectionNetworkResourceCustomizationURI =
            endpoint + COLLECTION_NETWORK_RESOURCE_CUSTOMIZATION + URI_SEPARATOR;
        instanceGroupURI = endpoint + INSTANCE_GROUP + URI_SEPARATOR;
        cloudifyManagerURI = endpoint + CLOUDIFY_MANAGER + URI_SEPARATOR;
        cloudSiteURI = endpoint + CLOUD_SITE + URI_SEPARATOR;
        homingInstanceURI = endpoint + HOMING_INSTANCE + URI_SEPARATOR;
        pnfResourceURI = endpoint + PNF_RESOURCE + URI_SEPARATOR;
        pnfResourceCustomizationURI = endpoint + PNF_RESOURCE_CUSTOMIZATION + URI_SEPARATOR;

    }

    public CatalogDbClient() {
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(
            new HttpComponentsClientHttpRequestFactory());

        ClientFactory clientFactory = Configuration.builder().setClientHttpRequestFactory(factory)
            .setRestTemplateConfigurer(restTemplate -> {
                restTemplate.getInterceptors().add((new SpringClientFilter()));

                restTemplate.getInterceptors().add((request, body, execution) -> {

                    request.getHeaders().add(HttpHeaders.AUTHORIZATION, msoAdaptersAuth);
                    request.getHeaders().add(LogConstants.TARGET_ENTITY_HEADER, TARGET_ENTITY);
                    return execution.execute(request, body);
                });
            }).build().buildClientFactory();
        serviceClient = clientFactory.create(Service.class);
        networkRecipeClient = clientFactory.create(NetworkRecipe.class);
        networkResourceCustomizationClient = clientFactory.create(NetworkResourceCustomization.class);
        vnfResourceClient = clientFactory.create(VnfResource.class);
        vnfResourceCustomizationClient = clientFactory.create(VnfResourceCustomization.class);
        vnfRecipeClient = clientFactory.create(VnfRecipe.class);
        orchestrationClient = clientFactory.create(OrchestrationFlow.class);
        vfModuleCustomizationClient = clientFactory.create(VfModuleCustomization.class);
        vfModuleClient = clientFactory.create(VfModule.class);
        vnfComponentsRecipeClient = clientFactory.create(VnfComponentsRecipe.class);
        northBoundRequestClient = clientFactory.create(NorthBoundRequest.class);
        rainyDayHandlerStatusClient = clientFactory.create(RainyDayHandlerStatus.class);
        buildingBlockDetailClient = clientFactory.create(BuildingBlockDetail.class);
        orchestrationStatusStateTransitionDirectiveClient = clientFactory
            .create(OrchestrationStatusStateTransitionDirective.class);
        vnfcInstanceGroupCustomizationClient = clientFactory.create(VnfcInstanceGroupCustomization.class);
        collectionResourceInstanceGroupCustomizationClient = clientFactory
            .create(CollectionResourceInstanceGroupCustomization.class);
        instanceGroupClient = clientFactory.create(InstanceGroup.class);
        networkCollectionResourceCustomizationClient = clientFactory
            .create(NetworkCollectionResourceCustomization.class);
        collectionNetworkResourceCustomizationClient = clientFactory
            .create(CollectionNetworkResourceCustomization.class);
        cloudSiteClient = clientFactory.create(CloudSite.class);
        homingInstanceClient = clientFactory.create(HomingInstance.class);
        cloudifyManagerClient = clientFactory.create(CloudifyManager.class);
        serviceRecipeClient = clientFactory.create(ServiceRecipe.class);
        cvnfcCustomizationClient = clientFactory.create(CvnfcCustomization.class);
        controllerSelectionReferenceClient = clientFactory.create(ControllerSelectionReference.class);
        externalServiceToInternalServiceClient = clientFactory.create(ExternalServiceToInternalService.class);
        vnfVfmoduleCvnfcConfigurationCustomizationClient = clientFactory.create(VnfVfmoduleCvnfcConfigurationCustomization.class);
        pnfResourceClient = clientFactory.create(PnfResource.class);
        pnfResourceCustomizationClient = clientFactory.create(PnfResourceCustomization.class);
    }

    public CatalogDbClient(String baseUri, String auth) {
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(
            new HttpComponentsClientHttpRequestFactory());

        ClientFactory clientFactory = Configuration.builder().setBaseUri(baseUri).setClientHttpRequestFactory(factory)
            .setRestTemplateConfigurer(restTemplate -> {
                restTemplate.getInterceptors().add((new SpringClientFilter()));

                restTemplate.getInterceptors().add((request, body, execution) -> {

                    request.getHeaders().add(HttpHeaders.AUTHORIZATION, auth);
                    request.getHeaders().add(LogConstants.TARGET_ENTITY_HEADER, TARGET_ENTITY);
                    return execution.execute(request, body);
                });
            }).build().buildClientFactory();
        serviceClient = clientFactory.create(Service.class);
        networkRecipeClient = clientFactory.create(NetworkRecipe.class);
        networkResourceCustomizationClient = clientFactory.create(NetworkResourceCustomization.class);
        vnfResourceClient = clientFactory.create(VnfResource.class);
        vnfResourceCustomizationClient = clientFactory.create(VnfResourceCustomization.class);
        vnfRecipeClient = clientFactory.create(VnfRecipe.class);
        orchestrationClient = clientFactory.create(OrchestrationFlow.class);
        vfModuleCustomizationClient = clientFactory.create(VfModuleCustomization.class);
        vfModuleClient = clientFactory.create(VfModule.class);
        vnfComponentsRecipeClient = clientFactory.create(VnfComponentsRecipe.class);
        northBoundRequestClient = clientFactory.create(NorthBoundRequest.class);
        rainyDayHandlerStatusClient = clientFactory.create(RainyDayHandlerStatus.class);
        buildingBlockDetailClient = clientFactory.create(BuildingBlockDetail.class);
        orchestrationStatusStateTransitionDirectiveClient = clientFactory
            .create(OrchestrationStatusStateTransitionDirective.class);
        vnfcInstanceGroupCustomizationClient = clientFactory.create(VnfcInstanceGroupCustomization.class);
        collectionResourceInstanceGroupCustomizationClient = clientFactory
            .create(CollectionResourceInstanceGroupCustomization.class);
        instanceGroupClient = clientFactory.create(InstanceGroup.class);
        networkCollectionResourceCustomizationClient = clientFactory
            .create(NetworkCollectionResourceCustomization.class);
        collectionNetworkResourceCustomizationClient = clientFactory
            .create(CollectionNetworkResourceCustomization.class);
        cloudSiteClient = clientFactory.create(CloudSite.class);
        homingInstanceClient = clientFactory.create(HomingInstance.class);
        cloudifyManagerClient = clientFactory.create(CloudifyManager.class);
        serviceRecipeClient = clientFactory.create(ServiceRecipe.class);
        cvnfcCustomizationClient = clientFactory.create(CvnfcCustomization.class);
        controllerSelectionReferenceClient = clientFactory.create(ControllerSelectionReference.class);
        externalServiceToInternalServiceClient = clientFactory.create(ExternalServiceToInternalService.class);
        vnfVfmoduleCvnfcConfigurationCustomizationClient = clientFactory.create(VnfVfmoduleCvnfcConfigurationCustomization.class);
        pnfResourceClient = clientFactory.create(PnfResource.class);
        pnfResourceCustomizationClient = clientFactory.create(PnfResourceCustomization.class);
    }

    public NetworkCollectionResourceCustomization getNetworkCollectionResourceCustomizationByID(
        String modelCustomizationUUID) {
        NetworkCollectionResourceCustomization networkCollectionResourceCustomization =
            this.getSingleResource(networkCollectionResourceCustomizationClient,
                getUri(networkCollectionResourceCustomizationURI + modelCustomizationUUID));
        if (networkCollectionResourceCustomization != null) {
            networkCollectionResourceCustomization.setModelCustomizationUUID(modelCustomizationUUID);
        }
        return networkCollectionResourceCustomization;
    }

    public Service getServiceByID(String modelUUID) {
        Service service = getSingleResource(serviceClient, getUri(serviceURI + modelUUID));
        if (service != null) {
            service.setModelUUID(modelUUID);
        }
        return service;
    }

    public VfModule getVfModuleByModelUUID(String modelUUID) {
        VfModule vfModule = getSingleResource(vfModuleClient, getUri(vfModuleURI + modelUUID));
        if (vfModule != null) {
            vfModule.setModelUUID(modelUUID);
        }
        return vfModule;
    }

    public VnfResource getVnfResourceByModelUUID(String modelUUID) {

        VnfResource vnfResource = this.getSingleResource(vnfResourceClient, getUri(vnfResourceURI + modelUUID));
        if (vnfResource != null) {
            vnfResource.setModelUUID(modelUUID);
        }
        return vnfResource;
    }

    public VnfResourceCustomization getVnfResourceCustomizationByModelCustomizationUUID(String modelCustomizationUUID) {
        VnfResourceCustomization vnfResourceCustomization = getSingleResource(vnfResourceCustomizationClient,
            getUri(vnfResourceCustomizationURI + modelCustomizationUUID));
        if (vnfResourceCustomization != null) {
            vnfResourceCustomization.setModelCustomizationUUID(modelCustomizationUUID);
        }
        return vnfResourceCustomization;
    }

    public PnfResource getPnfResourceByModelUUID(String modelUUID) {
        PnfResource PnfResource = this.getSingleResource(pnfResourceClient, getUri(pnfResourceURI + modelUUID));
        if (PnfResource != null) {
            PnfResource.setModelUUID(modelUUID);
        }
        return PnfResource;
    }

    public PnfResourceCustomization getPnfResourceCustomizationByModelCustomizationUUID(String modelCustomizationUUID) {
        PnfResourceCustomization pnfResourceCustomization = getSingleResource(pnfResourceCustomizationClient,
            getUri(pnfResourceCustomizationURI + modelCustomizationUUID));
        if (pnfResourceCustomization != null) {
            pnfResourceCustomization.setModelCustomizationUUID(modelCustomizationUUID);
        }
        return pnfResourceCustomization;
    }

    public CollectionNetworkResourceCustomization getCollectionNetworkResourceCustomizationByID(
        String modelCustomizationUUID) {
        CollectionNetworkResourceCustomization collectionNetworkResourceCustomization =
            this.getSingleResource(collectionNetworkResourceCustomizationClient, getUri(UriBuilder
                .fromUri(collectionNetworkResourceCustomizationURI + modelCustomizationUUID).build().toString()));
        if (collectionNetworkResourceCustomization != null) {
            collectionNetworkResourceCustomization.setModelCustomizationUUID(modelCustomizationUUID);
        }
        return collectionNetworkResourceCustomization;
    }

    public InstanceGroup getInstanceGroupByModelUUID(String modelUUID) {
        InstanceGroup instanceGroup = this.getSingleResource(instanceGroupClient, getUri(instanceGroupURI + modelUUID));
        if (instanceGroup != null) {
            instanceGroup.setModelUUID(modelUUID);
        }
        return instanceGroup;
    }

    public VfModuleCustomization getVfModuleCustomizationByModelCuztomizationUUID(String modelCustomizationUUID) {
        VfModuleCustomization vfModuleCust = this
            .getSingleResource(vfModuleCustomizationClient, getUri(vfModuleCustomizationURI + modelCustomizationUUID));
        if (vfModuleCust != null) {
            vfModuleCust.setModelCustomizationUUID(modelCustomizationUUID);
        }
        return vfModuleCust;
    }

    public NetworkResourceCustomization getNetworkResourceCustomizationByModelCustomizationUUID(
        String modelCustomizationUUID) {
        NetworkResourceCustomization networkResourceCustomization =
            this.getSingleResource(networkResourceCustomizationClient,
                getUri(networkResourceCustomizationURI + modelCustomizationUUID));
        if (networkResourceCustomization != null) {
            networkResourceCustomization.setModelCustomizationUUID(modelCustomizationUUID);
        }
        return networkResourceCustomization;
    }

    public BuildingBlockDetail getBuildingBlockDetail(String buildingBlockName) {
        BuildingBlockDetail buildingBlockDetail = getSingleResource(buildingBlockDetailClient, getUri(UriBuilder
            .fromUri(findOneByBuildingBlockName).queryParam(BUILDING_BLOCK_NAME, buildingBlockName).build()
            .toString()));
        if (buildingBlockDetail != null) {
            buildingBlockDetail.setBuildingBlockName(buildingBlockName);
        }
        return buildingBlockDetail;
    }


    public OrchestrationStatusStateTransitionDirective getOrchestrationStatusStateTransitionDirective(
        ResourceType resourceType, OrchestrationStatus orchestrationStatus, OrchestrationAction targetAction) {
        return getSingleResource(orchestrationStatusStateTransitionDirectiveClient, UriBuilder
            .fromUri(findOneByResourceTypeAndOrchestrationStatusAndTargetAction)
            .queryParam(RESOURCE_TYPE, resourceType.name())
            .queryParam(ORCHESTRATION_STATUS, orchestrationStatus.name())
            .queryParam(TARGET_ACTION, targetAction.name()).build());
    }

    public List<OrchestrationFlow> getOrchestrationFlowByAction(String action) {
        return this.getMultipleResources(orchestrationClient, UriBuilder
            .fromUri(findByAction).queryParam(ACTION, action).build());
    }

    public List<VnfcInstanceGroupCustomization> getVnfcInstanceGroupsByVnfResourceCust(String modelCustomizationUUID) {
        return this.getMultipleResources(vnfcInstanceGroupCustomizationClient, UriBuilder
            .fromUri(findVnfcInstanceGroupCustomizationByModelCustomizationUUID)
            .queryParam(MODEL_CUSTOMIZATION_UUID, modelCustomizationUUID).build());
    }

    public List<CollectionResourceInstanceGroupCustomization> getCollectionResourceInstanceGroupCustomizationByModelCustUUID(
        String modelCustomizationUUID) {
        return this.getMultipleResources(collectionResourceInstanceGroupCustomizationClient, UriBuilder
            .fromUri(findCollectionResourceInstanceGroupCustomizationByModelCustomizationUUID)
            .queryParam(MODEL_CUSTOMIZATION_UUID, modelCustomizationUUID).build());
    }

    public VfModuleCustomization getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID(
        String modelCustomizationUUID, String vfModuleModelUUID) {
        return this.getSingleResource(vfModuleCustomizationClient, getUri(UriBuilder
            .fromUri(findByModelCustomizationUUIDAndVfModuleModelUUID)
            .queryParam(MODEL_CUSTOMIZATION_UUID, modelCustomizationUUID)
            .queryParam(VF_MODULE_MODEL_UUID, vfModuleModelUUID).build().toString()));
    }

    public NorthBoundRequest getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(String requestAction,
        String resourceName, boolean aLaCarte) {
        return this.getSingleResource(northBoundRequestClient, UriBuilder
            .fromUri(findOneByActionAndRequestScopeAndIsAlacarte)
            .queryParam(ACTION, requestAction).queryParam(REQUEST_SCOPE, resourceName)
            .queryParam(IS_ALACARTE, aLaCarte).build());
    }

    public NorthBoundRequest getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(String requestAction,
        String resourceName, boolean aLaCarte, String cloudOwner) {
        return this.getSingleResource(northBoundRequestClient, getUri(UriBuilder
            .fromUri(findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwnerAndServiceType)
            .queryParam(ACTION, requestAction).queryParam(REQUEST_SCOPE, resourceName)
            .queryParam(IS_ALACARTE, aLaCarte)
            .queryParam(CLOUD_OWNER, cloudOwner)
            .queryParam(SERVICE_TYPE, ASTERISK).build().toString()));
    }

    public NorthBoundRequest getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwnerAndServiceType(
        String requestAction,
        String resourceName, boolean aLaCarte, String cloudOwner, String serviceType) {
        return this.getSingleResource(northBoundRequestClient, getUri(UriBuilder
            .fromUri(findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwnerAndServiceType)
            .queryParam(ACTION, requestAction).queryParam(REQUEST_SCOPE, resourceName)
            .queryParam(IS_ALACARTE, aLaCarte)
            .queryParam(CLOUD_OWNER, cloudOwner)
            .queryParam(SERVICE_TYPE, serviceType).build().toString()));
    }

    public RainyDayHandlerStatus getRainyDayHandlerStatusByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(
        String flowName, String serviceType, String vnfType, String errorCode, String workStep) {
        return this.getSingleResource(rainyDayHandlerStatusClient, getUri(UriBuilder
            .fromUri(findOneByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep)
            .queryParam(FLOW_NAME, flowName).queryParam(SERVICE_TYPE, serviceType)
            .queryParam(VNF_TYPE, vnfType).queryParam(ERROR_CODE, errorCode).queryParam(WORK_STEP, workStep)
            .build().toString()));
    }

    public ServiceRecipe getFirstByServiceModelUUIDAndAction(String modelUUID, String action) {
        return this.getSingleResource(serviceRecipeClient, getUri(UriBuilder
            .fromUri(findFirstByServiceModelUUIDAndActionURI)
            .queryParam(SERVICE_MODEL_UUID, modelUUID)
            .queryParam(ACTION, action).build().toString()));
    }

    public NetworkRecipe getFirstNetworkRecipeByModelNameAndAction(String modelName, String action) {
        return this.getSingleResource(networkRecipeClient, UriBuilder
            .fromUri(findFirstByModelNameAndAction)
            .queryParam(MODEL_NAME, modelName)
            .queryParam(ACTION, action).build());
    }

    public ControllerSelectionReference getControllerSelectionReferenceByVnfTypeAndActionCategory(String vnfType,
        String actionCategory) {
        return this.getSingleResource(controllerSelectionReferenceClient, UriBuilder
            .fromUri(endpoint
                + "/controllerSelectionReference/search/findControllerSelectionReferenceByVnfTypeAndActionCategory")
            .queryParam("VNF_TYPE", vnfType).queryParam("ACTION_CATEGORY", actionCategory).build());
    }

    public Service getFirstByModelNameOrderByModelVersionDesc(String modelName) {
        return this.getSingleResource(serviceClient, UriBuilder
            .fromUri(findFirstByModelNameURI)
            .queryParam(MODEL_NAME, modelName).build());
    }

    public ExternalServiceToInternalService findExternalToInternalServiceByServiceName(String serviceName) {
        return this.getSingleResource(externalServiceToInternalServiceClient, getUri(UriBuilder
            .fromUri(findExternalToInternalServiceByServiceName)
            .queryParam(SERVICE_NAME, serviceName).build().toString()));
    }

    public ServiceRecipe findServiceRecipeByActionAndServiceModelUUID(String action, String modelUUID) {
        return this.getSingleResource(serviceRecipeClient, getUri(UriBuilder
            .fromUri(findServiceRecipeByActionAndServiceModelUUID)
            .queryParam(ACTION, action)
            .queryParam(SERVICE_MODEL_UUID, modelUUID).build().toString()));
    }

    public Service getServiceByModelName(String modelName) {
        return this.getSingleResource(serviceClient, getUri(UriBuilder
            .fromUri(findServiceByModelName)
            .queryParam(MODEL_NAME, modelName).build().toString()));
    }

    public Service getServiceByModelUUID(String modelModelUUID) {
        return this.getSingleResource(serviceClient, getUri(UriBuilder
            .fromUri(findServiceByModelUUID)
            .queryParam(MODEL_UUID, modelModelUUID).build().toString()));
    }

    public VnfResource getFirstVnfResourceByModelInvariantUUIDAndModelVersion(String modelInvariantUUID,
        String modelVersion) {
        return this.getSingleResource(vnfResourceClient, getUri(UriBuilder
            .fromUri(findFirstResourceByModelInvariantUUIDAndModelVersion)
            .queryParam(MODEL_INVARIANT_UUID, modelInvariantUUID)
            .queryParam(MODEL_VERSION, modelVersion).build().toString()));
    }


    public VnfResourceCustomization getFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources(
        String modelInstanceName, VnfResource vnfResource) {
        return this.getSingleResource(vnfResourceCustomizationClient, getUri(UriBuilder
            .fromUri(findByModelInstanceNameAndVnfResources)
            .queryParam(MODEL_INSTANCE_NAME, modelInstanceName)
            .queryParam(VNF_RESOURCE_MODEL_UUID, vnfResource.getModelUUID()).build().toString()));
    }

    public VnfRecipe getFirstVnfRecipeByNfRoleAndAction(String nfRole, String action) {
        return this.getSingleResource(vnfRecipeClient, getUri(UriBuilder
            .fromUri(findFirstVnfRecipeByNfRoleAndAction)
            .queryParam(NF_ROLE, nfRole)
            .queryParam(ACTION, action).build().toString()));
    }

    public VnfComponentsRecipe getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(
        String vfModuleModelUUID, String vnfComponentType, String action) {
        return this.getSingleResource(vnfComponentsRecipeClient, getUri(UriBuilder
            .fromUri(findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction)
            .queryParam(VF_MODULE_MODEL_UUID, vfModuleModelUUID)
            .queryParam(VNF_COMPONENT_TYPE, vnfComponentType)
            .queryParam(ACTION, action).build().toString()));
    }

    public VnfComponentsRecipe getFirstVnfComponentsRecipeByVnfComponentTypeAndAction(String vnfComponentType,
        String action) {
        return this.getSingleResource(vnfComponentsRecipeClient, getUri(UriBuilder
            .fromUri(findFirstVnfComponentsRecipeByVnfComponentTypeAndAction)
            .queryParam(VNF_COMPONENT_TYPE, vnfComponentType)
            .queryParam(ACTION, action).build().toString()));
    }

    protected URI getUri(String template) {
        return URI.create(template);
    }

    public CloudifyManager getCloudifyManager(String id) {
        return this.getSingleResource(cloudifyManagerClient, getUri(cloudifyManagerURI + id));
    }

    public CloudSite getCloudSite(String id) {
        return this.getSingleResource(cloudSiteClient,
            getUri(cloudSiteURI + id));
    }

    public CloudSite getCloudSite(String id, String uri) {
        return this.getSingleResource(cloudSiteClient,
            getUri(uri + id));
    }

    public void postCloudSite(CloudSite cloudSite) {
        this.postSingleResource(cloudSiteClient, cloudSite);
    }

    public CloudSite getCloudSiteByClliAndAicVersion(String clli, String cloudVersion) {
        return this.getSingleResource(cloudSiteClient, getUri(UriBuilder
            .fromUri(findByClliAndCloudVersion)
            .queryParam(CLLI, clli).queryParam(CLOUD_VERSION, cloudVersion).build().toString()));
    }

    public HomingInstance getHomingInstance(String serviceInstanceId) {
        return this.getSingleResource(homingInstanceClient,
            getUri(homingInstanceURI + serviceInstanceId));
    }

    public HomingInstance getHomingInstance(String serviceInstanceId, String uri) {
        return this.getSingleResource(homingInstanceClient,
            getUri(uri + serviceInstanceId));
    }

    public void postHomingInstance(HomingInstance homingInstance) {
        this.postSingleResource(homingInstanceClient, homingInstance);
    }

    public Service getServiceByModelVersionAndModelInvariantUUID(String modelVersion, String modelInvariantUUID) {
        return this.getSingleResource(serviceClient, getUri(UriBuilder
            .fromUri(findFirstByModelVersionAndModelInvariantUUIDURI)
            .queryParam(MODEL_VERSION, modelVersion)
            .queryParam(MODEL_INVARIANT_UUID, modelInvariantUUID).build().toString()));
    }

    public VfModule getVfModuleByModelInvariantUUIDAndModelVersion(String modelInvariantUUID, String modelVersion) {
        return this.getSingleResource(vfModuleClient, getUri(UriBuilder
            .fromUri(findFirstVfModuleByModelInvariantUUIDAndModelVersion)
            .queryParam(MODEL_INVARIANT_UUID, modelInvariantUUID)
            .queryParam(MODEL_VERSION, modelVersion).build().toString()));
    }

    public List<Service> getServiceByModelInvariantUUIDOrderByModelVersionDesc(String modelInvariantUUID) {
        return this.getMultipleResources(serviceClient, getUri(UriBuilder
            .fromUri(findByModelInvariantUUIDURI)
            .queryParam(MODEL_INVARIANT_UUID, modelInvariantUUID).build().toString()));
    }

    public List<VfModule> getVfModuleByModelInvariantUUIDOrderByModelVersionDesc(String modelInvariantUUID) {
        return this.getMultipleResources(vfModuleClient, getUri(UriBuilder
            .fromUri(findVfModuleByModelInvariantUUIDOrderByModelVersionDesc)
            .queryParam(MODEL_INVARIANT_UUID, modelInvariantUUID).build().toString()));
    }

    private <T> T getSingleResource(Client<T> client, URI uri) {
        return client.get(uri);
    }

    private <T> List<T> getMultipleResources(Client<T> client, URI uri) {
        Iterable<T> iterator = client.getAll(uri);
        List<T> list = new ArrayList<>();
        Iterator<T> it = iterator.iterator();
        it.forEachRemaining(list::add);
        return list;
    }

    private <T> URI postSingleResource(Client<T> client, T type) {
        return client.post(type);
    }

    public List<CvnfcCustomization> getCvnfcCustomizationByVnfCustomizationUUIDAndVfModuleCustomizationUUID(
        String vnfCustomizationUUID, String vfModuleCustomizationUUID) {

        return this.getMultipleResources(cvnfcCustomizationClient, getUri(UriBuilder
            .fromUri(endpoint + "/cvnfcCustomization/search/findByVnfResourceCustomizationAndVfModuleCustomization")
            .queryParam("VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID", vnfCustomizationUUID)
            .queryParam("VF_MODULE_CUST_MODEL_CUSTOMIZATION_UUID", vfModuleCustomizationUUID).build().toString()));
    }

    public VnfVfmoduleCvnfcConfigurationCustomization getVnfVfmoduleCvnfcConfigurationCustomizationByVnfCustomizationUuidAndVfModuleCustomizationUuidAndCvnfcCustomizationUuid(String vnfCustomizationUuid,
            String vfModuleCustomizationUuid, String cvnfcCustomizationUuid) {
        return this.getSingleResource(vnfVfmoduleCvnfcConfigurationCustomizationClient, getUri(UriBuilder
                .fromUri(endpoint + "/vnfVfmoduleCvnfcConfigurationCustomization/search/findOneByVnfResourceCustomizationAndVfModuleCustomizationAndCvnfcCustomization")
                .queryParam("VNF_RESOURCE_CUST_MODEL_CUSTOMIZATION_UUID", vnfCustomizationUuid)
                .queryParam("VF_MODULE_MODEL_CUSTOMIZATION_UUID", vfModuleCustomizationUuid)
                .queryParam("CVNFC_MODEL_CUSTOMIZATION_UUID", cvnfcCustomizationUuid).build().toString()));
    }
}
