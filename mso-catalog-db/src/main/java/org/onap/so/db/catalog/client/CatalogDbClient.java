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

import com.google.common.base.Strings;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.http.HttpStatus;
import org.onap.so.logging.filter.base.Constants;
import org.onap.so.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.db.catalog.beans.BBNameSelectionReference;
import org.onap.so.db.catalog.beans.BuildingBlockDetail;
import org.onap.so.db.catalog.beans.BuildingBlockRollback;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.ExternalServiceToInternalService;
import org.onap.so.db.catalog.beans.HomingInstance;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkRecipe;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.OrchestrationAction;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.onap.so.db.catalog.beans.PnfResource;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.ProcessingFlags;
import org.onap.so.db.catalog.beans.ResourceType;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfComponentsRecipe;
import org.onap.so.db.catalog.beans.VnfRecipe;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.onap.so.rest.catalog.beans.Vnf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component("CatalogDbClient")
public class CatalogDbClient {

    private static final Logger logger = LoggerFactory.getLogger(CatalogDbClient.class);

    private static final String CLOUD_SITE = "/cloudSite";
    private static final String CLOUDIFY_MANAGER = "/cloudifyManager";
    private static final String RAINY_DAY_HANDLER_MACRO = "/rainy_day_handler_macro";
    private static final String NORTHBOUND_REQUEST_REF_LOOKUP = "/northbound_request_ref_lookup";
    private static final String NETWORK_RESOURCE_CUSTOMIZATION = "/networkResourceCustomization";
    private static final String NETWORK_RESOURCE = "/networkResource";
    private static final String COLLECTION_RESOURCE_INSTANCE_GROUP_CUSTOMIZATION =
            "/collectionResourceInstanceGroupCustomization";
    private static final String VNFC_INSTANCE_GROUP_CUSTOMIZATION = "/vnfcInstanceGroupCustomization";
    private static final String ORCHESTRATION_FLOW = "/orchestrationFlow";
    private static final String ORCHESTRATION_STATUS_STATE_TRANSITION_DIRECTIVE =
            "/orchestrationStatusStateTransitionDirective";
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
    private static final String WORKFLOW = "/workflow";
    private static final String BB_NAME_SELECTION_REFERENCE = "/bbNameSelectionReference";
    private static final String PROCESSING_FLAGS = "/processingFlags";
    private static final String BB_ROLLBACK = "/buildingBlockRollback";


    private static final String SEARCH = "/search";
    private static final String URI_SEPARATOR = "/";

    protected static final String SERVICE_MODEL_UUID = "serviceModelUUID";
    protected static final String SERVICE_NAME = "serviceName";
    protected static final String MODEL_UUID = "modelUUID";
    protected static final String MODEL_CUSTOMIZATION_UUID = "modelCustomizationUUID";
    protected static final String ACTION = "action";
    protected static final String MODEL_NAME = "modelName";
    protected static final String MODEL_VERSION = "modelVersion";
    protected static final String MODEL_INVARIANT_UUID = "modelInvariantUUID";
    protected static final String VNF_RESOURCE_MODEL_UUID = "vnfResourceModelUUID";
    protected static final String PNF_RESOURCE_MODEL_UUID = "pnfResourceModelUUID";
    protected static final String NF_ROLE = "nfRole";
    protected static final String VF_MODULE_MODEL_UUID = "vfModuleModelUUID";
    protected static final String VNF_COMPONENT_TYPE = "vnfComponentType";
    protected static final String BUILDING_BLOCK_NAME = "buildingBlockName";
    protected static final String RESOURCE_TYPE = "resourceType";
    protected static final String ORCHESTRATION_STATUS = "orchestrationStatus";
    protected static final String TARGET_ACTION = "targetAction";
    protected static final String REQUEST_SCOPE = "requestScope";
    protected static final String IS_ALACARTE = "isALaCarte";
    protected static final String CLOUD_OWNER = "cloudOwner";
    protected static final String FLOW_NAME = "flowName";
    protected static final String ERROR_MESSAGE = "errorMessage";
    protected static final String SERVICE_ROLE = "serviceRole";
    protected static final String SERVICE_TYPE = "serviceType";
    protected static final String VNF_TYPE = "vnfType";
    protected static final String ERROR_CODE = "errorCode";
    protected static final String WORK_STEP = "workStep";
    protected static final String CLLI = "clli";
    protected static final String CLOUD_VERSION = "cloudVersion";
    protected static final String HOMING_INSTANCE = "/homingInstance";
    protected static final String ARTIFACT_UUID = "artifactUUID";
    protected static final String SOURCE = "source";
    protected static final String RESOURCE_TARGET = "resourceTarget";
    protected static final String FLAG = "flag";
    protected static final String OPERATION_NAME = "operationName";

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
    private String findFirstResourceByModelInvariantUUIDAndModelVersion =
            "/findFirstResourceByModelInvariantUUIDAndModelVersion";
    private String findByModelInstanceNameAndVnfResources = "/findByModelInstanceNameAndVnfResources";
    private String findFirstVnfRecipeByNfRoleAndAction = "/findFirstVnfRecipeByNfRoleAndAction";
    private String findByModelCustomizationUUIDAndVfModuleModelUUID =
            "/findFirstByModelCustomizationUUIDAndVfModuleModelUUIDOrderByCreatedDesc";
    private String findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction =
            "/findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction";
    private String findFirstVnfComponentsRecipeByVnfComponentTypeAndAction =
            "/findFirstVnfComponentsRecipeByVnfComponentTypeAndAction";
    private String findVfModuleByModelInvariantUUIDOrderByModelVersionDesc =
            "/findByModelInvariantUUIDOrderByModelVersionDesc";
    private String findFirstVfModuleByModelInvariantUUIDAndModelVersion =
            "/findFirstVfModuleByModelInvariantUUIDAndModelVersion";
    private String findOneByBuildingBlockName = "/findOneByBuildingBlockName";
    private String findOneByResourceTypeAndOrchestrationStatusAndTargetAction =
            "/findOneByResourceTypeAndOrchestrationStatusAndTargetAction";
    private String findByAction = "/findByAction";
    private String findVnfcInstanceGroupCustomizationByModelCustomizationUUID = "/findByModelCustomizationUUID";
    private String findCollectionResourceInstanceGroupCustomizationByModelCustomizationUUID =
            "/findByModelCustomizationUUID";
    private String findOneByActionAndRequestScopeAndIsAlacarte = "/findOneByActionAndRequestScopeAndIsAlacarte";
    private String findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwner =
            "/findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwner";
    private String findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwnerAndServiceType =
            "/findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwnerAndServiceType";

