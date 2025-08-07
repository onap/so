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
 * Modifications Copyright (c) 2023 Nordix Foundation
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperProvider;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Platform;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTableReference;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceProxy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Tenant;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Vnfc;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.entities.ServiceModel;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.NoServiceInstanceFoundException;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.ResourceNotFoundException;
import org.onap.so.bpmn.servicedecomposition.tasks.exceptions.ServiceModelNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.CvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceProxyResourceCustomization;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.Networks;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component("BBInputSetup")
public class BBInputSetup implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(BBInputSetup.class);
    private static final String FLOW_VAR_NAME = "flowToBeCalled";
    private static final String LOOKUP_KEY_MAP_VAR_NAME = "lookupKeyMap";
    private static final String GBB_INPUT_VAR_NAME = "gBBInput";
    private static final String EXECUTE_BB_VAR_NAME = "buildingBlock";
    private static final String VOLUME_GROUP = "VolumeGroup";
    private static final String VF_MODULE = "VfModule";
    private static final String NETWORK = "Network";
    private static final String VNF = "Vnf";
    private static final String PNF = "Pnf";
    private static final String NETWORK_COLLECTION = "NetworkCollection";
    private static final String PREPROV = "PREPROV";
    private static final String CREATEVOLUME = "CreateVolume";
    private static final String CONTROLLER = "Controller";

    @Autowired
    private BBInputSetupUtils bbInputSetupUtils;

    @Autowired
    private BBInputSetupMapperLayer mapperLayer;

    @Autowired
    private CloudInfoFromAAI cloudInfoFromAAI;

    @Autowired
    private ExceptionBuilder exceptionUtil;

    private static final ObjectMapper mapper = new ObjectMapper();

    public BBInputSetupUtils getBbInputSetupUtils() {
        return bbInputSetupUtils;
    }

    public void setCloudInfoFromAAI(CloudInfoFromAAI cloudInfoFromAAI) {
        this.cloudInfoFromAAI = cloudInfoFromAAI;
    }

    public void setBbInputSetupUtils(BBInputSetupUtils bbInputSetupUtils) {
        this.bbInputSetupUtils = bbInputSetupUtils;
    }

    public BBInputSetupMapperLayer getMapperLayer() {
        return mapperLayer;
    }

    public void setMapperLayer(BBInputSetupMapperLayer mapperLayer) {
        this.mapperLayer = mapperLayer;
    }

    /**
     * This method is used for executing the building block.
     *
     * It will get the BB from the execution object by checking if the aLaCarte and homing is true.
     *
     * Then it will get the GBB and execute it.
     *
     * @param execution
     * @throws Exception
     * @return
     */
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            GeneralBuildingBlock outputBB = null;
            ExecuteBuildingBlock executeBB = this.getExecuteBBFromExecution(execution);
            String resourceId = executeBB.getResourceId();
            String requestAction = executeBB.getRequestAction();
            String vnfType = executeBB.getVnfType();
            boolean aLaCarte = Boolean.TRUE.equals(executeBB.isaLaCarte());
            boolean homing = Boolean.TRUE.equals(executeBB.isHoming());
            Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
            outputBB = this.getGBB(executeBB, lookupKeyMap, requestAction, aLaCarte, resourceId, vnfType);
            logger.debug("setting Homing");
            if (executeBB.getBuildingBlock().getBpmnFlowName().contains("AssignRANNssiBB")) {
                execution.setVariable("homing", true);
            } else {
                execution.setVariable("homing", false);
            }

            logger.debug("GeneralBB: {}", mapper.writeValueAsString(outputBB));

            setHomingFlag(outputBB, homing, lookupKeyMap);

            execution.setVariable(FLOW_VAR_NAME, executeBB.getBuildingBlock().getBpmnFlowName());
            execution.setVariable(GBB_INPUT_VAR_NAME, outputBB);
            execution.setVariable(LOOKUP_KEY_MAP_VAR_NAME, lookupKeyMap);

            if (outputBB.getRequestContext().getIsHelm()) {
                execution.setVariable("isHelm", true);
            } else {
                execution.setVariable("isHelm", false);
            }

            BuildingBlockExecution gBuildingBlockExecution = new DelegateExecutionImpl(execution);
            execution.setVariable("gBuildingBlockExecution", gBuildingBlockExecution);
            execution.setVariable("RetryCount", 1);
            execution.setVariable("handlingCode", "Success");
        } catch (Exception e) {
            logger.error("Exception occurred", e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e.getMessage());
        }
    }

    protected void setHomingFlag(GeneralBuildingBlock outputBB, boolean homing, Map<ResourceKey, String> lookupKeyMap) {

        if (lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID) != null && homing && outputBB != null) {
            for (GenericVnf vnf : outputBB.getCustomer().getServiceSubscription().getServiceInstances().get(0)
                    .getVnfs()) {
                if (vnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
                    vnf.setCallHoming(homing);
                }
            }
        }
    }

    protected ExecuteBuildingBlock getExecuteBBFromExecution(DelegateExecution execution) {
        return (ExecuteBuildingBlock) execution.getVariable(EXECUTE_BB_VAR_NAME);
    }

    protected GeneralBuildingBlock getGBB(ExecuteBuildingBlock executeBB, Map<ResourceKey, String> lookupKeyMap,
            String requestAction, boolean aLaCarte, String resourceId, String vnfType) throws Exception {
        String requestId = executeBB.getRequestId();
        this.populateLookupKeyMapWithIds(executeBB.getWorkflowResourceIds(), lookupKeyMap);
        RequestDetails requestDetails = executeBB.getRequestDetails();
        if (requestDetails == null) {
            requestDetails = bbInputSetupUtils.getRequestDetails(requestId);
        }
        if (requestDetails.getModelInfo() == null) {
            if (requestAction.contains("RanSlice")) {
                logger.debug(">>> RequestAction: {}", executeBB.getRequestAction());

                return this.getGBBRanSlicing(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId);
            }
            return this.getGBBCM(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId);
        } else {
            ModelType modelType = requestDetails.getModelInfo().getModelType();
            if (aLaCarte && modelType.equals(ModelType.service)) {
                return this.getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId);
            } else if (aLaCarte && !modelType.equals(ModelType.service)) {
                return this.getGBBALaCarteNonService(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId,
                        vnfType);
            } else {
                return this.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
            }
        }
    }

    protected void populateLookupKeyMapWithIds(WorkflowResourceIds workflowResourceIds,
            Map<ResourceKey, String> lookupKeyMap) {
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, workflowResourceIds.getServiceInstanceId());
        lookupKeyMap.put(ResourceKey.NETWORK_ID, workflowResourceIds.getNetworkId());
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, workflowResourceIds.getVnfId());
        lookupKeyMap.put(ResourceKey.PNF, workflowResourceIds.getPnfId());
        lookupKeyMap.put(ResourceKey.VF_MODULE_ID, workflowResourceIds.getVfModuleId());
        lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, workflowResourceIds.getVolumeGroupId());
        lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, workflowResourceIds.getConfigurationId());
        lookupKeyMap.put(ResourceKey.INSTANCE_GROUP_ID, workflowResourceIds.getInstanceGroupId());
        lookupKeyMap.put(ResourceKey.VNF_INSTANCE_NAME, workflowResourceIds.getVnfInstanceName());
        lookupKeyMap.put(ResourceKey.VF_MODULE_INSTANCE_NAME, workflowResourceIds.getVfModuleInstanceName());
        lookupKeyMap.put(ResourceKey.CHILD_SERVICE_INSTANCE_ID, workflowResourceIds.getChildServiceInstanceId());
        lookupKeyMap.put(ResourceKey.CHILD_SERVICE_INSTANCE_NAME, workflowResourceIds.getChildServiceInstanceName());
        lookupKeyMap.put(ResourceKey.PNF_INSTANCE_NAME, workflowResourceIds.getPnfInstanceName());
    }

    protected GeneralBuildingBlock getGBBALaCarteNonService(ExecuteBuildingBlock executeBB,
            RequestDetails requestDetails, Map<ResourceKey, String> lookupKeyMap, String requestAction,
            String resourceId, String vnfType) throws Exception {
        String bbName = executeBB.getBuildingBlock().getBpmnFlowName();
        String serviceInstanceId = lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID);
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = null;
        ServiceModel serviceModel = new ServiceModel();
        Service service = null;
        Service newService = null;
        boolean isReplace = false;
        if (serviceInstanceId != null) {
            aaiServiceInstance = bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId);
            if (aaiServiceInstance != null) {
                if (requestAction.equalsIgnoreCase("replaceInstance")
                        || requestAction.equalsIgnoreCase("replaceInstanceRetainAssignments")) {
                    RelatedInstanceList[] relatedInstanceList = requestDetails.getRelatedInstanceList();
                    if (relatedInstanceList != null) {
                        for (RelatedInstanceList relatedInstList : relatedInstanceList) {
                            RelatedInstance relatedInstance = relatedInstList.getRelatedInstance();
                            if (relatedInstance.getModelInfo().getModelType().equals(ModelType.service)) {
                                newService = bbInputSetupUtils.getCatalogServiceByModelUUID(
                                        relatedInstance.getModelInfo().getModelVersionId());
                                isReplace = true;
                            }
                        }
                    }
                }

                service = bbInputSetupUtils.getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());

                serviceModel.setNewService(newService);
                serviceModel.setCurrentService(service);

                if (service == null) {
                    String message = String.format(
                            "Related service instance model not found in MSO CatalogDB: model-version-id=%s",
                            aaiServiceInstance.getModelVersionId());
                    throw new ServiceModelNotFoundException(message);
                } else if (newService == null && isReplace) {
                    String message = "Related service instance model in Request not found in MSO CatalogDB";
                    throw new ServiceModelNotFoundException(message);
                }
            } else {
                String message = String.format("Related service instance from AAI not found: service-instance-id=%s",
                        serviceInstanceId);
                throw new NoServiceInstanceFoundException(message);
            }
        }

        ServiceInstance serviceInstance = this.getExistingServiceInstance(aaiServiceInstance);
        if (isReplace) {
            serviceInstance.setModelInfoServiceInstance(
                    this.mapperLayer.mapCatalogServiceIntoServiceInstance(serviceModel.getNewService()));
        } else {
            serviceInstance.setModelInfoServiceInstance(
                    this.mapperLayer.mapCatalogServiceIntoServiceInstance(serviceModel.getCurrentService()));
        }
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestId(executeBB.getRequestId())
                .setRequestDetails(requestDetails).setService(service).setBbName(bbName)
                .setServiceInstance(serviceInstance).setLookupKeyMap(lookupKeyMap).setResourceId(resourceId)
                .setVnfType(vnfType).setKey(executeBB.getBuildingBlock().getKey())
                .setConfigurationResourceKeys(executeBB.getConfigurationResourceKeys()).setExecuteBB(executeBB)
                .setRequestAction(requestAction).setIsReplace(isReplace).setServiceModel(serviceModel).build();
        this.populateObjectsOnAssignAndCreateFlows(parameter);
        return this.populateGBBWithSIAndAdditionalInfo(parameter);
    }

    protected GeneralBuildingBlock getGBBCM(ExecuteBuildingBlock executeBB, RequestDetails requestDetails,
            Map<ResourceKey, String> lookupKeyMap, String requestAction, String resourceId) throws Exception {
        ServiceInstance serviceInstance = new ServiceInstance();
        String serviceInstanceId = lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID);
        serviceInstance.setServiceInstanceId(serviceInstanceId);
        Customer customer = new Customer();
        List<GenericVnf> genericVnfs = serviceInstance.getVnfs();

        String vnfId = lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID);
        if (vnfId != null && !vnfId.isEmpty()) {
            org.onap.aai.domain.yang.GenericVnf aaiGenericVnf = bbInputSetupUtils.getAAIGenericVnf(vnfId);
            GenericVnf genericVnf = this.mapperLayer.mapAAIGenericVnfIntoGenericVnf(aaiGenericVnf);
            genericVnfs.add(genericVnf);
        }
        String instanceGroupId = lookupKeyMap.get(ResourceKey.INSTANCE_GROUP_ID);
        if (instanceGroupId != null && !instanceGroupId.isEmpty()) {
            org.onap.aai.domain.yang.InstanceGroup aaiInstancegroup =
                    bbInputSetupUtils.getAAIInstanceGroup(instanceGroupId);
            InstanceGroup instanceGroup = this.mapperLayer.mapAAIInstanceGroupIntoInstanceGroup(aaiInstancegroup);
            instanceGroup.setOrchestrationStatus(OrchestrationStatus.INVENTORIED);

            if (serviceInstanceId == null) {
                Optional<org.onap.aai.domain.yang.ServiceInstance> aaiServiceInstanceOpt =
                        bbInputSetupUtils.getRelatedServiceInstanceFromInstanceGroup(instanceGroupId);
                if (aaiServiceInstanceOpt.isPresent()) {
                    org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = aaiServiceInstanceOpt.get();
                    serviceInstance = this.mapperLayer.mapAAIServiceInstanceIntoServiceInstance(aaiServiceInstance);
                    WorkflowResourceIds workflowResourceIds = executeBB.getWorkflowResourceIds();
                    workflowResourceIds.setServiceInstanceId(serviceInstance.getServiceInstanceId());
                    lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, workflowResourceIds.getServiceInstanceId());
                } else {
                    throw new NoServiceInstanceFoundException("Related ServiceInstance not found in A&AI.");
                }
            }
            RelatedInstanceList[] relatedInstanceList = requestDetails.getRelatedInstanceList();
            if (relatedInstanceList != null) {
                for (RelatedInstanceList relatedInstList : relatedInstanceList) {
                    RelatedInstance relatedInstance = relatedInstList.getRelatedInstance();
                    if (relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf)) {
                        org.onap.aai.domain.yang.GenericVnf aaiVnf =
                                bbInputSetupUtils.getAAIGenericVnf(relatedInstance.getInstanceId());
                        GenericVnf vnf = this.mapperLayer.mapAAIGenericVnfIntoGenericVnf(aaiVnf);
                        instanceGroup.getVnfs().add(vnf);
                    }
                }
            }

            serviceInstance.getInstanceGroups().add(instanceGroup);
            customer.setServiceSubscription(new ServiceSubscription());
        }
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestDetails(requestDetails)
                .setServiceInstance(serviceInstance).setExecuteBB(executeBB).setRequestAction(requestAction)
                .setCustomer(customer).build();
        return this.populateGBBWithSIAndAdditionalInfo(parameter);
    }

    protected GeneralBuildingBlock getGBBRanSlicing(ExecuteBuildingBlock executeBB, RequestDetails requestDetails,
            Map<ResourceKey, String> lookupKeyMap, String requestAction, String resourceId) throws Exception {
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = null;
        String serviceInstanceId = lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID);

        executeBB.setHoming(true);
        Customer customer = new Customer();

        String subscriberId = executeBB.getRequestDetails().getSubscriberInfo().getGlobalSubscriberId();
        customer.setGlobalCustomerId(subscriberId);

        String subscriberName = executeBB.getRequestDetails().getSubscriberInfo().getSubscriberName();
        customer.setSubscriberName(subscriberName);

        String subscriptionType = executeBB.getRequestDetails().getRequestParameters().getSubscriptionServiceType();

        ServiceSubscription serviceSubscription = new ServiceSubscription();
        serviceSubscription.setServiceType(subscriptionType);

        customer.setServiceSubscription(serviceSubscription);

        String bbName = executeBB.getBuildingBlock().getBpmnFlowName();

        serviceInstanceAAI = getServiceInstanceAAI(requestDetails, customer, serviceInstanceId, false, bbName);

        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(serviceInstanceId);
        logger.debug(">>>>> serviceInstanceAAI: {}", serviceInstanceAAI);
        if (serviceInstanceAAI != null) {
            String modelVersionId = serviceInstanceAAI.getModelVersionId();

            Service service = bbInputSetupUtils.getCatalogServiceByModelUUID(modelVersionId);

            // Check if there is any existing method for mapping
            String modelInvariantId = serviceInstanceAAI.getModelInvariantId();
            String modelVersion = service.getModelVersion();
            String serviceType = service.getServiceType();
            String serviceRole = service.getServiceRole();
            String controllerActor = service.getControllerActor();
            String blueprintName = service.getBlueprintName();
            String blueprintVersion = service.getBlueprintVersion();

            ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
            modelInfoServiceInstance.setServiceType(serviceType);
            modelInfoServiceInstance.setServiceRole(serviceRole);
            modelInfoServiceInstance.setControllerActor(controllerActor);
            modelInfoServiceInstance.setBlueprintName(blueprintName);
            modelInfoServiceInstance.setBlueprintVersion(blueprintVersion);
            modelInfoServiceInstance.setModelInvariantUuid(modelInvariantId);
            modelInfoServiceInstance.setModelUuid(modelVersionId);
            modelInfoServiceInstance.setModelVersion(modelVersion);

            serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);
        }
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestDetails(requestDetails)
                .setServiceInstance(serviceInstance).setExecuteBB(executeBB).setRequestAction(requestAction)
                .setCustomer(customer).build();
        return this.populateGBBWithSIAndAdditionalInfo(parameter);

    }

    protected void populateObjectsOnAssignAndCreateFlows(BBInputSetupParameter parameter) throws Exception {
        parameter.setModelInfo(parameter.getRequestDetails().getModelInfo());
        parameter.setInstanceName(parameter.getRequestDetails().getRequestInfo().getInstanceName());
        parameter.setProductFamilyId(parameter.getRequestDetails().getRequestInfo().getProductFamilyId());
        ModelType modelType = parameter.getModelInfo().getModelType();
        parameter.setRelatedInstanceList(parameter.getRequestDetails().getRelatedInstanceList());

        parameter.setPlatform(parameter.getRequestDetails().getPlatform());
        parameter.setLineOfBusiness(parameter.getRequestDetails().getLineOfBusiness());
        String applicationId = "";
        if (parameter.getRequestDetails().getRequestInfo().getApplicationId() != null) {
            applicationId = parameter.getRequestDetails().getRequestInfo().getApplicationId();
            parameter.setApplicationId(applicationId);
        }

        if (modelType.equals(ModelType.network)) {
            parameter.getLookupKeyMap().put(ResourceKey.NETWORK_ID, parameter.getResourceId());
            this.populateL3Network(parameter);
        } else if (modelType.equals(ModelType.vnf) || modelType.equals(ModelType.cnf)) {
            parameter.getLookupKeyMap().put(ResourceKey.GENERIC_VNF_ID, parameter.getResourceId());
            this.populateGenericVnf(parameter);
        } else if (modelType.equals(ModelType.volumeGroup) || (modelType.equals(ModelType.vfModule)
                && (parameter.getBbName().equalsIgnoreCase(AssignFlows.VOLUME_GROUP.toString())
                        || parameter.getBbName().startsWith(CREATEVOLUME)))) {
            parameter.getLookupKeyMap().put(ResourceKey.VOLUME_GROUP_ID, parameter.getResourceId());
            this.populateVolumeGroup(parameter);
        } else if (modelType.equals(ModelType.vfModule)) {
            populateVfModuleOnAssignAndCreateFlows(parameter);
        } else if (modelType.equals(ModelType.instanceGroup)) {
            parameter.getLookupKeyMap().put(ResourceKey.INSTANCE_GROUP_ID, parameter.getResourceId());
            this.populateInstanceGroup(parameter);
        } else {
            return;
        }
    }

    protected void populateInstanceGroup(BBInputSetupParameter parameter) {
        InstanceGroup instanceGroup = new InstanceGroup();
        instanceGroup.setId(parameter.getInstanceGroupId());
        instanceGroup.setInstanceGroupName(parameter.getInstanceName());
        mapCatalogInstanceGroup(instanceGroup, parameter.getModelInfo(), parameter.getService());
        parameter.getServiceInstance().getInstanceGroups().add(instanceGroup);
    }

    protected void populateVfModuleOnAssignAndCreateFlows(BBInputSetupParameter parameter) throws Exception {
        if (parameter.getBbName().contains("Configuration")) {
            parameter.setResourceId(parameter.getLookupKeyMap().get(ResourceKey.CONFIGURATION_ID));
            parameter.getModelInfo().setModelCustomizationUuid(parameter.getConfigurationKey());
            populateConfiguration(parameter);
        } else {
            parameter.getLookupKeyMap().put(ResourceKey.VF_MODULE_ID, parameter.getResourceId());
            parameter.setCloudConfiguration(parameter.getRequestDetails().getCloudConfiguration());
            this.populateVfModule(parameter);
        }
    }

    protected void mapCatalogInstanceGroup(InstanceGroup instanceGroup, ModelInfo modelInfo, Service service) {
        // @TODO: this will populate the instanceGroup model info.
        // Dependent on MSO-5821 653458 US - MSO - Enhance Catalog DB Schema & Adapter
        // to support VNF Groups
    }

    protected void populateConfiguration(BBInputSetupParameter parameter) {
        Configuration configuration = null;
        String replaceVnfModelCustomizationUUID = "";
        if (parameter.getRelatedInstanceList() != null) {
            for (RelatedInstanceList relatedInstList : parameter.getRelatedInstanceList()) {
                RelatedInstance relatedInstance = relatedInstList.getRelatedInstance();
                if (relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf)) {
                    if (parameter.getIsReplace()) {
                        replaceVnfModelCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationId();
                    }
                }
            }
        }
        for (Configuration configurationTemp : parameter.getServiceInstance().getConfigurations()) {
            if (parameter.getLookupKeyMap().get(ResourceKey.CONFIGURATION_ID) != null
                    && configurationTemp.getConfigurationId()
                            .equalsIgnoreCase(parameter.getLookupKeyMap().get(ResourceKey.CONFIGURATION_ID))) {
                configuration = configurationTemp;
                org.onap.aai.domain.yang.Configuration aaiConfiguration =
                        bbInputSetupUtils.getAAIConfiguration(configuration.getConfigurationId());
                if (aaiConfiguration != null) {
                    parameter.getModelInfo().setModelCustomizationUuid(aaiConfiguration.getModelCustomizationId());
                }
            }
        }
        if (configuration == null
                && (parameter.getBbName().equalsIgnoreCase(AssignFlows.FABRIC_CONFIGURATION.toString())
                        || parameter.getBbName().equalsIgnoreCase(AssignFlows.VRF_CONFIGURATION.toString()))) {
            configuration = this.createConfiguration(parameter.getLookupKeyMap(), parameter.getInstanceName(),
                    parameter.getResourceId());
            parameter.getServiceInstance().getConfigurations().add(configuration);
        }
        if (configuration != null && parameter.getBbName().contains("Fabric")) {
            Vnfc vnfc = getVnfcToConfiguration(parameter.getConfigurationResourceKeys().getVnfcName());
            configuration.setVnfc(vnfc);
            if (!parameter.getBbName().contains("Delete")) {
                if (parameter.getIsReplace()) {
                    parameter.getConfigurationResourceKeys()
                            .setVnfResourceCustomizationUUID(replaceVnfModelCustomizationUUID);
                    mapCatalogConfiguration(configuration, parameter.getModelInfo(),
                            parameter.getServiceModel().getNewService(), parameter.getConfigurationResourceKeys());
                } else {
                    mapCatalogConfiguration(configuration, parameter.getModelInfo(),
                            parameter.getServiceModel().getCurrentService(), parameter.getConfigurationResourceKeys());
                }
            }
        } else if (configuration != null && parameter.getBbName().contains("Vrf")) {
            configuration.setModelInfoConfiguration(mapperLayer.mapCatalogConfigurationToConfiguration(
                    findConfigurationResourceCustomization(parameter.getModelInfo(), parameter.getService()), null));
            configuration.setConfigurationType(configuration.getModelInfoConfiguration().getConfigurationType());
            configuration.setConfigurationSubType(configuration.getModelInfoConfiguration().getConfigurationRole());
        }
    }

    protected Vnfc getVnfcToConfiguration(String vnfcName) {
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().vnfc(vnfcName));
        Optional<org.onap.aai.domain.yang.Vnfc> vnfcOp =
                bbInputSetupUtils.getAAIResourceDepthOne(uri).asBean(org.onap.aai.domain.yang.Vnfc.class);
        if (vnfcOp.isPresent()) {
            org.onap.aai.domain.yang.Vnfc vnfcAAI = vnfcOp.get();
            return this.mapperLayer.mapAAIVnfc(vnfcAAI);
        } else {
            return null;
        }
    }

    protected Configuration createConfiguration(Map<ResourceKey, String> lookupKeyMap, String instanceName,
            String resourceId) {
        lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, resourceId);
        Configuration configuration = new Configuration();
        configuration.setConfigurationId(resourceId);
        configuration.setConfigurationName(instanceName);
        configuration.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        return configuration;
    }

    protected void mapCatalogConfiguration(Configuration configuration, ModelInfo modelInfo, Service service,
            ConfigurationResourceKeys configurationResourceKeys) {
        ConfigurationResourceCustomization configurationResourceCustomization =
                findConfigurationResourceCustomization(modelInfo, service);
        CvnfcConfigurationCustomization vnfVfmoduleCvnfcConfigurationCustomization =
                findVnfVfmoduleCvnfcConfigurationCustomization(service.getModelUUID(),
                        configurationResourceKeys.getVfModuleCustomizationUUID(),
                        configurationResourceKeys.getVnfResourceCustomizationUUID(),
                        configurationResourceKeys.getCvnfcCustomizationUUID(), configurationResourceCustomization);
        if (configurationResourceCustomization != null && vnfVfmoduleCvnfcConfigurationCustomization != null) {
            configuration.setModelInfoConfiguration(this.mapperLayer.mapCatalogConfigurationToConfiguration(
                    configurationResourceCustomization, vnfVfmoduleCvnfcConfigurationCustomization));
        } else {
            logger.debug("for Fabric configuration mapping by VF MODULE CUST UUID: "
                    + configurationResourceKeys.getVfModuleCustomizationUUID());
            vnfVfmoduleCvnfcConfigurationCustomization = findVnfVfmoduleCvnfcConfigurationCustomization(
                    service.getModelUUID(), configurationResourceKeys.getVnfResourceCustomizationUUID(),
                    configurationResourceKeys.getVfModuleCustomizationUUID(),
                    configurationResourceKeys.getCvnfcCustomizationUUID());
            if (vnfVfmoduleCvnfcConfigurationCustomization != null) {
                configuration.setModelInfoConfiguration(this.mapperLayer
                        .mapCatalogConfigurationToConfiguration(vnfVfmoduleCvnfcConfigurationCustomization));
            }
        }
    }

    protected CvnfcConfigurationCustomization findVnfVfmoduleCvnfcConfigurationCustomization(String serviceModelUUID,
            String vfModuleCustomizationUUID, String vnfResourceCustomizationUUID, String cvnfcCustomizationUUID,
            ConfigurationResourceCustomization configurationResourceCustomization) {
        return bbInputSetupUtils.getCvnfcConfigurationCustomization(serviceModelUUID, vnfResourceCustomizationUUID,
                vfModuleCustomizationUUID, cvnfcCustomizationUUID);
    }

    protected ConfigurationResourceCustomization findConfigurationResourceCustomization(ModelInfo modelInfo,
            Service service) {
        for (ConfigurationResourceCustomization resourceCust : service.getConfigurationCustomizations()) {
            if (resourceCust.getModelCustomizationUUID().equalsIgnoreCase(modelInfo.getModelCustomizationUuid())) {
                return resourceCust;
            }
        }
        return null;
    }

    protected CvnfcConfigurationCustomization findVnfVfmoduleCvnfcConfigurationCustomization(String serviceModelUUID,
            String vnfResourceCustomizationUUID, String vfModuleCustomizationUUID, String cvnfcCustomizationUUID) {
        return bbInputSetupUtils.getCvnfcConfigurationCustomization(serviceModelUUID, vnfResourceCustomizationUUID,
                vfModuleCustomizationUUID, cvnfcCustomizationUUID);
    }

    protected void populateVfModule(BBInputSetupParameter parameter) throws Exception {
        String vnfModelCustomizationUUID = null;
        String replaceVnfModelCustomizationUUID = null;
        if (parameter.getRelatedInstanceList() != null) {
            for (RelatedInstanceList relatedInstList : parameter.getRelatedInstanceList()) {
                RelatedInstance relatedInstance = relatedInstList.getRelatedInstance();
                if (relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf)) {
                    if (parameter.getIsReplace()) {
                        replaceVnfModelCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationId();
                    } else {
                        vnfModelCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationId();
                    }
                }
                if (relatedInstance.getModelInfo().getModelType().equals(ModelType.volumeGroup)) {
                    parameter.getLookupKeyMap().put(ResourceKey.VOLUME_GROUP_ID, relatedInstance.getInstanceId());
                }
            }
        }
        GenericVnf vnf = null;
        for (GenericVnf tempVnf : parameter.getServiceInstance().getVnfs()) {
            if (tempVnf.getVnfId().equalsIgnoreCase(parameter.getLookupKeyMap().get(ResourceKey.GENERIC_VNF_ID))) {
                vnf = tempVnf;
                vnfModelCustomizationUUID =
                        this.bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId()).getModelCustomizationId();
                ModelInfo vnfModelInfo = new ModelInfo();
                if (parameter.getIsReplace()) {
                    vnfModelInfo.setModelCustomizationUuid(replaceVnfModelCustomizationUUID);
                    this.mapCatalogVnf(tempVnf, vnfModelInfo, parameter.getServiceModel().getNewService());
                } else {
                    vnfModelInfo.setModelCustomizationUuid(vnfModelCustomizationUUID);
                    this.mapCatalogVnf(tempVnf, vnfModelInfo, parameter.getServiceModel().getCurrentService());
                }
                Optional<String> volumeGroupIdOp = getVolumeGroupIdRelatedToVfModule(tempVnf, parameter.getModelInfo(),
                        parameter.getCloudConfiguration().getCloudOwner(),
                        parameter.getCloudConfiguration().getLcpCloudRegionId(), parameter.getLookupKeyMap());
                if (volumeGroupIdOp.isPresent()) {
                    parameter.getLookupKeyMap().put(ResourceKey.VOLUME_GROUP_ID, volumeGroupIdOp.get());
                }
                break;
            }
        }
        if (vnf != null) {
            VfModule vfModule = null;
            for (VfModule vfModuleTemp : vnf.getVfModules()) {
                if (parameter.getLookupKeyMap().get(ResourceKey.VF_MODULE_ID) != null && vfModuleTemp.getVfModuleId()
                        .equalsIgnoreCase(parameter.getLookupKeyMap().get(ResourceKey.VF_MODULE_ID))) {
                    vfModule = vfModuleTemp;
                }
                String vfModuleCustId = bbInputSetupUtils.getAAIVfModule(vnf.getVnfId(), vfModuleTemp.getVfModuleId())
                        .getModelCustomizationId();
                ModelInfo modelInfoVfModule = new ModelInfo();
                modelInfoVfModule.setModelCustomizationId(vfModuleCustId);
                if (parameter.getIsReplace() && parameter.getLookupKeyMap().get(ResourceKey.VF_MODULE_ID) != null
                        && vfModuleTemp.getVfModuleId()
                                .equalsIgnoreCase(parameter.getLookupKeyMap().get(ResourceKey.VF_MODULE_ID))) {
                    mapCatalogVfModule(vfModuleTemp, modelInfoVfModule, parameter.getServiceModel().getNewService(),
                            replaceVnfModelCustomizationUUID);
                } else {
                    mapCatalogVfModule(vfModuleTemp, modelInfoVfModule, parameter.getServiceModel().getCurrentService(),
                            vnfModelCustomizationUUID);
                }
            }
            if (vfModule == null && parameter.getBbName().equalsIgnoreCase(AssignFlows.VF_MODULE.toString())) {
                vfModule = createVfModule(parameter.getLookupKeyMap(), parameter.getResourceId(),
                        parameter.getInstanceName(), parameter.getInstanceParams());
                vnf.getVfModules().add(vfModule);
                if (parameter.getIsReplace()) {
                    mapCatalogVfModule(vfModule, parameter.getModelInfo(), parameter.getServiceModel().getNewService(),
                            replaceVnfModelCustomizationUUID);
                } else {
                    mapCatalogVfModule(vfModule, parameter.getModelInfo(),
                            parameter.getServiceModel().getCurrentService(), vnfModelCustomizationUUID);
                }
            }
            if (vfModule != null && vfModule.getModelInfoVfModule() != null
                    && vfModule.getModelInfoVfModule().getModelName() != null
                    && vfModule.getModelInfoVfModule().getModelName().contains("helm")) {
                parameter.setIsHelm(true);
            }
        } else {
            logger.debug("Related VNF instance Id not found: {}",
                    parameter.getLookupKeyMap().get(ResourceKey.GENERIC_VNF_ID));
            throw new Exception("Could not find relevant information for related VNF");
        }
    }

    protected Optional<String> getVolumeGroupIdRelatedToVfModule(GenericVnf vnf, ModelInfo modelInfo, String cloudOwner,
            String cloudRegionId, Map<ResourceKey, String> lookupKeyMap) {
        if (lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID) == null) {
            for (VolumeGroup volumeGroup : vnf.getVolumeGroups()) {
                String volumeGroupCustId =
                        bbInputSetupUtils.getAAIVolumeGroup(cloudOwner, cloudRegionId, volumeGroup.getVolumeGroupId())
                                .getModelCustomizationId();
                if (modelInfo.getModelCustomizationId().equalsIgnoreCase(volumeGroupCustId)) {
                    logger.debug("Found volume group for vfModule: " + volumeGroup.getVolumeGroupId());
                    return Optional.of(volumeGroup.getVolumeGroupId());
                }
            }
        }
        return Optional.empty();
    }

    protected void mapCatalogVfModule(VfModule vfModule, ModelInfo modelInfo, Service service,
            String vnfModelCustomizationUUID) {
        if (modelInfo.getModelCustomizationUuid() != null) {
            modelInfo.setModelCustomizationId(modelInfo.getModelCustomizationUuid());
        }
        VnfResourceCustomization vnfResourceCustomization = null;
        for (VnfResourceCustomization resourceCust : service.getVnfCustomizations()) {
            if (resourceCust.getModelCustomizationUUID().equalsIgnoreCase(vnfModelCustomizationUUID)) {
                vnfResourceCustomization = resourceCust;
                break;
            }
        }
        VfModuleCustomization vfResourceCustomization = null;
        if (vnfResourceCustomization != null) {
            vfResourceCustomization = vnfResourceCustomization.getVfModuleCustomizations().stream() // Convert to steam
                    .filter(x -> modelInfo.getModelCustomizationId().equalsIgnoreCase(x.getModelCustomizationUUID()))// find
                    // what
                    // we
                    // want
                    .findAny() // If 'findAny' then return found
                    .orElse(null);
        }
        if (vfResourceCustomization == null) {
            vfResourceCustomization = bbInputSetupUtils
                    .getVfModuleCustomizationByModelCuztomizationUUID(modelInfo.getModelCustomizationId());
        }
        if (vfResourceCustomization != null) {
            vfModule.setModelInfoVfModule(this.mapperLayer.mapCatalogVfModuleToVfModule(vfResourceCustomization));
        }
    }

    protected VfModule createVfModule(Map<ResourceKey, String> lookupKeyMap, String vfModuleId, String instanceName,
            List<Map<String, String>> instanceParams) {
        lookupKeyMap.put(ResourceKey.VF_MODULE_ID, vfModuleId);
        VfModule vfModule = new VfModule();
        vfModule.setVfModuleId(vfModuleId);
        vfModule.setVfModuleName(instanceName);
        vfModule.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        if (instanceParams != null) {
            for (Map<String, String> params : instanceParams) {
                vfModule.getCloudParams().putAll(params);
            }
        }
        return vfModule;
    }

    protected void populateVolumeGroup(BBInputSetupParameter parameter) throws Exception {
        String replaceVnfModelCustomizationUUID = null;
        VolumeGroup volumeGroup = null;
        GenericVnf vnf = null;
        String vnfModelCustomizationUUID = null;
        String generatedVnfType = parameter.getVnfType();
        if (generatedVnfType == null || generatedVnfType.isEmpty()) {
            generatedVnfType =
                    parameter.getService().getModelName() + "/" + parameter.getModelInfo().getModelCustomizationName();
        }
        if (parameter.getRelatedInstanceList() != null) {
            for (RelatedInstanceList relatedInstList : parameter.getRelatedInstanceList()) {
                RelatedInstance relatedInstance = relatedInstList.getRelatedInstance();
                if (relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf)) {
                    if (parameter.getIsReplace()) {
                        replaceVnfModelCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationId();
                    } else {
                        vnfModelCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationId();
                    }
                    break;
                }
            }
        }
        for (GenericVnf tempVnf : parameter.getServiceInstance().getVnfs()) {
            if (tempVnf.getVnfId().equalsIgnoreCase(parameter.getLookupKeyMap().get(ResourceKey.GENERIC_VNF_ID))) {
                vnf = tempVnf;
                vnfModelCustomizationUUID =
                        bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId()).getModelCustomizationId();
                ModelInfo vnfModelInfo = new ModelInfo();
                if (parameter.getIsReplace()) {
                    vnfModelInfo.setModelCustomizationUuid(replaceVnfModelCustomizationUUID);
                    mapCatalogVnf(tempVnf, vnfModelInfo, parameter.getServiceModel().getNewService());
                } else {
                    vnfModelInfo.setModelCustomizationUuid(vnfModelCustomizationUUID);
                    mapCatalogVnf(tempVnf, vnfModelInfo, parameter.getServiceModel().getCurrentService());
                }
                break;
            }
        }
        if (vnf != null && vnfModelCustomizationUUID != null) {
            for (VolumeGroup volumeGroupTemp : vnf.getVolumeGroups()) {
                if (parameter.getLookupKeyMap().get(ResourceKey.VOLUME_GROUP_ID) != null
                        && volumeGroupTemp.getVolumeGroupId()
                                .equalsIgnoreCase(parameter.getLookupKeyMap().get(ResourceKey.VOLUME_GROUP_ID))) {
                    volumeGroup = volumeGroupTemp;
                    if (volumeGroup.getModelInfoVfModule() == null) {
                        throw new Exception(
                                "ModelInfoVfModule is null for VolumeGroup: " + volumeGroup.getVolumeGroupId());
                    }
                    String volumeGroupCustId = volumeGroup.getModelInfoVfModule().getModelCustomizationUUID();
                    ModelInfo modelInfoVolumeGroup = new ModelInfo();
                    modelInfoVolumeGroup.setModelCustomizationId(volumeGroupCustId);
                    if (parameter.getIsReplace() && parameter.getLookupKeyMap().get(ResourceKey.VOLUME_GROUP_ID) != null
                            && volumeGroupTemp.getVolumeGroupId()
                                    .equalsIgnoreCase(parameter.getLookupKeyMap().get(ResourceKey.VOLUME_GROUP_ID))) {
                        mapCatalogVolumeGroup(volumeGroupTemp, modelInfoVolumeGroup,
                                parameter.getServiceModel().getNewService(), replaceVnfModelCustomizationUUID);
                    } else {
                        mapCatalogVolumeGroup(volumeGroupTemp, modelInfoVolumeGroup,
                                parameter.getServiceModel().getCurrentService(), vnfModelCustomizationUUID);
                    }
                    break;
                }
            }
            if (volumeGroup == null && parameter.getBbName().equalsIgnoreCase(AssignFlows.VOLUME_GROUP.toString())) {
                volumeGroup = createVolumeGroup(parameter.getLookupKeyMap(), parameter.getResourceId(),
                        parameter.getInstanceName(), generatedVnfType, parameter.getInstanceParams());
                vnf.getVolumeGroups().add(volumeGroup);
                if (parameter.getIsReplace()) {
                    if (parameter.getExecuteBB().getOldVolumeGroupName() != null
                            && !parameter.getExecuteBB().getOldVolumeGroupName().isEmpty()) {
                        volumeGroup.setVolumeGroupName(parameter.getExecuteBB().getOldVolumeGroupName());
                    }
                    mapCatalogVolumeGroup(volumeGroup, parameter.getModelInfo(),
                            parameter.getServiceModel().getNewService(), replaceVnfModelCustomizationUUID);
                } else {
                    mapCatalogVolumeGroup(volumeGroup, parameter.getModelInfo(),
                            parameter.getServiceModel().getCurrentService(), vnfModelCustomizationUUID);
                }
            }
        } else {
            logger.debug("Related VNF instance Id not found: {}",
                    parameter.getLookupKeyMap().get(ResourceKey.GENERIC_VNF_ID));
            throw new Exception("Could not find relevant information for related VNF");
        }
    }

    protected VolumeGroup createVolumeGroup(Map<ResourceKey, String> lookupKeyMap, String volumeGroupId,
            String instanceName, String vnfType, List<Map<String, String>> instanceParams) {
        lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, volumeGroupId);
        VolumeGroup volumeGroup = new VolumeGroup();
        volumeGroup.setVolumeGroupId(volumeGroupId);
        volumeGroup.setVolumeGroupName(instanceName);
        volumeGroup.setVnfType(vnfType);
        volumeGroup.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        if (instanceParams != null) {
            for (Map<String, String> params : instanceParams) {
                volumeGroup.getCloudParams().putAll(params);
            }
        }
        return volumeGroup;
    }

    protected void mapCatalogVolumeGroup(VolumeGroup volumeGroup, ModelInfo modelInfo, Service service,
            String vnfModelCustomizationUUID) {
        VfModuleCustomization vfResourceCustomization =
                getVfResourceCustomization(modelInfo, service, vnfModelCustomizationUUID);
        if (vfResourceCustomization != null) {
            volumeGroup.setModelInfoVfModule(this.mapperLayer.mapCatalogVfModuleToVfModule(vfResourceCustomization));
        }
    }

    protected VfModuleCustomization getVfResourceCustomization(ModelInfo modelInfo, Service service,
            String vnfModelCustomizationUUID) {
        VnfResourceCustomization vnfResourceCustomization = null;
        for (VnfResourceCustomization resourceCust : service.getVnfCustomizations()) {
            if (resourceCust.getModelCustomizationUUID().equalsIgnoreCase(vnfModelCustomizationUUID)) {
                vnfResourceCustomization = resourceCust;
                break;
            }
        }
        if (vnfResourceCustomization != null) {
            for (VfModuleCustomization vfResourceCust : vnfResourceCustomization.getVfModuleCustomizations()) {
                if (vfResourceCust.getModelCustomizationUUID()
                        .equalsIgnoreCase(modelInfo.getModelCustomizationUuid())) {
                    return vfResourceCust;
                }
            }

        }
        return null;
    }

    protected void populateGenericVnf(BBInputSetupParameter parameter) {
        GenericVnf vnf = null;
        ModelInfo instanceGroupModelInfo = null;
        String instanceGroupId = null;
        String generatedVnfType = parameter.getVnfType();
        String replaceVnfModelCustomizationUUID = null;
        if (generatedVnfType == null || generatedVnfType.isEmpty()) {
            generatedVnfType =
                    parameter.getService().getModelName() + "/" + parameter.getModelInfo().getModelCustomizationName();
        }
        if (parameter.getRelatedInstanceList() != null) {
            for (RelatedInstanceList relatedInstList : parameter.getRelatedInstanceList()) {
                RelatedInstance relatedInstance = relatedInstList.getRelatedInstance();
                if (relatedInstance.getModelInfo().getModelType().equals(ModelType.networkInstanceGroup)) {
                    instanceGroupModelInfo = relatedInstance.getModelInfo();
                    instanceGroupId = relatedInstance.getInstanceId();
                }
                if (relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf) && parameter.getIsReplace()) {
                    replaceVnfModelCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationId();
                }
            }
        }
        for (GenericVnf vnfTemp : parameter.getServiceInstance().getVnfs()) {
            if (parameter.getLookupKeyMap().get(ResourceKey.GENERIC_VNF_ID) != null && vnfTemp.getVnfId()
                    .equalsIgnoreCase(parameter.getLookupKeyMap().get(ResourceKey.GENERIC_VNF_ID))) {
                String vnfModelCustId =
                        bbInputSetupUtils.getAAIGenericVnf(vnfTemp.getVnfId()).getModelCustomizationId();
                if (parameter.getIsReplace() && replaceVnfModelCustomizationUUID != null && vnfTemp.getVnfId()
                        .equalsIgnoreCase(parameter.getLookupKeyMap().get(ResourceKey.GENERIC_VNF_ID))) {
                    parameter.getModelInfo().setModelCustomizationUuid(replaceVnfModelCustomizationUUID);
                } else {
                    parameter.getModelInfo().setModelCustomizationUuid(vnfModelCustId);
                }
                vnf = vnfTemp;
                break;
            }
        }
        if ((vnf == null && parameter.getBbName().equalsIgnoreCase(AssignFlows.VNF.toString()))
                || (parameter.getRequestDetails() != null && this.isCnf(parameter.getRequestDetails()))) {
            vnf = createGenericVnf(parameter.getLookupKeyMap(), parameter.getInstanceName(), parameter.getPlatform(),
                    parameter.getLineOfBusiness(), parameter.getResourceId(), generatedVnfType,
                    parameter.getInstanceParams(), parameter.getProductFamilyId(), parameter.getApplicationId());
            parameter.getServiceInstance().getVnfs().add(vnf);
            mapVnfcCollectionInstanceGroup(vnf, parameter.getModelInfo(), parameter.getService());
        }
        if (vnf != null) {
            mapCatalogVnf(vnf, parameter.getModelInfo(), parameter.getService());
            if (instanceGroupId != null && instanceGroupModelInfo != null
                    && instanceGroupModelInfo.getModelType().equals(ModelType.networkInstanceGroup)
                    && !instanceGroupInList(vnf, instanceGroupId)) {
                mapNetworkCollectionInstanceGroup(vnf, instanceGroupId);
            }
        }
    }

    private boolean isCnf(final RequestDetails requestDetails) {
        logger.debug("Executing isCNF to check the model type is CNF");
        if (requestDetails.getModelInfo() != null) {
            return ModelType.cnf.equals(requestDetails.getModelInfo().getModelType());
        }
        logger.debug("Not a CNF model type:{}", requestDetails);
        return false;
    }

    protected boolean instanceGroupInList(GenericVnf vnf, String instanceGroupId) {
        for (InstanceGroup instanceGroup : vnf.getInstanceGroups()) {
            if (instanceGroup.getId() != null && instanceGroup.getId().equalsIgnoreCase(instanceGroupId)) {
                return true;
            }
        }
        return false;
    }

    protected void mapVnfcCollectionInstanceGroup(GenericVnf genericVnf, ModelInfo modelInfo, Service service) {
        VnfResourceCustomization vnfResourceCustomization = getVnfResourceCustomizationFromService(modelInfo, service);
        if (vnfResourceCustomization != null) {
            List<VnfcInstanceGroupCustomization> vnfcInstanceGroups =
                    vnfResourceCustomization.getVnfcInstanceGroupCustomizations();
            for (VnfcInstanceGroupCustomization vnfcInstanceGroupCust : vnfcInstanceGroups) {
                InstanceGroup instanceGroup = this.createInstanceGroup();
                org.onap.so.db.catalog.beans.InstanceGroup catalogInstanceGroup = bbInputSetupUtils
                        .getCatalogInstanceGroup(vnfcInstanceGroupCust.getInstanceGroup().getModelUUID());
                instanceGroup.setModelInfoInstanceGroup(
                        this.mapperLayer.mapCatalogInstanceGroupToInstanceGroup(null, catalogInstanceGroup));
                instanceGroup.getModelInfoInstanceGroup().setFunction(vnfcInstanceGroupCust.getFunction());
                instanceGroup.getModelInfoInstanceGroup().setDescription(vnfcInstanceGroupCust.getDescription());
                genericVnf.getInstanceGroups().add(instanceGroup);
            }
        }
    }

    protected void mapNetworkCollectionInstanceGroup(GenericVnf genericVnf, String instanceGroupId) {
        org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup =
                bbInputSetupUtils.getAAIInstanceGroup(instanceGroupId);
        InstanceGroup instanceGroup = this.mapperLayer.mapAAIInstanceGroupIntoInstanceGroup(aaiInstanceGroup);
        instanceGroup.setModelInfoInstanceGroup(this.mapperLayer.mapCatalogInstanceGroupToInstanceGroup(null,
                this.bbInputSetupUtils.getCatalogInstanceGroup(aaiInstanceGroup.getModelVersionId())));
        genericVnf.getInstanceGroups().add(instanceGroup);
    }

    protected GenericVnf createGenericVnf(Map<ResourceKey, String> lookupKeyMap, String instanceName,
            org.onap.so.serviceinstancebeans.Platform platform,
            org.onap.so.serviceinstancebeans.LineOfBusiness lineOfBusiness, String vnfId, String vnfType,
            List<Map<String, String>> instanceParams, String productFamilyId, String applicationId) {
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, vnfId);
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId(vnfId);
        genericVnf.setVnfName(instanceName);
        genericVnf.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        genericVnf.setVnfType(vnfType);
        genericVnf.setProvStatus(PREPROV);
        genericVnf.setServiceId(productFamilyId);
        genericVnf.setApplicationId(applicationId);
        if (platform != null) {
            genericVnf.setPlatform(this.mapperLayer.mapRequestPlatform(platform));
        }
        if (lineOfBusiness != null) {
            genericVnf.setLineOfBusiness(this.mapperLayer.mapRequestLineOfBusiness(lineOfBusiness));
        }
        if (instanceParams != null) {
            for (Map<String, String> params : instanceParams) {
                genericVnf.getCloudParams().putAll(params);
            }
        }
        return genericVnf;
    }

    protected void mapCatalogVnf(GenericVnf genericVnf, ModelInfo modelInfo, Service service) {
        VnfResourceCustomization vnfResourceCustomization = getVnfResourceCustomizationFromService(modelInfo, service);
        if (vnfResourceCustomization != null) {
            genericVnf.setModelInfoGenericVnf(this.mapperLayer.mapCatalogVnfToVnf(vnfResourceCustomization));
        }
    }

    protected void mapCatalogPnf(Pnf pnf, ModelInfo modelInfo, Service service) {
        PnfResourceCustomization pnfResourceCustomization = getPnfResourceCustomizationFromService(modelInfo, service);
        if (pnfResourceCustomization != null) {
            pnf.setModelInfoPnf(this.mapperLayer.mapCatalogPnfToPnf(pnfResourceCustomization));
        }
    }

    protected VnfResourceCustomization getVnfResourceCustomizationFromService(ModelInfo modelInfo, Service service) {
        VnfResourceCustomization vnfResourceCustomization = null;
        for (VnfResourceCustomization resourceCust : service.getVnfCustomizations()) {
            if (resourceCust.getModelCustomizationUUID().equalsIgnoreCase(modelInfo.getModelCustomizationUuid())) {
                vnfResourceCustomization = resourceCust;
                break;
            }
        }
        return vnfResourceCustomization;
    }

    protected PnfResourceCustomization getPnfResourceCustomizationFromService(ModelInfo modelInfo, Service service) {
        PnfResourceCustomization pnfResourceCustomization = null;
        for (PnfResourceCustomization resourceCust : service.getPnfCustomizations()) {
            if (resourceCust.getModelCustomizationUUID().equalsIgnoreCase(modelInfo.getModelCustomizationUuid())) {
                pnfResourceCustomization = resourceCust;
                break;
            }
        }
        return pnfResourceCustomization;
    }

    protected void populateL3Network(BBInputSetupParameter parameter) {
        L3Network network = null;
        for (L3Network networkTemp : parameter.getServiceInstance().getNetworks()) {
            if (parameter.getLookupKeyMap().get(ResourceKey.NETWORK_ID) != null && networkTemp.getNetworkId()
                    .equalsIgnoreCase(parameter.getLookupKeyMap().get(ResourceKey.NETWORK_ID))) {
                network = networkTemp;
                break;
            }
        }
        if (network == null && (parameter.getBbName().equalsIgnoreCase(AssignFlows.NETWORK_A_LA_CARTE.toString())
                || parameter.getBbName().equalsIgnoreCase(AssignFlows.NETWORK_MACRO.toString()))) {
            network = createNetwork(parameter.getLookupKeyMap(), parameter.getInstanceName(), parameter.getResourceId(),
                    parameter.getInstanceParams(), parameter);
            parameter.getServiceInstance().getNetworks().add(network);
        }
        if (network != null) {
            mapCatalogNetwork(network, parameter.getModelInfo(), parameter.getService());
        }
    }

    protected L3Network createNetwork(Map<ResourceKey, String> lookupKeyMap, String instanceName, String networkId,
            List<Map<String, String>> instanceParams, BBInputSetupParameter parameter) {
        lookupKeyMap.put(ResourceKey.NETWORK_ID, networkId);
        L3Network network = new L3Network();
        network.setNetworkId(networkId);
        network.setNetworkName(instanceName);
        network.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        if (parameter != null) {
            if (parameter.getLineOfBusiness() != null) {
                network.setLineOfBusiness(this.mapperLayer.mapRequestLineOfBusiness(parameter.getLineOfBusiness()));
            }
            if (parameter.getLineOfBusiness() != null) {
                network.setPlatform(this.mapperLayer.mapRequestPlatform(parameter.getPlatform()));
            }
        }
        if (instanceParams != null) {
            for (Map<String, String> params : instanceParams) {
                network.getCloudParams().putAll(params);
            }
        }
        return network;
    }

    protected void mapCatalogNetwork(L3Network network, ModelInfo modelInfo, Service service) {
        NetworkResourceCustomization networkResourceCustomization = null;
        for (NetworkResourceCustomization resourceCust : service.getNetworkCustomizations()) {
            if (resourceCust.getModelCustomizationUUID().equalsIgnoreCase(modelInfo.getModelCustomizationUuid())) {
                networkResourceCustomization = resourceCust;
                break;
            }
        }
        if (networkResourceCustomization != null) {
            network.setModelInfoNetwork(this.mapperLayer.mapCatalogNetworkToNetwork(networkResourceCustomization));
        }
    }

    protected GeneralBuildingBlock getGBBALaCarteService(ExecuteBuildingBlock executeBB, RequestDetails requestDetails,
            Map<ResourceKey, String> lookupKeyMap, String requestAction, String resourceId) throws Exception {
        Customer customer = getCustomerAndServiceSubscription(requestDetails, resourceId);
        if (customer != null) {
            Project project = null;
            OwningEntity owningEntity = null;

            if (requestDetails.getProject() != null)
                project = mapperLayer.mapRequestProject(requestDetails.getProject());
            if (requestDetails.getOwningEntity() != null)
                owningEntity = mapperLayer.mapRequestOwningEntity(requestDetails.getOwningEntity());

            String modelVersionId = requestDetails.getModelInfo().getModelVersionId();

            if (ModelType.vnf == requestDetails.getModelInfo().getModelType()) {
                for (RelatedInstanceList relatedInstanceList : requestDetails.getRelatedInstanceList()) {
                    if (ModelType.service == relatedInstanceList.getRelatedInstance().getModelInfo().getModelType()) {
                        modelVersionId = relatedInstanceList.getRelatedInstance().getModelInfo().getModelVersionId();
                        break;
                    }
                }
            }
            if (ModelType.pnf == requestDetails.getModelInfo().getModelType()) {
                for (RelatedInstanceList relatedInstanceList : requestDetails.getRelatedInstanceList()) {
                    if (ModelType.service == relatedInstanceList.getRelatedInstance().getModelInfo().getModelType()) {
                        modelVersionId = relatedInstanceList.getRelatedInstance().getModelInfo().getModelVersionId();
                        break;
                    }
                }
            }

            Service service = bbInputSetupUtils.getCatalogServiceByModelUUID(modelVersionId);
            if (service == null) {
                service = bbInputSetupUtils.getCatalogServiceByModelVersionAndModelInvariantUUID(
                        requestDetails.getModelInfo().getModelVersion(),
                        requestDetails.getModelInfo().getModelInvariantId());
                if (service == null) {
                    throw new Exception("Could not find service for model version Id: "
                            + requestDetails.getModelInfo().getModelVersionId() + " and for model invariant Id: "
                            + requestDetails.getModelInfo().getModelInvariantId());
                }
            }
            ServiceInstance serviceInstance = this.getALaCarteServiceInstance(service, requestDetails, customer,
                    project, owningEntity, lookupKeyMap, resourceId, Boolean.TRUE.equals(executeBB.isaLaCarte()),
                    executeBB.getBuildingBlock().getBpmnFlowName());
            BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setRequestDetails(requestDetails)
                    .setServiceInstance(serviceInstance).setExecuteBB(executeBB).setRequestAction(requestAction)
                    .setCustomer(customer).build();
            return this.populateGBBWithSIAndAdditionalInfo(parameter);
        } else {
            throw new Exception("Could not find customer");
        }
    }

    protected Customer getCustomerAndServiceSubscription(RequestDetails requestDetails, String resourceId) {
        Customer customer;
        if (requestDetails.getSubscriberInfo() != null) {
            customer = this.getCustomerFromRequest(requestDetails);
        } else {
            customer = this.getCustomerFromURI(resourceId);
        }
        if (customer != null) {
            ServiceSubscription serviceSubscription = null;
            serviceSubscription = getServiceSubscription(requestDetails, customer);
            if (serviceSubscription == null) {
                serviceSubscription = getServiceSubscriptionFromURI(resourceId, customer);
            }
            customer.setServiceSubscription(serviceSubscription);
            return customer;
        } else {
            return null;
        }
    }

    protected ServiceSubscription getServiceSubscriptionFromURI(String resourceId, Customer customer) {
        Map<String, String> uriKeys = bbInputSetupUtils.getURIKeysFromServiceInstance(resourceId);
        String subscriptionServiceType =
                uriKeys.get(AAIFluentTypeBuilder.Types.SERVICE_SUBSCRIPTION.getUriParams().serviceType);
        org.onap.aai.domain.yang.ServiceSubscription serviceSubscriptionAAI =
                bbInputSetupUtils.getAAIServiceSubscription(customer.getGlobalCustomerId(), subscriptionServiceType);
        if (serviceSubscriptionAAI != null) {
            return mapperLayer.mapAAIServiceSubscription(serviceSubscriptionAAI);
        } else {
            return null;
        }
    }

    protected Customer getCustomerFromURI(String resourceId) {
        Map<String, String> uriKeys = bbInputSetupUtils.getURIKeysFromServiceInstance(resourceId);
        String globalCustomerId = uriKeys.get(AAIFluentTypeBuilder.Types.CUSTOMER.getUriParams().globalCustomerId);
        org.onap.aai.domain.yang.Customer customerAAI = this.bbInputSetupUtils.getAAICustomer(globalCustomerId);
        if (customerAAI != null) {
            return mapperLayer.mapAAICustomer(customerAAI);
        } else {
            return null;
        }
    }

    protected GeneralBuildingBlock populateGBBWithSIAndAdditionalInfo(BBInputSetupParameter parameter)
            throws Exception {
        GeneralBuildingBlock outputBB = new GeneralBuildingBlock();
        OrchestrationContext orchContext = mapperLayer.mapOrchestrationContext(parameter.getRequestDetails());
        RequestContext requestContext = mapperLayer.mapRequestContext(parameter.getRequestDetails());
        requestContext.setAction(parameter.getRequestAction());
        requestContext.setMsoRequestId(parameter.getExecuteBB().getRequestId());
        requestContext.setIsHelm(parameter.getIsHelm());
        org.onap.aai.domain.yang.CloudRegion aaiCloudRegion =
                bbInputSetupUtils.getCloudRegion(parameter.getRequestDetails().getCloudConfiguration());
        CloudRegion cloudRegion =
                mapperLayer.mapCloudRegion(parameter.getRequestDetails().getCloudConfiguration(), aaiCloudRegion);
        Tenant tenant = getTenant(parameter.getRequestDetails().getCloudConfiguration(), aaiCloudRegion);
        outputBB.setOrchContext(orchContext);
        outputBB.setRequestContext(requestContext);
        outputBB.setCloudRegion(cloudRegion);
        outputBB.setTenant(tenant);
        Customer customer = parameter.getCustomer();
        if (customer == null) {
            Map<String, String> uriKeys = bbInputSetupUtils
                    .getURIKeysFromServiceInstance(parameter.getServiceInstance().getServiceInstanceId());
            String globalCustomerId = uriKeys.get(AAIFluentTypeBuilder.Types.CUSTOMER.getUriParams().globalCustomerId);
            String subscriptionServiceType =
                    uriKeys.get(AAIFluentTypeBuilder.Types.SERVICE_SUBSCRIPTION.getUriParams().serviceType);
            customer = mapCustomer(globalCustomerId, subscriptionServiceType);
        }
        outputBB.setServiceInstance(parameter.getServiceInstance());
        if (customer.getServiceSubscription() != null) {
            customer.getServiceSubscription().getServiceInstances().add(parameter.getServiceInstance());
        }
        outputBB.setCustomer(customer);
        return outputBB;
    }

    protected Tenant getTenant(CloudConfiguration cloudConfiguration,
            org.onap.aai.domain.yang.CloudRegion aaiCloudRegion) throws Exception {
        Tenant tenant = new Tenant();
        if (cloudConfiguration != null && cloudConfiguration.getTenantId() != null && aaiCloudRegion != null
                && aaiCloudRegion.getTenants() != null) {
            for (org.onap.aai.domain.yang.Tenant aaiTenant : aaiCloudRegion.getTenants().getTenant()) {
                if (aaiTenant.getTenantId().equalsIgnoreCase(cloudConfiguration.getTenantId())) {
                    tenant = mapperLayer.mapTenant(aaiTenant);
                }
            }
            if (tenant.getTenantId() == null || tenant.getTenantName() == null) {
                throw new Exception("Invalid tenant information retrieved: tenantId = " + tenant.getTenantId()
                        + " tenantName = " + tenant.getTenantName());
            }
        }
        return tenant;
    }

    protected ServiceSubscription getServiceSubscription(RequestDetails requestDetails, Customer customer) {
        org.onap.aai.domain.yang.ServiceSubscription aaiServiceSubscription =
                bbInputSetupUtils.getAAIServiceSubscription(customer.getGlobalCustomerId(),
                        requestDetails.getRequestParameters().getSubscriptionServiceType());
        if (aaiServiceSubscription != null) {
            return mapperLayer.mapAAIServiceSubscription(aaiServiceSubscription);
        } else {
            return null;
        }
    }

    protected Customer getCustomerFromRequest(RequestDetails requestDetails) {
        org.onap.aai.domain.yang.Customer aaiCustomer =
                bbInputSetupUtils.getAAICustomer(requestDetails.getSubscriberInfo().getGlobalSubscriberId());
        if (aaiCustomer != null) {
            return mapperLayer.mapAAICustomer(aaiCustomer);
        } else {
            return null;
        }
    }

    protected ServiceInstance getALaCarteServiceInstance(Service service, RequestDetails requestDetails,
            Customer customer, Project project, OwningEntity owningEntity, Map<ResourceKey, String> lookupKeyMap,
            String serviceInstanceId, boolean aLaCarte, String bbName) throws Exception {
        ServiceInstance serviceInstance = this.getServiceInstanceHelper(requestDetails, customer, project, owningEntity,
                lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = null;
        if (customer != null && customer.getServiceSubscription() != null) {
            serviceInstanceAAI = bbInputSetupUtils.getAAIServiceInstanceByIdAndCustomer(customer.getGlobalCustomerId(),
                    customer.getServiceSubscription().getServiceType(), serviceInstanceId);
        } else {
            serviceInstanceAAI = bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId);
        }
        if (serviceInstanceAAI != null
                && !serviceInstanceAAI.getModelVersionId().equalsIgnoreCase(service.getModelUUID())) {
            Service tempService =
                    this.bbInputSetupUtils.getCatalogServiceByModelUUID(serviceInstanceAAI.getModelVersionId());
            if (tempService != null) {
                serviceInstance
                        .setModelInfoServiceInstance(mapperLayer.mapCatalogServiceIntoServiceInstance(tempService));
                return serviceInstance;
            } else {
                throw new Exception(
                        "Could not find model of existing SI. Service Instance in AAI already exists with different model version id: "
                                + serviceInstanceAAI.getModelVersionId());
            }
        }
        serviceInstance.setModelInfoServiceInstance(mapperLayer.mapCatalogServiceIntoServiceInstance(service));
        return serviceInstance;
    }

    protected GeneralBuildingBlock getGBBMacro(ExecuteBuildingBlock executeBB, RequestDetails requestDetails,
            Map<ResourceKey, String> lookupKeyMap, String requestAction, String resourceId, String vnfType)
            throws Exception {
        String bbName = executeBB.getBuildingBlock().getBpmnFlowName();
        String key = executeBB.getBuildingBlock().getKey();

        if (requestAction.equalsIgnoreCase("deleteInstance") || requestAction.equalsIgnoreCase("unassignInstance")
                || requestAction.equalsIgnoreCase("activateInstance")
                || requestAction.equalsIgnoreCase("activateFabricConfiguration")
                || requestAction.equalsIgnoreCase("recreateInstance")
                || requestAction.equalsIgnoreCase("replaceInstance")
                || requestAction.equalsIgnoreCase("upgradeInstance") || requestAction.equalsIgnoreCase("healthCheck")
                || requestAction.equalsIgnoreCase("upgradeCnf")) {
            return getGBBMacroExistingService(executeBB, lookupKeyMap, bbName, requestAction,
                    requestDetails.getCloudConfiguration());
        }

        String serviceInstanceId = lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID);
        GeneralBuildingBlock gBB =
                this.getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap, requestAction, serviceInstanceId);
        RequestParameters requestParams = requestDetails.getRequestParameters();
        Service service = null;
        if (gBB != null && gBB.getServiceInstance() != null
                && gBB.getServiceInstance().getModelInfoServiceInstance() != null
                && gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid() != null) {
            service = bbInputSetupUtils.getCatalogServiceByModelUUID(
                    gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());
        } else {
            throw new Exception("Could not get service instance for macro request");
        }
        if (requestParams != null && requestParams.getUserParams() != null) {
            for (Map<String, Object> userParams : requestParams.getUserParams()) {
                if (userParams.containsKey("service")) {
                    String input = mapper.writeValueAsString(userParams.get("service"));
                    return getGBBMacroUserParams(executeBB, requestDetails, lookupKeyMap, vnfType, bbName, key, gBB,
                            requestParams, service, input);
                }
            }
        }
        if (requestAction.equalsIgnoreCase("deactivateInstance")) {
            return gBB;
        } else if (requestAction.equalsIgnoreCase("createInstance")) {
            return getGBBMacroNoUserParamsCreate(executeBB, lookupKeyMap, bbName, key, gBB, service);
        } else {
            throw new IllegalArgumentException(
                    "No user params on requestAction: assignInstance. Please specify user params.");
        }
    }

    protected GeneralBuildingBlock getGBBMacroNoUserParamsCreate(ExecuteBuildingBlock executeBB,
            Map<ResourceKey, String> lookupKeyMap, String bbName, String key, GeneralBuildingBlock gBB, Service service)
            throws Exception {
        ServiceInstance serviceInstance = gBB.getServiceInstance();
        BBInputSetupParameter parameter = new BBInputSetupParameter.Builder().setExecuteBB(executeBB)
                .setRequestId(executeBB.getRequestId()).setServiceInstance(serviceInstance).setService(service)
                .setBbName(bbName).setLookupKeyMap(lookupKeyMap).setKey(key).build();
        if (bbName.contains(NETWORK) && !bbName.contains(NETWORK_COLLECTION)) {
            String networkId = lookupKeyMap.get(ResourceKey.NETWORK_ID);
            parameter.setResourceId(networkId);
            parameter.setModelInfo(new ModelInfo());

            if ((!Boolean.TRUE.equals(executeBB.getBuildingBlock().isVirtualLink()))) {
                NetworkResourceCustomization networkCust = getNetworkCustomizationByKey(key, service);
                if (networkCust != null) {
                    parameter.getModelInfo().setModelCustomizationUuid(networkCust.getModelCustomizationUUID());
                    this.populateL3Network(parameter);
                } else {
                    logger.debug("Could not find a network customization with key: {}", key);
                }
            } else {
                logger.debug("Orchestrating on Collection Network Resource Customization");
                CollectionNetworkResourceCustomization collectionNetworkResourceCust =
                        bbInputSetupUtils.getCatalogCollectionNetworkResourceCustByID(key);
                L3Network l3Network = getVirtualLinkL3Network(lookupKeyMap, bbName, key, networkId,
                        collectionNetworkResourceCust, serviceInstance);
                NetworkResourceCustomization networkResourceCustomization = mapperLayer
                        .mapCollectionNetworkResourceCustToNetworkResourceCust(collectionNetworkResourceCust);
                if (l3Network != null) {
                    l3Network.setModelInfoNetwork(mapperLayer.mapCatalogNetworkToNetwork(networkResourceCustomization));
                }
            }
        } else if (bbName.contains("Configuration")) {
            parameter.setResourceId(lookupKeyMap.get(ResourceKey.CONFIGURATION_ID));
            parameter.setModelInfo(new ModelInfo());
            parameter.getModelInfo().setModelCustomizationUuid(key);
            parameter.setConfigurationResourceKeys(executeBB.getConfigurationResourceKeys());
            parameter.setRequestDetails(executeBB.getRequestDetails());
            this.populateConfiguration(parameter);
        }
        if (executeBB.getWorkflowResourceIds() != null) {
            parameter.setResourceId(executeBB.getWorkflowResourceIds().getNetworkCollectionId());
            this.populateNetworkCollectionAndInstanceGroupAssign(parameter);
        }
        RelatedInstance relatedVpnBinding =
                bbInputSetupUtils.getRelatedInstanceByType(executeBB.getRequestDetails(), ModelType.vpnBinding);
        RelatedInstance relatedLocalNetwork =
                bbInputSetupUtils.getRelatedInstanceByType(executeBB.getRequestDetails(), ModelType.network);
        if (relatedVpnBinding != null && relatedLocalNetwork != null) {
            org.onap.aai.domain.yang.VpnBinding aaiVpnBinding =
                    bbInputSetupUtils.getAAIVpnBinding(relatedVpnBinding.getInstanceId());
            org.onap.aai.domain.yang.L3Network aaiLocalNetwork =
                    bbInputSetupUtils.getAAIL3Network(relatedLocalNetwork.getInstanceId());
            VpnBinding vpnBinding = mapperLayer.mapAAIVpnBinding(aaiVpnBinding);
            L3Network localNetwork = mapperLayer.mapAAIL3Network(aaiLocalNetwork);
            Optional<org.onap.aai.domain.yang.VpnBinding> aaiAICVpnBindingOp =
                    bbInputSetupUtils.getAICVpnBindingFromNetwork(aaiLocalNetwork);
            if (aaiAICVpnBindingOp.isPresent()) {
                localNetwork.getVpnBindings().add(mapperLayer.mapAAIVpnBinding(aaiAICVpnBindingOp.get()));
            }
            ServiceProxy serviceProxy = getServiceProxy(service);
            gBB.getServiceInstance().getServiceProxies().add(serviceProxy);
            gBB.getCustomer().getVpnBindings().add(vpnBinding);
            lookupKeyMap.put(ResourceKey.VPN_ID, vpnBinding.getVpnId());
            gBB.getServiceInstance().getNetworks().add(localNetwork);
            lookupKeyMap.put(ResourceKey.NETWORK_ID, localNetwork.getNetworkId());
        }
        return gBB;
    }

    protected ServiceProxy getServiceProxy(Service service) {
        if (!service.getServiceProxyCustomizations().isEmpty()) {
            ServiceProxyResourceCustomization serviceProxyCatalog = getServiceProxyResourceCustomization(service);
            ServiceProxy serviceProxy = new ServiceProxy();
            serviceProxy.setModelInfoServiceProxy(
                    mapperLayer.mapServiceProxyCustomizationToServiceProxy(serviceProxyCatalog));
            Service sourceService = serviceProxyCatalog.getSourceService();
            ServiceInstance sourceServiceShell = new ServiceInstance();
            sourceServiceShell
                    .setModelInfoServiceInstance(mapperLayer.mapCatalogServiceIntoServiceInstance(sourceService));
            serviceProxy.setServiceInstance(sourceServiceShell);
            serviceProxy.setType(sourceService.getServiceType());
            return serviceProxy;
        } else {
            return null;
        }
    }

    protected ServiceProxyResourceCustomization getServiceProxyResourceCustomization(Service service) {
        ServiceProxyResourceCustomization serviceProxyCatalog = null;
        for (ServiceProxyResourceCustomization serviceProxyTemp : service.getServiceProxyCustomizations()) {
            if (serviceProxyTemp.getSourceService() != null
                    && serviceProxyTemp.getSourceService().getServiceType().equalsIgnoreCase("TRANSPORT")) {
                serviceProxyCatalog = serviceProxyTemp;
            }
        }
        return serviceProxyCatalog;
    }

    protected L3Network getVirtualLinkL3Network(Map<ResourceKey, String> lookupKeyMap, String bbName, String key,
            String networkId, CollectionNetworkResourceCustomization collectionNetworkResourceCust,
            ServiceInstance serviceInstance) {
        if (collectionNetworkResourceCust != null) {
            if ((bbName.equalsIgnoreCase(AssignFlows.NETWORK_A_LA_CARTE.toString())
                    || bbName.equalsIgnoreCase(AssignFlows.NETWORK_MACRO.toString()))) {
                L3Network network = createNetwork(lookupKeyMap, null, networkId, null, null);
                serviceInstance.getNetworks().add(network);
                return network;
            } else {
                for (L3Network network : serviceInstance.getNetworks()) {
                    if (network.getNetworkId().equalsIgnoreCase(networkId)) {
                        return network;
                    }
                }
            }
        }
        return null;
    }

    protected NetworkResourceCustomization getNetworkCustomizationByKey(String key, Service service) {
        for (NetworkResourceCustomization networkCust : service.getNetworkCustomizations()) {
            if (networkCust.getModelCustomizationUUID().equalsIgnoreCase(key)) {
                return networkCust;
            }
        }
        return null;
    }

    protected GeneralBuildingBlock getGBBMacroExistingService(ExecuteBuildingBlock executeBB,
            Map<ResourceKey, String> lookupKeyMap, String bbName, String requestAction,
            CloudConfiguration cloudConfiguration) throws Exception {
        org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = null;
        String serviceInstanceId = lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID);
        RequestDetails requestDetails = executeBB.getRequestDetails();
        BBInputSetupParameter parameter =
                new BBInputSetupParameter.Builder().setExecuteBB(executeBB).setLookupKeyMap(lookupKeyMap)
                        .setBbName(bbName).setRequestAction(requestAction).setCloudConfiguration(cloudConfiguration)
                        .setRequestDetails(requestDetails).setResourceId(serviceInstanceId).build();
        GeneralBuildingBlock gBB = null;
        Service service = null;
        if (serviceInstanceId != null) {
            aaiServiceInstance = bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId);
            if (aaiServiceInstance != null) {
                String modelVersionId = aaiServiceInstance.getModelVersionId();
                if ("upgradeInstance".equalsIgnoreCase(requestAction)) {
                    modelVersionId = requestDetails.getModelInfo().getModelVersionId();
                }

                service = bbInputSetupUtils.getCatalogServiceByModelUUID(modelVersionId);

                if (service == null) {
                    String message = String.format(
                            "Related service instance model not found in MSO CatalogDB: model-version-id=%s",
                            aaiServiceInstance.getModelVersionId());
                    throw new ServiceModelNotFoundException(message);
                }
            } else {
                String message = String.format("Related service instance from AAI not found: service-instance-id=%s",
                        serviceInstanceId);
                throw new NoServiceInstanceFoundException(message);
            }
        }
        ServiceInstance serviceInstance = this.getExistingServiceInstance(aaiServiceInstance);
        serviceInstance.setModelInfoServiceInstance(this.mapperLayer.mapCatalogServiceIntoServiceInstance(service));
        parameter.setServiceInstance(serviceInstance);
        gBB = populateGBBWithSIAndAdditionalInfo(parameter);

        serviceInstance = gBB.getServiceInstance();
        CloudRegion cloudRegion = null;
        if (cloudConfiguration == null) {
            Optional<CloudRegion> cloudRegionOp = cloudInfoFromAAI.getCloudInfoFromAAI(serviceInstance);
            if (cloudRegionOp.isPresent()) {
                cloudRegion = cloudRegionOp.get();
            }
        }
        if (cloudConfiguration != null) {
            org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = bbInputSetupUtils.getCloudRegion(cloudConfiguration);
            cloudRegion = mapperLayer.mapCloudRegion(cloudConfiguration, aaiCloudRegion);
        }
        gBB.setCloudRegion(cloudRegion);

        /*
         * Below check is for CNF-Upgrade only. Reads new version VNF/VF-Module details from UserParams and delegates to
         * BBs. Reads data from RequestDetails.
         */
        String upgradeCnfModelCustomizationUUID = "";
        String upgradeCnfVfModuleModelCustomizationUUID = "";
        String upgradeCnfModelVersionId = "";
        String upgradeCnfVfModuleModelVersionId = "";
        if (requestDetails.getRelatedInstanceList() != null && requestAction.equalsIgnoreCase("upgradeCnf")) {
            if (requestDetails.getRequestParameters().getUserParams() != null) {
                List<RequestParameters> requestParams = new ArrayList<>();
                requestParams.add(requestDetails.getRequestParameters());
                for (RequestParameters reqParam : requestParams) {
                    for (Map<String, Object> params : reqParam.getUserParams()) {
                        if (params.containsKey("service")) {
                            org.onap.so.serviceinstancebeans.Service services = serviceMapper(params);
                            List<Vnfs> vnfs = services.getResources().getVnfs();
                            for (Vnfs vnfobj : vnfs) {
                                for (VfModules vfMod : vnfobj.getVfModules()) {
                                    upgradeCnfModelCustomizationUUID = vnfobj.getModelInfo().getModelCustomizationId();
                                    upgradeCnfModelVersionId = vnfobj.getModelInfo().getModelVersionId();
                                    upgradeCnfVfModuleModelCustomizationUUID =
                                            vfMod.getModelInfo().getModelCustomizationId();
                                    upgradeCnfVfModuleModelVersionId = vfMod.getModelInfo().getModelVersionId();
                                }
                            }
                        }
                    }
                }
            }
        }
        if (bbName.contains(VNF) || (bbName.contains(CONTROLLER)
                && (VNF).equalsIgnoreCase(executeBB.getBuildingBlock().getBpmnScope()))) {
            for (GenericVnf genericVnf : serviceInstance.getVnfs()) {
                if (lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID) != null
                        && genericVnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
                    org.onap.aai.domain.yang.GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(genericVnf.getVnfId());
                    ModelInfo modelInfo = new ModelInfo();
                    if ("upgradeCnf".equalsIgnoreCase(requestAction) && upgradeCnfModelCustomizationUUID != null
                            && !(bbName.contains("Deactivate"))) {
                        modelInfo.setModelCustomizationUuid(upgradeCnfModelCustomizationUUID);
                        modelInfo.setModelVersionId(upgradeCnfModelVersionId);
                        this.mapCatalogVnf(genericVnf, modelInfo, service);
                    } else if (vnf != null) {
                        modelInfo.setModelCustomizationUuid(vnf.getModelCustomizationId());
                        this.mapCatalogVnf(genericVnf, modelInfo, service);
                    }

                }
            }

        } else if (bbName.contains(VF_MODULE) || (bbName.contains(CONTROLLER)
                && (VF_MODULE).equalsIgnoreCase(executeBB.getBuildingBlock().getBpmnScope()))) {
            for (GenericVnf vnf : serviceInstance.getVnfs()) {
                for (VfModule vfModule : vnf.getVfModules()) {
                    if (lookupKeyMap.get(ResourceKey.VF_MODULE_ID) != null
                            && vfModule.getVfModuleId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.VF_MODULE_ID))) {
                        String vnfModelCustomizationUUID =
                                this.bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId()).getModelCustomizationId();
                        ModelInfo vnfModelInfo = new ModelInfo();
                        if ("upgradeCnf".equalsIgnoreCase(requestAction) && upgradeCnfModelCustomizationUUID != null
                                && !(bbName.contains("Deactivate"))) {
                            vnfModelInfo.setModelCustomizationUuid(upgradeCnfModelCustomizationUUID);
                            vnfModelInfo.setModelVersionId(upgradeCnfModelVersionId);
                            this.mapCatalogVnf(vnf, vnfModelInfo, service);
                        } else {
                            vnfModelInfo.setModelCustomizationUuid(vnfModelCustomizationUUID);
                            this.mapCatalogVnf(vnf, vnfModelInfo, service);
                        }
                        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, vnf.getVnfId());
                        String vfModuleCustomizationUUID = this.bbInputSetupUtils
                                .getAAIVfModule(vnf.getVnfId(), vfModule.getVfModuleId()).getModelCustomizationId();
                        ModelInfo vfModuleModelInfo = new ModelInfo();
                        if ("upgradeCnf".equalsIgnoreCase(requestAction) && upgradeCnfModelCustomizationUUID != null
                                && !(bbName.contains("Deactivate"))) {
                            vfModuleModelInfo.setModelCustomizationUuid(upgradeCnfVfModuleModelCustomizationUUID);
                            vfModuleModelInfo.setModelVersionId(upgradeCnfVfModuleModelVersionId);
                            this.mapCatalogVfModule(vfModule, vfModuleModelInfo, service,
                                    upgradeCnfModelCustomizationUUID);
                        } else {
                            vfModuleModelInfo.setModelCustomizationId(vfModuleCustomizationUUID);
                            this.mapCatalogVfModule(vfModule, vfModuleModelInfo, service, vnfModelCustomizationUUID);
                        }
                        if (cloudRegion != null) {
                            Optional<String> volumeGroupIdOp = getVolumeGroupIdRelatedToVfModule(vnf, vfModuleModelInfo,
                                    cloudRegion.getCloudOwner(), cloudRegion.getLcpCloudRegionId(), lookupKeyMap);
                            if (volumeGroupIdOp.isPresent()) {
                                lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, volumeGroupIdOp.get());
                            }
                        }
                        if (vfModule.getModelInfoVfModule() != null
                                && vfModule.getModelInfoVfModule().getModelName() != null
                                && vfModule.getModelInfoVfModule().getModelName().contains("helm")) {
                            gBB.getRequestContext().setIsHelm(true);
                        }
                        break;
                    }
                }
            }
        } else if (bbName.contains(VOLUME_GROUP)) {
            for (GenericVnf vnf : serviceInstance.getVnfs()) {
                for (VolumeGroup volumeGroup : vnf.getVolumeGroups()) {
                    if (lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID) != null && volumeGroup.getVolumeGroupId()
                            .equalsIgnoreCase(lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID))) {
                        String vnfModelCustomizationUUID =
                                this.bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId()).getModelCustomizationId();
                        ModelInfo vnfModelInfo = new ModelInfo();
                        vnfModelInfo.setModelCustomizationUuid(vnfModelCustomizationUUID);
                        this.mapCatalogVnf(vnf, vnfModelInfo, service);
                        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, vnf.getVnfId());
                        if (cloudRegion != null) {
                            String volumeGroupCustomizationUUID =
                                    this.bbInputSetupUtils
                                            .getAAIVolumeGroup(cloudRegion.getCloudOwner(),
                                                    cloudRegion.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId())
                                            .getModelCustomizationId();
                            ModelInfo volumeGroupModelInfo = new ModelInfo();
                            volumeGroupModelInfo.setModelCustomizationId(volumeGroupCustomizationUUID);
                            this.mapCatalogVolumeGroup(volumeGroup, volumeGroupModelInfo, service,
                                    vnfModelCustomizationUUID);
                        }
                        break;
                    }
                }
            }
        } else if (bbName.contains(NETWORK)) {
            for (L3Network network : serviceInstance.getNetworks()) {
                if (lookupKeyMap.get(ResourceKey.NETWORK_ID) != null
                        && network.getNetworkId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.NETWORK_ID))) {
                    String networkCustomizationUUID =
                            this.bbInputSetupUtils.getAAIL3Network(network.getNetworkId()).getModelCustomizationId();
                    ModelInfo modelInfo = new ModelInfo();
                    modelInfo.setModelCustomizationUuid(networkCustomizationUUID);
                    this.mapCatalogNetwork(network, modelInfo, service);
                    break;
                }
            }
        } else if (bbName.contains("Fabric")) {
            for (Configuration configuration : serviceInstance.getConfigurations()) {
                if (lookupKeyMap.get(ResourceKey.CONFIGURATION_ID) != null && configuration.getConfigurationId()
                        .equalsIgnoreCase(lookupKeyMap.get(ResourceKey.CONFIGURATION_ID))) {
                    String configurationCustUUID = this.bbInputSetupUtils
                            .getAAIConfiguration(configuration.getConfigurationId()).getModelCustomizationId();
                    ModelInfo modelInfo = new ModelInfo();
                    modelInfo.setModelCustomizationUuid(configurationCustUUID);
                    this.mapCatalogConfiguration(configuration, modelInfo, service,
                            executeBB.getConfigurationResourceKeys());
                    break;
                }
            }
        } else if (bbName.equals("HealthCheckBB")
                && (VNF).equalsIgnoreCase(executeBB.getBuildingBlock().getBpmnScope())) {
            this.setisHelmforHealthCheckBB(service, serviceInstance, gBB);
        }
        if (executeBB.getWorkflowResourceIds() != null) {
            parameter.setResourceId(executeBB.getWorkflowResourceIds().getNetworkCollectionId());
            parameter.setKey(executeBB.getBuildingBlock().getKey());
            this.populateNetworkCollectionAndInstanceGroupAssign(parameter);
        }
        return gBB;
    }

    protected GeneralBuildingBlock getGBBMacroUserParams(ExecuteBuildingBlock executeBB, RequestDetails requestDetails,
            Map<ResourceKey, String> lookupKeyMap, String vnfType, String bbName, String key, GeneralBuildingBlock gBB,
            RequestParameters requestParams, Service service, String input) throws Exception {
        ServiceInstance serviceInstance = gBB.getServiceInstance();
        org.onap.so.serviceinstancebeans.Service serviceMacro =
                mapper.readValue(input, org.onap.so.serviceinstancebeans.Service.class);

        Resources resources = serviceMacro.getResources();
        Vnfs vnfs = null;
        VfModules vfModules = null;
        Networks networks = null;

        CloudConfiguration cloudConfiguration = requestDetails.getCloudConfiguration();
        CloudRegion cloudRegion = setCloudConfiguration(gBB, cloudConfiguration);

        BBInputSetupParameter parameter =
                new BBInputSetupParameter.Builder().setRequestId(executeBB.getRequestId()).setService(service)
                        .setBbName(bbName).setServiceInstance(serviceInstance).setLookupKeyMap(lookupKeyMap).build();
        if (bbName.contains(VNF) || (bbName.contains(CONTROLLER)
                && (VNF).equalsIgnoreCase(executeBB.getBuildingBlock().getBpmnScope()))) {
            String vnfInstanceName = lookupKeyMap.get(ResourceKey.VNF_INSTANCE_NAME);
            if (StringUtils.isNotBlank(vnfInstanceName)) {
                vnfs = findVnfsByInstanceName(vnfInstanceName, resources);
            } else {
                vnfs = findVnfsByKey(key, resources);
            }

            // Vnf level cloud configuration takes precedence over service level cloud configuration.
            if (vnfs.getCloudConfiguration() != null) {
                setCloudConfiguration(gBB, vnfs.getCloudConfiguration());
            }

            String vnfId = lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID);
            // This stores the vnf id in request db to be retrieved later when
            // working on a vf module or volume group
            InfraActiveRequests request = this.bbInputSetupUtils.getInfraActiveRequest(executeBB.getRequestId());
            if (request != null) {
                this.bbInputSetupUtils.updateInfraActiveRequestVnfId(request, vnfId);
            }
            parameter.setModelInfo(vnfs.getModelInfo());
            parameter.setInstanceName(vnfs.getInstanceName());
            parameter.setPlatform(vnfs.getPlatform());
            parameter.setLineOfBusiness(vnfs.getLineOfBusiness());
            parameter.setResourceId(vnfId);
            parameter.setVnfType(vnfType);
            parameter.setInstanceParams(vnfs.getInstanceParams());
            parameter.setProductFamilyId(requestDetails.getRequestInfo().getProductFamilyId());
            String applicationId = "";
            if (vnfs.getApplicationId() != null) {
                applicationId = vnfs.getApplicationId();
            }
            parameter.setApplicationId(applicationId);
            this.populateGenericVnf(parameter);
        } else if (bbName.contains(PNF) || (bbName.contains(CONTROLLER)
                && (PNF).equalsIgnoreCase(executeBB.getBuildingBlock().getBpmnScope()))) {
            String pnfId = lookupKeyMap.get(ResourceKey.PNF);
            String pnfInstanceName = lookupKeyMap.get(ResourceKey.PNF_INSTANCE_NAME);
            if (StringUtils.isNotBlank(pnfInstanceName)) {
                resources.getPnfs().stream().filter(pnfs -> Objects.equals(pnfInstanceName, pnfs.getInstanceName()))
                        .findFirst()
                        .ifPresent(pnfs -> BBInputSetupPnf.populatePnfToServiceInstance(pnfs, pnfId, serviceInstance));
            } else {
                resources.getPnfs().stream()
                        .filter(pnfs -> Objects.equals(key, pnfs.getModelInfo().getModelCustomizationId())).findFirst()
                        .ifPresent(pnfs -> BBInputSetupPnf.populatePnfToServiceInstance(pnfs, pnfId, serviceInstance));
            }

            serviceInstance.getPnfs().stream().filter(pnf -> pnfInstanceName.equalsIgnoreCase(pnf.getPnfName()))
                    .findFirst().ifPresent(pnf -> {
                        ModelInfo pnfModelInfo = new ModelInfo();
                        pnfModelInfo.setModelCustomizationUuid(pnf.getModelInfoPnf().getModelCustomizationUuid());
                        pnfModelInfo.setModelCustomizationId(pnf.getModelInfoPnf().getModelCustomizationUuid());
                        mapCatalogPnf(pnf, pnfModelInfo, service);
                    });

        } else if (bbName.contains(VF_MODULE) || bbName.contains(VOLUME_GROUP) || (bbName.contains(CONTROLLER)
                && (VF_MODULE).equalsIgnoreCase(executeBB.getBuildingBlock().getBpmnScope()))) {
            String vfModuleInstanceName = lookupKeyMap.get(ResourceKey.VF_MODULE_INSTANCE_NAME);
            if (StringUtils.isNotBlank(vfModuleInstanceName)) {
                vfModules = getVfModulesByInstanceName(vfModuleInstanceName, resources);
            } else {
                vfModules = getVfModulesByKey(key, resources);
            }

            String vfModulesName = vfModules.getInstanceName();
            String vfModulesModelCustId = vfModules.getModelInfo().getModelCustomizationId();
            // Get the Vnf associated with vfModule
            Optional<org.onap.so.serviceinstancebeans.Vnfs> parentVnf = resources.getVnfs().stream()
                    .filter(aVnf -> aVnf.getCloudConfiguration() != null)
                    .filter(aVnf -> aVnf.getVfModules().stream()
                            .anyMatch(aVfModules -> aVfModules.getInstanceName().equals(vfModulesName) && aVfModules
                                    .getModelInfo().getModelCustomizationId().equals(vfModulesModelCustId)))
                    .findAny();

            // Get the cloud configuration from this Vnf
            if (parentVnf.isPresent()) {
                cloudRegion = setCloudConfiguration(gBB, parentVnf.get().getCloudConfiguration());
            }

            lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, getVnfId(executeBB, lookupKeyMap));

            parameter.setModelInfo(vfModules.getModelInfo());
            if (bbName.contains(VOLUME_GROUP)) {
                parameter.setResourceId(lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID));
                parameter.setInstanceName(vfModules.getVolumeGroupInstanceName());
                parameter.setVnfType(vnfType);
                parameter.setInstanceParams(vfModules.getInstanceParams());
                ServiceModel serviceModel = new ServiceModel();
                serviceModel.setCurrentService(service);
                parameter.setServiceModel(serviceModel);
                this.populateVolumeGroup(parameter);
            } else {
                parameter.setResourceId(lookupKeyMap.get(ResourceKey.VF_MODULE_ID));
                CloudConfiguration cloudConfig = new CloudConfiguration();
                cloudConfig.setLcpCloudRegionId(cloudRegion.getLcpCloudRegionId());
                cloudConfig.setCloudOwner(cloudRegion.getCloudOwner());
                ServiceModel serviceModel = new ServiceModel();
                serviceModel.setCurrentService(service);
                parameter.setServiceModel(serviceModel);
                parameter.setCloudConfiguration(cloudConfig);
                parameter.setInstanceName(vfModules.getInstanceName());
                parameter.setInstanceParams(vfModules.getInstanceParams());
                this.populateVfModule(parameter);
                gBB.getRequestContext().setIsHelm(parameter.getIsHelm());
            }
        } else if (bbName.contains(NETWORK)) {
            networks = findNetworksByKey(key, resources);
            String networkId = lookupKeyMap.get(ResourceKey.NETWORK_ID);
            if (networks != null) {
                // If service level cloud configuration is not provided then get it from networks.
                if (cloudConfiguration == null) {
                    Optional<org.onap.so.serviceinstancebeans.Networks> netWithCloudConfig = resources.getNetworks()
                            .stream().filter(aNetwork -> aNetwork.getCloudConfiguration() != null).findAny();
                    if (netWithCloudConfig.isPresent()) {
                        setCloudConfiguration(gBB, netWithCloudConfig.get().getCloudConfiguration());
                    } else {
                        logger.debug("Could not find any cloud configuration for this request.");
                    }
                }
                parameter.setInstanceName(networks.getInstanceName());
                parameter.setModelInfo(networks.getModelInfo());
                parameter.setInstanceParams(networks.getInstanceParams());
                parameter.setResourceId(networkId);
                this.populateL3Network(parameter);
            }
        } else if (bbName.contains("Configuration")) {
            String configurationId = lookupKeyMap.get(ResourceKey.CONFIGURATION_ID);
            ModelInfo configurationModelInfo = new ModelInfo();
            configurationModelInfo.setModelCustomizationUuid(key);
            ConfigurationResourceCustomization configurationCust =
                    findConfigurationResourceCustomization(configurationModelInfo, service);
            if (configurationCust != null) {
                parameter.setModelInfo(configurationModelInfo);
                parameter.setResourceId(configurationId);
                parameter.setConfigurationResourceKeys(executeBB.getConfigurationResourceKeys());
                parameter.setRequestDetails(executeBB.getRequestDetails());
                this.populateConfiguration(parameter);
            } else {
                logger.debug("Could not find a configuration customization with key: {}", key);
            }
        }
        return gBB;
    }

    /**
     * setCloudConfiguration - set cloud info on a building block.
     *
     * @param gBB
     * @param cloudConfiguration
     * @return CloudRegion
     * @throws Exception
     */
    private CloudRegion setCloudConfiguration(GeneralBuildingBlock gBB, CloudConfiguration cloudConfiguration)
            throws Exception {
        org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = bbInputSetupUtils.getCloudRegion(cloudConfiguration);
        Tenant tenant = getTenant(cloudConfiguration, aaiCloudRegion);
        gBB.setTenant(tenant);
        CloudRegion cloudRegion = mapperLayer.mapCloudRegion(cloudConfiguration, aaiCloudRegion);
        gBB.setCloudRegion(cloudRegion);
        return cloudRegion;
    }

    protected Networks findNetworksByKey(String key, Resources resources) {
        for (Networks networks : resources.getNetworks()) {
            if (networks.getModelInfo().getModelCustomizationId().equalsIgnoreCase(key)) {
                return networks;
            }
        }
        return null;
    }

    protected VfModules getVfModulesByInstanceName(String vfModuleInstanceName, Resources resources) {
        for (Vnfs vnfs : resources.getVnfs()) {
            for (VfModules vfModules : vnfs.getVfModules()) {
                if (vfModules.getInstanceName().equals(vfModuleInstanceName)) {
                    return vfModules;
                }
            }
        }
        throw new ResourceNotFoundException(
                "Could not find vf-module with instanceName: " + vfModuleInstanceName + " in userparams");
    }

    protected VfModules getVfModulesByKey(String key, Resources resources) {
        for (Vnfs vnfs : resources.getVnfs()) {
            for (VfModules vfModules : vnfs.getVfModules()) {
                if (vfModules.getModelInfo().getModelCustomizationId().equalsIgnoreCase(key)) {
                    return vfModules;
                }
            }
        }
        throw new ResourceNotFoundException("Could not find vf-module with key: " + key + " in userparams");
    }

    protected Vnfs findVnfsByInstanceName(String instanceName, Resources resources) {
        for (Vnfs tempVnfs : resources.getVnfs()) {
            if (tempVnfs.getInstanceName().equals(instanceName)) {
                return tempVnfs;
            }
        }
        throw new ResourceNotFoundException("Could not find vnf with instanceName: " + instanceName + " in userparams");
    }

    protected Vnfs findVnfsByKey(String key, Resources resources) {
        for (Vnfs tempVnfs : resources.getVnfs()) {
            if (tempVnfs.getModelInfo().getModelCustomizationId().equalsIgnoreCase(key)) {
                return tempVnfs;
            }
        }
        throw new ResourceNotFoundException("Could not find vnf with key: " + key + " in userparams");
    }

    protected String getVnfId(ExecuteBuildingBlock executeBB, Map<ResourceKey, String> lookupKeyMap) {
        String vnfId = lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID);
        if (vnfId == null) {
            InfraActiveRequests request = this.bbInputSetupUtils.getInfraActiveRequest(executeBB.getRequestId());
            vnfId = request.getVnfId();
        }

        return vnfId;
    }

    protected String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    protected ServiceInstance getServiceInstanceHelper(RequestDetails requestDetails, Customer customer,
            Project project, OwningEntity owningEntity, Map<ResourceKey, String> lookupKeyMap, String serviceInstanceId,
            boolean aLaCarte, Service service, String bbName) throws Exception {
        if (requestDetails.getRequestInfo().getInstanceName() == null && aLaCarte
                && bbName.equalsIgnoreCase(AssignFlows.SERVICE_INSTANCE.toString())) {
            throw new Exception("Request invalid missing: RequestInfo:InstanceName");
        } else {
            org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI;
            serviceInstanceAAI = getServiceInstanceAAI(requestDetails, customer, serviceInstanceId, aLaCarte, bbName);
            if (serviceInstanceAAI != null) {
                lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, serviceInstanceId);
                return this.getExistingServiceInstance(serviceInstanceAAI);
            } else {
                return createServiceInstance(requestDetails, project, owningEntity, lookupKeyMap, serviceInstanceId);
            }
        }
    }

    private org.onap.aai.domain.yang.ServiceInstance getServiceInstanceAAI(RequestDetails requestDetails,
            Customer customer, String serviceInstanceId, boolean aLaCarte, String bbName) throws Exception {
        org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = null;
        if (aLaCarte && bbName.equalsIgnoreCase(AssignFlows.SERVICE_INSTANCE.toString())) {
            serviceInstanceAAI = bbInputSetupUtils
                    .getAAIServiceInstanceByName(requestDetails.getRequestInfo().getInstanceName(), customer);
        }
        if (serviceInstanceId != null && serviceInstanceAAI == null) {
            if (customer != null && customer.getServiceSubscription() != null) {
                serviceInstanceAAI =
                        bbInputSetupUtils.getAAIServiceInstanceByIdAndCustomer(customer.getGlobalCustomerId(),
                                customer.getServiceSubscription().getServiceType(), serviceInstanceId);
            } else {
                serviceInstanceAAI = bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId);
            }
        }
        return serviceInstanceAAI;
    }

    protected ServiceInstance createServiceInstance(RequestDetails requestDetails, Project project,
            OwningEntity owningEntity, Map<ResourceKey, String> lookupKeyMap, String serviceInstanceId) {
        ServiceInstance serviceInstance = new ServiceInstance();
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, serviceInstanceId);
        serviceInstance.setServiceInstanceId(serviceInstanceId);
        if (requestDetails.getRequestInfo() != null) {
            serviceInstance.setServiceInstanceName(requestDetails.getRequestInfo().getInstanceName());
        }
        serviceInstance.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        serviceInstance.setProject(project);
        serviceInstance.setOwningEntity(owningEntity);
        return serviceInstance;
    }

    /**
     * This method is used for getting the existing service instance.
     *
     * This will map the serviceInstanceAAI to serviceInstance and return the serviceInstance.
     *
     * @throws Exception
     * @return serviceInstance
     */
    public ServiceInstance getExistingServiceInstance(org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI)
            throws Exception {
        ServiceInstance serviceInstance = mapperLayer.mapAAIServiceInstanceIntoServiceInstance(serviceInstanceAAI);
        if (serviceInstanceAAI.getRelationshipList() != null
                && serviceInstanceAAI.getRelationshipList().getRelationship() != null
                && !serviceInstanceAAI.getRelationshipList().getRelationship().isEmpty()) {
            addRelationshipsToSI(serviceInstanceAAI, serviceInstance);
        }
        return serviceInstance;
    }

    protected void populateNetworkCollectionAndInstanceGroupAssign(BBInputSetupParameter parameter) throws Exception {
        if (parameter.getServiceInstance().getCollection() == null
                && parameter.getBbName().equalsIgnoreCase(AssignFlows.NETWORK_COLLECTION.toString())) {
            Collection collection = this.createCollection(parameter.getResourceId());
            parameter.getServiceInstance().setCollection(collection);
            this.mapCatalogCollection(parameter.getService(), parameter.getServiceInstance().getCollection(),
                    parameter.getKey());
            if (isVlanTagging(parameter.getService(), parameter.getKey())) {
                InstanceGroup instanceGroup = this.createInstanceGroup();
                parameter.getServiceInstance().getCollection().setInstanceGroup(instanceGroup);
                this.mapCatalogNetworkCollectionInstanceGroup(parameter.getService(),
                        parameter.getServiceInstance().getCollection().getInstanceGroup(), parameter.getKey());
            }
        }
    }

    protected CollectionResourceCustomization findCatalogNetworkCollection(Service service, String key) {
        for (CollectionResourceCustomization collectionCust : service.getCollectionResourceCustomizations()) {
            if (collectionCust.getModelCustomizationUUID().equalsIgnoreCase(key)) {
                return collectionCust;
            }
        }
        return null;
    }

    protected boolean isVlanTagging(Service service, String key) {
        CollectionResourceCustomization collectionCust = findCatalogNetworkCollection(service, key);
        if (collectionCust != null) {
            CollectionResource collectionResource = collectionCust.getCollectionResource();
            if (collectionResource != null && collectionResource.getInstanceGroup() != null
                    && collectionResource.getInstanceGroup().getToscaNodeType() != null
                    && collectionResource.getInstanceGroup().getToscaNodeType().contains("NetworkCollection")) {
                return true;
            }
        }
        return false;
    }

    protected void mapCatalogNetworkCollectionInstanceGroup(Service service, InstanceGroup instanceGroup, String key) {
        CollectionResourceCustomization collectionCust = this.findCatalogNetworkCollection(service, key);
        org.onap.so.db.catalog.beans.InstanceGroup catalogInstanceGroup = null;
        if (collectionCust != null) {
            catalogInstanceGroup = collectionCust.getCollectionResource().getInstanceGroup();
        }
        instanceGroup.setModelInfoInstanceGroup(
                mapperLayer.mapCatalogInstanceGroupToInstanceGroup(collectionCust, catalogInstanceGroup));
    }

    protected void mapCatalogCollection(Service service, Collection collection, String key) {
        CollectionResourceCustomization collectionCust = findCatalogNetworkCollection(service, key);
        if (collectionCust != null) {
            CollectionResource collectionResource = collectionCust.getCollectionResource();
            if (collectionResource != null) {
                collection.setModelInfoCollection(
                        mapperLayer.mapCatalogCollectionToCollection(collectionCust, collectionResource));
            }
        }
    }

    protected Collection createCollection(String collectionId) {
        Collection collection = new Collection();
        collection.setId(collectionId);
        collection.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        return collection;
    }

    protected InstanceGroup createInstanceGroup() {
        InstanceGroup instanceGroup = new InstanceGroup();
        String instanceGroupId = this.generateRandomUUID();
        instanceGroup.setId(instanceGroupId);
        return instanceGroup;
    }

    protected void addRelationshipsToSI(org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI,
            ServiceInstance serviceInstance) throws Exception {
        AAIResultWrapper serviceInstanceWrapper = new AAIResultWrapper(
                new AAICommonObjectMapperProvider().getMapper().writeValueAsString(serviceInstanceAAI));
        Optional<Relationships> relationshipsOp = serviceInstanceWrapper.getRelationships();
        if (relationshipsOp.isPresent()) {
            mapRelationship(serviceInstance, relationshipsOp.get());
        }
    }

    private void mapRelationship(ServiceInstance serviceInstance, Relationships relationships) {
        this.mapProject(relationships.getByType(Types.PROJECT, uri -> uri.nodesOnly(true)), serviceInstance);
        this.mapOwningEntity(relationships.getByType(Types.OWNING_ENTITY, uri -> uri.nodesOnly(true)), serviceInstance);
        this.mapL3Networks(relationships.getRelatedUris(Types.L3_NETWORK), serviceInstance.getNetworks());
        this.mapGenericVnfs(relationships.getRelatedUris(Types.GENERIC_VNF), serviceInstance.getVnfs());
        this.mapPnfs(relationships.getRelatedUris(Types.PNF), serviceInstance.getPnfs());
        this.mapCollection(relationships.getByType(Types.COLLECTION), serviceInstance);
        this.mapConfigurations(relationships.getRelatedUris(Types.CONFIGURATION), serviceInstance.getConfigurations());
    }

    protected void mapConfigurations(List<AAIResourceUri> relatedAAIUris, List<Configuration> configurations) {
        for (AAIResourceUri aaiResourceUri : relatedAAIUris) {
            configurations.add(mapConfiguration(aaiResourceUri));
        }
    }

    protected Configuration mapConfiguration(AAIResourceUri aaiResourceUri) {
        AAIResultWrapper aaiConfigurationWrapper = this.bbInputSetupUtils.getAAIResourceDepthOne(aaiResourceUri);
        Optional<org.onap.aai.domain.yang.Configuration> aaiConfigurationOp =
                aaiConfigurationWrapper.asBean(org.onap.aai.domain.yang.Configuration.class);
        if (!aaiConfigurationOp.isPresent()) {
            return null;
        }

        return this.mapperLayer.mapAAIConfiguration(aaiConfigurationOp.get());
    }

    protected void mapGenericVnfs(List<AAIResourceUri> list, List<GenericVnf> genericVnfs) {
        for (AAIResourceUri aaiResourceUri : list) {
            genericVnfs.add(this.mapGenericVnf(aaiResourceUri));
        }
    }

    protected GenericVnf mapGenericVnf(AAIResourceUri aaiResourceUri) {
        AAIResultWrapper aaiGenericVnfWrapper = this.bbInputSetupUtils.getAAIResourceDepthOne(aaiResourceUri);
        Optional<org.onap.aai.domain.yang.GenericVnf> aaiGenericVnfOp =
                aaiGenericVnfWrapper.asBean(org.onap.aai.domain.yang.GenericVnf.class);
        if (!aaiGenericVnfOp.isPresent()) {
            return null;
        }

        GenericVnf genericVnf = this.mapperLayer.mapAAIGenericVnfIntoGenericVnf(aaiGenericVnfOp.get());

        Optional<Relationships> relationshipsOp = aaiGenericVnfWrapper.getRelationships();
        if (relationshipsOp.isPresent()) {
            Relationships relationships = relationshipsOp.get();
            this.mapPlatform(relationships.getByType(Types.PLATFORM, uri -> uri.nodesOnly(true)), genericVnf);
            this.mapLineOfBusiness(relationships.getByType(Types.LINE_OF_BUSINESS, uri -> uri.nodesOnly(true)),
                    genericVnf);
            genericVnf.getVolumeGroups().addAll(mapVolumeGroups(relationships.getByType(Types.VOLUME_GROUP)));
            genericVnf.getInstanceGroups().addAll(mapInstanceGroups(relationships.getByType(Types.INSTANCE_GROUP)));
        }

        return genericVnf;
    }

    protected void mapPnfs(List<AAIResourceUri> list, List<Pnf> pnfs) {
        for (AAIResourceUri aaiResourceUri : list) {
            pnfs.add(this.mapPnf(aaiResourceUri));
        }
    }

    protected Pnf mapPnf(AAIResourceUri aaiResourceUri) {
        AAIResultWrapper aaiPnfWrapper = this.bbInputSetupUtils.getAAIResourceDepthOne(aaiResourceUri);
        Optional<org.onap.aai.domain.yang.Pnf> aaiPnfWrapperOp =
                aaiPnfWrapper.asBean(org.onap.aai.domain.yang.Pnf.class);
        return aaiPnfWrapperOp.map(pnf -> this.mapperLayer.mapAAIPnfIntoPnf(pnf)).orElse(null);
    }

    protected List<InstanceGroup> mapInstanceGroups(List<AAIResultWrapper> instanceGroups) {
        List<InstanceGroup> instanceGroupsList = new ArrayList<>();
        for (AAIResultWrapper volumeGroupWrapper : instanceGroups) {
            instanceGroupsList.add(this.mapInstanceGroup(volumeGroupWrapper));
        }
        return instanceGroupsList;
    }

    protected InstanceGroup mapInstanceGroup(AAIResultWrapper instanceGroupWrapper) {
        Optional<org.onap.aai.domain.yang.InstanceGroup> aaiInstanceGroupOp =
                instanceGroupWrapper.asBean(org.onap.aai.domain.yang.InstanceGroup.class);
        org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = null;

        if (!aaiInstanceGroupOp.isPresent()) {
            return null;
        }

        aaiInstanceGroup = aaiInstanceGroupOp.get();
        InstanceGroup instanceGroup = this.mapperLayer.mapAAIInstanceGroupIntoInstanceGroup(aaiInstanceGroup);
        instanceGroup.setModelInfoInstanceGroup(this.mapperLayer.mapCatalogInstanceGroupToInstanceGroup(null,
                this.bbInputSetupUtils.getCatalogInstanceGroup(aaiInstanceGroup.getModelVersionId())));
        return instanceGroup;
    }

    protected List<VolumeGroup> mapVolumeGroups(List<AAIResultWrapper> volumeGroups) {
        List<VolumeGroup> volumeGroupsList = new ArrayList<>();
        for (AAIResultWrapper volumeGroupWrapper : volumeGroups) {
            volumeGroupsList.add(this.mapVolumeGroup(volumeGroupWrapper));
        }
        return volumeGroupsList;
    }

    protected VolumeGroup mapVolumeGroup(AAIResultWrapper volumeGroupWrapper) {
        Optional<org.onap.aai.domain.yang.VolumeGroup> aaiVolumeGroupOp =
                volumeGroupWrapper.asBean(org.onap.aai.domain.yang.VolumeGroup.class);
        org.onap.aai.domain.yang.VolumeGroup aaiVolumeGroup = null;

        if (!aaiVolumeGroupOp.isPresent()) {
            return null;
        }

        aaiVolumeGroup = aaiVolumeGroupOp.get();
        return this.mapperLayer.mapAAIVolumeGroup(aaiVolumeGroup);
    }

    protected void mapLineOfBusiness(List<AAIResultWrapper> lineOfBusinesses, GenericVnf genericVnf) {
        if (!lineOfBusinesses.isEmpty()) {
            AAIResultWrapper lineOfBusinessWrapper = lineOfBusinesses.get(0);
            Optional<org.onap.aai.domain.yang.LineOfBusiness> aaiLineOfBusinessOp =
                    lineOfBusinessWrapper.asBean(org.onap.aai.domain.yang.LineOfBusiness.class);
            if (aaiLineOfBusinessOp.isPresent()) {
                LineOfBusiness lineOfBusiness = this.mapperLayer.mapAAILineOfBusiness(aaiLineOfBusinessOp.get());
                genericVnf.setLineOfBusiness(lineOfBusiness);
            }
        }
    }

    protected void mapPlatform(List<AAIResultWrapper> platforms, GenericVnf genericVnf) {
        if (!platforms.isEmpty()) {
            AAIResultWrapper platformWrapper = platforms.get(0);
            Optional<org.onap.aai.domain.yang.Platform> aaiPlatformOp =
                    platformWrapper.asBean(org.onap.aai.domain.yang.Platform.class);
            if (aaiPlatformOp.isPresent()) {
                Platform platform = this.mapperLayer.mapAAIPlatform(aaiPlatformOp.get());
                genericVnf.setPlatform(platform);
            }
        }
    }

    protected void mapCollection(List<AAIResultWrapper> collections, ServiceInstance serviceInstance) {
        if (!collections.isEmpty()) {
            AAIResultWrapper collectionWrapper = collections.get(0);
            Optional<org.onap.aai.domain.yang.Collection> aaiCollectionOp =
                    collectionWrapper.asBean(org.onap.aai.domain.yang.Collection.class);
            aaiCollectionOp.ifPresent(
                    collection -> serviceInstanceSetCollection(serviceInstance, collectionWrapper, collection));
        }
    }

    private void serviceInstanceSetCollection(ServiceInstance serviceInstance, AAIResultWrapper collectionWrapper,
            org.onap.aai.domain.yang.Collection aaiCollection) {
        Collection collection = getCollection(aaiCollection);
        Optional<Relationships> relationshipsOp = collectionWrapper.getRelationships();
        relationshipsOp.ifPresent(relationships -> setInstanceGroupForCollection(collection, relationships));
        serviceInstance.setCollection(collection);
    }

    private void setInstanceGroupForCollection(Collection collection, Relationships relationships) {
        List<InstanceGroup> instanceGroupsList = mapInstanceGroups(relationships.getByType(Types.INSTANCE_GROUP));
        if (!instanceGroupsList.isEmpty()) {
            collection.setInstanceGroup(instanceGroupsList.get(0));
        }
    }

    private Collection getCollection(org.onap.aai.domain.yang.Collection aaiCollection) {
        Collection collection = this.mapperLayer.mapAAICollectionIntoCollection(aaiCollection);
        NetworkCollectionResourceCustomization collectionResourceCust = bbInputSetupUtils
                .getCatalogNetworkCollectionResourceCustByID(aaiCollection.getCollectionCustomizationId());
        collection.setModelInfoCollection(mapperLayer.mapCatalogCollectionToCollection(collectionResourceCust,
                collectionResourceCust.getCollectionResource()));
        return collection;
    }

    private org.onap.so.serviceinstancebeans.Service serviceMapper(Map<String, Object> params) throws IOException {
        String input = mapper.writeValueAsString(params.get("service"));
        return mapper.readValue(input, org.onap.so.serviceinstancebeans.Service.class);
    }

    private void setisHelmforHealthCheckBB(Service service, ServiceInstance serviceInstance, GeneralBuildingBlock gBB) {
        for (GenericVnf vnf : serviceInstance.getVnfs()) {
            for (VfModule vfModule : vnf.getVfModules()) {
                String vnfModelCustomizationUUID =
                        this.bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId()).getModelCustomizationId();
                ModelInfo vnfModelInfo = new ModelInfo();
                vnfModelInfo.setModelCustomizationUuid(vnfModelCustomizationUUID);
                this.mapCatalogVnf(vnf, vnfModelInfo, service);
                String vfModuleCustomizationUUID = this.bbInputSetupUtils
                        .getAAIVfModule(vnf.getVnfId(), vfModule.getVfModuleId()).getModelCustomizationId();
                ModelInfo vfModuleModelInfo = new ModelInfo();
                vfModuleModelInfo.setModelCustomizationId(vfModuleCustomizationUUID);
                this.mapCatalogVfModule(vfModule, vfModuleModelInfo, service, vnfModelCustomizationUUID);
                if (vfModule.getModelInfoVfModule() != null && vfModule.getModelInfoVfModule().getModelName() != null
                        && vfModule.getModelInfoVfModule().getModelName().contains("helm")) {
                    gBB.getRequestContext().setIsHelm(true);
                    break;
                }
            }
        }
    }

    protected void mapL3Networks(List<AAIResourceUri> list, List<L3Network> l3Networks) {
        for (AAIResourceUri aaiResourceUri : list) {
            l3Networks.add(this.mapL3Network(aaiResourceUri));
        }
    }

    protected L3Network mapL3Network(AAIResourceUri aaiResourceUri) {
        AAIResultWrapper aaiNetworkWrapper = this.bbInputSetupUtils.getAAIResourceDepthTwo(aaiResourceUri);
        Optional<org.onap.aai.domain.yang.L3Network> aaiL3NetworkOp =
                aaiNetworkWrapper.asBean(org.onap.aai.domain.yang.L3Network.class);
        org.onap.aai.domain.yang.L3Network aaiL3Network = null;

        if (!aaiL3NetworkOp.isPresent()) {
            return null;
        }

        aaiL3Network = aaiL3NetworkOp.get();
        L3Network network = this.mapperLayer.mapAAIL3Network(aaiL3Network);

        Optional<Relationships> relationshipsOp = aaiNetworkWrapper.getRelationships();
        if (relationshipsOp.isPresent()) {
            Relationships relationships = relationshipsOp.get();
            this.mapNetworkPolicies(relationships.getByType(Types.NETWORK_POLICY), network.getNetworkPolicies());
            mapRouteTableReferences(relationships.getByType(Types.ROUTE_TABLE_REFERENCE),
                    network.getContrailNetworkRouteTableReferences());
        }

        return network;
    }

    protected void mapNetworkPolicies(List<AAIResultWrapper> aaiNetworkPolicies, List<NetworkPolicy> networkPolicies) {
        for (AAIResultWrapper networkPolicyWrapper : aaiNetworkPolicies) {
            networkPolicies.add(this.mapNetworkPolicy(networkPolicyWrapper));
        }
    }

    protected NetworkPolicy mapNetworkPolicy(AAIResultWrapper networkPolicyWrapper) {
        Optional<org.onap.aai.domain.yang.NetworkPolicy> aaiNetworkPolicyOp =
                networkPolicyWrapper.asBean(org.onap.aai.domain.yang.NetworkPolicy.class);
        org.onap.aai.domain.yang.NetworkPolicy aaiNetworkPolicy = null;

        if (!aaiNetworkPolicyOp.isPresent()) {
            return null;
        }

        aaiNetworkPolicy = aaiNetworkPolicyOp.get();
        return this.mapperLayer.mapAAINetworkPolicy(aaiNetworkPolicy);
    }

    protected void mapRouteTableReferences(List<AAIResultWrapper> routeTableReferences,
            List<RouteTableReference> contrailNetworkRouteTableReferences) {
        for (AAIResultWrapper routeTableReferenceWrapper : routeTableReferences) {
            contrailNetworkRouteTableReferences.add(this.mapRouteTableReference(routeTableReferenceWrapper));
        }
    }

    protected RouteTableReference mapRouteTableReference(AAIResultWrapper routeTableReferenceWrapper) {
        Optional<org.onap.aai.domain.yang.RouteTableReference> aaiRouteTableReferenceOp =
                routeTableReferenceWrapper.asBean(org.onap.aai.domain.yang.RouteTableReference.class);
        org.onap.aai.domain.yang.RouteTableReference aaiRouteTableReference = null;

        if (!aaiRouteTableReferenceOp.isPresent()) {
            return null;
        }

        aaiRouteTableReference = aaiRouteTableReferenceOp.get();
        return this.mapperLayer.mapAAIRouteTableReferenceIntoRouteTableReference(aaiRouteTableReference);
    }

    protected void mapOwningEntity(List<AAIResultWrapper> owningEntities, ServiceInstance serviceInstance) {
        if (!owningEntities.isEmpty()) {
            AAIResultWrapper owningEntityWrapper = owningEntities.get(0);
            Optional<org.onap.aai.domain.yang.OwningEntity> aaiOwningEntityOp =
                    owningEntityWrapper.asBean(org.onap.aai.domain.yang.OwningEntity.class);
            if (aaiOwningEntityOp.isPresent()) {
                OwningEntity owningEntity = this.mapperLayer.mapAAIOwningEntity(aaiOwningEntityOp.get());
                serviceInstance.setOwningEntity(owningEntity);
            }
        }
    }

    protected void mapProject(List<AAIResultWrapper> projects, ServiceInstance serviceInstance) {
        if (!projects.isEmpty()) {
            AAIResultWrapper projectWrapper = projects.get(0);
            Optional<org.onap.aai.domain.yang.Project> aaiProjectOp =
                    projectWrapper.asBean(org.onap.aai.domain.yang.Project.class);
            if (aaiProjectOp.isPresent()) {
                Project project = this.mapperLayer.mapAAIProject(aaiProjectOp.get());
                serviceInstance.setProject(project);
            }
        }
    }

    protected Customer mapCustomer(String globalCustomerId, String subscriptionServiceType) {
        org.onap.aai.domain.yang.Customer aaiCustomer = this.bbInputSetupUtils.getAAICustomer(globalCustomerId);
        org.onap.aai.domain.yang.ServiceSubscription aaiServiceSubscription =
                this.bbInputSetupUtils.getAAIServiceSubscription(globalCustomerId, subscriptionServiceType);
        Customer customer = this.mapperLayer.mapAAICustomer(aaiCustomer);
        ServiceSubscription serviceSubscription = this.mapperLayer.mapAAIServiceSubscription(aaiServiceSubscription);
        if (serviceSubscription != null) {
            customer.setServiceSubscription(serviceSubscription);
        }
        return customer;
    }
}
