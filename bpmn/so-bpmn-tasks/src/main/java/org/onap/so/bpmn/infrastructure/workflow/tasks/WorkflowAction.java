/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2021 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.ASSIGNINSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.CONTROLLER;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.CREATE_INSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.FABRIC_CONFIGURATION;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.NETWORKCOLLECTION;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.REPLACEINSTANCE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.REPLACEINSTANCERETAINASSIGNMENTS;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.SERVICE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.USER_PARAM_SERVICE;
import static org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowActionConstants.WORKFLOW_ACTION_ERROR_MESSAGE;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SerializationUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.javatuples.Pair;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Vnfc;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.aai.domain.yang.VpnBinding;
import org.onap.aaiclient.client.aai.AAICommonObjectMapperProvider;
import org.onap.aaiclient.client.aai.AAIObjectName;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.Relationships;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader.VnfEBBLoader;
import org.onap.so.bpmn.infrastructure.workflow.tasks.excpetion.VnfcMultipleRelationshipException;
import org.onap.so.bpmn.infrastructure.workflow.tasks.utils.WorkflowResourceIdsUtils;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.client.orchestration.AAIEntityNotFoundException;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkflowAction {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowAction.class);

    private static final String SERVICE_INSTANCES = "serviceInstances";
    private static final String VF_MODULES = "vfModules";
    private static final String VNF_TYPE = "vnfType";
    private static final String CONFIGURATION = "Configuration";
    private static final String SUPPORTEDTYPES =
            "vnfs|vfModules|networks|networkCollections|volumeGroups|serviceInstances|instanceGroups";
    private static final String HOMINGSOLUTION = "Homing_Solution";
    private static final String SERVICE_TYPE_TRANSPORT = "TRANSPORT";
    private static final String SERVICE_TYPE_BONDING = "BONDING";
    private static final String CLOUD_OWNER = "DEFAULT";
    private static final String CREATENETWORKBB = "CreateNetworkBB";
    private static final String ACTIVATENETWORKBB = "ActivateNetworkBB";
    private static final String VOLUMEGROUP_DELETE_PATTERN = "(Un|De)(.*)Volume(.*)";
    private static final String VOLUMEGROUP_CREATE_PATTERN = "(A|C)(.*)Volume(.*)";
    private static final String DEFAULT_CLOUD_OWNER = "org.onap.so.cloud-owner";
    private static final String HOMING = "homing";

    @Autowired
    protected BBInputSetup bbInputSetup;
    @Autowired
    protected BBInputSetupUtils bbInputSetupUtils;
    @Autowired
    private ExceptionBuilder exceptionBuilder;
    @Autowired
    private CatalogDbClient catalogDbClient;
    @Autowired
    private AAIConfigurationResources aaiConfigurationResources;
    @Autowired
    private WorkflowActionExtractResourcesAAI workflowActionUtils;
    @Autowired
    private VrfValidation vrfValidation;
    @Autowired
    private Environment environment;
    @Autowired
    private UserParamsServiceTraversal userParamsServiceTraversal;
    @Autowired
    private AaiResourceIdValidator aaiResourceIdValidator;
    @Autowired
    private ExecuteBuildingBlockBuilder executeBuildingBlockBuilder;
    @Autowired
    private VnfEBBLoader vnfEBBLoader;

    public void setBbInputSetupUtils(BBInputSetupUtils bbInputSetupUtils) {
        this.bbInputSetupUtils = bbInputSetupUtils;
    }

    public void setBbInputSetup(BBInputSetup bbInputSetup) {
        this.bbInputSetup = bbInputSetup;
    }

    public void selectExecutionList(DelegateExecution execution) throws Exception {
        try {
            fillExecutionDefault(execution);
            final String bpmnRequest = (String) execution.getVariable(BBConstants.G_BPMN_REQUEST);
            ServiceInstancesRequest sIRequest =
                    new ObjectMapper().readValue(bpmnRequest, ServiceInstancesRequest.class);

            final String requestId = (String) execution.getVariable(BBConstants.G_REQUEST_ID);

            String uri = (String) execution.getVariable(BBConstants.G_URI);
            boolean isResume = isUriResume(uri);

            final boolean isALaCarte = (boolean) execution.getVariable(BBConstants.G_ALACARTE);
            Resource resource = getResource(bbInputSetupUtils, isResume, isALaCarte, uri, requestId);

            WorkflowResourceIds workflowResourceIds = populateResourceIdsFromApiHandler(execution);
            RequestDetails requestDetails = sIRequest.getRequestDetails();
            String requestAction = (String) execution.getVariable(BBConstants.G_ACTION);
            String resourceId = getResourceId(resource, requestAction, requestDetails, workflowResourceIds);
            WorkflowType resourceType = resource.getResourceType();

            String serviceInstanceId = getServiceInstanceId(execution, resourceId, resourceType);

            fillExecution(execution, requestDetails.getRequestInfo().getSuppressRollback(), resourceId, resourceType);
            List<ExecuteBuildingBlock> flowsToExecute;
            if (isRequestMacroServiceResume(isALaCarte, resourceType, requestAction, serviceInstanceId)) {
                String errorMessage = "Could not resume Macro flow. Error loading execution path.";
                flowsToExecute = loadExecuteBuildingBlocks(execution, requestId, errorMessage);
            } else if (isALaCarte && isResume) {
                String errorMessage =
                        "Could not resume request with request Id: " + requestId + ". No flowsToExecute was found";
                flowsToExecute = loadExecuteBuildingBlocks(execution, requestId, errorMessage);
            } else {
                String vnfType = (String) execution.getVariable(VNF_TYPE);
                String cloudOwner = getCloudOwner(requestDetails.getCloudConfiguration());
                List<OrchestrationFlow> orchFlows =
                        (List<OrchestrationFlow>) execution.getVariable(BBConstants.G_ORCHESTRATION_FLOW);
                final String apiVersion = (String) execution.getVariable(BBConstants.G_APIVERSION);
                final String serviceType =
                        Optional.ofNullable((String) execution.getVariable(BBConstants.G_SERVICE_TYPE)).orElse("");
                if (isALaCarte) {
                    flowsToExecute = loadExecuteBuildingBlocksForAlaCarte(orchFlows, execution, requestAction,
                            resourceType, cloudOwner, serviceType, sIRequest, requestId, workflowResourceIds,
                            requestDetails, resourceId, vnfType, apiVersion);
                } else {
                    flowsToExecute = loadExecuteBuildingBlocksForMacro(sIRequest, resourceType, requestAction,
                            execution, serviceInstanceId, resourceId, workflowResourceIds, orchFlows, cloudOwner,
                            serviceType, requestId, apiVersion, vnfType, requestDetails);
                }
            }
            // If the user set "Homing_Solution" to "none", disable homing, else if "Homing_Solution" is specified,
            // enable it.
            if (sIRequest.getRequestDetails().getRequestParameters() != null
                    && sIRequest.getRequestDetails().getRequestParameters().getUserParams() != null) {
                List<Map<String, Object>> userParams =
                        sIRequest.getRequestDetails().getRequestParameters().getUserParams();
                for (Map<String, Object> params : userParams) {
                    if (params.containsKey(HOMINGSOLUTION)) {
                        execution.setVariable(HOMING, !"none".equals(params.get(HOMINGSOLUTION)));
                    }
                }
            }

            if (CollectionUtils.isEmpty(flowsToExecute)) {
                throw new IllegalStateException("Macro did not come up with a valid execution path.");
            }

            List<String> flowNames = new ArrayList<>();
            logger.info("List of BuildingBlocks to execute:");

            flowsToExecute.forEach(ebb -> {
                logger.info(ebb.getBuildingBlock().getBpmnFlowName());
                flowNames.add(ebb.getBuildingBlock().getBpmnFlowName());
            });

            if (!isResume) {
                bbInputSetupUtils.persistFlowExecutionPath(requestId, flowsToExecute);
            }
            setExecutionVariables(execution, flowsToExecute, flowNames);

        } catch (Exception ex) {
            if (!(execution.hasVariable("WorkflowException")
                    || execution.hasVariable("WorkflowExceptionExceptionMessage"))) {
                buildAndThrowException(execution, "Exception while setting execution list. ", ex);
            } else {
                throw ex;
            }
        }
    }

    private List<ExecuteBuildingBlock> loadExecuteBuildingBlocksForAlaCarte(List<OrchestrationFlow> orchFlows,
            DelegateExecution execution, String requestAction, WorkflowType resourceType, String cloudOwner,
            String serviceType, ServiceInstancesRequest sIRequest, String requestId,
            WorkflowResourceIds workflowResourceIds, RequestDetails requestDetails, String resourceId, String vnfType,
            String apiVersion) throws Exception {
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
        if (orchFlows == null || orchFlows.isEmpty()) {
            orchFlows = queryNorthBoundRequestCatalogDb(execution, requestAction, resourceType, true, cloudOwner,
                    serviceType);
        }
        Resource resourceKey = getResourceKey(sIRequest, resourceType);

        ReplaceInstanceRelatedInformation replaceInfo = new ReplaceInstanceRelatedInformation();
        if ((requestAction.equalsIgnoreCase(REPLACEINSTANCE)
                || requestAction.equalsIgnoreCase(REPLACEINSTANCERETAINASSIGNMENTS))
                && resourceType.equals(WorkflowType.VFMODULE)) {
            logger.debug("Build a BB list for replacing BB modules");
            ConfigBuildingBlocksDataObject cbbdo = createConfigBuildingBlocksDataObject(execution, sIRequest, requestId,
                    workflowResourceIds, requestDetails, requestAction, resourceId, vnfType, orchFlows, apiVersion,
                    resourceKey, replaceInfo);
            orchFlows = getVfModuleReplaceBuildingBlocks(cbbdo);

            createBuildingBlocksForOrchFlows(execution, sIRequest, requestId, workflowResourceIds, requestDetails,
                    requestAction, resourceId, flowsToExecute, vnfType, orchFlows, apiVersion, resourceKey,
                    replaceInfo);
        } else {
            if (isConfiguration(orchFlows) && !requestAction.equalsIgnoreCase(CREATE_INSTANCE)) {
                addConfigBuildingBlocksToFlowsToExecuteList(execution, sIRequest, requestId, workflowResourceIds,
                        requestDetails, requestAction, resourceId, flowsToExecute, vnfType, apiVersion, resourceKey,
                        replaceInfo, orchFlows);
            }
            orchFlows = orchFlows.stream().filter(item -> !item.getFlowName().contains(FABRIC_CONFIGURATION))
                    .collect(Collectors.toList());

            for (OrchestrationFlow orchFlow : orchFlows) {
                ExecuteBuildingBlock ebb = executeBuildingBlockBuilder.buildExecuteBuildingBlock(orchFlow, requestId,
                        resourceKey, apiVersion, resourceId, requestAction, true, vnfType, workflowResourceIds,
                        requestDetails, false, null, null, false, replaceInfo);
                flowsToExecute.add(ebb);
            }
        }
        return flowsToExecute;
    }

    private List<ExecuteBuildingBlock> loadExecuteBuildingBlocksForMacro(ServiceInstancesRequest sIRequest,
            WorkflowType resourceType, String requestAction, DelegateExecution execution, String serviceInstanceId,
            String resourceId, WorkflowResourceIds workflowResourceIds, List<OrchestrationFlow> orchFlows,
            String cloudOwner, String serviceType, String requestId, String apiVersion, String vnfType,
            RequestDetails requestDetails) throws IOException, VrfBondingServiceException {
        List<ExecuteBuildingBlock> flowsToExecute;
        boolean containsService = false;
        List<Resource> resourceList = new ArrayList<>();
        List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();
        List<Map<String, Object>> userParams = sIRequest.getRequestDetails().getRequestParameters().getUserParams();
        if (resourceType == WorkflowType.SERVICE && requestAction.equalsIgnoreCase(ASSIGNINSTANCE)) {
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
        } else if (resourceType == WorkflowType.SERVICE && requestAction.equalsIgnoreCase(CREATE_INSTANCE)) {
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
            if (!foundRelated(resourceList)) {
                traverseCatalogDbService(execution, sIRequest, resourceList, aaiResourceIds);
            }
        } else if (resourceType == WorkflowType.SERVICE && ("activateInstance".equalsIgnoreCase(requestAction)
                || "unassignInstance".equalsIgnoreCase(requestAction)
                || "deleteInstance".equalsIgnoreCase(requestAction)
                || requestAction.equalsIgnoreCase("activate" + FABRIC_CONFIGURATION))) {
            // SERVICE-MACRO-ACTIVATE, SERVICE-MACRO-UNASSIGN, and
            // SERVICE-MACRO-DELETE
            // Will never get user params with service, macro will have
            // to query the SI in AAI to find related instances.
            traverseAAIService(execution, resourceList, resourceId, aaiResourceIds);
        } else if (resourceType == WorkflowType.SERVICE && "deactivateInstance".equalsIgnoreCase(requestAction)) {
            resourceList.add(new Resource(WorkflowType.SERVICE, "", false));
        } else if (resourceType == WorkflowType.VNF && (REPLACEINSTANCE.equalsIgnoreCase(requestAction)
                || ("recreateInstance".equalsIgnoreCase(requestAction)))) {
            vnfEBBLoader.traverseAAIVnf(execution, resourceList, workflowResourceIds.getServiceInstanceId(),
                    workflowResourceIds.getVnfId(), aaiResourceIds);
        } else if (resourceType == WorkflowType.VNF && "updateInstance".equalsIgnoreCase(requestAction)) {
            vnfEBBLoader.customTraverseAAIVnf(execution, resourceList, workflowResourceIds.getServiceInstanceId(),
                    workflowResourceIds.getVnfId(), aaiResourceIds);
        } else {
            buildAndThrowException(execution, "Current Macro Request is not supported");
        }
        StringBuilder foundObjects = new StringBuilder();
        for (WorkflowType type : WorkflowType.values()) {
            foundObjects.append(type).append(" - ")
                    .append((int) resourceList.stream().filter(x -> type.equals(x.getResourceType())).count())
                    .append("    ");
        }
        logger.info("Found {}", foundObjects);

        if (orchFlows == null || orchFlows.isEmpty()) {
            orchFlows = queryNorthBoundRequestCatalogDb(execution, requestAction, resourceType, false, cloudOwner,
                    serviceType);
        }
        boolean vnfReplace = false;
        if (resourceType.equals(WorkflowType.VNF) && (REPLACEINSTANCE.equalsIgnoreCase(requestAction)
                || REPLACEINSTANCERETAINASSIGNMENTS.equalsIgnoreCase(requestAction))) {
            vnfReplace = true;
        }
        flowsToExecute = executeBuildingBlockBuilder.buildExecuteBuildingBlockList(orchFlows, resourceList, requestId,
                apiVersion, resourceId, requestAction, vnfType, workflowResourceIds, requestDetails, vnfReplace);
        if (isNetworkCollectionInTheResourceList(resourceList)) {
            logger.info("Sorting for Vlan Tagging");
            flowsToExecute = sortExecutionPathByObjectForVlanTagging(flowsToExecute, requestAction);
        }
        // By default, enable homing at VNF level for CREATE_INSTANCE and ASSIGNINSTANCE
        if (resourceType == WorkflowType.SERVICE
                && (requestAction.equals(CREATE_INSTANCE) || requestAction.equals(ASSIGNINSTANCE))
                && resourceList.stream().anyMatch(x -> WorkflowType.VNF.equals(x.getResourceType()))) {
            execution.setVariable(HOMING, true);
            execution.setVariable("calledHoming", false);
        }
        if (resourceType == WorkflowType.SERVICE && (requestAction.equalsIgnoreCase(ASSIGNINSTANCE)
                || requestAction.equalsIgnoreCase(CREATE_INSTANCE))) {
            generateResourceIds(flowsToExecute, resourceList, serviceInstanceId);
        } else {
            updateResourceIdsFromAAITraversal(flowsToExecute, resourceList, aaiResourceIds, serviceInstanceId);
        }
        return flowsToExecute;
    }

    private void setExecutionVariables(DelegateExecution execution, List<ExecuteBuildingBlock> flowsToExecute,
            List<String> flowNames) {
        execution.setVariable("flowNames", flowNames);
        execution.setVariable(BBConstants.G_CURRENT_SEQUENCE, 0);
        execution.setVariable("retryCount", 0);
        execution.setVariable("isRollback", false);
        execution.setVariable("flowsToExecute", flowsToExecute);
        execution.setVariable("isRollbackComplete", false);
    }

    private boolean isContainsService(ServiceInstancesRequest sIRequest) {
        boolean containsService;
        List<Map<String, Object>> userParams = sIRequest.getRequestDetails().getRequestParameters().getUserParams();
        containsService = userParams.stream().anyMatch(param -> param.containsKey(USER_PARAM_SERVICE));
        return containsService;
    }

    private List<ExecuteBuildingBlock> loadExecuteBuildingBlocks(DelegateExecution execution, String requestId,
            String errorMessage) {
        List<ExecuteBuildingBlock> flowsToExecute;
        flowsToExecute = bbInputSetupUtils.loadOriginalFlowExecutionPath(requestId);
        if (flowsToExecute == null) {
            buildAndThrowException(execution, errorMessage);
        }
        return flowsToExecute;
    }

    private ConfigBuildingBlocksDataObject createConfigBuildingBlocksDataObject(DelegateExecution execution,
            ServiceInstancesRequest sIRequest, String requestId, WorkflowResourceIds workflowResourceIds,
            RequestDetails requestDetails, String requestAction, String resourceId, String vnfType,
            List<OrchestrationFlow> orchFlows, String apiVersion, Resource resourceKey,
            ReplaceInstanceRelatedInformation replaceInfo) {

        return new ConfigBuildingBlocksDataObject().setsIRequest(sIRequest).setOrchFlows(orchFlows)
                .setRequestId(requestId).setResourceKey(resourceKey).setApiVersion(apiVersion).setResourceId(resourceId)
                .setRequestAction(requestAction).setaLaCarte(true).setVnfType(vnfType)
                .setWorkflowResourceIds(workflowResourceIds).setRequestDetails(requestDetails).setExecution(execution)
                .setReplaceInformation(replaceInfo);
    }

    private void createBuildingBlocksForOrchFlows(DelegateExecution execution, ServiceInstancesRequest sIRequest,
            String requestId, WorkflowResourceIds workflowResourceIds, RequestDetails requestDetails,
            String requestAction, String resourceId, List<ExecuteBuildingBlock> flowsToExecute, String vnfType,
            List<OrchestrationFlow> orchFlows, String apiVersion, Resource resourceKey,
            ReplaceInstanceRelatedInformation replaceInfo) throws Exception {

        for (OrchestrationFlow orchFlow : orchFlows) {
            if (orchFlow.getFlowName().contains(CONFIGURATION)) {
                List<OrchestrationFlow> configOrchFlows = new ArrayList<>();
                configOrchFlows.add(orchFlow);
                addConfigBuildingBlocksToFlowsToExecuteList(execution, sIRequest, requestId, workflowResourceIds,
                        requestDetails, requestAction, resourceId, flowsToExecute, vnfType, apiVersion, resourceKey,
                        replaceInfo, configOrchFlows);
            } else {
                ExecuteBuildingBlock ebb = executeBuildingBlockBuilder.buildExecuteBuildingBlock(orchFlow, requestId,
                        resourceKey, apiVersion, resourceId, requestAction, true, vnfType, workflowResourceIds,
                        requestDetails, false, null, null, false, replaceInfo);
                flowsToExecute.add(ebb);
            }
        }
    }

    private void addConfigBuildingBlocksToFlowsToExecuteList(DelegateExecution execution,
            ServiceInstancesRequest sIRequest, String requestId, WorkflowResourceIds workflowResourceIds,
            RequestDetails requestDetails, String requestAction, String resourceId,
            List<ExecuteBuildingBlock> flowsToExecute, String vnfType, String apiVersion, Resource resourceKey,
            ReplaceInstanceRelatedInformation replaceInfo, List<OrchestrationFlow> configOrchFlows) throws Exception {

        ConfigBuildingBlocksDataObject cbbdo = createConfigBuildingBlocksDataObject(execution, sIRequest, requestId,
                workflowResourceIds, requestDetails, requestAction, resourceId, vnfType, configOrchFlows, apiVersion,
                resourceKey, replaceInfo);
        List<ExecuteBuildingBlock> configBuildingBlocks = getConfigBuildingBlocks(cbbdo);
        flowsToExecute.addAll(configBuildingBlocks);
    }

    private Resource getResourceKey(ServiceInstancesRequest sIRequest, WorkflowType resourceType) {
        String resourceId = "";
        ModelInfo modelInfo = sIRequest.getRequestDetails().getModelInfo();
        if (modelInfo != null) {
            if (modelInfo.getModelType().equals(ModelType.service)) {
                resourceId = modelInfo.getModelVersionId();
            } else {
                resourceId = modelInfo.getModelCustomizationId();
            }
        }
        return new Resource(resourceType, resourceId, true);
    }

    private String getCloudOwner(CloudConfiguration cloudConfiguration) {
        if (cloudConfiguration != null && cloudConfiguration.getCloudOwner() != null) {
            return cloudConfiguration.getCloudOwner();
        }
        logger.warn("cloud owner value not found in request details, it will be set as default");
        return environment.getProperty(DEFAULT_CLOUD_OWNER);
    }

    protected <T> List<T> getRelatedResourcesInVfModule(String vnfId, String vfModuleId, Class<T> resultClass,
            AAIObjectName name) {
        List<T> vnfcs = new ArrayList<>();
        AAIResourceUri uri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(vnfId).vfModule(vfModuleId));
        AAIResultWrapper vfModuleResultsWrapper = bbInputSetupUtils.getAAIResourceDepthOne(uri);
        Optional<Relationships> relationshipsOp = vfModuleResultsWrapper.getRelationships();
        if (relationshipsOp.isEmpty()) {
            logger.debug("No relationships were found for vfModule in AAI");
        } else {
            Relationships relationships = relationshipsOp.get();
            List<AAIResultWrapper> vnfcResultWrappers = relationships.getByType(name);
            for (AAIResultWrapper vnfcResultWrapper : vnfcResultWrappers) {
                Optional<T> vnfcOp = vnfcResultWrapper.asBean(resultClass);
                vnfcOp.ifPresent(vnfcs::add);
            }
        }
        return vnfcs;
    }

    protected <T> T getRelatedResourcesInVnfc(Vnfc vnfc, Class<T> resultClass, AAIObjectName name)
            throws VnfcMultipleRelationshipException {
        T configuration = null;
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().vnfc(vnfc.getVnfcName()));
        AAIResultWrapper vnfcResultsWrapper = bbInputSetupUtils.getAAIResourceDepthOne(uri);
        Optional<Relationships> relationshipsOp = vnfcResultsWrapper.getRelationships();
        if (relationshipsOp.isEmpty()) {
            logger.debug("No relationships were found for VNFC in AAI");
        } else {
            Relationships relationships = relationshipsOp.get();
            List<AAIResultWrapper> configurationResultWrappers =
                    this.getResultWrappersFromRelationships(relationships, name);
            if (configurationResultWrappers.size() > 1) {
                throw new VnfcMultipleRelationshipException(vnfc.getVnfcName());
            }
            if (!configurationResultWrappers.isEmpty()) {
                Optional<T> configurationOp = configurationResultWrappers.get(0).asBean(resultClass);
                if (configurationOp.isPresent()) {
                    configuration = configurationOp.get();
                }
            }
        }
        return configuration;
    }

    protected List<AAIResultWrapper> getResultWrappersFromRelationships(Relationships relationships,
            AAIObjectName name) {
        return relationships.getByType(name);
    }

    protected boolean isConfiguration(List<OrchestrationFlow> orchFlows) {
        for (OrchestrationFlow flow : orchFlows) {
            if (flow.getFlowName().contains(CONFIGURATION) && !"ConfigurationScaleOutBB".equals(flow.getFlowName())) {
                return true;
            }
        }
        return false;
    }

    protected List<ExecuteBuildingBlock> getConfigBuildingBlocks(ConfigBuildingBlocksDataObject dataObj)
            throws AAIEntityNotFoundException, VnfcMultipleRelationshipException {

        List<ExecuteBuildingBlock> flowsToExecuteConfigs = new ArrayList<>();
        List<OrchestrationFlow> result = dataObj.getOrchFlows().stream()
                .filter(item -> item.getFlowName().contains(FABRIC_CONFIGURATION)).collect(Collectors.toList());
        String vnfId = dataObj.getWorkflowResourceIds().getVnfId();
        String vfModuleId = dataObj.getWorkflowResourceIds().getVfModuleId();

        String vnfCustomizationUUID = bbInputSetupUtils.getAAIGenericVnf(vnfId).getModelCustomizationId();
        String vfModuleCustomizationUUID;
        org.onap.aai.domain.yang.VfModule aaiVfModule = bbInputSetupUtils.getAAIVfModule(vnfId, vfModuleId);

        if (aaiVfModule == null) {
            logger.error("No matching VfModule is found in Generic-Vnf in AAI for vnfId: {} and vfModuleId : {}", vnfId,
                    vfModuleId);
            throw new AAIEntityNotFoundException("No matching VfModule is found in Generic-Vnf in AAI for vnfId: "
                    + vnfId + " and vfModuleId : " + vfModuleId);
        } else {
            vfModuleCustomizationUUID = aaiVfModule.getModelCustomizationId();
        }
        String replaceVfModuleCustomizationUUID = "";
        String replaceVnfModuleCustomizationUUID = "";
        boolean isReplace = false;
        if (dataObj.getRequestAction().equalsIgnoreCase("replaceInstance")
                || dataObj.getRequestAction().equalsIgnoreCase("replaceInstanceRetainAssignments")) {
            for (RelatedInstanceList relatedInstList : dataObj.getRequestDetails().getRelatedInstanceList()) {
                RelatedInstance relatedInstance = relatedInstList.getRelatedInstance();
                if (relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf)) {
                    replaceVnfModuleCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationId();
                }
            }
            replaceVfModuleCustomizationUUID = dataObj.getRequestDetails().getModelInfo().getModelCustomizationId();
            isReplace = true;
        }

        List<org.onap.aai.domain.yang.Vnfc> vnfcs =
                getRelatedResourcesInVfModule(vnfId, vfModuleId, org.onap.aai.domain.yang.Vnfc.class, Types.VNFC);
        for (org.onap.aai.domain.yang.Vnfc vnfc : vnfcs) {
            WorkflowResourceIds workflowIdsCopy = SerializationUtils.clone(dataObj.getWorkflowResourceIds());
            org.onap.aai.domain.yang.Configuration configuration =
                    getRelatedResourcesInVnfc(vnfc, org.onap.aai.domain.yang.Configuration.class, Types.CONFIGURATION);
            if (configuration == null) {
                logger.warn(String.format("No configuration found for VNFC %s in AAI", vnfc.getVnfcName()));
                continue;
            }
            workflowIdsCopy.setConfigurationId(configuration.getConfigurationId());
            for (OrchestrationFlow orchFlow : result) {
                if (!isReplace || (isReplace && (orchFlow.getFlowName().contains("Delete")))) {
                    if (!isReplace) {
                        dataObj.getResourceKey().setVfModuleCustomizationId(vfModuleCustomizationUUID);
                        dataObj.getResourceKey().setVnfCustomizationId(vnfCustomizationUUID);
                    } else {
                        if (orchFlow.getFlowName().contains("Delete")) {
                            dataObj.getResourceKey().setVfModuleCustomizationId(vfModuleCustomizationUUID);
                            dataObj.getResourceKey().setVnfCustomizationId(vnfCustomizationUUID);
                        } else {
                            dataObj.getResourceKey().setVfModuleCustomizationId(replaceVfModuleCustomizationUUID);
                            dataObj.getResourceKey().setVnfCustomizationId(replaceVnfModuleCustomizationUUID);
                        }
                    }
                    dataObj.getResourceKey().setCvnfModuleCustomizationId(vnfc.getModelCustomizationId());
                    String vnfcName = vnfc.getVnfcName();
                    if (vnfcName == null || vnfcName.isEmpty()) {
                        buildAndThrowException(dataObj.getExecution(), "Exception in create execution list "
                                + ": VnfcName does not exist or is null while there is a configuration for the vfModule",
                                new Exception("Vnfc and Configuration do not match"));
                    }
                    ExecuteBuildingBlock ebb =
                            executeBuildingBlockBuilder.buildExecuteBuildingBlock(orchFlow, dataObj.getRequestId(),
                                    dataObj.getResourceKey(), dataObj.getApiVersion(), dataObj.getResourceId(),
                                    dataObj.getRequestAction(), dataObj.isaLaCarte(), dataObj.getVnfType(),
                                    workflowIdsCopy, dataObj.getRequestDetails(), false, null, vnfcName, true, null);
                    flowsToExecuteConfigs.add(ebb);
                }
            }
        }
        return flowsToExecuteConfigs;
    }

    protected void buildAndThrowException(DelegateExecution execution, String msg) {
        logger.error(msg);
        execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg);
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
    }

    protected void buildAndThrowException(DelegateExecution execution, String msg, Exception ex) {
        logger.error(msg, ex);
        execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg + ex.getMessage());
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg + ex.getMessage());
    }

    protected List<OrchestrationFlow> getVfModuleReplaceBuildingBlocks(ConfigBuildingBlocksDataObject dataObj)
            throws Exception {

        String vnfId = dataObj.getWorkflowResourceIds().getVnfId();
        String vfModuleId = dataObj.getWorkflowResourceIds().getVfModuleId();

        logger.debug("BUILDING REPLACE LIST");

        boolean volumeGroupExisted = false;
        boolean volumeGroupWillExist = false;
        boolean keepVolumeGroup = false;

        boolean rebuildVolumeGroups = false;
        if (dataObj.getRequestDetails().getRequestParameters() != null
                && dataObj.getRequestDetails().getRequestParameters().getRebuildVolumeGroups() != null) {
            rebuildVolumeGroups = dataObj.getRequestDetails().getRequestParameters().getRebuildVolumeGroups();
        }
        String volumeGroupName = "";
        Optional<VolumeGroup> volumeGroupFromVfModule =
                bbInputSetupUtils.getRelatedVolumeGroupFromVfModule(vnfId, vfModuleId);
        if (volumeGroupFromVfModule.isPresent()) {
            String volumeGroupId = volumeGroupFromVfModule.get().getVolumeGroupId();
            volumeGroupName = volumeGroupFromVfModule.get().getVolumeGroupName();
            logger.debug("Volume group id of the existing volume group is: {}", volumeGroupId);
            volumeGroupExisted = true;
            dataObj.getWorkflowResourceIds().setVolumeGroupId(volumeGroupId);
            dataObj.getReplaceInformation().setOldVolumeGroupName(volumeGroupName);
        }

        List<OrchestrationFlow> orchFlows = dataObj.getOrchFlows();
        VfModuleCustomization vfModuleCustomization = catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(
                dataObj.getRequestDetails().getModelInfo().getModelCustomizationUuid());
        if (vfModuleCustomization != null && vfModuleCustomization.getVfModule() != null
                && vfModuleCustomization.getVfModule().getVolumeHeatTemplate() != null
                && vfModuleCustomization.getVolumeHeatEnv() != null) {
            volumeGroupWillExist = true;
            if (!volumeGroupExisted) {
                String newVolumeGroupId = UUID.randomUUID().toString();
                dataObj.getWorkflowResourceIds().setVolumeGroupId(newVolumeGroupId);
                dataObj.getReplaceInformation().setOldVolumeGroupName(volumeGroupName);
                logger.debug("newVolumeGroupId: {}", newVolumeGroupId);
            }
        }

        if (volumeGroupExisted && volumeGroupWillExist && !rebuildVolumeGroups) {
            keepVolumeGroup = true;
        }

        if (!volumeGroupExisted || keepVolumeGroup) {
            logger.debug("Filtering out deletion of volume groups");
            orchFlows = orchFlows.stream().filter(item -> !item.getFlowName().matches(VOLUMEGROUP_DELETE_PATTERN))
                    .collect(Collectors.toList());
        }
        if (!volumeGroupWillExist || keepVolumeGroup) {
            logger.debug("Filtering out creation of volume groups");
            orchFlows = orchFlows.stream().filter(item -> !item.getFlowName().matches(VOLUMEGROUP_CREATE_PATTERN))
                    .collect(Collectors.toList());
        }

        return orchFlows;
    }



    private void updateResourceIdsFromAAITraversal(List<ExecuteBuildingBlock> flowsToExecute,
            List<Resource> resourceList, List<Pair<WorkflowType, String>> aaiResourceIds, String serviceInstanceId) {
        for (Pair<WorkflowType, String> pair : aaiResourceIds) {
            logger.debug("{}, {}", pair.getValue0(), pair.getValue1());
        }

        Arrays.stream(WorkflowType.values()).filter(type -> !type.equals(WorkflowType.SERVICE))
                .forEach(type -> resourceList.stream().filter(resource -> type.equals(resource.getResourceType()))
                        .forEach(resource -> updateWorkflowResourceIds(flowsToExecute, type, resource.getResourceId(),
                                retrieveAAIResourceId(aaiResourceIds, type), null, serviceInstanceId)));
    }

    private String retrieveAAIResourceId(List<Pair<WorkflowType, String>> aaiResourceIds, WorkflowType resource) {
        String id = null;
        for (int i = 0; i < aaiResourceIds.size(); i++) {
            if (aaiResourceIds.get(i).getValue0() == resource) {
                id = aaiResourceIds.get(i).getValue1();
                aaiResourceIds.remove(i);
                break;
            }
        }
        return id;
    }

    private void generateResourceIds(List<ExecuteBuildingBlock> flowsToExecute, List<Resource> resourceList,
            String serviceInstanceId) {
        Arrays.stream(WorkflowType.values()).filter(type -> !type.equals(WorkflowType.SERVICE))
                .forEach(type -> resourceList.stream().filter(resource -> type.equals(resource.getResourceType()))
                        .forEach(resource -> updateWorkflowResourceIds(flowsToExecute, type, resource.getResourceId(),
                                null, resource.getVirtualLinkKey(), serviceInstanceId)));
    }

    protected void updateWorkflowResourceIds(List<ExecuteBuildingBlock> flowsToExecute, WorkflowType resourceType,
            String key, String id, String virtualLinkKey, String serviceInstanceId) {
        String resourceId = id;
        if (resourceId == null) {
            resourceId = UUID.randomUUID().toString();
        }
        for (ExecuteBuildingBlock ebb : flowsToExecute) {
            if (key != null && key.equalsIgnoreCase(ebb.getBuildingBlock().getKey()) && (ebb.getBuildingBlock()
                    .getBpmnFlowName().contains(resourceType.toString())
                    || (ebb.getBuildingBlock().getBpmnFlowName().contains(CONTROLLER)
                            && ebb.getBuildingBlock().getBpmnScope().equalsIgnoreCase(resourceType.toString())))) {
                WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
                workflowResourceIds.setServiceInstanceId(serviceInstanceId);
                WorkflowResourceIdsUtils.setResourceIdByWorkflowType(workflowResourceIds, resourceType, resourceId);
                ebb.setWorkflowResourceIds(workflowResourceIds);
            }
            if (virtualLinkKey != null && ebb.getBuildingBlock().isVirtualLink()
                    && virtualLinkKey.equalsIgnoreCase(ebb.getBuildingBlock().getVirtualLinkKey())) {
                WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
                workflowResourceIds.setServiceInstanceId(serviceInstanceId);
                workflowResourceIds.setNetworkId(resourceId);
                ebb.setWorkflowResourceIds(workflowResourceIds);
            }
        }
    }

    protected CollectionResourceCustomization findCatalogNetworkCollection(DelegateExecution execution,
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

    protected void traverseCatalogDbService(DelegateExecution execution, ServiceInstancesRequest sIRequest,
            List<Resource> resourceList, List<Pair<WorkflowType, String>> aaiResourceIds)
            throws JsonProcessingException, VrfBondingServiceException {
        String modelUUID = sIRequest.getRequestDetails().getModelInfo().getModelVersionId();
        org.onap.so.db.catalog.beans.Service service = catalogDbClient.getServiceByID(modelUUID);

        if (service == null) {
            buildAndThrowException(execution, "Could not find the service model in catalog db.");
        } else {
            resourceList.add(new Resource(WorkflowType.SERVICE, service.getModelUUID(), false));
            RelatedInstance relatedVpnBinding =
                    bbInputSetupUtils.getRelatedInstanceByType(sIRequest.getRequestDetails(), ModelType.vpnBinding);
            RelatedInstance relatedLocalNetwork =
                    bbInputSetupUtils.getRelatedInstanceByType(sIRequest.getRequestDetails(), ModelType.network);

            if (relatedVpnBinding != null && relatedLocalNetwork != null) {
                traverseVrfConfiguration(aaiResourceIds, resourceList, service, relatedVpnBinding, relatedLocalNetwork);
            } else {
                traverseNetworkCollection(execution, resourceList, service);
            }
        }
    }

    protected void traverseVrfConfiguration(List<Pair<WorkflowType, String>> aaiResourceIds,
            List<Resource> resourceList, org.onap.so.db.catalog.beans.Service service,
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
                service.getConfigurationCustomizations().get(0).getModelCustomizationUUID(), false));

    }

    protected String getExistingAAIVrfConfiguration(RelatedInstance relatedVpnBinding,
            org.onap.aai.domain.yang.L3Network aaiLocalNetwork)
            throws JsonProcessingException, VrfBondingServiceException {
        Optional<Relationships> relationshipsOp = new AAIResultWrapper(
                new AAICommonObjectMapperProvider().getMapper().writeValueAsString(aaiLocalNetwork)).getRelationships();
        if (relationshipsOp.isPresent()) {
            List<AAIResultWrapper> configurationsRelatedToLocalNetwork =
                    relationshipsOp.get().getByType(Types.CONFIGURATION);
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

    protected void traverseNetworkCollection(DelegateExecution execution, List<Resource> resourceList,
            org.onap.so.db.catalog.beans.Service service) {
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
            traverseNetworkCollectionResourceCustomization(resourceList, collectionResourceCustomization);
        }
        traverseNetworkCollectionCustomization(resourceList, service);
    }

    private void traverseNetworkCollectionResourceCustomization(List<Resource> resourceList,
            CollectionResourceCustomization collectionResourceCustomization) {
        if (collectionResourceCustomizationShouldNotBeProcessed(resourceList, collectionResourceCustomization))
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
                        collectionNetworkResourceCust.getModelCustomizationUUID(), false);
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
            CollectionResourceCustomization collectionResourceCustomization) {
        if (collectionResourceCustomization == null) {
            logger.debug("No Network Collection Customization found");
            return true;
        }
        resourceList.add(new Resource(WorkflowType.NETWORKCOLLECTION,
                collectionResourceCustomization.getModelCustomizationUUID(), false));
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

    private void traverseNetworkCollectionCustomization(List<Resource> resourceList,
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
                    service.getNetworkCustomizations().get(i).getModelCustomizationUUID(), false));
        }
    }

    private boolean isNetworkCollectionInTheResourceList(List<Resource> resourceList) {
        return resourceList.stream().anyMatch(x -> WorkflowType.NETWORKCOLLECTION == x.getResourceType());
    }

    private boolean isVnfCustomizationsInTheService(org.onap.so.db.catalog.beans.Service service) {
        return !(service.getVnfCustomizations() == null || service.getVnfCustomizations().isEmpty());
    }

    private boolean isPnfCustomizationsInTheService(org.onap.so.db.catalog.beans.Service service) {
        return !(service.getPnfCustomizations() == null || service.getPnfCustomizations().isEmpty());
    }

    protected void traverseAAIService(DelegateExecution execution, List<Resource> resourceList, String resourceId,
            List<Pair<WorkflowType, String>> aaiResourceIds) {
        try {
            ServiceInstance serviceInstanceAAI = bbInputSetupUtils.getAAIServiceInstanceById(resourceId);
            org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO =
                    bbInputSetup.getExistingServiceInstance(serviceInstanceAAI);
            resourceList.add(new Resource(WorkflowType.SERVICE, serviceInstanceMSO.getServiceInstanceId(), false));
            traverseServiceInstanceMSOVnfs(resourceList, aaiResourceIds, serviceInstanceMSO);
            traverseServiceInstanceMSOPnfs(resourceList, aaiResourceIds, serviceInstanceMSO);
            if (serviceInstanceMSO.getNetworks() != null) {
                for (org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network network : serviceInstanceMSO
                        .getNetworks()) {
                    aaiResourceIds.add(new Pair<>(WorkflowType.NETWORK, network.getNetworkId()));
                    resourceList.add(new Resource(WorkflowType.NETWORK, network.getNetworkId(), false));
                }
            }
            if (serviceInstanceMSO.getCollection() != null) {
                logger.debug("found networkcollection");
                aaiResourceIds
                        .add(new Pair<>(WorkflowType.NETWORKCOLLECTION, serviceInstanceMSO.getCollection().getId()));
                resourceList.add(new Resource(WorkflowType.NETWORKCOLLECTION,
                        serviceInstanceMSO.getCollection().getId(), false));
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
                                resourceList.add(
                                        new Resource(WorkflowType.CONFIGURATION, config.getConfigurationId(), false));
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

    private void traverseServiceInstanceMSOVnfs(List<Resource> resourceList,
            List<Pair<WorkflowType, String>> aaiResourceIds,
            org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO) {
        if (serviceInstanceMSO.getVnfs() == null) {
            return;
        }
        for (org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf : serviceInstanceMSO.getVnfs()) {
            aaiResourceIds.add(new Pair<>(WorkflowType.VNF, vnf.getVnfId()));
            resourceList.add(new Resource(WorkflowType.VNF, vnf.getVnfId(), false));
            traverseVnfModules(resourceList, aaiResourceIds, vnf);
            if (vnf.getVolumeGroups() != null) {
                for (org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup : vnf.getVolumeGroups()) {
                    aaiResourceIds.add(new Pair<>(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId()));
                    resourceList.add(new Resource(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId(), false));
                }
            }
        }
    }

    private void traverseServiceInstanceMSOPnfs(List<Resource> resourceList,
            List<Pair<WorkflowType, String>> aaiResourceIds,
            org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO) {
        if (serviceInstanceMSO.getPnfs() == null) {
            return;
        }
        for (org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf pnf : serviceInstanceMSO.getPnfs()) {
            aaiResourceIds.add(new Pair<>(WorkflowType.PNF, pnf.getPnfId()));
            resourceList.add(new Resource(WorkflowType.PNF, pnf.getPnfId(), false));
        }
    }

    private void traverseVnfModules(List<Resource> resourceList, List<Pair<WorkflowType, String>> aaiResourceIds,
            org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf) {
        if (vnf.getVfModules() == null) {
            return;
        }
        for (VfModule vfModule : vnf.getVfModules()) {
            aaiResourceIds.add(new Pair<>(WorkflowType.VFMODULE, vfModule.getVfModuleId()));
            Resource resource = new Resource(WorkflowType.VFMODULE, vfModule.getVfModuleId(), false);
            resource.setBaseVfModule(vfModule.getModelInfoVfModule().getIsBaseBoolean());
            resourceList.add(resource);
        }
    }



    protected WorkflowResourceIds populateResourceIdsFromApiHandler(DelegateExecution execution) {
        return WorkflowResourceIdsUtils.getWorkflowResourceIdsFromExecution(execution);
    }

    protected Resource extractResourceIdAndTypeFromUri(String uri) {
        Pattern patt = Pattern.compile("[vV]\\d+.*?(?:(?:/(?<type>" + SUPPORTEDTYPES
                + ")(?:/(?<id>[^/]+))?)(?:/(?<action>[^/]+))?(?:/resume)?)?$");
        Matcher m = patt.matcher(uri);
        boolean generated = false;

        if (m.find()) {
            logger.debug("found match on {} : {} ", uri, m);
            String type = m.group("type");
            String id = m.group("id");
            String action = m.group("action");
            if (type == null) {
                throw new IllegalArgumentException("Uri could not be parsed. No type found. " + uri);
            }
            if (action == null) {
                if (type.equals(SERVICE_INSTANCES) && (id == null || "assign".equals(id))) {
                    id = UUID.randomUUID().toString();
                    generated = true;
                } else if (type.equals(VF_MODULES) && "scaleOut".equals(id)) {
                    id = UUID.randomUUID().toString();
                    generated = true;
                }
            } else {
                if (action.matches(SUPPORTEDTYPES)) {
                    id = UUID.randomUUID().toString();
                    generated = true;
                    type = action;
                }
            }
            return new Resource(WorkflowType.fromString(convertTypeFromPlural(type)), id, generated);
        } else {
            throw new IllegalArgumentException("Uri could not be parsed: " + uri);
        }
    }

    protected String convertTypeFromPlural(String type) {
        if (!type.matches(SUPPORTEDTYPES)) {
            return type;
        } else {
            if (type.equals(SERVICE_INSTANCES)) {
                return SERVICE;
            } else {
                return type.substring(0, 1).toUpperCase() + type.substring(1, type.length() - 1);
            }
        }
    }

    protected List<ExecuteBuildingBlock> sortExecutionPathByObjectForVlanTagging(List<ExecuteBuildingBlock> orchFlows,
            String requestAction) {
        List<ExecuteBuildingBlock> sortedOrchFlows = new ArrayList<>();
        if (requestAction.equals(CREATE_INSTANCE)) {
            for (ExecuteBuildingBlock ebb : orchFlows) {
                if (ebb.getBuildingBlock().getBpmnFlowName().equals("AssignNetworkBB")) {
                    String key = ebb.getBuildingBlock().getKey();
                    boolean isVirtualLink = Boolean.TRUE.equals(ebb.getBuildingBlock().isVirtualLink());
                    String virtualLinkKey = ebb.getBuildingBlock().getVirtualLinkKey();
                    sortedOrchFlows.add(ebb);
                    for (ExecuteBuildingBlock ebb2 : orchFlows) {
                        if (!isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals(CREATENETWORKBB)
                                && ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                        if (isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals(CREATENETWORKBB)
                                && ebb2.getBuildingBlock().getVirtualLinkKey().equalsIgnoreCase(virtualLinkKey)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                    }
                    for (ExecuteBuildingBlock ebb2 : orchFlows) {
                        if (!isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals(ACTIVATENETWORKBB)
                                && ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                        if (isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals(ACTIVATENETWORKBB)
                                && ebb2.getBuildingBlock().getVirtualLinkKey().equalsIgnoreCase(virtualLinkKey)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                    }
                } else if (ebb.getBuildingBlock().getBpmnFlowName().equals(CREATENETWORKBB)
                        || ebb.getBuildingBlock().getBpmnFlowName().equals(ACTIVATENETWORKBB)) {
                    continue;
                } else if (!"".equals(ebb.getBuildingBlock().getBpmnFlowName())) {
                    sortedOrchFlows.add(ebb);
                }
            }
        } else if (requestAction.equals("deleteInstance")) {
            for (ExecuteBuildingBlock ebb : orchFlows) {
                if (ebb.getBuildingBlock().getBpmnFlowName().equals("DeactivateNetworkBB")) {
                    sortedOrchFlows.add(ebb);
                    String key = ebb.getBuildingBlock().getKey();
                    for (ExecuteBuildingBlock ebb2 : orchFlows) {
                        if (ebb2.getBuildingBlock().getBpmnFlowName().equals("DeleteNetworkBB")
                                && ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                    }
                    for (ExecuteBuildingBlock ebb2 : orchFlows) {
                        if (ebb2.getBuildingBlock().getBpmnFlowName().equals("UnassignNetworkBB")
                                && ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
                            sortedOrchFlows.add(ebb2);
                            break;
                        }
                    }
                } else if (ebb.getBuildingBlock().getBpmnFlowName().equals("DeleteNetworkBB")
                        || ebb.getBuildingBlock().getBpmnFlowName().equals("UnassignNetworkBB")) {
                    continue;
                } else if (!ebb.getBuildingBlock().getBpmnFlowName().equals("")) {
                    sortedOrchFlows.add(ebb);
                }
            }
        }
        return sortedOrchFlows;
    }

    protected List<OrchestrationFlow> queryNorthBoundRequestCatalogDb(DelegateExecution execution, String requestAction,
            WorkflowType resourceName, boolean aLaCarte, String cloudOwner) {
        return this.queryNorthBoundRequestCatalogDb(execution, requestAction, resourceName, aLaCarte, cloudOwner, "");
    }

    protected List<OrchestrationFlow> queryNorthBoundRequestCatalogDb(DelegateExecution execution, String requestAction,
            WorkflowType resourceName, boolean aLaCarte, String cloudOwner, String serviceType) {
        List<OrchestrationFlow> listToExecute = new ArrayList<>();
        NorthBoundRequest northBoundRequest;
        if (serviceType.equalsIgnoreCase(SERVICE_TYPE_TRANSPORT)
                || serviceType.equalsIgnoreCase(SERVICE_TYPE_BONDING)) {
            northBoundRequest =
                    catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwnerAndServiceType(
                            requestAction, resourceName.toString(), aLaCarte, cloudOwner, serviceType);
        } else {
            northBoundRequest = catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(
                    requestAction, resourceName.toString(), aLaCarte, cloudOwner);
        }
        if (northBoundRequest == null) {
            northBoundRequest = catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(
                    requestAction, resourceName.toString(), aLaCarte, CLOUD_OWNER);
        }
        if (northBoundRequest == null) {
            buildAndThrowException(execution, String.format("The request: %s %s %s is not supported by GR_API.",
                    (aLaCarte ? "AlaCarte" : "Macro"), resourceName, requestAction));
        } else {
            if (northBoundRequest.getIsToplevelflow() != null) {
                execution.setVariable(BBConstants.G_ISTOPLEVELFLOW, northBoundRequest.getIsToplevelflow());
            }
            List<OrchestrationFlow> flows = northBoundRequest.getOrchestrationFlowList();
            if (flows == null) {
                flows = new ArrayList<>();
            } else {
                flows.sort(Comparator.comparingInt(OrchestrationFlow::getSequenceNumber));
            }
            for (OrchestrationFlow flow : flows) {
                if (!flow.getFlowName().contains("BB") && !flow.getFlowName().contains("Activity")) {
                    List<OrchestrationFlow> macroQueryFlows =
                            catalogDbClient.getOrchestrationFlowByAction(flow.getFlowName());
                    listToExecute.addAll(macroQueryFlows);
                } else {
                    listToExecute.add(flow);
                }
            }
        }
        return listToExecute;
    }

    public void handleRuntimeException(DelegateExecution execution) {
        StringBuilder wfeExpMsg = new StringBuilder("Runtime error ");
        String runtimeErrorMessage;
        try {
            String javaExpMsg = (String) execution.getVariable("BPMN_javaExpMsg");
            if (javaExpMsg != null && !javaExpMsg.isEmpty()) {
                wfeExpMsg.append(": ").append(javaExpMsg);
            }
            runtimeErrorMessage = wfeExpMsg.toString();
            logger.error(runtimeErrorMessage);
            execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, runtimeErrorMessage);
        } catch (Exception e) {
            logger.error("Runtime error", e);
            // if runtime message was mulformed
            runtimeErrorMessage = "Runtime error";
        }
        exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, runtimeErrorMessage);
    }

    protected boolean isUriResume(String uri) {
        return uri.endsWith("/resume");
    }

    protected boolean isRequestMacroServiceResume(boolean aLaCarte, WorkflowType resourceType, String requestAction,
            String serviceInstanceId) {
        return (!aLaCarte && resourceType == WorkflowType.SERVICE
                && (requestAction.equalsIgnoreCase(ASSIGNINSTANCE) || requestAction.equalsIgnoreCase(CREATE_INSTANCE))
                && (serviceInstanceId != null && serviceInstanceId.trim().length() > 1)
                && (bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId) != null));
    }

    protected boolean foundRelated(List<Resource> resourceList) {
        return (containsWorkflowType(resourceList, WorkflowType.VNF)
                || containsWorkflowType(resourceList, WorkflowType.PNF)
                || containsWorkflowType(resourceList, WorkflowType.NETWORK)
                || containsWorkflowType(resourceList, WorkflowType.NETWORKCOLLECTION));
    }

    protected boolean containsWorkflowType(List<Resource> resourceList, WorkflowType workflowType) {
        return resourceList.stream().anyMatch(resource -> resource.getResourceType().equals(workflowType));
    }

    private void fillExecutionDefault(DelegateExecution execution) {
        execution.setVariable("sentSyncResponse", false);
        execution.setVariable(HOMING, false);
        execution.setVariable("calledHoming", false);
        execution.setVariable(BBConstants.G_ISTOPLEVELFLOW, true);
    }

    private void fillExecution(DelegateExecution execution, boolean suppressRollback, String resourceId,
            WorkflowType resourceType) {
        execution.setVariable("suppressRollback", suppressRollback);
        execution.setVariable("resourceId", resourceId);
        execution.setVariable("resourceType", resourceType);
        execution.setVariable("resourceName", resourceType.toString());
    }

    private Resource getResource(BBInputSetupUtils bbInputSetupUtils, boolean isResume, boolean alaCarte, String uri,
            String requestId) {
        if (!alaCarte && isResume) {
            logger.debug("replacing URI {}", uri);
            uri = bbInputSetupUtils.loadOriginalInfraActiveRequestById(requestId).getRequestUrl();
            logger.debug("for RESUME with original value {}", uri);
        }
        return extractResourceIdAndTypeFromUri(uri);
    }

    private String getResourceId(Resource resource, String requestAction, RequestDetails requestDetails,
            WorkflowResourceIds workflowResourceIds) throws Exception {
        if (resource.isGenerated() && requestAction.equalsIgnoreCase("createInstance")
                && requestDetails.getRequestInfo().getInstanceName() != null) {
            return aaiResourceIdValidator.validateResourceIdInAAI(resource.getResourceId(), resource.getResourceType(),
                    requestDetails.getRequestInfo().getInstanceName(), requestDetails, workflowResourceIds);
        } else {
            return resource.getResourceId();
        }
    }

    private String getServiceInstanceId(DelegateExecution execution, String resourceId, WorkflowType resourceType) {
        String serviceInstanceId = (String) execution.getVariable("serviceInstanceId");
        if ((serviceInstanceId == null || serviceInstanceId.isEmpty()) && WorkflowType.SERVICE.equals(resourceType)) {
            serviceInstanceId = resourceId;
        }
        return serviceInstanceId;
    }

}