    private static final String findRainyDayHandler = "/findRainyDayHandler";
    private String findByClliAndCloudVersion = "/findByClliAndCloudVersion";
    private String findServiceByServiceInstanceId = "/findServiceByServiceInstanceId";
    private String findPnfResourceCustomizationByModelUuid = "/findPnfResourceCustomizationByModelUuid";
    private String findWorkflowByArtifactUUID = "/findByArtifactUUID";
    private String findWorkflowByVnfModelUUID = "/findWorkflowByVnfModelUUID";
    private String findWorkflowByPnfModelUUID = "/findWorkflowByPnfModelUUID";
    private String findWorkflowBySource = "/findBySource";
    private String findVnfResourceCustomizationByModelUuid = "/findVnfResourceCustomizationByModelUuid";
    private String findBBNameSelectionReferenceByControllerActorAndScopeAndAction =
            "/findBBNameSelectionReferenceByControllerActorAndScopeAndAction";
    private String findWorkflowByResourceTarget = "/findByResourceTarget";
    private String findProcessingFlagsByFlag = "/findByFlag";
    private String findWorkflowByOperationName = "/findByOperationName";

    private String serviceURI;
    private String vfModuleURI;
    private String vnfResourceURI;
    private String networkCollectionResourceCustomizationURI;
    private String networkResourceCustomizationURI;
    private String networkResourceURI;
    private String collectionNetworkResourceCustomizationURI;
    private String instanceGroupURI;
    private String cloudifyManagerURI;
    private String cloudSiteURI;
    private String homingInstanceURI;
    private String cvnfcResourceCustomizationURI;
    private String pnfResourceURI;
    private String pnfResourceCustomizationURI;
    private String workflowURI;
    private String buildingBlockRollbacksURI;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${mso.catalog.db.spring.endpoint:#{null}}")
    private String endpoint;

    @Value("${mso.db.auth:#{null}}")
    private String msoAdaptersAuth;

    @Autowired
    RestTemplate restTemplate;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        webClient = webClientBuilder.baseUrl(endpoint).defaultHeader(HttpHeaders.AUTHORIZATION, msoAdaptersAuth)
                .defaultHeader(Constants.HttpHeaders.TARGET_ENTITY_HEADER, TARGET_ENTITY).build();
        findExternalToInternalServiceByServiceName = endpoint + EXTERNAL_SERVICE_TO_INTERNAL_MODEL_MAPPING + SEARCH
                + findExternalToInternalServiceByServiceName;
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
        findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction = endpoint + VNF_COMPONENTS_RECIPE
                + SEARCH + findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction;
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
        findVnfcInstanceGroupCustomizationByModelCustomizationUUID = endpoint + VNFC_INSTANCE_GROUP_CUSTOMIZATION
                + SEARCH + findVnfcInstanceGroupCustomizationByModelCustomizationUUID;
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
        findByClliAndCloudVersion = endpoint + CLOUD_SITE + SEARCH + findByClliAndCloudVersion;

        findPnfResourceCustomizationByModelUuid =
                endpoint + PNF_RESOURCE_CUSTOMIZATION + SEARCH + findPnfResourceCustomizationByModelUuid;

        findWorkflowByArtifactUUID = endpoint + WORKFLOW + SEARCH + findWorkflowByArtifactUUID;
        findWorkflowByVnfModelUUID = endpoint + WORKFLOW + SEARCH + findWorkflowByVnfModelUUID;
        findWorkflowByPnfModelUUID = endpoint + WORKFLOW + SEARCH + findWorkflowByPnfModelUUID;
        findWorkflowBySource = endpoint + WORKFLOW + SEARCH + findWorkflowBySource;
        findWorkflowByResourceTarget = endpoint + WORKFLOW + SEARCH + findWorkflowByResourceTarget;

        findVnfResourceCustomizationByModelUuid =
                endpoint + VNF_RESOURCE_CUSTOMIZATION + SEARCH + findVnfResourceCustomizationByModelUuid;

        findBBNameSelectionReferenceByControllerActorAndScopeAndAction = endpoint + BB_NAME_SELECTION_REFERENCE + SEARCH
                + findBBNameSelectionReferenceByControllerActorAndScopeAndAction;

        findProcessingFlagsByFlag = endpoint + PROCESSING_FLAGS + SEARCH + findProcessingFlagsByFlag;
        findWorkflowByOperationName = endpoint + WORKFLOW + SEARCH + findWorkflowByOperationName;

        serviceURI = endpoint + SERVICE + URI_SEPARATOR;
        vfModuleURI = endpoint + VFMODULE + URI_SEPARATOR;
        vnfResourceURI = endpoint + VNF_RESOURCE + URI_SEPARATOR;
        networkCollectionResourceCustomizationURI =
                endpoint + NETWORK_COLLECTION_RESOURCE_CUSTOMIZATION + URI_SEPARATOR;
        networkResourceCustomizationURI = endpoint + NETWORK_RESOURCE_CUSTOMIZATION + URI_SEPARATOR;
        networkResourceURI = endpoint + NETWORK_RESOURCE + SEARCH;
        collectionNetworkResourceCustomizationURI =
                endpoint + COLLECTION_NETWORK_RESOURCE_CUSTOMIZATION + URI_SEPARATOR;
        instanceGroupURI = endpoint + INSTANCE_GROUP + URI_SEPARATOR;
        cloudifyManagerURI = endpoint + CLOUDIFY_MANAGER + URI_SEPARATOR;
        cloudSiteURI = endpoint + CLOUD_SITE + URI_SEPARATOR;
        homingInstanceURI = endpoint + HOMING_INSTANCE + URI_SEPARATOR;
        pnfResourceURI = endpoint + PNF_RESOURCE + URI_SEPARATOR;
        pnfResourceCustomizationURI = endpoint + PNF_RESOURCE_CUSTOMIZATION + URI_SEPARATOR;
        workflowURI = endpoint + WORKFLOW + URI_SEPARATOR;
        buildingBlockRollbacksURI = endpoint + BB_ROLLBACK + URI_SEPARATOR;
    }

    /*
     * Removed classes from clientfactory, scope for improving code here Migration from unsupported Bowman to Webclient
     */
    public CatalogDbClient() {}

    public CatalogDbClient(String baseUri, String auth) {
        this.endpoint = baseUri;
        this.msoAdaptersAuth = auth;
        init();
    }

    public NetworkCollectionResourceCustomization getNetworkCollectionResourceCustomizationByID(
            String modelCustomizationUUID) {
        NetworkCollectionResourceCustomization networkCollectionResourceCustomization =
                this.getSingleResource(NetworkCollectionResourceCustomization.class,
                        getUri(networkCollectionResourceCustomizationURI + modelCustomizationUUID));
        if (networkCollectionResourceCustomization != null) {
            networkCollectionResourceCustomization.setModelCustomizationUUID(modelCustomizationUUID);
        }
        return networkCollectionResourceCustomization;
    }

    public Service getServiceByID(String modelUUID) {
        try {
            Service service = getSingleResource(Service.class, getUri(serviceURI + modelUUID));
            if (service != null) {
                service.setModelUUID(modelUUID);
            }
            return service;
        } catch (WebClientResponseException.NotFound e) {
            throw new EntityNotFoundException("Unable to find Service with modelUUID: " + modelUUID, e);
        }
    }

    public VfModule getVfModuleByModelUUID(String modelUUID) {
        VfModule vfModule = getSingleResource(VfModule.class, getUri(vfModuleURI + modelUUID));
        if (vfModule != null) {
            vfModule.setModelUUID(modelUUID);
        }
        return vfModule;
    }

    public VnfResource getVnfResourceByModelUUID(String modelUUID) {

        VnfResource vnfResource = this.getSingleResource(VnfResource.class, getUri(vnfResourceURI + modelUUID));
        if (vnfResource != null) {
            vnfResource.setModelUUID(modelUUID);
        }
        return vnfResource;
    }

    // A VNFResource customization UUID is the same object across services, so we can return anyone from the list
    // In the future the client should query starting at a service model uuid
    public VnfResourceCustomization getVnfResourceCustomizationByModelCustomizationUUID(String modelCustomizationUUID) {
        List<VnfResourceCustomization> vnfResourceCustomization = this.getMultipleResources(
                VnfResourceCustomization.class, getUri(endpoint + VNF_RESOURCE_CUSTOMIZATION + SEARCH
                        + "/findByModelCustomizationUUID" + "?MODEL_CUSTOMIZATION_UUID=" + modelCustomizationUUID));
        if (vnfResourceCustomization != null && !vnfResourceCustomization.isEmpty()) {
            return vnfResourceCustomization.get(0);
        } else {
            return null;
        }
    }

    public List<VnfResourceCustomization> getVnfResourceCustomizationByModelUuid(String modelUuid) {
        return this.getMultipleResources(VnfResourceCustomization.class,
                getUri(UriBuilder.fromUri(findVnfResourceCustomizationByModelUuid)
                        .queryParam("SERVICE_MODEL_UUID", modelUuid).build().toString()));
    }

    public PnfResource getPnfResourceByModelUUID(String modelUUID) {
        PnfResource PnfResource = this.getSingleResource(PnfResource.class, getUri(pnfResourceURI + modelUUID));
        if (PnfResource != null) {
            PnfResource.setModelUUID(modelUUID);
        }
        return PnfResource;
    }

    public PnfResourceCustomization getPnfResourceCustomizationByModelCustomizationUUID(String modelCustomizationUUID) {
        PnfResourceCustomization pnfResourceCustomization = getSingleResource(PnfResourceCustomization.class,
                getUri(pnfResourceCustomizationURI + modelCustomizationUUID));
        if (pnfResourceCustomization != null) {
            pnfResourceCustomization.setModelCustomizationUUID(modelCustomizationUUID);
        }
        return pnfResourceCustomization;
    }

    public List<PnfResourceCustomization> getPnfResourceCustomizationByModelUuid(String modelUuid) {
        return this.getMultipleResources(PnfResourceCustomization.class,
                getUri(UriBuilder.fromUri(findPnfResourceCustomizationByModelUuid)
                        .queryParam("SERVICE_MODEL_UUID", modelUuid).build().toString()));
    }

    public CollectionNetworkResourceCustomization getCollectionNetworkResourceCustomizationByID(
            String modelCustomizationUUID) {
        CollectionNetworkResourceCustomization collectionNetworkResourceCustomization =
                this.getSingleResource(CollectionNetworkResourceCustomization.class,
                        getUri(UriBuilder.fromUri(collectionNetworkResourceCustomizationURI + modelCustomizationUUID)
                                .build().toString()));
        if (collectionNetworkResourceCustomization != null) {
            collectionNetworkResourceCustomization.setModelCustomizationUUID(modelCustomizationUUID);
        }
        return collectionNetworkResourceCustomization;
    }

    public InstanceGroup getInstanceGroupByModelUUID(String modelUUID) {
        InstanceGroup instanceGroup = this.getSingleResource(InstanceGroup.class, getUri(instanceGroupURI + modelUUID));
        if (instanceGroup != null) {
            instanceGroup.setModelUUID(modelUUID);
        }
        return instanceGroup;
    }

    public VfModuleCustomization getVfModuleCustomizationByModelCuztomizationUUID(String modelCustomizationUUID) {
        return this.getSingleResource(VfModuleCustomization.class,
                getUri(endpoint + VFMODULE_CUSTOMIZATION + SEARCH
                        + "/findFirstByModelCustomizationUUIDOrderByCreatedDesc" + "?MODEL_CUSTOMIZATION_UUID="
                        + modelCustomizationUUID));
    }

    public NetworkResourceCustomization getNetworkResourceCustomizationByModelCustomizationUUID(
            String modelCustomizationUUID) {
        NetworkResourceCustomization networkResourceCustomization = this.getSingleResource(
                NetworkResourceCustomization.class, getUri(networkResourceCustomizationURI + modelCustomizationUUID));
        if (networkResourceCustomization != null) {
            networkResourceCustomization.setModelCustomizationUUID(modelCustomizationUUID);
        }
        return networkResourceCustomization;
    }

    public NetworkResource getNetworkResourceByModelName(String networkType) {
        if (Strings.isNullOrEmpty(networkType)) {
            throw new EntityNotFoundException("networkType passed as Null or Empty String");
        }
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(networkResourceURI + "/findFirstByModelNameOrderByModelVersionDesc")
                            .queryParam("modelName", networkType).build())
                    .retrieve().bodyToMono(NetworkResource.class).block();
        } catch (WebClientResponseException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException("Unable to find NetworkResource By networkType " + networkType);
            }
            throw e;
        }
    }


    public BuildingBlockDetail getBuildingBlockDetail(String buildingBlockName) {
        BuildingBlockDetail buildingBlockDetail =
                getSingleResource(BuildingBlockDetail.class, getUri(UriBuilder.fromUri(findOneByBuildingBlockName)
                        .queryParam(BUILDING_BLOCK_NAME, buildingBlockName).build().toString()));
        if (buildingBlockDetail != null) {
            buildingBlockDetail.setBuildingBlockName(buildingBlockName);
        }
        return buildingBlockDetail;
    }


    public OrchestrationStatusStateTransitionDirective getOrchestrationStatusStateTransitionDirective(
            ResourceType resourceType, OrchestrationStatus orchestrationStatus, OrchestrationAction targetAction) {
        return getSingleResource(OrchestrationStatusStateTransitionDirective.class,
                UriBuilder.fromUri(findOneByResourceTypeAndOrchestrationStatusAndTargetAction)
                        .queryParam(RESOURCE_TYPE, resourceType.name())
                        .queryParam(ORCHESTRATION_STATUS, orchestrationStatus.name())
                        .queryParam(TARGET_ACTION, targetAction.name()).build());
    }

    public List<OrchestrationFlow> getOrchestrationFlowByAction(String action) {
        return this.getMultipleResources(OrchestrationFlow.class,
                UriBuilder.fromUri(findByAction).queryParam(ACTION, action).build());
    }

    public List<VnfcInstanceGroupCustomization> getVnfcInstanceGroupsByVnfResourceCust(String modelCustomizationUUID) {
        return this.getMultipleResources(VnfcInstanceGroupCustomization.class,
                UriBuilder.fromUri(findVnfcInstanceGroupCustomizationByModelCustomizationUUID)
                        .queryParam(MODEL_CUSTOMIZATION_UUID, modelCustomizationUUID).build());
    }

    public List<CollectionResourceInstanceGroupCustomization> getCollectionResourceInstanceGroupCustomizationByModelCustUUID(
            String modelCustomizationUUID) {
        return this.getMultipleResources(CollectionResourceInstanceGroupCustomization.class,
                UriBuilder.fromUri(findCollectionResourceInstanceGroupCustomizationByModelCustomizationUUID)
                        .queryParam(MODEL_CUSTOMIZATION_UUID, modelCustomizationUUID).build());
    }

    public VfModuleCustomization getVfModuleCustomizationByModelCustomizationUUIDAndVfModuleModelUUID(
            String modelCustomizationUUID, String vfModuleModelUUID) {
        return this.getSingleResource(VfModuleCustomization.class,
                getUri(UriBuilder.fromUri(findByModelCustomizationUUIDAndVfModuleModelUUID)
                        .queryParam("MODEL_CUSTOMIZATION_UUID", modelCustomizationUUID)
                        .queryParam("MODEL_UUID", vfModuleModelUUID).build().toString()));
    }

    public NorthBoundRequest getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(String requestAction,
            String resourceName, boolean aLaCarte) {
        return this.getSingleResource(NorthBoundRequest.class,
                UriBuilder.fromUri(findOneByActionAndRequestScopeAndIsAlacarte).queryParam(ACTION, requestAction)
                        .queryParam(REQUEST_SCOPE, resourceName).queryParam(IS_ALACARTE, aLaCarte).build());
    }

    public NorthBoundRequest getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(String requestAction,
            String resourceName, boolean aLaCarte, String cloudOwner) {
        return this.getSingleResource(NorthBoundRequest.class,
                getUri(UriBuilder.fromUri(findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwnerAndServiceType)
                        .queryParam(ACTION, requestAction).queryParam(REQUEST_SCOPE, resourceName)
                        .queryParam(IS_ALACARTE, aLaCarte).queryParam(CLOUD_OWNER, cloudOwner)
                        .queryParam(SERVICE_TYPE, ASTERISK).build().toString()));
    }

    public NorthBoundRequest getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwnerAndServiceType(
            String requestAction, String resourceName, boolean aLaCarte, String cloudOwner, String serviceType) {
        return this.getSingleResource(NorthBoundRequest.class,
                getUri(UriBuilder.fromUri(findOneByActionAndRequestScopeAndIsAlacarteAndCloudOwnerAndServiceType)
                        .queryParam(ACTION, requestAction).queryParam(REQUEST_SCOPE, resourceName)
                        .queryParam(IS_ALACARTE, aLaCarte).queryParam(CLOUD_OWNER, cloudOwner)
                        .queryParam(SERVICE_TYPE, serviceType).build().toString()));
    }

    public RainyDayHandlerStatus getRainyDayHandlerStatus(String flowName, String serviceType, String vnfType,
            String errorCode, String workStep, String errorMessage, String serviceRole) {
        logger.debug(
                "Get Rainy Day Status - Flow Name {}, Service Type: {} , vnfType {} , errorCode {}, workStep {}, errorMessage {}",
                flowName, serviceType, vnfType, errorCode, workStep, errorMessage);
        return this.getSingleResource(RainyDayHandlerStatus.class,
                UriComponentsBuilder.fromUriString(endpoint + RAINY_DAY_HANDLER_MACRO + SEARCH + findRainyDayHandler)
                        .queryParam(FLOW_NAME, flowName).queryParam(SERVICE_TYPE, serviceType)
                        .queryParam(VNF_TYPE, vnfType).queryParam(ERROR_CODE, errorCode).queryParam(WORK_STEP, workStep)
                        .queryParam(ERROR_MESSAGE, errorMessage).queryParam(SERVICE_ROLE, serviceRole).build().encode()
                        .toUri());
    }

    public ServiceRecipe getFirstByServiceModelUUIDAndAction(String modelUUID, String action) {
        return this.getSingleResource(ServiceRecipe.class,
                getUri(UriBuilder.fromUri(findFirstByServiceModelUUIDAndActionURI)
                        .queryParam(SERVICE_MODEL_UUID, modelUUID).queryParam(ACTION, action).build().toString()));
    }


    public NetworkRecipe getFirstNetworkRecipeByModelNameAndAction(String modelName, String action) {
        return this.getSingleResource(NetworkRecipe.class, UriBuilder.fromUri(findFirstByModelNameAndAction)
                .queryParam(MODEL_NAME, modelName).queryParam(ACTION, action).build());
    }

    public ControllerSelectionReference getControllerSelectionReferenceByVnfTypeAndActionCategory(String vnfType,
            String actionCategory) {
        return this.getSingleResource(ControllerSelectionReference.class, UriBuilder.fromUri(endpoint
                + "/controllerSelectionReference/search/findControllerSelectionReferenceByVnfTypeAndActionCategory")
                .queryParam("VNF_TYPE", vnfType).queryParam("ACTION_CATEGORY", actionCategory).build());
    }

    public Service getFirstByModelNameOrderByModelVersionDesc(String modelName) {
        return this.getSingleResource(Service.class,
                UriBuilder.fromUri(findFirstByModelNameURI).queryParam(MODEL_NAME, modelName).build());
    }

    public BBNameSelectionReference getBBNameSelectionReference(String controllerActor, String scope, String action) {

        return this.getSingleResource(BBNameSelectionReference.class,
                getUri(UriBuilder.fromUri(findBBNameSelectionReferenceByControllerActorAndScopeAndAction)
                        .queryParam("CONTROLLER_ACTOR", controllerActor).queryParam("SCOPE", scope)
                        .queryParam("ACTION", action).build().toString()));
    }

    public ExternalServiceToInternalService findExternalToInternalServiceByServiceName(String serviceName) {
        return this.getSingleResource(ExternalServiceToInternalService.class,
                getUri(UriBuilder.fromUri(findExternalToInternalServiceByServiceName)
                        .queryParam(SERVICE_NAME, serviceName).build().toString()));
    }

    public ServiceRecipe findServiceRecipeByActionAndServiceModelUUID(String action, String modelUUID) {
        return this.getSingleResource(ServiceRecipe.class,
                getUri(UriBuilder.fromUri(findServiceRecipeByActionAndServiceModelUUID).queryParam(ACTION, action)
                        .queryParam(SERVICE_MODEL_UUID, modelUUID).build().toString()));
    }

    public Service getServiceByModelName(String modelName) {
        return this.getSingleResource(Service.class, getUri(
                UriBuilder.fromUri(findServiceByModelName).queryParam(MODEL_NAME, modelName).build().toString()));
    }

    public Service getServiceByModelUUID(String modelModelUUID) {
        return this.getSingleResource(Service.class, getUri(
                UriBuilder.fromUri(findServiceByModelUUID).queryParam(MODEL_UUID, modelModelUUID).build().toString()));
    }

    public VnfResource getFirstVnfResourceByModelInvariantUUIDAndModelVersion(String modelInvariantUUID,
            String modelVersion) {
        return this.getSingleResource(VnfResource.class,
                getUri(UriBuilder.fromUri(findFirstResourceByModelInvariantUUIDAndModelVersion)
                        .queryParam(MODEL_INVARIANT_UUID, modelInvariantUUID).queryParam(MODEL_VERSION, modelVersion)
                        .build().toString()));
    }


    public VnfResourceCustomization getFirstVnfResourceCustomizationByModelInstanceNameAndVnfResources(
            String modelInstanceName, VnfResource vnfResource) {
        return this.getSingleResource(VnfResourceCustomization.class,
                getUri(UriBuilder.fromUri(findByModelInstanceNameAndVnfResources)
                        .queryParam("MODEL_INSTANCE_NAME", modelInstanceName)
                        .queryParam("VNF_RESOURCE_MODEL_UUID", vnfResource.getModelUUID()).build().toString()));
    }

    public VnfRecipe getFirstVnfRecipeByNfRoleAndAction(String nfRole, String action) {
        return this.getSingleResource(VnfRecipe.class, getUri(UriBuilder.fromUri(findFirstVnfRecipeByNfRoleAndAction)
                .queryParam(NF_ROLE, nfRole).queryParam(ACTION, action).build().toString()));
    }

    public VnfComponentsRecipe getFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction(
            String vfModuleModelUUID, String modelType, String action) {
        return this.getSingleResource(VnfComponentsRecipe.class,
                getUri(UriBuilder.fromUri(findFirstVnfComponentsRecipeByVfModuleModelUUIDAndVnfComponentTypeAndAction)
                        .queryParam(VF_MODULE_MODEL_UUID, vfModuleModelUUID).queryParam(VNF_COMPONENT_TYPE, modelType)
                        .queryParam(ACTION, action).build().toString()));
    }

    public VnfComponentsRecipe getFirstVnfComponentsRecipeByVnfComponentTypeAndAction(String modelType, String action) {
        return this.getSingleResource(VnfComponentsRecipe.class,
                getUri(UriBuilder.fromUri(findFirstVnfComponentsRecipeByVnfComponentTypeAndAction)
                        .queryParam(VNF_COMPONENT_TYPE, modelType).queryParam(ACTION, action).build().toString()));
    }


    protected URI getUri(String template) {
        return URI.create(template);
    }

    public CloudifyManager getCloudifyManager(String id) {
        return this.getSingleResource(CloudifyManager.class, getUri(cloudifyManagerURI + id));
    }

    public CloudSite getCloudSite(String id) {
        return this.getSingleResource(CloudSite.class, getUri(cloudSiteURI + id));
    }

    public CloudSite getCloudSite(String id, String uri) {
        return this.getSingleResource(CloudSite.class, getUri(uri + id));
    }

    // Bring back old version of methind since the caller - OofInfraUtils.java - is not running in a spring context
    public void postOofHomingCloudSite(CloudSite cloudSite) {
        this.postSingleResource(CloudSite.class, cloudSite);
    }

    public CloudSite postCloudSite(CloudSite cloudSite) {
        if (cloudSite == null) {
            throw new EntityNotFoundException("CloudSite passed as null");
        }
        try {
            return webClient.post().uri(uriBuilder -> uriBuilder.path("/cloudSite").build()).bodyValue(cloudSite)
                    .retrieve().bodyToMono(CloudSite.class).block();
        } catch (WebClientResponseException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException("Unable to find CloudSite with Cloud Site Id: " + cloudSite.getId());
            }
            throw e;
        }
    }

    public CloudSite updateCloudSite(CloudSite cloudSite) {
        if (cloudSite == null) {
            throw new EntityNotFoundException("CloudSite passed as null");
        }
        try {
            return webClient.put().uri(uriBuilder -> uriBuilder.path("/cloudSite/" + cloudSite.getId()).build())
                    .bodyValue(cloudSite).retrieve().bodyToMono(CloudSite.class).block();
        } catch (WebClientResponseException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException("Unable to find CloudSite with Cloud Site Id: " + cloudSite.getId());
            }
            throw e;
        }
    }

    public void deleteCloudSite(String cloudSiteId) {
        if (cloudSiteId == null) {
            throw new EntityNotFoundException("CloudSiteId passed as null");
        }
        try {
            webClient.delete().uri(uriBuilder -> uriBuilder.path("/cloudSite/" + cloudSiteId).build()).retrieve()
                    .bodyToMono(Void.class).block();
        } catch (WebClientResponseException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException("Unable to find CloudSite with Cloud Site Id: " + cloudSiteId);
            }
            throw e;
        }
    }

    public List<CloudSite> getCloudSites() {
        return this.getMultipleResources(CloudSite.class,
                UriBuilder.fromUri(endpoint + CLOUD_SITE).queryParam("size", "1000").build());
    }


    public CloudSite getCloudSiteByClliAndAicVersion(String clli, String cloudVersion) {
        return this.getSingleResource(CloudSite.class, getUri(UriBuilder.fromUri(findByClliAndCloudVersion)
                .queryParam(CLLI, clli).queryParam(CLOUD_VERSION, cloudVersion).build().toString()));
    }

    public HomingInstance getHomingInstance(String serviceInstanceId) {
        return this.getSingleResource(HomingInstance.class, getUri(homingInstanceURI + serviceInstanceId));
    }

    public HomingInstance getHomingInstance(String serviceInstanceId, String uri) {
        return this.getSingleResource(HomingInstance.class, getUri(uri + serviceInstanceId));
    }

    public void postHomingInstance(HomingInstance homingInstance) {
        this.postSingleResource(HomingInstance.class, homingInstance);
    }

    public Service getServiceByModelVersionAndModelInvariantUUID(String modelVersion, String modelInvariantUUID) {
        return this.getSingleResource(Service.class,
                getUri(UriBuilder.fromUri(findFirstByModelVersionAndModelInvariantUUIDURI)
                        .queryParam(MODEL_VERSION, modelVersion).queryParam(MODEL_INVARIANT_UUID, modelInvariantUUID)
                        .build().toString()));
    }

    public VfModule getVfModuleByModelInvariantUUIDAndModelVersion(String modelInvariantUUID, String modelVersion) {
        return this.getSingleResource(VfModule.class,
                getUri(UriBuilder.fromUri(findFirstVfModuleByModelInvariantUUIDAndModelVersion)
                        .queryParam(MODEL_INVARIANT_UUID, modelInvariantUUID).queryParam(MODEL_VERSION, modelVersion)
                        .build().toString()));
    }

    public List<Service> getServiceByModelInvariantUUIDOrderByModelVersionDesc(String modelInvariantUUID) {
        return this.getMultipleResources(Service.class, getUri(UriBuilder.fromUri(findByModelInvariantUUIDURI)
                .queryParam(MODEL_INVARIANT_UUID, modelInvariantUUID).build().toString()));
    }

    public List<VfModule> getVfModuleByModelInvariantUUIDOrderByModelVersionDesc(String modelInvariantUUID) {
        return this.getMultipleResources(VfModule.class,
                getUri(UriBuilder.fromUri(findVfModuleByModelInvariantUUIDOrderByModelVersionDesc)
                        .queryParam(MODEL_INVARIANT_UUID, modelInvariantUUID).build().toString()));
    }

    private <T> T getSingleResource(Class<T> clazz, URI uri) {
        return webClient.get().uri(uri).retrieve().bodyToMono(clazz).block();
    }

    private <T> List<T> getMultipleResources(Class<T> clazz, URI uri) {
        return webClient.get().uri(uri).retrieve().bodyToFlux(clazz).collectList().block();
    }

    private <T> URI postSingleResource(Class<T> clazz, T type) {
        return webClient.post().uri(endpoint).bodyValue(type).retrieve().bodyToMono(URI.class).block();
    }

    private <T> void deleteSingleResource(Class<T> clazz, URI uri) {
        webClient.delete().uri(uri).retrieve().bodyToMono(Void.class).block();
    }

    public List<CvnfcCustomization> getCvnfcCustomization(String serviceModelUUID, String vnfCustomizationUUID,
            String vfModuleCustomizationUUID) {
        Service service = this.getServiceByID(serviceModelUUID);
        VnfResourceCustomization vnfResourceCust =
                findVnfResourceCustomizationInList(vnfCustomizationUUID, service.getVnfCustomizations());
        VfModuleCustomization vfModuleCust =
                findVfModuleCustomizationInList(vfModuleCustomizationUUID, vnfResourceCust.getVfModuleCustomizations());
        return vfModuleCust.getCvnfcCustomization();
    }

    public VnfResourceCustomization findVnfResourceCustomizationInList(String vnfCustomizationUUID,
            List<VnfResourceCustomization> vnfResourceCusts) {
        if (vnfCustomizationUUID == null) {
            throw new EntityNotFoundException(
                    "a NULL UUID was provided in query to search for VnfResourceCustomization");
        }
        List<VnfResourceCustomization> filtered =
                vnfResourceCusts.stream().filter(v -> v.getModelCustomizationUUID() != null)
                        .filter(vnfCustRes -> vnfCustomizationUUID.equals(vnfCustRes.getModelCustomizationUUID()))
                        .collect(Collectors.toList());
        if (filtered != null && !filtered.isEmpty() && filtered.size() == 1) {
            return filtered.get(0);
        } else
            throw new EntityNotFoundException(
                    "Unable to find VnfResourceCustomization ModelCustomizationUUID:" + vnfCustomizationUUID);
    }

    protected VfModuleCustomization findVfModuleCustomizationInList(String vfModuleCustomizationUUID,
            List<VfModuleCustomization> vfModuleList) {
        if (vfModuleCustomizationUUID == null) {
            throw new EntityNotFoundException("a NULL UUID was provided in query to search for VfModuleCustomization");
        }
        List<VfModuleCustomization> filtered = vfModuleList.stream().filter(v -> v.getModelCustomizationUUID() != null)
                .filter(vfModuleCust -> vfModuleCustomizationUUID.equals(vfModuleCust.getModelCustomizationUUID()))
                .collect(Collectors.toList());
        if (filtered != null && !filtered.isEmpty() && filtered.size() == 1) {
            return filtered.get(0);
        } else
            throw new EntityNotFoundException(
                    "Unable to find VfModuleCustomization ModelCustomizationUUID:" + vfModuleCustomizationUUID);
    }

    protected CvnfcCustomization findCvnfcCustomizationInAList(String cvnfcCustomizationUuid,
            List<CvnfcCustomization> cvnfcCustomList) {
        if (cvnfcCustomizationUuid == null) {
            throw new EntityNotFoundException("a NULL UUID was provided in query to search for CvnfcCustomization");
        }
        List<CvnfcCustomization> filtered = cvnfcCustomList.stream().filter(c -> c.getModelCustomizationUUID() != null)
                .filter(cvnfc -> cvnfcCustomizationUuid.equals(cvnfc.getModelCustomizationUUID()))
                .collect(Collectors.toList());
        if (filtered != null && !filtered.isEmpty() && filtered.size() == 1) {
            logger.debug("Found CvnfcCustomization: {}", filtered.get(0));
            return filtered.get(0);
        } else
            throw new EntityNotFoundException(
                    "Unable to find CvnfcCustomization ModelCustomizationUUID:" + cvnfcCustomizationUuid);
    }

    public CvnfcConfigurationCustomization getCvnfcCustomization(String serviceModelUUID, String vnfCustomizationUuid,
            String vfModuleCustomizationUuid, String cvnfcCustomizationUuid) {
        List<CvnfcCustomization> cvnfcCustomization =
                getCvnfcCustomization(serviceModelUUID, vnfCustomizationUuid, vfModuleCustomizationUuid);
        CvnfcCustomization cvnfc = findCvnfcCustomizationInAList(cvnfcCustomizationUuid, cvnfcCustomization);
        List<CvnfcConfigurationCustomization> fabricConfigs = cvnfc.getCvnfcConfigurationCustomization();
        fabricConfigs.stream().filter(cvnfcCustom -> cvnfcCustom.getConfigurationResource().getToscaNodeType()
                .contains("FabricConfiguration")).collect(Collectors.toList());
        if (fabricConfigs != null && !fabricConfigs.isEmpty() && fabricConfigs.size() == 1) {
            logger.debug("Found Fabric Configuration: {}", fabricConfigs.get(0));
            return fabricConfigs.get(0);
        } else
            throw new EntityNotFoundException(
                    "Unable to find CvnfcConfigurationCustomization ModelCustomizationUUID:" + cvnfcCustomizationUuid);
    }

    public org.onap.so.rest.catalog.beans.Service getServiceModelInformation(String serviceModelUUID, String depth) {
        if (Strings.isNullOrEmpty(serviceModelUUID)) {
            throw new EntityNotFoundException("Service Model UUID passed as Null or Empty String");
        }
        try {
            HttpEntity<?> entity = getHttpEntity();
            return restTemplate.exchange(
                    UriComponentsBuilder.fromUriString(endpoint + "/ecomp/mso/catalog/v1/services/" + serviceModelUUID)
                            .queryParam("depth", depth).build().encode().toString(),
                    HttpMethod.GET, entity, org.onap.so.rest.catalog.beans.Service.class).getBody();
        } catch (HttpClientErrorException e) {
            logger.warn("Entity Not found in DLP", e);
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException("Unable to find Service with ServiceModelUUID:" + serviceModelUUID);
            }
            throw e;
        }
    }

    public void deleteServiceRecipe(String recipeId) {
        this.deleteSingleResource(ServiceRecipe.class,
                UriBuilder.fromUri(endpoint + SERVICE_RECIPE + URI_SEPARATOR + recipeId).build());
    }

    public void postServiceRecipe(ServiceRecipe recipe) {
        try {
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<ServiceRecipe> entity = new HttpEntity<>(recipe, headers);
            restTemplate.exchange(
                    UriComponentsBuilder.fromUriString(endpoint + "/serviceRecipe").build().encode().toString(),
                    HttpMethod.POST, entity, ServiceRecipe.class).getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException("Unable to find ServiceRecipe with  Id: " + recipe.getId());
            }
            throw e;
        }
    }

    public void postVnfRecipe(VnfRecipe recipe) {
        try {
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<VnfRecipe> entity = new HttpEntity<>(recipe, headers);
            restTemplate
                    .exchange(UriComponentsBuilder.fromUriString(endpoint + "/vnfRecipe").build().encode().toString(),
                            HttpMethod.POST, entity, VnfRecipe.class)
                    .getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException("Unable to find VnfRecipe with  Id: " + recipe.getId());
            }
            throw e;
        }
    }

    public void postNetworkRecipe(NetworkRecipe recipe) {
        try {
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<NetworkRecipe> entity = new HttpEntity<>(recipe, headers);
            restTemplate.exchange(
                    UriComponentsBuilder.fromUriString(endpoint + "/networkRecipe").build().encode().toString(),
                    HttpMethod.POST, entity, NetworkRecipe.class).getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException("Unable to find NetworkRecipe with  Id: " + recipe.getId());
            }
            throw e;
        }
    }

    public List<ServiceRecipe> getServiceRecipes() {
        return this.getMultipleResources(ServiceRecipe.class,
                UriBuilder.fromUri(endpoint + SERVICE_RECIPE).queryParam("size", "1000").build());
    }

    public List<NetworkRecipe> getNetworkRecipes() {
        return this.getMultipleResources(NetworkRecipe.class,
                UriBuilder.fromUri(endpoint + NETWORK_RECIPE).queryParam("size", "1000").build());
    }

    public List<NetworkResource> getNetworkResources() {
        return this.getMultipleResources(NetworkResource.class,
                UriBuilder.fromUri(endpoint + "/networkResource").queryParam("size", "1000").build());
    }

    public List<org.onap.so.rest.catalog.beans.Service> getServices() {
        try {
            HttpEntity<?> entity = getHttpEntity();
            return restTemplate
                    .exchange(
                            UriComponentsBuilder.fromUriString(endpoint + "/ecomp/mso/catalog/v1/services").build()
                                    .encode().toString(),
                            HttpMethod.GET, entity,
                            new ParameterizedTypeReference<List<org.onap.so.rest.catalog.beans.Service>>() {})
                    .getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling catalog database", e);
            throw e;
        }
    }

    public List<VnfResource> getVnfResources() {
        return this.getMultipleResources(VnfResource.class,
                UriBuilder.fromUri(endpoint + "/vnfResource").queryParam("size", "1000").build());
    }

    public List<VnfRecipe> getVnfRecipes() {
        return this.getMultipleResources(VnfRecipe.class,
                UriBuilder.fromUri(endpoint + VNF_RECIPE).queryParam("size", "1000").build());
    }

    public org.onap.so.rest.catalog.beans.Vnf getVnfModelInformation(String serviceModelUUID,
            String vnfCustomizationUUID, String depth) {
        if (Strings.isNullOrEmpty(serviceModelUUID)) {
            throw new EntityNotFoundException("Service Model UUID passed as Null or Empty String");
        }
        if (Strings.isNullOrEmpty(vnfCustomizationUUID)) {
            throw new EntityNotFoundException("Vnf Customization UUID passed as Null or Empty String");
        }
        try {
            HttpEntity<?> entity = getHttpEntity();
            return restTemplate
                    .exchange(
                            UriComponentsBuilder
                                    .fromUriString(endpoint + "/ecomp/mso/catalog/v1/services/" + serviceModelUUID
                                            + "/vnfs/" + vnfCustomizationUUID)
                                    .queryParam("depth", depth).build().encode().toString(),
                            HttpMethod.GET, entity, org.onap.so.rest.catalog.beans.Vnf.class)
                    .getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(
                        "Unable to find Vnf with Vnf Customization UUID:" + vnfCustomizationUUID);
            }
            throw e;
        }
    }

    public void updateVnf(String serviceModelUUID, org.onap.so.rest.catalog.beans.Vnf vnf) {
        if (vnf == null) {
            throw new EntityNotFoundException("Vnf passed as null");
        }
        try {
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<org.onap.so.rest.catalog.beans.Vnf> entity = new HttpEntity<>(vnf, headers);

            restTemplate.exchange(
                    UriComponentsBuilder.fromUriString(endpoint + "/ecomp/mso/catalog/v1/services/" + serviceModelUUID
                            + "/vnfs/" + vnf.getModelCustomizationId()).build().encode().toString(),
                    HttpMethod.PUT, entity, org.onap.so.rest.catalog.beans.Vnf.class).getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                throw new EntityNotFoundException(
                        "Unable to find Vnf with Vnf Customization UUID:" + vnf.getModelCustomizationId());
            }
            throw e;
        }
    }


    public Workflow findWorkflowByArtifactUUID(String artifactUUID) {
        return this.getSingleResource(Workflow.class, getUri(UriBuilder.fromUri(findWorkflowByArtifactUUID)
                .queryParam(ARTIFACT_UUID, artifactUUID).build().toString()));
    }

    public List<Workflow> findWorkflowByVnfModelUUID(String vnfResourceModelUUID) {
        return this.getMultipleResources(Workflow.class, getUri(UriBuilder.fromUri(findWorkflowByVnfModelUUID)
                .queryParam(VNF_RESOURCE_MODEL_UUID, vnfResourceModelUUID).build().toString()));
    }

    public List<Workflow> findWorkflowByPnfModelUUID(String pnfResourceModelUUID) {
        return this.getMultipleResources(Workflow.class, getUri(UriBuilder.fromUri(findWorkflowByPnfModelUUID)
                .queryParam(PNF_RESOURCE_MODEL_UUID, pnfResourceModelUUID).build().toString()));
    }

    public List<Workflow> findWorkflowBySource(String source) {
        return this.getMultipleResources(Workflow.class,
                getUri(UriBuilder.fromUri(findWorkflowBySource).queryParam(SOURCE, source).build().toString()));
    }

    public List<Workflow> findWorkflowByResourceTarget(String resourceTarget) {
        return this.getMultipleResources(Workflow.class, getUri(UriBuilder.fromUri(findWorkflowByResourceTarget)
                .queryParam(RESOURCE_TARGET, resourceTarget).build().toString()));
    }

    public List<Workflow> findWorkflowByOperationName(String operationName) {
        return this.getMultipleResources(Workflow.class, getUri(UriBuilder.fromUri(findWorkflowByOperationName)
                .queryParam(OPERATION_NAME, operationName).build().toString()));
    }

    public ProcessingFlags findProcessingFlagsByFlag(String flag) {
        return this.getSingleResource(ProcessingFlags.class,
                getUri(UriBuilder.fromUri(findProcessingFlagsByFlag).queryParam(FLAG, flag).build().toString()));
    }

    // TODO: redo using buildingBlockRollbackClient
    public List<BuildingBlockRollback> getBuildingBlockRollbackEntries() {
        try {
            HttpEntity<?> entity = getHttpEntity();
            return restTemplate.exchange(
                    UriComponentsBuilder.fromUriString(endpoint + "/ecomp/mso/catalog/v1/buildingBlockRollback").build()
                            .encode().toString(),
                    HttpMethod.GET, entity, new ParameterizedTypeReference<List<BuildingBlockRollback>>() {}).getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error Calling catalog database", e);
            throw e;
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, msoAdaptersAuth);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        headers.set(Constants.HttpHeaders.TARGET_ENTITY_HEADER, TARGET_ENTITY);
        return headers;
    }

    private HttpEntity<?> getHttpEntity() {
        HttpHeaders headers = getHttpHeaders();
        return new HttpEntity<>(headers);
    }
}
